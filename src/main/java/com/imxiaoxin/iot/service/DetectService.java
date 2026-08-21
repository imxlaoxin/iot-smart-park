package com.imxiaoxin.iot.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author imxiaoxin
 *
 */
public interface DetectService {

  /**
   * 保存错误图片用于训练
   * @param originalUrl 图片原始地址
   * @param fileName 图片名称
   */
  void saveErrorImageByUrlForTraining(String originalUrl, String fileName);

  /**
   * 保存错误图片用于训练
   * @param
   * @param bytes 图片字节数组
   * @param fileName 图片名称
   */
  void saveErrorImageByBytesForTraining(byte[] bytes, String fileName);

  /**
   * 获取当前已收集的纠错图片数量
   * @return 图片张数
   */
  int getErrorImageCount();

  /**
   * 上传现场真实视频段接口
   * @param file
   * @param fileName
   * @return
   */
  void saveVideoFile(MultipartFile file, String fileName) throws IOException;
}
