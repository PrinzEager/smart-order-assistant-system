package com.example.smart_order_assistant_system.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.smart_order_assistant_system.entity.WeatherRecord;
import com.example.smart_order_assistant_system.repository.WeatherRecordRepository;
import com.example.smart_order_assistant_system.response.WeatherObservationResponse;

@Service
@Transactional(readOnly = true)
public class WeatherRecordService {

    private final WeatherRecordRepository weatherRecordRepository;
    // 注入天氣觀測 Service，負責取得氣象署資料
    private final WeatherObservationService weatherObservationService;

    // 建構子注入 Repository 與 WeatherObservationService
    public WeatherRecordService(
            WeatherRecordRepository weatherRecordRepository,
            WeatherObservationService weatherObservationService) {

        this.weatherRecordRepository = weatherRecordRepository;
        this.weatherObservationService = weatherObservationService;
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
    // 2. 從氣象署 API 收集並儲存天氣資料
    // ========================================

    /**
     * 根據測站 ID 取得最新天氣觀測資料，並儲存至資料庫
     *
     * @param stationId 氣象測站 ID
     * @return 儲存完成的天氣紀錄
     */
    @Transactional
    public WeatherRecord collectWeatherRecord(String stationId) {

        // 檢查測站 ID 是否為空
        if (stationId == null || stationId.isBlank()) {
            throw new IllegalArgumentException("測站 ID 不可為空");
        }

        // 呼叫 WeatherObservationService 取得氣象署觀測資料
        WeatherObservationResponse observation =
                weatherObservationService.getObservation(stationId.trim())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "找不到指定測站的天氣觀測資料"));

        // 取得並整理測站 ID
        String actualStationId = observation.getStationId();

        if (actualStationId == null || actualStationId.isBlank()) {
            throw new IllegalArgumentException("觀測資料缺少測站 ID");
        }

        actualStationId = actualStationId.trim();
        // 確認觀測時間是否存在
        if (observation.getObservationTime() == null) {
            throw new IllegalArgumentException(
                    "氣象觀測時間不存在，無法儲存天氣紀錄");
        }

        // 確認測站名稱是否存在
        if (observation.getStationName() == null
                || observation.getStationName().isBlank()) {
            throw new IllegalArgumentException("測站名稱不可為空");
        }

        // 取得縣市名稱
        String countyName = observation.getCountyName();

        // 取得鄉鎮市區名稱
        String townName = observation.getTownName();

        // 確認地理資訊完整
        if (countyName == null || countyName.isBlank()
                || townName == null || townName.isBlank()) {
            throw new IllegalArgumentException("測站缺少完整地理資訊");
        }

        // 組合完整地點
        String location = countyName.trim() + townName.trim();

        // 確認觀測地點是否完整
        validateLocation(location);

        // 取得實際觀測時間
        LocalDateTime observationTime =
                observation.getObservationTime();

        // 從觀測時間取得日期
        LocalDate recordDate = observationTime.toLocalDate();

        // 從觀測時間取得時間
        LocalTime recordTime = observationTime.toLocalTime();

        // 檢查資料庫是否已經存在相同測站及觀測時間
        Optional<WeatherRecord> existingRecord =
                weatherRecordRepository
                        .findByStationIdAndRecordDateAndRecordTime(
                                actualStationId,
                                recordDate,
                                recordTime);

        // 如果已經存在，直接回傳原本的紀錄
        if (existingRecord.isPresent()) {
            return existingRecord.get();
        }

        // 建立新的 WeatherRecord Entity
        WeatherRecord record = new WeatherRecord();

        // 設定測站 ID
        record.setStationId(actualStationId);

        // 設定測站名稱
        record.setStationName(observation.getStationName());

        // 設定觀測日期及時間
        record.setRecordDate(recordDate);
        record.setRecordTime(recordTime);

        // 設定觀測地點
        record.setLocation(location);

        // 設定氣溫
        record.setTemperature(observation.getAirTemperature());

        // 設定相對濕度
        record.setHumidity(observation.getRelativeHumidity());

        // 設定降雨量
        record.setRainfall(observation.getPrecipitation());

        // 設定天氣狀況
        record.setWeatherCondition(observation.getWeather());

        // 設定資料建立時間
        record.setCreatedAt(LocalDateTime.now());

        // 執行原本的資料驗證
        validateWeatherRecord(record);

        // 將資料寫入 MySQL
        return weatherRecordRepository.save(record);
    }

    // ========================================
    // 3. 新增天氣紀錄
    // ========================================

    @Transactional
    public WeatherRecord createWeatherRecord(WeatherRecord record) {
        validateWeatherRecord(record);

        LocalDateTime now = LocalDateTime.now();

        // 移除地點前後的空白
        record.setLocation(record.getLocation().trim());

        // 設定資料建立時間
        record.setCreatedAt(now);

        return weatherRecordRepository.save(record);
    }

    // ========================================
    // 4. 私有驗證方法
    // ========================================

    // 驗證天氣紀錄基本資料
    private void validateWeatherRecord(WeatherRecord record) {
        if (record == null) {
            throw new IllegalArgumentException("天氣紀錄不可為空");
        }

        // 觀測時間為必填欄位
        if (record.getRecordTime() == null) {
            throw new IllegalArgumentException("觀測時間不可為空");
        }

        // 測站 ID 為必填欄位
        if (record.getStationId() == null
                || record.getStationId().isBlank()) {
            throw new IllegalArgumentException("測站 ID 不可為空");
        }

        // 測站名稱為必填欄位
        if (record.getStationName() == null
                || record.getStationName().isBlank()) {
            throw new IllegalArgumentException("測站名稱不可為空");
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