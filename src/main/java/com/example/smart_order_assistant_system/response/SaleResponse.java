package com.example.smart_order_assistant_system.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.smart_order_assistant_system.entity.Sale;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SaleResponse {

    // 銷售紀錄 ID
    private Long id;

    // 商品 ID
    private Long productId;

    // 商品名稱
    private String productName;

    // 銷售數量
    private Integer quantity;

    // 銷售單價
    private BigDecimal unitPrice;

    // 銷售總金額
    private BigDecimal totalAmount;

    // 銷售日期時間
    private LocalDateTime dateTime;

    // 建立時間
    private LocalDateTime createdAt;

    // 最後更新時間
    private LocalDateTime updatedAt;

    // Entity 轉換 Response DTO
    public static SaleResponse fromEntity(Sale sale) {

        SaleResponse response = new SaleResponse();

        response.setId(sale.getId());

        response.setProductId(
                sale.getProduct().getId());

        response.setProductName(
                sale.getProduct().getName());

        response.setQuantity(sale.getQuantity());

        response.setUnitPrice(sale.getUnitPrice());

        response.setTotalAmount(sale.getTotalAmount());

        response.setDateTime(sale.getDateTime());

        response.setCreatedAt(sale.getCreatedAt());

        response.setUpdatedAt(sale.getUpdatedAt());

        return response;
    }
}