package com.olam.digital.ofis.multifarm;

public class MultiFarmStakeHolderDTO {
    private String farmName;
    private String farmCode;

    private String farmerStakeHolderRoleId;
    private String farmerStakeHolderId;

    private String farmerId;



    public String getFarmName() {
        return farmName;
    }

    public void setFarmName(String farmName) {
        this.farmName = farmName;
    }

    public String getFarmCode() {
        return farmCode;
    }

    public void setFarmCode(String farmCode) {
        this.farmCode = farmCode;
    }

    public String getFarmerStakeHolderRoleId() {
        return farmerStakeHolderRoleId;
    }

    public void setFarmerStakeHolderRoleId(String farmerStakeHolderRoleId) {
        this.farmerStakeHolderRoleId = farmerStakeHolderRoleId;
    }

    public String getFarmerStakeHolderId() {
        return farmerStakeHolderId;
    }

    public void setFarmerStakeHolderId(String farmerStakeHolderId) {
        this.farmerStakeHolderId = farmerStakeHolderId;
    }

    public String getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(String farmerId) {
        this.farmerId = farmerId;
    }


}
