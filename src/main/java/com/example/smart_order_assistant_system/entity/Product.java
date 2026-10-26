package com.example.smart_order_assistant_system.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Getter
@Setter
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String sku;

    @Column(length = 50, unique = true)
    private String barcode;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(name = "cost_price", precision = 10, scale = 2)
    private BigDecimal costPrice;

    @Column(length = 20)
    private String unit;

    @Column(name = "current_stock")
    private Integer currentStock;

    @Column(name = "safety_stock")
    private Integer safetyStock;

    @Column(name = "reorder_point")
    private Integer reorderPoint;

    @Column(name = "lead_time_days")
    private Integer leadTimeDays;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(nullable = false)
    private Integer status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public Product() {
    }
}