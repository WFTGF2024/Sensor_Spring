package com.example.sensorspring.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * 用户会员关系实体
 * 参照 Sensor_Server 的 user_memberships 表设计
 */
@Entity
@Table(name = "user_memberships", indexes = {
    @Index(name = "idx_user_memberships_user", columnList = "user_id"),
    @Index(name = "idx_user_memberships_level", columnList = "level_id")
})
public class UserMembership {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "level_id", nullable = false)
    private MembershipLevel level;
    
    @Column(name = "start_date", nullable = false)
    private Instant startDate;
    
    @Column(name = "end_date")
    private Instant endDate; // null表示永久会员
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "storage_used", nullable = false)
    private Long storageUsed = 0L; // 已使用存储空间（字节）
    
    @Column(name = "file_count", nullable = false)
    private Integer fileCount = 0; // 文件数量
    
    @Column(name = "points_earned")
    private Integer pointsEarned = 0; // 获得的积分
    
    @Column(name = "auto_renew", nullable = false)
    private Boolean autoRenew = false; // 是否自动续费
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public MembershipLevel getLevel() { return level; }
    public void setLevel(MembershipLevel level) { this.level = level; }
    
    public Instant getStartDate() { return startDate; }
    public void setStartDate(Instant startDate) { this.startDate = startDate; }
    
    public Instant getEndDate() { return endDate; }
    public void setEndDate(Instant endDate) { this.endDate = endDate; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public Long getStorageUsed() { return storageUsed; }
    public void setStorageUsed(Long storageUsed) { this.storageUsed = storageUsed; }
    
    public Integer getFileCount() { return fileCount; }
    public void setFileCount(Integer fileCount) { this.fileCount = fileCount; }
    
    public Integer getPointsEarned() { return pointsEarned; }
    public void setPointsEarned(Integer pointsEarned) { this.pointsEarned = pointsEarned; }
    
    public Boolean getAutoRenew() { return autoRenew; }
    public void setAutoRenew(Boolean autoRenew) { this.autoRenew = autoRenew; }
    
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    
    // 计算属性
    public boolean isExpired() {
        return endDate != null && endDate.isBefore(Instant.now());
    }
    
    public boolean isPermanent() {
        return endDate == null;
    }
    
    public double getStorageUsagePercentage() {
        if (level == null || level.getStorageLimit() == 0) return 0;
        return (storageUsed * 100.0) / level.getStorageLimit();
    }
    
    public boolean isStorageFull() {
        return level != null && storageUsed >= level.getStorageLimit();
    }
}
