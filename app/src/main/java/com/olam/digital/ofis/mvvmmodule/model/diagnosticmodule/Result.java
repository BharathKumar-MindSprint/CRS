package com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule;

public class Result{
	private int totalSatisfactory;
	private String resultValue;
	private int totalActionPlan;
	private String totalBudget;
	private int totalComplaint;
	private Object resultColour;
	private String resultType;
	private int totalNonComplaint;
	private String resultRemarks;

	public int getTotalSatisfactory(){
		return totalSatisfactory;
	}

	public String getResultValue(){
		return resultValue;
	}

	public int getTotalActionPlan(){
		return totalActionPlan;
	}

	public String getTotalBudget(){
		return totalBudget;
	}

	public int getTotalComplaint(){
		return totalComplaint;
	}

	public Object getResultColour(){
		return resultColour;
	}

	public String getResultType(){
		return resultType;
	}

	public int getTotalNonComplaint(){
		return totalNonComplaint;
	}

	public String getResultRemarks(){
		return resultRemarks;
	}
}
