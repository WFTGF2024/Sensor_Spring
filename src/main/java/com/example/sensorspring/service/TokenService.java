package com.example.sensorspring.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;

@Service
public class TokenService {
    private final StringRedisTemplate redis;
    public TokenService(StringRedisTemplate redis){ this.redis=redis; }
    public void storeRefreshToken(String jti, Long userId, long ttlMillis){ redis.opsForValue().set("refresh:"+jti, String.valueOf(userId), Duration.ofMillis(ttlMillis)); }
    public boolean isRefreshTokenValid(String jti){ return Boolean.TRUE.equals(redis.hasKey("refresh:"+jti)); }
    public void revokeRefreshToken(String jti){ redis.delete("refresh:"+jti); }
    public void blacklistAccessToken(String jti, long ttlMillis){ redis.opsForValue().set("blacklist:"+jti, "1", Duration.ofMillis(ttlMillis)); }
}
