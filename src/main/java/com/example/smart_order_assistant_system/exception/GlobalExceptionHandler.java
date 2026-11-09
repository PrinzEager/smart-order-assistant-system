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

    // ==========================================
    // 1. 處理 Request 資料驗證錯誤
    // ==========================================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        // 取得所有驗證失敗的欄位
        ex.getBindingResult().getFieldErrors().forEach(error -> {

            errors.put(
                    error.getField(),
                    error.getDefaultMessage()
            );

        });

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errors);
    }

    // ==========================================
    // 2. 處理不合法的請求參數
    // ==========================================
    // 例如：
    // 商品數量不能小於 0
    // 指定的父分類不符合系統規則
    // 商品分類不存在或已停用（若目前使用此例外）
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(
            IllegalArgumentException ex) {

        Map<String, String> error = new HashMap<>();

        error.put("message", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    // ==========================================
    // 3. 處理找不到資料的例外
    // ==========================================
    // 例如：
    // 查詢不存在的商品
    // 查詢不存在的商品分類
    //
    // 注意：此方法適用於你自行定義的
    // ResourceNotFoundException
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleResourceNotFoundException(
            ResourceNotFoundException ex) {

        Map<String, String> error = new HashMap<>();

        error.put("message", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error);
    }

    // ==========================================
    // 4. 處理資料衝突的例外
    // ==========================================
    // 例如：
    // 同一父分類下商品分類名稱重複
    // 父分類仍有子分類，不能停用
    // 父分類仍有商品，不能軟刪除
    //
    // 注意：此方法適用於你自行定義的
    // BusinessConflictException
    @ExceptionHandler(BusinessConflictException.class)
    public ResponseEntity<Map<String, String>> handleBusinessConflictException(
            BusinessConflictException ex) {

        Map<String, String> error = new HashMap<>();

        error.put("message", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error);
    }

    // ==========================================
    // 5. 處理未預期的系統錯誤
    // ==========================================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(
            Exception ex) {

        // 將完整錯誤記錄在後端 Console，
        // 方便開發者除錯
        ex.printStackTrace();

        Map<String, String> error = new HashMap<>();

        // 不直接將系統內部錯誤資訊暴露給前端
        error.put("message", "系統發生未預期的錯誤，請稍後再試");

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }
}