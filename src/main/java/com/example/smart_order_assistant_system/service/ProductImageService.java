package com.example.smart_order_assistant_system.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

@Service
public class ProductImageService {

    // 圖片最大容量：5 MB
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    // 允許上傳的圖片格式
    private static final Map<String, String> ALLOWED_TYPES = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp"
    );

    // 從 application.properties 讀取圖片儲存位置
    @Value("${app.upload.product-image-dir:uploads/products}")
    private String uploadDir;

    /**
     * 上傳商品圖片
     *
     * @param file 使用者上傳的圖片
     * @return 圖片網址
     */
    public String uploadProductImage(MultipartFile file) {

        // 1. 檢查是否有上傳檔案
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("請選擇要上傳的圖片");
        }

        // 2. 檢查圖片大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("圖片大小不可超過 5MB");
        }

        // 3. 取得圖片 MIME Type
        String contentType = file.getContentType();

        // 4. 檢查圖片格式
        if (contentType == null || !ALLOWED_TYPES.containsKey(contentType)) {
            throw new IllegalArgumentException(
                    "只允許上傳 JPG、PNG、WEBP 格式的圖片"
            );
        }

        // 5. 產生唯一檔名
        String extension = ALLOWED_TYPES.get(contentType);
        String fileName = UUID.randomUUID() + extension;

        // 6. 建立圖片儲存路徑
        Path uploadPath = Path.of(uploadDir)
                .toAbsolutePath()
                .normalize();

        Path targetPath = uploadPath.resolve(fileName).normalize();

        // 確認目標路徑仍在指定資料夾內
        if (!targetPath.startsWith(uploadPath)) {
            throw new IllegalArgumentException("不合法的圖片儲存路徑");
        }

        try {
            // 7. 如果資料夾不存在，就自動建立
            Files.createDirectories(uploadPath);

            // 8. 將圖片寫入伺服器資料夾
            Files.copy(file.getInputStream(), targetPath);

        } catch (IOException e) {
            throw new IllegalStateException("圖片儲存失敗", e);
        }

        // 9. 回傳圖片網址
        return "/uploads/products/" + fileName;
    }

    /**
     * 刪除商品圖片
     *
     * @param imageUrl 資料庫中儲存的圖片網址
     */
    public void deleteProductImage(String imageUrl) {

        // 1. 沒有圖片網址，就不需要刪除
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        // 2. 確認圖片網址是系統管理的商品圖片
        String prefix = "/uploads/products/";

        if (!imageUrl.startsWith(prefix)) {
            throw new IllegalArgumentException("不合法的商品圖片路徑");
        }

        // 3. 取得圖片檔名
        String fileName = imageUrl.substring(prefix.length());

        // 避免有人透過路徑跳脫指定資料夾
        if (fileName.isBlank()
                || !Path.of(fileName).getFileName().toString().equals(fileName)) {
            throw new IllegalArgumentException("不合法的商品圖片檔名");
        }

        // 4. 組合實際圖片路徑
        Path uploadPath = Path.of(uploadDir)
                .toAbsolutePath()
                .normalize();

        Path imagePath = uploadPath.resolve(fileName).normalize();

        if (!imagePath.startsWith(uploadPath)) {
            throw new IllegalArgumentException("不合法的圖片儲存路徑");
        }

        // 5. 刪除圖片
        try {
            Files.deleteIfExists(imagePath);
        } catch (IOException e) {
            throw new IllegalStateException("圖片刪除失敗", e);
        }
    }
}