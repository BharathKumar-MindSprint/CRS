package com.olam.digital.ofis.mvvmmodule.model.LocalUnit;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "GlobalUnit")
public class GlobalUnit {

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "globalUnitId")
    public int globalUnitId;

    @ColumnInfo(name = "globalMetric")
    public String globalMetric;

    @ColumnInfo(name = "globalUnit")
    public String globalUnit;

    @ColumnInfo(name = "globalAbbreviation")
    public String globalAbbreviation;

    @Ignore
    @ColumnInfo(name = "localUnit")
    public ArrayList<LocalUnit> localUnit;

    public ArrayList<LocalUnit> getLocalUnit() {
        return localUnit;
    }

    public void setLocalUnit(ArrayList<LocalUnit> localUnit) {
        this.localUnit = localUnit;
    }

}
