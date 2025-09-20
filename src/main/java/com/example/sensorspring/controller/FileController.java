package com.example.sensorspring.controller;

import com.example.sensorspring.dto.*;
import com.example.sensorspring.entity.FileAsset;
import com.example.sensorspring.entity.UploadSession;
import com.example.sensorspring.entity.User;
import com.example.sensorspring.exception.NotFoundException;
import com.example.sensorspring.repository.UserRepository;
import com.example.sensorspring.service.AuditService;
import com.example.sensorspring.service.FileService;
import com.example.sensorspring.service.UploadService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final UserRepository users; private final FileService files; private final UploadService uploads; private final AuditService audit;
    public FileController(UserRepository users, FileService files, UploadService uploads, AuditService audit) {
        this.users=users; this.files=files; this.uploads=uploads; this.audit=audit;
    }

    private User currentUser(){ String username = SecurityContextHolder.getContext().getAuthentication().getName(); return users.findByUsername(username).orElseThrow(() -> new NotFoundException("未找到用户")); }

    @GetMapping
    public ResponseEntity<Page<FileMetadataResponse>> list(@RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size) {
        User u = currentUser(); return ResponseEntity.ok(files.listMyFiles(u, page, size).map(FileMetadataResponse::from));
    }

    @PostMapping(path="/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileMetadataResponse> uploadSingle(@RequestParam("file") MultipartFile file,
                                                             @RequestParam(value="description", required=false) String description,
                                                             @RequestParam(value="permission", required=false) FileAsset.Permission permission,
                                                             HttpServletRequest req) throws Exception {
        User u = currentUser();
        FileAsset asset = files.uploadSingle(u, file, description, permission);
        audit.log(u.getId(), "UPLOAD_SINGLE", "FileAsset", String.valueOf(asset.getId()), req, 200);
        return ResponseEntity.ok(FileMetadataResponse.from(asset));
    }

    @PostMapping("/uploads")
    public ResponseEntity<UploadInitResponse> initUpload(@Valid @RequestBody UploadInitRequest request, HttpServletRequest req) throws Exception {
        User u = currentUser();
        UploadSession s = uploads.createSession(u, request.getFileName(), request.getTotalSize(), request.getTotalChunks());
        audit.log(u.getId(), "UPLOAD_INIT", "UploadSession", s.getUploadId(), req, 200);
        return ResponseEntity.ok(new UploadInitResponse(s.getUploadId(), s.getTempDir()));
    }

    @PostMapping(path="/uploads/{uploadId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SimpleResponse> uploadChunk(@PathVariable String uploadId, @RequestParam("index") int index,
                                                      @RequestParam("chunk") MultipartFile chunk, HttpServletRequest req) throws Exception {
        User u = currentUser();
        uploads.saveChunk(uploadId, index, chunk.getBytes());
        audit.log(u.getId(), "UPLOAD_CHUNK", "UploadSession", uploadId, req, 200);
        return ResponseEntity.ok(new SimpleResponse("OK"));
    }

    @PostMapping("/uploads/{uploadId}/complete")
    public ResponseEntity<FileMetadataResponse> complete(@PathVariable String uploadId,
                                                         @RequestParam(value="description", required=false) String description,
                                                         @RequestParam(value="permission", required=false) FileAsset.Permission permission,
                                                         @RequestParam(value="fileName", required=false) String fileName,
                                                         HttpServletRequest req) throws Exception {
        User u = currentUser();
        Path assembled = uploads.assemble(uploadId);
        String name = (fileName == null || fileName.isBlank()) ? "file_" + uploadId : fileName;
        FileAsset saved = files.finalizeFromAssembled(u, assembled, name, description, permission);
        uploads.cleanup(uploadId);
        audit.log(u.getId(), "UPLOAD_COMPLETE", "FileAsset", String.valueOf(saved.getId()), req, 200);
        return ResponseEntity.ok(FileMetadataResponse.from(saved));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadMy(@PathVariable Long id) throws Exception {
        User u = currentUser();
        FileAsset asset = files.getMyFile(u, id);
        Path p = Path.of(asset.getBlob().getStoragePath());
        FileSystemResource res = new FileSystemResource(p.toFile());
        String encoded = URLEncoder.encode(asset.getFileName(), StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                .contentLength(res.contentLength())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(res);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FileMetadataResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateFileRequest request, HttpServletRequest req) {
        User u = currentUser();
        FileAsset asset = files.getMyFile(u, id);
        if (request.getFileName() != null) asset.setFileName(request.getFileName());
        if (request.getDescription() != null) asset.setDescription(request.getDescription());
        if (request.getPermission() != null) asset.setPermission(request.getPermission());
        var saved = files.save(asset);
        audit.log(u.getId(), "FILE_UPDATE", "FileAsset", String.valueOf(saved.getId()), req, 200);
        return ResponseEntity.ok(FileMetadataResponse.from(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SimpleResponse> delete(@PathVariable Long id, HttpServletRequest req) throws Exception {
        User u = currentUser();
        files.deleteMyFile(u, id);
        audit.log(u.getId(), "FILE_DELETE", "FileAsset", String.valueOf(id), req, 200);
        return ResponseEntity.ok(new SimpleResponse("已删除"));
    }
}
