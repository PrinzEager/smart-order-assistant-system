
package com.example.smart_order_assistant_system.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.stereotype.Service;

import com.example.smart_order_assistant_system.client.CwaApiClient;
import com.example.smart_order_assistant_system.response.WeatherStationResponse;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.JsonNode;

@Service
@RequiredArgsConstructor
public class WeatherStationService {

    // 注入中央氣象署 API Client
    private final CwaApiClient cwaApiClient;

    /**
     * 取得所有不重複的縣市名稱
     *
     * @return 已去重且排序的縣市清單
     */
    public List<String> getCounties() {

        // 取得中央氣象署所有測站資料
        JsonNode stations = cwaApiClient.getStations();

        // 使用 TreeSet 自動去重並排序
        Set<String> counties = new TreeSet<>();

        // 確認取得的資料存在且為陣列
        if (stations == null || !stations.isArray()) {
            return new ArrayList<>();
        }

        // 逐筆讀取測站資料
        for (JsonNode station : stations) {

            // 取得測站地理資訊
            JsonNode geoInfo = station.path("GeoInfo");

            // 取得縣市名稱節點
            JsonNode countyNode = geoInfo.path("CountyName");

            // 確認節點存在且不是 null
            String countyName = countyNode.isMissingNode() || countyNode.isNull()
                    ? ""
                    : countyNode.asText();

            // 排除空白資料
            if (!countyName.isBlank()) {
                counties.add(countyName.trim());
            }
        }

        // 將 Set 轉換成 List 回傳
        return new ArrayList<>(counties);
    }

    /**
     * 根據縣市取得不重複的鄉鎮名稱
     *
     * @param county 縣市名稱
     * @return 該縣市所有不重複且排序的鄉鎮清單
     */
    public List<String> getTowns(String county) {

        // 取得中央氣象署所有測站資料
        JsonNode stations = cwaApiClient.getStations();

        // 使用 TreeSet 自動去重並排序
        Set<String> towns = new TreeSet<>();

        // 確認測站資料存在且為陣列
        if (stations == null || !stations.isArray()) {
            return new ArrayList<>();
        }

        // 逐筆讀取測站資料
        for (JsonNode station : stations) {

            // 取得測站地理資訊
            JsonNode geoInfo = station.path("GeoInfo");

            // 取得縣市名稱節點
            JsonNode countyNode = geoInfo.path("CountyName");

            // 取得鄉鎮名稱節點
            JsonNode townNode = geoInfo.path("TownName");

            // 確認縣市節點存在且不是 null
            String stationCounty = countyNode.isMissingNode() || countyNode.isNull()
                    ? ""
                    : countyNode.asText();

            // 確認鄉鎮節點存在且不是 null
            String townName = townNode.isMissingNode() || townNode.isNull()
                    ? ""
                    : townNode.asText();

            // 比對縣市名稱，只處理指定縣市的測站
            if (stationCounty.trim().equals(county.trim())) {

                // 排除空白鄉鎮資料
                if (!townName.isBlank()) {
                    towns.add(townName.trim());
                }
            }
        }

        // 將 Set 轉換成 List 回傳
        return new ArrayList<>(towns);
    }

    /**
     * 根據縣市與鄉鎮查詢測站資料
     *
     * @param county 縣市名稱
     * @param town 鄉鎮名稱
     * @return 符合條件的測站清單
     */
    public List<WeatherStationResponse> getStations(
            String county, String town) {

        // 取得中央氣象署所有測站資料
        JsonNode stations = cwaApiClient.getStations();

        // 建立測站回傳清單
        List<WeatherStationResponse> result = new ArrayList<>();

        // 檢查輸入條件是否為空白
        if (county == null || county.isBlank()
                || town == null || town.isBlank()) {
            return result;
        }

        // 確認測站資料存在且為陣列
        if (stations == null || !stations.isArray()) {
            return result;
        }

        // 逐筆讀取測站資料
        for (JsonNode station : stations) {

            // 取得測站地理資訊
            JsonNode geoInfo = station.path("GeoInfo");

            // 取得縣市與鄉鎮名稱
            String countyName = geoInfo.path("CountyName").asText();
            String townName = geoInfo.path("TownName").asText();

            // 比對使用者指定的縣市與鄉鎮
            if (!countyName.trim().equals(county.trim())
                    || !townName.trim().equals(town.trim())) {
                continue;
            }

            // 取得 WGS84 緯度與經度
            Double latitude = getWgs84Coordinate(
                    geoInfo, "StationLatitude");

            Double longitude = getWgs84Coordinate(
                    geoInfo, "StationLongitude");

            // 建立測站 DTO
            WeatherStationResponse response =
                    new WeatherStationResponse(
                            station.path("StationId").asText(),
                            station.path("StationName").asText(),
                            countyName,
                            townName,
                            latitude,
                            longitude
                    );

            // 加入回傳清單
            result.add(response);
        }

        return result;
    }

    /**
     * 取得測站的 WGS84 座標
     *
     * @param geoInfo 測站地理資訊
     * @param coordinateField 要取得的座標欄位
     * @return 座標數值，若找不到則回傳 null
     */
    private Double getWgs84Coordinate(
            JsonNode geoInfo, String coordinateField) {

        // 取得所有座標資料
        JsonNode coordinates = geoInfo.path("Coordinates");

        // 確認座標資料存在且為陣列
        if (!coordinates.isArray()) {
            return null;
        }

        // 逐筆尋找 WGS84 座標
        for (JsonNode coordinate : coordinates) {

            String coordinateName =
                    coordinate.path("CoordinateName").asText();

            // 只使用 WGS84 座標
            if ("WGS84".equals(coordinateName)) {

                // 取得指定的經度或緯度
                String value =
                        coordinate.path(coordinateField).asText();

                // 排除空白資料
                if (value.isBlank()) {
                    return null;
                }

                // 將座標字串轉換成 Double
                try {
                    return Double.valueOf(value);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }

        // 找不到 WGS84 座標
        return null;
    }
}
