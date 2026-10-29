package com.example.smart_order_assistant_system.entity;

import java.time.LocalDateTime;

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
@Table(name = "suppliers")
public class Supplier {

    // 供應商編號（主鍵）
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    // 供應商名稱
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    // 供應商聯絡人
    @Column(name = "contact_person", length = 50)
    private String contactPerson;

    // 聯絡電話
    @Column(name = "phone", length = 30)
    private String phone;

    // 電子郵件
    @Column(name = "email", length = 100)
    private String email;

    // 供應商地址
    @Column(name = "address", length = 255)
    private String address;

    // 資料狀態：0=停用或已刪除，1=啟用
    @Column(name = "status", nullable = false)
    private Byte status = 1;

    // 資料建立時間
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 資料最後更新時間
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 軟刪除時間；NULL 表示未刪除
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}