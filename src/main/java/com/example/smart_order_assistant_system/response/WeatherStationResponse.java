package com.example.smart_order_assistant_system.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WeatherStationResponse {

    // 測站唯一識別碼
    private String stationId;

    // 測站名稱
    private String stationName;

    // 所屬縣市
    private String countyName;

    // 所屬鄉鎮
    private String townName;

    // WGS84 緯度
    private Double latitude;

    // WGS84 經度
    private Double longitude;
}
