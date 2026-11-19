package com.example.smart_order_assistant_system.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WeatherDemandRuleResponse {

    // 天氣需求規則編號
    private Long id;

    // 商品分類編號
    private Long categoryId;

    // 商品分類名稱
    private String categoryName;

    // 天氣規則名稱
    private String ruleName;

    // 天氣指標
    private String weatherElement;

    // 條件運算子
    private String operator;

    // 條件判斷數值
    private BigDecimal thresholdValue;

    // 區間上限
    private BigDecimal upperThresholdValue;

    // 天氣需求調整係數
    private BigDecimal adjustmentFactor;

    // 資料狀態
    private Byte status;

    // 資料建立時間
    private LocalDateTime createdAt;

    // 資料最後更新時間
    private LocalDateTime updatedAt;
}
