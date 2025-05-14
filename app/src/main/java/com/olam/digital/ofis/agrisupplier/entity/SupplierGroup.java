package com.olam.digital.ofis.agrisupplier.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "supplier_group")
public class SupplierGroup {
    @PrimaryKey
    @NonNull
    @SerializedName("supplierGroupId")
    public Integer supplierGroupId;
    @SerializedName("supplierGroupName")
    public String supplierGroupName;
    @SerializedName("countryId")
    public Integer countryId;
    @SerializedName("productId")
    public Integer productId;
    @SerializedName("supplierTypeId")
    public Integer supplierTypeId;
}
