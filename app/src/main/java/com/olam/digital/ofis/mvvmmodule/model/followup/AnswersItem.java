package com.olam.digital.ofis.mvvmmodule.model.followup;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class AnswersItem{

	@SerializedName("answerId")
	private Object answerId;

	@SerializedName("complianceStatusId")
	private Object complianceStatusId;

	@SerializedName("questionId")
	private int questionId;

	@SerializedName("answerValue")
	private String answerValue;

	@SerializedName("captureProofs")
	private Object captureProofs;

	@SerializedName("actionPlanId")
	private int actionPlanId;

	@SerializedName("actionPlanText")
	private String actionPlanText;

	@SerializedName("captureProof")
	private Object captureProof;

	public Object getAnswerId(){
		return answerId;
	}

	public Object getComplianceStatusId(){
		return complianceStatusId;
	}

	public int getQuestionId(){
		return questionId;
	}

	public String getAnswerValue(){
		return answerValue;
	}

	public Object getCaptureProofs(){
		return captureProofs;
	}

	public int getActionPlanId(){
		return actionPlanId;
	}

	public String getActionPlanText(){
		return actionPlanText;
	}

	public Object getCaptureProof(){
		return captureProof;
	}
}