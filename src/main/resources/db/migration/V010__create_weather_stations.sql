-- 建立測站基本資料表
CREATE TABLE weather_stations (

    -- 測站編號，作為主鍵
    station_id VARCHAR(20) NOT NULL,

    -- 測站名稱
    station_name VARCHAR(100) NOT NULL,

    -- 所屬縣市
    county_name VARCHAR(50) NOT NULL,

    -- 所屬鄉鎮
    town_name VARCHAR(50) NOT NULL,

    -- 測站緯度
    latitude DOUBLE NULL,

    -- 測站經度
    longitude DOUBLE NULL,

    -- 是否啟用自動收集，預設啟用
    enabled BOOLEAN NOT NULL DEFAULT TRUE,

    -- 設定主鍵
    PRIMARY KEY (station_id),

    -- 建立啟用狀態索引
    INDEX idx_weather_stations_enabled (enabled)

) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4;