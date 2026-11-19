
package com.example.smart_order_assistant_system.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.smart_order_assistant_system.entity.ProductCategory;
import com.example.smart_order_assistant_system.entity.WeatherDemandRule;
import com.example.smart_order_assistant_system.repository.ProductCategoryRepository;
import com.example.smart_order_assistant_system.repository.WeatherDemandRuleRepository;
import com.example.smart_order_assistant_system.request.WeatherDemandRuleRequest;
import com.example.smart_order_assistant_system.response.WeatherDemandRuleResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WeatherDemandRuleService {

    // 商品分類 Repository
    private final ProductCategoryRepository categoryRepository;

    // 天氣需求規則 Repository
    private final WeatherDemandRuleRepository ruleRepository;

    // 啟用狀態
    private static final Byte ACTIVE = 1;

    // 停用狀態
    private static final Byte INACTIVE = 0;

    // ==========================================
    // 1. 查詢所有有效天氣規則
    // ==========================================

    /**
     * 查詢所有啟用且未軟刪除的天氣需求規則。
     *
     * @return 天氣規則 Response 清單
     */
    public List<WeatherDemandRuleResponse> findAllRules() {

        return ruleRepository
                .findByStatusAndDeletedAtIsNullOrderByIdAsc(ACTIVE)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    // ==========================================
    // 2. 查詢單筆天氣規則
    // ==========================================

    /**
     * 依照規則 ID 查詢有效天氣規則。
     *
     * @param id 天氣規則 ID
     * @return 天氣規則 Response
     */
    public WeatherDemandRuleResponse findRuleById(Long id) {

        WeatherDemandRule rule = findAvailableRule(id);

        return convertToResponse(rule);
    }

    // ==========================================
    // 3. 新增天氣需求規則
    // ==========================================

    /**
     * 新增天氣需求規則。
     *
     * 只允許設定在啟用且未刪除的第二層商品分類。
     *
     * @param request 新增規則資料
     * @return 新增後的規則 Response
     */
    @Transactional
    public WeatherDemandRuleResponse createRule(
            WeatherDemandRuleRequest request) {

        // 驗證輸入資料
        validateRequest(request);

        // 查詢並驗證商品分類
        ProductCategory category =
                findValidCategory(request.getCategoryId());

        // 檢查同一分類是否有重複規則名稱
        validateDuplicateRuleName(
                category.getId(),
                request.getRuleName(),
                null
        );

        // 建立天氣需求規則 Entity
        WeatherDemandRule rule = new WeatherDemandRule();

        // 設定商品分類
        rule.setCategory(category);

        // 設定規則資料
        applyRequest(rule, request);

        // 設定啟用狀態
        rule.setStatus(ACTIVE);

        // 設定建立及更新時間
        LocalDateTime now = LocalDateTime.now();

        rule.setCreatedAt(now);
        rule.setUpdatedAt(now);

        // 新增規則
        WeatherDemandRule savedRule = ruleRepository.save(rule);

        // 回傳新增結果
        return convertToResponse(savedRule);
    }

    // ==========================================
    // 4. 修改天氣需求規則
    // ==========================================

    /**
     * 修改指定天氣需求規則。
     *
     * @param id 天氣規則 ID
     * @param request 修改規則資料
     * @return 修改後的規則 Response
     */
    @Transactional
    public WeatherDemandRuleResponse updateRule(
            Long id,
            WeatherDemandRuleRequest request) {

        // 驗證輸入資料
        validateRequest(request);

        // 查詢原有有效規則
        WeatherDemandRule rule = findAvailableRule(id);

        // 查詢並驗證新的商品分類
        ProductCategory category =
                findValidCategory(request.getCategoryId());

        // 檢查是否與其他規則名稱重複
        validateDuplicateRuleName(
                category.getId(),
                request.getRuleName(),
                id
        );

        // 更新商品分類
        rule.setCategory(category);

        // 更新規則資料
        applyRequest(rule, request);

        // 更新最後修改時間
        rule.setUpdatedAt(LocalDateTime.now());

        // 儲存修改結果
        WeatherDemandRule updatedRule =
                ruleRepository.save(rule);

        // 回傳修改結果
        return convertToResponse(updatedRule);
    }

    // ==========================================
    // 5. 軟刪除天氣需求規則
    // ==========================================

    /**
     * 軟刪除指定天氣需求規則。
     *
     * 不直接刪除資料，而是將狀態設為停用，
     * 並記錄軟刪除時間。
     *
     * @param id 天氣規則 ID
     */
    @Transactional
    public void deleteRule(Long id) {

        // 查詢有效規則
        WeatherDemandRule rule = findAvailableRule(id);

        // 設定為停用
        rule.setStatus(INACTIVE);

        // 記錄軟刪除時間
        rule.setDeletedAt(LocalDateTime.now());

        // 儲存修改
        ruleRepository.save(rule);
    }

    // ==========================================
    // 6. 驗證天氣規則輸入資料
    // ==========================================

    /**
     * 驗證新增及修改的規則資料。
     *
     * @param request 規則 Request
     */
    private void validateRequest(WeatherDemandRuleRequest request) {

        // Request 不可為空
        if (request == null) {
            throw new IllegalArgumentException(
                    "天氣規則資料不可為空"
            );
        }

        // 商品分類 ID 驗證
        if (request.getCategoryId() == null
                || request.getCategoryId() <= 0) {

            throw new IllegalArgumentException(
                    "商品分類 ID 必須大於 0"
            );
        }

        // 規則名稱驗證
        if (request.getRuleName() == null
                || request.getRuleName().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "天氣規則名稱不可為空"
            );
        }

        if (request.getRuleName().trim().length() > 100) {
            throw new IllegalArgumentException(
                    "天氣規則名稱不可超過 100 個字"
            );
        }

        // 天氣指標驗證
        validateWeatherElement(request.getWeatherElement());

        // 條件運算子驗證
        validateOperator(request.getOperator());

        // 條件數值驗證
        if (request.getThresholdValue() == null) {
            throw new IllegalArgumentException(
                    "條件數值不可為空"
            );
        }

        // 區間條件驗證
        if ("BETWEEN".equals(request.getOperator())) {

            // BETWEEN 必須設定上限
            if (request.getUpperThresholdValue() == null) {
                throw new IllegalArgumentException(
                        "使用 BETWEEN 時必須設定區間上限"
                );
            }

            // 上限必須大於下限
            if (request.getUpperThresholdValue()
                    .compareTo(request.getThresholdValue()) <= 0) {

                throw new IllegalArgumentException(
                        "區間上限必須大於條件下限"
                );
            }

        } else {

            // 非 BETWEEN 不允許設定區間上限
            if (request.getUpperThresholdValue() != null) {
                throw new IllegalArgumentException(
                        "只有 BETWEEN 條件可以設定區間上限"
                );
            }
        }

        // 天氣調整係數驗證
        if (request.getAdjustmentFactor() == null
                || request.getAdjustmentFactor()
                        .compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "天氣調整係數必須大於 0"
            );
        }
    }

    // ==========================================
    // 7. 驗證天氣指標
    // ==========================================

    /**
     * 限制可使用的天氣指標。
     *
     * @param weatherElement 天氣指標
     */
    private void validateWeatherElement(String weatherElement) {

        if (weatherElement == null) {
            throw new IllegalArgumentException(
                    "天氣指標不可為空"
            );
        }

        if (!List.of(
                "TEMPERATURE",
                "HUMIDITY",
                "RAIN_PROBABILITY"
        ).contains(weatherElement)) {

            throw new IllegalArgumentException(
                    "不支援的天氣指標"
            );
        }
    }

    // ==========================================
    // 8. 驗證條件運算子
    // ==========================================

    /**
     * 限制可使用的條件運算子。
     *
     * @param operator 條件運算子
     */
    private void validateOperator(String operator) {

        if (operator == null) {
            throw new IllegalArgumentException(
                    "條件運算子不可為空"
            );
        }

        if (!List.of(
                "GT",
                "GTE",
                "LT",
                "LTE",
                "BETWEEN"
        ).contains(operator)) {

            throw new IllegalArgumentException(
                    "不支援的條件運算子"
            );
        }
    }

    // ==========================================
    // 9. 查詢並驗證商品分類
    // ==========================================

    /**
     * 只允許使用啟用且未刪除的第二層商品分類。
     *
     * @param categoryId 商品分類 ID
     * @return 有效商品分類
     */
    private ProductCategory findValidCategory(Long categoryId) {

        // 查詢啟用且未刪除的商品分類
        ProductCategory category = categoryRepository
                .findByIdAndStatusAndDeletedAtIsNull(
                        categoryId,
                        ACTIVE
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "商品分類不存在、已停用或已刪除"
                        )
                );

        // 第二層分類必須具有上層分類
        if (category.getParent() == null) {
            throw new IllegalArgumentException(
                    "只能將天氣需求規則設定在第二層商品分類"
            );
        }

        // 確認上層分類仍然有效
        ProductCategory parent = category.getParent();

        if (!ACTIVE.equals(parent.getStatus())
                || parent.getDeletedAt() != null) {

            throw new IllegalArgumentException(
                    "商品分類的上層分類已停用或已刪除"
            );
        }

        return category;
    }

    // ==========================================
    // 10. 查詢有效天氣規則
    // ==========================================

    /**
     * 查詢啟用且未軟刪除的天氣規則。
     *
     * @param id 天氣規則 ID
     * @return 有效天氣規則
     */
    private WeatherDemandRule findAvailableRule(Long id) {

        // 驗證規則 ID
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "天氣規則 ID 必須大於 0"
            );
        }

        return ruleRepository
                .findByIdAndStatusAndDeletedAtIsNull(
                        id,
                        ACTIVE
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "天氣規則不存在、已停用或已刪除"
                        )
                );
    }

    // ==========================================
    // 11. 檢查規則名稱是否重複
    // ==========================================

    /**
     * 檢查同一商品分類是否有重複規則名稱。
     *
     * @param categoryId 商品分類 ID
     * @param ruleName 規則名稱
     * @param excludeId 修改時排除的規則 ID
     */
    private void validateDuplicateRuleName(
            Long categoryId,
            String ruleName,
            Long excludeId) {

        String normalizedName = ruleName.trim();

        boolean exists;

        // 修改時排除目前規則本身
        if (excludeId == null) {

            exists = ruleRepository
                    .existsByCategory_IdAndRuleNameAndDeletedAtIsNull(
                            categoryId,
                            normalizedName
                    );

        } else {

            exists = ruleRepository
                    .existsByCategory_IdAndRuleNameAndDeletedAtIsNullAndIdNot(
                            categoryId,
                            normalizedName,
                            excludeId
                    );
        }

        if (exists) {
            throw new IllegalArgumentException(
                    "此商品分類已存在相同名稱的天氣規則"
            );
        }
    }

    // ==========================================
    // 12. 將 Request 資料設定至 Entity
    // ==========================================

    /**
     * 將 Request 欄位設定至 Entity。
     *
     * @param rule 天氣規則 Entity
     * @param request Request DTO
     */
    private void applyRequest(
            WeatherDemandRule rule,
            WeatherDemandRuleRequest request) {

        rule.setRuleName(request.getRuleName().trim());

        rule.setWeatherElement(request.getWeatherElement());

        rule.setOperator(request.getOperator());

        rule.setThresholdValue(request.getThresholdValue());

        rule.setUpperThresholdValue(
                request.getUpperThresholdValue()
        );

        rule.setAdjustmentFactor(
                request.getAdjustmentFactor()
        );
    }

    // ==========================================
    // 13. Entity 轉換為 Response
    // ==========================================

    /**
     * 將 Entity 轉換為 API Response。
     *
     * @param rule 天氣規則 Entity
     * @return 天氣規則 Response
     */
    private WeatherDemandRuleResponse convertToResponse(
            WeatherDemandRule rule) {

        return new WeatherDemandRuleResponse(
                rule.getId(),
                rule.getCategory().getId(),
                rule.getCategory().getName(),
                rule.getRuleName(),
                rule.getWeatherElement(),
                rule.getOperator(),
                rule.getThresholdValue(),
                rule.getUpperThresholdValue(),
                rule.getAdjustmentFactor(),
                rule.getStatus(),
                rule.getCreatedAt(),
                rule.getUpdatedAt()
        );
    }
}
