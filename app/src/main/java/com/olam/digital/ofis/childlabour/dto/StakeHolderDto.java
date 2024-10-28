package com.olam.digital.ofis.childlabour.dto;

public class StakeHolderDto {

    private String stakeHolderId;
    private String stakeHolderName;
    private String farmerRole;

    public StakeHolderDto(String stakeHolderId, String stakeHolderName, String farmerRole) {
        this.stakeHolderId = stakeHolderId;
        this.stakeHolderName = stakeHolderName;
        this.farmerRole = farmerRole;
    }

    public String getStakeHolderName() {
        return stakeHolderName;
    }

    public void setStakeHolderName(String stakeHolderName) {
        this.stakeHolderName = stakeHolderName;
    }

    public String getFarmerRole() {
        return farmerRole;
    }

    public void setFarmerRole(String farmerRole) {
        this.farmerRole = farmerRole;
    }

    public String getStakeHolderId() {
        return stakeHolderId;
    }

    public void setStakeHolderId(String stakeHolderId) {
        this.stakeHolderId = stakeHolderId;
    }
}
