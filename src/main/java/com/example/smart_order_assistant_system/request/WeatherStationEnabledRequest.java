package com.example.smart_order_assistant_system.request;

import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

// 自動產生 Getter、Setter
@Getter
@Setter
public class WeatherStationEnabledRequest {

    // 是否啟用測站
    // 必須傳入 true 或 false，不可以省略或傳入 null
    @NotNull(message = "測站啟用狀態不可為空")
    private Boolean enabled;

}