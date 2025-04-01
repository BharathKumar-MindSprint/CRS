package com.olam.digital.ofis.mvvmmodule.model.followup;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class AnswersItem{

	public AnswersItem(int answerId, int complianceStatusId, int questionId, String answerValue, String captureProofs, int actionPlanId, String actionPlanText, List<String> captureProof, String displayType) {
		this.answerId = answerId;
		this.complianceStatusId = complianceStatusId;
		this.questionId = questionId;
		this.answerValue = answerValue;
		this.captureProofs = captureProofs;
		this.actionPlanId = actionPlanId;
		this.actionPlanText = actionPlanText;
		this.captureProof = captureProof;
		this.displayType = displayType;
	}

	public int getAnswerId() {
		return answerId;
	}

	public void setAnswerId(int answerId) {
		this.answerId = answerId;
	}

	public int getComplianceStatusId() {
		return complianceStatusId;
	}

	public void setComplianceStatusId(int complianceStatusId) {
		this.complianceStatusId = complianceStatusId;
	}

	public int getQuestionId() {
		return questionId;
	}

	public void setQuestionId(int questionId) {
		this.questionId = questionId;
	}

	public String getAnswerValue() {
		return answerValue;
	}

	public void setAnswerValue(String answerValue) {
		this.answerValue = answerValue;
	}

	public String getCaptureProofs() {
		return captureProofs;
	}

	public void setCaptureProofs(String captureProofs) {
		this.captureProofs = captureProofs;
	}

	public int getActionPlanId() {
		return actionPlanId;
	}

	public void setActionPlanId(int actionPlanId) {
		this.actionPlanId = actionPlanId;
	}

	public String getActionPlanText() {
		return actionPlanText;
	}

	public void setActionPlanText(String actionPlanText) {
		this.actionPlanText = actionPlanText;
	}

	public List<String> getCaptureProof() {
		return captureProof;
	}

	public void setCaptureProof(List<String> captureProof) {
		this.captureProof = captureProof;
	}

	public String getDisplayType() {
		return displayType;
	}

	public void setDisplayType(String displayType) {
		this.displayType = displayType;
	}

	@SerializedName("answerId")
	private int answerId;

	@SerializedName("complianceStatusId")
	private int complianceStatusId;

	@SerializedName("questionId")
	private int questionId;

	@SerializedName("answerValue")
	private String answerValue;

	@SerializedName("captureProofs")
	private String  captureProofs;

	@SerializedName("actionPlanId")
	private int actionPlanId;

	@SerializedName("actionPlanText")
	private String actionPlanText;

	@SerializedName("captureProof")
	private List<String> captureProof;

	@SerializedName("displayType")
	private String displayType;

}