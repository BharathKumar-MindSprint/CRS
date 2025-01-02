package com.olam.digital.ofis.mvvmmodule.roomdb.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "DmCurrency")
public class DmCurrency {
    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "moduleDraftId")
    public int moduleDraftId;

    @ColumnInfo(name = "currencyDraftId")
    public int currencyDraftId;

    @ColumnInfo(name = "currencyId")
    public int currencyId;

    @ColumnInfo(name = "isDefault")
    public boolean isDefault;

}
