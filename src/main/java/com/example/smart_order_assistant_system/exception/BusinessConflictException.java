package com.example.smart_order_assistant_system.exception;

// 用於處理業務規則衝突
public class BusinessConflictException extends RuntimeException {

    public BusinessConflictException(String message) {
        super(message);
    }
}