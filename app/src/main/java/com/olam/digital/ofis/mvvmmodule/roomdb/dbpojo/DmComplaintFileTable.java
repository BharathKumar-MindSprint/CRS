package com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "DmComplaintFileTable")
public class DmComplaintFileTable {

    @PrimaryKey(autoGenerate = true)
    public long uid;

    @ColumnInfo(name = "file_name")
    private String file_name;

    @ColumnInfo(name = "questionId")
    private String questionId;

    @ColumnInfo(name = "farmerId")
    private String farmerId;

    public String getSurveyType() {
        return surveyType;
    }

    public void setSurveyType(String surveyType) {
        this.surveyType = surveyType;
    }

    @ColumnInfo(name = "surveyType")
    private String surveyType;

    @ColumnInfo(name = "attempt")
    private int attempt;

    @ColumnInfo(name = "ofisFileName")
    private String ofisFileName;

    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    private byte[] fileData;

    public int getAttempt() {
        return attempt;
    }

    public void setAttempt(int attempt) {
        this.attempt = attempt;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public String getOfisFileName() {
        return ofisFileName;
    }

    public void setOfisFileName(String ofisFileName) {
        this.ofisFileName = ofisFileName;
    }

    public byte[] getFileData() {
        return fileData;
    }

    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public String getQuestionId() {
        return questionId;
    }

    public String getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(String farmerId) {
        this.farmerId = farmerId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }
}
