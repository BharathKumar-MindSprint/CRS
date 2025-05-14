package com.olam.digital.ofis.agrisupplier.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.olam.digital.ofis.agrisupplier.dto.SupplierDto;
import com.olam.digital.ofis.agrisupplier.entity.Supplier;
import com.olam.digital.ofis.agrisupplier.entity.SupplierGroup;
import com.olam.digital.ofis.agrisupplier.entity.SupplierGroupWithSuppliers;
import com.olam.digital.ofis.agrisupplier.entity.SupplierSupplierGroupCrossRef;
import com.olam.digital.ofis.agrisupplier.entity.SupplierWithGroups;

import java.util.List;

@Dao
public interface SupplierDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSuppliers(List<Supplier> suppliers);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCrossRefs(List<SupplierSupplierGroupCrossRef> crossRefs);

    @Transaction
    @Query("SELECT * FROM supplier WHERE supplierId = :supplierId")
    SupplierWithGroups getSupplierWithGroups(Long supplierId);

    @Transaction
    @Query("SELECT * FROM supplier_group WHERE supplierGroupId = :groupId")
    SupplierGroupWithSuppliers getGroupWithSuppliers(int groupId);

    @Transaction
    @Query("SELECT DISTINCT s.* FROM supplier s LEFT JOIN SupplierSupplierGroupCrossRef x ON s.id = x.id WHERE ((:query == '' OR (s.supplierName LIKE '%' || :query || '%')) OR (:query == '' OR (s.supplierId LIKE '%' || :query || '%'))) AND (:countryId == 0 OR s.countryId = :countryId) AND (:productId == 0 OR s.productId = :productId) AND (:supplierTypeId == 0 OR s.supplierTypeId = :supplierTypeId) AND (:supplierGroupCount == 0 OR x.supplierGroupId IN (:supplierGroup))")
    List<SupplierDto> getAllSuppliers(Long countryId, Long productId, Long supplierTypeId, List<Integer> supplierGroup, int supplierGroupCount, String query);

    @Query("DELETE FROM supplier")
    void deleteAll();
}
