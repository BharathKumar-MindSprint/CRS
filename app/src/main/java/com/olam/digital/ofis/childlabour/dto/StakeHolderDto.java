package com.olam.digital.ofis.childlabour.dto;

public class StakeHolderDto {

    private String msId;
    private String msName;
    private Integer msRoleId;

    private String olamMultiStakeholderId;

    public StakeHolderDto() {}

    public StakeHolderDto(String msId, String msName, Integer msRoleId, String olamMultiStakeholderId) {
        this.msId = msId;
        this.msName = msName;
        this.msRoleId = msRoleId;
        this.olamMultiStakeholderId = olamMultiStakeholderId;
    }

    public String getMsId() {
        return msId;
    }

    public void setMsId(String msId) {
        this.msId = msId;
    }

    public String getMsName() {
        return msName;
    }

    public void setMsName(String msName) {
        this.msName = msName;
    }

    public int getMsRoleId() {
        return msRoleId;
    }

    public void setMsRoleId(Integer msRoleId) {
        this.msRoleId = msRoleId;
    }

    public String getOlamMultiStakeholderId() {
        return olamMultiStakeholderId;
    }

    public void setOlamMultiStakeholderId(String olamMultiStakeholderId) {
        this.olamMultiStakeholderId = olamMultiStakeholderId;
    }
}
