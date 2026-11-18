package com.example.smart_order_assistant_system.request;

import com.example.smart_order_assistant_system.entity.Supplier;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SupplierRequest {

    // 供應商名稱
    @NotBlank(message = "供應商名稱不可為空")
    @Size(max = 100, message = "供應商名稱不可超過 100 字")
    private String name;

    // 供應商聯絡人
    @Size(max = 50, message = "聯絡人不可超過 50 字")
    private String contactPerson;

    // 聯絡電話
    @Size(max = 30, message = "電話不可超過 30 字")
    private String phone;

    // 電子郵件
    @Email(message = "電子郵件格式不正確")
    @Size(max = 100, message = "電子郵件不可超過 100 字")
    private String email;

    // 供應商地址
    @Size(max = 255, message = "地址不可超過 255 字")
    private String address;

    // 將 SupplierRequest 轉換成 Supplier Entity
    public Supplier toEntity() {

        Supplier supplier = new Supplier();

        supplier.setName(this.name);
        supplier.setContactPerson(this.contactPerson);
        supplier.setPhone(this.phone);
        supplier.setEmail(this.email);
        supplier.setAddress(this.address);

        return supplier;
    }
}