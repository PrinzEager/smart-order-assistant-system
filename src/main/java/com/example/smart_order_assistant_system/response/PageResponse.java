package com.example.smart_order_assistant_system.response;

import java.util.List;

import lombok.Getter;

@Getter
public class PageResponse<T> {

    // 當前頁面的資料
    private List<T> content;

    // 目前頁碼
    private int page;

    // 每頁筆數
    private int size;

    // 總資料筆數
    private long totalElements;

    // 總頁數
    private long totalPages;

    public PageResponse(
            List<T> content,
            int page,
            int size,
            long totalElements) {

        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;

        // 計算總頁數
        this.totalPages = (long) Math.ceil(
                (double) totalElements / size
        );
    }
}