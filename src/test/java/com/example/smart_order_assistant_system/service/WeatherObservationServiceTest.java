package com.example.smart_order_assistant_system.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.smart_order_assistant_system.client.CwaApiClient;
import com.example.smart_order_assistant_system.response.WeatherObservationResponse;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

// 使用 Mockito 執行單元測試
@ExtendWith(MockitoExtension.class)
class WeatherObservationServiceTest {

    // 模擬氣象署 API Client，不會真的呼叫外部 API
    @Mock
    private CwaApiClient cwaApiClient;

    // 將模擬的 Client 注入真正的 Service
    @InjectMocks
    private WeatherObservationService weatherObservationService;

    // 建立 JSON 轉換工具
    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    /**
     * 測試氣象資料缺少部分欄位時，
     * Service 是否仍能正常建立 Response DTO
     */
    @Test
    void getObservation_shouldReturnDataWhenSomeFieldsAreMissing() {

        // 模擬氣象署回傳的 JSON 資料
        String json = """
                [
                    {
                        "StationId": "TEST001",
                        "StationName": "測試測站",
                        "GeoInfo": {
                            "CountyName": "高雄市",
                            "TownName": "路竹區"
                        },
                        "ObsTime": {
                            "DateTime": "2026-11-18T19:00:00+08:00"
                        },
                        "WeatherElement": {
                            "Weather": "晴",
                            "Now": {},
                            "AirTemperature": "24.8",
                            "RelativeHumidity": "75"
                        }
                    }
                ]
                """;

        // 將 JSON 字串轉換成 JsonNode
        JsonNode stations = jsonMapper.readTree(json);

        // 當 Service 呼叫 getStations() 時，
        // 回傳我們事先準備好的測試資料
        when(cwaApiClient.getStations()).thenReturn(stations);

        // 執行真正的 Service 方法
        Optional<WeatherObservationResponse> result =
                weatherObservationService.getObservation("TEST001");

        // 確認有成功取得資料
        assertTrue(result.isPresent());

        // 取得 Response DTO
        WeatherObservationResponse response = result.get();

        // 確認基本資料正常
        assertEquals("TEST001", response.getStationId());
        assertEquals("測試測站", response.getStationName());
        assertEquals("高雄市", response.getCountyName());
        assertEquals("路竹區", response.getTownName());
        assertEquals("晴", response.getWeather());

        // 確認時間正確
        assertNotNull(response.getObservationTime());
        assertEquals(
                LocalDateTime.of(2026, 11, 18, 19, 0, 0),
                response.getObservationTime()
        );

        // 確認有資料的數值欄位正常
        assertEquals(
                0,
                new BigDecimal("24.8").compareTo(response.getAirTemperature())
        );

        assertEquals(
                0,
                new BigDecimal("75").compareTo(response.getRelativeHumidity())
        );

        // 確認缺少的欄位正確回傳 null
        assertNull(response.getPrecipitation());
        assertNull(response.getWindSpeed());
    }

    /**
     * 測試氣象資料出現不合法數值時，
     * parseDecimal() 是否能正確處理
     */
    @Test
    void getObservation_shouldReturnNullWhenNumericValueIsInvalid() {

        // 模擬氣象署回傳不合法的數值
        String json = """
                [
                    {
                        "StationId": "TEST002",
                        "StationName": "測試測站",
                        "GeoInfo": {
                            "CountyName": "高雄市",
                            "TownName": "路竹區"
                        },
                        "ObsTime": {
                            "DateTime": "2026-11-18T19:00:00+08:00"
                        },
                        "WeatherElement": {
                            "Weather": "晴",
                            "Now": {
                                "Precipitation": "N/A"
                            },
                            "AirTemperature": "abc",
                            "RelativeHumidity": "",
                            "WindSpeed": "2.7"
                        }
                    }
                ]
                """;

        // 將 JSON 轉換成 JsonNode
        JsonNode stations = jsonMapper.readTree(json);

        // 模擬 API Client 回傳測試資料
        when(cwaApiClient.getStations()).thenReturn(stations);

        // 執行 Service
        Optional<WeatherObservationResponse> result =
                weatherObservationService.getObservation("TEST002");

        // 確認成功找到測站
        assertTrue(result.isPresent());

        // 取得 Response DTO
        WeatherObservationResponse response = result.get();

        // 確認不合法數值都轉換成 null
        assertNull(response.getAirTemperature());
        assertNull(response.getRelativeHumidity());
        assertNull(response.getPrecipitation());

        // 確認合法數值仍然可以正常轉換
        assertNotNull(response.getWindSpeed());

        assertEquals(
                0,
                new BigDecimal("2.7").compareTo(response.getWindSpeed())
        );
    }

    /**
     * 測試觀測時間格式錯誤時，
     * Service 是否能保留其他資料並將時間設為 null
     */
    @Test
    void getObservation_shouldReturnNullWhenDateTimeIsInvalid() {

        // 模擬氣象署回傳錯誤的觀測時間
        String json = """
                [
                    {
                        "StationId": "TEST003",
                        "StationName": "測試測站",
                        "GeoInfo": {
                            "CountyName": "高雄市",
                            "TownName": "路竹區"
                        },
                        "ObsTime": {
                            "DateTime": "2026/11/18 19:00"
                        },
                        "WeatherElement": {
                            "Weather": "晴",
                            "Now": {
                                "Precipitation": "0"
                            },
                            "AirTemperature": "24.8",
                            "RelativeHumidity": "75",
                            "WindSpeed": "2.7"
                        }
                    }
                ]
                """;

        // 將 JSON 轉換成 JsonNode
        JsonNode stations = jsonMapper.readTree(json);

        // 模擬 API Client 回傳測試資料
        when(cwaApiClient.getStations()).thenReturn(stations);

        // 執行 Service
        Optional<WeatherObservationResponse> result =
                weatherObservationService.getObservation("TEST003");

        // 確認仍然可以取得測站資料
        assertTrue(result.isPresent());

        // 取得 Response DTO
        WeatherObservationResponse response = result.get();

        // 錯誤時間應該轉換成 null
        assertNull(response.getObservationTime());

        // 其他正常資料仍然可以取得
        assertEquals("TEST003", response.getStationId());
        assertEquals("晴", response.getWeather());

        assertEquals(
                0,
                new BigDecimal("24.8").compareTo(response.getAirTemperature())
        );
    }
}
