package com.example.smart_order_assistant_system.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.smart_order_assistant_system.entity.Sale;

public interface SaleRepository extends JpaRepository<Sale, Long> {

    // 查詢指定商品的所有銷售紀錄
    List<Sale> findByProduct_Id(Long productId);

    // 查詢指定時間區間內的銷售紀錄
    List<Sale> findByDateTimeBetween(
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    );

    // 查詢指定商品在指定時間區間內的銷售紀錄
    List<Sale> findByProduct_IdAndDateTimeBetween(
            Long productId,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    );

    // 查詢指定商品尚未軟刪除的銷售紀錄
    List<Sale> findByProduct_IdAndDeletedAtIsNull(Long productId);

    // 查詢指定商品在指定時間區間內，且尚未軟刪除的銷售紀錄
    List<Sale> findByProduct_IdAndDateTimeBetweenAndDeletedAtIsNull(
            Long productId,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    );

    // 查詢所有尚未軟刪除的銷售紀錄
    List<Sale> findByDeletedAtIsNull();

}