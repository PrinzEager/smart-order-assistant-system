
package com.example.smart_order_assistant_system.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.smart_order_assistant_system.entity.Product;
import com.example.smart_order_assistant_system.entity.Sale;
import com.example.smart_order_assistant_system.request.SaleRequest;
import com.example.smart_order_assistant_system.response.PageResponse;
import com.example.smart_order_assistant_system.response.SaleResponse;
import com.example.smart_order_assistant_system.service.SaleService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/sales")
@Transactional(readOnly = true)
public class SaleController {

    private final SaleService saleService;

    // 建構子注入 Service
    public SaleController(SaleService saleService) {
        this.saleService = saleService;
    }

    // ========================================
    // 1. 查詢所有銷售紀錄（分頁及條件查詢）
    // ========================================

    @GetMapping
    public ResponseEntity<PageResponse<SaleResponse>> getAllSales(

            @RequestParam(defaultValue = "1") int page,

            @RequestParam(defaultValue = "10") int size,

            @RequestParam(required = false) Long productId,

            @RequestParam(required = false)
            LocalDateTime startDateTime,

            @RequestParam(required = false)
            LocalDateTime endDateTime) {

        // 分頁參數驗證
        if (page < 1) {
            page = 1;
        }

        if (size < 1) {
            size = 10;
        }

        if (size > 50) {
            size = 50;
        }

        // 建立分頁條件，ID 降冪排序
        Pageable pageable = PageRequest.of(
                page - 1,
                size,
                Sort.by(Sort.Direction.DESC, "id")
        );

        // 日期區間必須同時提供
        if ((startDateTime == null && endDateTime != null)
                || (startDateTime != null && endDateTime == null)) {
            throw new IllegalArgumentException(
                    "開始時間與結束時間必須同時提供");
        }

        // 依照查詢條件取得資料
        Page<Sale> salePage;

        if (productId != null
                && startDateTime != null) {

            salePage = saleService.findSalesByProductAndDateRange(
                    productId,
                    startDateTime,
                    endDateTime,
                    pageable);

        } else if (productId != null) {

            salePage = saleService.findSalesByProduct(
                    productId,
                    pageable);

        } else if (startDateTime != null) {

            salePage = saleService.findSalesByDateRange(
                    startDateTime,
                    endDateTime,
                    pageable);

        } else {

            salePage = saleService.findAllSales(pageable);
        }

        // Entity 轉換 Response DTO
        List<SaleResponse> responses = salePage.getContent()
                .stream()
                .map(SaleResponse::fromEntity)
                .toList();

        // 組合分頁回傳資料
        PageResponse<SaleResponse> response =
                new PageResponse<>(
                        responses,
                        page,
                        size,
                        salePage.getTotalElements()
                );

        return ResponseEntity.ok(response);
    }

    // ========================================
    // 2. 查詢單筆銷售紀錄
    // ========================================

    @GetMapping("/{id}")
    public ResponseEntity<SaleResponse> getSaleById(
            @PathVariable Long id) {

        Sale sale = saleService.findSaleById(id);

        return ResponseEntity.ok(
                SaleResponse.fromEntity(sale));
    }

    // ========================================
    // 3. 新增銷售紀錄
    // ========================================

    @PostMapping
    @Transactional
    public ResponseEntity<SaleResponse> createSale(
            @Valid @RequestBody SaleRequest request) {

        Sale sale = convertToEntity(request);

        Sale createdSale = saleService.createSale(sale);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(SaleResponse.fromEntity(createdSale));
    }

    // ========================================
    // 4. 修改銷售紀錄
    // ========================================

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<SaleResponse> updateSale(
            @PathVariable Long id,
            @Valid @RequestBody SaleRequest request) {

        Sale sale = convertToEntity(request);

        Sale updatedSale = saleService.updateSale(id, sale);

        return ResponseEntity.ok(
                SaleResponse.fromEntity(updatedSale));
    }

    // ========================================
    // 5. 軟刪除銷售紀錄
    // ========================================

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deleteSale(
            @PathVariable Long id) {

        saleService.deleteSale(id);

        return ResponseEntity.noContent().build();
    }

    // ========================================
    // 6. Request DTO 轉換 Entity
    // ========================================

    private Sale convertToEntity(SaleRequest request) {

        Sale sale = new Sale();

        // 建立商品關聯，只設定商品 ID
        Product product = new Product();
        product.setId(request.getProductId());

        sale.setProduct(product);

        sale.setQuantity(request.getQuantity());

        sale.setUnitPrice(request.getUnitPrice());

        sale.setDateTime(request.getDateTime());

        return sale;
    }
}
