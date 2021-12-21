package com.olam.digital.ofis.model;

import java.io.Serializable;

// Pojo class for national id details
public class NationalIdDTO implements Serializable {

    private int id;
    private int farmerId;
    private String nationalidtype;
    private String nationalidNumber;
    private String photo;
    private String expiryDate;

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(int farmerId) {
        this.farmerId = farmerId;
    }

    public String getNationalidtype() {
        return nationalidtype;
    }

    public void setNationalidtype(String nationalidtype) {
        this.nationalidtype = nationalidtype;
    }

    public String getNationalidNumber() {
        return nationalidNumber;
    }

    public void setNationalidNumber(String nationalidNumber) {
        this.nationalidNumber = nationalidNumber;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }
}
