package com.example.smart_order_assistant_system.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "product_categories")
public class ProductCategory {

    // 商品分類編號（主鍵）
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    // 分類名稱
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    // 上層分類編號：支援多層分類
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "parent_id",
        foreignKey = @ForeignKey(name = "fk_categories_parent")
    )
    private ProductCategory parent;

    // 下層分類：一個分類可以有多個子分類
    @OneToMany(mappedBy = "parent")
    private List<ProductCategory> children = new ArrayList<>();

    // 資料狀態：0=停用或已刪除，1=啟用
    @Column(name = "status", nullable = false)
    private Byte status = 1;

    // 資料建立時間
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 資料最後更新時間
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 軟刪除時間；NULL 表示未刪除
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}