
package com.example.smart_order_assistant_system.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.smart_order_assistant_system.entity.ProductCategory;

public interface ProductCategoryRepository
        extends JpaRepository<ProductCategory, Long> {

    // 依分類名稱查詢
    List<ProductCategory> findByName(String name);

    // 依分類名稱模糊搜尋，忽略英文大小寫
    List<ProductCategory> findByNameContainingIgnoreCase(String name);

    // 依分類狀態查詢
    List<ProductCategory> findByStatus(Byte status);

    // 查詢啟用且未軟刪除的分類
    List<ProductCategory> findByStatusAndDeletedAtIsNull(Byte status);

    // 查詢指定上層分類底下的所有子分類
    List<ProductCategory> findByParent_Id(Long parentId);

    // 查詢最上層分類（沒有上層分類）
    List<ProductCategory> findByParentIsNull();

    // 依分類名稱查詢單一分類（名稱必須唯一才適合使用 Optional）
    Optional<ProductCategory> findByNameAndParentIsNull(String name);

    /*
      只適用於查詢最上層分類，而且假設同名的最上層分類不會重複
      如果資料庫沒有設定唯一限制，實際上仍可能查到多筆資料
      若不打算限制分類名稱唯一，可改為
    */
    // List<ProductCategory> findByNameAndParentIsNull(String name);

}
