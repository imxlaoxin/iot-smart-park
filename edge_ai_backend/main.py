import cv2
import threading
import time
import requests
import json
import hyperlpr3 as lpr3
from ultralytics import YOLO
# 引入 YOLO 官方底层的画笔和取色器工具
from ultralytics.utils.plotting import Annotator, colors
import os

# --- WebRTC 与 FastAPI 相关包 ---
import fractions
import asyncio
from contextlib import asynccontextmanager
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from aiortc import MediaStreamTrack, RTCPeerConnection, RTCSessionDescription
from av import VideoFrame

os.environ["OPENCV_FFMPEG_CAPTURE_OPTIONS"] = "rtsp_transport;udp|buffer_size;1024|rw_timeout;5000000"

# ==================== 1. 业务配置中心 ====================
JAVA_SERVER = "http://localhost:8080"
JAVA_EXP_DETECT_API = f"{JAVA_SERVER}/iot/detect/exp-detect-record"
JAVA_BIZ_DETECT_API = f"{JAVA_SERVER}/iot/detect/biz-detect-record"

SERVER_LAN_IP = "10.46.100.210"

# 定义三模型路径
INNER_MODEL_PATH = "../../models/inner.pt"  # 内部模型 (640)
INNER_MODEL_PREDICT_SIZE = 640
OUTER_MODEL_PATH = "../../models/outer-2.pt"  # 出入口模型 (960)
OUTER_MODEL_PREDICT_SIZE = 960
ENV_MODEL_PATH = "../../models/env.pt"  # 异常环境模型 (640)
ENV_MODEL_PREDICT_SIZE = 640

# 模型置信度阈值配置 (核心调优参数)
INNER_BIZ_CONF = 0.15  # 内部天眼 (车辆检测) 及格线：45%
OUTER_BIZ_CONF = 0.55  # 出入口门神 (车牌框检测) 及格线：60%
ENV_FIRE_CONF = 0.55  # 环境异常 (火灾检测) 及格线：50%
LPR_OCR_CONF = 0.90  # 车牌 OCR 认字及格线 (保留原有)

EXIT_MIN_PLATE_WIDTH = 110  # 摄像头 2 (出口) 的触发宽度
ENTRANCE_MIN_PLATE_WIDTH = 110  # 摄像头 3 (入口) 的触发宽度

fire_report_interval = 30  # 30 秒内不重复报警

is_detect = False  # 是否开启前端实时视频检测框的展示

CAMERAS = {
    "1": "rtsp://169.254.15.12:554/user=admin&password=&channel=1&state=0.sdp?",
    "2": "rtsp://169.254.129.11:554/user=admin&password=&channel=1&state=0.sdp?",
    "3": "rtsp://169.254.129.12:554/user=admin&password=&channel=1&state=0.sdp?",
}


