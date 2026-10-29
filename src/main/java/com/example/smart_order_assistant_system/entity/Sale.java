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
@Table(name = "sales")
public class Sale {

    // 銷售紀錄編號（主鍵）
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    // 商品編號（外鍵）
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "product_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_sales_product")
    )
    private Product product;

    // 銷售數量
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    // 銷售當下的商品單價
    @Column(
        name = "unit_price",
        nullable = false,
        precision = 10,
        scale = 2
    )
    private BigDecimal unitPrice;

    // 此筆銷售總金額
    @Column(
        name = "total_amount",
        nullable = false,
        precision = 12,
        scale = 2
    )
    private BigDecimal totalAmount;

    // 實際銷售日期與時間
    @Column(name = "date_time", nullable = false)
    private LocalDateTime dateTime;

    // 銷售紀錄建立時間
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 銷售紀錄最後更新時間
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 軟刪除時間；NULL 表示未刪除
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}