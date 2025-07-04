package com.olam.digital.ofis.recordpolygon;

public class PolygonDataDto {

    public int id;
    public String plotName;
    public String positionData;
    public String centerPlot;
    public Double plotArea;
    public String deActivationFlag;
    public String reasonForDeActivation;
    public String isDeActivationFlagSynced;
    public String isSynced;


    private PolygonDataDto(Builder builder) {
        this.id = builder.id;
        this.plotName = builder.plotName;
        this.positionData = builder.positionData;
        this.centerPlot = builder.centerPlot;
        this.plotArea = builder.plotArea;
        this.deActivationFlag = builder.deActivationFlag;
        this.reasonForDeActivation = builder.reasonForDeActivation;
        this.isDeActivationFlagSynced = builder.isDeActivationFlagSynced;
        this.isSynced = builder.isSynced;
    }

    // Static nested Builder class
    public static class Builder {
        private int id;
        private String plotName;
        private String positionData;
        private String centerPlot;
        private Double plotArea;
        private String deActivationFlag;
        private String reasonForDeActivation;
        private String isDeActivationFlagSynced;
        private String isSynced;

        public Builder() {
            // default constructor
        }

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder plotName(String plotName) {
            this.plotName = plotName;
            return this;
        }

        public Builder positionData(String positionData) {
            this.positionData = positionData;
            return this;
        }

        public Builder centerPlot(String centerPlot) {
            this.centerPlot = centerPlot;
            return this;
        }

        public Builder plotArea(String plotArea) {
            this.plotArea = plotArea == null || plotArea.isEmpty() ? null : Double.valueOf(plotArea);
            return this;
        }

        public Builder deActivationFlag(String deActivationFlag) {
            this.deActivationFlag = deActivationFlag;
            return this;
        }

        public Builder reasonForDeActivation(String reasonForDeActivation) {
            this.reasonForDeActivation = reasonForDeActivation;
            return this;
        }

        public Builder isDeActivationFlagSynced(String isDeActivationFlagSynced) {
            this.isDeActivationFlagSynced = isDeActivationFlagSynced;
            return this;
        }

        public Builder isSynced(String isSynced) {
            this.isSynced = isSynced;
            return this;
        }


        public PolygonDataDto build() {
            return new PolygonDataDto(this);
        }
    }
}
