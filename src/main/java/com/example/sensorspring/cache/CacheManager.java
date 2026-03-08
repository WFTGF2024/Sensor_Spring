package com.example.sensorspring.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 缓存管理器
 * 参照 Sensor_Server 的 cache_utils.py 实现
 */
@Component
public class CacheManager {
    
    private static final Logger logger = LoggerFactory.getLogger(CacheManager.class);
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final boolean enabled;
    private final long defaultTtl;
    
    // 缓存键前缀
    private static final String USER_PREFIX = "user:";
    private static final String FILE_PREFIX = "file:";
    private static final String MEMBERSHIP_PREFIX = "membership:";
    
    public CacheManager(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = new ObjectMapper();
        this.enabled = redisTemplate != null;
        this.defaultTtl = 3600; // 默认1小时
    }
    
    /**
     * 检查Redis是否启用
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * 缓存用户信息
     */
    public void cacheUser(Long userId, Object userData) {
        cacheUser(userId, userData, null);
    }
    
    public void cacheUser(Long userId, Object userData, Long ttl) {
        if (!enabled) return;
        try {
            String key = USER_PREFIX + userId;
            long expireTime = ttl != null ? ttl : defaultTtl;
            redisTemplate.opsForValue().set(key, userData, expireTime, TimeUnit.SECONDS);
            logger.debug("缓存用户: {}", key);
        } catch (Exception e) {
            logger.error("缓存用户失败: userId={}", userId, e);
        }
    }
    
    /**
     * 获取缓存的用户信息
     */
    public Object getUser(Long userId) {
        if (!enabled) return null;
        try {
            String key = USER_PREFIX + userId;
            Object value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                logger.debug("缓存命中: {}", key);
            }
            return value;
        } catch (Exception e) {
            logger.error("获取用户缓存失败: userId={}", userId, e);
            return null;
        }
    }
    
    /**
     * 使用户缓存失效
     */
    public void invalidateUser(Long userId) {
        if (!enabled) return;
        try {
            String key = USER_PREFIX + userId;
            redisTemplate.delete(key);
            logger.debug("用户缓存失效: {}", key);
        } catch (Exception e) {
            logger.error("使用户缓存失效失败: userId={}", userId, e);
        }
    }
    
    /**
     * 缓存文件信息
     */
    public void cacheFile(Long fileId, Object fileData) {
        cacheFile(fileId, fileData, null);
    }
    
    public void cacheFile(Long fileId, Object fileData, Long ttl) {
        if (!enabled) return;
        try {
            String key = FILE_PREFIX + fileId;
            long expireTime = ttl != null ? ttl : defaultTtl;
            redisTemplate.opsForValue().set(key, fileData, expireTime, TimeUnit.SECONDS);
            logger.debug("缓存文件: {}", key);
        } catch (Exception e) {
            logger.error("缓存文件失败: fileId={}", fileId, e);
        }
    }
    
    /**
     * 获取缓存的文件信息
     */
    public Object getFile(Long fileId) {
        if (!enabled) return null;
        try {
            String key = FILE_PREFIX + fileId;
            Object value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                logger.debug("缓存命中: {}", key);
            }
            return value;
        } catch (Exception e) {
            logger.error("获取文件缓存失败: fileId={}", fileId, e);
            return null;
        }
    }
    
    /**
     * 使文件缓存失效
     */
    public void invalidateFile(Long fileId) {
        if (!enabled) return;
        try {
            String key = FILE_PREFIX + fileId;
            redisTemplate.delete(key);
            logger.debug("文件缓存失效: {}", key);
        } catch (Exception e) {
            logger.error("使文件缓存失效失败: fileId={}", fileId, e);
        }
    }
    
    /**
     * 缓存会员信息
     */
    public void cacheMembership(Long userId, Object membershipData) {
        cacheMembership(userId, membershipData, null);
    }
    
    public void cacheMembership(Long userId, Object membershipData, Long ttl) {
        if (!enabled) return;
        try {
            String key = MEMBERSHIP_PREFIX + userId;
            long expireTime = ttl != null ? ttl : defaultTtl;
            redisTemplate.opsForValue().set(key, membershipData, expireTime, TimeUnit.SECONDS);
            logger.debug("缓存会员: {}", key);
        } catch (Exception e) {
            logger.error("缓存会员失败: userId={}", userId, e);
        }
    }
    
    /**
     * 获取缓存的会员信息
     */
    public Object getMembership(Long userId) {
        if (!enabled) return null;
        try {
            String key = MEMBERSHIP_PREFIX + userId;
            Object value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                logger.debug("缓存命中: {}", key);
            }
            return value;
        } catch (Exception e) {
            logger.error("获取会员缓存失败: userId={}", userId, e);
            return null;
        }
    }
    
    /**
     * 使会员缓存失效
     */
    public void invalidateMembership(Long userId) {
        if (!enabled) return;
        try {
            String key = MEMBERSHIP_PREFIX + userId;
            redisTemplate.delete(key);
            logger.debug("会员缓存失效: {}", key);
        } catch (Exception e) {
            logger.error("使会员缓存失效失败: userId={}", userId, e);
        }
    }
    
    /**
     * 清除匹配模式的所有缓存
     */
    public long clearPattern(String pattern) {
        if (!enabled) return 0;
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                Long deleted = redisTemplate.delete(keys);
                logger.debug("清除缓存模式 {}: {} 个键", pattern, deleted);
                return deleted != null ? deleted : 0;
            }
            return 0;
        } catch (Exception e) {
            logger.error("清除缓存模式失败: pattern={}", pattern, e);
            return 0;
        }
    }
    
    /**
     * 清除所有缓存
     */
    public long clearAll() {
        long count = 0;
        count += clearPattern(USER_PREFIX + "*");
        count += clearPattern(FILE_PREFIX + "*");
        count += clearPattern(MEMBERSHIP_PREFIX + "*");
        return count;
    }
    
    /**
     * 获取缓存统计信息
     */
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("enabled", enabled);
        
        if (!enabled) {
            return stats;
        }
        
        try {
            // 统计各种缓存的数量
            Set<String> userKeys = redisTemplate.keys(USER_PREFIX + "*");
            Set<String> fileKeys = redisTemplate.keys(FILE_PREFIX + "*");
            Set<String> membershipKeys = redisTemplate.keys(MEMBERSHIP_PREFIX + "*");
            
            stats.put("userCacheCount", userKeys != null ? userKeys.size() : 0);
            stats.put("fileCacheCount", fileKeys != null ? fileKeys.size() : 0);
            stats.put("membershipCacheCount", membershipKeys != null ? membershipKeys.size() : 0);
            stats.put("totalCacheCount", 
                (userKeys != null ? userKeys.size() : 0) +
                (fileKeys != null ? fileKeys.size() : 0) +
                (membershipKeys != null ? membershipKeys.size() : 0));
        } catch (Exception e) {
            logger.error("获取缓存统计失败", e);
        }
        
        return stats;
    }
}
