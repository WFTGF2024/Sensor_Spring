package com.example.sensorspring.events;

import com.example.sensorspring.entity.AuditLog;
import com.example.sensorspring.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import java.time.Instant;

@Component
public class UserEventConsumer {
    private static final Logger log = LoggerFactory.getLogger(UserEventConsumer.class);
    private final AuditLogRepository audits;
    public UserEventConsumer(AuditLogRepository audits){ this.audits=audits; }

    @RabbitListener(queues = "user.registered.q")
    public void onUserRegistered(UserRegisteredEvent evt){
        AuditLog l=new AuditLog();
        l.setUserId(evt.getUserId());
        l.setAction("WELCOME_EMAIL_QUEUED");
        l.setTargetType("User"); l.setTargetId(String.valueOf(evt.getUserId()));
        l.setMethod("MQ"); l.setPath("user.registered"); l.setStatus(202);
        l.setCreatedAt(Instant.now());
        audits.save(l);
        log.info("Handled user.registered for {}", evt.getUsername());
    }
}
