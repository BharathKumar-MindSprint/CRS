package com.olam.digital.ofis.agrisupplier.dto;

import android.os.Parcel;
import android.os.Parcelable;

public class SupplierSurveyDto implements Parcelable {
    private Integer id;
    private String name;
    private String nameFr;
    private String nameEs;
    private String nameIdn;
    private String namePt;
    private String nameTr;
    private String nameLo;
    private String nameVi;
    private String nameTh;
    private String description;
    private String descriptionPt;
    private String descriptionVi;
    private String descriptionEs;
    private String descriptionTh;
    private String descriptionLo;
    private String descriptionIdn;
    private String descriptionTr;
    private String descriptionFr;
    private String moduleType;
    private String startDate;
    private String captureLocation;

    public SupplierSurveyDto(Integer id, String name, String nameFr, String nameEs, String nameIdn, String namePt, String nameTr, String nameLo, String nameVi, String nameTh, String description, String descriptionPt, String descriptionVi, String descriptionEs, String descriptionTh, String descriptionLo, String descriptionIdn, String descriptionTr, String descriptionFr, String moduleType, String startDate, String captureLocation) {
        this.id = id;
        this.name = name;
        this.nameFr = nameFr;
        this.nameEs = nameEs;
        this.nameIdn = nameIdn;
        this.namePt = namePt;
        this.nameTr = nameTr;
        this.nameLo = nameLo;
        this.nameVi = nameVi;
        this.nameTh = nameTh;
        this.description = description;
        this.descriptionPt = descriptionPt;
        this.descriptionVi = descriptionVi;
        this.descriptionEs = descriptionEs;
        this.descriptionTh = descriptionTh;
        this.descriptionLo = descriptionLo;
        this.descriptionIdn = descriptionIdn;
        this.descriptionTr = descriptionTr;
        this.descriptionFr = descriptionFr;
        this.moduleType = moduleType;
        this.startDate = startDate;
        this.captureLocation = captureLocation;
    }

    protected SupplierSurveyDto(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readInt();
        }
        name = in.readString();
        nameFr = in.readString();
        nameEs = in.readString();
        nameIdn = in.readString();
        namePt = in.readString();
        nameTr = in.readString();
        nameLo = in.readString();
        nameVi = in.readString();
        nameTh = in.readString();
        description = in.readString();
        descriptionPt = in.readString();
        descriptionVi = in.readString();
        descriptionEs = in.readString();
        descriptionTh = in.readString();
        descriptionLo = in.readString();
        descriptionIdn = in.readString();
        descriptionTr = in.readString();
        descriptionFr = in.readString();
        moduleType = in.readString();
        startDate = in.readString();
        captureLocation = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(id);
        }
        dest.writeString(name);
        dest.writeString(nameFr);
        dest.writeString(nameEs);
        dest.writeString(nameIdn);
        dest.writeString(namePt);
        dest.writeString(nameTr);
        dest.writeString(nameLo);
        dest.writeString(nameVi);
        dest.writeString(nameTh);
        dest.writeString(description);
        dest.writeString(descriptionPt);
        dest.writeString(descriptionVi);
        dest.writeString(descriptionEs);
        dest.writeString(descriptionTh);
        dest.writeString(descriptionLo);
        dest.writeString(descriptionIdn);
        dest.writeString(descriptionTr);
        dest.writeString(descriptionFr);
        dest.writeString(moduleType);
        dest.writeString(startDate);
        dest.writeString(captureLocation);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SupplierSurveyDto> CREATOR = new Creator<SupplierSurveyDto>() {
        @Override
        public SupplierSurveyDto createFromParcel(Parcel in) {
            return new SupplierSurveyDto(in);
        }

        @Override
        public SupplierSurveyDto[] newArray(int size) {
            return new SupplierSurveyDto[size];
        }
    };

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameFr() {
        return nameFr;
    }

    public void setNameFr(String nameFr) {
        this.nameFr = nameFr;
    }

    public String getNameEs() {
        return nameEs;
    }

    public void setNameEs(String nameEs) {
        this.nameEs = nameEs;
    }

    public String getNameIdn() {
        return nameIdn;
    }

    public void setNameIdn(String nameId) {
        this.nameIdn = nameId;
    }

    public String getNamePt() {
        return namePt;
    }

    public void setNamePt(String namePt) {
        this.namePt = namePt;
    }

    public String getNameTr() {
        return nameTr;
    }

    public void setNameTr(String nameTr) {
        this.nameTr = nameTr;
    }

    public String getNameLo() {
        return nameLo;
    }

    public void setNameLo(String nameLo) {
        this.nameLo = nameLo;
    }

    public String getNameVi() {
        return nameVi;
    }

    public void setNameVi(String nameVi) {
        this.nameVi = nameVi;
    }

    public String getNameTh() {
        return nameTh;
    }

    public void setNameTh(String nameTh) {
        this.nameTh = nameTh;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescriptionPt() {
        return descriptionPt;
    }

    public void setDescriptionPt(String descriptionPt) {
        this.descriptionPt = descriptionPt;
    }

    public String getDescriptionVi() {
        return descriptionVi;
    }

    public void setDescriptionVi(String descriptionVi) {
        this.descriptionVi = descriptionVi;
    }

    public String getDescriptionEs() {
        return descriptionEs;
    }

    public void setDescriptionEs(String descriptionEs) {
        this.descriptionEs = descriptionEs;
    }

    public String getDescriptionTh() {
        return descriptionTh;
    }

    public void setDescriptionTh(String descriptionTh) {
        this.descriptionTh = descriptionTh;
    }

    public String getDescriptionLo() {
        return descriptionLo;
    }

    public void setDescriptionLo(String descriptionLo) {
        this.descriptionLo = descriptionLo;
    }

    public String getDescriptionIdn() {
        return descriptionIdn;
    }

    public void setDescriptionIdn(String descriptionIdn) {
        this.descriptionIdn = descriptionIdn;
    }

    public String getDescriptionTr() {
        return descriptionTr;
    }

    public void setDescriptionTr(String descriptionTr) {
        this.descriptionTr = descriptionTr;
    }

    public String getDescriptionFr() {
        return descriptionFr;
    }

    public void setDescriptionFr(String descriptionFr) {
        this.descriptionFr = descriptionFr;
    }

    public String getModuleType() {
        return moduleType;
    }

    public void setModuleType(String moduleType) {
        this.moduleType = moduleType;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getCaptureLocation() {
        return captureLocation;
    }

    public void setCaptureLocation(String captureLocation) {
        this.captureLocation = captureLocation;
    }
}
