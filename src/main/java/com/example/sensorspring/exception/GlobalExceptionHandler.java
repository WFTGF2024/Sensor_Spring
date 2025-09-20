package com.example.sensorspring.exception;

import com.example.sensorspring.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private ApiError base(HttpServletRequest req, String code, String message){
        ApiError err=new ApiError(code,message); err.setPath(req.getRequestURI()); err.setRequestId(MDC.get("reqId")); return err;
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req){
        Map<String,Object> details=new HashMap<>(); for(FieldError fe:ex.getBindingResult().getFieldErrors()){details.put(fe.getField(),fe.getDefaultMessage());}
        ApiError err=base(req,"VALIDATION_ERROR","参数校验失败"); err.setDetails(details); return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
    }
    @ExceptionHandler(NotFoundException.class) public ResponseEntity<ApiError> nfe(NotFoundException ex,HttpServletRequest req){return ResponseEntity.status(HttpStatus.NOT_FOUND).body(base(req,"NOT_FOUND",ex.getMessage()));}
    @ExceptionHandler(BadRequestException.class) public ResponseEntity<ApiError> bre(BadRequestException ex,HttpServletRequest req){return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(base(req,"BAD_REQUEST",ex.getMessage()));}
    @ExceptionHandler(UnauthorizedException.class) public ResponseEntity<ApiError> ue(UnauthorizedException ex,HttpServletRequest req){return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(base(req,"UNAUTHORIZED",ex.getMessage()));}
    @ExceptionHandler(Exception.class) public ResponseEntity<ApiError> other(Exception ex,HttpServletRequest req){return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(base(req,"INTERNAL_ERROR",ex.getMessage()));}
}
