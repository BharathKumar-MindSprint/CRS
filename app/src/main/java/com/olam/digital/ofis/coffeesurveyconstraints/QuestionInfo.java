package com.olam.digital.ofis.coffeesurveyconstraints;

import org.json.JSONObject;

public class QuestionInfo {
    private String dataType;
    private  int questionId;


    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }
}
