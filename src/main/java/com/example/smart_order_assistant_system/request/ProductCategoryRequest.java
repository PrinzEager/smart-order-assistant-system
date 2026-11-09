package com.example.smart_order_assistant_system.request;

import com.example.smart_order_assistant_system.entity.ProductCategory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductCategoryRequest {

    // 分類名稱
    @NotBlank(message = "商品分類名稱不可為空")
    @Size(max = 100, message = "商品分類名稱不可超過100個字")
    private String name;

    // 上層分類 ID
    // 如果是最上層分類，parentId 可以為 null
    private Long parentId;

    /**
     * 將 Request DTO 轉換成 Entity
     *
     * @return ProductCategory Entity
     */
    public ProductCategory toEntity() {

        ProductCategory category = new ProductCategory();

        category.setName(this.name);

        return category;
    }
}
