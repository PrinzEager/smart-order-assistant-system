package com.example.smart_order_assistant_system.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SaleRequest {

    // 商品 ID
    @NotNull(message = "商品 ID 不可為空")
    @Positive(message = "商品 ID 必須大於 0")
    private Long productId;

    // 銷售數量
    @NotNull(message = "銷售數量不可為空")
    @Positive(message = "銷售數量必須大於 0")
    private Integer quantity;

    // 銷售單價
    @NotNull(message = "銷售單價不可為空")
    @DecimalMin(value = "0.00", message = "銷售單價不可小於 0")
    @Digits(
        integer = 8,
        fraction = 2,
        message = "銷售單價最多 8 位整數及 2 位小數"
    )
    private BigDecimal unitPrice;

    // 銷售日期時間（選填）
    private LocalDateTime dateTime;
}
