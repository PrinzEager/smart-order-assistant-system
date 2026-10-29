package com.example.smart_order_assistant_system.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.smart_order_assistant_system.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // 依商品名稱完全相同查詢
    // 商品名稱可能重複，因此使用 List
    List<Product> findByName(String name);

    // 依商品名稱模糊查詢
    List<Product> findByNameContaining(String name);

    // 依商品代碼查詢
    Optional<Product> findBySku(String sku);

    // 依商品狀態查詢
    List<Product> findByStatus(Byte status);

    // 查詢啟用且未軟刪除的商品
    List<Product> findByStatusAndDeletedAtIsNull(Byte status);

    // 查詢已刪除的商品
    List<Product> findByDeletedAtIsNotNull(Byte status);

    // 商品名稱模糊搜尋
    List<Product> findByNameContainingIgnoreCase(String name);

    // 依分類編號查詢商品
    List<Product> findByCategory_Id(Long categoryId);


}