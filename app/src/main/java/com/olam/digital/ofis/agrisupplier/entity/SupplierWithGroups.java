package com.olam.digital.ofis.agrisupplier.entity;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import java.util.List;

public class SupplierWithGroups {
    @Embedded
    public Supplier supplier;

    @Relation(
            parentColumn = "id",
            entityColumn = "supplierGroupId",
            associateBy = @Junction(SupplierSupplierGroupCrossRef.class)
    )
    public List<SupplierGroup> supplierGroups;
}
