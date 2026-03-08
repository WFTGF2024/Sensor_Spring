package com.example.sensorspring.dto;

import java.time.Instant;

/**
 * 会员信息响应
 */
public class MembershipInfoResponse {
    private Long membershipId;
    private Long userId;
    private Long levelId;
    private String levelName;
    private String levelCode;
    private Long storageLimit;
    private String storageLimitFormatted;
    private Long maxFileSize;
    private String maxFileSizeFormatted;
    private Integer maxFileCount;
    private Long storageUsed;
    private String storageUsedFormatted;
    private Integer fileCount;
    private Double storageUsagePercentage;
    private Boolean storageFull;
    private Instant startDate;
    private Instant endDate;
    private String endDateFormatted;
    private Boolean active;
    private Boolean canShareFiles;
    private Boolean canCreatePublicLinks;
    
    // Getters and Setters
    public Long getMembershipId() { return membershipId; }
    public void setMembershipId(Long membershipId) { this.membershipId = membershipId; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public Long getLevelId() { return levelId; }
    public void setLevelId(Long levelId) { this.levelId = levelId; }
    
    public String getLevelName() { return levelName; }
    public void setLevelName(String levelName) { this.levelName = levelName; }
    
    public String getLevelCode() { return levelCode; }
    public void setLevelCode(String levelCode) { this.levelCode = levelCode; }
    
    public Long getStorageLimit() { return storageLimit; }
    public void setStorageLimit(Long storageLimit) { this.storageLimit = storageLimit; }
    
    public String getStorageLimitFormatted() { return storageLimitFormatted; }
    public void setStorageLimitFormatted(String storageLimitFormatted) { this.storageLimitFormatted = storageLimitFormatted; }
    
    public Long getMaxFileSize() { return maxFileSize; }
    public void setMaxFileSize(Long maxFileSize) { this.maxFileSize = maxFileSize; }
    
    public String getMaxFileSizeFormatted() { return maxFileSizeFormatted; }
    public void setMaxFileSizeFormatted(String maxFileSizeFormatted) { this.maxFileSizeFormatted = maxFileSizeFormatted; }
    
    public Integer getMaxFileCount() { return maxFileCount; }
    public void setMaxFileCount(Integer maxFileCount) { this.maxFileCount = maxFileCount; }
    
    public Long getStorageUsed() { return storageUsed; }
    public void setStorageUsed(Long storageUsed) { this.storageUsed = storageUsed; }
    
    public String getStorageUsedFormatted() { return storageUsedFormatted; }
    public void setStorageUsedFormatted(String storageUsedFormatted) { this.storageUsedFormatted = storageUsedFormatted; }
    
    public Integer getFileCount() { return fileCount; }
    public void setFileCount(Integer fileCount) { this.fileCount = fileCount; }
    
    public Double getStorageUsagePercentage() { return storageUsagePercentage; }
    public void setStorageUsagePercentage(Double storageUsagePercentage) { this.storageUsagePercentage = storageUsagePercentage; }
    
    public Boolean getStorageFull() { return storageFull; }
    public void setStorageFull(Boolean storageFull) { this.storageFull = storageFull; }
    
    public Instant getStartDate() { return startDate; }
    public void setStartDate(Instant startDate) { this.startDate = startDate; }
    
    public Instant getEndDate() { return endDate; }
    public void setEndDate(Instant endDate) { this.endDate = endDate; }
    
    public String getEndDateFormatted() { return endDateFormatted; }
    public void setEndDateFormatted(String endDateFormatted) { this.endDateFormatted = endDateFormatted; }
    
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    
    public Boolean getCanShareFiles() { return canShareFiles; }
    public void setCanShareFiles(Boolean canShareFiles) { this.canShareFiles = canShareFiles; }
    
    public Boolean getCanCreatePublicLinks() { return canCreatePublicLinks; }
    public void setCanCreatePublicLinks(Boolean canCreatePublicLinks) { this.canCreatePublicLinks = canCreatePublicLinks; }
}
