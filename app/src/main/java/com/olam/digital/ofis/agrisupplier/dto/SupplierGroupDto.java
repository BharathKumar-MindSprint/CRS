package com.olam.digital.ofis.agrisupplier.dto;

import com.google.gson.annotations.SerializedName;

public class SupplierGroupDto {

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

    public SupplierGroupDto(Integer supplierGroupId, String supplierGroupName, Integer countryId, Integer productId, Integer supplierTypeId) {
        this.supplierGroupId = supplierGroupId;
        this.supplierGroupName = supplierGroupName;
        this.countryId = countryId;
        this.productId = productId;
        this.supplierTypeId = supplierTypeId;
    }

    public Integer getSupplierGroupId() {
        return supplierGroupId;
    }

    public void setSupplierGroupId(Integer supplierGroupId) {
        this.supplierGroupId = supplierGroupId;
    }

    public String getSupplierGroupName() {
        return supplierGroupName;
    }

    public void setSupplierGroupName(String supplierGroupName) {
        this.supplierGroupName = supplierGroupName;
    }

    public Integer getCountryId() {
        return countryId;
    }

    public void setCountryId(Integer countryId) {
        this.countryId = countryId;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Integer getSupplierTypeId() {
        return supplierTypeId;
    }

    public void setSupplierTypeId(Integer supplierTypeId) {
        this.supplierTypeId = supplierTypeId;
    }
}
