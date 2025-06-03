package com.olam.digital.ofis.agrisupplier.dto;

import com.google.gson.annotations.SerializedName;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.ComplianceIndicatorItem;
import com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule.SectionsItem;

import java.util.List;

public class SupplierSurveyResponse {
    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("nameFr")
    private String nameFr;

    @SerializedName("namePt")
    private String namePt;

    @SerializedName("nameLo")
    private String nameLo;

    @SerializedName("nameTr")
    private String nameTr;

    @SerializedName("nameTh")
    private String nameTh;

    @SerializedName("nameVi")
    private String nameVi;

    @SerializedName("nameEs")
    private String nameEs;

    @SerializedName("nameIdn")
    private String nameIdn;

    @SerializedName("description")
    private String description;

    @SerializedName("descriptionPt")
    private String descriptionPt;

    @SerializedName("descriptionFr")
    private String descriptionFr;

    @SerializedName("descriptionVi")
    private String descriptionVi;

    @SerializedName("descriptionEs")
    private String descriptionEs;

    @SerializedName("descriptionTh")
    private String descriptionTh;

    @SerializedName("descriptionLo")
    private String descriptionLo;

    @SerializedName("descriptionIdn")
    private String descriptionIdn;

    @SerializedName("descriptionTr")
    private String descriptionTr;

    @SerializedName("surveyType")
    private int surveyType;

    @SerializedName("assessmentType")
    private String assessmentType;

    @SerializedName("supplierType")
    private Integer supplierType;

    @SerializedName("displayLogic")
    private String displayLogic;

    @SerializedName("captureLocation")
    public String captureLocation;

    @SerializedName("sections")
    private List<SectionsItem> sections;

    @SerializedName("moduleAssignment")
    public SupplierSurveyAssignments moduleAssignment;

    @SerializedName("complianceIndicator")
    private List<ComplianceIndicatorItem> complianceIndicator;

    @SerializedName("maxScore")
    private int maxScore;


    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public String getNamePt() {
        return namePt;
    }

    public void setNamePt(String namePt) {
        this.namePt = namePt;
    }

    public String getNameLo() {
        return nameLo;
    }

    public void setNameLo(String nameLo) {
        this.nameLo = nameLo;
    }

    public String getNameTr() {
        return nameTr;
    }

    public void setNameTr(String nameTr) {
        this.nameTr = nameTr;
    }

    public String getNameTh() {
        return nameTh;
    }

    public void setNameTh(String nameTh) {
        this.nameTh = nameTh;
    }

    public String getNameVi() {
        return nameVi;
    }

    public void setNameVi(String nameVi) {
        this.nameVi = nameVi;
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

    public void setNameIdn(String nameIdn) {
        this.nameIdn = nameIdn;
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

    public String getDescriptionFr() {
        return descriptionFr;
    }

    public void setDescriptionFr(String descriptionFr) {
        this.descriptionFr = descriptionFr;
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

    public int getSurveyType() {
        return surveyType;
    }

    public void setSurveyType(int surveyType) {
        this.surveyType = surveyType;
    }

    public String getAssessmentType() {
        return assessmentType;
    }

    public void setAssessmentType(String assessmentType) {
        this.assessmentType = assessmentType;
    }

    public Integer getSupplierType() {
        return supplierType;
    }

    public void setSupplierType(Integer supplierType) {
        this.supplierType = supplierType;
    }

    public String getDisplayLogic() {
        return displayLogic;
    }

    public void setDisplayLogic(String displayLogic) {
        this.displayLogic = displayLogic;
    }

    public String getCaptureLocation() {
        return captureLocation;
    }

    public void setCaptureLocation(String captureLocation) {
        this.captureLocation = captureLocation;
    }

    public List<SectionsItem> getSections() {
        return sections;
    }

    public void setSections(List<SectionsItem> sections) {
        this.sections = sections;
    }

    public SupplierSurveyAssignments getModuleAssignment() {
        return moduleAssignment;
    }

    public void setModuleAssignment(SupplierSurveyAssignments moduleAssignment) {
        this.moduleAssignment = moduleAssignment;
    }

    public List<ComplianceIndicatorItem> getComplianceIndicator() {
        return complianceIndicator;
    }

    public void setComplianceIndicator(List<ComplianceIndicatorItem> complianceIndicator) {
        this.complianceIndicator = complianceIndicator;
    }

    public int getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(int maxScore) {
        this.maxScore = maxScore;
    }
}
