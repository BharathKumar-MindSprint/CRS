package com.olam.digital.ofis.agrisupplier.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.olam.digital.ofis.agrisupplier.entity.SupplierGroup;
import com.olam.digital.ofis.model.Information;

import java.util.List;
import java.util.Objects;

@Dao
public interface SupplierGroupDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSupplierGroups(List<SupplierGroup> groups);

    @Query("SELECT supplierGroupId as id, supplierGroupName as name  FROM supplier_group WHERE (:supplierTypeId == 0 OR supplierTypeId = :supplierTypeId)")
    List<Information> getSupplierGroups(Long supplierTypeId);


    @Query("DELETE FROM supplier_group")
    void deleteAll();
}
