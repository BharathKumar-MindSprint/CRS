package com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "DmCurrenciesItem")
public class DmCurrenciesItem {
    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "currencyId")
    public int currencyId;

    @ColumnInfo(name = "code")
    public String code;

    @ColumnInfo(name = "notation")
    public String notation;

    @ColumnInfo(name = "name")
    public String name;
}
