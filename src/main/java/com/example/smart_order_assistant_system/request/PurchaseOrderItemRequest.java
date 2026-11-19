package com.example.smart_order_assistant_system.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PurchaseOrderItemRequest {

    // 商品編號：不可為空
    @NotNull(message = "商品編號不可為空")
    @Positive(message = "商品編號必須大於 0")
    private Long productId;

    // 訂貨數量：不可為空，且必須大於 0
    @NotNull(message = "訂貨數量不可為空")
    @Positive(message = "訂貨數量必須大於 0")
    private Integer quantity;
}
