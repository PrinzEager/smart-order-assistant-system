package com.example.smart_order_assistant_system.response;

import java.time.LocalDateTime;

import com.example.smart_order_assistant_system.entity.Supplier;

import lombok.Getter;

@Getter
public class SupplierResponse {

    private final Long id;

    private final String name;

    private final String contactPerson;

    private final String phone;

    private final String email;

    private final String address;

    private final Byte status;

    private final LocalDateTime createdAt;

    private final LocalDateTime updatedAt;

    // 將 Supplier Entity 轉換成 Response
    public SupplierResponse(Supplier supplier) {

        this.id = supplier.getId();
        this.name = supplier.getName();
        this.contactPerson = supplier.getContactPerson();
        this.phone = supplier.getPhone();
        this.email = supplier.getEmail();
        this.address = supplier.getAddress();
        this.status = supplier.getStatus();
        this.createdAt = supplier.getCreatedAt();
        this.updatedAt = supplier.getUpdatedAt();
    }
}
