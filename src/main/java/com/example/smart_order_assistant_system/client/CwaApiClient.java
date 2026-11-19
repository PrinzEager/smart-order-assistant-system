package com.example.smart_order_assistant_system.client;

import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import tools.jackson.databind.JsonNode;

@Component
public class CwaApiClient {

    // 中央氣象署目前使用的測站觀測資料集 ID
    private static final String DATA_ID = "O-A0001-001";

    // 中央氣象署高雄市未來 1 週鄉鎮天氣預報資料集 ID
    private static final String FORECAST_DATA_ID = "F-D0047-067";

    // 商店所在地區
    private static final String FORECAST_LOCATION = "路竹區";

    // Spring RestClient：負責發送 HTTP 請求
    private final RestClient restClient;

    // 中央氣象署 API 授權金鑰
    private final String authorization;

    public CwaApiClient(
            RestClient.Builder builder,
            @Value("${cwa.api.base-url}") String baseUrl,
            @Value("${cwa.api.authorization}") String authorization) {

        // 建立 HTTP Client，設定連線逾時時間為 5 秒
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        // 建立 HTTP Request Factory
        JdkClientHttpRequestFactory requestFactory =
                new JdkClientHttpRequestFactory(httpClient);

        // 設定讀取回應資料的逾時時間為 10 秒
        requestFactory.setReadTimeout(Duration.ofSeconds(10));

        // 建立 RestClient，設定 API 基礎網址及 HTTP 請求工廠
        this.restClient = builder
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();

        this.authorization = authorization;
    }

    /**
     * 取得中央氣象署原始天氣資料
     *
     * @return API 回傳的 JSON 資料
     */
    public JsonNode getWeatherData() {

        try {
            // 發送 GET 請求，取得中央氣象署資料
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/{dataId}")
                            .queryParam("Authorization", authorization)
                            .queryParam("format", "JSON")
                            .build(DATA_ID))
                    .retrieve()
                    .body(JsonNode.class);

        } catch (RestClientResponseException e) {

            // 處理 HTTP 4xx、5xx 狀態碼錯誤
            int statusCode = e.getStatusCode().value();

            throw new IllegalStateException(
                    "中央氣象署 API 回應錯誤，HTTP 狀態碼："
                            + statusCode
                            + "，原因：" + getHttpErrorMessage(statusCode),
                    e
            );

        } catch (ResourceAccessException e) {

            // 處理連線失敗、連線逾時及讀取逾時等問題
            throw new IllegalStateException(
                    "連線或讀取中央氣象署 API 失敗，請檢查網路或 API 是否逾時",
                    e
            );

        } catch (RestClientException e) {

            // 處理其他 RestClient 呼叫錯誤
            throw new IllegalStateException(
                    "呼叫中央氣象署 API 時發生錯誤",
                    e
            );
        }
    }

    /**
     * 根據 HTTP 狀態碼取得容易理解的錯誤訊息
     *
     * @param statusCode HTTP 狀態碼
     * @return 對應的錯誤說明
     */
    private String getHttpErrorMessage(int statusCode) {

        // 依照 HTTP 狀態碼分類錯誤原因
        switch (statusCode) {

            case 401:
                return "API 金鑰可能錯誤或尚未通過授權";

            case 403:
                return "目前沒有存取此 API 的權限";

            case 404:
                return "API 資料集或請求路徑不存在";

            case 429:
                return "API 請求次數超過限制";

            default:
                if (statusCode >= 500) {
                    return "中央氣象署伺服器發生錯誤";
                }

                return "HTTP 請求未成功";
        }
    }

    /**
     * 取得高雄市未來一週天氣預報原始 JSON。
     *
     * @return 中央氣象署回傳的 JSON 資料
     */
    public JsonNode getWeatherForecast() {

        try {
            // 呼叫中央氣象署未來一週鄉鎮天氣預報 API
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/{dataId}")

                            // 使用 Query Parameter 傳送授權碼
                            .queryParam("Authorization", authorization)

                            // 指定 JSON 格式
                            .queryParam("format", "JSON")

                            .build(FORECAST_DATA_ID))

                    .retrieve()
                    .body(JsonNode.class);

        } catch (RestClientResponseException e) {

            // 處理 HTTP 4xx、5xx 錯誤
            int statusCode = e.getStatusCode().value();

            throw new IllegalStateException(
                    "中央氣象署天氣預報 API 回應錯誤，HTTP 狀態碼："
                            + statusCode
                            + "，原因：" + getHttpErrorMessage(statusCode),
                    e
            );

        } catch (ResourceAccessException e) {

            // 處理連線失敗及逾時
            throw new IllegalStateException(
                    "連線或讀取中央氣象署天氣預報 API 失敗",
                    e
            );

        } catch (RestClientException e) {

            // 處理其他 HTTP 呼叫錯誤
            throw new IllegalStateException(
                    "呼叫中央氣象署天氣預報 API 時發生錯誤",
                    e
            );
        }
    }

    /**
     * 取得測站觀測資料陣列
     *
     * @return 測站資料 JSON 陣列
     */
    public JsonNode getStations() {

        // 取得中央氣象署原始資料
        JsonNode response = getWeatherData();

        // 確認 API 是否有回傳資料
        if (response == null || response.isNull()) {
            throw new IllegalStateException(
                    "中央氣象署 API 未回傳任何資料");
        }

        // 取得 records 節點
        JsonNode records = response.path("records");

        // 確認 records 是否存在
        if (records.isMissingNode() || records.isNull()) {
            throw new IllegalStateException(
                    "中央氣象署 API 回應缺少 records 資料");
        }

        // 取得 Station 測站資料
        JsonNode stations = records.path("Station");

        // 確認 Station 是否為陣列
        if (!stations.isArray()) {
            throw new IllegalStateException(
                    "中央氣象署 API 的 Station 資料格式錯誤");
        }

        return stations;
    }
}
