package com.example.sensorspring.service;

import com.example.sensorspring.entity.FileAsset;
import com.example.sensorspring.entity.FileBlob;
import com.example.sensorspring.entity.User;
import com.example.sensorspring.exception.BadRequestException;
import com.example.sensorspring.exception.NotFoundException;
import com.example.sensorspring.events.EventPublisher;
import com.example.sensorspring.repository.FileAssetRepository;
import com.example.sensorspring.repository.FileBlobRepository;
import com.example.sensorspring.util.FilenameUtil;
import com.example.sensorspring.util.HashingUtil;
import org.apache.commons.io.FileUtils;
    import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileService {
    private final FileBlobRepository blobs; private final FileAssetRepository assets; private final String uploadRoot; private final EventPublisher publisher;
    public FileService(FileBlobRepository blobs, FileAssetRepository assets, @Value("${app.upload-root:/data/uploads}") String uploadRoot, EventPublisher publisher){
        this.blobs=blobs; this.assets=assets; this.uploadRoot=uploadRoot; this.publisher=publisher;
    }

    public Page<FileAsset> listMyFiles(User user, int page, int size){ return assets.findByOwner(user, PageRequest.of(page, size)); }

    @Transactional
    public FileAsset uploadSingle(User owner, MultipartFile file, String description, FileAsset.Permission permission) throws Exception {
        if (file.isEmpty()) throw new BadRequestException("空文件");
        Path tempDir = Paths.get(uploadRoot, "tmp", UUID.randomUUID().toString());
        Files.createDirectories(tempDir);
        Path tempFile = tempDir.resolve("upload.bin");
        file.transferTo(tempFile.toFile());
        FileAsset asset = finalizeFromAssembled(owner, tempFile, file.getOriginalFilename(), description, permission);
        FileUtils.deleteQuietly(tempDir.toFile());
        return asset;
    }

    @Transactional
    public FileAsset finalizeFromAssembled(User owner, Path assembledFile, String originalFileName, String description, FileAsset.Permission permission) throws Exception {
        String hash;
        try (InputStream in = Files.newInputStream(assembledFile)) { hash = HashingUtil.sha256Hex(in); }
        long size = Files.size(assembledFile);
        Path finalPath = objectPath(hash);
        Files.createDirectories(finalPath.getParent());
        if (!Files.exists(finalPath)) Files.move(assembledFile, finalPath); else Files.deleteIfExists(assembledFile);

        FileBlob blob = blobs.findById(hash).orElseGet(() -> {
            FileBlob b = new FileBlob(); b.setHash(hash); b.setSize(size); b.setStoragePath(finalPath.toString()); b.setRefCount(0L);
            return blobs.save(b);
        });
        blob.setRefCount(blob.getRefCount()+1); blobs.save(blob);

        FileAsset asset = new FileAsset();
        asset.setOwner(owner); asset.setBlob(blob);
        asset.setFileName(FilenameUtil.sanitize(originalFileName));
        asset.setDescription(description);
        asset.setPermission(permission == null ? FileAsset.Permission.PRIVATE : permission);
        asset = assets.save(asset);
        publisher.publishFileUploaded(asset);
        return asset;
    }

    @Transactional
    public void deleteMyFile(User owner, Long id) throws java.io.IOException {
        FileAsset asset = assets.findByIdAndOwner(id, owner).orElseThrow(() -> new NotFoundException("文件不存在"));
        FileBlob blob = asset.getBlob();
        assets.delete(asset);
        blob.setRefCount(Math.max(0, blob.getRefCount()-1)); blobs.save(blob);
        if (blob.getRefCount() <= 0) { Files.deleteIfExists(Path.of(blob.getStoragePath())); blobs.delete(blob); }
    }

    public FileAsset getMyFile(User owner, Long id){ return assets.findByIdAndOwner(id, owner).orElseThrow(() -> new NotFoundException("文件不存在")); }
    public FileAsset getPublicFile(Long id){ FileAsset a = assets.findById(id).orElseThrow(() -> new NotFoundException("文件不存在")); if (a.getPermission()!=FileAsset.Permission.PUBLIC) throw new BadRequestException("该文件非公开"); return a; }
    private Path objectPath(String hash){ String p1 = hash.substring(0,2); return Paths.get(uploadRoot, "objects", p1, hash); }
    public FileAsset save(FileAsset asset){ return assets.save(asset); }
}