# ==================== 2. 核心抓取与分析类 ====================
class GlobalCameraManager:
    # 接收专属的 predict_size 参数
    def __init__(self, cam_id, rtsp_url, model, lpr_catcher, predict_size, biz_conf=0.45, env_conf=0.50, env_model=None,
                 env_predict_size=640):
        self.cam_id = cam_id
        self.rtsp_url = rtsp_url
        self.model = model
        self.lpr_catcher = lpr_catcher
        self.predict_size = predict_size  # 保存专属分辨率
        self.biz_conf = biz_conf  # 业务置信度
        # 环境检测参数
        self.env_conf = env_conf  # 环境置信度
        self.env_model = env_model
        self.env_predict_size = env_predict_size
        self.last_env_infer_time = 0  # 用于控制环境模型的推理频率(降频节约算力)

        # 火灾框渲染缓存 (解决闪烁问题)
        self.cached_env_results = None
        # 火灾边缘状态机 (解决警报解除问题)
        self.is_fire_alarming = False  # 判断当前是否处于火灾警报状态
        self.last_fire_seen_time = 0  # 最后一次肉眼看到火的绝对时间
        self.last_fire_report_time = 0  # 最后一次向后端发送报警API的绝对时间

        if isinstance(rtsp_url, int) or (isinstance(rtsp_url, str) and rtsp_url.isdigit()):
            self.cap = cv2.VideoCapture(int(rtsp_url))
        else:
            self.cap = cv2.VideoCapture(rtsp_url, cv2.CAP_FFMPEG)

        self.cap.set(cv2.CAP_PROP_BUFFERSIZE, 1)
        self.raw_frame = None
        self.display_frame = None
        self.stopped = False
        self.lock = threading.Lock()

        self.latest_results = None
        self.ai_render_frame = None  # 专门用于存放已经画好的框的图像
        self.triggered_tracks = set()
        self.last_fire_time = 0

        self.is_detect = False  # 控制每个摄像头的实时框的显示效果

        self.is_recording = False
        self.video_writer = None
        self.record_filename = ""

    def start(self):
        threading.Thread(target=self._capture_loop, daemon=True).start()
        threading.Thread(target=self._ai_analysis_loop, daemon=True).start()
        print(f"摄像头 {self.cam_id} AI 业务引擎已启动 (推理分辨率: {self.predict_size})")
        return self

    def _capture_loop(self):
        while not self.stopped:
            if not self.cap.grab():
                time.sleep(0.5)
                self.cap.open(self.rtsp_url)
                continue
            success, frame = self.cap.retrieve()
            if success:
                with self.lock:
                    self.raw_frame = frame
                    self.display_frame = frame.copy()
                    if self.is_recording and self.video_writer is not None:
                        self.video_writer.write(frame)
            else:
                time.sleep(0.01)

    def _ai_analysis_loop(self):
        while not self.stopped:
            frame = self.get_raw_frame()
            if frame is not None:
                try:
                    # 1. 常规业务模型极速推理 (追踪车辆)
                    results = self.model.track(frame, conf=self.biz_conf, imgsz=self.predict_size, persist=True,
                                               verbose=False)

                    plotted_frame = frame.copy()
                    if results and results[0].boxes:
                        annotator = Annotator(plotted_frame, line_width=2)
                        for box in results[0].boxes:
                            b = box.xyxy[0].cpu().numpy()
                            c = int(box.cls[0])
                            conf = float(box.conf[0])
                            name = results[0].names[c]
                            label = f"{name} {conf:.2f}"
                            annotator.box_label(b, label, color=colors(c, True))
                        plotted_frame = annotator.result()

                    # 2. 环境异常模型降频推理 & 状态机 & 渲染缓存
                    if self.env_model is not None:
                        current_time = time.time()

                        # 【A. 降频推理 (1秒1次)】
                        if current_time - self.last_env_infer_time > 1.0:
                            self.last_env_infer_time = current_time
                            env_results = self.env_model.predict(frame, conf=self.env_conf, imgsz=self.env_predict_size,
                                                                 verbose=False)

                            # 更新缓存用于画框
                            if env_results and len(env_results[0].boxes) > 0:
                                self.cached_env_results = env_results[0]
                            else:
                                self.cached_env_results = None

                            # 每次推理后，将结果交给“状态机”去研判是否要发请求给后端
                            self._process_env_state_machine(frame, self.cached_env_results, plotted_frame, current_time)

                        # 【B. 全帧率渲染缓存 (解决闪烁)】
                        # 无论这一帧有没有做推理，只要缓存里有火，就死死把它画在屏幕上！
                        if self.cached_env_results is not None:
                            env_annotator = Annotator(plotted_frame, line_width=3)
                            for box in self.cached_env_results.boxes:
                                b = box.xyxy[0].cpu().numpy()
                                c = int(box.cls[0])
                                conf = float(box.conf[0])
                                name = self.cached_env_results.names[c]
                                label = f"{name.upper()} {conf:.2f}"
                                # 强制使用显眼的纯红色 (BGR: 0,0,255)
                                env_annotator.box_label(b, label, color=(0, 0, 255))
                            plotted_frame = env_annotator.result()
                    # ====================================================

                    with self.lock:
                        self.ai_render_frame = plotted_frame

                        # 3. 触发常规业务逻辑
                    if results and results[0].boxes:
                        self._process_logic(frame, results[0], plotted_frame)

                except Exception as e:
                    print(f"⚠️ AI 分析线程发生异常: {e}")
            time.sleep(0.04)

    # 火灾状态机研判中心
    def _process_env_state_machine(self, frame, res, det_frame, current_time):
        has_fire_in_current_frame = False
        highest_conf = 0.0

        # 判断当前这1秒的推断里，到底有没有火
        if res and res.boxes:
            for box in res.boxes:
                name = res.names[int(box.cls[0])]
                if name == 'fire':
                    has_fire_in_current_frame = True
                    highest_conf = max(highest_conf, float(box.conf[0]))

        if has_fire_in_current_frame:
            # 记录最后一次看到火的绝对时间
            self.last_fire_seen_time = current_time

            if not self.is_fire_alarming:
                # 状态流转：平时没火 -> 突然起火
                self.is_fire_alarming = True
                self.last_fire_report_time = current_time
                print(f"摄像机 {self.cam_id} 突发火灾！立即上报后端...")
                threading.Thread(target=self.capture_and_send,
                                 args=(frame, det_frame, 'fire', "exp", highest_conf)).start()
            else:
                # 状态维持：一直在烧
                if current_time - self.last_fire_report_time > fire_report_interval:
                    self.last_fire_report_time = current_time
                    print(f"摄像机 {self.cam_id} 火灾持续中！再次上报后端...")
                    threading.Thread(target=self.capture_and_send,
                                     args=(frame, det_frame, 'fire', "exp", highest_conf)).start()
        else:
            # 当前帧没看到火
            if self.is_fire_alarming:
                # 消亡机制：如果已经超过 10 秒没看到火了，说明火被扑灭了 (或者是误报由于人走开了消失了)
                if current_time - self.last_fire_seen_time > 10.0:
                    self.is_fire_alarming = False
                    print(f"摄像机 {self.cam_id} 火灾已消失超过 10 秒，上报后端解除警报！")
                    # 发送特殊的 'fire_clear' 事件！
                    threading.Thread(target=self.capture_and_send,
                                     args=(frame, det_frame, 'fire_clear', "exp", 1.0)).start()

    # 原本的业务处理流，现在只干常规的活
    def _process_logic(self, frame, res, det_frame):
        for box in res.boxes:
            cls_id = int(box.cls[0])
            label = res.names[cls_id]
            conf = float(box.conf[0])
            xyxy = box.xyxy[0].cpu().numpy()
            t_id = int(box.id[0]) if box.id is not None else None

            # 内部天眼 (Cam 1) 的车辆逻辑 (原有的火灾逻辑已移除)
            if str(self.cam_id) == "1":
                # 未来可以在这里写判断车辆有没有压线违停的逻辑
                pass

                # 出入口门神 (Cam 2, 3) 处理车牌逻辑保持不变
            elif str(self.cam_id) in ["2", "3"]:
                if label == 'license-plate':
                    # ... (后面的 plate_width 和 _handle_plate 逻辑完全保持不变)
                    plate_width = xyxy[2] - xyxy[0]
                    current_min_width = EXIT_MIN_PLATE_WIDTH if str(
                        self.cam_id) == "2" else ENTRANCE_MIN_PLATE_WIDTH
                    if plate_width > current_min_width:
                        if t_id is not None and t_id not in self.triggered_tracks:
                            self._handle_plate(frame, det_frame, xyxy, t_id, conf)

    def _handle_plate(self, frame, det_frame, xyxy, t_id, conf):
        x1, y1, x2, y2 = map(int, xyxy)

        # 拦截1：物理长宽比拦截
        # 真实中国车牌的物理长宽比大约是 440mm / 140mm ≈ 3.14
        # 如果 YOLO 框出来的物体是个正方形，或者细长条，直接在源头截杀，省下 OCR 算力！
        box_width = x2 - x1
        box_height = y2 - y1
        if box_height <= 0: return
        aspect_ratio = box_width / box_height
        if aspect_ratio < 1.5 or aspect_ratio > 5.0:
            # 比例严重失调，绝对不是车牌
            return

        pad_x = 15
        pad_y = 15

        crop = frame[max(0, y1 - pad_y):min(frame.shape[0], y2 + pad_y),
        max(0, x1 - pad_x):min(frame.shape[1], x2 + pad_x)]

        # 拉普拉斯清晰度检测 (防模糊截杀)
        gray_crop = cv2.cvtColor(crop, cv2.COLOR_BGR2GRAY)
        blur_score = cv2.Laplacian(gray_crop, cv2.CV_64F).var()

        BLUR_THRESHOLD = 80

        if blur_score < BLUR_THRESHOLD:
            print(
                f"🌫摄像机 {self.cam_id} 车牌模糊被拦截 (清晰度: {blur_score:.1f} < {BLUR_THRESHOLD})，等待下一帧对焦...")
            return

        # 送给 HyperLPR3 识别
        res_lpr = self.lpr_catcher(crop)

        if res_lpr:
            plate_no = str(res_lpr[0][0])
            lpr_conf = float(res_lpr[0][1])
            plate_type_int = int(res_lpr[0][2]) if len(res_lpr[0]) > 2 else 0

            # 拦截 2：字符规则校验 (Regex)
            import re

            # 规则 A: 拦截没有包含任何中文字符的假车牌 (防纯数字 1111111)
            if not re.search(r'[\u4e00-\u9fa5]', plate_no):
                print(f"拦截到伪造条纹背景 (无汉字): {plate_no}")
                return

            # 规则 B: 拦截连续出现 4 个以上相同字符的极度异常数据
            if re.search(r'(.)\1{4,}', plate_no):
                print(f"拦截到异常叠字幻觉: {plate_no}")
                return

            color_map = {0: "blue", 1: "yellow", 2: "green", 3: "green"}
            plate_color = color_map.get(plate_type_int, "default")

            print(f"HyperLPR3 识别结果: {plate_no} | 置信度: {lpr_conf:.2f}")

            if lpr_conf > LPR_OCR_CONF:
                self.triggered_tracks.add(t_id)
                ocr_info = json.dumps({"car-license-number": plate_no, "color": plate_color}, ensure_ascii=False)
                print(f"捕获清晰车牌: {plate_no} [{plate_color}] (OCR置信度: {lpr_conf:.2f})")
                threading.Thread(target=self.capture_and_send,
                                 args=(frame, det_frame, "license-plate", "biz", conf, ocr_info)).start()
            else:
                print(f"⚠️ 模糊车牌: {plate_no} [{plate_color}] (OCR置信度: {lpr_conf:.2f})")

    def capture_and_send(self, frame, det_frame, label, detect_type, conf, ocr_json=None):
        try:
            _, raw_buf = cv2.imencode('.jpg', frame, [int(cv2.IMWRITE_JPEG_QUALITY), 95])
            _, det_buf = cv2.imencode('.jpg', det_frame, [int(cv2.IMWRITE_JPEG_QUALITY), 95])
            files = {
                'originalFile': ('original.jpg', raw_buf.tobytes(), 'image/jpeg'),
                'detectFile': ('detect.jpg', det_buf.tobytes(), 'image/jpeg')
            }
            data = {'cam_id': self.cam_id, 'confidence': f"{conf:.2f}"}
            if detect_type == "biz":
                url = JAVA_BIZ_DETECT_API
                data.update({'detect_type': label})
                if ocr_json: data.update({'detect_result': ocr_json})
            else:
                url = JAVA_EXP_DETECT_API
                data.update({'alarm_type': label})
            requests.post(url, files=files, data=data, timeout=5)
        except Exception as e:
            print(f"抓拍推送异常: {e}")

    def get_raw_frame(self):
        with self.lock: return self.raw_frame.copy() if self.raw_frame is not None else None

    def get_display_frame(self):
        with self.lock: return self.display_frame.copy() if self.display_frame is not None else None

    # 给 WebRTC 提取画好框的画面专用的方法
    def get_render_frame(self):
        with self.lock:
            if self.ai_render_frame is not None:
                return self.ai_render_frame.copy()
            elif self.display_frame is not None:
                return self.display_frame.copy()
            return None

    def start_recording(self):
        with self.lock:
            if self.is_recording or self.raw_frame is None:
                return
            h, w = self.raw_frame.shape[:2]
            self.record_filename = f"camera_{self.cam_id}_{int(time.time())}.mp4"
            fourcc = cv2.VideoWriter_fourcc(*'mp4v')
            self.video_writer = cv2.VideoWriter(self.record_filename, fourcc, 25.0, (w, h))
            self.is_recording = True
            print(f"摄像机 {self.cam_id} 开始后台无损录像: {self.record_filename}")

    def stop_recording_and_upload(self):
        with self.lock:
            if not self.is_recording:
                return
            self.is_recording = False
            if self.video_writer:
                self.video_writer.release()
                self.video_writer = None
                print(f"摄像机 {self.cam_id} 录像结束，准备上传...")
        threading.Thread(target=self._upload_video_file, args=(self.record_filename,), daemon=True).start()

    def _upload_video_file(self, filename):
        try:
            url = f"{JAVA_BIZ_DETECT_API}/upload-video-case"
            with open(filename, 'rb') as f:
                files = {'file': (filename, f, 'video/mp4')}
                data = {'camId': self.cam_id}
                response = requests.post(url, files=files, data=data, timeout=60)

            if response.status_code == 200:
                print(f"录像 {filename} 成功推送到 Java 后台")
                if os.path.exists(filename):
                    os.remove(filename)
            else:
                print(f"录像 {filename} 上传 Java 失败: {response.text}")
        except Exception as e:
            print(f"录像上传发生异常: {e}")

    def stop(self):
        self.stopped = True
        self.cap.release()


