package com.example.smart_order_assistant_system.scheduler;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.smart_order_assistant_system.entity.WeatherStation;
import com.example.smart_order_assistant_system.service.WeatherRecordService;
import com.example.smart_order_assistant_system.service.WeatherStationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// 標記為 Spring 管理的元件
@Component

// 自動建立 Logger，方便記錄排程執行狀況
@Slf4j

// 自動建立建構子，注入所需 Service
@RequiredArgsConstructor
public class WeatherRecordScheduler {

    // 注入天氣資料服務
    private final WeatherRecordService weatherRecordService;

    // 注入測站資料服務
    private final WeatherStationService weatherStationService;

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

            // 從資料庫取得所有已啟用的測站
            List<WeatherStation> enabledStations =
                    weatherStationService.getEnabledStations();

            // 檢查是否有已啟用的測站
            if (enabledStations.isEmpty()) {
                log.info("目前沒有已啟用的測站，結束本次收集排程");
                return;
            }

            // 記錄本次要收集的測站數量
            log.info("本次預計收集 {} 個測站的天氣資料",
                    enabledStations.size());

            // 逐一收集每個已啟用測站的天氣資料
            for (WeatherStation station : enabledStations) {

                // 取得測站編號
                String stationId = station.getStationId();

                try {

                    // 收集目前測站的天氣資料
                    weatherRecordService.collectWeatherRecord(stationId);

                    // 記錄單一測站收集成功
                    log.info("測站 {} 天氣資料收集成功", stationId);

                } catch (Exception e) {

                    // 記錄單一測站收集失敗
                    // 不影響其他測站繼續收集
                    log.error("測站 {} 天氣資料收集失敗",
                            stationId, e);
                }
            }

            // 記錄整體排程執行完成
            log.info("天氣資料自動收集排程執行完成");

        } catch (Exception e) {

            // 處理取得啟用測站清單等整體流程發生的錯誤
            log.error("天氣資料自動收集排程執行失敗", e);
        }
    }
}
