package com.example.sensorspring.entity;

import com.example.sensorspring.security.Role;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "users",
       uniqueConstraints = {
         @UniqueConstraint(name = "uq_users_username", columnNames = "username"),
         @UniqueConstraint(name = "uq_users_email", columnNames = "email"),
         @UniqueConstraint(name = "uq_users_phone", columnNames = "phone")
       })
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false, length=64) private String username;
    @Column(nullable=false, length=128) private String email;
    @Column(length=32) private String phone;
    @Column(name="password_hash", nullable=false, length=255) private String passwordHash;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=16) private Role role = Role.USER;
    @Column(length=32) private String qq;
    @Column(length=64) private String wechat;
    @Column(nullable=false) private Integer point = 0;
    @Column(name="created_at", nullable=false) private Instant createdAt = Instant.now();
    @Column(name="updated_at", nullable=false) private Instant updatedAt = Instant.now();
    @PreUpdate public void preUpdate() { this.updatedAt = Instant.now(); }
    // getters/setters
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; } public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; } public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; } public void setPhone(String phone) { this.phone = phone; }
    public String getPasswordHash() { return passwordHash; } public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public Role getRole() { return role; } public void setRole(Role role) { this.role = role; }
    public String getQq() { return qq; } public void setQq(String qq) { this.qq = qq; }
    public String getWechat() { return wechat; } public void setWechat(String wechat) { this.wechat = wechat; }
    public Integer getPoint() { return point; } public void setPoint(Integer point) { this.point = point; }
    public Instant getCreatedAt() { return createdAt; } public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; } public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
