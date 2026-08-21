package com.imxiaoxin.iot.utils;

import cn.hutool.core.util.StrUtil;
import com.imxiaoxin.iot.config.properties.MinioProperties;
import com.imxiaoxin.iot.exception.BizException;
import io.minio.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Slf4j
public class MinIOFileStorageUtil {

    private MinioClient minioClient;

    private MinioProperties minioProperties;

    public MinIOFileStorageUtil(MinioClient minioClient, MinioProperties minioProperties) {
        this.minioClient = minioClient;
        this.minioProperties = minioProperties;
    }

    private final static String separator = "/";

    /**
     * @param dirPath
     * @param filename  yyyy/mm/dd/file.jpg
     * @return
     */
    public static String builderFilePath(String dirPath,String filename) {
        StringBuilder stringBuilder = new StringBuilder(50);
        if(!StrUtil.isEmpty(dirPath)){
            stringBuilder.append(dirPath).append(separator);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        String todayStr = sdf.format(new Date());
        stringBuilder.append(todayStr).append(separator);
        stringBuilder.append(UUID.randomUUID());
        stringBuilder.append("_");
        stringBuilder.append(filename);
        return stringBuilder.toString();
    }

    public static void main(String[] args) {
        System.out.println(builderFilePath("imxiaoxin", "test.jpg"));
    }

    /**
     * 上传文件，如果bucket不存在，则创建
     * @param file
     * @return
     */
    public String uploadFile(MultipartFile file, String folderName) {
      String objectName = null;
      try {
        boolean bucketExists = minioClient.bucketExists(
            BucketExistsArgs.builder()
                .bucket(minioProperties.getBucketName())
                .build());
        if (!bucketExists) {
            minioClient.makeBucket(
                MakeBucketArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .build());
            minioClient.setBucketPolicy(
                SetBucketPolicyArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .config(createBucketPolicyConfig(minioProperties.getBucketName()))
                    .build());
        }
        objectName = builderFilePath(folderName, file.getOriginalFilename());
        minioClient.putObject(
            PutObjectArgs.builder()
                .bucket(minioProperties.getBucketName())
                .stream(file.getInputStream(), file.getSize(), -1)
                .object(objectName)
                .contentType(file.getContentType())
                .build());
      } catch (Exception e) {
          throw new BizException("上传文件失败");
      }
      return String.join("/",minioProperties.getEndpoint(),minioProperties.getBucketName(),objectName);
    }

    /**
     * 上传字节数组 (byte[]) 到 MinIO
     * @param bytes 图片字节数据
     * @param folderName 文件夹名 (建议带后缀，如 .jpg)
     * @param fileName 文件名
     * @param contentType 内容类型 (如 image/jpeg)
     * @return 返回上传后的完整访问路径
     */
    public String uploadFile(byte[] bytes, String folderName, String fileName,String contentType) {
        if (bytes == null || bytes.length == 0) {
            log.error("上传内容为空");
            return null;
        }

        // 1. 生成存储路径 (利用类中已有的方法)
        // 假设你要存放在 'detect' 目录下，如果没有目录要求，dirPath 传空字符串
        String filePath = builderFilePath(folderName, fileName);

        try (InputStream inputStream = new ByteArrayInputStream(bytes)) {
            // 2. 构建上传参数
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                .bucket(minioProperties.getBucketName())
                .object(filePath)
                .stream(inputStream, bytes.length, -1) // 指定长度
                .contentType(contentType)
                .build();

            // 3. 执行上传
            minioClient.putObject(putObjectArgs);

            // 4. 拼接并返回完整访问路径
            return minioProperties.getEndpoint() + separator + minioProperties.getBucketName() + separator + filePath;
        } catch (Exception e) {
            log.error("MinIO 上传字节流失败: {}", e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private String createBucketPolicyConfig(String bucketName) {
        return """
            {
              "Statement" : [ {
                "Action" : "s3:GetObject",
                "Effect" : "Allow",
                "Principal" : "*",
                "Resource" : "arn:aws:s3:::%s/*"
              } ],
              "Version" : "2012-10-17"
            }
            """.formatted(bucketName);
    }

    /**
     *  上传图片文件
     * @param prefix  文件前缀
     * @param filename  文件名
     * @param inputStream 文件流
     * @return  文件全路径
     */
    public String uploadImgFile(String prefix, String filename, InputStream inputStream) {
        String filePath = builderFilePath(prefix, filename);
        try {
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .object(filePath)
                    .contentType("image/jpg")
                    .bucket(minioProperties.getBucketName())
                    .stream(inputStream,inputStream.available(),-1)
                    .build();
            minioClient.putObject(putObjectArgs);
            StringBuilder urlPath = new StringBuilder(minioProperties.getEndpoint());
            urlPath.append(separator+minioProperties.getBucketName());
            urlPath.append(separator);
            urlPath.append(filePath);
            return urlPath.toString();
        } catch (Exception ex){
            log.error("minio put file error.",ex);
            throw new BizException("上传文件失败");
        }
    }

    /**
     *  上传html文件
     * @param prefix  文件前缀
     * @param filename   文件名
     * @param inputStream  文件流
     * @return  文件全路径
     */
    public String uploadHtmlFile(String prefix, String filename,InputStream inputStream) {
        String filePath = builderFilePath(prefix, filename);
        try {
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .object(filePath)
                    .contentType("text/html")
                    .bucket(minioProperties.getBucketName()).stream(inputStream,inputStream.available(),-1)
                    .build();
            minioClient.putObject(putObjectArgs);
            StringBuilder urlPath = new StringBuilder(minioProperties.getEndpoint());
            urlPath.append(separator+minioProperties.getBucketName());
            urlPath.append(separator);
            urlPath.append(filePath);
            return urlPath.toString();
        }catch (Exception ex){
            log.error("minio put file error.",ex);
            ex.printStackTrace();
            throw new RuntimeException("上传文件失败");
        }
    }

    /**
     * 删除文件
     * @param pathUrl  文件全路径
     */
    public void delete(String pathUrl) {
        String key = pathUrl.replace(minioProperties.getEndpoint()+"/","");
        int index = key.indexOf(separator);
        String bucket = key.substring(0,index);
        String filePath = key.substring(index+1);
        // 删除Objects
        RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder().bucket(bucket).object(filePath).build();
        try {
            minioClient.removeObject(removeObjectArgs);
        } catch (Exception e) {
            log.error("minio remove file error.  pathUrl:{}",pathUrl);
            e.printStackTrace();
        }
    }


    /**
     * 下载文件
     * @param pathUrl  文件全路径
     * @return  文件流
     *
     */
    public byte[] downLoadFile(String pathUrl)  {
        String key = pathUrl.replace(minioProperties.getEndpoint()+"/","");
        int index = key.indexOf(separator);
        String bucket = key.substring(0,index);
        String filePath = key.substring(index+1);
        InputStream inputStream = null;
        try {
            inputStream = minioClient.getObject(GetObjectArgs.builder().bucket(minioProperties.getBucketName()).object(filePath).build());
        } catch (Exception e) {
            log.error("minio down file error.  pathUrl:{}",pathUrl);
            e.printStackTrace();
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buff = new byte[100];
        int rc = 0;
        while (true) {
            try {
                if (!((rc = inputStream.read(buff, 0, 100)) > 0)) break;
            } catch (IOException e) {
                e.printStackTrace();
            }
            byteArrayOutputStream.write(buff, 0, rc);
        }
        return byteArrayOutputStream.toByteArray();
    }
}