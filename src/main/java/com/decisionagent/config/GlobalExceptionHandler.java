package com.decisionagent.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 *
 * 【核心职责】
 * 统一处理 REST API 抛出的所有异常
 * 将异常转换为友好的 JSON 响应
 *
 * 【解决的问题】
 * - 避免向用户暴露堆栈跟踪信息
 * - 统一错误响应格式
 * - 记录错误日志便于排查问题
 *
 * 【处理的异常类型】
 * 1. 参数验证异常（MethodArgumentNotValidException）
 *    - 当请求参数不符合 @Valid 规则时触发
 *    - 返回 400 Bad Request
 *
 * 2. 通用异常（Exception）
 *    - 捕获所有未处理的异常
 *    - 返回 500 Internal Server Error
 *
 * @author DecisionAgent Team
 * @version 1.0.0
 */
@Slf4j
@RestControllerAdvice  // 这是一个 REST 控制器增强器，会自动应用于所有 @Controller
public class GlobalExceptionHandler {

    /**
     * 处理参数验证异常
     *
     * 【触发场景】
     * 当请求体中的字段不满足验证规则时触发
     * 例如：@NotBlank 字段为空
     *
     * @param ex 参数验证异常
     * @return 400 错误响应，包含详细的字段错误信息
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        // 步骤 1：提取所有字段错误
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        // 步骤 2：构建错误响应
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());  // 错误发生时间
        response.put("status", HttpStatus.BAD_REQUEST.value());  // HTTP 状态码 400
        response.put("error", "Validation Failed");  // 错误类型
        response.put("details", errors);  // 详细的字段错误

        // 步骤 3：记录警告日志
        log.warn("【全局异常处理】参数验证失败：{}", errors);

        // 步骤 4：返回 400 错误响应
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 处理通用异常（兜底处理）
     *
     * 【触发场景】
     * 捕获所有未被特定处理器捕获的异常
     * 这是最后的保障，避免返回 500 错误页面
     *
     * @param ex 通用异常
     * @return 500 错误响应，包含异常消息
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex) {
        // 记录错误日志（包含完整的堆栈跟踪）
        log.error("【全局异常处理】发生未预期的错误", ex);

        // 构建错误响应
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());  // 错误发生时间
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());  // HTTP 状态码 500
        response.put("error", "Internal Server Error");  // 错误类型
        response.put("message", ex.getMessage());  // 错误消息

        // 返回 500 错误响应
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
