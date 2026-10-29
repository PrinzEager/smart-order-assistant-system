package com.example.smart_order_assistant_system.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.smart_order_assistant_system.entity.PurchaseOrderItem;

public interface PurchaseOrderItemRepository
        extends JpaRepository<PurchaseOrderItem, Long> {

    // 查詢指定訂貨單的所有商品明細
    List<PurchaseOrderItem> findByPurchaseOrder_Id(Long purchaseOrderId);

    // 查詢指定商品的所有訂貨紀錄
    List<PurchaseOrderItem> findByProduct_Id(Long productId);

    // 查詢指定訂貨單中某項商品的明細
    List<PurchaseOrderItem> findByPurchaseOrder_IdAndProduct_Id(
            Long purchaseOrderId,
            Long productId
    );

}