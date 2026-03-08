package com.example.sensorspring.controller;

import com.example.sensorspring.cache.CacheManager;
import com.example.sensorspring.monitor.PerformanceMonitor;
import com.example.sensorspring.dto.SimpleResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 监控控制器
 * 参照 Sensor_Server 的 monitor_controller.py 实现
 */
@RestController
@RequestMapping("/api/monitor")
public class MonitorController {
    
    private final CacheManager cacheManager;
    private final PerformanceMonitor performanceMonitor;
    
    public MonitorController(CacheManager cacheManager, PerformanceMonitor performanceMonitor) {
        this.cacheManager = cacheManager;
        this.performanceMonitor = performanceMonitor;
    }
    
    /**
     * 系统健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "healthy");
        health.put("timestamp", java.time.Instant.now().toString());
        
        Map<String, Object> components = new HashMap<>();
        
        // 检查Redis
        if (cacheManager.isEnabled()) {
            components.put("redis", "healthy");
        } else {
            components.put("redis", "disabled");
        }
        
        // 检查监控
        if (performanceMonitor.isEnabled()) {
            components.put("monitoring", "healthy");
        } else {
            components.put("monitoring", "disabled");
        }
        
        health.put("components", components);
        return ResponseEntity.ok(health);
    }
    
    /**
     * 获取所有统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getAllStats() {
        return ResponseEntity.ok(performanceMonitor.getAllStats());
    }
    
    /**
     * 获取缓存统计
     */
    @GetMapping("/cache")
    public ResponseEntity<Map<String, Object>> getCacheStats() {
        Map<String, Object> result = new HashMap<>();
        result.put("performance", performanceMonitor.getCacheStats());
        result.put("cacheManager", cacheManager.getStats());
        return ResponseEntity.ok(result);
    }
    
    /**
     * 获取数据库统计
     */
    @GetMapping("/database")
    public ResponseEntity<Map<String, Object>> getDatabaseStats() {
        return ResponseEntity.ok(performanceMonitor.getDatabaseStats());
    }
    
    /**
     * 获取系统统计
     */
    @GetMapping("/system")
    public ResponseEntity<Map<String, Object>> getSystemStats() {
        return ResponseEntity.ok(performanceMonitor.getSystemStats());
    }
    
    /**
     * 清除所有缓存
     */
    @PostMapping("/cache/clear")
    public ResponseEntity<SimpleResponse> clearCache() {
        long count = cacheManager.clearAll();
        return ResponseEntity.ok(new SimpleResponse("已清除 " + count + " 个缓存"));
    }
    
    /**
     * 重置性能统计
     */
    @PostMapping("/stats/reset")
    public ResponseEntity<SimpleResponse> resetStats() {
        performanceMonitor.resetStats();
        return ResponseEntity.ok(new SimpleResponse("性能统计已重置"));
    }
}
