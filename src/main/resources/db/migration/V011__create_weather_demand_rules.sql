
CREATE TABLE weather_demand_rules (

    -- 天氣需求規則編號（主鍵）
    id BIGINT NOT NULL AUTO_INCREMENT,

    -- 對應商品分類編號（外鍵）
    category_id BIGINT NOT NULL,

    -- 天氣規則名稱
    rule_name VARCHAR(100) NOT NULL,

    -- 天氣指標：TEMPERATURE、HUMIDITY、RAIN_PROBABILITY
    weather_element VARCHAR(30) NOT NULL,

    -- 條件運算子：GT、GTE、LT、LTE、BETWEEN
    `operator` VARCHAR(10) NOT NULL,

    -- 條件判斷的數值
    threshold_value DECIMAL(10, 2) NOT NULL,

    -- 區間條件上限（BETWEEN 使用）
    upper_threshold_value DECIMAL(10, 2) DEFAULT NULL,

    -- 天氣需求調整係數
    adjustment_factor DECIMAL(5, 2) NOT NULL,

    -- 資料狀態：0=停用，1=啟用
    status TINYINT NOT NULL DEFAULT 1,

    -- 資料建立時間
    created_at DATETIME(6) NOT NULL,

    -- 資料最後更新時間
    updated_at DATETIME(6) NOT NULL,

    -- 軟刪除時間；NULL 表示未刪除
    deleted_at DATETIME(6) DEFAULT NULL,

    -- 設定主鍵
    PRIMARY KEY (id),

    -- 商品分類外鍵
    CONSTRAINT fk_weather_rules_category
        FOREIGN KEY (category_id)
        REFERENCES product_categories(id),

    -- 分類查詢索引
    INDEX idx_weather_rules_category (category_id),

    -- 狀態及軟刪除查詢索引
    INDEX idx_weather_rules_status_deleted (status, deleted_at)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
