## 项目概览

「云瞳智泊」智能停车场系统（IoT Smart Park），由三部分组成：

1. **Java 后端**（本仓库主体，`src/`）— Spring Boot 3.3.5 / Java 17 / Maven，端口 8080，系统中枢：接收边缘 AI 识别结果与传感器上报，落库（MySQL + MyBatis-Plus）、上传图片/视频到 MinIO、通过 MQTT 向硬件与前端广播、调用 LLM Agent 生成告警处置建议。
2. **边缘 AI 后端**（`edge_ai_backend/main.py`）— Python FastAPI（端口 8000），运行在边缘设备上：拉取 3 路 RTSP 摄像头，跑 YOLO 模型（车辆/车牌/火灾）与 HyperLPR3 车牌 OCR，将识别结果 POST 给 Java 后端，并通过 aiortc/WebRTC 向前端推流。模型文件在仓库外（`../../models/*.pt`），依赖 opencv、ultralytics、hyperlpr3、aiortc、av、fastapi。
3. **基础设施**（`src/main/resources/docker-compose.yml`）— EMQX（MQTT broker :1883，也是 webhook 上报源）、MinIO（:9000）、Redis（:6379）。MySQL 不在此 compose 中。

前端（Vue，dev 端口 5173）不在本仓库；CORS 白名单配置在 `application-dev.yml` 的 `cors.allow-origins`。根目录的 `云瞳智泊-项目演示视频.mp4` 是演示视频，非代码。

## 常用命令

```bash
mvn compile              # 编译
mvn spring-boot:run      # 启动后端（dev profile，端口 8080）
mvn package              # 打包可执行 jar
mvn test                 # 运行测试
mvn test -Dtest=LLMTest  # 运行单个测试类（LLM 连通性测试，需真实网络）
cd edge_ai_backend && python main.py   # 启动边缘 AI 服务（端口 8000，需摄像头与模型文件）
```

- 依赖中间件（EMQX/MinIO/Redis）用 `docker compose -f src/main/resources/docker-compose.yml up -d` 启动；MySQL 需要自行准备（dev 库：`iot-intelligent-park`，root/root）。
- 建库脚本：`src/main/resources/iot-smart-park.sql`。
- API 文档（Knife4j）：http://localhost:8080/doc.html
- Spring 日志输出到 `iot-log/`（`logging.file.path`），该目录已被 .gitignore 忽略。

## 核心架构

### 数据流（三条主线）

**车牌识别流**：RTSP 摄像头 2/3 → YOLO `outer-2.pt` 检测车牌框 → 多级过滤（长宽比 1.5–5.0、拉普拉斯模糊度 ≥80、HyperLPR3 OCR 置信度 >0.90、正则校验含汉字）→ POST `/iot/detect/biz-detect-record`（originalFile + detectFile + cam_id + detect_result JSON）→ Java 落库 + 上传 MinIO + MQTT 发布到 `biz_detect` 主题（杆机/计费联动）。摄像头 1（`inner.pt`）做场内车辆检测，业务逻辑暂为空。

**火灾/紧急告警流**：`env.pt` 降频推理（1 秒 1 次）→ 状态机：起火立即上报、持续燃烧每 30s 重报、10s 无火发 `fire_clear` → POST `/iot/detect/exp-detect-record` → Java 落库 + MQTT 发布到 `exp_detect` 主题。`fire_clear` 是控制指令：只走 MQTT 下发，**不落库**。

**传感器环境告警流**：EMQX 规则引擎 webhook → `MqttWebhookController`（`/iot/webhook/*`：充电桩、环境快照、车位状态、环境告警）→ 环境告警 (`/env_exp`) 异步调用 LLM Agent（`SmartParkAgent`）生成 ≤50 字处置建议 → MQTT 发布到 `exp_env_advice` 主题。Redis 冷却锁（每告警类型 60s）防高频请求大模型，冷却期内下发历史建议兜底。

### Java 分层（`com.imxiaoxin.iot`）

- `controller/` — REST 接口，统一前缀 `/iot/**`，响应统一包装为 `R<T>` / 分页 `PageR<T>`（`common/`）。
- `service/impl/` — 业务逻辑，MyBatis-Plus `ServiceImpl` 模式；`entity` 继承 `BaseEntity`（createTime/updateTime 由 `handler/MyMetaObjectHandler` 自动填充）。
- `mqtt/` — 出站方向：`MqttMessageSender`（注入即用），主题定义在 `application.yml` 的 `mqtt.pub-topics` 并绑定到 `MqttProperties`。入站全靠 EMQX webhook，Java 端无 MQTT 订阅。
- `agent/` — LangChain4j AI Agent：`SmartParkAgent`（qwen-max，`QIANWEN_APIKEY` 环境变量；或注释切换到 Ollama `deepseek-r1:1.5b`）+ `ExpEnvTools`（工具：查询全局环境基准线、扫描充电桩健康状态；prompt 在 `resources/prompts/exp_env_prompt.txt`）。
- `model/enums/` — 枚举实现 `BaseEnum`（code/desc），请求参数经 `factory/StringToBaseEnumConverterFactory` 自动转换。
- 异步线程池：`config/AsyncConfig` 的 `taskExecutor` bean，文件上传、AI 分析均用 `CompletableFuture.runAsync(..., taskExecutor)` 异步化。

### 边缘 AI 后端要点（`edge_ai_backend/main.py`）

- `JAVA_SERVER = "http://localhost:8080"` 硬编码指向 Java 后端；`SERVER_LAN_IP`、`CAMERAS` RTSP 地址、模型路径、置信度阈值均在文件头部「业务配置中心」区，按环境调整。
- 每路摄像头一个 `GlobalCameraManager`：抓帧线程 + AI 推理线程 + 状态机；WebRTC 推流固定 25 FPS（`CameraStreamTrack.recv` 内 `asyncio.sleep(0.04)` 物理限速，勿删，删了会黑屏）。
- 前端切换检测框显示走 `/toggle_detect/{cam_id}/{state}`；录像上传走 Java 的 `/upload-video-case`；难例抓拍走 `/capture_raw/{cam_id}` → Java `/capture-hard-case`。

### 训练纠错闭环

前端把识别记录标为「误报/漏报」（`/update-process-status`）时，Java 异步把原始图下载保存到 `src/main/resources/datasets/images/`，攒满 100 张后日志提示去 Python 端跑增量训练（`train.py` 不在本仓库）。
