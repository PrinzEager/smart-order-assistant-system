package com.example.smart_order_assistant_system.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.smart_order_assistant_system.entity.PurchaseOrderItem;

public interface PurchaseOrderItemRepository
        extends JpaRepository<PurchaseOrderItem, Long> {

    // 根據訂貨單 ID 查詢所有訂貨明細
    List<PurchaseOrderItem> findByPurchaseOrder_Id(Long purchaseOrderId);

    // 根據商品 ID 查詢相關訂貨明細
    List<PurchaseOrderItem> findByProduct_Id(Long productId);

    // 檢查指定訂貨單是否已經存在指定商品的明細
    boolean existsByPurchaseOrder_IdAndProduct_Id(
            Long purchaseOrderId,
            Long productId
    );
}
