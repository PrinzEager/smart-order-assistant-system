CREATE TABLE purchase_order_items (
    id BIGINT NOT NULL AUTO_INCREMENT
        COMMENT '訂貨明細編號（主鍵）',

    purchase_order_id BIGINT NOT NULL
        COMMENT '訂貨單編號（外鍵）',

    product_id BIGINT NOT NULL
        COMMENT '商品編號（外鍵）',

    quantity INT NOT NULL
        COMMENT '訂貨數量',

    unit_price DECIMAL(10, 2) NOT NULL
        COMMENT '訂貨當下的商品單價',

    subtotal DECIMAL(12, 2) NOT NULL
        COMMENT '此項商品的小計金額',

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        COMMENT '訂貨明細建立時間',

    PRIMARY KEY (id),

    CONSTRAINT fk_poi_order
        FOREIGN KEY (purchase_order_id)
        REFERENCES purchase_orders(id),

    CONSTRAINT fk_poi_product
        FOREIGN KEY (product_id)
        REFERENCES products(id),

    INDEX idx_poi_order (purchase_order_id),
    INDEX idx_poi_product (product_id),

    CONSTRAINT chk_poi_quantity
        CHECK (quantity > 0),

    CONSTRAINT chk_poi_unit_price
        CHECK (unit_price >= 0),

    CONSTRAINT chk_poi_subtotal
        CHECK (subtotal >= 0)
)
ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci
COMMENT='訂貨單商品明細表';