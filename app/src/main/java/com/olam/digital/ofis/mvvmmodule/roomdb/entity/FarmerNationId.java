package com.olam.digital.ofis.mvvmmodule.roomdb.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "FarmerNationId")
public class FarmerNationId {
    @PrimaryKey
    @ColumnInfo(name="id")
    public int id;
    @ColumnInfo(name="farmerId")
    public int farmerId;
    @ColumnInfo(name="msId")
    public int msId;
    @ColumnInfo(name="nationIdType")
    public int nationIdType;
    @ColumnInfo(name="nationIdNumber")
    public int nationIdNumber;
    @ColumnInfo(name="isSynced")
    public int isSynced;
    @ColumnInfo(name="expiryDate")
    public int expiryDate;
    @ColumnInfo(name="photo")
    public int photo;




}
