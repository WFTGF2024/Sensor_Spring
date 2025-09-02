package com.example.sensor_spring.controller;

import com.example.sensor_spring.config.AppProperties;
import com.example.sensor_spring.dao.FileDao;
import com.example.sensor_spring.util.FilenameUtil;
import com.example.sensor_spring.util.HashingUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/download")
public class DownloadController {

    private final FileDao fileDao;
    private final AppProperties props;

    private static final Set<String> ALLOWED_PERMISSIONS = Set.of("public", "private");

    public DownloadController(FileDao fileDao, AppProperties props) {
        this.fileDao = fileDao;
        this.props = props;
    }

    private Path ensureUserFolder(long userId) throws IOException {
        Path root = Paths.get(props.getUploadRoot());
        Path folder = root.resolve(String.valueOf(userId));
        Files.createDirectories(folder);
        return folder;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file,
                                      @RequestParam(value = "file_permission", required = false) String permission,
                                      @RequestParam(value = "description", required = false) String description,
                                      HttpSession session) throws IOException {
        Long userId = currentUser(session);
        if (userId == null) return error(401, "Authentication required");
        if (file == null || file.isEmpty() || file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()) {
            return error(400, "No file provided");
        }

        String filename = FilenameUtil.secureFilename(file.getOriginalFilename());
        Path userFolder = ensureUserFolder(userId);
        Path destPath = userFolder.resolve(filename);

        long threshold = props.getInMemoryUploadLimit();
        long contentLen = file.getSize();
        String fileHash;
        long fileSize;

        if (filename.toLowerCase().endsWith(".zip")) {
            Files.copy(file.getInputStream(), destPath, StandardCopyOption.REPLACE_EXISTING);
            fileSize = Files.size(destPath);
            fileHash = HashingUtil.sha256OfZipAggregate(destPath);
        } else {
            if (contentLen <= threshold) {
                byte[] data = file.getBytes();
                fileSize = data.length;
                java.security.MessageDigest md;
                try { md = java.security.MessageDigest.getInstance("SHA-256"); }
                catch (Exception e) { throw new IllegalStateException(e); }
                md.update(data, 0, data.length);
                StringBuilder sb = new StringBuilder();
                for (byte b : md.digest()) sb.append(String.format("%02x", b));
                fileHash = sb.toString();
                Files.write(destPath, data);
            } else {
                Files.copy(file.getInputStream(), destPath, StandardCopyOption.REPLACE_EXISTING);
                fileSize = Files.size(destPath);
                fileHash = HashingUtil.sha256File(destPath);
            }
        }

        if (fileDao.fileHashExists(fileHash)) {
            Files.deleteIfExists(destPath);
            return error(400, "file already exists");
        }

        String perm = (permission == null ? "private" : permission.toLowerCase(Locale.ROOT));
        if (!ALLOWED_PERMISSIONS.contains(perm)) {
            Files.deleteIfExists(destPath);
            return error(400, "file_permission must be 'public' or 'private'");
        }

        long fileId = fileDao.insertFile(userId, filename, destPath.toString(), description, perm, fileHash, fileSize);

        Map<String, Object> res = new HashMap<>();
        res.put("message", "Upload successful");
        res.put("file_id", fileId);
        res.put("file_name", filename);
        res.put("file_permission", perm);
        res.put("description", description);
        res.put("file_hash", fileHash);
        res.put("file_size", fileSize);
        res.put("uploaded_at", Instant.now().toString());
        res.put("_status", 201);
        return res;
    }

