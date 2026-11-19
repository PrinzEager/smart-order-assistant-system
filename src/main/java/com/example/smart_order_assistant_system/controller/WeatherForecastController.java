package com.example.smart_order_assistant_system.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.smart_order_assistant_system.response.WeatherForecastResponse;
import com.example.smart_order_assistant_system.service.WeatherForecastService;

import lombok.RequiredArgsConstructor;

import tools.jackson.databind.JsonNode;

// 天氣預報 Controller
@RestController
@RequestMapping("/api/weather-forecasts")
@RequiredArgsConstructor
public class WeatherForecastController {

    // 天氣預報 Service
    private final WeatherForecastService weatherForecastService;

    /**
     * 取得商店所在地區的未來一週天氣預報。
     *
     * @return 高雄市路竹區天氣預報 JSON
     */
    @GetMapping
    public WeatherForecastResponse getStoreWeatherForecast() {

        // 呼叫 Service 取得整理後的天氣預報
        return weatherForecastService.getStoreWeatherForecast();
    }
}
