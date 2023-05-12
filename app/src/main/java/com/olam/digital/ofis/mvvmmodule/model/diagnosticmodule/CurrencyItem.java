package com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule;

import com.google.gson.annotations.SerializedName;

public class CurrencyItem{

	@SerializedName("isDefault")
	private boolean isDefault;

	@SerializedName("moduleDraftId")
	private Object moduleDraftId;

	@SerializedName("currencyDraftId")
	private int currencyDraftId;

	@SerializedName("currencyId")
	private int currencyId;

	public void setIsDefault(boolean isDefault){
		this.isDefault = isDefault;
	}

	public boolean isIsDefault(){
		return isDefault;
	}

	public void setModuleDraftId(Object moduleDraftId){
		this.moduleDraftId = moduleDraftId;
	}

	public Object getModuleDraftId(){
		return moduleDraftId;
	}

	public void setCurrencyDraftId(int currencyDraftId){
		this.currencyDraftId = currencyDraftId;
	}

	public int getCurrencyDraftId(){
		return currencyDraftId;
	}

	public void setCurrencyId(int currencyId){
		this.currencyId = currencyId;
	}

	public int getCurrencyId(){
		return currencyId;
	}

	@Override
 	public String toString(){
		return 
			"CurrencyItem{" + 
			"isDefault = '" + isDefault + '\'' + 
			",moduleDraftId = '" + moduleDraftId + '\'' + 
			",currencyDraftId = '" + currencyDraftId + '\'' + 
			",currencyId = '" + currencyId + '\'' + 
			"}";
		}
}