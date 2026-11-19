package com.example.smart_order_assistant_system.service;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.smart_order_assistant_system.client.CwaApiClient;
import com.example.smart_order_assistant_system.response.WeatherForecastPeriodResponse;
import com.example.smart_order_assistant_system.response.WeatherForecastResponse;

import lombok.RequiredArgsConstructor;

import tools.jackson.databind.JsonNode;

@Service
@RequiredArgsConstructor
public class WeatherForecastService {

    // 中央氣象署 API Client
    private final CwaApiClient cwaApiClient;

    // 商店所在縣市
    private static final String CITY_NAME = "高雄市";

    // 商店所在鄉鎮
    private static final String LOCATION_NAME = "路竹區";

    /**
     * 取得商店所在地區的未來一週天氣預報。
     *
     * @return 路竹區天氣預報 DTO
     */
    public WeatherForecastResponse getStoreWeatherForecast() {

        // 取得中央氣象署原始 JSON
        JsonNode response = cwaApiClient.getWeatherForecast();

        // 確認 API 是否有回傳資料
        if (response == null || response.isNull()) {
            throw new IllegalStateException(
                    "中央氣象署沒有回傳天氣預報資料");
        }

        // 檢查 API 是否回傳成功
        if ("false".equalsIgnoreCase(
                response.path("success").stringValue())) {

            throw new IllegalStateException(
                    "中央氣象署天氣預報 API 回傳失敗");
        }

        // 取得 records 節點
        JsonNode records = response.path("records");

        if (records.isMissingNode() || records.isNull()) {
            throw new IllegalStateException(
                    "天氣預報資料缺少 records 節點");
        }

        // 取得縣市資料陣列
        JsonNode locationsGroups = records.path("Locations");

        if (!locationsGroups.isArray()) {
            throw new IllegalStateException(
                    "天氣預報資料的 Locations 格式錯誤");
        }

        // 尋找高雄市
        for (JsonNode city : locationsGroups) {

            String cityName = getText(city, "LocationsName");

            if (!CITY_NAME.equals(cityName)) {
                continue;
            }

            // 取得高雄市底下的鄉鎮資料
            JsonNode towns = city.path("Location");

            if (!towns.isArray()) {
                throw new IllegalStateException(
                        "高雄市鄉鎮天氣預報資料格式錯誤");
            }

            // 尋找路竹區
            for (JsonNode town : towns) {

                String townName = getText(town, "LocationName");

                if (LOCATION_NAME.equals(townName)) {

                    // 找到路竹區，開始轉換 DTO
                    return convertToResponse(town);
                }
            }

            throw new IllegalStateException(
                    "中央氣象署沒有提供高雄市路竹區的天氣預報");
        }

        throw new IllegalStateException(
                "中央氣象署回傳資料中找不到高雄市");
    }

