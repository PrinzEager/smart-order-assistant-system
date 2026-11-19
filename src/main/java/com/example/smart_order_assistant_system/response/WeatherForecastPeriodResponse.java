package com.example.smart_order_assistant_system.response;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// 單一時段天氣預報回傳 DTO
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WeatherForecastPeriodResponse {

    // 預報開始時間
    private String startTime;

    // 預報結束時間
    private String endTime;

    // 平均氣溫（攝氏）
    private Integer averageTemperature;

    // 最高氣溫（攝氏）
    private Integer maxTemperature;

    // 最低氣溫（攝氏）
    private Integer minTemperature;

    // 平均相對濕度（百分比）
    private Integer relativeHumidity;

    // 12 小時降雨機率（百分比）
    // 若氣象署回傳 "-"，則使用 null
    private Integer precipitationProbability;

    // 天氣現象，例如：晴時多雲、陰天
    private String weather;

    // 天氣現象代碼
    private String weatherCode;

    // 天氣預報綜合描述
    private String weatherDescription;
}
