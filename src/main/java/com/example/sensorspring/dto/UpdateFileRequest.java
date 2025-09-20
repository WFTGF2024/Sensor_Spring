package com.example.sensorspring.dto;

import com.example.sensorspring.entity.FileAsset;
import jakarta.validation.constraints.Size;

public class UpdateFileRequest {
    @Size(min=1, max=255) private String fileName;
    @Size(max=4096) private String description;
    private FileAsset.Permission permission;
    public String getFileName() { return fileName; } public void setFileName(String fileName) { this.fileName = fileName; }
    public String getDescription() { return description; } public void setDescription(String description) { this.description = description; }
    public FileAsset.Permission getPermission() { return permission; } public void setPermission(FileAsset.Permission permission) { this.permission = permission; }
}
