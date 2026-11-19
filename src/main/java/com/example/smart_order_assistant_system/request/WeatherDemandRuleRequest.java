package com.example.smart_order_assistant_system.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WeatherDemandRuleRequest {

    // 商品分類 ID
    @NotNull(message = "商品分類 ID 不可為空")
    private Long categoryId;

    // 天氣規則名稱
    @NotBlank(message = "天氣規則名稱不可為空")
    @Size(max = 100, message = "天氣規則名稱不可超過 100 個字")
    private String ruleName;

    // 天氣指標：TEMPERATURE、HUMIDITY、RAIN_PROBABILITY
    @NotBlank(message = "天氣指標不可為空")
    private String weatherElement;

    // 條件運算子：GT、GTE、LT、LTE、BETWEEN
    @NotBlank(message = "條件運算子不可為空")
    private String operator;

    // 條件判斷數值
    @NotNull(message = "條件數值不可為空")
    @Digits(integer = 8, fraction = 2, message = "條件數值最多 8 位整數及 2 位小數")
    private BigDecimal thresholdValue;

    // 區間上限，只有 BETWEEN 需要填寫
    @Digits(integer = 8, fraction = 2, message = "區間上限最多 8 位整數及 2 位小數")
    private BigDecimal upperThresholdValue;

    // 天氣需求調整係數
    // 例如 1.20 代表增加 20%
    @NotNull(message = "天氣調整係數不可為空")
    @DecimalMin(value = "0.01", message = "天氣調整係數必須大於 0")
    @Digits(integer = 3, fraction = 2, message = "調整係數最多 3 位整數及 2 位小數")
    private BigDecimal adjustmentFactor;
}
