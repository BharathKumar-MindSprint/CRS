package com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule;

import com.google.gson.annotations.SerializedName;

public class CurrencyItem{

	@SerializedName("isDefault")
	private boolean isDefault;

	public CurrencyItem(boolean isDefault, int moduleDraftId, int currencyDraftId, int currencyId) {
		this.isDefault = isDefault;
		this.moduleDraftId = moduleDraftId;
		this.currencyDraftId = currencyDraftId;
		this.currencyId = currencyId;
	}

	@SerializedName("moduleDraftId")
	private int moduleDraftId;

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

	public void setModuleDraftId(int moduleDraftId){
		this.moduleDraftId = moduleDraftId;
	}

	public int getModuleDraftId(){
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


}