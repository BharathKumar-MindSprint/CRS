package com.olam.digital.ofis.downloadsurvey.dto;

import java.io.Serializable;

// Pojo class for village details
public class PlotDTO implements Serializable {


    private String plotName;
    private String plotActualName;
    private int plotId;
    private boolean isDeforestationPlot;
    private boolean isPALegalityPlot;
    private String cleansedStatus;

    private boolean isEUDRSurveyCompleted;
    private boolean isPALegalitySurveyCompleted;

    private String geoDbFlagStatus;



    public String getPlotActualName() {
        return plotActualName;
    }

    public void setPlotActualName(String plotActualName) {
        this.plotActualName = plotActualName;
    }

    public String getPlotName() {
        return plotName;
    }

    public void setPlotName(String plotName) {
        this.plotName = plotName;
    }

    public int getPlotId() {
        return plotId;
    }

    public void setPlotId(int plotId) {
        this.plotId = plotId;
    }

    public boolean isDeforestationPlot() {
        return isDeforestationPlot;
    }

    public void setDeforestationPlot(boolean deforestationPlot) {
        isDeforestationPlot = deforestationPlot;
    }

    public void setCleansedStatus(String cleansedStatus) {
        this.cleansedStatus = cleansedStatus;
    }

    public boolean isPALegalityPlot() {
        return isPALegalityPlot;
    }

    public void setPALegalityPlot(boolean PALegalityPlot) {
        isPALegalityPlot = PALegalityPlot;
    }

    public String getCleansedStatus() {
        return cleansedStatus;
    }

    public boolean isEUDRSurveyCompleted() {
        return isEUDRSurveyCompleted;
    }

    public void setEUDRSurveyCompleted(boolean EUDRSurveyCompleted) {
        isEUDRSurveyCompleted = EUDRSurveyCompleted;
    }

    public boolean isPALegalitySurveyCompleted() {
        return isPALegalitySurveyCompleted;
    }

    public void setPALegalitySurveyCompleted(boolean PALegalitySurveyCompleted) {
        isPALegalitySurveyCompleted = PALegalitySurveyCompleted;
    }

    public String getGeoDbFlagStatus() {
        return geoDbFlagStatus;
    }

    public void setGeoDbFlagStatus(String geoDbFlagStatus) {
        this.geoDbFlagStatus = geoDbFlagStatus;
    }
}
