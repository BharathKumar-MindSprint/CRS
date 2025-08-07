package com.olam.digital.ofis.mvvmmodule.model.followup;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.SerializedName;

public class ExistingModuleItem{

	@SerializedName("result")
	private Result result;

	@SerializedName("farmerId")
	private int farmerId;

	@SerializedName("enumeratorId")
	private String enumeratorId;

	@SerializedName("answers")
	private ArrayList<AnswersItem> answers;

	@SerializedName("submissionDate")
	private String submissionDate;

	@SerializedName("id")
	private int id;

	@SerializedName("farmerGroupId")
	private int farmerGroupId;

	@SerializedName("isMobile")
	private boolean isMobile;

	@SerializedName("moduleId")
	private int moduleId;

	@SerializedName("currencyId")
	private int currencyId;

	@SerializedName("followupCount")
	private int followUpCount;

	@SerializedName("isFollowupComplete")
	private String isFollowUpCompleted;

	@SerializedName("firstFollowupDate")
	private String firstFollowupDate;

	public Result getResult() {
		return result;
	}

	public void setResult(Result result) {
		this.result = result;
	}

	public int getFarmerId() {
		return farmerId;
	}

	public void setFarmerId(int farmerId) {
		this.farmerId = farmerId;
	}

	public String getEnumeratorId() {
		return enumeratorId;
	}

	public void setEnumeratorId(String enumeratorId) {
		this.enumeratorId = enumeratorId;
	}

	public ArrayList<AnswersItem> getAnswers() {
		return answers;
	}

	public void setAnswers(ArrayList<AnswersItem> answers) {
		this.answers = answers;
	}

	public String getSubmissionDate() {
		return submissionDate;
	}

	public void setSubmissionDate(String submissionDate) {
		this.submissionDate = submissionDate;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getFarmerGroupId() {
		return farmerGroupId;
	}

	public void setFarmerGroupId(int farmerGroupId) {
		this.farmerGroupId = farmerGroupId;
	}

	public boolean isMobile() {
		return isMobile;
	}

	public void setMobile(boolean mobile) {
		isMobile = mobile;
	}

	public int getModuleId() {
		return moduleId;
	}

	public void setModuleId(int moduleId) {
		this.moduleId = moduleId;
	}

	public int getCurrencyId() {
		return currencyId;
	}

	public void setCurrencyId(int currencyId) {
		this.currencyId = currencyId;
	}

	public int getFollowUpCount() {
		return followUpCount;
	}

	public void setFollowUpCount(int followUpCount) {
		this.followUpCount = followUpCount;
	}

	public String getIsFollowUpCompleted() {
		return isFollowUpCompleted;
	}

	public void setIsFollowUpCompleted(String isFollowUpCompleted) {
		this.isFollowUpCompleted = isFollowUpCompleted;
	}

	public String getFirstFollowupDate() {
		return firstFollowupDate;
	}

	public void setFirstFollowupDate(String firstFollowupDate) {
		this.firstFollowupDate = firstFollowupDate;
	}
}