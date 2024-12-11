package com.olam.digital.ofis.mvvmmodule.roomdb.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "DmActionPlanCurrenciesTable")
public class DmActionPlanCurrenciesTable {

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "currencyId")
    public int currencyId;

    @ColumnInfo(name = "moduleId")
    public int moduleId;

    @ColumnInfo(name = "code")
    public String code;

    @ColumnInfo(name = "farmerId")
    public String farmerId;

    @ColumnInfo(name = "plotId", defaultValue = "0")
    public Integer plotId;

}
