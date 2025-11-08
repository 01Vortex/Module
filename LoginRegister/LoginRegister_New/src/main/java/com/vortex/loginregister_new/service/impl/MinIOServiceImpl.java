package com.vortex.loginregister_new.service.impl;

import com.vortex.loginregister_new.config.MinIOConfig;
import com.vortex.loginregister_new.service.MinIOService;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * MinIO服务实现类
 *
 * @author Vortex
 * @since 2024
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MinIOServiceImpl implements MinIOService {

    private final MinioClient minioClient;
    private final MinIOConfig minIOConfig;

    @Override
    public String uploadFile(String bucketName, String objectName, MultipartFile file) throws Exception {
        // 确保存储桶存在
        ensureBucketExists(bucketName);
        // 确保存储桶为公开读取
        setBucketPublicRead(bucketName);

        // 上传文件
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            log.info("文件上传成功: bucket={}, object={}", bucketName, objectName);
            return getFileUrl(bucketName, objectName);
        }
    }

    @Override
    public String uploadFile(String bucketName, String objectName, InputStream inputStream, String contentType, long size) throws Exception {
        // 确保存储桶存在
        ensureBucketExists(bucketName);
        // 确保存储桶为公开读取
        setBucketPublicRead(bucketName);

        // 上传文件
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(inputStream, size, -1)
                        .contentType(contentType)
                        .build()
        );
        log.info("文件上传成功: bucket={}, object={}", bucketName, objectName);
        return getFileUrl(bucketName, objectName);
    }

    @Override
    public void deleteFile(String bucketName, String objectName) throws Exception {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            log.info("文件删除成功: bucket={}, object={}", bucketName, objectName);
        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equals("NoSuchKey")) {
                log.warn("文件不存在，无需删除: bucket={}, object={}", bucketName, objectName);
            } else {
                throw e;
            }
        }
    }

    @Override
    public void ensureBucketExists(String bucketName) throws Exception {
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            log.info("创建存储桶: {}", bucketName);
            // 创建后立即设置公开读取策略
            setBucketPublicRead(bucketName);
        }
    }

    @Override
    public void setBucketPublicRead(String bucketName) throws Exception {
        try {
            // 设置存储桶策略为公开读取
            String policy = String.format(
                    "{\n" +
                    "  \"Version\": \"2012-10-17\",\n" +
                    "  \"Statement\": [\n" +
                    "    {\n" +
                    "      \"Effect\": \"Allow\",\n" +
                    "      \"Principal\": {\n" +
                    "        \"AWS\": [\"*\"]\n" +
                    "      },\n" +
                    "      \"Action\": [\"s3:GetObject\"],\n" +
                    "      \"Resource\": [\"arn:aws:s3:::%s/*\"]\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}",
                    bucketName
            );
            
            minioClient.setBucketPolicy(
                    SetBucketPolicyArgs.builder()
                            .bucket(bucketName)
                            .config(policy)
                            .build()
            );
            log.info("设置存储桶 {} 为公开读取", bucketName);
        } catch (Exception e) {
            // 如果设置策略失败，记录警告但不抛出异常（可能是权限问题）
            log.warn("设置存储桶 {} 公开读取策略失败: {}", bucketName, e.getMessage());
            // 尝试使用旧版本的API（通过设置对象ACL）
            try {
                // 注意：MinIO的新版本可能不支持设置对象ACL，这里只记录日志
                log.debug("尝试通过其他方式设置存储桶访问权限");
            } catch (Exception ex) {
                log.warn("无法设置存储桶访问权限: {}", ex.getMessage());
            }
        }
    }

    @Override
    public String getFileUrl(String bucketName, String objectName) {
        // 构建文件访问URL
        String protocol = minIOConfig.isSecure() ? "https" : "http";
        String endpoint = minIOConfig.getEndpoint();
        // 移除http://或https://前缀（如果有）
        if (endpoint.startsWith("http://")) {
            endpoint = endpoint.substring(7);
        } else if (endpoint.startsWith("https://")) {
            endpoint = endpoint.substring(8);
        }
        return String.format("%s://%s/%s/%s", protocol, endpoint, bucketName, objectName);
    }
}

