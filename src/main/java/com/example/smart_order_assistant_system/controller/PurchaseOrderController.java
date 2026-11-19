package com.example.smart_order_assistant_system.controller;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;

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
    // 8. 修改訂貨單基本資料
    // ========================================

    @PutMapping("/{id}")
    public ResponseEntity<PurchaseOrderResponse> updatePurchaseOrder(
            @PathVariable Long id,
            @Valid @RequestBody PurchaseOrderUpdateRequest request) {

        // 建立修改用 Entity
        PurchaseOrder newData = new PurchaseOrder();

        // 有提供供應商 ID 時，才設定供應商資料
        if (request.getSupplierId() != null) {

            Supplier supplier = new Supplier();
            supplier.setId(request.getSupplierId());

            newData.setSupplier(supplier);
        }

        // 設定預計到貨日期
        newData.setExpectedDeliveryDate(
                request.getExpectedDeliveryDate());

        // 設定訂貨備註
        newData.setRemark(request.getRemark());

        // 呼叫 Service 更新訂貨單
        PurchaseOrder updatedOrder =
                purchaseOrderService.updatePurchaseOrder(id, newData);

        // 回傳修改後的訂貨單
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
