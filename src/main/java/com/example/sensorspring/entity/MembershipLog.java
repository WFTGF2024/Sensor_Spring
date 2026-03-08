package com.example.sensorspring.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * 会员操作日志实体
 * 参照 Sensor_Server 的 membership_logs 表设计
 */
@Entity
@Table(name = "membership_logs", indexes = {
    @Index(name = "idx_membership_logs_user", columnList = "user_id"),
    @Index(name = "idx_membership_logs_created", columnList = "created_at")
})
public class MembershipLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "action_type", nullable = false, length = 20)
    private String actionType; // upgrade, renew, cancel, downgrade
    
    @Column(name = "action_detail", columnDefinition = "TEXT")
    private String actionDetail;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "old_level_id")
    private MembershipLevel oldLevel;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_level_id")
    private MembershipLevel newLevel;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id")
    private User operator;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    
    public String getActionDetail() { return actionDetail; }
    public void setActionDetail(String actionDetail) { this.actionDetail = actionDetail; }
    
    public MembershipLevel getOldLevel() { return oldLevel; }
    public void setOldLevel(MembershipLevel oldLevel) { this.oldLevel = oldLevel; }
    
    public MembershipLevel getNewLevel() { return newLevel; }
    public void setNewLevel(MembershipLevel newLevel) { this.newLevel = newLevel; }
    
    public User getOperator() { return operator; }
    public void setOperator(User operator) { this.operator = operator; }
    
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
