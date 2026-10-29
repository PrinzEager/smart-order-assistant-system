package com.example.smart_order_assistant_system.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.smart_order_assistant_system.entity.WeatherRecord;
import com.example.smart_order_assistant_system.repository.WeatherRecordRepository;

@Service
@Transactional(readOnly = true)
public class WeatherRecordService {

    private final WeatherRecordRepository weatherRecordRepository;

    // 建構子注入 Repository
    public WeatherRecordService(
            WeatherRecordRepository weatherRecordRepository) {
        this.weatherRecordRepository = weatherRecordRepository;
    }

    // ========================================
    // 1. 查詢功能
    // ========================================

    // 查詢所有天氣紀錄
    public List<WeatherRecord> findAllWeatherRecords() {
        return weatherRecordRepository.findAll();
    }

    // 依天氣紀錄 ID 查詢
    public WeatherRecord findWeatherRecordById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("天氣紀錄 ID 不可為空");
        }

        return weatherRecordRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("找不到天氣紀錄"));
    }

    // 查詢指定日期的天氣紀錄
    public List<WeatherRecord> findWeatherRecordsByDate(
            LocalDate recordDate) {

        validateDate(recordDate);

        return weatherRecordRepository.findByRecordDate(recordDate);
    }

    // 查詢指定日期區間的天氣紀錄
    public List<WeatherRecord> findWeatherRecordsByDateRange(
            LocalDate startDate,
            LocalDate endDate) {

        validateDateRange(startDate, endDate);

        return weatherRecordRepository
                .findByRecordDateBetween(startDate, endDate);
    }

    // 查詢指定地點的所有天氣紀錄
    public List<WeatherRecord> findWeatherRecordsByLocation(
            String location) {

        validateLocation(location);

        return weatherRecordRepository
                .findByLocation(location.trim());
    }

    // 依地點名稱模糊搜尋
    public List<WeatherRecord> searchWeatherRecordsByLocation(
            String location) {

        validateLocation(location);

        return weatherRecordRepository
                .findByLocationContainingIgnoreCase(location.trim());
    }

    // 查詢指定地點、指定日期的天氣紀錄
    public List<WeatherRecord> findWeatherRecordsByLocationAndDate(
            String location,
            LocalDate recordDate) {

        validateLocation(location);
        validateDate(recordDate);

        return weatherRecordRepository
                .findByLocationAndRecordDate(
                        location.trim(),
                        recordDate);
    }

    // 查詢指定地點在日期區間內的天氣紀錄
    public List<WeatherRecord> findWeatherRecordsByLocationAndDateRange(
            String location,
            LocalDate startDate,
            LocalDate endDate) {

        validateLocation(location);
        validateDateRange(startDate, endDate);

        return weatherRecordRepository
                .findByLocationAndRecordDateBetween(
                        location.trim(),
                        startDate,
                        endDate);
    }

    // 查詢指定地點、指定日期最新建立的紀錄
    public WeatherRecord findLatestWeatherRecord(
            LocalDate recordDate,
            String location) {

        validateDate(recordDate);
        validateLocation(location);

        return weatherRecordRepository
                .findFirstByRecordDateAndLocationOrderByCreatedAtDesc(
                        recordDate,
                        location.trim())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "找不到指定日期與地點的天氣紀錄"));
    }

    // ========================================
    // 2. 新增天氣紀錄
    // ========================================

    @Transactional
    public WeatherRecord createWeatherRecord(WeatherRecord record) {
        validateWeatherRecord(record);

        LocalDateTime now = LocalDateTime.now();

        // 移除地點前後的空白
        record.setLocation(record.getLocation().trim());

        // 未指定觀測時間時，使用中午 12:00
        if (record.getRecordTime() == null) {
            record.setRecordTime(LocalTime.NOON);
        }

        // 設定資料建立時間
        record.setCreatedAt(now);

        return weatherRecordRepository.save(record);
    }

    // ========================================
    // 3. 私有驗證方法
    // ========================================

    // 驗證天氣紀錄基本資料
    private void validateWeatherRecord(WeatherRecord record) {
        if (record == null) {
            throw new IllegalArgumentException("天氣紀錄不可為空");
        }

        // 日期為必填欄位
        validateDate(record.getRecordDate());

        // 地點為必填欄位
        validateLocation(record.getLocation());

        // 溫度允許負數，例如冬季低溫
        validateDecimalScale(
                record.getTemperature(), "氣溫");

        // 相對濕度必須介於 0 到 100
        if (record.getHumidity() != null
                && (record.getHumidity().compareTo(BigDecimal.ZERO) < 0
                || record.getHumidity().compareTo(
                        new BigDecimal("100")) > 0)) {
            throw new IllegalArgumentException(
                    "相對濕度必須介於 0 到 100");
        }

        validateDecimalScale(
                record.getHumidity(), "相對濕度");

        // 降雨量不可為負數
        if (record.getRainfall() != null
                && record.getRainfall().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("降雨量不可小於 0");
        }

        validateDecimalScale(
                record.getRainfall(), "降雨量");

        // 天氣狀況最多 50 字
        if (record.getWeatherCondition() != null
                && record.getWeatherCondition().length() > 50) {
            throw new IllegalArgumentException(
                    "天氣狀況不可超過 50 字");
        }
    }

    // 驗證日期不可為空
    private void validateDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("日期不可為空");
        }
    }

    // 驗證地點不可為空，且不可超過 100 字
    private void validateLocation(String location) {
        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException("觀測地點不可為空");
        }

        if (location.trim().length() > 100) {
            throw new IllegalArgumentException(
                    "觀測地點不可超過 100 字");
        }
    }

    // 驗證日期區間
    private void validateDateRange(
            LocalDate startDate,
            LocalDate endDate) {

        validateDate(startDate);
        validateDate(endDate);

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException(
                    "開始日期不可晚於結束日期");
        }
    }

    // 驗證小數位數，避免超出資料庫 scale=2 的設定
    private void validateDecimalScale(
            BigDecimal value,
            String fieldName) {

        if (value != null && value.scale() > 2) {
            throw new IllegalArgumentException(
                    fieldName + "最多只能有兩位小數");
        }
    }
}