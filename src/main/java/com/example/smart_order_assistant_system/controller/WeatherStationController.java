package com.example.smart_order_assistant_system.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.smart_order_assistant_system.request.WeatherStationEnabledRequest;
import com.example.smart_order_assistant_system.entity.WeatherStation;
import com.example.smart_order_assistant_system.response.WeatherStationResponse;
import com.example.smart_order_assistant_system.service.WeatherStationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/weather-stations")
@RequiredArgsConstructor
public class WeatherStationController {

    // 注入測站資料 Service
    private final WeatherStationService weatherStationService;

    /**
     * 查詢所有不重複的縣市名稱
     *
     * GET /api/weather-stations/counties
     */
    @GetMapping("/counties")
    public List<String> getCounties() {

        // 呼叫 Service 取得縣市清單
        return weatherStationService.getCounties();
    }

    /**
     * 根據縣市查詢不重複的鄉鎮名稱
     *
     * GET /api/weather-stations/towns?county=高雄市
     */
    @GetMapping("/towns")
    public List<String> getTowns(
            @RequestParam String county) {

        // 呼叫 Service 查詢指定縣市的鄉鎮
        return weatherStationService.getTowns(county);
    }

    /**
     * 根據縣市與鄉鎮查詢測站
     *
     * GET /api/weather-stations?county=高雄市&town=路竹區
     */
    @GetMapping
    public List<WeatherStationResponse> getStations(
            @RequestParam String county,
            @RequestParam String town) {

        // 呼叫 Service 查詢指定地區的測站
        return weatherStationService.getStations(county, town);
    }

    /**
     * 將指定測站儲存到 MySQL
     *
     * POST /api/weather-stations/{stationId}
     */
    @PostMapping("/{stationId}")
    public WeatherStation saveStation(
            @PathVariable String stationId) {

        // 呼叫 Service 將測站資料儲存到 MySQL
        return weatherStationService.saveStation(stationId);
    }

    /**
     * 查詢所有已啟用的測站
     *
     * GET /api/weather-stations/enabled
     *
     * @return 所有已啟用的測站清單
     */
    @GetMapping("/enabled")
    public List<WeatherStation> getEnabledStations() {

        // 呼叫 Service 查詢所有已啟用的測站
        return weatherStationService.getEnabledStations();
    }

    /**
     * 修改指定測站的啟用狀態
     *
     * PATCH /api/weather-stations/{stationId}/enabled
     *
     * @param stationId 測站編號
     * @param request 使用者傳入的啟用狀態
     * @return 修改後的測站資料
     */
    @PatchMapping("/{stationId}/enabled")
    public WeatherStation updateStationEnabled(
            @PathVariable String stationId,
            @Valid @RequestBody WeatherStationEnabledRequest request) {

        // 呼叫 Service 修改測站啟用狀態
        return weatherStationService.updateStationEnabled(
                stationId,
                request.getEnabled()
        );
    }
}