    /**
     * 將路竹區原始 JSON 轉換成天氣預報 DTO。
     *
     * 使用 StartTime 和 EndTime 作為時段識別依據，
     * 將不同 WeatherElement 的資料合併到同一個時段。
     *
     * @param town 路竹區原始 JSON
     * @return 整理完成的天氣預報 DTO
     */
    private WeatherForecastResponse convertToResponse(JsonNode town) {

        // 取得 WeatherElement 陣列
        JsonNode weatherElements = town.path("WeatherElement");

        if (!weatherElements.isArray()) {
            throw new IllegalStateException(
                    "路竹區天氣預報缺少 WeatherElement 資料");
        }

        // 使用 LinkedHashMap 保留原始預報時段順序
        Map<String, WeatherForecastPeriodResponse> forecastMap =
                new LinkedHashMap<>();

        // 遍歷各種天氣項目
        for (JsonNode element : weatherElements) {

            // 取得該天氣項目的各時段資料
            JsonNode times = element.path("Time");

            if (!times.isArray()) {
                continue;
            }

            // 遍歷每一個預報時段
            for (JsonNode time : times) {

                String startTime = getText(time, "StartTime");
                String endTime = getText(time, "EndTime");

                // 確認預報時間是否存在
                if (startTime == null || endTime == null) {
                    continue;
                }

                // 使用開始及結束時間組成唯一識別鍵
                String key = startTime + "|" + endTime;

                // 取得或建立該時段的 DTO
                WeatherForecastPeriodResponse period =
                        forecastMap.computeIfAbsent(
                                key,
                                k -> new WeatherForecastPeriodResponse(
                                        startTime,
                                        endTime,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null
                                )
                        );

                // 取得該時段的 ElementValue
                JsonNode values = time.path("ElementValue");

                if (!values.isArray() || values.isEmpty()) {
                    continue;
                }

                // 取得第一筆預報數值
                JsonNode value = values.get(0);

                // 將不同天氣項目的資料填入同一個 DTO
                applyWeatherValue(period, value);
            }
        }

        // 建立完整地區預報回應
        WeatherForecastResponse response =
                new WeatherForecastResponse();

        response.setLocationName(
                getText(town, "LocationName"));

        response.setGeocode(
                getText(town, "Geocode"));

        response.setLatitude(
                getText(town, "Latitude"));

        response.setLongitude(
                getText(town, "Longitude"));

        // 將整理好的預報時段放入回應
        response.setForecasts(
                new java.util.ArrayList<>(forecastMap.values()));

        return response;
    }

    /**
     * 根據 ElementValue 裡的實際欄位，
     * 將天氣數值填入對應的 DTO。
     *
     * @param period 單一預報時段
     * @param value 氣象署回傳的 ElementValue
     */
    private void applyWeatherValue(
            WeatherForecastPeriodResponse period,
            JsonNode value) {

        // 平均氣溫
        if (!value.path("Temperature").isMissingNode()) {
            period.setAverageTemperature(
                    toInteger(value, "Temperature"));
        }

        // 最高氣溫
        if (!value.path("MaxTemperature").isMissingNode()) {
            period.setMaxTemperature(
                    toInteger(value, "MaxTemperature"));
        }

        // 最低氣溫
        if (!value.path("MinTemperature").isMissingNode()) {
            period.setMinTemperature(
                    toInteger(value, "MinTemperature"));
        }

        // 平均相對濕度
        if (!value.path("RelativeHumidity").isMissingNode()) {
            period.setRelativeHumidity(
                    toInteger(value, "RelativeHumidity"));
        }

        // 12 小時降雨機率
        if (!value.path("ProbabilityOfPrecipitation").isMissingNode()) {
            period.setPrecipitationProbability(
                    toInteger(value, "ProbabilityOfPrecipitation"));
        }

        // 天氣現象
        if (!value.path("Weather").isMissingNode()) {
            period.setWeather(
                    getText(value, "Weather"));
        }

        // 天氣現象代碼
        if (!value.path("WeatherCode").isMissingNode()) {
            period.setWeatherCode(
                    getText(value, "WeatherCode"));
        }

        // 天氣預報綜合描述
        if (!value.path("WeatherDescription").isMissingNode()) {
            period.setWeatherDescription(
                    getText(value, "WeatherDescription"));
        }
    }

    /**
     * 安全取得 JSON 字串。
     *
     * @param node JSON 節點
     * @param field 欄位名稱
     * @return 欄位文字，不存在時回傳 null
     */
    private String getText(JsonNode node, String field) {

        JsonNode value = node.path(field);

        if (value.isMissingNode() || value.isNull()) {
            return null;
        }

        return value.stringValue();
    }

    /**
     * 將氣象署回傳的字串轉換成 Integer。
     *
     * 例如：
     * "25" -> 25
     * "-"  -> null
     *
     * @param node JSON 節點
     * @param field 欄位名稱
     * @return 整數，無法轉換時回傳 null
     */
    private Integer toInteger(JsonNode node, String field) {

        String value = getText(node, field);

        if (value == null || value.isBlank() || "-".equals(value)) {
            return null;
        }

        try {
            return Integer.valueOf(value);

        } catch (NumberFormatException e) {

            // 避免單筆錯誤資料造成整份預報轉換失敗
            return null;
        }
    }
}
