package com.olam.digital.ofis.downloadsurvey.polygondatadownload.dto;

public class PlotDisplayDto {

    Long plotId;
    Long farmerId;
    String farmerName;
    String plotName;
    String plotSize;

    boolean isChecked;

    public Long getPlotId() {
        return plotId;
    }

    public void setPlotId(Long plotId) {
        this.plotId = plotId;
    }

    public Long getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(Long farmerId) {
        this.farmerId = farmerId;
    }

    public String getFarmerName() {
        return farmerName;
    }

    public void setFarmerName(String farmerName) {
        this.farmerName = farmerName;
    }

    public String getPlotName() {
        return plotName;
    }

    public void setPlotName(String plotName) {
        this.plotName = plotName;
    }

    public String getPlotSize() {
        return plotSize;
    }

    public void setPlotSize(String plotSize) {
        this.plotSize = plotSize;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public PlotDisplayDto(Builder builder) {
        this.plotId = builder.plotId;
        this.farmerId = builder.farmerId;
        this.farmerName = builder.farmerName;
        this.plotName = builder.plotName;
        this.plotSize = builder.plotSize;
        this.isChecked = builder.isChecked;
    }

    public static class Builder {

        Long plotId;
        Long farmerId;
        String farmerName;
        String plotName;
        String plotSize;
        boolean isChecked;

        public Builder setPlotId(Long plotId) {
            this.plotId = plotId;
            return this;
        }

        public Builder setFarmerId(Long farmerId) {
            this.farmerId = farmerId;
            return this;
        }

        public Builder setFarmerName(String farmerName) {
            this.farmerName = farmerName;
            return this;
        }

        public Builder setPlotName(String plotName) {
            this.plotName = plotName;
            return this;
        }

        public Builder setPlotSize(String plotSize) {
            this.plotSize = plotSize;
            return this;
        }

        public Builder setIsChecked(boolean isChecked) {
            this.isChecked = isChecked;
            return this;
        }

        public PlotDisplayDto build() {
            return new PlotDisplayDto(this);
        }
    }
}
