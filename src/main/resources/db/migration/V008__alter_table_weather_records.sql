-- 新增測站 ID
ALTER TABLE weather_records
ADD COLUMN station_id VARCHAR(20) NOT NULL AFTER id;

-- 新增測站名稱
ALTER TABLE weather_records
ADD COLUMN station_name VARCHAR(50) NOT NULL AFTER station_id;

-- 建立唯一索引，防止同一測站的觀測資料重複寫入
ALTER TABLE weather_records
ADD CONSTRAINT uk_weather_station_observation
UNIQUE (station_id, record_date, record_time);
