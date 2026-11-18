
package com.example.smart_order_assistant_system.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.example.smart_order_assistant_system.entity.Supplier;
import com.example.smart_order_assistant_system.request.SupplierRequest;
import com.example.smart_order_assistant_system.response.PageResponse;
import com.example.smart_order_assistant_system.response.SupplierResponse;
import com.example.smart_order_assistant_system.service.SupplierService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    // ==========================================
    // 1. 取得所有供應商（支援分頁與關鍵字搜尋）
    // ==========================================

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<SupplierResponse> getAll(

            @RequestParam(defaultValue = "1") int page,

            @RequestParam(defaultValue = "10") int size,

            @RequestParam(required = false) String keyword) {

        // 預設頁碼從 1 開始
        if (page < 1) {
            page = 1;
        }

        // 每頁最少數量不能為 0
        if (size < 1) {
            size = 10;
        }

        // 每頁最多 50 筆
        if (size > 50) {
            size = 50;
        }

        // Spring Data JPA 頁碼從 0 開始
        Pageable pageable = PageRequest.of(
                page - 1,
                size,
                Sort.by(Sort.Direction.DESC, "id")
        );

        Page<Supplier> suppliers;

        // 有輸入關鍵字就搜尋供應商名稱
        if (keyword != null && !keyword.isBlank()) {

            suppliers = supplierService.searchSuppliers(
                    keyword,
                    pageable
            );

        } else {

            suppliers = supplierService.findAllSuppliers(pageable);
        }

        // Entity 轉換成 Response
        List<SupplierResponse> responses =
                suppliers.getContent()
                        .stream()
                        .map(SupplierResponse::new)
                        .toList();

        return new PageResponse<>(
                responses,
                page,
                size,
                suppliers.getTotalElements()
        );
    }

    // ==========================================
    // 2. 根據供應商 ID 查詢
    // ==========================================

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public SupplierResponse getById(@PathVariable Long id) {

        Supplier supplier = supplierService.findSupplierById(id);

        return new SupplierResponse(supplier);
    }

    // ==========================================
    // 3. 新增供應商
    // ==========================================

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SupplierResponse create(
            @Valid @RequestBody SupplierRequest request) {

        Supplier supplier = request.toEntity();

        Supplier savedSupplier =
                supplierService.createSupplier(supplier);

        return new SupplierResponse(savedSupplier);
    }

    // ==========================================
    // 4. 修改供應商
    // ==========================================

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public SupplierResponse update(
            @PathVariable Long id,
            @Valid @RequestBody SupplierRequest request) {

        Supplier supplier = request.toEntity();

        Supplier updatedSupplier =
                supplierService.updateSupplier(id, supplier);

        return new SupplierResponse(updatedSupplier);
    }

    // ==========================================
    // 5. 停用供應商
    // ==========================================

    @PatchMapping("/{id}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable Long id) {

        supplierService.deactivateSupplier(id);
    }

    // ==========================================
    // 6. 啟用供應商
    // ==========================================

    @PatchMapping("/{id}/activate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void activate(@PathVariable Long id) {

        supplierService.activateSupplier(id);
    }

    // ==========================================
    // 7. 軟刪除供應商
    // ==========================================

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {

        supplierService.deleteSupplier(id);
    }
}
