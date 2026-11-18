package com.example.smart_order_assistant_system.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.smart_order_assistant_system.entity.WeatherStation;

// 繼承 JpaRepository，取得基本 CRUD 功能
public interface WeatherStationRepository
        extends JpaRepository<WeatherStation, String> {

    // 查詢所有已啟用的測站
    List<WeatherStation> findByEnabledTrue();

}