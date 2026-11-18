package com.example.smart_order_assistant_system.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 自動產生 Getter、Setter
@Getter
@Setter

// 自動產生無參數建構子
@NoArgsConstructor

// 標記為 JPA Entity
@Entity

// 對應 MySQL weather_stations 資料表
@Table(name = "weather_stations")
public class WeatherStation {

    // 測站編號作為主鍵，不使用自動遞增
    @Id
    @Column(name = "station_id", nullable = false)
    private String stationId;

    // 測站名稱
    @Column(name = "station_name", nullable = false)
    private String stationName;

    // 所屬縣市
    @Column(name = "county_name", nullable = false)
    private String countyName;

    // 所屬鄉鎮
    @Column(name = "town_name", nullable = false)
    private String townName;

    // 測站緯度
    @Column(name = "latitude")
    private Double latitude;

    // 測站經度
    @Column(name = "longitude")
    private Double longitude;

    // 是否啟用自動收集，預設為 true
    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;
}