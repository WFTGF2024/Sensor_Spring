package com.example.sensorspring.exception;

/**
 * 请求频率超限异常
 * 参照 Sensor_Server 的 RateLimitError 实现
 */
public class RateLimitExceededException extends RuntimeException {
    public RateLimitExceededException(String message) {
        super(message);
    }
    
    public RateLimitExceededException() {
        super("请求频率超限，请稍后重试");
    }
}
