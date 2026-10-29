package com.example.smart_order_assistant_system.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.smart_order_assistant_system.entity.Supplier;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    // 依供應商名稱查詢（名稱可能重複，因此回傳 List）
    List<Supplier> findByName(String name);

    // 依供應商名稱進行模糊搜尋，不區分英文大小寫
    List<Supplier> findByNameContainingIgnoreCase(String name);

    // 依供應商電話查詢
    List<Supplier> findByPhone(String phone);

    // 依供應商電子郵件查詢
    // 查詢結果可能有一筆資料，也可能沒有資料
    // 電子郵件不重複時才適合使用 Optional
    Optional<Supplier> findByEmail(String email);

    // 允許多筆供應商共用電子郵件
    // List<Supplier> findByEmail(String email);

    // 查詢指定狀態的供應商，例如 1=啟用、0=停用
    List<Supplier> findByStatus(Byte status);

    // 查詢所有啟用且尚未軟刪除的供應商
    List<Supplier> findByStatusAndDeletedAtIsNull(Byte status);

    // 查詢所有尚未軟刪除的供應商
    List<Supplier> findByDeletedAtIsNull();

    // 依供應商編號查詢尚未軟刪除的資料
    Optional<Supplier> findByIdAndDeletedAtIsNull(Long id);
}