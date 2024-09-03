package com.olam.digital.ofis.downloadsurvey.dto;

import java.io.Serializable;

// Pojo class for village details
public class PlotDTO implements Serializable {


    private String plotName;
    private String plotActualName;
    private int plotId;
    private boolean isDeforestationPlot;
    private boolean isPALegalityFlag;


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

    public boolean isPALegalityFlag() {
        return isPALegalityFlag;
    }

    public void setPALegalityFlag(boolean PALegalityFlag) {
        isPALegalityFlag = PALegalityFlag;
    }
}
