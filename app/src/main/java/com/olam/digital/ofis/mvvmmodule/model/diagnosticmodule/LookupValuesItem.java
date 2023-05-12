package com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class LookupValuesItem{

	@SerializedName("valueTr")
	private Object valueTr;

	@SerializedName("valueId")
	private Object valueId;

	@SerializedName("valueVi")
	private Object valueVi;

	@SerializedName("proofFormat")
	private String proofFormat;

	@SerializedName("valueTh")
	private Object valueTh;

	@SerializedName("isCaptureProof")
	private boolean isCaptureProof;

	@SerializedName("valueEs")
	private Object valueEs;

	@SerializedName("valueFr")
	private Object valueFr;

	@SerializedName("complianceId")
	private int complianceId;

	@SerializedName("compliance")
	private String compliance;

	@SerializedName("allowCustomActionPlan")
	private boolean allowCustomActionPlan;

	@SerializedName("valueLo")
	private Object valueLo;

	@SerializedName("actionPlans")
	private Object actionPlans;

	@SerializedName("id")
	private int id;

	@SerializedName("position")
	private int position;

	@SerializedName("valuePt")
	private Object valuePt;

	@SerializedName("value")
	private String value;

	public void setValueTr(Object valueTr){
		this.valueTr = valueTr;
	}

	public Object getValueTr(){
		return valueTr;
	}

	public void setValueId(Object valueId){
		this.valueId = valueId;
	}

	public Object getValueId(){
		return valueId;
	}

	public void setValueVi(Object valueVi){
		this.valueVi = valueVi;
	}

	public Object getValueVi(){
		return valueVi;
	}

	public void setProofFormat(String proofFormat){
		this.proofFormat = proofFormat;
	}

	public String getProofFormat(){
		return proofFormat;
	}

	public void setValueTh(Object valueTh){
		this.valueTh = valueTh;
	}

	public Object getValueTh(){
		return valueTh;
	}

	public void setIsCaptureProof(boolean isCaptureProof){
		this.isCaptureProof = isCaptureProof;
	}

	public boolean isIsCaptureProof(){
		return isCaptureProof;
	}

	public void setValueEs(Object valueEs){
		this.valueEs = valueEs;
	}

	public Object getValueEs(){
		return valueEs;
	}

	public void setValueFr(Object valueFr){
		this.valueFr = valueFr;
	}

	public Object getValueFr(){
		return valueFr;
	}

	public void setComplianceId(int complianceId){
		this.complianceId = complianceId;
	}

	public int getComplianceId(){
		return complianceId;
	}

	public void setCompliance(String compliance){
		this.compliance = compliance;
	}

	public String getCompliance(){
		return compliance;
	}

	public void setAllowCustomActionPlan(boolean allowCustomActionPlan){
		this.allowCustomActionPlan = allowCustomActionPlan;
	}

	public boolean isAllowCustomActionPlan(){
		return allowCustomActionPlan;
	}

	public void setValueLo(Object valueLo){
		this.valueLo = valueLo;
	}

	public Object getValueLo(){
		return valueLo;
	}

	public void setActionPlans(Object actionPlans){
		this.actionPlans = actionPlans;
	}

	public Object getActionPlans(){
		return actionPlans;
	}

	public void setId(int id){
		this.id = id;
	}

	public int getId(){
		return id;
	}

	public void setPosition(int position){
		this.position = position;
	}

	public int getPosition(){
		return position;
	}

	public void setValuePt(Object valuePt){
		this.valuePt = valuePt;
	}

	public Object getValuePt(){
		return valuePt;
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
			"LookupValuesItem{" + 
			"valueTr = '" + valueTr + '\'' + 
			",valueId = '" + valueId + '\'' + 
			",valueVi = '" + valueVi + '\'' + 
			",proofFormat = '" + proofFormat + '\'' + 
			",valueTh = '" + valueTh + '\'' + 
			",isCaptureProof = '" + isCaptureProof + '\'' + 
			",valueEs = '" + valueEs + '\'' + 
			",valueFr = '" + valueFr + '\'' + 
			",complianceId = '" + complianceId + '\'' + 
			",compliance = '" + compliance + '\'' + 
			",allowCustomActionPlan = '" + allowCustomActionPlan + '\'' + 
			",valueLo = '" + valueLo + '\'' + 
			",actionPlans = '" + actionPlans + '\'' + 
			",id = '" + id + '\'' + 
			",position = '" + position + '\'' + 
			",valuePt = '" + valuePt + '\'' + 
			",value = '" + value + '\'' + 
			"}";
		}
}