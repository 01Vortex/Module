package com.vortex.loginregister_new.controller;

import com.vortex.loginregister_new.service.VirtualThreadDemoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

/**
 * 虚拟线程测试控制器
 * 演示 Java 21 虚拟线程的强大并发能力
 *
 * @author Vortex
 * @since 2024
 */
@Slf4j
@RestController
@RequestMapping("/virtual-thread")
@RequiredArgsConstructor
public class VirtualThreadController {

    private final VirtualThreadDemoService demoService;

    /**
     * 检查当前线程信息
     */
    @GetMapping("/info")
    public Map<String, Object> getThreadInfo() {
        Thread currentThread = Thread.currentThread();
        
        Map<String, Object> result = new HashMap<>();
        result.put("threadName", currentThread.getName());
        result.put("threadId", currentThread.threadId());
        result.put("isVirtual", currentThread.isVirtual());
        result.put("threadClass", currentThread.getClass().getName());
        result.put("message", currentThread.isVirtual() ? 
                "✅ 当前使用虚拟线程！" : "⚠️ 当前使用平台线程");
        
        log.info("📊 线程信息: {}", result);
        return result;
    }

    /**
     * 测试异步任务（使用虚拟线程）
     */
    @GetMapping("/async-test")
    public Map<String, Object> testAsync(@RequestParam(defaultValue = "5") int taskCount) {
        Instant start = Instant.now();
        
        log.info("🚀 开始执行 {} 个异步任务", taskCount);
        
        // 创建多个异步任务
        List<CompletableFuture<String>> futures = IntStream.range(0, taskCount)
                .mapToObj(i -> demoService.executeAsyncTask("Task-" + i, 1000))
                .toList();
        
        // 等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        // 获取所有结果
        List<String> results = futures.stream()
                .map(CompletableFuture::join)
                .toList();
        
        Duration duration = Duration.between(start, Instant.now());
        
        Map<String, Object> result = new HashMap<>();
        result.put("taskCount", taskCount);
        result.put("results", results);
        result.put("executionTime", duration.toMillis() + "ms");
        result.put("message", String.format("✅ 完成 %d 个任务，总耗时: %dms", taskCount, duration.toMillis()));
        
        log.info("✅ 所有异步任务完成，总耗时: {}ms", duration.toMillis());
        return result;
    }

    /**
     * 虚拟线程并发性能测试
     * 创建大量虚拟线程，展示其轻量级特性
     */
    @GetMapping("/performance-test")
    public Map<String, Object> performanceTest(
            @RequestParam(defaultValue = "1000") int threadCount,
            @RequestParam(defaultValue = "100") long sleepTime) {
        
        Instant start = Instant.now();
        
        log.info("🔥 开始性能测试: 创建 {} 个虚拟线程", threadCount);
        
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<CompletableFuture<String>> futures = IntStream.range(0, threadCount)
                    .mapToObj(i -> CompletableFuture.supplyAsync(() -> {
                        try {
                            Thread.sleep(sleepTime);
                            return String.format("VT-%d: %s (虚拟: %s)",
                                    i,
                                    Thread.currentThread().getName(),
                                    Thread.currentThread().isVirtual());
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return "Error: " + e.getMessage();
                        }
                    }, executor))
                    .toList();
            
            // 等待所有任务完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            
            Duration duration = Duration.between(start, Instant.now());
            
            Map<String, Object> result = new HashMap<>();
            result.put("threadCount", threadCount);
            result.put("sleepTime", sleepTime + "ms");
            result.put("totalExecutionTime", duration.toMillis() + "ms");
            result.put("averageTime", (duration.toMillis() / (double) threadCount) + "ms");
            result.put("threadsPerSecond", (threadCount * 1000.0 / duration.toMillis()));
            result.put("message", String.format("🚀 成功创建并执行 %d 个虚拟线程！", threadCount));
            result.put("conclusion", threadCount >= 1000 ? 
                    "✅ 虚拟线程可以轻松处理成千上万的并发任务！" : 
                    "💡 尝试增加 threadCount 参数来测试更高并发");
            
            log.info("✅ 性能测试完成: {} 个线程，耗时 {}ms", threadCount, duration.toMillis());
            return result;
            
        } catch (Exception e) {
            log.error("❌ 性能测试失败", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", e.getMessage());
            return errorResult;
        }
    }

