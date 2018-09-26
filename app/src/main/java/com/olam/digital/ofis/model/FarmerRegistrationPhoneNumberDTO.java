package com.olam.digital.ofis.model;

public class FarmerRegistrationPhoneNumberDTO {
    private Long id ;
    private Long farmerId;
    private String phoneNumber;
    private Boolean primaryNumber;
    private Boolean deletedFlag;
    private Long farmerPhoneNumberId;
    private Long countryId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(Long farmerId) {
        this.farmerId = farmerId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Boolean getPrimaryNumber() {
        return primaryNumber;
    }

    public void setPrimaryNumber(Boolean primaryNumber) {
        this.primaryNumber = primaryNumber;
    }

    public Boolean getDeletedFlag() {
        return deletedFlag;
    }

    public void setDeletedFlag(Boolean deletedFlag) {
        this.deletedFlag = deletedFlag;
    }

    public Long getFarmerPhoneNumberId() {
        return farmerPhoneNumberId;
    }

    public void setFarmerPhoneNumberId(Long farmerPhoneNumberId) {
        this.farmerPhoneNumberId = farmerPhoneNumberId;
    }

    public Long getCountryId() {
        return countryId;
    }

    public void setCountryId(Long countryId) {
        this.countryId = countryId;
    }
}
