package com.example.smart_order_assistant_system.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    // 刪除指定訂貨單的所有商品明細
    void deleteByPurchaseOrder_Id(Long purchaseOrderId);

    // 加總指定商品已送出但尚未到貨的訂貨數量
    @Query("""
        SELECT COALESCE(SUM(item.quantity), 0)
        FROM PurchaseOrderItem item
        WHERE item.product.id = :productId
        AND item.purchaseOrder.orderStatus = 'SUBMITTED'
        AND item.purchaseOrder.status = 1
        AND item.purchaseOrder.deletedAt IS NULL
    """)
    Long sumOutstandingQuantityByProductId(
            @Param("productId") Long productId
    );
}
