package com.example.smart_order_assistant_system.constant;

/*
 * 訂貨單狀態常數
 */
public final class OrderStatus {

    // 訂貨單狀態代碼
    public static final String DRAFT = "DRAFT";
    public static final String SUBMITTED = "SUBMITTED";
    public static final String RECEIVED = "RECEIVED";
    public static final String CANCELLED = "CANCELLED";

    // 不允許建立此類別的物件
    private OrderStatus() {
    }

    /*
     * 將英文狀態轉換為中文名稱
     */
    public static String getChineseName(String status) {
        if (status == null) {
            return "未知狀態";
        }

        return switch (status) {
            case DRAFT -> "草稿";
            case SUBMITTED -> "已送出";
            case RECEIVED -> "已到貨";
            case CANCELLED -> "已取消";
            default -> "未知狀態";
        };
    }
}