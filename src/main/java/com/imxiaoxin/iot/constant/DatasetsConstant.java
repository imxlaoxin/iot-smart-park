package com.imxiaoxin.iot.constant;

import java.io.File;

/**
 * @author imxiaoxin
 *
 */
public interface DatasetsConstant {
  // 1. 图片保存的相对路径（使用 File.separator 保证跨平台兼容）
  String IMAGE_DIR = "src" + File.separator + "main" + File.separator + "resources" + File.separator + "datasets" + File.separator + "images";

  // 2. 视频保存的具体路径
  String VIDEO_DIR = "src" + File.separator + "main" + File.separator + "resources" + File.separator + "datasets" + File.separator + "videos";

}
