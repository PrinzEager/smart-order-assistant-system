CREATE TABLE weather_records (
    id BIGINT NOT NULL AUTO_INCREMENT
        COMMENT '天氣紀錄編號（主鍵）',

    record_date DATE NOT NULL
        COMMENT '天氣紀錄日期',

    record_time TIME NOT NULL DEFAULT '12:00:00'
        COMMENT '天氣紀錄時間',

    temperature DECIMAL(5, 2) NULL
        COMMENT '氣溫，單位為攝氏度',

    humidity DECIMAL(5, 2) NULL
        COMMENT '相對濕度，單位為百分比',

    weather_condition VARCHAR(50) NULL
        COMMENT '天氣狀況，例如晴天、多雲、下雨',

    rainfall DECIMAL(8, 2) NULL
        COMMENT '降雨量，單位為毫米',

    location VARCHAR(100) NOT NULL
        COMMENT '天氣觀測地點',

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        COMMENT '天氣資料建立時間',

    PRIMARY KEY (id),

    INDEX idx_weather_date_location (record_date, location),
    INDEX idx_weather_location_time
        (location, record_date, record_time),

    CONSTRAINT chk_weather_humidity
        CHECK (humidity IS NULL OR humidity BETWEEN 0 AND 100),

    CONSTRAINT chk_weather_rainfall
        CHECK (rainfall IS NULL OR rainfall >= 0)
)
ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_unicode_ci
COMMENT='天氣紀錄表，供訂貨需求分析使用';