# ==================== 3. WebRTC 视频轨道定义 ====================
class CameraStreamTrack(MediaStreamTrack):
    kind = "video"

    def __init__(self, manager):
        super().__init__()
        self.manager = manager
        self.start_time = time.time()

    async def recv(self):

        # 加上严格的帧率“物理刹车”
        # 强行让 WebRTC 推流线程每次执行前等待 40 毫秒 (即最高 25 FPS 的帧率)
        # 这能彻底避免因提取速度太快而导致的并发死锁和黑屏！
        await asyncio.sleep(0.04)

        # 根据开关，决定给前端推“带框图”还是“干净原图”
        if self.manager.is_detect:
            frame = self.manager.get_render_frame()
        else:
            frame = self.manager.get_display_frame()

        # 防空帧死锁
        while frame is None:
            await asyncio.sleep(0.02)  # 缩短等待时间，提升平滑度
            frame = self.manager.get_render_frame() if self.manager.is_detect else self.manager.get_display_frame()

        frame_rgb = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
        video_frame = VideoFrame.from_ndarray(frame_rgb, format="rgb24")

        timestamp = int((time.time() - self.start_time) * 90000)
        video_frame.pts = timestamp
        video_frame.time_base = fractions.Fraction(1, 90000)
        return video_frame


# ==================== 4. FastAPI 服务与路由 ====================
camera_managers = {}
pcs = set()


