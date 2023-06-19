package com.olam.digital.ofis.mvvmmodule.model.followup;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.SerializedName;

public class FollowUpResponseItem {

	@SerializedName("followUp")
	private ArrayList<FollowUpItem> followUpItemArrayList;

	@SerializedName("diagnosticModules")
	private ArrayList<ExistingModuleItem> existingModuleItemArrayList;


	public ArrayList<FollowUpItem> getFollowUpItemArrayList() {
		return followUpItemArrayList;
	}

	public void setFollowUpItemArrayList(ArrayList<FollowUpItem> followUpItemArrayList) {
		this.followUpItemArrayList = followUpItemArrayList;
	}


	public ArrayList<ExistingModuleItem> getExistingModuleItemArrayList() {
		return existingModuleItemArrayList;
	}

	public void setExistingModuleItemArrayList(ArrayList<ExistingModuleItem> existingModuleItemArrayList) {
		this.existingModuleItemArrayList = existingModuleItemArrayList;
	}


}