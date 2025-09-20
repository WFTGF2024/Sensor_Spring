package com.example.sensorspring.events;

import com.example.sensorspring.entity.AuditLog;
import com.example.sensorspring.repository.AuditLogRepository;
import com.example.sensorspring.repository.FileAssetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

@Component
public class FileEventConsumer {
    private static final Logger log = LoggerFactory.getLogger(FileEventConsumer.class);
    private final FileAssetRepository assets; private final AuditLogRepository audits;
    public FileEventConsumer(FileAssetRepository a, AuditLogRepository b){ assets=a; audits=b; }

    @RabbitListener(queues = "file.uploaded.q")
    @Transactional
    public void onFileUploaded(FileUploadedEvent evt){
        // 示例：写一条审计，并标记大文件（>100MB）为 206 状态
        AuditLog l = new AuditLog();
        l.setUserId(evt.getOwnerId());
        l.setAction("ASYNC_FILE_POST_PROCESS");
        l.setTargetType("FileAsset"); l.setTargetId(String.valueOf(evt.getFileId()));
        l.setMethod("MQ"); l.setPath("file.uploaded");
        l.setStatus(evt.getSize()!=null && evt.getSize() > (100L*1024*1024) ? 206 : 200);
        l.setCreatedAt(Instant.now());
        audits.save(l);
        log.info("Processed file.uploaded event fileId={}, size={}, hash={}", evt.getFileId(), evt.getSize(), evt.getHash());
    }
}
