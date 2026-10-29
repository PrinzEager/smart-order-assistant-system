package com.example.smart_order_assistant_system.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.smart_order_assistant_system.entity.ProductCategory;
import com.example.smart_order_assistant_system.repository.ProductCategoryRepository;

@Service
@Transactional(readOnly = true)
public class ProductCategoryService {

    private final ProductCategoryRepository categoryRepository;

    // 建構子注入
    public ProductCategoryService(
            ProductCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // 查詢所有啟用且未軟刪除的分類
    public List<ProductCategory> findAllCategories() {
        return categoryRepository.findByStatusAndDeletedAtIsNull(
                (byte) 1
        );
    }

    // 依 ID 查詢分類
    public ProductCategory findCategoryById(Long id) {
        ProductCategory category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "找不到商品分類，ID：" + id
                        )
                );

        if (!isAvailable(category)) {
            throw new IllegalArgumentException(
                    "分類不存在、已停售或已刪除"
            );
        }

        return category;
    }

    // 依分類名稱搜尋
    public List<ProductCategory> searchCategoriesByName(String name) {
        if (name == null || name.isBlank()) {
            return findAllCategories();
        }

        return categoryRepository
                .findByNameContainingIgnoreCase(name.trim())
                .stream()
                .filter(this::isAvailable)
                .toList();
    }

    // 查詢指定上層分類底下的子分類
    public List<ProductCategory> findChildrenByParentId(Long parentId) {
        // 確認上層分類存在且可使用
        findCategoryById(parentId);

        return categoryRepository.findByParent_Id(parentId)
                .stream()
                .filter(this::isAvailable)
                .toList();
    }

    // 查詢所有最上層分類
    public List<ProductCategory> findRootCategories() {
        return categoryRepository.findByParentIsNull()
                .stream()
                .filter(this::isAvailable)
                .toList();
    }

    // 新增分類
    @Transactional
    public ProductCategory createCategory(ProductCategory category) {
        validateCategory(category);

        // 檢查上層分類是否有效
        if (category.getParent() != null) {
            ProductCategory parent =
                    findCategoryById(category.getParent().getId());

            category.setParent(parent);
        }

        // 檢查同一層級是否有相同名稱
        // Entity 並沒有 parentId 這個欄位或 getter
        // 因為是透過 ProductCategory parent 物件建立自我關聯
        validateDuplicateName(
        category.getName(),
        category.getParent() == null
                ? null
                : category.getParent().getId()
        );

        LocalDateTime now = LocalDateTime.now();

        category.setStatus((byte) 1);
        category.setDeletedAt(null);
        category.setCreatedAt(now);
        category.setUpdatedAt(now);

        return categoryRepository.save(category);
    }

    // 修改分類
    @Transactional
    public ProductCategory updateCategory(
            Long id, ProductCategory newData) {

        validateCategory(newData);

        ProductCategory existing = findCategoryById(id);

        // 取得新的上層分類
        ProductCategory newParent = null;

        if (newData.getParent() != null) {
            Long parentId = newData.getParent().getId();

            if (parentId == null) {
                throw new IllegalArgumentException(
                        "上層分類 ID 不可為空"
                );
            }

            if (id.equals(parentId)) {
                throw new IllegalArgumentException(
                        "分類不可設定自己為上層分類"
                );
            }

            newParent = findCategoryById(parentId);

            // 不可把自己的下層分類設成上層分類
            if (isDescendant(newParent, id)) {
                throw new IllegalArgumentException(
                        "不可將自己的下層分類設定為上層分類"
                );
            }
        }

        // 檢查同一層級是否有相同名稱
        validateDuplicateName(
                newData.getName(),
                newParent == null ? null : newParent.getId(),
                id
        );

        existing.setName(newData.getName());
        existing.setParent(newParent);
        existing.setUpdatedAt(LocalDateTime.now());

        return categoryRepository.save(existing);
    }

    // 停用分類
    @Transactional
    public ProductCategory deactivateCategory(Long id) {
        ProductCategory category = findCategoryById(id);

        category.setStatus((byte) 0);
        category.setUpdatedAt(LocalDateTime.now());

        return categoryRepository.save(category);
    }

    // 軟刪除分類
    @Transactional
    public ProductCategory deleteCategory(Long id) {
        ProductCategory category = findCategoryById(id);

        // 不允許刪除仍有啟用子分類的分類
        boolean hasActiveChildren =
                categoryRepository.findByParent_Id(id)
                        .stream()
                        .anyMatch(this::isAvailable);

        if (hasActiveChildren) {
            throw new IllegalArgumentException(
                    "此分類仍有啟用的子分類，請先處理子分類"
            );
        }

        category.setStatus((byte) 0);
        category.setDeletedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());

        return categoryRepository.save(category);
    }

    // 驗證分類名稱
    private void validateCategory(ProductCategory category) {
        if (category == null) {
            throw new IllegalArgumentException("分類資料不可為空");
        }

        if (category.getName() == null
                || category.getName().isBlank()) {
            throw new IllegalArgumentException("分類名稱不可為空");
        }

        if (category.getName().length() > 100) {
            throw new IllegalArgumentException(
                    "分類名稱不可超過 100 個字元"
            );
        }
    }

    // 檢查同一層級是否有重複分類名稱
    private void validateDuplicateName(
            String name, Long parentId) {
        validateDuplicateName(name, parentId, null);
    }

    private void validateDuplicateName(
            String name, Long parentId, Long excludeId) {

        List<ProductCategory> categories;

        if (parentId == null) {
            categories = categoryRepository
                    .findByNameAndParentIsNull(name)
                    .stream()
                    .toList();
        } else {
            categories = categoryRepository.findByParent_Id(parentId)
                    .stream()
                    .filter(c -> c.getName().equals(name))
                    .toList();
        }

        boolean duplicate = categories.stream()
                .anyMatch(c ->
                        !c.getId().equals(excludeId)
                                && c.getDeletedAt() == null
                );

        if (duplicate) {
            throw new IllegalArgumentException(
                    "同一層級已存在相同名稱的分類：" + name
            );
        }
    }

    // 判斷指定分類是否為某個分類的下層分類
    private boolean isDescendant(
            ProductCategory category, Long ancestorId) {

        ProductCategory current = category;

        while (current != null) {
            if (ancestorId.equals(current.getId())) {
                return true;
            }

            current = current.getParent();
        }

        return false;
    }

    // 判斷分類是否啟用且未軟刪除
    private boolean isAvailable(ProductCategory category) {
        return category.getStatus() != null
                && category.getStatus() == 1
                && category.getDeletedAt() == null;
    }
}