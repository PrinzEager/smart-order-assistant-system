package com.example.smart_order_assistant_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

// 啟用 Spring Boot 自動設定
@SpringBootApplication

// 啟用定時排程功能
@EnableScheduling
public class SmartOrderAssistantSystemApplication {

    public static void main(String[] args) {

        // 啟動 Spring Boot 專案
        SpringApplication.run(
            SmartOrderAssistantSystemApplication.class,
            args
        );
    }
}