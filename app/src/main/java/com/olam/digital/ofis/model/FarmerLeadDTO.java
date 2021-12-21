package com.olam.digital.ofis.model;

public class FarmerLeadDTO {

    int farmerLeadId;
    String farmerLeadDisplayId,farmerLeadFullName,contactNumber;



    public int getFarmerLeadId() {
        return farmerLeadId;
    }

    public void setFarmerLeadId(int farmerLeadId) {
        this.farmerLeadId = farmerLeadId;
    }

    public String getFarmerLeadDisplayId() {
        return farmerLeadDisplayId;
    }

    public void setFarmerLeadDisplayId(String farmerLeadDisplayId) {
        this.farmerLeadDisplayId = farmerLeadDisplayId;
    }

    public String getFarmerLeadFullName() {
        return farmerLeadFullName;
    }

    public void setFarmerLeadFullName(String farmerLeadFullName) {
        this.farmerLeadFullName = farmerLeadFullName;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }
}
