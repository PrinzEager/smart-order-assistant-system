
package com.example.smart_order_assistant_system.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.smart_order_assistant_system.entity.Product;
import com.example.smart_order_assistant_system.repository.ProductRepository;
import com.example.smart_order_assistant_system.repository.SaleRepository;
import com.example.smart_order_assistant_system.repository.PurchaseOrderRepository;
import com.example.smart_order_assistant_system.response.OrderRecommendationResponse;
import com.example.smart_order_assistant_system.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderRecommendationService {

    // 商品 Repository：負責查詢商品資料
    private final ProductRepository productRepository;

    // 銷售紀錄 Repository：負責統計歷史銷售數量
    private final SaleRepository saleRepository;

    // 訂貨單 Repository：負責統計尚未到貨的訂貨數量
    private final PurchaseOrderRepository purchaseOrderRepository;

    // 商品啟用狀態
    private static final Byte ACTIVE = 1;

    // ==========================================
    // 1. 查詢可用商品
    // ==========================================

    // 查詢所有啟用且尚未軟刪除的商品
    public List<Product> findAvailableProducts() {

        return productRepository.findByStatusAndDeletedAtIsNull(ACTIVE);
    }

    // ==========================================
    // 2. 統計歷史銷售數量
    // ==========================================

    /**
     * 計算指定商品上個月相同期間的銷售總數量。
     *
     * 預估期間的起始日與結束日都包含在統計範圍內。
     *
     * 例如：
     * 預估期間：2026-11-01 ～ 2026-11-07
     * 歷史期間：2026-10-01 ～ 2026-10-07
     *
     * @param productId 商品 ID
     * @param forecastStartDate 預估期間起始日
     * @param forecastEndDate 預估期間結束日
     * @return 歷史銷售總數量
     */
    public Integer calculateHistoricalSalesQuantity(
            Long productId,
            LocalDate forecastStartDate,
            LocalDate forecastEndDate) {

        // 驗證商品 ID
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("商品 ID 必須大於 0");
        }

        // 驗證預估日期區間
        validateForecastDateRange(
                forecastStartDate,
                forecastEndDate
        );

        // 計算預估期間的天數（起始日與結束日都包含）
        long periodDays = ChronoUnit.DAYS.between(
                forecastStartDate,
                forecastEndDate
        ) + 1;

        // 取得預估起始日期的上個月
        YearMonth lastMonth = YearMonth
                .from(forecastStartDate)
                .minusMonths(1);

        // 計算上個月對應的起始日期
        // 若上個月沒有相同日期，則使用該月最後一天
        LocalDate historicalStartDate = lastMonth.atDay(
                Math.min(
                        forecastStartDate.getDayOfMonth(),
                        lastMonth.lengthOfMonth()
                )
        );

        // 計算歷史期間的結束日期
        LocalDate historicalEndDate = historicalStartDate.plusDays(
                periodDays - 1
        );

        // 設定歷史銷售查詢的起始時間
        LocalDateTime startDateTime =
                historicalStartDate.atStartOfDay();

        // 設定歷史銷售查詢的結束時間
        // 使用當天最後一刻，確保結束日期也會被統計
        LocalDateTime endDateTime = historicalEndDate
                .plusDays(1)
                .atStartOfDay()
                .minusNanos(1);

        // 統計指定商品在歷史期間內的銷售總數量
        // Repository 已使用 COALESCE，沒有銷售資料時會回傳 0
        Long totalQuantity =
                saleRepository.sumQuantityByProductAndDateTimeBetween(
                        productId,
                        startDateTime,
                        endDateTime
                );

        // 防止 Repository 未來修改後回傳 null
        return totalQuantity == null
                ? 0
                : Math.toIntExact(totalQuantity);
    }

    // ==========================================
    // 3. 統計尚未到貨數量
    // ==========================================

    /**
     * 計算指定商品目前尚未到貨的訂貨數量。
     *
     * 只統計：
     * 1. 訂貨單狀態為 SUBMITTED
     * 2. 訂貨單資料狀態為正常
     * 3. 訂貨單尚未軟刪除
     *
     * @param productId 商品 ID
     * @return 尚未到貨數量
     */
    public Long calculateOutstandingQuantity(Long productId) {

        // 驗證商品 ID
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("商品 ID 必須大於 0");
        }

        // 從 Repository 取得尚未到貨數量
        Long outstandingQuantity =
                purchaseOrderRepository.calculateOutstandingQuantity(
                        productId
                );

        // 若沒有未到貨紀錄，則回傳 0
        return outstandingQuantity == null
                ? 0L
                : outstandingQuantity;
    }

    // ==========================================
    // 4. 計算建議訂貨數量
    // ==========================================

    /**
     * 計算指定商品的建議訂貨數量。
     *
     * 計算公式：
     *
     * 歷史銷售量 + 安全庫存 - 目前庫存 - 未到貨數量
     *
     * 若計算結果小於 0，則建議訂貨量為 0。
     *
     * @param productId 商品 ID
     * @param forecastStartDate 預估期間起始日
     * @param forecastEndDate 預估期間結束日
     * @return 建議訂貨數量
     */
    public Integer calculateRecommendedQuantity(
            Long productId,
            LocalDate forecastStartDate,
            LocalDate forecastEndDate) {

        // 查詢商品，確認商品存在、啟用且未被刪除
        Product product = findAvailableProduct(productId);

        // 計算歷史相同期間的銷售預估數量
        Integer historicalSalesQuantity =
                calculateHistoricalSalesQuantity(
                        productId,
                        forecastStartDate,
                        forecastEndDate
                );

        // 計算目前已送出但尚未到貨的訂貨數量
        // 將 Repository 回傳的 Long 轉成 DTO 使用的 Integer
        Integer outstandingQuantity = Math.toIntExact(
                calculateOutstandingQuantity(productId)
        );

        // 使用共用方法計算建議訂貨數量
        return calculateRecommendedQuantity(
                product,
                historicalSalesQuantity,
                outstandingQuantity
        );
    }

    /**
     * 共用的建議訂貨數量計算方法。
     *
     * 讓單獨計算訂貨數量與產生完整訂貨建議，
     * 使用相同的計算公式，避免結果不一致。
     *
     * @param product 商品資料
     * @param historicalSalesQuantity 歷史銷售數量
     * @param outstandingQuantity 尚未到貨數量
     * @return 建議訂貨數量
     */
    private Integer calculateRecommendedQuantity(
            Product product,
            Integer historicalSalesQuantity,
            Integer outstandingQuantity) {

        // 取得商品安全庫存
        Integer safetyStock = product.getSafetyStock();

        // 取得商品目前庫存
        Integer currentStock = product.getCurrentStock();

        // 計算可用庫存
        long availableStock =
                currentStock.longValue() + outstandingQuantity.longValue();

        // 計算建議訂貨數量
        long recommendedQuantity =
                historicalSalesQuantity.longValue()
                + safetyStock.longValue()
                - availableStock;

        // 建議訂貨數量不可小於 0
        return Math.toIntExact(
                Math.max(recommendedQuantity, 0L)
        );
    }

    // ==========================================
    // 5. 產生完整訂貨建議
    // ==========================================

    /**
     * 產生指定商品的完整訂貨建議。
     *
     * 整合：
     * 1. 商品基本資料
     * 2. 歷史銷售數量
     * 3. 安全庫存
     * 4. 目前庫存
     * 5. 補貨警戒點
     * 6. 供應商交貨天數
     * 7. 尚未到貨數量
     * 8. 可用庫存
     * 9. 建議訂貨數量
     *
     * @param productId 商品 ID
     * @param forecastStartDate 預估期間起始日
     * @param forecastEndDate 預估期間結束日
     * @return 完整訂貨建議 Response
     */
    public OrderRecommendationResponse generateRecommendation(
            Long productId,
            LocalDate forecastStartDate,
            LocalDate forecastEndDate) {

        // 驗證預估日期區間
        validateForecastDateRange(
                forecastStartDate,
                forecastEndDate
        );

        // 查詢商品，確認商品存在、啟用且未被刪除
        Product product = findAvailableProduct(productId);

        // 計算歷史相同期間的銷售總數量
        Integer historicalSalesQuantity =
                calculateHistoricalSalesQuantity(
                        productId,
                        forecastStartDate,
                        forecastEndDate
                );

        // 取得商品安全庫存
        Integer safetyStock = product.getSafetyStock();

        // 取得商品目前庫存
        Integer currentStock = product.getCurrentStock();

        // 取得商品補貨警戒點
        Integer reorderPoint = product.getReorderPoint();

        // 取得供應商平均交貨天數
        Integer leadTimeDays = product.getLeadTimeDays();

        // 計算目前已送出但尚未到貨的訂貨數量
        // 將 Repository 回傳的 Long 轉成 DTO 使用的 Integer
        Integer outstandingQuantity = Math.toIntExact(
                calculateOutstandingQuantity(productId)
        );

        // 計算可用庫存（目前庫存 + 未到貨數量）
        Integer availableStock = Math.toIntExact(
                currentStock.longValue() + outstandingQuantity
        );

        // 使用共用方法計算建議訂貨數量
        Integer recommendedQuantity =
                calculateRecommendedQuantity(
                        product,
                        historicalSalesQuantity,
                        outstandingQuantity
                );

        // 判斷是否需要補貨
        Boolean needsReorder = recommendedQuantity > 0;

        // 組合完整訂貨建議 Response
        return new OrderRecommendationResponse(
                product.getId(),
                product.getName(),
                product.getSku(),
                currentStock,
                safetyStock,
                reorderPoint,
                leadTimeDays,
                forecastStartDate,
                forecastEndDate,
                historicalSalesQuantity,
                outstandingQuantity,
                availableStock,
                recommendedQuantity,
                needsReorder
        );
    }

    // ==========================================
    // 6. 私有輔助方法
    // ==========================================

    /**
     * 查詢有效商品。
     *
     * 只允許查詢啟用且尚未軟刪除的商品。
     *
     * @param productId 商品 ID
     * @return 有效商品
     */
    private Product findAvailableProduct(Long productId) {

        // 驗證商品 ID
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("商品 ID 必須大於 0");
        }

        // 查詢啟用且尚未軟刪除的商品
        return productRepository
                .findByIdAndStatusAndDeletedAtIsNull(
                        productId,
                        ACTIVE
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "商品不存在、已停售或已刪除"
                        )
                );
    }

    /**
     * 驗證預估日期區間。
     *
     * @param startDate 預估起始日
     * @param endDate 預估結束日
     */
    private void validateForecastDateRange(
            LocalDate startDate,
            LocalDate endDate) {

        // 起始日與結束日都不可為空
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException(
                    "預估期間起始日與結束日不可為空"
            );
        }

        // 起始日不可晚於結束日
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException(
                    "預估期間起始日不可晚於結束日"
            );
        }
    }
}
