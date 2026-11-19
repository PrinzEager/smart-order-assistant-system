package com.example.smart_order_assistant_system.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.example.smart_order_assistant_system.entity.Product;
import com.example.smart_order_assistant_system.entity.PurchaseOrder;
import com.example.smart_order_assistant_system.entity.PurchaseOrderItem;
import com.example.smart_order_assistant_system.entity.Supplier;
import com.example.smart_order_assistant_system.request.PurchaseOrderItemRequest;
import com.example.smart_order_assistant_system.request.PurchaseOrderRequest;
import com.example.smart_order_assistant_system.response.PurchaseOrderItemResponse;
import com.example.smart_order_assistant_system.response.PurchaseOrderResponse;
import com.example.smart_order_assistant_system.service.PurchaseOrderService;
import com.example.smart_order_assistant_system.request.PurchaseOrderUpdateRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    // ========================================
    // 1. 查詢所有訂貨單
    // ========================================

    @GetMapping
    public ResponseEntity<List<PurchaseOrderResponse>> findAllPurchaseOrders() {

        List<PurchaseOrderResponse> responses =
                purchaseOrderService.findAllPurchaseOrders()
                        .stream()
                        .map(this::convertToResponse)
                        .toList();

        return ResponseEntity.ok(responses);
    }


    // ========================================
    // 1-1. 分頁及多條件查詢訂貨單
    // ========================================

    @GetMapping("/page")
    public ResponseEntity<Page<PurchaseOrderResponse>> findPurchaseOrdersPage(

            // 供應商 ID，選填
            @RequestParam(required = false) Long supplierId,

            // 訂單狀態，選填
            @RequestParam(required = false) String orderStatus,

            // 開始日期，選填，格式：yyyy-MM-dd
            @RequestParam(required = false) LocalDate startDate,

            // 結束日期，選填，格式：yyyy-MM-dd
            @RequestParam(required = false) LocalDate endDate,

            // 頁碼，預設從第 0 頁開始
            @RequestParam(defaultValue = "0") int page,

            // 每頁筆數，預設 10 筆
            @RequestParam(defaultValue = "10") int size,

            // 排序欄位，預設依建立時間排序
            @RequestParam(defaultValue = "createdAt") String sortBy,

            // 排序方向，預設由新到舊
            @RequestParam(defaultValue = "DESC") String direction) {

        // ========================================
        // I. 驗證分頁參數
        // ========================================

        // 檢查頁碼不可小於 0
        if (page < 0) {
            throw new IllegalArgumentException(
                    "頁碼不可小於 0");
        }

        // 檢查每頁筆數必須大於 0
        if (size <= 0) {
            throw new IllegalArgumentException(
                    "每頁筆數必須大於 0");
        }

        // 限制每頁最多查詢 100 筆
        if (size > 100) {
            throw new IllegalArgumentException(
                    "每頁筆數不可超過 100");
        }

        // ========================================
        // II. 驗證排序欄位
        // ========================================

        // 只允許指定欄位進行排序
        if (!List.of("createdAt", "orderDate", "totalAmount", "id")
                .contains(sortBy)) {

            throw new IllegalArgumentException(
                    "不支援的排序欄位");
        }

        // ========================================
        // III. 驗證排序方向
        // ========================================

        Sort.Direction sortDirection;

        try {
            sortDirection = Sort.Direction.fromString(direction);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "排序方向只能是 ASC 或 DESC");
        }

        // ========================================
        // IV. 建立分頁及排序條件
        // ========================================

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, sortBy)
        );

        // ========================================
        // V. 呼叫 Service 執行多條件查詢
        // ========================================

        Page<PurchaseOrderResponse> responses =
                purchaseOrderService.findPurchaseOrdersByConditions(
                        supplierId,
                        orderStatus,
                        startDate,
                        endDate,
                        pageable
                )
                .map(this::convertToResponse);

        // 回傳分頁查詢結果
        return ResponseEntity.ok(responses);
    }


    // ========================================
    // 2. 查詢單筆訂貨單
    // ========================================

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOrderResponse> findPurchaseOrderById(
            @PathVariable Long id) {

        PurchaseOrder order =
                purchaseOrderService.findPurchaseOrderById(id);

        return ResponseEntity.ok(convertToResponse(order));
    }

    // ========================================
    // 3. 依訂貨狀態查詢
    // ========================================

    @GetMapping("/status/{status}")
    public ResponseEntity<List<PurchaseOrderResponse>> findByStatus(
            @PathVariable String status) {

        List<PurchaseOrderResponse> responses =
                purchaseOrderService.findPurchaseOrdersByStatus(status)
                        .stream()
                        .map(this::convertToResponse)
                        .toList();

        return ResponseEntity.ok(responses);
    }

    // ========================================
    // 4. 依供應商查詢
    // ========================================

    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<List<PurchaseOrderResponse>> findBySupplier(
            @PathVariable Long supplierId) {

        List<PurchaseOrderResponse> responses =
                purchaseOrderService.findPurchaseOrdersBySupplier(supplierId)
                        .stream()
                        .map(this::convertToResponse)
                        .toList();

        return ResponseEntity.ok(responses);
    }

    // ========================================
    // 5. 依日期區間查詢
    // ========================================

    @GetMapping("/date-range")
    public ResponseEntity<List<PurchaseOrderResponse>> findByDateRange(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {

        List<PurchaseOrderResponse> responses =
                purchaseOrderService.findPurchaseOrdersByDateRange(
                        startDate,
                        endDate
                )
                .stream()
                .map(this::convertToResponse)
                .toList();

        return ResponseEntity.ok(responses);
    }

    // ========================================
    // 6. 查詢訂貨明細
    // ========================================

    @GetMapping("/{id}/items")
    public ResponseEntity<List<PurchaseOrderItemResponse>> findItems(
            @PathVariable Long id) {

        List<PurchaseOrderItemResponse> responses =
                purchaseOrderService.findPurchaseOrderItems(id)
                        .stream()
                        .map(this::convertToItemResponse)
                        .toList();

        return ResponseEntity.ok(responses);
    }

    // ========================================
    // 7. 新增訂貨單
    // ========================================

    @PostMapping
    public ResponseEntity<PurchaseOrderResponse> createPurchaseOrder(
            @Valid @RequestBody PurchaseOrderRequest request) {

        // 將 Request DTO 轉換成訂貨單 Entity
        PurchaseOrder order = new PurchaseOrder();

        Supplier supplier = new Supplier();
        supplier.setId(request.getSupplierId());

        order.setSupplier(supplier);
        order.setExpectedDeliveryDate(
                request.getExpectedDeliveryDate());
        order.setRemark(request.getRemark());

        // 將商品明細 DTO 轉換成 Entity
        List<PurchaseOrderItem> items =
                request.getItems()
                        .stream()
                        .map(this::convertToItemEntity)
                        .toList();

        // 呼叫 Service 建立訂貨單
        PurchaseOrder savedOrder =
                purchaseOrderService.createPurchaseOrder(order, items);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(convertToResponse(savedOrder));
    }

    // ========================================
    // 8. 修改訂貨單及商品明細
    // ========================================

    @PutMapping("/{id}")
    public ResponseEntity<PurchaseOrderResponse> updatePurchaseOrder(
            @PathVariable Long id,
            @Valid @RequestBody PurchaseOrderUpdateRequest request) {

        // 呼叫 Service 修改訂貨單及商品明細
        PurchaseOrder updatedOrder =
                purchaseOrderService.updatePurchaseOrder(id, request);

        // 回傳修改後的訂貨單資料
        return ResponseEntity.ok(convertToResponse(updatedOrder));
    }

    // ========================================
    // 9. 送出訂貨單
    // ========================================

    @PatchMapping("/{id}/submit")
    public ResponseEntity<PurchaseOrderResponse> submitPurchaseOrder(
            @PathVariable Long id) {

        PurchaseOrder order =
                purchaseOrderService.submitPurchaseOrder(id);

        return ResponseEntity.ok(convertToResponse(order));
    }

    // ========================================
    // 10. 標記已到貨
    // ========================================

    @PatchMapping("/{id}/receive")
    public ResponseEntity<PurchaseOrderResponse> receivePurchaseOrder(
            @PathVariable Long id) {

        PurchaseOrder order =
                purchaseOrderService.receivePurchaseOrder(id);

        return ResponseEntity.ok(convertToResponse(order));
    }

    // ========================================
    // 11. 取消訂貨單
    // ========================================

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<PurchaseOrderResponse> cancelPurchaseOrder(
            @PathVariable Long id) {

        PurchaseOrder order =
                purchaseOrderService.cancelPurchaseOrder(id);

        return ResponseEntity.ok(convertToResponse(order));
    }

    // ========================================
    // 12. 軟刪除訂貨單
    // ========================================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePurchaseOrder(
            @PathVariable Long id) {

        purchaseOrderService.deletePurchaseOrder(id);

        return ResponseEntity.noContent().build();
    }

    // ========================================
    // 13. Request DTO 轉換 Entity
    // ========================================

    // 將商品明細 Request DTO 轉換成 Entity
    private PurchaseOrderItem convertToItemEntity(
            PurchaseOrderItemRequest request) {

        PurchaseOrderItem item = new PurchaseOrderItem();

        Product product = new Product();
        product.setId(request.getProductId());

        item.setProduct(product);
        item.setQuantity(request.getQuantity());

        return item;
    }

    // ========================================
    // 14. Entity 轉換 Response DTO
    // ========================================

    // 將訂貨單 Entity 轉換成 Response DTO
    private PurchaseOrderResponse convertToResponse(
            PurchaseOrder order) {

        PurchaseOrderResponse response = new PurchaseOrderResponse();

        response.setId(order.getId());

        // 供應商資訊
        response.setSupplierId(order.getSupplier().getId());
        response.setSupplierName(order.getSupplier().getName());

        response.setOrderDate(order.getOrderDate());
        response.setExpectedDeliveryDate(
                order.getExpectedDeliveryDate());
        response.setOrderStatus(order.getOrderStatus());
        response.setTotalAmount(order.getTotalAmount());
        response.setRemark(order.getRemark());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());

        // 查詢並轉換訂貨明細
        List<PurchaseOrderItemResponse> items =
                purchaseOrderService.findPurchaseOrderItems(order.getId())
                        .stream()
                        .map(this::convertToItemResponse)
                        .toList();

        response.setItems(items);

        return response;
    }

    // 將訂貨明細 Entity 轉換成 Response DTO
    private PurchaseOrderItemResponse convertToItemResponse(
            PurchaseOrderItem item) {

        PurchaseOrderItemResponse response =
                new PurchaseOrderItemResponse();

        response.setId(item.getId());

        response.setProductId(item.getProduct().getId());
        response.setProductName(item.getProduct().getName());

        response.setQuantity(item.getQuantity());
        response.setUnitPrice(item.getUnitPrice());
        response.setSubtotal(item.getSubtotal());

        return response;
    }
}
