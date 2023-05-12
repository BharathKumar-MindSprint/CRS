package com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule;

import com.google.gson.annotations.SerializedName;

public class ComplianceIndicatorItem{

	@SerializedName("moduleDraftId")
	private Object moduleDraftId;

	@SerializedName("labelTh")
	private Object labelTh;

	@SerializedName("labelPt")
	private Object labelPt;

	@SerializedName("compliance")
	private String compliance;

	@SerializedName("labelIdn")
	private Object labelIdn;

	@SerializedName("labelEs")
	private Object labelEs;

	@SerializedName("labelTr")
	private Object labelTr;

	@SerializedName("id")
	private int id;

	@SerializedName("label")
	private String label;

	@SerializedName("labelFr")
	private Object labelFr;

	@SerializedName("labelLo")
	private Object labelLo;

	@SerializedName("labelVi")
	private Object labelVi;

	public void setModuleDraftId(Object moduleDraftId){
		this.moduleDraftId = moduleDraftId;
	}

	public Object getModuleDraftId(){
		return moduleDraftId;
	}

	public void setLabelTh(Object labelTh){
		this.labelTh = labelTh;
	}

	public Object getLabelTh(){
		return labelTh;
	}

	public void setLabelPt(Object labelPt){
		this.labelPt = labelPt;
	}

	public Object getLabelPt(){
		return labelPt;
	}

	public void setCompliance(String compliance){
		this.compliance = compliance;
	}

	public String getCompliance(){
		return compliance;
	}

	public void setLabelIdn(Object labelIdn){
		this.labelIdn = labelIdn;
	}

	public Object getLabelIdn(){
		return labelIdn;
	}

	public void setLabelEs(Object labelEs){
		this.labelEs = labelEs;
	}

	public Object getLabelEs(){
		return labelEs;
	}

	public void setLabelTr(Object labelTr){
		this.labelTr = labelTr;
	}

	public Object getLabelTr(){
		return labelTr;
	}

	public void setId(int id){
		this.id = id;
	}

	public int getId(){
		return id;
	}

	public void setLabel(String label){
		this.label = label;
	}

	public String getLabel(){
		return label;
	}

	public void setLabelFr(Object labelFr){
		this.labelFr = labelFr;
	}

	public Object getLabelFr(){
		return labelFr;
	}

	public void setLabelLo(Object labelLo){
		this.labelLo = labelLo;
	}

	public Object getLabelLo(){
		return labelLo;
	}

	public void setLabelVi(Object labelVi){
		this.labelVi = labelVi;
	}

	public Object getLabelVi(){
		return labelVi;
	}

	@Override
 	public String toString(){
		return 
			"ComplianceIndicatorItem{" + 
			"moduleDraftId = '" + moduleDraftId + '\'' + 
			",labelTh = '" + labelTh + '\'' + 
			",labelPt = '" + labelPt + '\'' + 
			",compliance = '" + compliance + '\'' + 
			",labelIdn = '" + labelIdn + '\'' + 
			",labelEs = '" + labelEs + '\'' + 
			",labelTr = '" + labelTr + '\'' + 
			",id = '" + id + '\'' + 
			",label = '" + label + '\'' + 
			",labelFr = '" + labelFr + '\'' + 
			",labelLo = '" + labelLo + '\'' + 
			",labelVi = '" + labelVi + '\'' + 
			"}";
		}
}