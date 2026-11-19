package com.example.smart_order_assistant_system.request;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PurchaseOrderRequest {

    // 供應商編號：不可為空，且必須大於 0
    @NotNull(message = "供應商編號不可為空")
    @Positive(message = "供應商編號必須大於 0")
    private Long supplierId;

    // 預計到貨日期：可以不填寫
    private LocalDate expectedDeliveryDate;

    // 訂貨備註：最多 500 個字元
    @Size(max = 500, message = "訂貨備註不可超過 500 個字元")
    private String remark;

    // 訂貨商品明細：至少需要一筆
    @NotEmpty(message = "訂貨商品明細不可為空")
    @Valid
    private List<PurchaseOrderItemRequest> items;
}
