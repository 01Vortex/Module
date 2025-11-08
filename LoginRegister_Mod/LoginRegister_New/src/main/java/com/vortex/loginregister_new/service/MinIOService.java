package com.vortex.loginregister_new.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * MinIO服务接口
 *
 * @author Vortex
 * @since 2024
 */
public interface MinIOService {

    /**
     * 上传文件
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称（文件路径）
     * @param file       文件
     * @return 文件访问URL
     * @throws Exception 上传异常
     */
    String uploadFile(String bucketName, String objectName, MultipartFile file) throws Exception;

    /**
     * 上传文件流
     *
     * @param bucketName  存储桶名称
     * @param objectName  对象名称（文件路径）
     * @param inputStream 输入流
     * @param contentType 内容类型
     * @param size        文件大小
     * @return 文件访问URL
     * @throws Exception 上传异常
     */
    String uploadFile(String bucketName, String objectName, InputStream inputStream, String contentType, long size) throws Exception;

    /**
     * 删除文件
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称（文件路径）
     * @throws Exception 删除异常
     */
    void deleteFile(String bucketName, String objectName) throws Exception;

    /**
     * 检查存储桶是否存在，不存在则创建
     *
     * @param bucketName 存储桶名称
     * @throws Exception 异常
     */
    void ensureBucketExists(String bucketName) throws Exception;

    /**
     * 设置存储桶为公开读取
     *
     * @param bucketName 存储桶名称
     * @throws Exception 异常
     */
    void setBucketPublicRead(String bucketName) throws Exception;

    /**
     * 获取文件访问URL
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称（文件路径）
     * @return 文件访问URL
     */
    String getFileUrl(String bucketName, String objectName);
}

