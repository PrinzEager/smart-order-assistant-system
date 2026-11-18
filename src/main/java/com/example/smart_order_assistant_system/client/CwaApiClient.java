package com.example.smart_order_assistant_system.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import tools.jackson.databind.JsonNode;

@Component
public class CwaApiClient {

    private static final String DATA_ID = "O-A0001-001";

    private final RestClient restClient;
    private final String authorization;

    public CwaApiClient(
            RestClient.Builder builder,
            @Value("${cwa.api.base-url}") String baseUrl,
            @Value("${cwa.api.authorization}") String authorization) {

        this.restClient = builder
                .baseUrl(baseUrl)
                .build();

        this.authorization = authorization;
    }

    // 取得中央氣象署天氣資料
    public JsonNode getWeatherData() {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/{dataId}")
                        .queryParam("Authorization", authorization)
                        .queryParam("format", "JSON")
                        .build(DATA_ID))
                .retrieve()
                .body(JsonNode.class);
    }

    // 取得中央氣象署站點資料
    public JsonNode getStations() {
        JsonNode response = getWeatherData();

        return response.path("records").path("Station");
    }
}
