package com.example.sensorspring.service;

import com.example.sensorspring.entity.AuditLog;
import com.example.sensorspring.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class AuditService {
    private final AuditLogRepository repo;
    public AuditService(AuditLogRepository repo){ this.repo=repo; }
    public void log(Long userId,String action,String targetType,String targetId,HttpServletRequest req,Integer status){
        try{ AuditLog l=new AuditLog(); l.setUserId(userId); l.setAction(action); l.setTargetType(targetType); l.setTargetId(targetId);
             l.setIp(req.getRemoteAddr()); l.setUserAgent(req.getHeader("User-Agent")); l.setRequestId(req.getHeader("X-Request-Id"));
             l.setMethod(req.getMethod()); l.setPath(req.getRequestURI()); l.setStatus(status); repo.save(l);}catch(Exception ignore){}
    }
}
