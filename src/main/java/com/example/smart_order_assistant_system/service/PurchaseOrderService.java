package com.example.smart_order_assistant_system.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.smart_order_assistant_system.entity.Product;
import com.example.smart_order_assistant_system.entity.PurchaseOrder;
import com.example.smart_order_assistant_system.entity.PurchaseOrderItem;
import com.example.smart_order_assistant_system.entity.Supplier;
import com.example.smart_order_assistant_system.repository.ProductRepository;
import com.example.smart_order_assistant_system.repository.PurchaseOrderItemRepository;
import com.example.smart_order_assistant_system.repository.PurchaseOrderRepository;
import com.example.smart_order_assistant_system.repository.SupplierRepository;
import com.example.smart_order_assistant_system.constant.OrderStatus;

@Service
@Transactional(readOnly = true)
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;

    // 建構子注入 Repository
    public PurchaseOrderService(
            PurchaseOrderRepository purchaseOrderRepository,
            PurchaseOrderItemRepository purchaseOrderItemRepository,
            SupplierRepository supplierRepository,
            ProductRepository productRepository) {

        this.purchaseOrderRepository = purchaseOrderRepository;
        this.purchaseOrderItemRepository = purchaseOrderItemRepository;
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
    }

    // ========================================
    // 1. 查詢功能
    // ========================================

    // 查詢所有正常且未軟刪除的訂貨單
    public List<PurchaseOrder> findAllPurchaseOrders() {
        return purchaseOrderRepository.findAll()
                .stream()
                .filter(this::isAvailable)
                .toList();
    }

    // 依訂貨單 ID 查詢
    public PurchaseOrder findPurchaseOrderById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("訂貨單 ID 不可為空");
        }

        return purchaseOrderRepository.findByIdAndDeletedAtIsNull(id)
                .filter(this::isAvailable)
                .orElseThrow(() ->
                        new IllegalArgumentException("找不到有效的訂貨單"));
    }

    // 依訂貨流程狀態查詢
    public List<PurchaseOrder> findPurchaseOrdersByStatus(
            String orderStatus) {

        validateOrderStatus(orderStatus);

        return purchaseOrderRepository
                .findByOrderStatusAndDeletedAtIsNull(orderStatus)
                .stream()
                .filter(this::isAvailable)
                .toList();
    }

    // 依供應商 ID 查詢訂貨單
    public List<PurchaseOrder> findPurchaseOrdersBySupplier(
            Long supplierId) {

        if (supplierId == null) {
            throw new IllegalArgumentException("供應商 ID 不可為空");
        }

        return purchaseOrderRepository
                .findBySupplier_Id(supplierId)
                .stream()
                .filter(this::isAvailable)
                .toList();
    }

    // 依訂貨日期區間查詢
    public List<PurchaseOrder> findPurchaseOrdersByDateRange(
            LocalDateTime startDate,
            LocalDateTime endDate) {

        validateDateRange(startDate, endDate);

        return purchaseOrderRepository
                .findByOrderDateBetweenAndDeletedAtIsNull(
                        startDate, endDate)
                .stream()
                .filter(this::isAvailable)
                .toList();
    }

    // 查詢指定訂貨單的所有商品明細
    public List<PurchaseOrderItem> findPurchaseOrderItems(Long orderId) {
        // 先確認訂貨單存在且有效
        findPurchaseOrderById(orderId);

        return purchaseOrderItemRepository
                .findByPurchaseOrder_Id(orderId);
    }

    // ========================================
    // 2. 新增訂貨單及明細
    // ========================================

    @Transactional
    public PurchaseOrder createPurchaseOrder(
            PurchaseOrder order,
            List<PurchaseOrderItem> items) {

        validateNewOrder(order, items);

        // 確認供應商存在、啟用且未刪除
        Supplier supplier = findAvailableSupplier(
                order.getSupplier().getId());

        LocalDateTime now = LocalDateTime.now();

        order.setSupplier(supplier);
        order.setOrderDate(
                order.getOrderDate() == null
                        ? now
                        : order.getOrderDate());

        order.setOrderStatus(OrderStatus.DRAFT);
        order.setStatus((byte) 1);
        order.setTotalAmount(BigDecimal.ZERO);
        order.setDeletedAt(null);
        order.setCreatedAt(now);
        order.setUpdatedAt(now);

        // 先儲存訂貨單，取得資料庫產生的 ID
        PurchaseOrder savedOrder =
                purchaseOrderRepository.save(order);

        BigDecimal totalAmount = BigDecimal.ZERO;

        // 逐筆驗證並儲存訂貨明細
        for (PurchaseOrderItem item : items) {
            validateOrderItem(item);

            Product product = findAvailableProduct(
                    item.getProduct().getId());

            item.setPurchaseOrder(savedOrder);
            item.setProduct(product);

            // 計算商品小計
            BigDecimal subtotal = item.getUnitPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));

            item.setSubtotal(subtotal);
            item.setCreatedAt(now);

            purchaseOrderItemRepository.save(item);

            // 累加訂貨單總金額
            totalAmount = totalAmount.add(subtotal);
        }

        // 更新訂貨單總金額
        savedOrder.setTotalAmount(totalAmount);
        savedOrder.setUpdatedAt(LocalDateTime.now());

        return purchaseOrderRepository.save(savedOrder);
    }

    // ========================================
    // 3. 修改訂貨單基本資料
    // ========================================

    @Transactional
    public PurchaseOrder updatePurchaseOrder(
            Long id,
            PurchaseOrder newData) {

        if (newData == null) {
            throw new IllegalArgumentException("訂貨單資料不可為空");
        }

        PurchaseOrder existing = findPurchaseOrderById(id);

        // 只允許修改草稿中的訂貨單
        if (!OrderStatus.DRAFT.equals(existing.getOrderStatus())) {
            throw new IllegalArgumentException(
                    "只有 DRAFT 狀態的訂貨單可以修改");
        }

        // 有提供新供應商時，檢查供應商是否有效
        if (newData.getSupplier() != null) {
            if (newData.getSupplier().getId() == null) {
                throw new IllegalArgumentException("供應商 ID 不可為空");
            }

            Supplier supplier = findAvailableSupplier(
                    newData.getSupplier().getId());

            existing.setSupplier(supplier);
        }

        // 預計到貨日期可以設為 null
        LocalDate expectedDate = newData.getExpectedDeliveryDate();

        if (expectedDate != null
                && expectedDate.isBefore(
                        existing.getOrderDate().toLocalDate())) {
            throw new IllegalArgumentException(
                    "預計到貨日期不可早於訂貨日期");
        }

        existing.setExpectedDeliveryDate(expectedDate);
        existing.setRemark(normalizeOptionalText(newData.getRemark()));
        existing.setUpdatedAt(LocalDateTime.now());

        return purchaseOrderRepository.save(existing);
    }

    // ========================================
    // 4. 訂貨流程狀態管理
    // ========================================

    // 草稿送出
    @Transactional
    public PurchaseOrder submitPurchaseOrder(Long id) {
        PurchaseOrder order = findPurchaseOrderById(id);

        if (!OrderStatus.DRAFT.equals(order.getOrderStatus())) {
            throw new IllegalArgumentException(
                    "只有 DRAFT 狀態的訂貨單可以送出");
        }

        // 確認至少有一筆明細
        List<PurchaseOrderItem> items =
                purchaseOrderItemRepository
                        .findByPurchaseOrder_Id(id);

        if (items.isEmpty()) {
            throw new IllegalArgumentException(
                    "訂貨單至少需要一筆商品明細");
        }

        order.setOrderStatus(OrderStatus.SUBMITTED);
        order.setUpdatedAt(LocalDateTime.now());

        return purchaseOrderRepository.save(order);
    }

    // 標記為已到貨
    @Transactional
    public PurchaseOrder receivePurchaseOrder(Long id) {
        PurchaseOrder order = findPurchaseOrderById(id);

        if (!OrderStatus.SUBMITTED.equals(order.getOrderStatus())) {
            throw new IllegalArgumentException(
                    "只有 SUBMITTED 狀態的訂貨單可以標記到貨");
        }

        order.setOrderStatus(OrderStatus.RECEIVED);
        order.setUpdatedAt(LocalDateTime.now());

        return purchaseOrderRepository.save(order);
    }

    // 取消訂貨單
    @Transactional
    public PurchaseOrder cancelPurchaseOrder(Long id) {
        PurchaseOrder order = findPurchaseOrderById(id);

        if (!OrderStatus.DRAFT.equals(order.getOrderStatus())
                && !OrderStatus.SUBMITTED.equals(order.getOrderStatus())) {
            throw new IllegalArgumentException(
                    "只有 DRAFT 或 SUBMITTED 狀態的訂貨單可以取消");
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());

        return purchaseOrderRepository.save(order);
    }

    // ========================================
    // 5. 軟刪除訂貨單
    // ========================================

    @Transactional
    public PurchaseOrder deletePurchaseOrder(Long id) {
        PurchaseOrder order = findPurchaseOrderById(id);

        // 已送出或已到貨的訂貨單保留歷史紀錄
        if (!OrderStatus.DRAFT.equals(order.getOrderStatus())
                && !OrderStatus.CANCELLED.equals(order.getOrderStatus())) {
            throw new IllegalArgumentException(
                    "只有 DRAFT 或 CANCELLED 狀態的訂貨單可以刪除");
        }

        LocalDateTime now = LocalDateTime.now();

        order.setStatus((byte) 0);
        order.setDeletedAt(now);
        order.setUpdatedAt(now);

        return purchaseOrderRepository.save(order);
    }

    // ========================================
    // 6. 私有輔助方法
    // ========================================

    // 驗證新訂貨單
    private void validateNewOrder(
            PurchaseOrder order,
            List<PurchaseOrderItem> items) {

        if (order == null) {
            throw new IllegalArgumentException("訂貨單不可為空");
        }

        if (order.getSupplier() == null
                || order.getSupplier().getId() == null) {
            throw new IllegalArgumentException("必須指定供應商");
        }

        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException(
                    "訂貨單至少需要一筆商品明細");
        }

        if (order.getRemark() != null
                && order.getRemark().length() > 500) {
            throw new IllegalArgumentException(
                    "訂貨備註不可超過 500 字");
        }

        if (order.getExpectedDeliveryDate() != null
                && order.getOrderDate() != null
                && order.getExpectedDeliveryDate().isBefore(
                        order.getOrderDate().toLocalDate())) {
            throw new IllegalArgumentException(
                    "預計到貨日期不可早於訂貨日期");
        }
    }

    // 驗證訂貨明細
    private void validateOrderItem(PurchaseOrderItem item) {
        if (item == null) {
            throw new IllegalArgumentException("訂貨明細不可為空");
        }

        if (item.getProduct() == null
                || item.getProduct().getId() == null) {
            throw new IllegalArgumentException(
                    "每筆訂貨明細都必須指定商品");
        }

        if (item.getQuantity() == null || item.getQuantity() <= 0) {
            throw new IllegalArgumentException("訂貨數量必須大於 0");
        }

        if (item.getUnitPrice() == null
                || item.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("訂貨單價不可小於 0");
        }

        if (item.getUnitPrice().scale() > 2) {
            throw new IllegalArgumentException(
                    "訂貨單價最多只能有兩位小數");
        }
    }

    // 確認供應商存在、啟用且未刪除
    private Supplier findAvailableSupplier(Long supplierId) {
        if (supplierId == null) {
            throw new IllegalArgumentException("供應商 ID 不可為空");
        }

        return supplierRepository.findByIdAndDeletedAtIsNull(supplierId)
                .filter(supplier ->
                        supplier.getStatus() != null
                                && supplier.getStatus() == 1)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "找不到啟用中的供應商，或供應商已被刪除"));
    }

    // 確認商品存在、啟用且未刪除
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

    // 判斷訂貨單是否正常且未軟刪除
    private boolean isAvailable(PurchaseOrder order) {
        return order.getStatus() != null
                && order.getStatus() == 1
                && order.getDeletedAt() == null;
    }

    // 驗證訂貨流程狀態
    private void validateOrderStatus(String orderStatus) {
        if (!OrderStatus.DRAFT.equals(orderStatus)
                && !OrderStatus.SUBMITTED.equals(orderStatus)
                && !OrderStatus.RECEIVED.equals(orderStatus)
                && !OrderStatus.CANCELLED.equals(orderStatus)) {
            throw new IllegalArgumentException("不合法的訂貨單狀態");
        }
    }

    // 驗證日期區間
    private void validateDateRange(
            LocalDateTime startDate,
            LocalDateTime endDate) {

        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException(
                    "開始日期與結束日期不可為空");
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException(
                    "開始日期不可晚於結束日期");
        }
    }

    // 空白字串轉為 null
    private String normalizeOptionalText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String trimmed = value.trim();

        if (trimmed.length() > 500) {
            throw new IllegalArgumentException(
                    "訂貨備註不可超過 500 字");
        }

        return trimmed;
    }
}