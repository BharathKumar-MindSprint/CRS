package com.olam.digital.ofis.mvvmmodule.model.followup;

import androidx.room.ColumnInfo;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class FollowUpItem{

	@SerializedName("questionId")
	private int questionId;

	@SerializedName("updatedBy")
	private String updatedBy;

	@SerializedName("dmCapturedProof")
	private List<String> dmCapturedProof;

	@SerializedName("isPortal")
	private boolean isPortal;

	@SerializedName("attempt")
	private int attempt;

	@SerializedName("createdAt")
	private String createdAt;

	@SerializedName("complianceId")
	private int complianceId;

	@SerializedName("createdBy")
	private String createdBy;

	@SerializedName("actionPlanId")
	private int actionPlanId;

	@SerializedName("actionPlanText")
	private String actionPlanText;

	@SerializedName("id")
	private int id;

	@SerializedName("moduleId")
	private int moduleId;

	@SerializedName("dmSubModId")
	private int dmSubModId;

	@SerializedName("status")
	private String status;

	@SerializedName("updatedAt")
	private String updatedAt;

	@ColumnInfo(name = "farmerId")
	public long farmerId;

	public long getFarmerId() {
		return farmerId;
	}

	public void setFarmerId(long farmerId) {
		this.farmerId = farmerId;
	}

	public int getQuestionId() {
		return questionId;
	}

	public void setQuestionId(int questionId) {
		this.questionId = questionId;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	public List<String> getDmCapturedProof() {
		return dmCapturedProof;
	}

	public void setDmCapturedProof(List<String> dmCapturedProof) {
		this.dmCapturedProof = dmCapturedProof;
	}

	public boolean isPortal() {
		return isPortal;
	}

	public void setPortal(boolean portal) {
		isPortal = portal;
	}

	public int getAttempt() {
		return attempt;
	}

	public void setAttempt(int attempt) {
		this.attempt = attempt;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	public int getComplianceId() {
		return complianceId;
	}

	public void setComplianceId(int complianceId) {
		this.complianceId = complianceId;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
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

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getModuleId() {
		return moduleId;
	}

	public void setModuleId(int moduleId) {
		this.moduleId = moduleId;
	}

	public int getDmSubModId() {
		return dmSubModId;
	}

	public void setDmSubModId(int dmSubModId) {
		this.dmSubModId = dmSubModId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(String updatedAt) {
		this.updatedAt = updatedAt;
	}
}