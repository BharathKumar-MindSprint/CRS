package com.olam.digital.ofis.agrisupplier.dto;

import com.google.gson.annotations.SerializedName;
import com.olam.digital.ofis.agrisupplier.entity.SupplierSubmittedModules;

import java.util.List;

public class SupplierSubmittedModuleResponse {
    @SerializedName("supplierModuleData")
    public List<SupplierSubmittedModules> supplierModuleData;

    @SerializedName("errorMessage")
    public String errorMessage;
}
