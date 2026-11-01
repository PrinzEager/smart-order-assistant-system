package com.example.smart_order_assistant_system.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// 統一處理 Controller 發生的例外
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 處理 Request 資料驗證錯誤
    // 當 Request 中的 @NotBlank、@NotNull、@Size 等驗證條件不符合時，
    // Spring 會觸發 MethodArgumentNotValidException
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(
            MethodArgumentNotValidException ex) {

        // 建立 HashMap，儲存驗證失敗的欄位名稱與錯誤訊息
        // Key：Request 欄位名稱
        // Value：對應的驗證錯誤訊息
        Map<String, String> errors = new HashMap<>();

        // 取得所有驗證失敗的欄位，並逐一處理
        ex.getBindingResult().getFieldErrors().forEach(error -> {

            // 將錯誤欄位名稱與錯誤訊息存入 Map
            errors.put(
                    error.getField(),        // 取得發生錯誤的欄位名稱
                    error.getDefaultMessage() // 取得欄位的驗證錯誤訊息
            );

        });

        // 回傳 HTTP 400 Bad Request
        // 表示前端傳入的 Request 資料不符合後端驗證條件
        // 並將錯誤訊息作為 Response Body 回傳給前端
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errors);
    }
}