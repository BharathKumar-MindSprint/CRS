package com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.olam.digital.ofis.farmplandownload.model.DownloadFormList;

import java.util.ArrayList;
import java.util.List;

public class ResponseModel {

    @SerializedName("diagnosticModules")
    @Expose
    ArrayList<DiagnosticModulesList> diagnosticModules;

    @SerializedName("answerTypes")
    @Expose
    private ArrayList<AnswerTypesItem> answerTypes;

    @SerializedName("questionTypes")
    @Expose
    private ArrayList<QuestionTypesItem> questionTypes;

    @SerializedName("currencies")
    @Expose
    private ArrayList<CurrenciesItem> currencies;

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @SerializedName("errorMessage")
    @Expose
    private String errorMessage;

    public ArrayList<AnswerTypesItem> getAnswerTypes() {
        return answerTypes;
    }

    public void setAnswerTypes(ArrayList<AnswerTypesItem> answerTypes) {
        this.answerTypes = answerTypes;
    }

    public ArrayList<QuestionTypesItem> getQuestionTypes() {
        return questionTypes;
    }

    public void setQuestionTypes(ArrayList<QuestionTypesItem> questionTypes) {
        this.questionTypes = questionTypes;
    }

    public ArrayList<CurrenciesItem> getCurrencies() {
        return currencies;
    }

    public void setCurrencies(ArrayList<CurrenciesItem> currencies) {
        this.currencies = currencies;
    }


    public ResponseModel(ArrayList<DiagnosticModulesList> diagnosticModules) {
        this.diagnosticModules = diagnosticModules;
    }

    public ArrayList<DiagnosticModulesList> getDiagnosticModules() {
        return diagnosticModules;
    }

    public void setDiagnosticModules(ArrayList<DiagnosticModulesList> diagnosticModules) {
        this.diagnosticModules = diagnosticModules;
    }


}
