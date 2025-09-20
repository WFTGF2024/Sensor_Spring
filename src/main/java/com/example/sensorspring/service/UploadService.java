package com.example.sensorspring.service;

import com.example.sensorspring.entity.UploadSession;
import com.example.sensorspring.entity.User;
import com.example.sensorspring.repository.UploadSessionRepository;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.UUID;

@Service
public class UploadService {
    private final UploadSessionRepository sessions; private final String uploadRoot;
    public UploadService(UploadSessionRepository sessions, @Value("${app.upload-root:/data/uploads}") String uploadRoot){ this.sessions=sessions; this.uploadRoot=uploadRoot; }

    @Transactional
    public UploadSession createSession(User owner, String fileName, long totalSize, int totalChunks) throws Exception {
        String uploadId = UUID.randomUUID().toString();
        Path tempDir = Paths.get(uploadRoot, "tmp", uploadId);
        Files.createDirectories(tempDir);
        UploadSession s=new UploadSession(); s.setUploadId(uploadId); s.setOwner(owner); s.setFileName(fileName); s.setTotalSize(totalSize); s.setTotalChunks(totalChunks);
        s.setTempDir(tempDir.toString()); s.setExpiresAt(Instant.now().plusSeconds(24*3600));
        return sessions.save(s);
    }

    @Transactional
    public UploadSession saveChunk(String uploadId, int index, byte[] data) throws Exception {
        UploadSession s = sessions.findById(uploadId).orElseThrow(() -> new RuntimeException("Upload session not found"));
        if (!"IN_PROGRESS".equals(s.getStatus())) throw new RuntimeException("Upload not in progress");
        Path part = Paths.get(s.getTempDir(), String.format("part.%05d", index));
        Files.write(part, data);
        s.setReceivedChunks(Math.min(s.getTotalChunks(), s.getReceivedChunks()+1));
        return sessions.save(s);
    }

    public Path assemble(String uploadId) throws Exception {
        UploadSession s = sessions.findById(uploadId).orElseThrow(() -> new RuntimeException("Upload session not found"));
        if (s.getReceivedChunks() < s.getTotalChunks()) throw new RuntimeException("Chunks not complete");
        Path tempFile = Paths.get(s.getTempDir(), "assembled.bin");
        try (var out = Files.newOutputStream(tempFile)) {
            for (int i=0;i<s.getTotalChunks();i++){ Path part = Paths.get(s.getTempDir(), String.format("part.%05d", i)); Files.copy(part, out); }
        }
        s.setStatus("ASSEMBLED"); sessions.save(s); return tempFile;
    }

    public void cleanup(String uploadId) throws Exception {
        UploadSession s = sessions.findById(uploadId).orElse(null);
        if (s != null) { FileUtils.deleteQuietly(Paths.get(s.getTempDir()).toFile()); sessions.delete(s); }
    }
}
