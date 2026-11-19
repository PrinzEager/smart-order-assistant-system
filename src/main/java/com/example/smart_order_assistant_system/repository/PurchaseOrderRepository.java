package com.example.smart_order_assistant_system.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.smart_order_assistant_system.entity.PurchaseOrder;

public interface PurchaseOrderRepository
        extends JpaRepository<PurchaseOrder, Long> {

    // 根據訂單狀態及資料狀態查詢未刪除的訂貨單
    List<PurchaseOrder> findByOrderStatusAndStatusAndDeletedAtIsNull(
            String orderStatus,
            Byte status
    );

    // 根據供應商 ID 查詢未刪除的訂貨單
    List<PurchaseOrder> findBySupplier_IdAndStatusAndDeletedAtIsNull(
            Long supplierId,
            Byte status
    );

    // 根據訂單日期區間查詢未刪除的訂貨單
    List<PurchaseOrder> findByOrderDateBetweenAndStatusAndDeletedAtIsNull(
            LocalDateTime startDate,
            LocalDateTime endDate,
            Byte status
    );

    // 根據訂貨單 ID 查詢未刪除的訂貨單
    Optional<PurchaseOrder> findByIdAndStatusAndDeletedAtIsNull(
            Long id,
            Byte status
    );

    // 查詢所有未刪除的訂貨單
    List<PurchaseOrder> findByStatusAndDeletedAtIsNull(Byte status);

    // 分頁查詢所有有效且未軟刪除的訂貨單
    Page<PurchaseOrder> findByStatusAndDeletedAtIsNull(
            Byte status,
            Pageable pageable
    );

    // 根據供應商、訂單狀態及日期區間進行多條件分頁查詢
    @Query("""
            SELECT po
            FROM PurchaseOrder po
            WHERE po.status = :status
              AND po.deletedAt IS NULL
              AND (:supplierId IS NULL OR po.supplier.id = :supplierId)
              AND (:orderStatus IS NULL OR po.orderStatus = :orderStatus)
              AND (:startDate IS NULL OR po.orderDate >= :startDate)
              AND (:endDate IS NULL OR po.orderDate < :endDate)
            """)
    Page<PurchaseOrder> findByConditions(
            @Param("supplierId") Long supplierId,
            @Param("orderStatus") String orderStatus,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("status") Byte status,
            Pageable pageable
    );

    // 計算指定商品目前所有已送出訂貨單的未到貨數量
    @Query("""
            SELECT COALESCE(SUM(item.quantity), 0)
            FROM PurchaseOrderItem item
            WHERE item.product.id = :productId
              AND item.purchaseOrder.orderStatus = 'SUBMITTED'
              AND item.purchaseOrder.status = 1
              AND item.purchaseOrder.deletedAt IS NULL
            """)
    Long calculateOutstandingQuantity(
            @Param("productId") Long productId
    );
}
