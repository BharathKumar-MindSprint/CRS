package com.olam.digital.ofis.agrisupplier.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "supplier_submitted_modules")
public class SupplierSubmittedModules {
    @NonNull
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    @SerializedName("submittedModuleId")
    public Integer id;

    @ColumnInfo(name = "submitted_date")
    @SerializedName("submissionDate")
    public String submittedDate;

    @ColumnInfo(name = "supplier_id")
    @SerializedName("supplierId")
    public String supplierId;

    @ColumnInfo(name = "module_id")
    @SerializedName("moduleId")
    public String moduleId;

    @ColumnInfo(name = "sync_status")
    @SerializedName("syncStatus")
    public String syncStatus;

    @ColumnInfo(name = "is_survey_completed")
    public String isSurveyCompleted;

    public SupplierSubmittedModules(String submittedDate, String supplierId, String moduleId, String syncStatus, String isSurveyCompleted) {
        this.submittedDate = submittedDate;
        this.supplierId = supplierId;
        this.moduleId = moduleId;
        this.syncStatus = syncStatus;
        this.isSurveyCompleted = isSurveyCompleted;
    }
}
