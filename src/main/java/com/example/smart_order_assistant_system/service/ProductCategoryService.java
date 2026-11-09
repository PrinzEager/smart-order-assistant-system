package com.example.smart_order_assistant_system.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.smart_order_assistant_system.entity.ProductCategory;
import com.example.smart_order_assistant_system.repository.ProductCategoryRepository;
import com.example.smart_order_assistant_system.repository.ProductRepository;
import com.example.smart_order_assistant_system.request.ProductCategoryRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductCategoryService {

    private final ProductCategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    // 分類狀態：啟用
    private static final Byte ACTIVE = 1;

    // 分類狀態：停用
    private static final Byte INACTIVE = 0;

    /**
     * 查詢所有啟用且未刪除的分類
     */
    public List<ProductCategory> findAllCategories() {

        return categoryRepository.findByStatusAndDeletedAtIsNull(ACTIVE);
    }

    /**
     * 根據 ID 查詢啟用分類
     */
    public ProductCategory findCategoryById(Long categoryId) {

        return categoryRepository
                .findByIdAndStatusAndDeletedAtIsNull(categoryId, ACTIVE)
                .orElseThrow(() ->
                        new IllegalArgumentException("商品分類不存在或已停用"));
    }

    /**
     * 查詢最上層分類
     */
    public List<ProductCategory> findRootCategories() {

        return categoryRepository
                .findByParentIsNullAndStatusAndDeletedAtIsNull(ACTIVE);
    }

    /**
     * 查詢指定分類底下的子分類
     */
    public List<ProductCategory> findChildren(Long parentId) {

        // 先確認上層分類存在且啟用
        findCategoryById(parentId);

        return categoryRepository
                .findByParent_IdAndStatusAndDeletedAtIsNull(parentId, ACTIVE);
    }

    /**
     * 搜尋分類名稱
     */
    public List<ProductCategory> searchCategories(String keyword) {

        if (keyword == null || keyword.isBlank()) {
            return findAllCategories();
        }

        return categoryRepository
                .findByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNull(
                        keyword.trim(),
                        ACTIVE
                );
    }

    /**
     * 新增商品分類
     * 新增商品分類時，null 代表最上層分類，否則為子分類
     * 新增商品分類時，若上層分類(直接輸入id的情況下)不存在，會拋出 IllegalArgumentException
     * 新增商品分類時，若同一個上層分類底下已有相同名稱的分類，會拋出 IllegalArgumentException
     */
    @Transactional
    public ProductCategory createCategory(ProductCategoryRequest request) {

        validateRequest(request);

        String name = request.getName().trim();

        // 取得上層分類
        ProductCategory parent = getValidParent(request.getParentId());

        // 檢查分類名稱是否重複
        checkDuplicateName(name, parent);

        // DTO 轉換成 Entity
        ProductCategory category = request.toEntity();

        category.setName(name);
        category.setParent(parent);
        category.setStatus(ACTIVE);
        category.setDeletedAt(null);

        LocalDateTime now = LocalDateTime.now();

        category.setCreatedAt(now);
        category.setUpdatedAt(now);

        return categoryRepository.save(category);
    }

    /**
     * 修改商品分類
     */
    @Transactional
    public ProductCategory updateCategory(
            Long categoryId,
            ProductCategoryRequest request) {

        validateRequest(request);

        // 找出原本分類
        ProductCategory category = findCategoryById(categoryId);

        String name = request.getName().trim();

        // 取得新的上層分類
        ProductCategory parent = getValidParent(request.getParentId());

        // 檢查是否會產生循環關聯
        validateHierarchy(category, parent);

        // 檢查名稱是否與其他分類重複
        checkDuplicateNameForUpdate(name, parent, categoryId);

        // 更新資料
        category.setName(name);
        category.setParent(parent);
        category.setUpdatedAt(LocalDateTime.now());

        return categoryRepository.save(category);
    }

    /**
     * 停用商品分類
     */
    @Transactional
    public void deactivateCategory(Long categoryId) {

        ProductCategory category = findCategoryById(categoryId);

        // 有子分類時，不允許直接停用
        if (!categoryRepository.findByParent_Id(categoryId).isEmpty()) {
            throw new IllegalArgumentException(
                    "此分類底下仍有子分類，請先處理子分類"
            );
        }

        category.setStatus(INACTIVE);
        category.setUpdatedAt(LocalDateTime.now());

        categoryRepository.save(category);
    }

    /**
     * 軟刪除商品分類
     */
    @Transactional
    public void deleteCategory(Long categoryId) {

        ProductCategory category = findCategoryById(categoryId);

        // 有子分類時，不允許刪除
        if (!categoryRepository.findByParent_Id(categoryId).isEmpty()) {
            throw new IllegalArgumentException(
                    "此分類底下仍有子分類，無法刪除"
            );
        }

        // 有商品使用此分類時，不允許刪除
        if (!productRepository.findByCategory_Id(categoryId).isEmpty()) {
            throw new IllegalArgumentException(
                    "此分類仍有商品使用，無法刪除"
            );
        }

        LocalDateTime now = LocalDateTime.now();

        category.setStatus(INACTIVE);
        category.setDeletedAt(now);
        category.setUpdatedAt(now);

        categoryRepository.save(category);
    }

    /**
     * 驗證 Request
     */
    private void validateRequest(ProductCategoryRequest request) {

        if (request == null) {
            throw new IllegalArgumentException("分類資料不可為空");
        }

        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("分類名稱不可為空");
        }

        if (request.getName().trim().length() > 100) {
            throw new IllegalArgumentException("分類名稱不可超過100個字");
        }
    }

    /**
     * 取得有效的上層分類
     */
    private ProductCategory getValidParent(Long parentId) {

        // null 代表最上層分類
        if (parentId == null) {
            return null;
        }

        return findCategoryById(parentId);
    }

    /**
     * 新增時檢查分類名稱是否重複
     */
    private void checkDuplicateName(
            String name,
            ProductCategory parent) {

        boolean exists;

        if (parent == null) {

            exists = categoryRepository
                    .existsByNameAndParentIsNullAndStatusAndDeletedAtIsNull(
                            name,
                            ACTIVE
                    );

        } else {

            exists = categoryRepository
                    .existsByNameAndParent_IdAndStatusAndDeletedAtIsNull(
                            name,
                            parent.getId(),
                            ACTIVE
                    );
        }

        if (exists) {
            throw new IllegalArgumentException("相同上層分類底下已有相同名稱");
        }
    }

    /**
     * 修改時檢查分類名稱是否重複
     */
    private void checkDuplicateNameForUpdate(
            String name,
            ProductCategory parent,
            Long categoryId) {

        boolean exists;

        if (parent == null) {

            exists = categoryRepository
                    .existsByNameAndParentIsNullAndStatusAndDeletedAtIsNullAndIdNot(
                            name,
                            ACTIVE,
                            categoryId
                    );

        } else {

            exists = categoryRepository
                    .existsByNameAndParent_IdAndStatusAndDeletedAtIsNullAndIdNot(
                            name,
                            parent.getId(),
                            ACTIVE,
                            categoryId
                    );
        }

        if (exists) {
            throw new IllegalArgumentException("相同上層分類底下已有相同名稱");
        }
    }

    /**
     * 檢查是否會形成循環分類
     */
    private void validateHierarchy(
            ProductCategory category,
            ProductCategory newParent) {

        ProductCategory current = newParent;

        while (current != null) {

            // 不允許自己成為自己的上層分類
            if (current.getId().equals(category.getId())) {
                throw new IllegalArgumentException(
                        "不可將自己或自己的下層分類設為上層分類"
                );
            }

            // 持續往上尋找
            current = current.getParent();
        }
    }
}