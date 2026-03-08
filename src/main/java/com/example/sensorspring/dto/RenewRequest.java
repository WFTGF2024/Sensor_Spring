package com.example.sensorspring.dto;

/**
 * 续费会员请求
 */
public class RenewRequest {
    private Integer durationDays;
    private String paymentMethod;
    private String transactionId;
    
    // Getters and Setters
    public Integer getDurationDays() { return durationDays; }
    public void setDurationDays(Integer durationDays) { this.durationDays = durationDays; }
    
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
}
