package com.example.sensorspring.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 性能监控器
 * 参照 Sensor_Server 的 monitor.py 实现
 */
@Component
public class PerformanceMonitor {
    
    private static final Logger logger = LoggerFactory.getLogger(PerformanceMonitor.class);
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final boolean enabled;
    private final double sampleRate;
    
    // 统计计数器
    private final AtomicLong userCacheHits = new AtomicLong(0);
    private final AtomicLong userCacheMisses = new AtomicLong(0);
    private final AtomicLong fileCacheHits = new AtomicLong(0);
    private final AtomicLong fileCacheMisses = new AtomicLong(0);
    private final AtomicLong membershipCacheHits = new AtomicLong(0);
    private final AtomicLong membershipCacheMisses = new AtomicLong(0);
    
    // 数据库查询统计
    private final AtomicLong dbSelectCount = new AtomicLong(0);
    private final AtomicLong dbSelectDuration = new AtomicLong(0);
    private final AtomicLong dbInsertCount = new AtomicLong(0);
    private final AtomicLong dbInsertDuration = new AtomicLong(0);
    private final AtomicLong dbUpdateCount = new AtomicLong(0);
    private final AtomicLong dbUpdateDuration = new AtomicLong(0);
    private final AtomicLong dbDeleteCount = new AtomicLong(0);
    private final AtomicLong dbDeleteDuration = new AtomicLong(0);
    
    private static final String MONITOR_PREFIX = "monitor:";
    
