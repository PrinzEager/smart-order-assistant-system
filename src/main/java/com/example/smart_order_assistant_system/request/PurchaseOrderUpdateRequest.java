package com.example.smart_order_assistant_system.request;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PurchaseOrderUpdateRequest {

    // 供應商編號：允許不修改，提供時必須大於 0
    @Positive(message = "供應商編號必須大於 0")
    private Long supplierId;

    // 預計到貨日期：允許不提供
    private LocalDate expectedDeliveryDate;

    // 訂貨備註：最多 500 個字元
    @Size(max = 500, message = "訂貨備註不可超過 500 個字元")
    private String remark;

    // 商品明細清單：提供時逐筆驗證商品編號及訂貨數量
    @Valid
    private List<PurchaseOrderItemRequest> items;
}