    /**
     * 对比测试：虚拟线程 vs 平台线程
     */
    @GetMapping("/compare")
    public Map<String, Object> compareThreads(@RequestParam(defaultValue = "100") int taskCount) {
        log.info("📊 开始对比测试: 虚拟线程 vs 平台线程");
        
        Map<String, Object> result = new HashMap<>();
        
        // 测试虚拟线程
        Instant virtualStart = Instant.now();
        try (var virtualExecutor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<CompletableFuture<Void>> virtualFutures = IntStream.range(0, taskCount)
                    .mapToObj(i -> CompletableFuture.runAsync(() -> {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }, virtualExecutor))
                    .toList();
            
            CompletableFuture.allOf(virtualFutures.toArray(new CompletableFuture[0])).join();
        }
        long virtualTime = Duration.between(virtualStart, Instant.now()).toMillis();
        
        // 测试平台线程（固定线程池）
        Instant platformStart = Instant.now();
        try (var platformExecutor = Executors.newFixedThreadPool(
                Math.min(taskCount, Runtime.getRuntime().availableProcessors() * 2))) {
            List<CompletableFuture<Void>> platformFutures = IntStream.range(0, taskCount)
                    .mapToObj(i -> CompletableFuture.runAsync(() -> {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }, platformExecutor))
                    .toList();
            
            CompletableFuture.allOf(platformFutures.toArray(new CompletableFuture[0])).join();
        }
        long platformTime = Duration.between(platformStart, Instant.now()).toMillis();
        
        result.put("taskCount", taskCount);
        result.put("virtualThreadTime", virtualTime + "ms");
        result.put("platformThreadTime", platformTime + "ms");
        result.put("improvement", String.format("%.2f%%", ((platformTime - virtualTime) / (double) platformTime) * 100));
        result.put("speedup", String.format("%.2fx", platformTime / (double) virtualTime));
        result.put("conclusion", virtualTime < platformTime ? 
                "🚀 虚拟线程性能更优！" : "⚠️ 虚拟线程适合 I/O 密集型任务");
        
        log.info("📊 对比测试完成 - 虚拟线程: {}ms, 平台线程: {}ms", virtualTime, platformTime);
        return result;
    }

    /**
     * 模拟真实场景：批量数据库查询
     */
    @GetMapping("/batch-query")
    public Map<String, Object> batchQuery(@RequestParam(defaultValue = "50") int queryCount) {
        Instant start = Instant.now();
        
        log.info("🔍 开始批量数据库查询: {} 个查询", queryCount);
        
        // 使用虚拟线程并发执行查询
        List<CompletableFuture<String>> futures = IntStream.range(0, queryCount)
                .mapToObj(i -> demoService.simulateDatabaseQuery("SELECT * FROM user WHERE id = " + i))
                .toList();
        
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        List<String> results = futures.stream()
                .map(CompletableFuture::join)
                .toList();
        
        Duration duration = Duration.between(start, Instant.now());
        
        Map<String, Object> result = new HashMap<>();
        result.put("queryCount", queryCount);
        result.put("executionTime", duration.toMillis() + "ms");
        result.put("queriesPerSecond", (queryCount * 1000.0 / duration.toMillis()));
        result.put("message", "✅ 批量查询完成！虚拟线程大幅提升并发能力");
        result.put("sampleResults", results.subList(0, Math.min(5, results.size())));
        
        log.info("✅ 批量查询完成: {} 个查询，耗时 {}ms", queryCount, duration.toMillis());
        return result;
    }

    /**
     * 展示虚拟线程的可扩展性
     * 可以创建数百万个虚拟线程而不会耗尽系统资源
     */
    @GetMapping("/scalability-test")
    public Map<String, Object> scalabilityTest(
            @RequestParam(defaultValue = "10000") int threadCount) {
        
        log.info("📈 开始可扩展性测试: {} 个虚拟线程", threadCount);
        
        Instant start = Instant.now();
        long memoryBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<CompletableFuture<Void>> futures = IntStream.range(0, threadCount)
                    .mapToObj(i -> CompletableFuture.runAsync(() -> {
                        try {
                            // 短暂休眠
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }, executor))
                    .toList();
            
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }
        
        Duration duration = Duration.between(start, Instant.now());
        long memoryAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long memoryUsed = memoryAfter - memoryBefore;
        
        Map<String, Object> result = new HashMap<>();
        result.put("threadCount", threadCount);
        result.put("executionTime", duration.toMillis() + "ms");
        result.put("memoryUsedMB", String.format("%.2f MB", memoryUsed / (1024.0 * 1024.0)));
        result.put("memoryPerThread", String.format("%.2f KB", memoryUsed / (1024.0 * threadCount)));
        result.put("message", String.format("🎉 成功创建 %,d 个虚拟线程！", threadCount));
        result.put("tip", "💡 虚拟线程占用内存极小，可以轻松创建百万级别的线程");
        
        log.info("✅ 可扩展性测试完成: {} 个线程，内存占用: {:.2f} MB",
                threadCount, memoryUsed / (1024.0 * 1024.0));
        
        return result;
    }
}

