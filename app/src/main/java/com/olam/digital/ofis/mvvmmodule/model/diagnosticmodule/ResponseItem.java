package com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule;

import java.util.List;

public class ResponseItem{
	private Result result;
	private int farmerId;
	private Object enumeratorId;
	private List<AnswersItem> answers;
	private String submissionDate;
	private int id;
	private int farmerGroupId;
	private Object isMobile;
	private int moduleId;
	private int currencyId;

	public Result getResult(){
		return result;
	}

	public int getFarmerId(){
		return farmerId;
	}

	public Object getEnumeratorId(){
		return enumeratorId;
	}

	public List<AnswersItem> getAnswers(){
		return answers;
	}

	public String getSubmissionDate(){
		return submissionDate;
	}

	public int getId(){
		return id;
	}

	public int getFarmerGroupId(){
		return farmerGroupId;
	}

	public Object getIsMobile(){
		return isMobile;
	}

	public int getModuleId(){
		return moduleId;
	}

	public int getCurrencyId(){
		return currencyId;
	}
}