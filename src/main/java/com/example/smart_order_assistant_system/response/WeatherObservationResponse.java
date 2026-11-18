package com.example.smart_order_assistant_system.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

// 天氣觀測資料回傳 Response
@Data
public class WeatherObservationResponse {

    // 測站 ID
    private String stationId;

    // 測站名稱
    private String stationName;

    // 縣市名稱
    private String countyName;

    // 鄉鎮名稱
    private String townName;

    // 觀測時間
    private LocalDateTime observationTime;

    // 天氣現象
    private String weather;

    // 降雨量（mm）
    private BigDecimal precipitation;

    // 氣溫（℃）
    private BigDecimal airTemperature;

    // 相對濕度（%）
    private BigDecimal relativeHumidity;

    // 風速（m/s）
    private BigDecimal windSpeed;
}
