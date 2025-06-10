package com.olam.digital.ofis.agrisupplier.dto;

import com.google.gson.annotations.SerializedName;

public class SupplierTypeDto {
    @SerializedName("id")
    private Integer supplierTypeId;
    @SerializedName("type")
    private String type;

    @SerializedName("type_es")
    private String typeEs;

    @SerializedName("type_fr")
    private String typeFr;

    @SerializedName("type_id")
    private String typeId;

    @SerializedName("type_in")
    private String typeIn;

    @SerializedName("type_lo")
    private String typeLo;

    @SerializedName("type_pt")
    private String typePt;

    @SerializedName("type_th")
    private String typeTh;

    @SerializedName("type_tr")
    private String typeTr;

    @SerializedName("type_vi")
    private String typeVi;

    public SupplierTypeDto(Integer supplierTypeId, String type, String typeEs, String typeFr, String typeId, String typeIn, String typeLo, String typePt, String typeTh, String typeTr, String typeVi) {
        this.supplierTypeId = supplierTypeId;
        this.type = type;
        this.typeEs = typeEs;
        this.typeFr = typeFr;
        this.typeId = typeId;
        this.typeIn = typeIn;
        this.typeLo = typeLo;
        this.typePt = typePt;
        this.typeTh = typeTh;
        this.typeTr = typeTr;
        this.typeVi = typeVi;
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

    public String getTypeEs() {
        return typeEs;
    }

    public void setTypeEs(String typeEs) {
        this.typeEs = typeEs;
    }

    public String getTypeFr() {
        return typeFr;
    }

    public void setTypeFr(String typeFr) {
        this.typeFr = typeFr;
    }

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public String getTypeIn() {
        return typeIn;
    }

    public void setTypeIn(String typeIn) {
        this.typeIn = typeIn;
    }

    public String getTypeLo() {
        return typeLo;
    }

    public void setTypeLo(String typeLo) {
        this.typeLo = typeLo;
    }

    public String getTypePt() {
        return typePt;
    }

    public void setTypePt(String typePt) {
        this.typePt = typePt;
    }

    public String getTypeTh() {
        return typeTh;
    }

    public void setTypeTh(String typeTh) {
        this.typeTh = typeTh;
    }

    public String getTypeTr() {
        return typeTr;
    }

    public void setTypeTr(String typeTr) {
        this.typeTr = typeTr;
    }

    public String getTypeVi() {
        return typeVi;
    }

    public void setTypeVi(String typeVi) {
        this.typeVi = typeVi;
    }
}
