package com.example.sensorspring.dto;

import java.time.Instant;
import java.util.Map;

public class ApiError {
    private String code;
    private String message;
    private String traceId;
    private String requestId;
    private String path;
    private Instant timestamp = Instant.now();
    private Map<String, Object> details;
    public ApiError() {}
    public ApiError(String code, String message) { this.code = code; this.message = message; }
    // getters/setters
    public String getCode() { return code; } public void setCode(String code) { this.code = code; }
    public String getMessage() { return message; } public void setMessage(String message) { this.message = message; }
    public String getTraceId() { return traceId; } public void setTraceId(String traceId) { this.traceId = traceId; }
    public String getRequestId() { return requestId; } public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getPath() { return path; } public void setPath(String path) { this.path = path; }
    public Instant getTimestamp() { return timestamp; } public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public Map<String, Object> getDetails() { return details; } public void setDetails(Map<String, Object> details) { this.details = details; }
}
