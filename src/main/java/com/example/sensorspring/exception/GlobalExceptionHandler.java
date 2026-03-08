package com.example.sensorspring.exception;

import com.example.sensorspring.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 * 参照 Sensor_Server 的 errors.py 改进
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    private ApiError base(HttpServletRequest req, String code, String message) {
        ApiError err = new ApiError(code, message);
        err.setPath(req.getRequestURI());
        err.setRequestId(MDC.get("reqId"));
        return err;
    }
    
    /**
     * 参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, Object> details = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            details.put(fe.getField(), fe.getDefaultMessage());
        }
        ApiError err = base(req, "VALIDATION_ERROR", "参数校验失败");
        err.setDetails(details);
        logger.warn("参数验证失败: {}", details);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
    }
    
    /**
     * 资源未找到异常
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotFoundException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(base(req, "NOT_FOUND", ex.getMessage()));
    }
    
    /**
     * 错误请求异常
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequest(BadRequestException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(base(req, "BAD_REQUEST", ex.getMessage()));
    }
    
    /**
     * 未授权异常
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiError> handleUnauthorized(UnauthorizedException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(base(req, "UNAUTHORIZED", ex.getMessage()));
    }
    
    /**
     * 禁止访问异常
     */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiError> handleForbidden(ForbiddenException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(base(req, "FORBIDDEN", ex.getMessage()));
    }
    
    /**
     * 资源冲突异常
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflict(ConflictException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(base(req, "CONFLICT", ex.getMessage()));
    }
    
    /**
     * 请求频率超限异常
     */
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiError> handleRateLimit(RateLimitExceededException ex, HttpServletRequest req) {
        logger.warn("请求频率超限: {} {}", req.getMethod(), req.getRequestURI());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(base(req, "RATE_LIMIT_EXCEEDED", ex.getMessage()));
    }
    
    /**
     * 文件大小超限异常
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiError> handleMaxSize(MaxUploadSizeExceededException ex, HttpServletRequest req) {
        logger.warn("文件大小超限: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
            .body(base(req, "REQUEST_ENTITY_TOO_LARGE", "文件大小超过限制"));
    }
    
    /**
     * 存储空间不足异常
     */
    @ExceptionHandler(StorageLimitExceededException.class)
    public ResponseEntity<ApiError> handleStorageLimit(StorageLimitExceededException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.INSUFFICIENT_STORAGE)
            .body(base(req, "STORAGE_LIMIT_EXCEEDED", ex.getMessage()));
    }
    
    /**
     * 非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(base(req, "VALIDATION_ERROR", ex.getMessage()));
    }
    
    /**
     * 其他所有异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleOther(Exception ex, HttpServletRequest req) {
        logger.error("未处理的异常: {} {}", req.getMethod(), req.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(base(req, "INTERNAL_ERROR", "服务器内部错误"));
    }
}
