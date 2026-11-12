package com.example.smart_order_assistant_system.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;

import com.example.smart_order_assistant_system.entity.ProductCategory;
import com.example.smart_order_assistant_system.repository.ProductCategoryRepository;
import com.example.smart_order_assistant_system.repository.ProductRepository;
import com.example.smart_order_assistant_system.request.ProductCategoryRequest;

@ExtendWith(MockitoExtension.class)
class ProductCategoryServiceTest {

    @Mock
    private ProductCategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductCategoryService categoryService;

    @Test
    void testCreateCategory_WhenParentIsSecondLevel_ShouldThrowException() {

        // Arrange：準備測試資料

        // 第一層分類：飲料
        ProductCategory rootCategory = new ProductCategory();
        rootCategory.setId(1L);
        rootCategory.setName("飲料");
        rootCategory.setParent(null);
        rootCategory.setStatus((byte) 1);

        // 第二層分類：茶類
        ProductCategory secondLevelCategory = new ProductCategory();
        secondLevelCategory.setId(2L);
        secondLevelCategory.setName("茶類");
        secondLevelCategory.setParent(rootCategory);
        secondLevelCategory.setStatus((byte) 1);

        // 模擬 Repository 查詢到「茶類」
        when(categoryRepository.findByIdAndStatusAndDeletedAtIsNull(
                2L, (byte) 1
        )).thenReturn(java.util.Optional.of(secondLevelCategory));

        // 建立新增分類的 Request
        ProductCategoryRequest request = new ProductCategoryRequest();
        request.setName("紅茶");
        request.setParentId(2L);

        // Act & Assert：執行並驗證結果

        assertThrows(
            IllegalArgumentException.class,
            () -> categoryService.createCategory(request)
        );

        // 確認沒有執行新增分類
        verify(categoryRepository, never()).save(
            org.mockito.ArgumentMatchers.any(ProductCategory.class)
        );
    }

    @Test
    void testCreateCategory_WhenParentIsFirstLevel_ShouldCreateSuccessfully() {

        // Arrange：準備測試資料

        // 建立第一層分類：飲料
        ProductCategory rootCategory = new ProductCategory();
        rootCategory.setId(1L);
        rootCategory.setName("飲料");
        rootCategory.setParent(null);
        rootCategory.setStatus((byte) 1);

        // 模擬查詢第一層分類成功
        when(categoryRepository.findByIdAndStatusAndDeletedAtIsNull(
                1L, (byte) 1
        )).thenReturn(java.util.Optional.of(rootCategory));

        // 模擬分類名稱沒有重複
        when(categoryRepository
                .existsByNameAndParent_IdAndStatusAndDeletedAtIsNull(
                        "茶類", 1L, (byte) 1
                )).thenReturn(false);

        // 模擬 Repository 儲存資料時，回傳傳入的 Entity
        when(categoryRepository.save(any(ProductCategory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // 建立新增分類的 Request
        ProductCategoryRequest request = new ProductCategoryRequest();
        request.setName("茶類");
        request.setParentId(1L);


        // Act：執行新增分類
        ProductCategory result = categoryService.createCategory(request);


        // Assert：驗證新增結果

        // 確認回傳的 Entity 不為 null
        assertNotNull(result);

        // 使用 ArgumentCaptor 捕捉傳入 save() 的 Entity
        ArgumentCaptor<ProductCategory> captor =
                ArgumentCaptor.forClass(ProductCategory.class);

        verify(categoryRepository).save(captor.capture());

        // 取得實際準備儲存的 Entity
        ProductCategory savedCategory = captor.getValue();

        // 驗證分類名稱
        assertEquals("茶類", savedCategory.getName());

        // 驗證父分類
        assertEquals(rootCategory, savedCategory.getParent());

        // 驗證狀態為啟用
        assertEquals((byte) 1, savedCategory.getStatus());

        // 驗證沒有被軟刪除
        assertNull(savedCategory.getDeletedAt());

        // 驗證建立時間與更新時間
        assertNotNull(savedCategory.getCreatedAt());
        assertNotNull(savedCategory.getUpdatedAt());
    }

    @Test
    void testCreateCategory_WhenCategoryNameAlreadyExists_ShouldThrowException() {

        // Arrange：準備測試資料

        // 建立第一層分類：飲料
        ProductCategory rootCategory = new ProductCategory();
        rootCategory.setId(1L);
        rootCategory.setName("飲料");
        rootCategory.setParent(null);
        rootCategory.setStatus((byte) 1);

        // 模擬查詢第一層分類成功
        when(categoryRepository.findByIdAndStatusAndDeletedAtIsNull(
                1L, (byte) 1
        )).thenReturn(java.util.Optional.of(rootCategory));

        // 模擬分類名稱已經存在
        when(categoryRepository
                .existsByNameAndParent_IdAndStatusAndDeletedAtIsNull(
                        "茶類", 1L, (byte) 1
                )).thenReturn(true);

        // 建立新增分類的 Request
        ProductCategoryRequest request = new ProductCategoryRequest();
        request.setName("茶類");
        request.setParentId(1L);


        // Act & Assert：驗證是否拋出例外

        assertThrows(
            IllegalArgumentException.class,
            () -> categoryService.createCategory(request)
        );

        // 確認沒有執行新增
        verify(categoryRepository, never()).save(
                any(ProductCategory.class)
        );
    }
}