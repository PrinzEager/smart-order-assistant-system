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
     * 新增商品圖片
     *
     * POST /api/products/{productId}/image
     */
    @PostMapping("/{productId}/image")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, String> uploadProductImage(
            @PathVariable Long productId,
            @RequestParam("file") MultipartFile file) {

        return saveProductImage(productId, file, "商品圖片上傳成功");
    }

    /**
     * 更新商品圖片
     *
     * PUT /api/products/{productId}/image
     */
    @PutMapping("/{productId}/image")
    public Map<String, String> updateProductImage(
            @PathVariable Long productId,
            @RequestParam("file") MultipartFile file) {

        return saveProductImage(productId, file, "商品圖片更新成功");
    }

    /**
     * 共用圖片儲存流程
     */
    private Map<String, String> saveProductImage(
            Long productId,
            MultipartFile file,
            String message) {

        // 1. 先確認商品存在，並取得原本的圖片網址
        Product oldProduct = productService.findProductById(productId);
        String oldImageUrl = oldProduct.getImageUrl();

        // 2. 儲存新圖片
        String newImageUrl = productImageService.uploadProductImage(file);

        Product updatedProduct;

        try {
            // 3. 更新資料庫中的圖片網址
            updatedProduct = productService.updateProductImageUrl(
                    productId,
                    newImageUrl
            );

        } catch (RuntimeException e) {

            // 4. 如果資料庫更新失敗，清除剛剛上傳的新圖片
            productImageService.deleteProductImage(newImageUrl);

            throw e;
        }

        // 5. 新圖片更新成功後，再刪除舊圖片
        if (oldImageUrl != null && !oldImageUrl.equals(newImageUrl)) {
            productImageService.deleteProductImage(oldImageUrl);
        }

        // 6. 回傳結果
        return Map.of(
                "message", message,
                "productId", updatedProduct.getId().toString(),
                "imageUrl", updatedProduct.getImageUrl()
        );
    }

    /**
     * 刪除商品圖片
     *
     * DELETE /api/products/{productId}/image
     */
    @DeleteMapping("/{productId}/image")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, String> deleteProductImage(
            @PathVariable Long productId) {

        // 1. 找到商品
        Product product = productService.findProductById(productId);

        // 2. 取得原本的圖片網址
        String oldImageUrl = product.getImageUrl();

        // 3. 將資料庫中的圖片網址設為 null
        productService.updateProductImageUrl(productId, null);

        // 4. 刪除實際圖片檔案
        productImageService.deleteProductImage(oldImageUrl);

        // 5. 回傳結果
        return Map.of(
                "message", "商品圖片刪除成功",
                "productId", productId.toString()
        );
    }
}
