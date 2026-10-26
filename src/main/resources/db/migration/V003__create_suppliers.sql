CREATE TABLE suppliers (
    id BIGINT NOT NULL AUTO_INCREMENT
        COMMENT '供應商編號（主鍵）',

    name VARCHAR(100) NOT NULL
        COMMENT '供應商名稱',

    contact_person VARCHAR(50) NULL
        COMMENT '供應商聯絡人',

    phone VARCHAR(30) NULL
        COMMENT '聯絡電話',

    email VARCHAR(100) NULL
        COMMENT '電子郵件',

    address VARCHAR(255) NULL
        COMMENT '供應商地址',

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

    INDEX idx_suppliers_name (name),
    INDEX idx_suppliers_status (status),

    CONSTRAINT chk_suppliers_status
        CHECK (status IN (0, 1))
)
ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci
COMMENT='供應商資料表';