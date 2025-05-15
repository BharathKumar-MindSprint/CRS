package com.olam.digital.ofis.agrisupplier.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SupplierMappingDto {
    @SerializedName("id")
    public Long supplierId;

    @SerializedName("supplierGroupIds")
    public List<Integer> supplierGroupIds;
}
