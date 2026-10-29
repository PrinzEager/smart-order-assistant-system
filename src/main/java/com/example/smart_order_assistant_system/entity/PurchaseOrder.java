package com.example.smart_order_assistant_system.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
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
@Table(name = "purchase_orders")
public class PurchaseOrder {

    // 訂貨單編號（主鍵）
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    // 供應商編號（外鍵）
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "supplier_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_purchase_orders_supplier")
    )
    private Supplier supplier;

    // 訂貨日期與時間
    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    // 預計到貨日期
    @Column(name = "expected_delivery_date")
    private LocalDate expectedDeliveryDate;

    // 訂貨流程：DRAFT、SUBMITTED、RECEIVED、CANCELLED
    @Column(name = "order_status", nullable = false, length = 20)
    private String orderStatus = "DRAFT";

    // 資料狀態：0=軟刪除，1=正常
    @Column(name = "status", nullable = false)
    private Byte status = 1;

    // 訂貨單總金額
    @Column(
        name = "total_amount",
        nullable = false,
        precision = 12,
        scale = 2
    )
    private BigDecimal totalAmount = BigDecimal.ZERO;

    // 訂貨備註
    @Column(name = "remark", length = 500)
    private String remark;

    // 訂貨單建立時間
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 訂貨單最後更新時間
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 軟刪除時間；NULL 表示未刪除
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}