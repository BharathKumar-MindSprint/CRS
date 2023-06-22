package com.olam.digital.ofis.mvvmmodule.roomdb.followuptable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "ExistingModuleItemFollowupTable")
public class ExistingModuleItemFollowupTable {

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "farmerId")
    public int farmerId;

    @ColumnInfo(name = "enumeratorId")
    public String enumeratorId;

    @ColumnInfo(name = "submissionDate")
    public String submissionDate;

    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "farmerGroupId")
    public int farmerGroupId;

    @ColumnInfo(name = "isMobile")
    public boolean isMobile;

    @ColumnInfo(name = "moduleId")
    public int moduleId;

    @ColumnInfo(name = "currencyId")
    public int currencyId;
}