    @GetMapping("/files")
    public Object listFiles(HttpSession session) {
        Long userId = currentUser(session);
        if (userId == null) return error(401, "Authentication required");
        var list = fileDao.listFiles(userId);
        for (Map<String, Object> rec : list) {
            Object ts = rec.get("updated_at");
            if (ts instanceof java.sql.Timestamp t) {
                rec.put("updated_at", t.toInstant().toString());
            }
        }
        return list;
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<?> downloadFile(@PathVariable("fileId") long fileId, HttpSession session) throws IOException {
        Long userId = currentUser(session);
        if (userId == null) {
            return ResponseEntity.status(401)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\":\"Authentication required\"}");
        }

        var rowOpt = fileDao.findByIdAndUser(fileId, userId);
        if (rowOpt.isEmpty()) {
            return ResponseEntity.status(404)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\":\"File not found\"}");
        }

        var row = rowOpt.get();
        Path path = Paths.get(String.valueOf(row.get("file_path")));
        if (!Files.exists(path)) {
            return ResponseEntity.status(404)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\":\"File not found on server\"}");
        }

        String fname = String.valueOf(row.get("file_name"));
        byte[] content = Files.readAllBytes(path);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fname + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(content);
    }


    @PutMapping("/file/{fileId}")
    @Transactional
    public Map<String, Object> updateFileMetadata(@PathVariable("fileId") long fileId,
                                                  @RequestBody Map<String, Object> data,
                                                  HttpSession session) throws IOException {
        Long userId = currentUser(session);
        if (userId == null) return error(401, "Authentication required");

        String newName = data.containsKey("file_name") ? String.valueOf(data.get("file_name")) : null;
        String newPerm = data.containsKey("file_permission") ? String.valueOf(data.get("file_permission")) : null;
        if (newName == null && newPerm == null) return error(400, "Nothing to update");
        if (newPerm != null) {
            String permLower = newPerm.toLowerCase(Locale.ROOT);
            if (!ALLOWED_PERMISSIONS.contains(permLower)) return error(400, "file_permission must be 'public' or 'private'");
            newPerm = permLower;
        }

        var recOpt = fileDao.findByIdAndUser(fileId, userId);
        if (recOpt.isEmpty()) return error(404, "File not found");
        var rec = recOpt.get();
        String oldName = String.valueOf(rec.get("file_name"));
        String oldPath = String.valueOf(rec.get("file_path"));
        String oldPerm = String.valueOf(rec.get("file_permission"));
        String updatedName = null;
        String updatedPath = null;

        if (newName != null && !newName.equals(oldName)) {
            String secureNew = FilenameUtil.secureFilename(newName);
            Path userFolder = ensureUserFolder(userId);
            Path newPath = userFolder.resolve(secureNew);
            Files.move(Paths.get(oldPath), newPath, StandardCopyOption.REPLACE_EXISTING);
            updatedName = secureNew;
            updatedPath = newPath.toString();
        }

        if (newPerm != null && !newPerm.equals(oldPerm)) {
            // set below
        } else {
            newPerm = null;
        }

        fileDao.updateMeta(fileId, updatedName, updatedPath, newPerm);
        Map<String, Object> res = new HashMap<>();
        res.put("message", "Update successful");
        return res;
    }

    @DeleteMapping("/file/{fileId}")
    @Transactional
    public Map<String, Object> deleteFile(@PathVariable("fileId") long fileId, HttpSession session) throws IOException {
        Long userId = currentUser(session);
        if (userId == null) return error(401, "Authentication required");

        var recOpt = fileDao.findByIdAndUser(fileId, userId);
        if (recOpt.isEmpty()) return error(404, "File not found");
        var rec = recOpt.get();
        Path p = Paths.get(String.valueOf(rec.get("file_path")));

        fileDao.deleteById(fileId);
        try { Files.deleteIfExists(p); } catch (Exception ignored) {}

        Map<String, Object> res = new HashMap<>();
        res.put("message", "Delete successful");
        return res;
    }

    private static Long currentUser(HttpSession session) {
        Object uid = session.getAttribute("user_id");
        return uid == null ? null : ((Number) uid).longValue();
    }

    private static Map<String, Object> error(int code, String msg) {
        Map<String, Object> m = new HashMap<>();
        m.put("error", msg);
        m.put("_status", code);
        return m;
    }
}
