package com.example.sensorspring.events;

import com.example.sensorspring.config.RabbitConfig;
import com.example.sensorspring.entity.FileAsset;
import com.example.sensorspring.entity.User;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class EventPublisher {
    private final RabbitTemplate tpl;
    public EventPublisher(RabbitTemplate tpl){ this.tpl=tpl; }
    public void publishFileUploaded(FileAsset a){
        tpl.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.RK_FILE_UPLOADED,
           new FileUploadedEvent(a.getId(), a.getOwner().getId(), a.getFileName(), a.getBlob().getHash(), a.getBlob().getSize(), a.getPermission().name()));
    }
    public void publishUserRegistered(User u){
        tpl.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.RK_USER_REGISTERED,
           new UserRegisteredEvent(u.getId(), u.getUsername(), u.getEmail()));
    }
}
