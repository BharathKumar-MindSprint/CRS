package com.olam.digital.ofis.mvvmmodule.roomdb.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

import java.util.List;

@Entity(tableName = "conditional_logic")
public class ConditionalLogic {

    @PrimaryKey
    @NonNull
    @SerializedName("id")
    private Integer id;

    @SerializedName("moduleId")
    private Integer moduleId;

    @SerializedName("subModuleId")
    private Integer subModuleId;

    @SerializedName("questionId")
    private Integer questionId;

    @SerializedName("isAnswered")
    private Boolean isAnswered;

    @SerializedName("conditionalId")
    private Integer conditionalId;

    @SerializedName("conditionalText")
    private String conditionalText;

    @SerializedName("lookUpValueIds")
    private String skipLookupIds;

    @SerializedName("skipQuestionIds")
    private String skipQuestions;

    public ConditionalLogic(@NonNull Integer id, Integer moduleId, Integer subModuleId, Integer questionId, Boolean isAnswered, Integer conditionalId, String conditionalText, String skipLookupIds, String skipQuestions) {
        this.id = id;
        this.moduleId = moduleId;
        this.subModuleId = subModuleId;
        this.questionId = questionId;
        this.isAnswered = isAnswered;
        this.conditionalId = conditionalId;
        this.conditionalText = conditionalText;
        this.skipLookupIds = skipLookupIds;
        this.skipQuestions = skipQuestions;
    }

    @NonNull
    public Integer getId() {
        return id;
    }

    public void setId(@NonNull Integer id) {
        this.id = id;
    }

    public Integer getModuleId() {
        return moduleId;
    }

    public void setModuleId(Integer moduleId) {
        this.moduleId = moduleId;
    }

    public Integer getSubModuleId() {
        return subModuleId;
    }

    public void setSubModuleId(Integer subModuleId) {
        this.subModuleId = subModuleId;
    }

    public Integer getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Integer questionId) {
        this.questionId = questionId;
    }

    public Boolean getAnswered() {
        return isAnswered;
    }

    public void setAnswered(Boolean answered) {
        isAnswered = answered;
    }

    public Integer getConditionalId() {
        return conditionalId;
    }

    public void setConditionalId(Integer conditionalId) {
        this.conditionalId = conditionalId;
    }

    public String getConditionalText() {
        return conditionalText;
    }

    public void setConditionalText(String conditionalText) {
        this.conditionalText = conditionalText;
    }

    public String getSkipLookupIds() {
        return skipLookupIds;
    }

    public void setSkipLookupIds(String skipLookupIds) {
        this.skipLookupIds = skipLookupIds;
    }

    public String getSkipQuestions() {
        return skipQuestions;
    }

    public void setSkipQuestions(String skipQuestions) {
        this.skipQuestions = skipQuestions;
    }
}
