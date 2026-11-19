package com.example.smart_order_assistant_system.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PurchaseOrderResponse {

    // 訂貨單編號
    private Long id;

    // 供應商編號
    private Long supplierId;

    // 供應商名稱
    private String supplierName;

    // 訂貨日期與時間
    private LocalDateTime orderDate;

    // 預計到貨日期
    private LocalDate expectedDeliveryDate;

    // 訂貨單狀態：DRAFT、SUBMITTED、RECEIVED、CANCELLED
    private String orderStatus;

    // 訂貨單總金額
    private BigDecimal totalAmount;

    // 訂貨備註
    private String remark;

    // 訂貨商品明細
    private List<PurchaseOrderItemResponse> items;

    // 訂貨單建立時間
    private LocalDateTime createdAt;

    // 訂貨單最後更新時間
    private LocalDateTime updatedAt;
}
