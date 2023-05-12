package com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule;

import com.google.gson.annotations.SerializedName;

public class ActionPlansItem{

	@SerializedName("actionPlan")
	private String actionPlan;

	@SerializedName("isDefault")
	private boolean isDefault;

	@SerializedName("id")
	private int id;

	public void setActionPlan(String actionPlan){
		this.actionPlan = actionPlan;
	}

	public String getActionPlan(){
		return actionPlan;
	}

	public void setIsDefault(boolean isDefault){
		this.isDefault = isDefault;
	}

	public boolean isIsDefault(){
		return isDefault;
	}

	public void setId(int id){
		this.id = id;
	}

	public int getId(){
		return id;
	}

	@Override
 	public String toString(){
		return 
			"ActionPlansItem{" + 
			"actionPlan = '" + actionPlan + '\'' + 
			",isDefault = '" + isDefault + '\'' + 
			",id = '" + id + '\'' + 
			"}";
		}
}