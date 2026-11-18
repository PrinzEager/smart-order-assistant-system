package com.example.smart_order_assistant_system.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
     *
     * @param county 使用者指定的縣市名稱
     * @return 該縣市的鄉鎮清單
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
     *
     * @param county 使用者指定的縣市
     * @param town 使用者指定的鄉鎮
     * @return 符合條件的測站資料
     */
    @GetMapping
    public List<WeatherStationResponse> getStations(
            @RequestParam String county,
            @RequestParam String town) {

        // 呼叫 Service 查詢指定地區的測站
        return weatherStationService.getStations(county, town);
    }
}
