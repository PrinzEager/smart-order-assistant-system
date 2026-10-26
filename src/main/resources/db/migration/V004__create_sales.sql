CREATE TABLE sales (
    id BIGINT NOT NULL AUTO_INCREMENT
        COMMENT '銷售紀錄編號（主鍵）',

    product_id BIGINT NOT NULL
        COMMENT '商品編號（外鍵）',

    quantity INT NOT NULL
        COMMENT '銷售數量',

    unit_price DECIMAL(10, 2) NOT NULL
        COMMENT '銷售當下的商品單價',

    total_amount DECIMAL(12, 2) NOT NULL
        COMMENT '此筆銷售總金額',

    date_time DATETIME NOT NULL
        COMMENT '實際銷售日期與時間',

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        COMMENT '銷售紀錄建立時間',

    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP
        COMMENT '銷售紀錄最後更新時間',

    deleted_at DATETIME NULL
        COMMENT '軟刪除時間；NULL 表示未刪除',

    PRIMARY KEY (id),

    CONSTRAINT fk_sales_product
        FOREIGN KEY (product_id)
        REFERENCES products(id),

    INDEX idx_sales_product_date (product_id, date_time),
    INDEX idx_sales_date (date_time),

    CONSTRAINT chk_sales_quantity
        CHECK (quantity > 0),

    CONSTRAINT chk_sales_unit_price
        CHECK (unit_price >= 0),

    CONSTRAINT chk_sales_total
        CHECK (total_amount >= 0)
)
ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci
COMMENT='商品銷售紀錄表';