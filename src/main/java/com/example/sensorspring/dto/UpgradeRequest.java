package com.example.sensorspring.dto;

/**
 * 升级会员请求
 */
public class UpgradeRequest {
    private Long levelId;
    private Integer durationDays; // -1表示永久，null表示永久
    private String paymentMethod;
    private String transactionId;
    
    // Getters and Setters
    public Long getLevelId() { return levelId; }
    public void setLevelId(Long levelId) { this.levelId = levelId; }
    
    public Integer getDurationDays() { return durationDays; }
    public void setDurationDays(Integer durationDays) { this.durationDays = durationDays; }
    
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
}
