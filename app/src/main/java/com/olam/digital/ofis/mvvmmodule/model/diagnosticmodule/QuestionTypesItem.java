package com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule;

import com.google.gson.annotations.SerializedName;

public class QuestionTypesItem{

	@SerializedName("id")
	private int id;

	@SerializedName("value")
	private String value;

	public void setId(int id){
		this.id = id;
	}

	public int getId(){
		return id;
	}

	public void setValue(String value){
		this.value = value;
	}

	public String getValue(){
		return value;
	}

	@Override
 	public String toString(){
		return 
			"QuestionTypesItem{" + 
			"id = '" + id + '\'' + 
			",value = '" + value + '\'' + 
			"}";
		}
}