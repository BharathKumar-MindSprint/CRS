package com.olam.digital.ofis.model;

import java.io.Serializable;

// Pojo class for national id type details
public class NationalIdTypeDTO implements Serializable {

    private int id;
    private String type = "type";
    private String nationalIdLimit = "nationalIdLimit";
    private String photoRequired = "photoRequired";
    private String typeFr = "typeFr";
    private String typeEs = "typeEs";
    private String typePt = "typePt";
    private String typeId = "typeId";
    private String typeTr = "typeTr";
    private String typeLo = "typeLo";
    private String typeVi = "typeVi";
    private String typeTh = "typeTh";
    private String active = "active";

    /*public NationalIdTypeDTO(NationalIdTypeDTO nationalIdTypeDTO) {
        this.id = nationalIdTypeDTO.id;
        this.type = nationalIdTypeDTO.type;
        this.nationalIdLimit = nationalIdTypeDTO.nationalIdLimit;
        this.photoRequired = nationalIdTypeDTO.photoRequired;
        this.typeFr = nationalIdTypeDTO.typeFr;
        this.typeEs = nationalIdTypeDTO.typeEs;
        this.typePt = nationalIdTypeDTO.typePt;
        this.typeId = nationalIdTypeDTO.typeId;
        this.typeTr = nationalIdTypeDTO.typeTr;
        this.typeLo = nationalIdTypeDTO.typeLo;
        this.typeVi = nationalIdTypeDTO.typeVi;
        this.typeTh = nationalIdTypeDTO.typeTh;
        this.active = nationalIdTypeDTO.active;
    }*/

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNationalIdLimit() {
        return nationalIdLimit;
    }

    public void setNationalIdLimit(String nationalIdLimit) {
        this.nationalIdLimit = nationalIdLimit;
    }

    public String getPhotoRequired() {
        return photoRequired;
    }

    public void setPhotoRequired(String photoRequired) {
        this.photoRequired = photoRequired;
    }

    public String getTypeFr() {
        return typeFr;
    }

    public void setTypeFr(String typeFr) {
        this.typeFr = typeFr;
    }

    public String getTypeEs() {
        return typeEs;
    }

    public void setTypeEs(String typeEs) {
        this.typeEs = typeEs;
    }

    public String getTypePt() {
        return typePt;
    }

    public void setTypePt(String typePt) {
        this.typePt = typePt;
    }

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public String getTypeTr() {
        return typeTr;
    }

    public void setTypeTr(String typeTr) {
        this.typeTr = typeTr;
    }

    public String getTypeLo() {
        return typeLo;
    }

    public void setTypeLo(String typeLo) {
        this.typeLo = typeLo;
    }

    public String getTypeVi() {
        return typeVi;
    }

    public void setTypeVi(String typeVi) {
        this.typeVi = typeVi;
    }

    public String getTypeTh() {
        return typeTh;
    }

    public void setTypeTh(String typeTh) {
        this.typeTh = typeTh;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }
}
