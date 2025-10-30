package com.vortex.loginregister_new.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * 虚拟线程演示服务
 * 展示虚拟线程在异步任务中的应用
 *
 * @author Vortex
 * @since 2024
 */
@Slf4j
@Service
public class VirtualThreadDemoService {

    /**
     * 异步任务示例 - 使用虚拟线程执行
     * 
     * @param taskName 任务名称
     * @param sleepTime 睡眠时间（毫秒）
     * @return CompletableFuture
     */
    @Async
    public CompletableFuture<String> executeAsyncTask(String taskName, long sleepTime) {
        String threadInfo = String.format("任务: %s | 线程: %s | 是否虚拟线程: %s",
                taskName,
                Thread.currentThread().getName(),
                Thread.currentThread().isVirtual());
        
        log.info("⚡ 开始执行异步任务 - {}", threadInfo);
        
        try {
            // 模拟耗时操作（I/O 密集型）
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("任务被中断: {}", taskName, e);
        }
        
        log.info("✅ 完成异步任务 - {}", threadInfo);
        return CompletableFuture.completedFuture(threadInfo);
    }

    /**
     * 模拟数据库查询操作（I/O 密集型）
     */
    @Async
    public CompletableFuture<String> simulateDatabaseQuery(String query) {
        log.info("🔍 执行数据库查询: {} | 线程: {} | 虚拟线程: {}",
                query,
                Thread.currentThread().getName(),
                Thread.currentThread().isVirtual());
        
        try {
            // 模拟数据库查询延迟
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return CompletableFuture.completedFuture("查询结果: " + query);
    }

    /**
     * 模拟 HTTP 请求（I/O 密集型）
     */
    @Async
    public CompletableFuture<String> simulateHttpRequest(String url) {
        log.info("🌐 发送 HTTP 请求: {} | 线程: {} | 虚拟线程: {}",
                url,
                Thread.currentThread().getName(),
                Thread.currentThread().isVirtual());
        
        try {
            // 模拟网络延迟
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return CompletableFuture.completedFuture("响应数据: " + url);
    }

    /**
     * 同步方法 - 用于对比
     */
    public String executeSyncTask(String taskName, long sleepTime) {
        String threadInfo = String.format("任务: %s | 线程: %s | 是否虚拟线程: %s",
                taskName,
                Thread.currentThread().getName(),
                Thread.currentThread().isVirtual());
        
        log.info("🔄 执行同步任务 - {}", threadInfo);
        
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return threadInfo;
    }
}

