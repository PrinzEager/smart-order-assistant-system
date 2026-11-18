package com.example.smart_order_assistant_system.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.smart_order_assistant_system.exception.ResourceNotFoundException;
import com.example.smart_order_assistant_system.response.WeatherObservationResponse;
import com.example.smart_order_assistant_system.service.WeatherObservationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/weather/observations")
@RequiredArgsConstructor
public class WeatherObservationController {

    // 注入天氣觀測資料 Service
    private final WeatherObservationService weatherObservationService;

    /**
     * 根據測站 ID 查詢最新天氣觀測資料
     *
     * @param stationId 測站 ID
     * @return 指定測站的最新天氣觀測資料
     */
    @GetMapping("/{stationId}")
    public WeatherObservationResponse getObservation(
            @PathVariable String stationId) {

        // 呼叫 Service 查詢指定測站的觀測資料
        return weatherObservationService.getObservation(stationId)

                // 找到測站時回傳觀測資料
                .orElseThrow(() -> new ResourceNotFoundException(
                        "找不到指定測站的天氣觀測資料"));
    }
}
