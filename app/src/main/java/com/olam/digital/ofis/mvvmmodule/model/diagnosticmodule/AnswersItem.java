package com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule;

public class AnswersItem{
	private int answerId;
	private int complianceStatusId;
	private int questionId;
	private Object answerValue;
	private String captureProofs;
	private Object actionPlanId;
	private String actionPlanText;
	private Object captureProof;

	public int getAnswerId(){
		return answerId;
	}

	public int getComplianceStatusId(){
		return complianceStatusId;
	}

	public int getQuestionId(){
		return questionId;
	}

	public Object getAnswerValue(){
		return answerValue;
	}

	public String getCaptureProofs(){
		return captureProofs;
	}

	public Object getActionPlanId(){
		return actionPlanId;
	}

	public String getActionPlanText(){
		return actionPlanText;
	}

	public Object getCaptureProof(){
		return captureProof;
	}
}
