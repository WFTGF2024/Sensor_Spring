package com.example.sensorspring.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="file_assets", indexes = {
    @Index(name="idx_file_assets_owner", columnList = "owner_id"),
    @Index(name="idx_file_assets_updated", columnList = "updated_at")
})
public class FileAsset {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false, fetch=FetchType.LAZY) @JoinColumn(name="owner_id", nullable=false)
    private User owner;

    @ManyToOne(optional=false, fetch=FetchType.LAZY) @JoinColumn(name="blob_hash", referencedColumnName="hash", nullable=false)
    private FileBlob blob;

    @Column(name="file_name", nullable=false, length=255) private String fileName;
    @Column(columnDefinition="TEXT") private String description;

    @Enumerated(EnumType.STRING) @Column(nullable=false, length=16)
    private Permission permission = Permission.PRIVATE;

    @Column(name="created_at", nullable=false) private Instant createdAt = Instant.now();
    @Column(name="updated_at", nullable=false) private Instant updatedAt = Instant.now();

    public enum Permission { PRIVATE, PUBLIC }
    @PreUpdate public void preUpdate() { this.updatedAt = Instant.now(); }

    // getters/setters
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public User getOwner() { return owner; } public void setOwner(User owner) { this.owner = owner; }
    public FileBlob getBlob() { return blob; } public void setBlob(FileBlob blob) { this.blob = blob; }
    public String getFileName() { return fileName; } public void setFileName(String fileName) { this.fileName = fileName; }
    public String getDescription() { return description; } public void setDescription(String description) { this.description = description; }
    public Permission getPermission() { return permission; } public void setPermission(Permission permission) { this.permission = permission; }
    public Instant getCreatedAt() { return createdAt; } public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; } public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
