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
@Table(name = "purchase_order_items")
public class PurchaseOrderItem {

    // 訂貨明細編號（主鍵）
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    // 訂貨單編號（外鍵）
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "purchase_order_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_poi_order")
    )
    private PurchaseOrder purchaseOrder;

    // 商品編號（外鍵）
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "product_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_poi_product")
    )
    private Product product;

    // 訂貨數量
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    // 訂貨當下的商品單價
    @Column(
        name = "unit_price",
        nullable = false,
        precision = 10,
        scale = 2
    )
    private BigDecimal unitPrice;

    // 此項商品的小計金額
    @Column(
        name = "subtotal",
        nullable = false,
        precision = 12,
        scale = 2
    )
    private BigDecimal subtotal;

    // 訂貨明細建立時間
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
