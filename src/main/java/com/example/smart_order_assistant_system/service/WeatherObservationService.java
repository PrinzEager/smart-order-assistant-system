package com.example.smart_order_assistant_system.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.smart_order_assistant_system.client.CwaApiClient;
import com.example.smart_order_assistant_system.response.WeatherObservationResponse;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.JsonNode;

@Service
@RequiredArgsConstructor
public class WeatherObservationService {

    // 注入中央氣象署 API Client
    private final CwaApiClient cwaApiClient;

    /**
     * 根據測站 ID 查詢最新天氣觀測資料
     *
     * @param stationId 測站 ID
     * @return 找到測站則回傳觀測資料，否則回傳空 Optional
     */
    public Optional<WeatherObservationResponse> getObservation(
            String stationId) {

        // 檢查測站 ID 是否為空
        if (stationId == null || stationId.isBlank()) {
            return Optional.empty();
        }

        // 取得中央氣象署原始資料
        JsonNode stations = cwaApiClient.getStations();

        // 確認 API 回傳資料存在
        if (stations == null) {
            throw new IllegalStateException(
                    "中央氣象署 API 未回傳測站資料");
        }

        // 確認 API 回傳資料格式為陣列
        if (!stations.isArray()) {
            throw new IllegalStateException(
                    "中央氣象署 API 回傳的測站資料格式錯誤");
        }

        // 逐筆尋找指定測站
        for (JsonNode station : stations) {

            // 取得測站 ID
            String currentStationId =
                    station.path("StationId").stringValue();

            // 排除沒有測站 ID 的資料
            if (currentStationId == null || currentStationId.isBlank()) {
                continue;
            }

            // 比對使用者指定的測站 ID
            boolean isSameStation =
                    stationId.trim().equals(currentStationId.trim());

            if (!isSameStation) {
                continue;
            }

            // 找到指定測站，取得地理資訊
            JsonNode geoInfo = station.path("GeoInfo");

            // 取得觀測時間
            String dateTime = station.path("ObsTime")
                    .path("DateTime").stringValue();

            // 取得天氣觀測資料
            JsonNode weatherElement =
                    station.path("WeatherElement");

            // 取得降雨量資料
            JsonNode now = weatherElement.path("Now");

            // 建立 Response DTO
            WeatherObservationResponse response =
                    new WeatherObservationResponse();

            response.setStationId(currentStationId);

            response.setStationName(
                    station.path("StationName").stringValue());

            response.setCountyName(
                    geoInfo.path("CountyName").stringValue());

            response.setTownName(
                    geoInfo.path("TownName").stringValue());

            // 將含有時區的時間轉換成 LocalDateTime
            if (dateTime != null && !dateTime.isBlank()) {
                try {
                    // 嘗試解析觀測時間
                    response.setObservationTime(
                            OffsetDateTime.parse(dateTime).toLocalDateTime());

                } catch (java.time.format.DateTimeParseException e) {

                    // 時間格式錯誤時保留 null，不影響其他天氣資料
                    throw new IllegalStateException(
                            "測站 " + currentStationId
                            + " 的觀測時間格式錯誤：" + dateTime,
                            e
                    );
                }
            }

            response.setWeather(
                    weatherElement.path("Weather").stringValue());

            response.setPrecipitation(
                    parseDecimal(now.path("Precipitation")));

            response.setAirTemperature(
                    parseDecimal(weatherElement.path("AirTemperature")));

            response.setRelativeHumidity(
                    parseDecimal(weatherElement.path("RelativeHumidity")));

            response.setWindSpeed(
                    parseDecimal(weatherElement.path("WindSpeed")));

            return Optional.of(response);
        }

        // 找不到指定測站
        return Optional.empty();
    }

    /**
     * 將 JSON 節點轉換成 BigDecimal
     *
     * @param node JSON 資料節點
     * @return 轉換成功回傳 BigDecimal，否則回傳 null
     */
    private BigDecimal parseDecimal(JsonNode node) {

        // 檢查節點是否不存在或為 null
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }

        // 取得原始字串
        String value = node.stringValue();

        // 排除 null 或空白資料
        if (value == null || value.isBlank()) {
            return null;
        }

        // 將字串轉換成 BigDecimal
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
