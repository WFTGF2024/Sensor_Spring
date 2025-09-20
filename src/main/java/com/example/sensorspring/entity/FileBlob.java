package com.example.sensorspring.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="file_blobs")
public class FileBlob {
    @Id @Column(length=64) private String hash;
    @Column(nullable=false) private Long size;
    @Column(name="storage_path", nullable=false, length=512) private String storagePath;
    @Column(name="ref_count", nullable=false) private Long refCount = 0L;
    @Column(name="created_at", nullable=false) private Instant createdAt = Instant.now();
    // getters/setters
    public String getHash() { return hash; } public void setHash(String hash) { this.hash = hash; }
    public Long getSize() { return size; } public void setSize(Long size) { this.size = size; }
    public String getStoragePath() { return storagePath; } public void setStoragePath(String storagePath) { this.storagePath = storagePath; }
    public Long getRefCount() { return refCount; } public void setRefCount(Long refCount) { this.refCount = refCount; }
    public Instant getCreatedAt() { return createdAt; } public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
