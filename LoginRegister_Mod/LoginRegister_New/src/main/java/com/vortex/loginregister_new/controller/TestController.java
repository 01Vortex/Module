package com.vortex.loginregister_new.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试控制器 - 用于测试日志颜色高亮
 *
 * @author Vortex
 * @since 2024
 */
@Slf4j
@RestController
@RequestMapping("/test")
public class TestController {

    /**
     * 测试各种日志级别的颜色高亮
     */
    @GetMapping("/log")
    public Map<String, Object> testLog() {
        log.trace("🔍 这是 TRACE 级别日志 - 最详细的追踪信息");
        log.debug("🐛 这是 DEBUG 级别日志 - 调试信息");
        log.info("✅ 这是 INFO 级别日志 - 一般信息");
        log.warn("⚠️ 这是 WARN 级别日志 - 警告信息");
        log.error("❌ 这是 ERROR 级别日志 - 错误信息");

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "日志测试完成！请查看控制台的彩色输出");
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    /**
     * 测试异常日志
     */
    @GetMapping("/log-error")
    public Map<String, Object> testErrorLog() {
        try {
            // 故意抛出异常来测试错误日志
            @SuppressWarnings("unused")
            int num = 10 / 0;
        } catch (Exception e) {
            log.error("💥 发生异常啦！", e);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "异常日志测试完成！请查看控制台的红色错误堆栈");
        return result;
    }

    /**
     * 测试多行日志
     */
    @GetMapping("/log-multi")
    public Map<String, Object> testMultiLineLog() {
        log.info("====================================");
        log.info("开始执行批量操作");
        log.info("====================================");
        
        for (int i = 1; i <= 5; i++) {
            log.debug("处理第 {} 条数据", i);
            
            if (i == 3) {
                log.warn("第 {} 条数据存在警告：数据可能不完整", i);
            }
            
            if (i == 4) {
                log.error("第 {} 条数据处理失败：数据格式错误", i);
            }
        }
        
        log.info("====================================");
        log.info("批量操作完成");
        log.info("====================================");

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "多行日志测试完成！");
        return result;
    }
}

