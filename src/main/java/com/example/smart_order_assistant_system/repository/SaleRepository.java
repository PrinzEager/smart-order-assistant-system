package com.example.smart_order_assistant_system.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.smart_order_assistant_system.entity.Sale;

public interface SaleRepository extends JpaRepository<Sale, Long> {

    // 查詢所有尚未軟刪除的銷售紀錄（分頁）
    Page<Sale> findByDeletedAtIsNull(Pageable pageable);

    // 查詢指定商品尚未軟刪除的銷售紀錄（分頁）
    Page<Sale> findByProduct_IdAndDeletedAtIsNull(
            Long productId,
            Pageable pageable
    );

    // 查詢指定時間區間內尚未軟刪除的銷售紀錄（分頁）
    Page<Sale> findByDateTimeBetweenAndDeletedAtIsNull(
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            Pageable pageable
    );

    // 查詢指定商品在指定時間區間內尚未軟刪除的銷售紀錄（分頁）
    Page<Sale> findByProduct_IdAndDateTimeBetweenAndDeletedAtIsNull(
            Long productId,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            Pageable pageable
    );
}
