
package com.example.smart_order_assistant_system.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tools.jackson.databind.JsonNode;
import com.example.smart_order_assistant_system.client.CwaApiClient;

@RestController
@RequestMapping("/api/test/cwa")
public class CwaTestController {

    private final CwaApiClient cwaApiClient;

    public CwaTestController(CwaApiClient cwaApiClient) {
        this.cwaApiClient = cwaApiClient;
    }

    // 測試取得中央氣象署天氣資料
    @GetMapping("/weather")
    public JsonNode getWeatherData() {
        return cwaApiClient.getWeatherData();
    }

    // 測試取得中央氣象署站點資料
    @GetMapping("/stations/count")
    public int getStationCount() {
        return cwaApiClient.getStations().size();
    }
}
