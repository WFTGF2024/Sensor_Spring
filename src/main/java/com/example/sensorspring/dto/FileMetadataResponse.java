package com.example.sensorspring.dto;

import com.example.sensorspring.entity.FileAsset;
import java.time.Instant;

public class FileMetadataResponse {
    private Long id; private String fileName; private String description; private String permission; private Long size; private String hash; private Instant createdAt; private Instant updatedAt;
    public static FileMetadataResponse from(FileAsset a){ FileMetadataResponse r=new FileMetadataResponse(); r.id=a.getId(); r.fileName=a.getFileName(); r.description=a.getDescription(); r.permission=a.getPermission().name(); r.size=a.getBlob().getSize(); r.hash=a.getBlob().getHash(); r.createdAt=a.getCreatedAt(); r.updatedAt=a.getUpdatedAt(); return r; }
    public Long getId(){return id;} public String getFileName(){return fileName;} public String getDescription(){return description;} public String getPermission(){return permission;} public Long getSize(){return size;} public String getHash(){return hash;} public Instant getCreatedAt(){return createdAt;} public Instant getUpdatedAt(){return updatedAt;}
}
