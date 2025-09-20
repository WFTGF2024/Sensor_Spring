package com.example.sensorspring.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="audit_logs", indexes = { @Index(name="idx_audit_user_time", columnList = "user_id, created_at") })
public class AuditLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name="user_id") private Long userId;
    @Column(nullable=false, length=64) private String action;
    @Column(name="target_type", length=64) private String targetType;
    @Column(name="target_id", length=64) private String targetId;
    @Column(length=64) private String ip;
    @Column(name="user_agent", length=255) private String userAgent;
    @Column(name="request_id", length=64) private String requestId;
    @Column(length=8) private String method;
    @Column(length=255) private String path;
    private Integer status;
    @Column(name="created_at", nullable=false) private Instant createdAt = Instant.now();
    // getters/setters
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; } public void setUserId(Long userId) { this.userId = userId; }
    public String getAction() { return action; } public void setAction(String action) { this.action = action; }
    public String getTargetType() { return targetType; } public void setTargetType(String targetType) { this.targetType = targetType; }
    public String getTargetId() { return targetId; } public void setTargetId(String targetId) { this.targetId = targetId; }
    public String getIp() { return ip; } public void setIp(String ip) { this.ip = ip; }
    public String getUserAgent() { return userAgent; } public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    public String getRequestId() { return requestId; } public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getMethod() { return method; } public void setMethod(String method) { this.method = method; }
    public String getPath() { return path; } public void setPath(String path) { this.path = path; }
    public Integer getStatus() { return status; } public void setStatus(Integer status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; } public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
