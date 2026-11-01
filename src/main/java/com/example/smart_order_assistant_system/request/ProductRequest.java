package com.example.smart_order_assistant_system.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.example.smart_order_assistant_system.entity.Product;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class ProductRequest {

    // 商品編號
    @NotBlank(message = "商品 SKU 不可為空")
    @Size(max = 50, message = "商品 SKU 最多 50 個字元")
    private String sku;

    // 商品條碼
    @Size(max = 50, message = "商品條碼最多 50 個字元")
    private String barcode;

    // 商品圖片路徑
    private String imageUrl;

    // 商品名稱
    @NotBlank(message = "商品名稱不可為空")
    @Size(max = 100, message = "商品名稱最多 100 個字元")
    private String name;

    // 商品售價
    @NotNull(message = "商品售價不可為空")
    @DecimalMin(value = "0.01", message = "商品售價不可小於 0")
    private BigDecimal price;

    // 商品成本
    @DecimalMin(value = "0.01", message = "商品成本不可小於 0")
    private BigDecimal costPrice;

    // 商品單位
    @NotBlank(message = "商品單位不可為空")
    private String unit;

    // 目前庫存
    @NotNull(message = "目前庫存不可為空")
    @Min(value = 0, message = "目前庫存不可小於 0")
    private Integer currentStock;

    // 安全庫存
    @NotNull(message = "安全庫存不可為空")
    @Min(value = 0, message = "安全庫存不可小於 0")
    private Integer safetyStock;

    // 再訂購點
    @NotNull(message = "再訂購點不可為空")
    @Min(value = 0, message = "再訂購點不可小於 0")
    private Integer reorderPoint;

    // 供應商交貨天數
    @NotNull(message = "交貨天數不可為空")
    @Min(value = 0, message = "交貨天數不可小於 0")
    private Integer leadTimeDays;

    // 商品保存期限
    @Min(value = 0, message = "保存期限不可小於 0")
    private Integer shelfLifeDays;


    // 將 ProductRequest 轉換成 Product Entity
    public Product toEntity() {

        Product product = new Product();

        product.setSku(this.sku);
        product.setBarcode(this.barcode);
        product.setImageUrl(this.imageUrl);
        product.setName(this.name);
        product.setPrice(this.price);
        product.setCostPrice(this.costPrice);
        product.setUnit(this.unit);
        product.setCurrentStock(this.currentStock);
        product.setSafetyStock(this.safetyStock);
        product.setReorderPoint(this.reorderPoint);
        product.setLeadTimeDays(this.leadTimeDays);
        product.setShelfLifeDays(this.shelfLifeDays);

        return product;
    }
}