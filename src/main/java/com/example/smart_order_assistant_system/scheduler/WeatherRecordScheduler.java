package com.example.smart_order_assistant_system.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.smart_order_assistant_system.service.WeatherRecordService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// 標記為 Spring 管理的元件
@Component

// 自動建立 Logger，方便記錄排程執行狀況
@Slf4j

// 自動建立建構子，注入 WeatherRecordService
@RequiredArgsConstructor
public class WeatherRecordScheduler {

    // 注入天氣資料服務
    private final WeatherRecordService weatherRecordService;

    // 每小時的第 0 分 0 秒執行一次
    // 使用台灣時區
    @Scheduled(
        cron = "0 0 * * * *",
        zone = "Asia/Taipei"
    )
    public void collectWeatherRecord() {

        // 記錄排程開始執行
        log.info("開始執行天氣資料自動收集排程");

        try {

            // 收集 C0V750 測站的天氣資料
            weatherRecordService.collectWeatherRecord("C0V750");

            // 記錄排程成功
            log.info("天氣資料自動收集完成");

        } catch (Exception e) {

            // 發生錯誤時記錄例外訊息
            log.error("天氣資料自動收集失敗", e);
        }
    }
}