package com.olam.digital.ofis.agrisupplier.dto;

import com.google.gson.annotations.SerializedName;
import com.olam.digital.ofis.agrisupplier.entity.Supplier;
import com.olam.digital.ofis.agrisupplier.entity.SupplierGroup;
import com.olam.digital.ofis.agrisupplier.entity.SupplierType;

import java.util.List;

public class SupplierResponse {
    @SerializedName("suppliers")
    private List<Supplier> suppliers;

    @SerializedName("supplierMappings")
    private List<SupplierMappingDto> supplierMapping;

    @SerializedName("supplierType")
    private List<SupplierType> supplierType;

    @SerializedName("supplierGroup")
    private List<SupplierGroup> supplierGroup;
    @SerializedName("errorMessage")
    private String errorMessage;

    public SupplierResponse(List<Supplier> suppliers, List<SupplierMappingDto> supplierMappings, List<SupplierType> supplierType, List<SupplierGroup> supplierGroup, String errorMessage) {
        this.suppliers = suppliers;
        supplierMapping = supplierMappings;
        this.supplierType = supplierType;
        this.supplierGroup = supplierGroup;
        this.errorMessage = errorMessage;
    }

    public List<Supplier> getSuppliers() {
        return suppliers;
    }

    public void setSuppliers(List<Supplier> suppliers) {
        this.suppliers = suppliers;
    }

    public List<SupplierMappingDto> getSupplierGroupMapping() {
        return supplierMapping;
    }

    public void setSupplierGroupMapping(List<SupplierMappingDto> supplierMapping) {
        supplierMapping = supplierMapping;
    }

    public List<SupplierType> getSupplierType() {
        return supplierType;
    }

    public void setSupplierType(List<SupplierType> supplierType) {
        this.supplierType = supplierType;
    }

    public List<SupplierGroup> getSupplierGroup() {
        return supplierGroup;
    }

    public void setSupplierGroup(List<SupplierGroup> supplierGroup) {
        this.supplierGroup = supplierGroup;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
