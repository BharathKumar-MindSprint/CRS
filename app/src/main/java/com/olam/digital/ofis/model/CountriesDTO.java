package com.olam.digital.ofis.model;

public class CountriesDTO {
    private int DialingCodeId;
    private String DialingCode;
    private String Country;

    public int getDialingCodeId() {
        return DialingCodeId;
    }

    public void setDialingCodeId(int dialingCodeId) {
        DialingCodeId = dialingCodeId;
    }

    public String getDialingCode() {
        return DialingCode;
    }

    public void setDialingCode(String dialingCode) {
        DialingCode = dialingCode;
    }

    public String getCountry() {
        return Country;
    }

    public void setCountry(String country) {
        Country = country;
    }
}
