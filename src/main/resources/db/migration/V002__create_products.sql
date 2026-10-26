CREATE TABLE products (
    id BIGINT NOT NULL AUTO_INCREMENT
        COMMENT '商品編號（主鍵）',

    category_id BIGINT NULL
        COMMENT '商品分類編號（外鍵）',

    sku VARCHAR(50) NOT NULL
        COMMENT '內部商品代碼',

    barcode VARCHAR(50) NULL
        COMMENT '商品條碼，保留前導零',

    image_url VARCHAR(500) NULL
        COMMENT '商品主圖片路徑或 URL',

    name VARCHAR(100) NOT NULL
        COMMENT '商品名稱',

    price DECIMAL(10, 2) NOT NULL
        COMMENT '商品售價',

    cost_price DECIMAL(10, 2) NULL
        COMMENT '商品進貨成本',

    unit VARCHAR(20) NOT NULL DEFAULT '個'
        COMMENT '庫存單位',

    current_stock INT NOT NULL DEFAULT 0
        COMMENT '目前庫存數量',

    safety_stock INT NOT NULL DEFAULT 0
        COMMENT '安全庫存數量',

    reorder_point INT NOT NULL DEFAULT 0
        COMMENT '補貨警戒點',

    lead_time_days INT NOT NULL DEFAULT 0
        COMMENT '供應商平均交貨天數',

    shelf_life_days INT NULL
        COMMENT '商品保存期限天數',

    status TINYINT NOT NULL DEFAULT 1
        COMMENT '資料狀態：0=停用或已刪除，1=啟用',

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        COMMENT '商品資料建立時間',

    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP
        COMMENT '商品資料最後更新時間',

    deleted_at DATETIME NULL
        COMMENT '軟刪除時間；NULL 表示未刪除',

    PRIMARY KEY (id),

    CONSTRAINT fk_products_category
        FOREIGN KEY (category_id)
        REFERENCES product_categories(id),

    CONSTRAINT uq_products_sku UNIQUE (sku),
    CONSTRAINT uq_products_barcode UNIQUE (barcode),

    INDEX idx_products_name (name),
    INDEX idx_products_category_status (category_id, status),
    INDEX idx_products_stock (current_stock),
    INDEX idx_products_reorder (reorder_point),

    CONSTRAINT chk_products_price
        CHECK (price >= 0),

    CONSTRAINT chk_products_cost_price
        CHECK (cost_price IS NULL OR cost_price >= 0),

    CONSTRAINT chk_products_stock
        CHECK (current_stock >= 0),

    CONSTRAINT chk_products_safety_stock
        CHECK (safety_stock >= 0),

    CONSTRAINT chk_products_reorder_point
        CHECK (reorder_point >= 0),

    CONSTRAINT chk_products_lead_time
        CHECK (lead_time_days >= 0),

    CONSTRAINT chk_products_shelf_life
        CHECK (shelf_life_days IS NULL OR shelf_life_days >= 0),

    CONSTRAINT chk_products_status
        CHECK (status IN (0, 1))
)
ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci
COMMENT='商品基本資料、搜尋、圖片及庫存管理表';