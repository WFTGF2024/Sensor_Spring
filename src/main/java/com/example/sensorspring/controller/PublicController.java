package com.example.sensorspring.controller;

import com.example.sensorspring.dto.FileMetadataResponse;
import com.example.sensorspring.entity.FileAsset;
import com.example.sensorspring.service.FileService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    private final FileService files;
    public PublicController(FileService files) { this.files = files; }

    @GetMapping("/files/{id}/meta")
    public ResponseEntity<FileMetadataResponse> meta(@PathVariable Long id) {
        FileAsset asset = files.getPublicFile(id);
        return ResponseEntity.ok(FileMetadataResponse.from(asset));
    }

    @GetMapping("/files/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) throws Exception {
        FileAsset asset = files.getPublicFile(id);
        Path p = Path.of(asset.getBlob().getStoragePath());
        FileSystemResource res = new FileSystemResource(p.toFile());
        String encoded = URLEncoder.encode(asset.getFileName(), StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                .contentLength(res.contentLength())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(res);
    }
}
