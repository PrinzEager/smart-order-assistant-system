package com.example.smart_order_assistant_system.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.smart_order_assistant_system.entity.Supplier;
import com.example.smart_order_assistant_system.repository.SupplierRepository;

@Service
@Transactional(readOnly = true)
public class SupplierService {

    private final SupplierRepository supplierRepository;

    // 建構子注入 Repository
    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    // ==============================
    // 1. 查詢功能
    // ==============================

    // 查詢所有啟用且未刪除的供應商
    public List<Supplier> findAllSuppliers() {
        return supplierRepository
                .findByStatusAndDeletedAtIsNull((byte) 1);
    }

    // 依供應商 ID 查詢未刪除的供應商
    public Supplier findSupplierById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("供應商 ID 不可為空");
        }

        return supplierRepository.findByIdAndDeletedAtIsNull(id)
                .filter(this::isAvailable)
                .orElseThrow(() ->
                        new IllegalArgumentException("找不到啟用中的供應商"));
    }

    // 依供應商名稱模糊搜尋
    public List<Supplier> searchSuppliersByName(String name) {
        if (name == null || name.isBlank()) {
            return findAllSuppliers();
        }

        return supplierRepository
                .findByNameContainingIgnoreCase(name.trim())
                .stream()
                .filter(this::isAvailable)
                .toList();
    }

    // 依電話查詢供應商
    public List<Supplier> findSuppliersByPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("電話不可為空");
        }

        return supplierRepository.findByPhone(phone.trim())
                .stream()
                .filter(this::isAvailable)
                .toList();
    }

    // 依電子郵件查詢供應商
    public Supplier findSupplierByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("電子郵件不可為空");
        }

        return supplierRepository.findByEmail(email.trim())
                .filter(this::isAvailable)
                .orElseThrow(() ->
                        new IllegalArgumentException("找不到符合的供應商"));
    }

    // ==============================
    // 2. 新增功能
    // ==============================

    @Transactional
    public Supplier createSupplier(Supplier supplier) {
        validateSupplier(supplier);

        String email = normalizeOptionalText(supplier.getEmail());

        // 目前 Repository 的 findByEmail 回傳 Optional，
        // 因此這裡假設 email 不可重複。
        if (email != null
                && supplierRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("此電子郵件已被使用");
        }

        LocalDateTime now = LocalDateTime.now();

        supplier.setName(supplier.getName().trim());
        supplier.setContactPerson(
                normalizeOptionalText(supplier.getContactPerson()));
        supplier.setPhone(normalizeOptionalText(supplier.getPhone()));
        supplier.setEmail(email);
        supplier.setAddress(normalizeOptionalText(supplier.getAddress()));

        supplier.setStatus((byte) 1);
        supplier.setDeletedAt(null);
        supplier.setCreatedAt(now);
        supplier.setUpdatedAt(now);

        return supplierRepository.save(supplier);
    }

    // ==============================
    // 3. 修改功能
    // ==============================

    @Transactional
    public Supplier updateSupplier(Long id, Supplier newData) {
        validateSupplier(newData);

        // 先確認原供應商存在且仍啟用
        Supplier existing = findSupplierById(id);

        String email = normalizeOptionalText(newData.getEmail());

        // 檢查電子郵件是否已被其他供應商使用
        if (email != null) {
            supplierRepository.findByEmail(email)
                    .filter(other -> !other.getId().equals(existing.getId()))
                    .ifPresent(other -> {
                        throw new IllegalArgumentException(
                                "此電子郵件已被其他供應商使用");
                    });
        }

        existing.setName(newData.getName().trim());
        existing.setContactPerson(
                normalizeOptionalText(newData.getContactPerson()));
        existing.setPhone(normalizeOptionalText(newData.getPhone()));
        existing.setEmail(email);
        existing.setAddress(normalizeOptionalText(newData.getAddress()));
        existing.setUpdatedAt(LocalDateTime.now());

        return supplierRepository.save(existing);
    }

    // ==============================
    // 4. 停用功能
    // ==============================

    @Transactional
    public Supplier deactivateSupplier(Long id) {
        Supplier supplier = findSupplierById(id);

        supplier.setStatus((byte) 0);
        supplier.setUpdatedAt(LocalDateTime.now());

        return supplierRepository.save(supplier);
    }

    // ==============================
    // 5. 軟刪除功能
    // ==============================

    @Transactional
    public Supplier deleteSupplier(Long id) {
        Supplier supplier = findSupplierById(id);

        LocalDateTime now = LocalDateTime.now();

        supplier.setStatus((byte) 0);
        supplier.setDeletedAt(now);
        supplier.setUpdatedAt(now);

        return supplierRepository.save(supplier);
    }

    // ==============================
    // 6. 私有輔助方法
    // ==============================

    // 判斷供應商是否啟用且未刪除
    private boolean isAvailable(Supplier supplier) {
        return supplier.getStatus() != null
                && supplier.getStatus() == 1
                && supplier.getDeletedAt() == null;
    }

    // 驗證供應商資料
    private void validateSupplier(Supplier supplier) {
        if (supplier == null) {
            throw new IllegalArgumentException("供應商資料不可為空");
        }

        if (supplier.getName() == null
                || supplier.getName().isBlank()) {
            throw new IllegalArgumentException("供應商名稱不可為空");
        }

        if (supplier.getName().trim().length() > 100) {
            throw new IllegalArgumentException("供應商名稱不可超過 100 字");
        }

        if (supplier.getContactPerson() != null
                && supplier.getContactPerson().trim().length() > 50) {
            throw new IllegalArgumentException("聯絡人不可超過 50 字");
        }

        if (supplier.getPhone() != null
                && supplier.getPhone().trim().length() > 30) {
            throw new IllegalArgumentException("電話不可超過 30 字");
        }

        if (supplier.getEmail() != null
                && supplier.getEmail().trim().length() > 100) {
            throw new IllegalArgumentException("電子郵件不可超過 100 字");
        }

        if (supplier.getAddress() != null
                && supplier.getAddress().trim().length() > 255) {
            throw new IllegalArgumentException("地址不可超過 255 字");
        }
    }

    // 將空白字串轉成 null，並移除前後空白
    private String normalizeOptionalText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}