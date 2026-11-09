package com.example.smart_order_assistant_system.exception;

// 用於處理找不到資料的情況
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}