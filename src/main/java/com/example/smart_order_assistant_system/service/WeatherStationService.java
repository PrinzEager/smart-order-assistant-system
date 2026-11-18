package com.example.smart_order_assistant_system.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.smart_order_assistant_system.client.CwaApiClient;
import com.example.smart_order_assistant_system.response.WeatherStationResponse;

import com.example.smart_order_assistant_system.entity.WeatherStation;
import com.example.smart_order_assistant_system.repository.WeatherStationRepository;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.JsonNode;

@Service
@RequiredArgsConstructor
public class WeatherStationService {

    // 注入中央氣象署 API Client
    private final CwaApiClient cwaApiClient;
    // 注入測站資料 Repository
    private final WeatherStationRepository weatherStationRepository;

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
                    : countyNode.stringValue();

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
                    : countyNode.stringValue();

            // 確認鄉鎮節點存在且不是 null
            String townName = townNode.isMissingNode() || townNode.isNull()
                    ? ""
                    : townNode.stringValue();

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
            String countyName = geoInfo.path("CountyName").stringValue();
            String townName = geoInfo.path("TownName").stringValue();

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
                            station.path("StationId").stringValue(),
                            station.path("StationName").stringValue(),
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
     * 將指定測站儲存到 MySQL
     *
     * @param stationId 中央氣象署測站編號
     * @return 儲存後的測站資料
     */
    @Transactional
    public WeatherStation saveStation(String stationId) {

        // 檢查測站編號是否為空白
        if (stationId == null || stationId.isBlank()) {
            throw new IllegalArgumentException("測站編號不可為空");
        }

        // 檢查資料庫是否已經存在該測站
        // 如果存在就直接回傳，避免重複新增
        WeatherStation existingStation =
                weatherStationRepository.findById(stationId.trim())
                        .orElse(null);

        if (existingStation != null) {
            return existingStation;
        }

        // 向中央氣象署取得所有測站資料
        JsonNode stations = cwaApiClient.getStations();

        // 確認測站資料存在且為陣列
        if (stations == null || !stations.isArray()) {
            throw new IllegalStateException("無法取得中央氣象署測站資料");
        }

        // 尋找使用者指定的測站
        for (JsonNode station : stations) {

            // 取得測站編號
            String currentStationId =
                    station.path("StationId").stringValue();

            // 比對測站編號
            if (!stationId.trim().equals(currentStationId.trim())) {
                continue;
            }

            // 取得測站地理資訊
            JsonNode geoInfo = station.path("GeoInfo");

            // 取得縣市名稱
            String countyName =
                    geoInfo.path("CountyName").stringValue();

            // 取得鄉鎮名稱
            String townName =
                    geoInfo.path("TownName").stringValue();

            // 取得測站緯度
            Double latitude = getWgs84Coordinate(
                    geoInfo, "StationLatitude");

            // 取得測站經度
            Double longitude = getWgs84Coordinate(
                    geoInfo, "StationLongitude");

            // 建立測站 Entity
            WeatherStation weatherStation = new WeatherStation();

            // 設定測站編號
            weatherStation.setStationId(currentStationId);

            // 設定測站名稱
            weatherStation.setStationName(
                    station.path("StationName").stringValue());

            // 設定縣市名稱
            weatherStation.setCountyName(countyName);

            // 設定鄉鎮名稱
            weatherStation.setTownName(townName);

            // 設定測站緯度
            weatherStation.setLatitude(latitude);

            // 設定測站經度
            weatherStation.setLongitude(longitude);

            // 預設啟用天氣資料收集
            weatherStation.setEnabled(true);

            // 將測站資料儲存到 MySQL
            return weatherStationRepository.save(weatherStation);
        }

        // 找不到指定測站
        throw new IllegalArgumentException(
                "中央氣象署不存在此測站：" + stationId);
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
                    coordinate.path("CoordinateName").stringValue();

            // 只使用 WGS84 座標
            if ("WGS84".equals(coordinateName)) {

                // 取得指定的經度或緯度
                String value =
                        coordinate.path(coordinateField).stringValue();

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
