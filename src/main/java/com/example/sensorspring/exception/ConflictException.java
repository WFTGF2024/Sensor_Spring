package com.example.sensorspring.exception;

/**
 * 冲突异常
 * 参照 Sensor_Server 的 ConflictError 实现
 */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
