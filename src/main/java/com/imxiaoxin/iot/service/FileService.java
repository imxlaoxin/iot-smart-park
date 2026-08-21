package com.imxiaoxin.iot.service;


import org.springframework.web.multipart.MultipartFile;


/**
 * 文件相关服务
 * @author imxiaoxin
 * description:
 */
public interface FileService {

  String upload(MultipartFile file, String folderName);

  String upload(byte[] bytes, String folderName, String fileName, String contentType);

  byte[] download(String url);

} 