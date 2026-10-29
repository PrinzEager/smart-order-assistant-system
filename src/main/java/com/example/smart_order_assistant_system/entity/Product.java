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
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "products")
public class Product {

    // 商品編號（主鍵）
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    // 商品分類編號（外鍵）
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "category_id",
        foreignKey = @ForeignKey(name = "fk_products_category")
    )
    private ProductCategory category;

    // 內部商品代碼
    @Column(name = "sku", nullable = false, length = 50, unique = true)
    private String sku;

    // 商品條碼，保留前導零
    @Column(name = "barcode", length = 50, unique = true)
    private String barcode;

    // 商品主圖片路徑或 URL
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    // 商品名稱
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    // 商品售價
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    // 商品進貨成本
    @Column(name = "cost_price", precision = 10, scale = 2)
    private BigDecimal costPrice;

    // 庫存單位
    @Column(name = "unit", nullable = false, length = 20)
    private String unit = "個";

    // 目前庫存數量
    @Column(name = "current_stock", nullable = false)
    private Integer currentStock = 0;

    // 安全庫存數量
    @Column(name = "safety_stock", nullable = false)
    private Integer safetyStock = 0;

    // 補貨警戒點
    @Column(name = "reorder_point", nullable = false)
    private Integer reorderPoint = 0;

    // 供應商平均交貨天數
    @Column(name = "lead_time_days", nullable = false)
    private Integer leadTimeDays = 0;

    // 商品保存期限天數
    @Column(name = "shelf_life_days")
    private Integer shelfLifeDays;

    // 資料狀態：0=停用或已刪除，1=啟用
    @Column(name = "status", nullable = false)
    private Byte status = 1;

    // 商品資料建立時間
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 商品資料最後更新時間
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 軟刪除時間；NULL 表示未刪除
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}