package com.example.smart_order_assistant_system.exception;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// 統一處理 Controller 發生的例外
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 建立 Logger，取代原本的 printStackTrace()
    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ==========================================
    // 1. 處理 Request DTO 資料驗證錯誤
    // ==========================================
    // 例如：
    // 商品名稱不可為空
    // 商品名稱長度不可超過限制
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        // 取得所有驗證失敗的欄位及錯誤訊息
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
    // 4. 處理缺少必要的 Request Parameter
    // ==========================================
    // 例如：
    // API 要求傳入 county 參數，但使用者沒有提供
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, String>> handleMissingParameterException(
            MissingServletRequestParameterException ex) {

        Map<String, String> error = new HashMap<>();

        // 取得缺少的參數名稱
        String parameterName = ex.getParameterName();

        error.put(
                "message",
                "缺少必要參數：" + parameterName
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    // ==========================================
    // 5. 處理資料衝突的例外
    // ==========================================
    // 例如：
    // 同一父分類下商品分類名稱重複
    // 父分類仍有子分類，不能停用
    // 父分類仍有商品，不能軟刪除
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
    // 6. 處理 Request Parameter 型別錯誤
    // ==========================================
    // 例如：
    // GET /api/products?page=abc&size=10
    // page 預期為數字，卻傳入文字
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleTypeMismatchException(
            MethodArgumentTypeMismatchException ex) {

        Map<String, String> error = new HashMap<>();

        error.put(
                "message",
                "請求參數型別錯誤，請確認輸入內容"
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    // ==========================================
    // 7. 處理 JSON 或 Request Body 格式錯誤
    // ==========================================
    // 例如：
    // JSON 格式錯誤
    // 數字欄位傳入無法轉換的文字
    // 日期格式不符合預期
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleMessageNotReadableException(
            HttpMessageNotReadableException ex) {

        Map<String, String> error = new HashMap<>();

        error.put(
                "message",
                "請求資料格式錯誤，請確認 JSON 格式及欄位內容"
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    // ==========================================
    // 8. 處理未預期的系統錯誤
    // ==========================================
    // 例如：
    // 資料庫連線異常
    // 未預期的 NullPointerException
    // 其他沒有被個別 Handler 處理的例外
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(
            Exception ex) {

        // 將完整錯誤記錄在後端 Console，方便開發者除錯
        log.error("系統發生未預期的錯誤", ex);

        Map<String, String> error = new HashMap<>();

        // 不直接將系統內部錯誤資訊暴露給前端
        error.put(
                "message",
                "系統發生未預期的錯誤，請稍後再試"
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }

    // ==========================================
    // 9.處理 HTTP 請求方法不支援
    // ==========================================
    // 例如：
    // API 只接受 POST，卻使用 GET 呼叫
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, String>> handleMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex) {

        Map<String, String> error = new HashMap<>();

        error.put(
                "message",
                "不支援此 HTTP 請求方法，請確認 API 使用的請求方式"
        );

        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(error);
    }
}
