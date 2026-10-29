package com.example.smart_order_assistant_system.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "weather_records")
public class WeatherRecord {

    // 天氣紀錄編號（主鍵）
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    // 天氣紀錄日期
    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    // 天氣紀錄時間
    @Column(name = "record_time", nullable = false)
    private LocalTime recordTime = LocalTime.NOON;

    // 氣溫，單位為攝氏度
    @Column(name = "temperature", precision = 5, scale = 2)
    private BigDecimal temperature;

    // 相對濕度，單位為百分比
    @Column(name = "humidity", precision = 5, scale = 2)
    private BigDecimal humidity;

    // 天氣狀況，例如晴天、多雲、下雨
    @Column(name = "weather_condition", length = 50)
    private String weatherCondition;

    // 降雨量，單位為毫米
    @Column(name = "rainfall", precision = 8, scale = 2)
    private BigDecimal rainfall;

    // 天氣觀測地點
    @Column(name = "location", nullable = false, length = 100)
    private String location;

    // 天氣資料建立時間
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}