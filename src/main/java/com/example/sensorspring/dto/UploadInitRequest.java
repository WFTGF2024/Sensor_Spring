package com.example.sensorspring.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class UploadInitRequest {
    @NotBlank private String fileName;
    @NotNull @Min(1) private Long totalSize;
    @NotNull @Min(1) private Integer totalChunks;
    public String getFileName() { return fileName; } public void setFileName(String fileName) { this.fileName = fileName; }
    public Long getTotalSize() { return totalSize; } public void setTotalSize(Long totalSize) { this.totalSize = totalSize; }
    public Integer getTotalChunks() { return totalChunks; } public void setTotalChunks(Integer totalChunks) { this.totalChunks = totalChunks; }
}
