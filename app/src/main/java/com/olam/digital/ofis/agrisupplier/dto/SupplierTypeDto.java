package com.olam.digital.ofis.agrisupplier.dto;

import com.google.gson.annotations.SerializedName;

public class SupplierTypeDto {
    @SerializedName("id")
    private Integer supplierTypeId;
    @SerializedName("type")
    private String type;

    public SupplierTypeDto(Integer supplierTypeId, String type) {
        this.supplierTypeId = supplierTypeId;
        this.type = type;
    }

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
