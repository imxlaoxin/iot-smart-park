package com.imxiaoxin.iot.service.impl;

import com.imxiaoxin.iot.service.FileService;
import com.imxiaoxin.iot.utils.MinIOFileStorageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author: imxiaoxin
 * description:
 */
@Slf4j
@Service
public class FileServiceImpl implements FileService {

  @Autowired
  private MinIOFileStorageUtil minIOFileStorageUtil;

  @Override
  public String upload(MultipartFile file, String folderName) {
    return minIOFileStorageUtil.uploadFile(file, folderName);
  }

  @Override
  public String upload(byte[] bytes, String folderName, String fileName, String contentType) {
    return minIOFileStorageUtil.uploadFile(bytes, folderName, fileName, contentType);
  }

  @Override
  public byte[] download(String url) {
    return minIOFileStorageUtil.downLoadFile(url);
  }


}