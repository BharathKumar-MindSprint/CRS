package com.olam.digital.ofis.downloadsurvey.dto;

import java.io.Serializable;

// Pojo class for village details
public class PlotDTO implements Serializable {


    private String plotName;
    private String plotActualName;
    private int plotId;

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
}
