package com.example.smart_order_assistant_system.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.smart_order_assistant_system.entity.Supplier;
import com.example.smart_order_assistant_system.exception.BusinessConflictException;
import com.example.smart_order_assistant_system.exception.ResourceNotFoundException;
import com.example.smart_order_assistant_system.repository.SupplierRepository;

@Service
@Transactional(readOnly = true)
public class SupplierService {

    // 供應商狀態常數
    private static final byte ACTIVE = 1;
    private static final byte INACTIVE = 0;

    private final SupplierRepository supplierRepository;

    // 建構子注入 Repository
    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    // ==========================================
    // 1. 查詢功能
    // ==========================================

    // 查詢所有啟用且未刪除的供應商
    public List<Supplier> findAllSuppliers() {
        return supplierRepository
                .findByStatusAndDeletedAtIsNull(ACTIVE);
    }

    // 依供應商 ID 查詢啟用且未刪除的供應商
    public Supplier findSupplierById(Long id) {

        if (id == null) {
            throw new IllegalArgumentException("供應商 ID 不可為空");
        }

        return supplierRepository.findByIdAndDeletedAtIsNull(id)
                .filter(this::isAvailable)
                .orElseThrow(() ->
                        new ResourceNotFoundException("找不到啟用中的供應商"));
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

        return supplierRepository
                .findByEmailAndDeletedAtIsNull(email.trim())
                .filter(this::isAvailable)
                .orElseThrow(() ->
                        new ResourceNotFoundException("找不到符合的供應商"));
    }

    // 分頁查詢所有啟用且未刪除的供應商
    public Page<Supplier> findAllSuppliers(Pageable pageable) {

        return supplierRepository.findByStatusAndDeletedAtIsNull(
                ACTIVE,
                pageable
        );
    }

    // 分頁搜尋啟用且未刪除的供應商
    public Page<Supplier> searchSuppliers(
            String keyword,
            Pageable pageable) {

        if (keyword == null || keyword.isBlank()) {
            return findAllSuppliers(pageable);
        }

        return supplierRepository
                .findByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNull(
                        keyword.trim(),
                        ACTIVE,
                        pageable
                );
    }

    // ==========================================
    // 2. 新增功能
    // ==========================================

    @Transactional
    public Supplier createSupplier(Supplier supplier) {

        // 驗證供應商資料
        validateSupplier(supplier);

        String email = normalizeOptionalText(supplier.getEmail());

        // 檢查電子郵件是否已被其他未刪除的供應商使用
        // 已軟刪除的供應商不再佔用原本的電子郵件
        if (email != null
                && supplierRepository
                        .findByEmailAndDeletedAtIsNull(email)
                        .isPresent()) {

            throw new BusinessConflictException("此電子郵件已被使用");
        }

        LocalDateTime now = LocalDateTime.now();

        // 正規化供應商資料，移除前後空白
        supplier.setName(supplier.getName().trim());

        supplier.setContactPerson(
                normalizeOptionalText(supplier.getContactPerson()));

        supplier.setPhone(
                normalizeOptionalText(supplier.getPhone()));

        supplier.setEmail(email);

        supplier.setAddress(
                normalizeOptionalText(supplier.getAddress()));

        // 新增供應商預設為啟用狀態
        supplier.setStatus(ACTIVE);

        // 新增資料不應有刪除時間
        supplier.setDeletedAt(null);

        // 設定建立時間及更新時間
        supplier.setCreatedAt(now);
        supplier.setUpdatedAt(now);

        return supplierRepository.save(supplier);
    }

    // ==========================================
    // 3. 修改功能
    // ==========================================

    @Transactional
    public Supplier updateSupplier(Long id, Supplier newData) {

        // 驗證新供應商資料
        validateSupplier(newData);

        // 確認原供應商存在、啟用且未刪除
        Supplier existing = findSupplierById(id);

        String email = normalizeOptionalText(newData.getEmail());

        // 檢查是否有其他未刪除的供應商使用相同電子郵件
        // 排除目前正在修改的供應商
        if (email != null
                && supplierRepository
                        .findByEmailAndDeletedAtIsNullAndIdNot(
                                email,
                                existing.getId()
                        )
                        .isPresent()) {

            throw new BusinessConflictException(
                    "此電子郵件已被其他供應商使用"
            );
        }

        // 更新供應商基本資料
        existing.setName(newData.getName().trim());

        existing.setContactPerson(
                normalizeOptionalText(newData.getContactPerson()));

        existing.setPhone(
                normalizeOptionalText(newData.getPhone()));

        existing.setEmail(email);

        existing.setAddress(
                normalizeOptionalText(newData.getAddress()));

        // 更新最後修改時間
        existing.setUpdatedAt(LocalDateTime.now());

        // 儲存修改後的供應商資料
        return supplierRepository.save(existing);
    }

    // ==========================================
    // 4. 停用功能
    // ==========================================

    @Transactional
    public Supplier deactivateSupplier(Long id) {

        // 查詢未刪除的供應商，不限定目前狀態
        Supplier supplier = findNotDeletedSupplier(id);

        // 已停用的供應商不可重複停用
        if (supplier.getStatus() != null
                && supplier.getStatus() == INACTIVE) {

            throw new BusinessConflictException(
                    "供應商已停用，無須重複停用"
            );
        }

        // 更新為停用狀態
        supplier.setStatus(INACTIVE);
        supplier.setUpdatedAt(LocalDateTime.now());

        return supplierRepository.save(supplier);
    }

    // ==========================================
    // 5. 軟刪除功能
    // ==========================================

    @Transactional
    public Supplier deleteSupplier(Long id) {

        // 查詢尚未軟刪除的供應商
        // 不限制啟用或停用，因此停用中的供應商也可以刪除
        Supplier supplier = findNotDeletedSupplier(id);

        LocalDateTime now = LocalDateTime.now();

        // 更新為停用狀態
        supplier.setStatus(INACTIVE);

        // 設定軟刪除時間
        supplier.setDeletedAt(now);

        // 更新最後修改時間
        supplier.setUpdatedAt(now);

        return supplierRepository.save(supplier);
    }

    // ==========================================
    // 6. 啟用功能
    // ==========================================

    @Transactional
    public Supplier activateSupplier(Long id) {

        // 查詢尚未軟刪除的供應商
        Supplier supplier = findNotDeletedSupplier(id);

        // 已啟用的供應商不可重複啟用
        if (supplier.getStatus() != null
                && supplier.getStatus() == ACTIVE) {

            throw new BusinessConflictException(
                    "供應商已啟用，無須重複啟用"
            );
        }

        // 更新為啟用狀態
        supplier.setStatus(ACTIVE);
        supplier.setUpdatedAt(LocalDateTime.now());

        return supplierRepository.save(supplier);
    }

    // ==========================================
    // 7. 私有輔助方法
    // ==========================================

    // 查詢尚未軟刪除的供應商，不限制啟用或停用狀態
    private Supplier findNotDeletedSupplier(Long id) {

        if (id == null) {
            throw new IllegalArgumentException("供應商 ID 不可為空");
        }

        return supplierRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("找不到供應商"));
    }

    // 判斷供應商是否啟用且未軟刪除
    private boolean isAvailable(Supplier supplier) {

        return supplier.getStatus() != null
                && supplier.getStatus() == ACTIVE
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
