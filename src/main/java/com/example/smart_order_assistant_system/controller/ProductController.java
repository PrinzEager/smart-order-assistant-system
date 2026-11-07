package com.example.smart_order_assistant_system.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.example.smart_order_assistant_system.response.PageResponse;
import com.example.smart_order_assistant_system.response.ProductResponse;
import com.example.smart_order_assistant_system.entity.Product;
import com.example.smart_order_assistant_system.request.ProductRequest;
import com.example.smart_order_assistant_system.service.ProductService;

import jakarta.validation.Valid;

// import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;


    // ==========================================
    // 1. 取得所有商品（支援分頁與關鍵字搜尋）
    // ==========================================

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<ProductResponse> getAll(

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

        // 每頁最大不能超過 50 筆
        if (size > 50) {
            size = 50;
        }

        // Spring Data JPA 的頁碼從 0 開始
        // 所以要將使用者傳入的 page - 1
        Pageable pageable = PageRequest.of(
                page - 1,
                size,
                Sort.by(Sort.Direction.DESC, "id")
        );

        Page<Product> products;

        // 如果有輸入關鍵字，就進行商品名稱搜尋
        if (keyword != null && !keyword.isBlank()) {

            products = productService.searchProducts(
                    keyword,
                    pageable
            );

        } else {

            // 沒有關鍵字，就取得所有正常商品
            products = productService.findAllProducts(pageable);
        }

        // 將 Product Entity 轉換成 ProductResponse
        List<ProductResponse> productResponses =
                products.getContent()
                        .stream()
                        .map(ProductResponse::new)
                        .toList();

        // 取得總資料筆數
        long totalElements = products.getTotalElements();

        // 回傳分頁結果
        return new PageResponse<>(
                productResponses,
                page,
                size,
                totalElements
        );
    }


    // ==========================================
    // 2. 根據商品 ID 查詢商品
    // ==========================================

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ProductResponse getById(@PathVariable Long id) {

        Product product = productService.findProductById(id);

        return new ProductResponse(product);
    }


    // ==========================================
    // 3. 新增商品
    // ==========================================

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(
        @Valid @RequestBody ProductRequest request) {

    Product product = request.toEntity();

    Product savedProduct = productService.createProduct(product);

    return new ProductResponse(savedProduct);
}


    // ==========================================
    // 4. 修改商品
    // ==========================================

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ProductResponse update(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {

        Product product = request.toEntity();

        Product updatedProduct = productService.updateProduct(id, product);

        return new ProductResponse(updatedProduct);
    }


    // ==========================================
    // 5. 停用商品
    // ==========================================

    @PatchMapping("/{id}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable Long id) {

        productService.deactivateProduct(id);
    }


    // ==========================================
    // 6. 刪除商品
    // ==========================================

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {

        productService.deleteProduct(id);
    }

}