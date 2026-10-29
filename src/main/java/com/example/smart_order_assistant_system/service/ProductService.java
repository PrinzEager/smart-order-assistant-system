package com.example.smart_order_assistant_system.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.smart_order_assistant_system.entity.Product;
import com.example.smart_order_assistant_system.repository.ProductRepository;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    // 建構子注入：讓 Spring 將 ProductRepository 傳入
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // 查詢所有啟用且未軟刪除的商品
    public List<Product> findAllProducts() {
        return productRepository.findByStatusAndDeletedAtIsNull(
                (byte) 1
        );
    }

    // 依商品 ID 查詢啟用且未軟刪除的商品
    public Product findProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "找不到商品，商品 ID：" + id
                        )
                );

        if (!isAvailable(product)) {
            throw new IllegalArgumentException(
                    "商品不存在、已停售或已刪除"
            );
        }

        return product;
    }

    // 依商品名稱搜尋，不區分大小寫
    public List<Product> searchProductsByName(String name) {
        if (name == null || name.isBlank()) {
            return findAllProducts();
        }

        return productRepository
                .findByNameContainingIgnoreCase(name.trim())
                .stream()
                .filter(this::isAvailable)
                .toList();
    }

    // 依商品代碼查詢
    public Product findProductBySku(String sku) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "找不到商品代碼：" + sku
                        )
                );

        if (!isAvailable(product)) {
            throw new IllegalArgumentException(
                    "商品不存在、已停售或已刪除"
            );
        }

        return product;
    }

    // 依商品分類 ID 查詢
    public List<Product> findProductsByCategory(Long categoryId) {
        return productRepository.findByCategory_Id(categoryId)
                .stream()
                .filter(this::isAvailable)
                .toList();
    }

    // 新增商品
    @Transactional
    public Product createProduct(Product product) {
        validateProduct(product);

        // 商品代碼不可重複
        if (productRepository.findBySku(product.getSku()).isPresent()) {
            throw new IllegalArgumentException(
                    "商品代碼已存在：" + product.getSku()
            );
        }

        // 新商品不可直接建立成已刪除狀態
        product.setStatus((byte) 1);
        product.setDeletedAt(null);

        LocalDateTime now = LocalDateTime.now();
        product.setCreatedAt(now);
        product.setUpdatedAt(now);

        return productRepository.save(product);
    }

    // 修改商品
    @Transactional
    public Product updateProduct(Long id, Product newData) {
        validateProduct(newData);

        Product existingProduct = findProductById(id);

        // 如果修改商品代碼，檢查是否與其他商品重複
        productRepository.findBySku(newData.getSku())
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new IllegalArgumentException(
                            "已存在商品代碼："
                                    + newData.getSku()
                    );
                });

        // 更新商品基本資料
        existingProduct.setCategory(newData.getCategory());
        existingProduct.setSku(newData.getSku());
        existingProduct.setBarcode(newData.getBarcode());
        existingProduct.setImageUrl(newData.getImageUrl());
        existingProduct.setName(newData.getName());
        existingProduct.setPrice(newData.getPrice());
        existingProduct.setCostPrice(newData.getCostPrice());
        existingProduct.setUnit(newData.getUnit());
        existingProduct.setCurrentStock(newData.getCurrentStock());
        existingProduct.setSafetyStock(newData.getSafetyStock());
        existingProduct.setReorderPoint(newData.getReorderPoint());
        existingProduct.setLeadTimeDays(newData.getLeadTimeDays());
        existingProduct.setShelfLifeDays(newData.getShelfLifeDays());
        existingProduct.setUpdatedAt(LocalDateTime.now());

        return productRepository.save(existingProduct);
    }

    // 停用商品：保留資料，不設定刪除時間
    @Transactional
    public Product deactivateProduct(Long id) {
        Product product = findProductById(id);

        product.setStatus((byte) 0);
        product.setUpdatedAt(LocalDateTime.now());

        return productRepository.save(product);
    }

    // 軟刪除商品：保留資料並記錄刪除時間
    @Transactional
    public Product deleteProduct(Long id) {
        Product product = findProductById(id);

        product.setStatus((byte) 0);
        product.setDeletedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        return productRepository.save(product);
    }

    // 檢查商品是否啟用且未軟刪除
    private boolean isAvailable(Product product) {
        return product.getStatus() != null
                && product.getStatus() == 1
                && product.getDeletedAt() == null;
    }

    // 驗證商品必要欄位與數值
    private void validateProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("商品資料不可為空");
        }

        if (product.getName() == null
                || product.getName().isBlank()) {
            throw new IllegalArgumentException("商品名稱不可為空");
        }

        if (product.getSku() == null
                || product.getSku().isBlank()) {
            throw new IllegalArgumentException("商品代碼不可為空");
        }

        if (product.getPrice() == null
                || product.getPrice().signum() < 0) {
            throw new IllegalArgumentException(
                    "商品售價不可為空或小於 0"
            );
        }

        if (product.getCostPrice() != null
                && product.getCostPrice().signum() < 0) {
            throw new IllegalArgumentException(
                    "商品進貨成本不可小於 0"
            );
        }

        if (product.getUnit() == null
                || product.getUnit().isBlank()) {
            throw new IllegalArgumentException("庫存單位不可為空");
        }

        if (product.getCurrentStock() == null
                || product.getCurrentStock() < 0) {
            throw new IllegalArgumentException(
                    "目前庫存不可為空或小於 0"
            );
        }

        if (product.getSafetyStock() == null
                || product.getSafetyStock() < 0) {
            throw new IllegalArgumentException(
                    "安全庫存不可為空或小於 0"
            );
        }

        if (product.getReorderPoint() == null
                || product.getReorderPoint() < 0) {
            throw new IllegalArgumentException(
                    "補貨警戒點不可為空或小於 0"
            );
        }

        if (product.getLeadTimeDays() == null
                || product.getLeadTimeDays() < 0) {
            throw new IllegalArgumentException(
                    "交貨天數不可為空或小於 0"
            );
        }

        if (product.getShelfLifeDays() != null
                && product.getShelfLifeDays() < 0) {
            throw new IllegalArgumentException(
                    "保存期限不可小於 0"
            );
        }
    }
}