package com.olam.digital.ofis.mvvmmodule.model.LocalUnit.ListItems;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.olam.digital.ofis.mvvmmodule.model.LocalUnit.GlobalUnit;
import com.olam.digital.ofis.mvvmmodule.model.LocalUnit.LocalUnit;

import java.util.ArrayList;

public class OutputData {

    @SerializedName("globalUnit")
    @Expose
    public ArrayList<GlobalUnit> globalUnits;

    public ArrayList<GlobalUnit> getGlobalUnits() {
        return globalUnits;
    }

    public void setGlobalUnits(ArrayList<GlobalUnit> globalUnits) {
        this.globalUnits = globalUnits;
    }

    public OutputData(ArrayList<GlobalUnit> globalUnits) {
        this.globalUnits = globalUnits;
    }
}
