package com.example.smart_order_assistant_system.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.smart_order_assistant_system.entity.Product;
import com.example.smart_order_assistant_system.entity.Sale;
import com.example.smart_order_assistant_system.repository.ProductRepository;
import com.example.smart_order_assistant_system.repository.SaleRepository;

@Service
@Transactional(readOnly = true)
public class SaleService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;

    // 建構子注入 Repository
    public SaleService(
            SaleRepository saleRepository,
            ProductRepository productRepository) {
        this.saleRepository = saleRepository;
        this.productRepository = productRepository;
    }

    // ========================================
    // 1. 查詢功能
    // ========================================

    // 查詢所有尚未軟刪除的銷售紀錄
    public List<Sale> findAllSales() {
        return saleRepository.findByDeletedAtIsNull();
    }

    // 依銷售紀錄 ID 查詢
    public Sale findSaleById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("銷售紀錄 ID 不可為空");
        }

        return saleRepository.findById(id)
                .filter(sale -> sale.getDeletedAt() == null)
                .orElseThrow(() ->
                        new IllegalArgumentException("找不到銷售紀錄"));
    }

    // 查詢指定商品的所有有效銷售紀錄
    public List<Sale> findSalesByProduct(Long productId) {
        validateProductId(productId);

        return saleRepository
                .findByProduct_IdAndDeletedAtIsNull(productId);
    }

    // 查詢指定時間區間內的有效銷售紀錄
    public List<Sale> findSalesByDateRange(
            LocalDateTime startDateTime,
            LocalDateTime endDateTime) {

        validateDateRange(startDateTime, endDateTime);

        return saleRepository
                .findByDateTimeBetween(startDateTime, endDateTime)
                .stream()
                .filter(sale -> sale.getDeletedAt() == null)
                .toList();
    }

    // 查詢指定商品在指定時間區間內的有效銷售紀錄
    public List<Sale> findSalesByProductAndDateRange(
            Long productId,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime) {

        validateProductId(productId);
        validateDateRange(startDateTime, endDateTime);

        return saleRepository
                .findByProduct_IdAndDateTimeBetweenAndDeletedAtIsNull(
                        productId,
                        startDateTime,
                        endDateTime);
    }

    // ========================================
    // 2. 新增銷售紀錄
    // ========================================

    @Transactional
    public Sale createSale(Sale sale) {
        validateSale(sale);

        // 確認商品存在、已啟用且未刪除
        Product product = findAvailableProduct(
                sale.getProduct().getId());

        LocalDateTime now = LocalDateTime.now();

        sale.setProduct(product);

        // 避免使用者輸入不正確的總金額，統一由系統計算
        sale.setTotalAmount(
                calculateTotalAmount(
                        sale.getQuantity(),
                        sale.getUnitPrice()));

        // 沒有指定銷售時間時，預設為現在
        if (sale.getDateTime() == null) {
            sale.setDateTime(now);
        }

        sale.setCreatedAt(now);
        sale.setUpdatedAt(now);
        sale.setDeletedAt(null);

        return saleRepository.save(sale);
    }

    // ========================================
    // 3. 修改銷售紀錄
    // ========================================

    @Transactional
    public Sale updateSale(Long id, Sale newData) {
        validateSale(newData);

        // 取得原本的有效銷售紀錄
        Sale existing = findSaleById(id);

        // 確認新指定的商品存在且可使用
        Product product = findAvailableProduct(
                newData.getProduct().getId());

        existing.setProduct(product);
        existing.setQuantity(newData.getQuantity());
        existing.setUnitPrice(newData.getUnitPrice());

        // 總金額由系統重新計算
        existing.setTotalAmount(
                calculateTotalAmount(
                        newData.getQuantity(),
                        newData.getUnitPrice()));

        // 未提供新銷售時間時，保留原本的時間
        if (newData.getDateTime() != null) {
            existing.setDateTime(newData.getDateTime());
        }

        existing.setUpdatedAt(LocalDateTime.now());

        return saleRepository.save(existing);
    }

    // ========================================
    // 4. 軟刪除銷售紀錄
    // ========================================

    @Transactional
    public Sale deleteSale(Long id) {
        Sale sale = findSaleById(id);

        LocalDateTime now = LocalDateTime.now();

        sale.setDeletedAt(now);
        sale.setUpdatedAt(now);

        return saleRepository.save(sale);
    }

    // ========================================
    // 5. 私有輔助方法
    // ========================================

    // 驗證銷售紀錄基本資料
    private void validateSale(Sale sale) {
        if (sale == null) {
            throw new IllegalArgumentException("銷售紀錄不可為空");
        }

        if (sale.getProduct() == null
                || sale.getProduct().getId() == null) {
            throw new IllegalArgumentException("必須指定商品");
        }

        if (sale.getQuantity() == null || sale.getQuantity() <= 0) {
            throw new IllegalArgumentException("銷售數量必須大於 0");
        }

        if (sale.getUnitPrice() == null
                || sale.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("銷售單價不可小於 0");
        }

        // 避免單價的小數位超出資料庫欄位 scale=2
        if (sale.getUnitPrice().scale() > 2) {
            throw new IllegalArgumentException("銷售單價最多只能有兩位小數");
        }
    }

    // 計算銷售總金額
    private BigDecimal calculateTotalAmount(
            Integer quantity,
            BigDecimal unitPrice) {

        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    // 確認商品存在且可使用
    private Product findAvailableProduct(Long productId) {
        if (productId == null) {
            throw new IllegalArgumentException("商品 ID 不可為空");
        }

        return productRepository.findById(productId)
                .filter(product ->
                        product.getStatus() != null
                                && product.getStatus() == 1
                                && product.getDeletedAt() == null)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "找不到啟用中的商品，或商品已被刪除"));
    }

    // 驗證商品 ID
    private void validateProductId(Long productId) {
        if (productId == null) {
            throw new IllegalArgumentException("商品 ID 不可為空");
        }
    }

    // 驗證時間區間
    private void validateDateRange(
            LocalDateTime startDateTime,
            LocalDateTime endDateTime) {

        if (startDateTime == null || endDateTime == null) {
            throw new IllegalArgumentException("開始時間與結束時間不可為空");
        }

        if (startDateTime.isAfter(endDateTime)) {
            throw new IllegalArgumentException("開始時間不可晚於結束時間");
        }
    }
}