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

    @Test
    void testUpdateCategory_WhenNameChanged_ShouldSuccess() {
        
        // Arrange：準備測試資料

        // 建立原本的第一層分類：飲料
        ProductCategory category = new ProductCategory();
        category.setId(1L);
        category.setName("飲料");
        category.setParent(null);
        category.setStatus((byte) 1);

        // 模擬查詢原本分類成功
        when(categoryRepository.findByIdAndStatusAndDeletedAtIsNull(
                1L, (byte) 1
        )).thenReturn(java.util.Optional.of(category));

        // 模擬修改後的分類名稱沒有重複
        when(categoryRepository
                .existsByNameAndParentIsNullAndStatusAndDeletedAtIsNullAndIdNot(
                        "飲品", (byte) 1, 1L
                )).thenReturn(false);

        // 模擬 Repository 儲存資料時，回傳傳入的 Entity
        when(categoryRepository.save(any(ProductCategory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // 建立修改分類的 Request
        ProductCategoryRequest request = new ProductCategoryRequest();
        request.setName("飲品");
        request.setParentId(null);


        // Act：執行修改分類
        ProductCategory result = categoryService.updateCategory(1L, request);


        // Assert：驗證修改結果

        // 確認回傳結果不為 null
        assertNotNull(result);

        // 確認分類名稱已經修改
        assertEquals("飲品", result.getName());

        // 確認仍然是第一層分類
        assertNull(result.getParent());

        // 使用 ArgumentCaptor 捕捉傳入 save() 的 Entity
        ArgumentCaptor<ProductCategory> captor =
                ArgumentCaptor.forClass(ProductCategory.class);

        verify(categoryRepository).save(captor.capture());

        // 取得實際準備儲存的 Entity
        ProductCategory updatedCategory = captor.getValue();

        // 驗證儲存的分類名稱
        assertEquals("飲品", updatedCategory.getName());

        // 驗證分類 ID 沒有改變
        assertEquals(1L, updatedCategory.getId());

        // 驗證更新時間有設定
        assertNotNull(updatedCategory.getUpdatedAt());
    }

    @Test
    void testUpdateCategory_WhenDuplicateName_ShouldThrowException() {

        // Arrange：準備測試資料

        // 建立第一個分類：飲料
        ProductCategory category = new ProductCategory();
        category.setId(1L);
        category.setName("飲料");
        category.setParent(null);
        category.setStatus((byte) 1);

        // 模擬查詢原本分類成功
        when(categoryRepository.findByIdAndStatusAndDeletedAtIsNull(
                1L, (byte) 1
        )).thenReturn(java.util.Optional.of(category));

        // 模擬資料庫已經存在相同名稱的分類
        when(categoryRepository
                .existsByNameAndParentIsNullAndStatusAndDeletedAtIsNullAndIdNot(
                        "食品", (byte) 1, 1L
                )).thenReturn(true);

        // 建立修改分類的 Request
        ProductCategoryRequest request = new ProductCategoryRequest();
        request.setName("食品");
        request.setParentId(null);


        // Act & Assert：驗證是否拋出例外

        assertThrows(
                IllegalArgumentException.class,
                () -> categoryService.updateCategory(1L, request)
        );

        // 確認發生錯誤時沒有執行儲存
        verify(categoryRepository, never()).save(
                any(ProductCategory.class)
        );
    }

    @Test
    void testUpdateCategory_WhenParentChanged_ShouldSuccess() {

        // Arrange：準備測試資料

        // 建立第一層分類：飲料
        ProductCategory oldParent = new ProductCategory();
        oldParent.setId(1L);
        oldParent.setName("飲料");
        oldParent.setParent(null);
        oldParent.setStatus((byte) 1);

        // 建立第一層分類：食品
        ProductCategory newParent = new ProductCategory();
        newParent.setId(2L);
        newParent.setName("食品");
        newParent.setParent(null);
        newParent.setStatus((byte) 1);

        // 建立第二層分類：茶類
        ProductCategory category = new ProductCategory();
        category.setId(3L);
        category.setName("茶類");
        category.setParent(oldParent);
        category.setStatus((byte) 1);

        // 模擬查詢原本的分類：茶類
        when(categoryRepository.findByIdAndStatusAndDeletedAtIsNull(
                3L, (byte) 1
        )).thenReturn(java.util.Optional.of(category));

        // 模擬查詢新的父分類：食品
        when(categoryRepository.findByIdAndStatusAndDeletedAtIsNull(
                2L, (byte) 1
        )).thenReturn(java.util.Optional.of(newParent));

        // 模擬新父分類底下沒有相同名稱
        when(categoryRepository
                .existsByNameAndParent_IdAndStatusAndDeletedAtIsNullAndIdNot(
                        "茶類", 2L, (byte) 1, 3L
                )).thenReturn(false);

        // 模擬 Repository 儲存資料時，回傳傳入的 Entity
        when(categoryRepository.save(any(ProductCategory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // 建立修改分類的 Request
        ProductCategoryRequest request = new ProductCategoryRequest();
        request.setName("茶類");
        request.setParentId(2L);


        // Act：執行修改分類
        ProductCategory result = categoryService.updateCategory(3L, request);


        // Assert：驗證修改結果

        // 確認回傳結果不為 null
        assertNotNull(result);

        // 確認分類名稱沒有改變
        assertEquals("茶類", result.getName());

        // 確認父分類已經修改成食品
        assertEquals(2L, result.getParent().getId());

        // 使用 ArgumentCaptor 捕捉傳入 save() 的 Entity
        ArgumentCaptor<ProductCategory> captor =
                ArgumentCaptor.forClass(ProductCategory.class);

        verify(categoryRepository).save(captor.capture());

        // 取得實際準備儲存的 Entity
        ProductCategory updatedCategory = captor.getValue();

        // 驗證儲存的分類 ID
        assertEquals(3L, updatedCategory.getId());

        // 驗證儲存的父分類 ID
        assertEquals(2L, updatedCategory.getParent().getId());

        // 驗證分類名稱
        assertEquals("茶類", updatedCategory.getName());

        // 驗證更新時間
        assertNotNull(updatedCategory.getUpdatedAt());
    }

    @Test
    void testUpdateCategory_WhenParentIsSecondLevel_ShouldThrowException() {

        // Arrange：準備測試資料

        // 建立第一層分類：飲料
        ProductCategory rootCategory = new ProductCategory();
        rootCategory.setId(1L);
        rootCategory.setName("飲料");
        rootCategory.setParent(null);
        rootCategory.setStatus((byte) 1);

        // 建立第二層分類：茶類
        ProductCategory secondLevelCategory = new ProductCategory();
        secondLevelCategory.setId(2L);
        secondLevelCategory.setName("茶類");
        secondLevelCategory.setParent(rootCategory);
        secondLevelCategory.setStatus((byte) 1);

        // 模擬查詢原本分類：飲料
        when(categoryRepository.findByIdAndStatusAndDeletedAtIsNull(
                1L, (byte) 1
        )).thenReturn(java.util.Optional.of(rootCategory));

        // 模擬查詢新的父分類：茶類
        when(categoryRepository.findByIdAndStatusAndDeletedAtIsNull(
                2L, (byte) 1
        )).thenReturn(java.util.Optional.of(secondLevelCategory));

        // 建立修改分類的 Request
        ProductCategoryRequest request = new ProductCategoryRequest();
        request.setName("飲料");
        request.setParentId(2L);


        // Act & Assert：驗證是否拋出例外

        assertThrows(
                IllegalArgumentException.class,
                () -> categoryService.updateCategory(1L, request)
        );

        // 確認沒有執行儲存
        verify(categoryRepository, never()).save(
                any(ProductCategory.class)
        );
    }

    @Test
    void testUpdateCategory_WhenCircularReference_ShouldThrowException() {

        // Arrange：準備測試資料

        // 建立第一層分類：飲料
        ProductCategory category = new ProductCategory();
        category.setId(1L);
        category.setName("飲料");
        category.setParent(null);
        category.setStatus((byte) 1);

        // 模擬查詢原本分類成功
        when(categoryRepository.findByIdAndStatusAndDeletedAtIsNull(
                1L, (byte) 1
        )).thenReturn(java.util.Optional.of(category));

        // 建立修改分類的 Request
        // 將自己設定成自己的父分類
        ProductCategoryRequest request = new ProductCategoryRequest();
        request.setName("飲料");
        request.setParentId(1L);


        // Act & Assert：驗證是否拋出循環分類例外

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> categoryService.updateCategory(1L, request)
        );

        // 確認是循環分類驗證所拋出的訊息
        assertEquals(
                "不可將自己或自己的下層分類設為上層分類",
                exception.getMessage()
        );

        // 確認沒有執行儲存
        verify(categoryRepository, never()).save(
                any(ProductCategory.class)
        );
    }
}