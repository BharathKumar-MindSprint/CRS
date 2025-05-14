package com.olam.digital.ofis.agrisupplier.entity;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import java.util.List;

public class SupplierGroupWithSuppliers {
    @Embedded
    public SupplierGroup supplierGroup;

    @Relation(
            parentColumn = "supplierGroupId",
            entityColumn = "id",
            associateBy = @Junction(SupplierSupplierGroupCrossRef.class)
    )
    public List<Supplier> suppliers;
}
