package com.olam.digital.ofis.agrisupplier.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.olam.digital.ofis.agrisupplier.entity.SupplierSubmittedModules;

import java.util.List;

@Dao
public interface SupplierSubmittedModuleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSubmittedModule(List<SupplierSubmittedModules> supplierSubmittedModules);

    @Insert
    void insert(SupplierSubmittedModules supplierSubmittedModules);

    @Query("DELETE FROM supplier_submitted_modules")
    void deleteAll();

    @Query("DELETE FROM supplier_submitted_modules WHERE module_id = :moduleId AND supplier_id = :supplierId AND sync_status = :syncStatus")
    void deleteSubmittedModule(String moduleId, String supplierId, String syncStatus);

    @Query("SELECT * FROM supplier_submitted_modules WHERE module_id = :moduleId AND supplier_id = :supplierId")
    SupplierSubmittedModules getSubmittedModule(String moduleId, String supplierId);

    @Query("UPDATE supplier_submitted_modules SET sync_status = 'true' WHERE module_id = :moduleId AND supplier_id = :supplierId")
    void updateSyncStatus(String moduleId, String supplierId);
}
