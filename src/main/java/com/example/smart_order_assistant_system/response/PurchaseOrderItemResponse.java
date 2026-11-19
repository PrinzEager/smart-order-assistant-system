package com.example.smart_order_assistant_system.response;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PurchaseOrderItemResponse {

    // 訂貨明細編號
    private Long id;

    // 商品編號
    private Long productId;

    // 商品名稱
    private String productName;

    // 訂貨數量
    private Integer quantity;

    // 訂貨當下的商品單價
    private BigDecimal unitPrice;

    // 商品小計
    private BigDecimal subtotal;
}
