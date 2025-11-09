package com.vortex.loginregister_new.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Web MVC 配置
 * 用于配置静态资源访问
 *
 * @author Vortex
 * @since 2024
 */
@Slf4j
@Configuration
public class WebMvcConfig implements WebMvcConfigurer, Ordered {

    @Value("${file.upload.path:uploads}")
    private String uploadPath;

    @Value("${file.upload.url-prefix:/api/uploads}")
    private String urlPrefix;

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // 配置上传文件的静态资源访问
        // urlPrefix格式: /api/uploads，需要映射到 /uploads/**
        // 注意：这里使用的是去掉 context-path 后的路径
        String uploadUrl = urlPrefix.replace("/api", "") + "/**";
        
        // 获取上传目录的绝对路径（与UserController中的逻辑保持一致）
        java.nio.file.Path basePath = Paths.get(uploadPath);
        
        // 如果是相对路径，转换为项目根目录下的绝对路径
        if (!basePath.isAbsolute()) {
            String userDir = System.getProperty("user.dir");
            basePath = Paths.get(userDir).resolve(uploadPath);
        }
        
        // 转换为字符串，Windows路径需要特殊处理，统一使用 /
        String uploadDir = basePath.toAbsolutePath().toString().replace("\\", "/");
        
        // 确保路径以 / 结尾
        if (!uploadDir.endsWith("/")) {
            uploadDir += "/";
        }
        uploadDir = "file:" + uploadDir;
        
        // 只处理 /uploads/** 路径，避免拦截其他路径（如 /oauth/**）
        registry.addResourceHandler(uploadUrl)
                .addResourceLocations(uploadDir)
                .setCachePeriod(3600) // 缓存1小时
                .resourceChain(false); // 禁用资源链，避免拦截控制器路径
        
        log.debug("静态资源处理器已配置 - URL: {}, 目录: {}", uploadUrl, uploadDir);
    }
    
    /**
     * 设置配置优先级，确保 Controller 路径优先于静态资源处理
     */
    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE; // 最低优先级，确保 Controller 路径优先
    }
}

