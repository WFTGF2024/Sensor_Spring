package com.example.sensorspring.controller;

import com.example.sensorspring.dto.LoginRequest;
import com.example.sensorspring.dto.RegisterRequest;
import com.example.sensorspring.dto.SimpleResponse;
import com.example.sensorspring.dto.TokenResponse;
import com.example.sensorspring.entity.User;
import com.example.sensorspring.exception.BadRequestException;
import com.example.sensorspring.security.JwtService;
import com.example.sensorspring.service.AuditService;
import com.example.sensorspring.service.TokenService;
import com.example.sensorspring.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService users;
    private final AuthenticationManager authManager;
    private final JwtService jwt;
    private final TokenService tokens;
    private final AuditService audit;

    public AuthController(UserService users, AuthenticationManager authManager, JwtService jwt, TokenService tokens, AuditService audit) {
        this.users = users; this.authManager = authManager; this.jwt = jwt; this.tokens = tokens; this.audit = audit;
    }

    @PostMapping("/register")
    public ResponseEntity<SimpleResponse> register(@Valid @RequestBody RegisterRequest req, HttpServletRequest httpReq) {
        User u = users.register(req.getUsername(), req.getEmail(), req.getPassword());
        audit.log(u.getId(), "REGISTER", "User", String.valueOf(u.getId()), httpReq, 200);
        return ResponseEntity.ok(new SimpleResponse("注册成功"));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest req, HttpServletRequest httpReq) {
        Authentication auth = authManager.authenticate(new UsernamePasswordAuthenticationToken(req.getUsernameOrEmail(), req.getPassword()));
        User u = users.getByUsernameOrEmail(req.getUsernameOrEmail());
        String access = jwt.generateAccessToken(u.getId(), u.getUsername(), u.getRole().name());
        String refresh = jwt.generateRefreshToken(u.getId());
        String refreshJti = jwt.parse(refresh).getBody().getId();
        long ttlMillis = jwt.parse(refresh).getBody().getExpiration().getTime() - Instant.now().toEpochMilli();
        tokens.storeRefreshToken(refreshJti, u.getId(), ttlMillis);
        audit.log(u.getId(), "LOGIN", "User", String.valueOf(u.getId()), httpReq, 200);
        return ResponseEntity.ok(new TokenResponse(access, refresh));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestParam("refreshToken") String refreshToken, HttpServletRequest httpReq) {
        var jws = jwt.parse(refreshToken);
        if (!jwt.isRefreshToken(refreshToken)) throw new BadRequestException("非法的刷新令牌");
        String jti = jws.getBody().getId();
        Long userId = Long.valueOf(jws.getBody().getSubject());
        if (!tokens.isRefreshTokenValid(jti)) throw new BadRequestException("刷新令牌已失效");
        User u = users.getById(userId);
        String access = jwt.generateAccessToken(u.getId(), u.getUsername(), u.getRole().name());
        audit.log(u.getId(), "REFRESH_TOKEN", "User", String.valueOf(u.getId()), httpReq, 200);
        return ResponseEntity.ok(new TokenResponse(access, refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<SimpleResponse> logout(@RequestHeader(name="Authorization", required=false) String authHeader,
                                                 @RequestParam(value="refreshToken", required=false) String refreshToken,
                                                 HttpServletRequest httpReq) {
        Long userId = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            var jws = jwt.parse(authHeader.substring(7));
            userId = Long.valueOf(jws.getBody().getSubject());
            String jti = jws.getBody().getId();
            long ttlMillis = jws.getBody().getExpiration().getTime() - System.currentTimeMillis();
            if (ttlMillis > 0) tokens.blacklistAccessToken(jti, ttlMillis);
        }
        if (refreshToken != null && !refreshToken.isBlank()) {
            var jws2 = jwt.parse(refreshToken);
            tokens.revokeRefreshToken(jws2.getBody().getId());
            if (userId == null) userId = Long.valueOf(jws2.getBody().getSubject());
        }
        audit.log(userId, "LOGOUT", "User", userId == null ? null : String.valueOf(userId), httpReq, 200);
        return ResponseEntity.ok(new SimpleResponse("已登出"));
    }
}
