package com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class Response{

	@SerializedName("Response")
	private List<ResponseItem> response;

	public void setResponse(List<ResponseItem> response){
		this.response = response;
	}

	public List<ResponseItem> getResponse(){
		return response;
	}

	@Override
 	public String toString(){
		return 
			"Response{" + 
			"response = '" + response + '\'' + 
			"}";
		}
}