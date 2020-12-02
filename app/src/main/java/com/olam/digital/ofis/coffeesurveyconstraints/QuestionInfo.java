package com.olam.digital.ofis.coffeesurveyconstraints;

import org.json.JSONObject;

public class QuestionInfo {
    private String dataType;
    private JSONObject displayConfig;
    private  int questionId;

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public JSONObject getDisplayConfig() {
        return displayConfig;
    }

    public void setDisplayConfig(JSONObject displayConfig) {
        this.displayConfig = displayConfig;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }
}
