package com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.olam.digital.ofis.farmplandownload.model.DownloadFormList;

import java.util.ArrayList;
import java.util.List;

public class ResponseModel {

    @SerializedName("diagnosticModules")
    ArrayList<DiagnosticModulesList> diagnosticModules;

    @SerializedName("answerTypes")
    private ArrayList<AnswerTypesItem> answerTypes;

    @SerializedName("questionTypes")
    private ArrayList<QuestionTypesItem> questionTypes;

    @SerializedName("currencies")
    private ArrayList<CurrenciesItem> currencies;

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @SerializedName("errorMessage")
    private String errorMessage;

    @SerializedName("plotMasterData")
    private List<DmMasterDataModel> DmMasterData;

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

    public List<DmMasterDataModel> getDmMasterData() {
        return DmMasterData;
    }

    public void setDmMasterData(List<DmMasterDataModel> dmMasterData) {
        DmMasterData = dmMasterData;
    }
}
