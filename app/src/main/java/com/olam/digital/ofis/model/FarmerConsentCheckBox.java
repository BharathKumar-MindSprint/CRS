package com.olam.digital.ofis.model;

public class FarmerConsentCheckBox {
    private Boolean ofiSystem;
    private Boolean traceability;
    private Boolean salesAndDevelpment;
    private Boolean advAndPromotion;
    private Boolean research;
    private boolean traceAndPromotion;
    private boolean dataAndBusiness;

    public FarmerConsentCheckBox() {
    }

    public FarmerConsentCheckBox(Boolean ofiSystem, Boolean traceability, Boolean salesAndDevelpment, Boolean advAndPromotion, Boolean research, boolean traceAndPromotion, boolean dataAndBusiness) {
        this.ofiSystem = ofiSystem;
        this.traceability = traceability;
        this.salesAndDevelpment = salesAndDevelpment;
        this.advAndPromotion = advAndPromotion;
        this.research = research;
        this.traceAndPromotion = traceAndPromotion;
        this.dataAndBusiness = dataAndBusiness;
    }

    public FarmerConsentCheckBox(Boolean ofiSystem, Boolean traceability, Boolean salesAndDevelpment, Boolean advAndPromotion, Boolean research) {
        this.ofiSystem = ofiSystem;
        this.traceability = traceability;
        this.salesAndDevelpment = salesAndDevelpment;
        this.advAndPromotion = advAndPromotion;
        this.research = research;
    }

    public Boolean getOfiSystem() {
        return ofiSystem;
    }

    public void setOfiSystem(Boolean ofiSystem) {
        this.ofiSystem = ofiSystem;
    }

    public Boolean getTraceability() {
        return traceability;
    }

    public boolean isTraceAndPromotion() {
        return traceAndPromotion;
    }

    public void setTraceAndPromotion(boolean traceAndPromotion) {
        this.traceAndPromotion = traceAndPromotion;
    }

    public boolean isDataAndBusiness() {
        return dataAndBusiness;
    }

    public void setDataAndBusiness(boolean dataAndBusiness) {
        this.dataAndBusiness = dataAndBusiness;
    }

    public void setTraceability(Boolean traceability) {
        this.traceability = traceability;
    }

    public Boolean getSalesAndDevelpment() {
        return salesAndDevelpment;
    }

    public void setSalesAndDevelpment(Boolean salesAndDevelpment) {
        this.salesAndDevelpment = salesAndDevelpment;
    }

    public Boolean getAdvAndPromotion() {
        return advAndPromotion;
    }

    public void setAdvAndPromotion(Boolean advAndPromotion) {
        this.advAndPromotion = advAndPromotion;
    }

    public Boolean getResearch() {
        return research;
    }

    public void setResearch(Boolean research) {
        this.research = research;
    }
}
