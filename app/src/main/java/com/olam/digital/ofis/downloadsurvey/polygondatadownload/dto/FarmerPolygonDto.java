package com.olam.digital.ofis.downloadsurvey.polygondatadownload.dto;

public class FarmerPolygonDto {

    Long id;
    String name;
    String olamFarmerId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOlamFarmerId() {
        return olamFarmerId;
    }

    public void setOlamFarmerId(String olamFarmerId) {
        this.olamFarmerId = olamFarmerId;
    }

    public FarmerPolygonDto(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.olamFarmerId = builder.olamFarmerId;
    }

    public static class Builder {

        Long id;
        String name;
        String olamFarmerId;

        public FarmerPolygonDto.Builder setId(long id) {
            this.id = id;
            return this;
        }

        public FarmerPolygonDto.Builder setName(String name) {
            this.name = name;
            return this;
        }

        public FarmerPolygonDto.Builder setOlamFarmerId(String olamFarmerId) {
            this.olamFarmerId = olamFarmerId;
            return this;
        }

        public FarmerPolygonDto build() {
            return new FarmerPolygonDto(this);
        }

    }
}
