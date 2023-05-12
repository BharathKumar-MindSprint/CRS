package com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule;

import com.google.gson.annotations.SerializedName;

public class Recurrence{

	@SerializedName("recurrenceFrequency")
	private int recurrenceFrequency;

	@SerializedName("isCustomRecurrence")
	private boolean isCustomRecurrence;

	@SerializedName("recurrenceType")
	private String recurrenceType;

	public void setRecurrenceFrequency(int recurrenceFrequency){
		this.recurrenceFrequency = recurrenceFrequency;
	}

	public int getRecurrenceFrequency(){
		return recurrenceFrequency;
	}

	public void setIsCustomRecurrence(boolean isCustomRecurrence){
		this.isCustomRecurrence = isCustomRecurrence;
	}

	public boolean isIsCustomRecurrence(){
		return isCustomRecurrence;
	}

	public void setRecurrenceType(String recurrenceType){
		this.recurrenceType = recurrenceType;
	}

	public String getRecurrenceType(){
		return recurrenceType;
	}

	@Override
 	public String toString(){
		return 
			"Recurrence{" + 
			"recurrenceFrequency = '" + recurrenceFrequency + '\'' + 
			",isCustomRecurrence = '" + isCustomRecurrence + '\'' + 
			",recurrenceType = '" + recurrenceType + '\'' + 
			"}";
		}
}