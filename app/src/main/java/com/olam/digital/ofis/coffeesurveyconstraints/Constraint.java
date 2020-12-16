package com.olam.digital.ofis.coffeesurveyconstraints;

public class Constraint {

    private boolean success;
    private String errorMsg;
    private  boolean overLimit;

    public boolean isOverLimit() {
        return overLimit;
    }

    public void setOverLimit(boolean overLimit) {
        this.overLimit = overLimit;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}


