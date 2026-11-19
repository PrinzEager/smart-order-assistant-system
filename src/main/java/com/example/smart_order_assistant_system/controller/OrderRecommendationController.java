package com.example.smart_order_assistant_system.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.smart_order_assistant_system.entity.Product;
import com.example.smart_order_assistant_system.response.OrderRecommendationResponse;
import com.example.smart_order_assistant_system.service.OrderRecommendationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/order-recommendations")
public class OrderRecommendationController {

    // 訂貨建議 Service：負責執行訂貨建議計算
    private final OrderRecommendationService orderRecommendationService;

    // 查詢所有啟用商品的訂貨建議
    @GetMapping
    public List<OrderRecommendationResponse> findAllRecommendations(

            // 預估需求起始日期
            @RequestParam
            @DateTimeFormat(iso = ISO.DATE)
            LocalDate forecastStartDate,

            // 預估需求結束日期
            @RequestParam
            @DateTimeFormat(iso = ISO.DATE)
            LocalDate forecastEndDate
    ) {

        // 檢查日期區間是否正確
        validateForecastDates(forecastStartDate, forecastEndDate);

        // 查詢所有啟用且未軟刪除的商品
        List<Product> products =
                orderRecommendationService.findAvailableProducts();

        // 建立訂貨建議結果集合
        List<OrderRecommendationResponse> recommendations =
                new ArrayList<>();

        // 逐一計算每項商品的訂貨建議
        for (Product product : products) {

            // 產生單一商品的完整訂貨建議
            OrderRecommendationResponse recommendation =
                    orderRecommendationService.generateRecommendation(
                            product.getId(),
                            forecastStartDate,
                            forecastEndDate
                    );

            // 加入結果集合
            recommendations.add(recommendation);
        }

        // 回傳所有商品的訂貨建議
        return recommendations;
    }

    // 查詢指定商品的訂貨建議
    @GetMapping("/{productId}")
    public OrderRecommendationResponse findRecommendationByProductId(

            // 商品 ID
            @PathVariable Long productId,

            // 預估需求起始日期
            @RequestParam
            @DateTimeFormat(iso = ISO.DATE)
            LocalDate forecastStartDate,

            // 預估需求結束日期
            @RequestParam
            @DateTimeFormat(iso = ISO.DATE)
            LocalDate forecastEndDate
    ) {

        // 檢查日期區間是否正確
        validateForecastDates(forecastStartDate, forecastEndDate);

        // 計算並回傳指定商品的訂貨建議
        return orderRecommendationService.generateRecommendation(
                productId,
                forecastStartDate,
                forecastEndDate
        );
    }

    // 檢查預估需求日期區間
    private void validateForecastDates(
            LocalDate forecastStartDate,
            LocalDate forecastEndDate
    ) {

        // 結束日期不可早於起始日期
        if (forecastEndDate.isBefore(forecastStartDate)) {
            throw new IllegalArgumentException(
                    "預估結束日期不可早於預估起始日期"
            );
        }
    }
}
