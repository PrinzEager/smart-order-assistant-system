package com.example.smart_order_assistant_system.controller;

import com.example.smart_order_assistant_system.entity.Product;
import com.example.smart_order_assistant_system.service.ProductImageService;
import com.example.smart_order_assistant_system.service.ProductService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductImageController {

    private final ProductImageService productImageService;
    private final ProductService productService;

    /**
     * 上傳商品圖片
     *
     * POST /api/products/{productId}/image
     */
    @PostMapping("/{productId}/image")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, String> uploadProductImage(
            @PathVariable Long productId,
            @RequestParam("file") MultipartFile file) {

        // 1. 將圖片儲存到伺服器
        String imageUrl =
                productImageService.uploadProductImage(file);

        // 2. 將圖片網址更新到商品資料
        Product product =
                productService.updateProductImageUrl(productId, imageUrl);

        // 3. 回傳結果
        return Map.of(
                "message", "商品圖片上傳成功",
                "productId", product.getId().toString(),
                "imageUrl", product.getImageUrl()
        );
    }
}
