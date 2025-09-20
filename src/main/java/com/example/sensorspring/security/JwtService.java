package com.example.sensorspring.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {

    private final SecretKey key;
    private final long accessTtlMillis;
    private final long refreshTtlMillis;

    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.access-ttl-minutes:15}") long accessTtlMinutes,
                      @Value("${jwt.refresh-ttl-days:7}") long refreshTtlDays) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTtlMillis = accessTtlMinutes * 60 * 1000;
        this.refreshTtlMillis = refreshTtlDays * 24 * 60 * 60 * 1000;
    }

    public String generateAccessToken(Long userId, String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("uid", userId);
        claims.put("un", username);
        claims.put("rol", role);
        String jti = UUID.randomUUID().toString();
        Instant now = Instant.now();
        return Jwts.builder()
                .setId(jti)
                .setSubject(String.valueOf(userId))
                .setClaims(claims)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusMillis(accessTtlMillis)))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public String generateRefreshToken(Long userId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(String.valueOf(userId))
                .claim("typ", "refresh")
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusMillis(refreshTtlMillis)))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public Jws<Claims> parse(String token) { return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token); }
    public String getJti(String token) { return parse(token).getBody().getId(); }
    public Long getUserId(String token) { return Long.valueOf(parse(token).getBody().getSubject()); }
    public boolean isRefreshToken(String token) { Object typ = parse(token).getBody().get("typ"); return "refresh".equals(typ); }
}
