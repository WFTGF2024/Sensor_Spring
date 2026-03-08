package com.example.sensorspring.exception;

/**
 * 禁止访问异常
 * 参照 Sensor_Server 的 AuthorizationError 实现
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
    
    public ForbiddenException() {
        super("禁止访问");
    }
}