@asynccontextmanager
async def lifespan(app: FastAPI):
    print(f"泊车 AI 后台识别服务启动")

    print("正在加载内部天眼模型 (Cam 1)...")
    inner_model = YOLO(INNER_MODEL_PATH, task='detect')

    print("正在加载出入口门神模型 (Cam 2/3)...")
    outer_model_2 = YOLO(OUTER_MODEL_PATH, task='detect')
    outer_model_3 = YOLO(OUTER_MODEL_PATH, task='detect')

    # 加载环境异常专用模型
    print("正在加载环境异常监测模型 (Fire)...")
    env_model = YOLO(ENV_MODEL_PATH, task='detect')

    lpr_model = lpr3.LicensePlateCatcher()

    camera_managers["1"] = GlobalCameraManager(
        "1", CAMERAS["1"], inner_model, lpr_model, INNER_MODEL_PREDICT_SIZE, biz_conf=INNER_BIZ_CONF,
        env_model=env_model, env_predict_size=ENV_MODEL_PREDICT_SIZE, env_conf=ENV_FIRE_CONF
    ).start()

    camera_managers["2"] = GlobalCameraManager(
        "2", CAMERAS["2"], outer_model_2, lpr_model, OUTER_MODEL_PREDICT_SIZE, biz_conf=OUTER_BIZ_CONF
    ).start()
    camera_managers["3"] = GlobalCameraManager(
        "3", CAMERAS["3"], outer_model_3, lpr_model, OUTER_MODEL_PREDICT_SIZE, biz_conf=OUTER_BIZ_CONF
    ).start()

    yield
    print("正在释放资源...")
    for m in camera_managers.values(): m.stop()
    for pc in list(pcs): await pc.close()


