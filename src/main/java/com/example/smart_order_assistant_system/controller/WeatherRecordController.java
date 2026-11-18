package com.example.smart_order_assistant_system.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.smart_order_assistant_system.entity.WeatherRecord;
import com.example.smart_order_assistant_system.service.WeatherRecordService;

@RestController
@RequestMapping("/api/weather-records")
public class WeatherRecordController {

    // 注入天氣紀錄 Service，負責處理查詢及資料收集
    private final WeatherRecordService weatherRecordService;

    // 使用建構子注入 WeatherRecordService
    public WeatherRecordController(
            WeatherRecordService weatherRecordService) {
        this.weatherRecordService = weatherRecordService;
    }

    // ========================================
    // 1. 從氣象署 API 收集並儲存天氣資料
    // ========================================

    /**
     * 根據測站 ID 取得最新觀測資料並儲存
     *
     * 範例：
     * POST /api/weather-records/collect?stationId=C0V750
     */
    @PostMapping("/collect")
    public ResponseEntity<WeatherRecord> collectWeatherRecord(
            @RequestParam String stationId) {

        // 呼叫 Service 收集並儲存指定測站的天氣資料
        WeatherRecord record =
                weatherRecordService.collectWeatherRecord(stationId);

        // 回傳成功儲存的天氣紀錄
        return ResponseEntity.ok(record);
    }

    // ========================================
    // 2. 查詢天氣紀錄
    // ========================================

    // 查詢所有天氣紀錄
    // GET /api/weather-records
    @GetMapping
    public ResponseEntity<List<WeatherRecord>> findAllWeatherRecords() {

        List<WeatherRecord> records =
                weatherRecordService.findAllWeatherRecords();

        return ResponseEntity.ok(records);
    }

    // 根據 ID 查詢天氣紀錄
    // GET /api/weather-records/1
    @GetMapping("/{id}")
    public ResponseEntity<WeatherRecord> findWeatherRecordById(
            @PathVariable Long id) {

        WeatherRecord record =
                weatherRecordService.findWeatherRecordById(id);

        return ResponseEntity.ok(record);
    }

    // 根據日期查詢天氣紀錄
    // GET /api/weather-records/date?date=2026-11-18
    @GetMapping("/date")
    public ResponseEntity<List<WeatherRecord>> findWeatherRecordsByDate(
            @RequestParam LocalDate date) {

        List<WeatherRecord> records =
                weatherRecordService.findWeatherRecordsByDate(date);

        return ResponseEntity.ok(records);
    }

    // 根據日期區間查詢天氣紀錄
    // GET /api/weather-records/date-range?startDate=2026-11-01&endDate=2026-11-18
    @GetMapping("/date-range")
    public ResponseEntity<List<WeatherRecord>> findWeatherRecordsByDateRange(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {

        List<WeatherRecord> records =
                weatherRecordService.findWeatherRecordsByDateRange(
                        startDate,
                        endDate);

        return ResponseEntity.ok(records);
    }

    // 根據地點查詢天氣紀錄
    // GET /api/weather-records/location?location=高雄市路竹區
    @GetMapping("/location")
    public ResponseEntity<List<WeatherRecord>> findWeatherRecordsByLocation(
            @RequestParam String location) {

        List<WeatherRecord> records =
                weatherRecordService.findWeatherRecordsByLocation(location);

        return ResponseEntity.ok(records);
    }

    // 根據地點名稱進行模糊搜尋
    // GET /api/weather-records/location/search?keyword=路竹
    @GetMapping("/location/search")
    public ResponseEntity<List<WeatherRecord>> searchWeatherRecordsByLocation(
            @RequestParam String keyword) {

        List<WeatherRecord> records =
                weatherRecordService.searchWeatherRecordsByLocation(keyword);

        return ResponseEntity.ok(records);
    }

    // 根據地點及日期查詢天氣紀錄
    // GET /api/weather-records/location/date?location=高雄市路竹區&date=2026-11-18
    @GetMapping("/location/date")
    public ResponseEntity<List<WeatherRecord>> findWeatherRecordsByLocationAndDate(
            @RequestParam String location,
            @RequestParam LocalDate date) {

        List<WeatherRecord> records =
                weatherRecordService.findWeatherRecordsByLocationAndDate(
                        location,
                        date);

        return ResponseEntity.ok(records);
    }

    // 根據地點及日期區間查詢天氣紀錄
    // GET /api/weather-records/location/date-range?location=高雄市路竹區&startDate=2026-11-01&endDate=2026-11-18
    @GetMapping("/location/date-range")
    public ResponseEntity<List<WeatherRecord>> findWeatherRecordsByLocationAndDateRange(
            @RequestParam String location,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {

        List<WeatherRecord> records =
                weatherRecordService.findWeatherRecordsByLocationAndDateRange(
                        location,
                        startDate,
                        endDate);

        return ResponseEntity.ok(records);
    }

    // 查詢指定地點及日期最新建立的天氣紀錄
    // GET /api/weather-records/latest?location=高雄市路竹區&date=2026-11-18
    @GetMapping("/latest")
    public ResponseEntity<WeatherRecord> findLatestWeatherRecord(
            @RequestParam LocalDate date,
            @RequestParam String location) {

        WeatherRecord record =
                weatherRecordService.findLatestWeatherRecord(
                        date,
                        location);

        return ResponseEntity.ok(record);
    }
}
