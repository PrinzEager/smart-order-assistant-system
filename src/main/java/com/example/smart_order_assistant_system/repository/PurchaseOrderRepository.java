package com.example.smart_order_assistant_system.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;

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
}