app = FastAPI(lifespan=lifespan)
app.add_middleware(CORSMiddleware, allow_origins=["*"], allow_methods=["*"], allow_headers=["*"])


class WebRTCOffer(BaseModel):
    sdp: str
    type: str


@app.post("/offer/{cam_id}")
async def offer(cam_id: str, params: WebRTCOffer):
    if cam_id not in camera_managers: return {"error": "未找到对应的摄像头实例"}
    offer = RTCSessionDescription(sdp=params.sdp, type=params.type)
    pc = RTCPeerConnection()
    pcs.add(pc)

    @pc.on("connectionstatechange")
    async def on_state():
        if pc.connectionState in ["failed", "closed"]: pcs.discard(pc)

    # 监听更底层的 ICE 网络状态，一旦前端强制断开，立刻掐断后端 P2P 引擎
    @pc.on("iceconnectionstatechange")
    async def on_ice_state():
        if pc.iceConnectionState in ["failed", "closed", "disconnected"]:
            try:
                await pc.close()
            except Exception:
                pass
            pcs.discard(pc)

    pc.addTrack(CameraStreamTrack(camera_managers[cam_id]))
    await pc.setRemoteDescription(offer)
    answer = await pc.createAnswer()
    await pc.setLocalDescription(answer)

    fixed_sdp = pc.localDescription.sdp.replace("169.254.69.94", SERVER_LAN_IP)
    return {"sdp": fixed_sdp, "type": pc.localDescription.type}


