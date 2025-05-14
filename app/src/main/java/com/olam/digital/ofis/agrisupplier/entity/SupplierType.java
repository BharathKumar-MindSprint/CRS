package com.olam.digital.ofis.agrisupplier.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "supplier_type")
public class SupplierType {

    @PrimaryKey
    @NonNull
    @SerializedName("id")
    private Integer supplierTypeId;
    @SerializedName("type")
    private String type;

    public Integer getSupplierTypeId() {
        return supplierTypeId;
    }

    public void setSupplierTypeId(Integer supplierTypeId) {
        this.supplierTypeId = supplierTypeId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
