package com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TextBoxModuleQuestions {

    public TextBoxModuleQuestions(int answerType, boolean isAllowNegative, boolean isAllowDecimal, double minimumRange, double maximumRange, List<DmRangeTextBoxModuleQuestions> dmRangeQuestionsList) {
        this.answerType = answerType;
        this.isAllowNegative = isAllowNegative;
        this.isAllowDecimal = isAllowDecimal;
        this.minimumRange = minimumRange;
        this.maximumRange = maximumRange;
        this.dmRangeQuestionsList = dmRangeQuestionsList;
    }

    public int getAnswerType() {
        return answerType;
    }

    public void setAnswerType(int answerType) {
        this.answerType = answerType;
    }

    public boolean isAllowNegative() {
        return isAllowNegative;
    }

    public void setAllowNegative(boolean allowNegative) {
        isAllowNegative = allowNegative;
    }

    public boolean isAllowDecimal() {
        return isAllowDecimal;
    }

    public void setAllowDecimal(boolean allowDecimal) {
        isAllowDecimal = allowDecimal;
    }

    public double getMinimumRange() {
        return minimumRange;
    }

    public void setMinimumRange(double minimumRange) {
        this.minimumRange = minimumRange;
    }

    public double getMaximumRange() {
        return maximumRange;
    }

    public void setMaximumRange(double maximumRange) {
        this.maximumRange = maximumRange;
    }

    public List<DmRangeTextBoxModuleQuestions> getDmRangeQuestionsList() {
        return dmRangeQuestionsList;
    }

    public void setDmRangeQuestionsList(List<DmRangeTextBoxModuleQuestions> dmRangeQuestionsList) {
        this.dmRangeQuestionsList = dmRangeQuestionsList;
    }

    @SerializedName("answerType")
    private int answerType;

    @SerializedName("isAllowNegative")
    private boolean isAllowNegative;

    @SerializedName("isAllowDecimal")
    private boolean isAllowDecimal;

    @SerializedName("minimumRange")
    private double minimumRange;

    @SerializedName("maximumRange")
    private double maximumRange;

    @SerializedName("dmRange")
    private List<DmRangeTextBoxModuleQuestions> dmRangeQuestionsList;
}