@app.get("/capture_raw/{cam_id}")
async def capture_raw(cam_id: str):
    if cam_id not in camera_managers: return {"error": "camera not found"}
    manager = camera_managers[cam_id]
    frame = manager.get_raw_frame()
    if frame is None: return {"error": "frame not available"}

    try:
        _, buf = cv2.imencode('.jpg', frame, [int(cv2.IMWRITE_JPEG_QUALITY), 95])
        files = {'file': (f'hard_case_{cam_id}_{int(time.time())}.jpg', buf.tobytes(), 'image/jpeg')}
        data = {'camId': cam_id}
        target_url = f"{JAVA_SERVER}/iot/detect/biz-detect-record/capture-hard-case"
        response = requests.post(target_url, files=files, data=data, timeout=5)

        if response.status_code == 200:
            return {"status": "ok", "msg": "原始难例已同步"}
        else:
            return {"error": f"Java server error: {response.text}"}
    except Exception as e:
        print(f"后端抓拍转发异常: {e}")
        return {"error": str(e)}


@app.get("/start_record/{cam_id}")
async def api_start_record(cam_id: str):
    if cam_id in camera_managers:
        camera_managers[cam_id].start_recording()
        return {"status": "ok", "msg": "开始录制"}
    return {"error": "camera not found"}


@app.get("/stop_record/{cam_id}")
async def api_stop_record(cam_id: str):
    if cam_id in camera_managers:
        camera_managers[cam_id].stop_recording_and_upload()
        return {"status": "ok", "msg": "停止录制并上传"}
    return {"error": "camera not found"}


# 支持通过 cam_id 单独控制对应摄像头的检测框
@app.get("/toggle_detect/{cam_id}/{state}")
async def api_toggle_detect(cam_id: str, state: int):
    if cam_id not in camera_managers:
        return {"error": "未找到对应的摄像头实例"}

    manager = camera_managers[cam_id]
    manager.is_detect = (state == 1)  # 修改该实例专属的状态

    action = "显示" if manager.is_detect else "隐藏"
    print(f"👁️ 收到前端指令：摄像头 {cam_id} 已{action}实时检测框")

    return {"status": "ok", "msg": f"摄像头 {cam_id} 检测框已{action}", "is_detect": manager.is_detect}


if __name__ == '__main__':
    import uvicorn

    uvicorn.run(app, host="0.0.0.0", port=8000)
