package com.example.smart_order_assistant_system.response;

import java.math.BigDecimal;

import com.example.smart_order_assistant_system.entity.Product;

import lombok.Getter;

@Getter
public class ProductResponse {

    private Long id;

    private String sku;

    private String barcode;

    private String imageUrl;

    private String name;

    private BigDecimal price;

    private BigDecimal costPrice;

    private String unit;

    private Integer currentStock;

    private Integer safetyStock;

    private Integer reorderPoint;

    private Integer leadTimeDays;

    private Integer shelfLifeDays;

    // 將 Product Entity 轉換成 ProductResponse
    public ProductResponse(Product product) {

        this.id = product.getId();
        this.sku = product.getSku();
        this.barcode = product.getBarcode();
        this.imageUrl = product.getImageUrl();
        this.name = product.getName();
        this.price = product.getPrice();
        this.costPrice = product.getCostPrice();
        this.unit = product.getUnit();
        this.currentStock = product.getCurrentStock();
        this.safetyStock = product.getSafetyStock();
        this.reorderPoint = product.getReorderPoint();
        this.leadTimeDays = product.getLeadTimeDays();
        this.shelfLifeDays = product.getShelfLifeDays();
    }
}