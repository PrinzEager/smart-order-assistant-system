package com.example.smart_order_assistant_system.response;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 訂貨建議 API 回傳 DTO
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderRecommendationResponse {

    // 商品編號
    private Long productId;

    // 商品名稱
    private String productName;

    // 商品內部代碼
    private String sku;

    // 目前庫存數量
    private Integer currentStock;

    // 商品安全庫存數量
    private Integer safetyStock;

    // 商品補貨警戒點
    private Integer reorderPoint;

    // 供應商平均交貨天數
    private Integer leadTimeDays;

    // 預估需求起始日期
    private LocalDate forecastStartDate;

    // 預估需求結束日期
    private LocalDate forecastEndDate;

    // 上個月相同期間的銷售總數量
    private Integer historicalSalesQuantity;

    // 已確認但尚未到貨的訂貨數量
    private Integer outstandingQuantity;

    // 可用庫存（目前庫存 + 已確認未到貨數量）
    private Integer availableStock;

    // 系統計算出的建議訂貨數量
    private Integer recommendedQuantity;

    // 是否需要補貨
    private Boolean needsReorder;
}
