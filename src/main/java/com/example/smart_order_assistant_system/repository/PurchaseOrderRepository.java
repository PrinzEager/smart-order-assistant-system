package com.example.smart_order_assistant_system.repository;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.smart_order_assistant_system.entity.PurchaseOrder;

public interface PurchaseOrderRepository
        extends JpaRepository<PurchaseOrder, Long> {

    // 依訂貨流程狀態查詢
    List<PurchaseOrder> findByOrderStatus(String orderStatus);

    // 查詢指定狀態且未軟刪除的訂貨單
    List<PurchaseOrder> findByOrderStatusAndDeletedAtIsNull(
            String orderStatus
    );

    // 依供應商編號查詢訂貨單
    List<PurchaseOrder> findBySupplier_Id(Long supplierId);

    // 依訂貨日期時間區間查詢
    List<PurchaseOrder> findByOrderDateBetween(
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    // 查詢指定日期區間內、未軟刪除的訂貨單
    List<PurchaseOrder> findByOrderDateBetweenAndDeletedAtIsNull(
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    // 依訂貨單編號及未軟刪除條件查詢
    Optional<PurchaseOrder> findByIdAndDeletedAtIsNull(Long id);

}