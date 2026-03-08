package com.example.sensorspring.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * 会员等级实体
 * 参照 Sensor_Server 的 membership_levels 表设计
 */
@Entity
@Table(name = "membership_levels")
public class MembershipLevel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "level_name", nullable = false, length = 50)
    private String levelName;
    
    @Column(name = "level_code", nullable = false, unique = true, length = 20)
    private String levelCode;
    
    @Column(name = "display_order")
    private Integer displayOrder;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "storage_limit", nullable = false)
    private Long storageLimit; // 存储容量限制（字节）
    
    @Column(name = "max_file_size", nullable = false)
    private Long maxFileSize; // 单文件大小限制（字节）
    
    @Column(name = "max_file_count", nullable = false)
    private Integer maxFileCount; // 文件数量限制
    
    @Column(name = "download_speed_limit")
    private Long downloadSpeedLimit; // 下载速度限制（字节/秒），0表示无限制
    
    @Column(name = "upload_speed_limit")
    private Long uploadSpeedLimit; // 上传速度限制（字节/秒），0表示无限制
    
    @Column(name = "daily_download_limit")
    private Integer dailyDownloadLimit; // 每日下载次数限制，0表示无限制
    
    @Column(name = "daily_upload_limit")
    private Integer dailyUploadLimit; // 每日上传次数限制，0表示无限制
    
    @Column(name = "can_share_files", nullable = false)
    private Boolean canShareFiles = false; // 是否可以分享文件
    
    @Column(name = "can_create_public_links", nullable = false)
    private Boolean canCreatePublicLinks = false; // 是否可以创建公开链接
    
    private Integer priority = 1;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
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
    
    public String getLevelName() { return levelName; }
    public void setLevelName(String levelName) { this.levelName = levelName; }
    
    public String getLevelCode() { return levelCode; }
    public void setLevelCode(String levelCode) { this.levelCode = levelCode; }
    
    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Long getStorageLimit() { return storageLimit; }
    public void setStorageLimit(Long storageLimit) { this.storageLimit = storageLimit; }
    
    public Long getMaxFileSize() { return maxFileSize; }
    public void setMaxFileSize(Long maxFileSize) { this.maxFileSize = maxFileSize; }
    
    public Integer getMaxFileCount() { return maxFileCount; }
    public void setMaxFileCount(Integer maxFileCount) { this.maxFileCount = maxFileCount; }
    
    public Long getDownloadSpeedLimit() { return downloadSpeedLimit; }
    public void setDownloadSpeedLimit(Long downloadSpeedLimit) { this.downloadSpeedLimit = downloadSpeedLimit; }
    
    public Long getUploadSpeedLimit() { return uploadSpeedLimit; }
    public void setUploadSpeedLimit(Long uploadSpeedLimit) { this.uploadSpeedLimit = uploadSpeedLimit; }
    
    public Integer getDailyDownloadLimit() { return dailyDownloadLimit; }
    public void setDailyDownloadLimit(Integer dailyDownloadLimit) { this.dailyDownloadLimit = dailyDownloadLimit; }
    
    public Integer getDailyUploadLimit() { return dailyUploadLimit; }
    public void setDailyUploadLimit(Integer dailyUploadLimit) { this.dailyUploadLimit = dailyUploadLimit; }
    
    public Boolean getCanShareFiles() { return canShareFiles; }
    public void setCanShareFiles(Boolean canShareFiles) { this.canShareFiles = canShareFiles; }
    
    public Boolean getCanCreatePublicLinks() { return canCreatePublicLinks; }
    public void setCanCreatePublicLinks(Boolean canCreatePublicLinks) { this.canCreatePublicLinks = canCreatePublicLinks; }
    
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
