package com.example.smart_order_assistant_system.config;

import java.nio.file.Path;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // 取得 uploads 資料夾的絕對路徑
        String uploadPath = Path.of("uploads")
                .toAbsolutePath()
                .normalize()
                .toUri()
                .toString();

        // 設定圖片網址與實際資料夾的對應關係
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath);
    }
}