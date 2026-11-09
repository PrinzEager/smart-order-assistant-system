package com.example.smart_order_assistant_system.response;

import java.time.LocalDateTime;

import com.example.smart_order_assistant_system.entity.ProductCategory;

import lombok.Getter;

@Getter
public class ProductCategoryResponse {

    private Long id;

    private String name;

    private Long parentId;

    private Byte status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public ProductCategoryResponse(ProductCategory category) {

        this.id = category.getId();

        this.name = category.getName();

        this.parentId = category.getParent() == null
                ? null
                : category.getParent().getId();

        this.status = category.getStatus();

        this.createdAt = category.getCreatedAt();

        this.updatedAt = category.getUpdatedAt();
    }
}