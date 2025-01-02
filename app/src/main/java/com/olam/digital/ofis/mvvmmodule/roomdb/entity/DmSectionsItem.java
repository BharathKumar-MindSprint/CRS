package com.olam.digital.ofis.mvvmmodule.roomdb.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "DmSectionsItem")
public class DmSectionsItem {
    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "nameTh")
    public String nameTh;

    @ColumnInfo(name = "nameVi")
    public String nameVi;

    @ColumnInfo(name = "namePt")
    public String namePt;

    @ColumnInfo(name = "nameLo")
    public String nameLo;

    @ColumnInfo(name = "nameEs")
    public String nameEs;

    @ColumnInfo(name = "nameFr")
    public String nameFr;

    @ColumnInfo(name = "nameTr")
    public String nameTr;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "nameId")
    public String nameId;

    @ColumnInfo(name = "description")
    public String description;

    @ColumnInfo(name = "descriptionPt")
    public String descriptionPt;

    @ColumnInfo(name = "descriptionVi")
    public String descriptionVi;

    @ColumnInfo(name = "descriptionFr")
    public String descriptionFr;

    @ColumnInfo(name = "descriptionEs")
    public String descriptionEs;

    @ColumnInfo(name = "descriptionTh")
    public String descriptionTh;

    @ColumnInfo(name = "descriptionLo")
    public String descriptionLo;

    @ColumnInfo(name = "descriptionIdn")
    public String descriptionIdn;

    @ColumnInfo(name = "descriptionTr")
    public String descriptionTr;

    @ColumnInfo(name = "isDefault")
    public boolean isDefault;

    @ColumnInfo(name = "showDescription")
    public boolean showDescription;

    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "position")
    public int position;

    @ColumnInfo(name = "moduleId")
    public int moduleId;


}
