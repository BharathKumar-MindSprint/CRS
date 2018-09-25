package com.webpoint0.ofiscompanion.model;

import java.util.Date;
import java.util.List;

public class FarmerRegistrationDTO {

    private Long id;
    private Long farmerId;
    private String displayFarmerId;
    private String farmerFirstName;
    private String farmerLastName;
    private Date dateOfBirth;
    private String gender;
    private String email;
    private boolean isActive;
    private boolean deletedFlag;
    private String farmerName;
    private Long countryId;
    private Long stateId;
    private Long talukId;
    private Long districtId;
    private Long villageId;
    private int farmerType;
    private List<FarmerRegistrationPhoneNumberDTO> phoneNumbers;
    private Date createdTs;
    private Date updatedTs;
    private String createdBy;
    private String updatedBy;

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

    public String getDisplayFarmerId() {
        return displayFarmerId;
    }

    public void setDisplayFarmerId(String displayFarmerId) {
        this.displayFarmerId = displayFarmerId;
    }

    public String getFarmerFirstName() {
        return farmerFirstName;
    }

    public void setFarmerFirstName(String farmerFirstName) {
        this.farmerFirstName = farmerFirstName;
    }

    public String getFarmerLastName() {
        return farmerLastName;
    }

    public void setFarmerLastName(String farmerLastName) {
        this.farmerLastName = farmerLastName;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isDeletedFlag() {
        return deletedFlag;
    }

    public void setDeletedFlag(boolean deletedFlag) {
        this.deletedFlag = deletedFlag;
    }

    public String getFarmerName() {
        return farmerName;
    }

    public void setFarmerName(String farmerName) {
        this.farmerName = farmerName;
    }

    public Long getCountryId() {
        return countryId;
    }

    public void setCountryId(Long countryId) {
        this.countryId = countryId;
    }

    public Long getStateId() {
        return stateId;
    }

    public void setStateId(Long stateId) {
        this.stateId = stateId;
    }

    public Long getTalukId() {
        return talukId;
    }

    public void setTalukId(Long talukId) {
        this.talukId = talukId;
    }

    public Long getDistrictId() {
        return districtId;
    }

    public void setDistrictId(Long districtId) {
        this.districtId = districtId;
    }

    public Long getVillageId() {
        return villageId;
    }

    public void setVillageId(Long villageId) {
        this.villageId = villageId;
    }

    public int getFarmerType() {
        return farmerType;
    }

    public void setFarmerType(int farmerType) {
        this.farmerType = farmerType;
    }

    public List<FarmerRegistrationPhoneNumberDTO> getPhoneNumbers() {
        return phoneNumbers;
    }

    public void setPhoneNumbers(List<FarmerRegistrationPhoneNumberDTO> phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }

    public Date getCreatedTs() {
        return createdTs;
    }

    public void setCreatedTs(Date createdTs) {
        this.createdTs = createdTs;
    }

    public Date getUpdatedTs() {
        return updatedTs;
    }

    public void setUpdatedTs(Date updatedTs) {
        this.updatedTs = updatedTs;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public int getAppId() {
        return appId;
    }

    public void setAppId(int appId) {
        this.appId = appId;
    }

    public Boolean getCertified() {
        return isCertified;
    }

    public void setCertified(Boolean certified) {
        isCertified = certified;
    }

    public String getCountryIsoCode() {
        return countryIsoCode;
    }

    public void setCountryIsoCode(String countryIsoCode) {
        this.countryIsoCode = countryIsoCode;
    }

    public Long getGlobalFamerId() {
        return globalFamerId;
    }

    public void setGlobalFamerId(Long globalFamerId) {
        this.globalFamerId = globalFamerId;
    }

    public Long getBuyingAgentId() {
        return buyingAgentId;
    }

    public void setBuyingAgentId(Long buyingAgentId) {
        this.buyingAgentId = buyingAgentId;
    }

    public Long getFarmerGroupId() {
        return farmerGroupId;
    }

    public void setFarmerGroupId(Long farmerGroupId) {
        this.farmerGroupId = farmerGroupId;
    }

    public Long getTempFarmerId() {
        return tempFarmerId;
    }

    public void setTempFarmerId(Long tempFarmerId) {
        this.tempFarmerId = tempFarmerId;
    }

    public Long getModerationStatusId() {
        return moderationStatusId;
    }

    public void setModerationStatusId(Long moderationStatusId) {
        this.moderationStatusId = moderationStatusId;
    }

    public Long getEnumeratorId() {
        return enumeratorId;
    }

    public void setEnumeratorId(Long enumeratorId) {
        this.enumeratorId = enumeratorId;
    }

    public String getJoinOlam() {
        return joinOlam;
    }

    public void setJoinOlam(String joinOlam) {
        this.joinOlam = joinOlam;
    }

    public Long getFarmerAge() {
        return farmerAge;
    }

    public void setFarmerAge(Long farmerAge) {
        this.farmerAge = farmerAge;
    }

    public Long getTotalArea() {
        return totalArea;
    }

    public void setTotalArea(Long totalArea) {
        this.totalArea = totalArea;
    }

    public Long getPlaceId() {
        return placeId;
    }

    public void setPlaceId(Long placeId) {
        this.placeId = placeId;
    }

    public Long getSectionId() {
        return sectionId;
    }

    public void setSectionId(Long sectionId) {
        this.sectionId = sectionId;
    }

    public Long getEducationLevelId() {
        return educationLevelId;
    }

    public void setEducationLevelId(Long educationLevelId) {
        this.educationLevelId = educationLevelId;
    }

    public String getSubDistrict() {
        return subDistrict;
    }

    public void setSubDistrict(String subDistrict) {
        this.subDistrict = subDistrict;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public Long getRegionId() {
        return regionId;
    }

    public void setRegionId(Long regionId) {
        this.regionId = regionId;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getNationalId() {
        return nationalId;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOlamFarmerId() {
        return olamFarmerId;
    }

    public void setOlamFarmerId(String olamFarmerId) {
        this.olamFarmerId = olamFarmerId;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public Boolean getConsentFarmer() {
        return consentFarmer;
    }

    public void setConsentFarmer(Boolean consentFarmer) {
        this.consentFarmer = consentFarmer;
    }

    public Boolean getConsentOthers() {
        return consentOthers;
    }

    public void setConsentOthers(Boolean consentOthers) {
        this.consentOthers = consentOthers;
    }

    public String getUploadId() {
        return uploadId;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

    public Boolean getFullySynced() {
        return fullySynced;
    }

    public void setFullySynced(Boolean fullySynced) {
        this.fullySynced = fullySynced;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Long getBlockStatusId() {
        return blockStatusId;
    }

    public void setBlockStatusId(Long blockStatusId) {
        this.blockStatusId = blockStatusId;
    }

    public String getBlockReasonText() {
        return blockReasonText;
    }

    public void setBlockReasonText(String blockReasonText) {
        this.blockReasonText = blockReasonText;
    }

    public Long getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(Long mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getHasBankAccount() {
        return hasBankAccount;
    }

    public void setHasBankAccount(Boolean hasBankAccount) {
        this.hasBankAccount = hasBankAccount;
    }

    public Double getCreditBalance() {
        return creditBalance;
    }

    public void setCreditBalance(Double creditBalance) {
        this.creditBalance = creditBalance;
    }

    private int appId;

    private Boolean isCertified;

    private String countryIsoCode;
    private Long globalFamerId;
    private Long buyingAgentId;
    private Long farmerGroupId;
    private Long tempFarmerId;
    private Long moderationStatusId;
    private Long enumeratorId;
    private String joinOlam;
    private Long farmerAge;
    private Long totalArea;

    private Long placeId;
    private Long sectionId;
    private Long educationLevelId;

    private String subDistrict;
    private String streetAddress;
    private Long regionId;

    private Double latitude;
    private Double longitude;
    private String nationalId;
    private String postalCode;
    private String language;
    private String title;
    private String olamFarmerId;

    private String photo;
    private String signature;
    private Boolean consentFarmer;
    private Boolean consentOthers;
    private String uploadId;
    private Boolean fullySynced;
    private Date createdAt;

    private String notes;
    private Long blockStatusId;
    private String blockReasonText;

    private Long mobileNumber;
    private String status;

    private Boolean hasBankAccount;
    private Double creditBalance;
}
