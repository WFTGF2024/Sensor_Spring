package com.example.sensorspring.exception;

/**
 * 存储空间不足异常
 * 参照 Sensor_Server 的 StorageLimitExceededError 实现
 */
public class StorageLimitExceededException extends RuntimeException {
    public StorageLimitExceededException(String message) {
        super(message);
    }
    
    public StorageLimitExceededException() {
        super("存储空间不足");
    }
}
