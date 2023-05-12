package com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule;

import com.google.gson.annotations.SerializedName;

public class CurrenciesItem{

	@SerializedName("code")
	private String code;

	@SerializedName("notation")
	private String notation;

	@SerializedName("name")
	private String name;

	@SerializedName("currencyId")
	private int currencyId;

	public void setCode(String code){
		this.code = code;
	}

	public String getCode(){
		return code;
	}

	public void setNotation(String notation){
		this.notation = notation;
	}

	public String getNotation(){
		return notation;
	}

	public void setName(String name){
		this.name = name;
	}

	public String getName(){
		return name;
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
			"CurrenciesItem{" + 
			"code = '" + code + '\'' + 
			",notation = '" + notation + '\'' + 
			",name = '" + name + '\'' + 
			",currencyId = '" + currencyId + '\'' + 
			"}";
		}
}