    public PerformanceMonitor(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.enabled = redisTemplate != null;
        this.sampleRate = 0.1; // 10%采样率
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * 记录缓存命中
     */
    public void recordCacheHit(String cacheType, String key) {
        if (!shouldSample()) return;
        
        switch (cacheType.toLowerCase()) {
            case "user":
                userCacheHits.incrementAndGet();
                break;
            case "file":
                fileCacheHits.incrementAndGet();
                break;
            case "membership":
                membershipCacheHits.incrementAndGet();
                break;
        }
        
        if (enabled) {
            try {
                redisTemplate.opsForHash().increment(MONITOR_PREFIX + "cache:stats", cacheType + "_hits", 1);
            } catch (Exception e) {
                logger.error("记录缓存命中失败", e);
            }
        }
    }
    
    /**
     * 记录缓存未命中
     */
    public void recordCacheMiss(String cacheType, String key) {
        if (!shouldSample()) return;
        
        switch (cacheType.toLowerCase()) {
            case "user":
                userCacheMisses.incrementAndGet();
                break;
            case "file":
                fileCacheMisses.incrementAndGet();
                break;
            case "membership":
                membershipCacheMisses.incrementAndGet();
                break;
        }
        
        if (enabled) {
            try {
                redisTemplate.opsForHash().increment(MONITOR_PREFIX + "cache:stats", cacheType + "_misses", 1);
            } catch (Exception e) {
                logger.error("记录缓存未命中失败", e);
            }
        }
    }
    
    /**
     * 记录数据库查询
     */
    public void recordDatabaseQuery(String queryType, long durationMs, boolean success) {
        if (!shouldSample()) return;
        
        switch (queryType.toLowerCase()) {
            case "select":
                dbSelectCount.incrementAndGet();
                dbSelectDuration.addAndGet(durationMs);
                break;
            case "insert":
                dbInsertCount.incrementAndGet();
                dbInsertDuration.addAndGet(durationMs);
                break;
            case "update":
                dbUpdateCount.incrementAndGet();
                dbUpdateDuration.addAndGet(durationMs);
                break;
            case "delete":
                dbDeleteCount.incrementAndGet();
                dbDeleteDuration.addAndGet(durationMs);
                break;
        }
        
        if (enabled) {
            try {
                String key = MONITOR_PREFIX + "db:" + queryType;
                redisTemplate.opsForHash().increment(key, "count", 1);
                redisTemplate.opsForHash().increment(key, "total_duration", durationMs);
                if (!success) {
                    redisTemplate.opsForHash().increment(key, "errors", 1);
                }
            } catch (Exception e) {
                logger.error("记录数据库查询失败", e);
            }
        }
    }
    
    /**
     * 获取缓存统计
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("userHits", userCacheHits.get());
        stats.put("userMisses", userCacheMisses.get());
        stats.put("fileHits", fileCacheHits.get());
        stats.put("fileMisses", fileCacheMisses.get());
        stats.put("membershipHits", membershipCacheHits.get());
        stats.put("membershipMisses", membershipCacheMisses.get());
        
        long totalHits = userCacheHits.get() + fileCacheHits.get() + membershipCacheHits.get();
        long totalMisses = userCacheMisses.get() + fileCacheMisses.get() + membershipCacheMisses.get();
        
        stats.put("totalHits", totalHits);
        stats.put("totalMisses", totalMisses);
        
        long total = totalHits + totalMisses;
        stats.put("hitRate", total > 0 ? Math.round(totalHits * 100.0 / total) / 100.0 : 0);
        
        return stats;
    }
    
    /**
     * 获取数据库统计
     */
    public Map<String, Object> getDatabaseStats() {
        Map<String, Object> stats = new HashMap<>();
        
        long selectCount = dbSelectCount.get();
        long insertCount = dbInsertCount.get();
        long updateCount = dbUpdateCount.get();
        long deleteCount = dbDeleteCount.get();
        
        stats.put("select", createQueryStats(selectCount, dbSelectDuration.get()));
        stats.put("insert", createQueryStats(insertCount, dbInsertDuration.get()));
        stats.put("update", createQueryStats(updateCount, dbUpdateDuration.get()));
        stats.put("delete", createQueryStats(deleteCount, dbDeleteDuration.get()));
        
        return stats;
    }
    
    private Map<String, Object> createQueryStats(long count, long totalDuration) {
        Map<String, Object> queryStats = new HashMap<>();
        queryStats.put("count", count);
        queryStats.put("totalDuration", totalDuration);
        queryStats.put("avgDuration", count > 0 ? Math.round(totalDuration * 100.0 / count) / 100.0 : 0);
        return queryStats;
    }
    
    /**
     * 获取系统统计
     */
    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            SystemInfo systemInfo = new SystemInfo();
            OperatingSystem os = systemInfo.getOperatingSystem();
            CentralProcessor processor = systemInfo.getHardware().getProcessor();
            GlobalMemory memory = systemInfo.getHardware().getMemory();
            FileSystem fileSystem = os.getFileSystem();
            
            // CPU使用率
            double cpuLoad = processor.getSystemLoadAverage(1)[0];
            if (cpuLoad < 0) cpuLoad = 0;
            stats.put("cpuPercent", Math.round(cpuLoad * 100) / 100.0);
            
            // 内存使用
            long totalMemory = memory.getTotal();
            long availableMemory = memory.getAvailable();
            long usedMemory = totalMemory - availableMemory;
            double memoryPercent = (usedMemory * 100.0) / totalMemory;
            
            stats.put("memoryPercent", Math.round(memoryPercent * 100) / 100.0);
            stats.put("memoryUsedMb", usedMemory / 1024 / 1024);
            stats.put("memoryTotalMb", totalMemory / 1024 / 1024);
            
            // 磁盘使用
            long totalDisk = 0;
            long usedDisk = 0;
            for (OSFileStore store : fileSystem.getFileStores()) {
                totalDisk += store.getTotalSpace();
                usedDisk += store.getTotalSpace() - store.getUsableSpace();
            }
            double diskPercent = totalDisk > 0 ? (usedDisk * 100.0) / totalDisk : 0;
            stats.put("diskUsagePercent", Math.round(diskPercent * 100) / 100.0);
            
            // 线程数
            stats.put("activeThreads", Thread.activeCount());
            
            stats.put("timestamp", Instant.now().toString());
            
        } catch (Exception e) {
            logger.error("获取系统统计失败", e);
            stats.put("error", e.getMessage());
        }
        
        return stats;
    }
    
    /**
     * 获取所有统计信息
     */
    public Map<String, Object> getAllStats() {
        Map<String, Object> allStats = new HashMap<>();
        allStats.put("cache", getCacheStats());
        allStats.put("database", getDatabaseStats());
        allStats.put("system", getSystemStats());
        allStats.put("monitoring", Map.of(
            "enabled", enabled,
            "sampleRate", sampleRate
        ));
        return allStats;
    }
    
    /**
     * 是否应该采样
     */
    private boolean shouldSample() {
        return Math.random() < sampleRate;
    }
    
    /**
     * 重置统计
     */
    public void resetStats() {
        userCacheHits.set(0);
        userCacheMisses.set(0);
        fileCacheHits.set(0);
        fileCacheMisses.set(0);
        membershipCacheHits.set(0);
        membershipCacheMisses.set(0);
        
        dbSelectCount.set(0);
        dbSelectDuration.set(0);
        dbInsertCount.set(0);
        dbInsertDuration.set(0);
        dbUpdateCount.set(0);
        dbUpdateDuration.set(0);
        dbDeleteCount.set(0);
        dbDeleteDuration.set(0);
        
        logger.info("性能监控统计已重置");
    }
}
