package com.olam.digital.ofis.agrisupplier.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.olam.digital.ofis.agrisupplier.dto.SupplierDto;
import com.olam.digital.ofis.agrisupplier.entity.Supplier;

import java.util.List;

@Dao
public interface SupplierDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSuppliers(List<Supplier> suppliers);

    @Transaction
    @Query("SELECT DISTINCT * FROM supplier WHERE ((:query == '' OR (supplierName LIKE '%' || :query || '%')) OR (:query == '' OR (supplierId LIKE '%' || :query || '%'))) AND (:countryId == 0 OR countryId = :countryId) AND (:productId == 0 OR productId = :productId) AND (:supplierTypeId == 0 OR supplierTypeId = :supplierTypeId) AND (:supplierGroupCount == 0 OR supplierGroupId IN (:supplierGroup))")
    List<SupplierDto> getAllSuppliers(Long countryId, Long productId, Long supplierTypeId, List<Integer> supplierGroup, int supplierGroupCount, String query);

    @Query("DELETE FROM supplier")
    void deleteAll();

    @Query("SELECT * FROM supplier WHERE id = :id")
    Supplier getSupplierById(String id);
}
