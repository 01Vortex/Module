package com.vortex.loginregister_new.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.task.SimpleAsyncTaskExecutorBuilder;
import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executors;

/**
 * 虚拟线程配置类
 * 启用 Java 21 的虚拟线程（Virtual Threads）特性
 * 
 * 虚拟线程的优势：
 * 1. 轻量级：可以创建数百万个虚拟线程
 * 2. 低成本：内存占用极小
 * 3. 高并发：适合 I/O 密集型任务
 * 4. 简单：使用方式与传统线程完全一致
 *
 * @author 01Vortex
 * @since 2024
 */
@Slf4j
@Configuration
@EnableAsync
public class VirtualThreadConfig {

    /**
     * 配置 Tomcat 使用虚拟线程处理请求
     * 这将大幅提升 Web 应用的并发处理能力
     */
    @Bean
    public TomcatProtocolHandlerCustomizer<?> protocolHandlerVirtualThreadExecutorCustomizer() {
        return protocolHandler -> {
            log.info("✅ 配置 Tomcat 使用虚拟线程");
            protocolHandler.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        };
    }

    /**
     * 配置异步任务执行器使用虚拟线程
     * 替代默认的线程池
     */
    @Bean(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME)
    public AsyncTaskExecutor asyncTaskExecutor() {
        return new TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor());
    }

    /**
     * 配置简单异步任务执行器（使用虚拟线程）
     */
    @Bean
    public SimpleAsyncTaskExecutorBuilder simpleAsyncTaskExecutorBuilder() {
        SimpleAsyncTaskExecutorBuilder builder = new SimpleAsyncTaskExecutorBuilder();
        builder = builder.virtualThreads(true);
        builder = builder.threadNamePrefix("vt-");
        return builder;
    }
}

