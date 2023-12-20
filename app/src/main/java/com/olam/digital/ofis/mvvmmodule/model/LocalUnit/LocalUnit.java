package com.olam.digital.ofis.mvvmmodule.model.LocalUnit;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.math.BigDecimal;

@Entity (tableName = "LocalUnit")
public class LocalUnit {

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "localUnitId")
    public int localUnitId;

    @ColumnInfo(name = "globalUnitFk")
    public int globalUnitFk;

    @ColumnInfo(name = "localUnitMetric")
    public String localUnitMetric;

    @ColumnInfo(name = "localUnitAbbreviation")
    public String localUnitAbbreviation;

    @ColumnInfo(name = "conversionFactor")
    public String conversionFactor;

    @ColumnInfo(name = "countryId")
    public int countryId;

    @ColumnInfo(name = "productId")
    public int productId;


}
