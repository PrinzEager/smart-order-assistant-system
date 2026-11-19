
package com.example.smart_order_assistant_system.response;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// 地區天氣預報回傳 DTO
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WeatherForecastResponse {

    // 預報地區名稱
    private String locationName;

    // 鄉鎮地區代碼
    private String geocode;

    // 地區緯度
    private String latitude;

    // 地區經度
    private String longitude;

    // 未來各時段天氣預報
    private List<WeatherForecastPeriodResponse> forecasts;
}
