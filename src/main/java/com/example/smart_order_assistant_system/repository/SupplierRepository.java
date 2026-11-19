package com.example.smart_order_assistant_system.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.smart_order_assistant_system.entity.Supplier;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    // ==========================================
    // 1. 供應商名稱查詢
    // ==========================================

    // 依供應商名稱查詢（名稱可能重複，因此回傳 List）
    List<Supplier> findByName(String name);

    // 依供應商名稱進行模糊搜尋，不區分英文大小寫
    List<Supplier> findByNameContainingIgnoreCase(String name);

    // 依供應商名稱搜尋（分頁），排除已軟刪除資料
    Page<Supplier> findByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNull(
            String name,
            Byte status,
            Pageable pageable
    );

    // ==========================================
    // 2. 供應商電話查詢
    // ==========================================

    // 依供應商電話查詢
    List<Supplier> findByPhone(String phone);

    // ==========================================
    // 3. 供應商電子郵件查詢
    // ==========================================

    // 依電子郵件查詢尚未軟刪除的供應商
    // 軟刪除的供應商不再佔用電子郵件
    Optional<Supplier> findByEmailAndDeletedAtIsNull(String email);

    // 修改供應商時，查詢是否有其他未刪除的供應商使用相同電子郵件
    // IdNot 代表排除目前正在修改的供應商
    Optional<Supplier> findByEmailAndDeletedAtIsNullAndIdNot(
            String email,
            Long id
    );

    // ==========================================
    // 4. 供應商狀態查詢
    // ==========================================

    // 查詢指定狀態的供應商，例如 1=啟用、0=停用
    List<Supplier> findByStatus(Byte status);

    // 查詢指定狀態且尚未軟刪除的供應商
    List<Supplier> findByStatusAndDeletedAtIsNull(Byte status);

    // 分頁查詢指定狀態且尚未軟刪除的供應商
    Page<Supplier> findByStatusAndDeletedAtIsNull(
            Byte status,
            Pageable pageable
    );

    // ==========================================
    // 5. 軟刪除查詢
    // ==========================================

    // 查詢所有尚未軟刪除的供應商
    List<Supplier> findByDeletedAtIsNull();

    // 依供應商編號查詢尚未軟刪除的資料
    Optional<Supplier> findByIdAndDeletedAtIsNull(Long id);
}