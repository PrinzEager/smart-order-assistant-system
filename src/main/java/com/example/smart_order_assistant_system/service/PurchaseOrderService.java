package com.example.smart_order_assistant_system.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.smart_order_assistant_system.constant.OrderStatus;
import com.example.smart_order_assistant_system.entity.Product;
import com.example.smart_order_assistant_system.entity.PurchaseOrder;
import com.example.smart_order_assistant_system.entity.PurchaseOrderItem;
import com.example.smart_order_assistant_system.entity.Supplier;
import com.example.smart_order_assistant_system.exception.BusinessConflictException;
import com.example.smart_order_assistant_system.exception.ResourceNotFoundException;
import com.example.smart_order_assistant_system.repository.ProductRepository;
import com.example.smart_order_assistant_system.repository.PurchaseOrderItemRepository;
import com.example.smart_order_assistant_system.repository.PurchaseOrderRepository;
import com.example.smart_order_assistant_system.repository.SupplierRepository;

@Service
@Transactional(readOnly = true)
public class PurchaseOrderService {

    private static final Byte ACTIVE = 1;
    private static final Byte INACTIVE = 0;

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

        return purchaseOrderRepository
                .findByStatusAndDeletedAtIsNull(ACTIVE);
    }

    // 依訂貨單 ID 查詢
    public PurchaseOrder findPurchaseOrderById(Long id) {

        if (id == null) {
            throw new IllegalArgumentException("訂貨單 ID 不可為空");
        }

        return purchaseOrderRepository
                .findByIdAndStatusAndDeletedAtIsNull(id, ACTIVE)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "找不到有效的訂貨單"));
    }

    // 依訂貨流程狀態查詢
    public List<PurchaseOrder> findPurchaseOrdersByStatus(
            String orderStatus) {

        validateOrderStatus(orderStatus);

        return purchaseOrderRepository
                .findByOrderStatusAndStatusAndDeletedAtIsNull(
                        orderStatus,
                        ACTIVE
                );
    }

    // 依供應商 ID 查詢訂貨單
    public List<PurchaseOrder> findPurchaseOrdersBySupplier(
            Long supplierId) {

        // 檢查供應商 ID 是否有效
        if (supplierId == null) {
            throw new IllegalArgumentException("供應商 ID 不可為空");
        }

        // 查詢該供應商的有效訂貨單
        // 不檢查供應商目前是否啟用，以保留歷史訂單查詢能力
        return purchaseOrderRepository
                .findBySupplier_IdAndStatusAndDeletedAtIsNull(supplierId, ACTIVE);
    }

    // 依訂貨日期區間查詢
    public List<PurchaseOrder> findPurchaseOrdersByDateRange(
            LocalDateTime startDate,
            LocalDateTime endDate) {

        validateDateRange(startDate, endDate);

        return purchaseOrderRepository
                .findByOrderDateBetweenAndStatusAndDeletedAtIsNull(
                        startDate,
                        endDate,
                        ACTIVE
                );
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

        // 驗證訂貨單基本資料
        validateNewOrder(order, items);

        // 確認供應商存在、啟用且未刪除
        Supplier supplier = findAvailableSupplier(
                order.getSupplier().getId());

        LocalDateTime now = LocalDateTime.now();

        // 設定訂貨單基本資料
        order.setId(null);
        order.setSupplier(supplier);

        // 未提供訂貨日期時，使用目前時間
        if (order.getOrderDate() == null) {
            order.setOrderDate(now);
        }

        // 再次檢查預計到貨日期
        if (order.getExpectedDeliveryDate() != null
                && order.getExpectedDeliveryDate().isBefore(
                        order.getOrderDate().toLocalDate())) {
            throw new IllegalArgumentException(
                    "預計到貨日期不可早於訂貨日期");
        }

        // 新增訂貨單一律從草稿狀態開始
        order.setOrderStatus(OrderStatus.DRAFT);
        order.setStatus(ACTIVE);
        order.setTotalAmount(BigDecimal.ZERO);
        order.setDeletedAt(null);
        order.setCreatedAt(now);
        order.setUpdatedAt(now);

        // 先儲存訂貨單，取得資料庫產生的 ID
        PurchaseOrder savedOrder =
                purchaseOrderRepository.save(order);

        BigDecimal totalAmount = BigDecimal.ZERO;

        // 用來檢查同一張訂貨單是否有重複商品
        Set<Long> productIds = new HashSet<>();

        // 逐筆驗證並儲存訂貨明細
        for (PurchaseOrderItem item : items) {

            validateOrderItem(item);

            Long productId = item.getProduct().getId();

            // 同一張訂貨單不允許重複新增相同商品
            if (!productIds.add(productId)) {
                throw new BusinessConflictException(
                        "同一張訂貨單不可重複新增相同商品");
            }

            // 確認商品存在、啟用且未刪除
            Product product = findAvailableProduct(productId);

            // 確認商品進貨成本存在且合理
            BigDecimal unitPrice = product.getCostPrice();

            if (unitPrice == null) {
                throw new BusinessConflictException(
                        "商品「" + product.getName()
                                + "」尚未設定進貨成本，無法建立訂貨單");
            }

            if (unitPrice.compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessConflictException(
                        "商品進貨成本不可小於 0");
            }

            // 檢查進貨成本的小數位數
            if (unitPrice.stripTrailingZeros().scale() > 2) {
                throw new BusinessConflictException(
                        "商品進貨成本最多只能有兩位小數");
            }

            // 計算商品小計
            BigDecimal subtotal = unitPrice.multiply(
                    BigDecimal.valueOf(item.getQuantity()));

            // 設定訂貨明細資料
            item.setId(null);
            item.setPurchaseOrder(savedOrder);
            item.setProduct(product);

            // 使用商品目前的進貨成本作為訂貨當下單價
            item.setUnitPrice(unitPrice);
            item.setSubtotal(subtotal);
            item.setCreatedAt(now);

            // 儲存訂貨明細
            purchaseOrderItemRepository.save(item);

            // 累加訂貨單總金額
            totalAmount = totalAmount.add(subtotal);
        }

        // 更新訂貨單總金額及最後更新時間
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
            throw new BusinessConflictException(
                    "只有 DRAFT 狀態的訂貨單可以修改");
        }

        // 有提供新供應商時，檢查供應商是否有效
        if (newData.getSupplier() != null) {

            if (newData.getSupplier().getId() == null) {
                throw new IllegalArgumentException(
                        "供應商 ID 不可為空");
            }

            Supplier supplier = findAvailableSupplier(
                    newData.getSupplier().getId());

            existing.setSupplier(supplier);
        }

        // 預計到貨日期可以設為 null
        LocalDate expectedDate =
                newData.getExpectedDeliveryDate();

        if (expectedDate != null
                && expectedDate.isBefore(
                        existing.getOrderDate().toLocalDate())) {
            throw new IllegalArgumentException(
                    "預計到貨日期不可早於訂貨日期");
        }

        // 更新訂貨單資料
        existing.setExpectedDeliveryDate(expectedDate);
        existing.setRemark(
                normalizeOptionalText(newData.getRemark()));
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

        // 只有草稿狀態可以送出
        if (!OrderStatus.DRAFT.equals(order.getOrderStatus())) {
            throw new BusinessConflictException(
                    "只有 DRAFT 狀態的訂貨單可以送出");
        }

        // 確認至少有一筆明細
        List<PurchaseOrderItem> items =
                purchaseOrderItemRepository
                        .findByPurchaseOrder_Id(id);

        if (items.isEmpty()) {
            throw new BusinessConflictException(
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

        // 只有已送出的訂貨單可以標記到貨
        if (!OrderStatus.SUBMITTED.equals(order.getOrderStatus())) {
            throw new BusinessConflictException(
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

        // 只有草稿或已送出的訂貨單可以取消
        if (!OrderStatus.DRAFT.equals(order.getOrderStatus())
                && !OrderStatus.SUBMITTED.equals(
                        order.getOrderStatus())) {
            throw new BusinessConflictException(
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
                && !OrderStatus.CANCELLED.equals(
                        order.getOrderStatus())) {
            throw new BusinessConflictException(
                    "只有 DRAFT 或 CANCELLED 狀態的訂貨單可以刪除");
        }

        LocalDateTime now = LocalDateTime.now();

        // 設定軟刪除狀態
        order.setStatus(INACTIVE);
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

        // 有提供訂貨日期時，檢查預計到貨日期
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

        if (item.getQuantity() == null
                || item.getQuantity() <= 0) {
            throw new IllegalArgumentException(
                    "訂貨數量必須大於 0");
        }
    }

    // 確認供應商存在、啟用且未刪除
    private Supplier findAvailableSupplier(Long supplierId) {

        if (supplierId == null) {
            throw new IllegalArgumentException("供應商 ID 不可為空");
        }

        // 先確認供應商是否存在且未刪除
        Supplier supplier = supplierRepository
                .findByIdAndDeletedAtIsNull(supplierId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "找不到供應商，或供應商已被刪除"));

        // 再確認供應商是否啟用
        if (!ACTIVE.equals(supplier.getStatus())) {
            throw new BusinessConflictException(
                    "停用中的供應商無法建立訂貨單");
        }

        return supplier;
    }

    // 確認商品存在、啟用且未刪除
    private Product findAvailableProduct(Long productId) {

        if (productId == null) {
            throw new IllegalArgumentException("商品 ID 不可為空");
        }

        // 先確認商品是否存在
        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "找不到指定商品"));

        // 確認商品是否已軟刪除
        if (product.getDeletedAt() != null) {
            throw new ResourceNotFoundException(
                    "商品已被刪除，無法建立訂貨單");
        }

        // 確認商品是否啟用
        if (!ACTIVE.equals(product.getStatus())) {
            throw new BusinessConflictException(
                    "停用中的商品無法建立訂貨單");
        }

        return product;
    }

    // 驗證訂貨流程狀態
    private void validateOrderStatus(String orderStatus) {

        if (!OrderStatus.DRAFT.equals(orderStatus)
                && !OrderStatus.SUBMITTED.equals(orderStatus)
                && !OrderStatus.RECEIVED.equals(orderStatus)
                && !OrderStatus.CANCELLED.equals(orderStatus)) {
            throw new IllegalArgumentException(
                    "不合法的訂貨單狀態");
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
