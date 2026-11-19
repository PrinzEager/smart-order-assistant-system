package com.example.smart_order_assistant_system.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "weather_demand_rules")
public class WeatherDemandRule {

    // 天氣需求規則編號（主鍵）
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    // 對應商品分類
    // 一個商品分類可以設定多筆天氣需求規則
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "category_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_weather_rules_category")
    )
    private ProductCategory category;

    // 天氣規則名稱
    @Column(name = "rule_name", nullable = false, length = 100)
    private String ruleName;

    // 天氣指標
    // TEMPERATURE：氣溫
    // HUMIDITY：濕度
    // RAIN_PROBABILITY：降雨機率
    @Column(name = "weather_element", nullable = false, length = 30)
    private String weatherElement;

    // 條件運算子
    // GT：大於
    // GTE：大於等於
    // LT：小於
    // LTE：小於等於
    // BETWEEN：介於兩個數值之間
    @Column(name = "operator", nullable = false, length = 10)
    private String operator;

    // 條件判斷的數值
    @Column(name = "threshold_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal thresholdValue;

    // 區間條件的上限數值
    // 只有使用 BETWEEN 時才需要設定
    @Column(name = "upper_threshold_value", precision = 10, scale = 2)
    private BigDecimal upperThresholdValue;

    // 天氣需求調整係數
    // 1.20 代表需求增加 20%
    // 0.80 代表需求減少 20%
    // 1.00 代表需求不變
    @Column(name = "adjustment_factor", nullable = false, precision = 5, scale = 2)
    private BigDecimal adjustmentFactor;

    // 資料狀態：0=停用，1=啟用
    @Column(name = "status", nullable = false)
    private Byte status = 1;

    // 資料建立時間
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 資料最後更新時間
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 軟刪除時間；NULL 表示未刪除
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // 新增資料前，自動設定建立時間與更新時間
    @PrePersist
    protected void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        updatedAt = now;
    }

    // 修改資料前，自動更新最後修改時間
    @PreUpdate
    protected void onUpdate() {

        updatedAt = LocalDateTime.now();
    }
}
