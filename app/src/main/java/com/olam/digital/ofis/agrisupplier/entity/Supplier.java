package com.olam.digital.ofis.agrisupplier.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "supplier")
public class Supplier {
    @PrimaryKey
    @NonNull
    @SerializedName("id")
    public Integer id;

    @SerializedName("supplierId")
    public Long supplierId;
    @SerializedName("supplierName")
    public String supplierName;
    @SerializedName("primaryPhoneNumber")
    public String primaryPhoneNumber;
    @SerializedName("secondaryPhoneNumber")
    public String secondaryPhoneNumber;
    @SerializedName("supplierTypeId")
    public Integer supplierTypeId;
    @SerializedName("countryId")
    public Integer countryId;
    @SerializedName("productId")
    public Integer productId;

}
