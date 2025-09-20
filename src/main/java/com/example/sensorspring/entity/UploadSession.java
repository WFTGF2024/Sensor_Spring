package com.example.sensorspring.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="upload_sessions")
public class UploadSession {
    @Id @Column(name="upload_id", length=36) private String uploadId;
    @ManyToOne(optional=false, fetch=FetchType.LAZY) @JoinColumn(name="owner_id", nullable=false) private User owner;
    @Column(name="file_name", nullable=false, length=255) private String fileName;
    @Column(name="total_size", nullable=false) private Long totalSize;
    @Column(name="total_chunks", nullable=false) private Integer totalChunks;
    @Column(name="received_chunks", nullable=false) private Integer receivedChunks = 0;
    @Column(name="temp_dir", nullable=false, length=512) private String tempDir;
    @Column(name="status", nullable=false, length=16) private String status = "IN_PROGRESS";
    @Column(name="created_at", nullable=false) private Instant createdAt = Instant.now();
    @Column(name="expires_at") private Instant expiresAt;
    // getters/setters
    public String getUploadId() { return uploadId; } public void setUploadId(String uploadId) { this.uploadId = uploadId; }
    public User getOwner() { return owner; } public void setOwner(User owner) { this.owner = owner; }
    public String getFileName() { return fileName; } public void setFileName(String fileName) { this.fileName = fileName; }
    public Long getTotalSize() { return totalSize; } public void setTotalSize(Long totalSize) { this.totalSize = totalSize; }
    public Integer getTotalChunks() { return totalChunks; } public void setTotalChunks(Integer totalChunks) { this.totalChunks = totalChunks; }
    public Integer getReceivedChunks() { return receivedChunks; } public void setReceivedChunks(Integer receivedChunks) { this.receivedChunks = receivedChunks; }
    public String getTempDir() { return tempDir; } public void setTempDir(String tempDir) { this.tempDir = tempDir; }
    public String getStatus() { return status; } public void setStatus(String status) { this.status = status; }
    public java.time.Instant getCreatedAt() { return createdAt; } public void setCreatedAt(java.time.Instant createdAt) { this.createdAt = createdAt; }
    public java.time.Instant getExpiresAt() { return expiresAt; } public void setExpiresAt(java.time.Instant expiresAt) { this.expiresAt = expiresAt; }
}
