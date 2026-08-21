package com.imxiaoxin.iot.service.impl;

import cn.hutool.core.io.FileUtil;
import com.imxiaoxin.iot.constant.DatasetsConstant;
import com.imxiaoxin.iot.service.DetectService;
import com.imxiaoxin.iot.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author imxiaoxin
 *
 */
@Slf4j
@Service
public class DetectServiceImpl implements DetectService {

  @Autowired
  private FileService fileService;

  @Override
  public void saveErrorImageByUrlForTraining(String originalUrl, String fileName) {
    // 1. 下载图片字节数组
    byte[] bytes = fileService.download(originalUrl);

    if (bytes != null) {
      try {
        // 2. 获取项目根目录 (G:/project/iot/iot-smart-park)
        String projectPath = System.getProperty("user.dir");

        // 3. 手动拼接源码目录的路径
        // 使用 File.separator 保证 Windows 和 Linux 的兼容性
        String savePath = projectPath + File.separator + DatasetsConstant.IMAGE_DIR + File.separator + fileName;

        // 4. 写入文件
        FileUtil.writeBytes(bytes, savePath);
        log.info("纠错图片已保存至: {}", savePath);
      } catch (Exception e) {
        log.error("保存训练图片失败: {}", fileName, e);
      }
    }
  }

  @Override
  public void saveErrorImageByBytesForTraining(byte[] bytes, String fileName) {
    if (bytes != null) {
      try {
        // 2. 获取项目根目录 (G:/project/iot/iot-smart-park)
        String projectPath = System.getProperty("user.dir");

        // 3. 手动拼接源码目录的路径
        // 使用 File.separator 保证 Windows 和 Linux 的兼容性
        String savePath = projectPath + File.separator + DatasetsConstant.IMAGE_DIR + File.separator + fileName;

        // 4. 写入文件
        FileUtil.writeBytes(bytes, savePath);
        log.info("纠错图片已保存至: {}", savePath);
      } catch (Exception e) {
        log.error("保存训练图片失败: {}", fileName, e);
      }
    }
  }

  @Override
  public int getErrorImageCount() {
    try {
      String projectPath = System.getProperty("user.dir");
      String imagesPath = projectPath + File.separator + DatasetsConstant.IMAGE_DIR;

      if (!FileUtil.exist(imagesPath)) return 0;

      // 【核心修改】：使用 loopFiles 递归遍历 images 及其所有子目录
      // 第二个参数是文件过滤器，只统计图片格式
      List<File> imageFiles = FileUtil.loopFiles(imagesPath, file ->
          file.isFile() && file.getName().toLowerCase().matches(".*\\.(jpg|jpeg|png)$")
      );

      return imageFiles.size();
    } catch (Exception e) {
      log.error("递归统计图片总数失败", e);
      return 0;
    }
  }

  @Override
  public void saveVideoFile(MultipartFile file, String fileName) throws IOException {
    String projectPath = System.getProperty("user.dir");
    String videoPath = projectPath + File.separator + DatasetsConstant.VIDEO_DIR;
    File dir = new File(videoPath);
    if (!dir.exists()) {
      dir.mkdirs();
    }
    File destFile = new File(dir, fileName);
    file.transferTo(destFile); // 保存到磁盘
  }

}
