package com.example.smart_order_assistant_system.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.smart_order_assistant_system.entity.WeatherRecord;

public interface WeatherRecordRepository
        extends JpaRepository<WeatherRecord, Long> {

    // 查詢指定日期的所有天氣紀錄
    List<WeatherRecord> findByRecordDate(LocalDate recordDate);

    // 查詢指定日期區間的天氣紀錄
    List<WeatherRecord> findByRecordDateBetween(
            LocalDate startDate,
            LocalDate endDate
    );

    // 查詢指定地點的所有天氣紀錄
    List<WeatherRecord> findByLocation(String location);

    // 查詢指定地點、指定日期的天氣紀錄
    List<WeatherRecord> findByLocationAndRecordDate(
            String location,
            LocalDate recordDate
    );

    // 查詢指定地點在指定日期區間內的天氣紀錄
    List<WeatherRecord> findByLocationAndRecordDateBetween(
            String location,
            LocalDate startDate,
            LocalDate endDate
    );

    // 依地點模糊搜尋，不區分英文大小寫
    List<WeatherRecord> findByLocationContainingIgnoreCase(
            String location
    );

    // 查詢指定日期及地點的最新建立紀錄
    Optional<WeatherRecord> findFirstByRecordDateAndLocationOrderByCreatedAtDesc(
            LocalDate recordDate,
            String location
    );
}