package com.example.smart_order_assistant_system.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.example.smart_order_assistant_system.entity.ProductCategory;
import com.example.smart_order_assistant_system.request.ProductCategoryRequest;
import com.example.smart_order_assistant_system.response.ProductCategoryResponse;
import com.example.smart_order_assistant_system.service.ProductCategoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/product-categories")
@RequiredArgsConstructor
public class ProductCategoryController {

    private final ProductCategoryService categoryService;

    // ==========================================
    // 1. 查詢所有啟用分類
    // GET /api/product-categories
    // ==========================================

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ProductCategoryResponse> getAllCategories() {

        return categoryService.findAllCategories()
                .stream()
                .map(ProductCategoryResponse::new)
                .toList();
    }

    // ==========================================
    // 2. 根據 ID 查詢分類
    // GET /api/product-categories/{id}
    // ==========================================

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ProductCategoryResponse getCategoryById(
            @PathVariable Long id) {

        ProductCategory category =
                categoryService.findCategoryById(id);

        return new ProductCategoryResponse(category);
    }

    // ==========================================
    // 3. 查詢最上層分類
    // GET /api/product-categories/roots
    // ==========================================

    @GetMapping("/roots")
    @ResponseStatus(HttpStatus.OK)
    public List<ProductCategoryResponse> getRootCategories() {

        return categoryService.findRootCategories()
                .stream()
                .map(ProductCategoryResponse::new)
                .toList();
    }

    // ==========================================
    // 4. 查詢指定分類的子分類
    // GET /api/product-categories/{id}/children
    // ==========================================

    @GetMapping("/{id}/children")
    @ResponseStatus(HttpStatus.OK)
    public List<ProductCategoryResponse> getChildren(
            @PathVariable Long id) {

        return categoryService.findChildren(id)
                .stream()
                .map(ProductCategoryResponse::new)
                .toList();
    }

    // ==========================================
    // 5. 搜尋分類
    // GET /api/product-categories/search?keyword=飲料
    // ==========================================

    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public List<ProductCategoryResponse> searchCategories(
            @RequestParam(required = false) String keyword) {

        return categoryService.searchCategories(keyword)
                .stream()
                .map(ProductCategoryResponse::new)
                .toList();
    }

    // ==========================================
    // 6. 新增分類
    // POST /api/product-categories
    // ==========================================

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductCategoryResponse createCategory(
            @Valid @RequestBody ProductCategoryRequest request) {

        ProductCategory category =
                categoryService.createCategory(request);

        return new ProductCategoryResponse(category);
    }

    // ==========================================
    // 7. 修改分類
    // PUT /api/product-categories/{id}
    // ==========================================

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ProductCategoryResponse updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody ProductCategoryRequest request) {

        ProductCategory category =
                categoryService.updateCategory(id, request);

        return new ProductCategoryResponse(category);
    }

    // ==========================================
    // 8. 停用分類
    // PATCH /api/product-categories/{id}/deactivate
    // ==========================================

    @PatchMapping("/{id}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateCategory(@PathVariable Long id) {

        categoryService.deactivateCategory(id);
    }

    // ==========================================
    // 9. 軟刪除分類
    // DELETE /api/product-categories/{id}
    // ==========================================

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long id) {

        categoryService.deleteCategory(id);
    }
}