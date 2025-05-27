package com.olam.digital.ofis.agrisupplier.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.olam.digital.ofis.agrisupplier.dto.SupplierTypeDto;
import com.olam.digital.ofis.agrisupplier.entity.SupplierGroup;
import com.olam.digital.ofis.agrisupplier.entity.SupplierType;

import java.util.List;

@Dao
public interface SupplierTypeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSupplierType(List<SupplierType> supplierTypes);

    @Query("SELECT * FROM supplier_type LIMIT 2")
    List<SupplierTypeDto> getAllSupplierType();

    @Query("SELECT type FROM supplier_type")
    List<String> getAllSupplierTypeNames();

    @Query("DELETE FROM supplier_type")
    void deleteAll();

    @Query("SELECT * FROM supplier_type WHERE supplierTypeId = :supplierTypeId")
    SupplierTypeDto getSupplierTypeNameById(Integer supplierTypeId);
}
