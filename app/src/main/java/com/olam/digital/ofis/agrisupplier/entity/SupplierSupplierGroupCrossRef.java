package com.olam.digital.ofis.agrisupplier.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(primaryKeys = {"id", "supplierGroupId"},
        foreignKeys = {
                @ForeignKey(entity = Supplier.class,
                        parentColumns = "id",
                        childColumns = "id",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = SupplierGroup.class,
                        parentColumns = "supplierGroupId",
                        childColumns = "supplierGroupId",
                        onDelete = ForeignKey.CASCADE)
        },
        indices = {@Index("supplierGroupId"), @Index("id")})
public class SupplierSupplierGroupCrossRef {
    @NonNull
    public Long id;
    @NonNull
    public Integer supplierGroupId;

    public SupplierSupplierGroupCrossRef(Long id, Integer supplierGroupId) {
        this.id = id;
        this.supplierGroupId = supplierGroupId;
    }
}
