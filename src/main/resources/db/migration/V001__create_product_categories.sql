CREATE TABLE product_categories (
    id BIGINT NOT NULL AUTO_INCREMENT
        COMMENT '商品分類編號（主鍵）',

    name VARCHAR(100) NOT NULL
        COMMENT '分類名稱',

    parent_id BIGINT NULL
        COMMENT '上層分類編號',

    status TINYINT NOT NULL DEFAULT 1
        COMMENT '資料狀態：0=停用或已刪除，1=啟用',

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        COMMENT '資料建立時間',

    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP
        COMMENT '資料最後更新時間',

    deleted_at DATETIME NULL
        COMMENT '軟刪除時間；NULL 表示未刪除',

    PRIMARY KEY (id),

    CONSTRAINT fk_categories_parent
        FOREIGN KEY (parent_id)
        REFERENCES product_categories(id),

    INDEX idx_categories_parent (parent_id),
    INDEX idx_categories_status (status),

    CONSTRAINT chk_categories_status
        CHECK (status IN (0, 1))
)
ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci
COMMENT='商品分類資料表';