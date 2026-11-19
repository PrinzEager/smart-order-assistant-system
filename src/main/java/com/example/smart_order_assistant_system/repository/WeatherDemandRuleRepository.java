package com.example.smart_order_assistant_system.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.smart_order_assistant_system.entity.WeatherDemandRule;

@Repository
public interface WeatherDemandRuleRepository
        extends JpaRepository<WeatherDemandRule, Long> {

    // 商品分類啟用狀態
    Byte ACTIVE = 1;

    // ==========================================
    // 1. 查詢指定分類的有效天氣規則
    // ==========================================

    /**
     * 查詢指定商品分類底下所有啟用且未軟刪除的天氣規則。
     *
     * @param categoryId 商品分類 ID
     * @param status 資料狀態
     * @return 天氣規則清單
     */
    List<WeatherDemandRule>
    findByCategory_IdAndStatusAndDeletedAtIsNullOrderByIdAsc(
            Long categoryId,
            Byte status
    );

    // ==========================================
    // 2. 依照分類及天氣指標查詢規則
    // ==========================================

    /**
     * 查詢指定分類及天氣指標的有效規則。
     *
     * 例如：
     * categoryId = 2
     * weatherElement = TEMPERATURE
     *
     * @param categoryId 商品分類 ID
     * @param weatherElement 天氣指標
     * @param status 資料狀態
     * @return 符合條件的天氣規則清單
     */
    List<WeatherDemandRule>
    findByCategory_IdAndWeatherElementAndStatusAndDeletedAtIsNull(
            Long categoryId,
            String weatherElement,
            Byte status
    );

    // ==========================================
    // 3. 查詢指定 ID 的有效天氣規則
    // ==========================================

    /**
     * 查詢指定編號且啟用、未軟刪除的天氣規則。
     *
     * @param id 天氣規則 ID
     * @param status 資料狀態
     * @return 天氣規則 Optional
     */
    Optional<WeatherDemandRule>
    findByIdAndStatusAndDeletedAtIsNull(
            Long id,
            Byte status
    );

    // ==========================================
    // 4. 檢查分類是否存在相同規則名稱
    // ==========================================

    /**
     * 檢查同一分類是否已經存在相同名稱的規則。
     *
     * 停用規則仍視為已存在。
     * 已軟刪除的規則不列入檢查。
     *
     * @param categoryId 商品分類 ID
     * @param ruleName 規則名稱
     * @return true 表示已存在
     */
    boolean existsByCategory_IdAndRuleNameAndDeletedAtIsNull(
            Long categoryId,
            String ruleName
    );

    // ==========================================
    // 5. 修改規則時檢查重複名稱
    // ==========================================

    /**
     * 檢查同一分類是否有其他規則使用相同名稱。
     *
     * 排除目前正在修改的規則 ID。
     *
     * @param categoryId 商品分類 ID
     * @param ruleName 規則名稱
     * @param id 目前修改的規則 ID
     * @return true 表示存在其他相同名稱的規則
     */
    boolean existsByCategory_IdAndRuleNameAndDeletedAtIsNullAndIdNot(
            Long categoryId,
            String ruleName,
            Long id
    );

    // 查詢所有啟用且尚未軟刪除的天氣規則
    List<WeatherDemandRule> findByStatusAndDeletedAtIsNullOrderByIdAsc(
            Byte status
    );
}
