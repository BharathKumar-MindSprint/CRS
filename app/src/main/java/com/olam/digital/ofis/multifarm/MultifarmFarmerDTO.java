package com.olam.digital.ofis.multifarm;

public class MultifarmFarmerDTO {

    private long farmerId;
    private String farmerName;

    public long getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(long farmerId) {
        this.farmerId = farmerId;
    }

    public String getFarmerName() {
        return farmerName;
    }

    public void setFarmerName(String farmerName) {
        this.farmerName = farmerName;
    }
}
