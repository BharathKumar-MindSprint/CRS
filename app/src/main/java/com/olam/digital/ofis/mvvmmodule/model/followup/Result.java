package com.olam.digital.ofis.mvvmmodule.model.followup;

import com.google.gson.annotations.SerializedName;

public class Result{

	@SerializedName("totalSatisfactory")
	private int totalSatisfactory;

	@SerializedName("resultValue")
	private String resultValue;

	@SerializedName("totalActionPlan")
	private int totalActionPlan;

	@SerializedName("totalBudget")
	private String totalBudget;

	@SerializedName("totalComplaint")
	private int totalComplaint;

	@SerializedName("resultColour")
	private String resultColour;

	@SerializedName("resultType")
	private String resultType;

	@SerializedName("totalNonComplaint")
	private int totalNonComplaint;

	@SerializedName("resultRemarks")
	private String resultRemarks;

	public String getOverAllResult() {
		return overAllResult;
	}

	public void setOverAllResult(String overAllResult) {
		this.overAllResult = overAllResult;
	}

	@SerializedName("overAllResult")
	private String overAllResult;

	public int getTotalSatisfactory() {
		return totalSatisfactory;
	}

	public void setTotalSatisfactory(int totalSatisfactory) {
		this.totalSatisfactory = totalSatisfactory;
	}

	public String getResultValue() {
		return resultValue;
	}

	public void setResultValue(String resultValue) {
		this.resultValue = resultValue;
	}

	public int getTotalActionPlan() {
		return totalActionPlan;
	}

	public void setTotalActionPlan(int totalActionPlan) {
		this.totalActionPlan = totalActionPlan;
	}

	public String getTotalBudget() {
		return totalBudget;
	}

	public void setTotalBudget(String totalBudget) {
		this.totalBudget = totalBudget;
	}

	public int getTotalComplaint() {
		return totalComplaint;
	}

	public void setTotalComplaint(int totalComplaint) {
		this.totalComplaint = totalComplaint;
	}

	public String getResultColour() {
		return resultColour;
	}

	public void setResultColour(String resultColour) {
		this.resultColour = resultColour;
	}

	public String getResultType() {
		return resultType;
	}

	public void setResultType(String resultType) {
		this.resultType = resultType;
	}

	public int getTotalNonComplaint() {
		return totalNonComplaint;
	}

	public void setTotalNonComplaint(int totalNonComplaint) {
		this.totalNonComplaint = totalNonComplaint;
	}

	public String getResultRemarks() {
		return resultRemarks;
	}

	public void setResultRemarks(String resultRemarks) {
		this.resultRemarks = resultRemarks;
	}




}