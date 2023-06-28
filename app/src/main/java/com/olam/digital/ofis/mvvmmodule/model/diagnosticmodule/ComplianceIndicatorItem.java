package com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule;

import com.google.gson.annotations.SerializedName;

public class ComplianceIndicatorItem{

	@SerializedName("moduleDraftId")
	private int moduleDraftId;

	@SerializedName("labelTh")
	private String labelTh;

	@SerializedName("labelPt")
	private String labelPt;

	@SerializedName("compliance")
	private String compliance;

	@SerializedName("labelIdn")
	private String labelIdn;

	@SerializedName("labelEs")
	private String labelEs;

	@SerializedName("labelTr")
	private String labelTr;

	@SerializedName("id")
	private int id;

	@SerializedName("label")
	private String label;

	@SerializedName("labelFr")
	private String labelFr;

	@SerializedName("labelLo")
	private String labelLo;

	@SerializedName("labelVi")
	private String labelVi;

	@SerializedName("score")
	private int score;

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public int getModuleDraftId() {
		return moduleDraftId;
	}

	public void setModuleDraftId(int moduleDraftId) {
		this.moduleDraftId = moduleDraftId;
	}

	public String getLabelTh() {
		return labelTh;
	}

	public void setLabelTh(String labelTh) {
		this.labelTh = labelTh;
	}

	public String getLabelPt() {
		return labelPt;
	}

	public void setLabelPt(String labelPt) {
		this.labelPt = labelPt;
	}

	public String getCompliance() {
		return compliance;
	}

	public void setCompliance(String compliance) {
		this.compliance = compliance;
	}

	public String getLabelIdn() {
		return labelIdn;
	}

	public void setLabelIdn(String labelIdn) {
		this.labelIdn = labelIdn;
	}

	public String getLabelEs() {
		return labelEs;
	}

	public void setLabelEs(String labelEs) {
		this.labelEs = labelEs;
	}

	public String getLabelTr() {
		return labelTr;
	}

	public void setLabelTr(String labelTr) {
		this.labelTr = labelTr;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getLabelFr() {
		return labelFr;
	}

	public void setLabelFr(String labelFr) {
		this.labelFr = labelFr;
	}

	public String getLabelLo() {
		return labelLo;
	}

	public void setLabelLo(String labelLo) {
		this.labelLo = labelLo;
	}

	public String getLabelVi() {
		return labelVi;
	}

	public void setLabelVi(String labelVi) {
		this.labelVi = labelVi;
	}

	public ComplianceIndicatorItem(int moduleDraftId, String labelTh, String labelPt, String compliance, String labelIdn, String labelEs, String labelTr, int id, String label, String labelFr, String labelLo, String labelVi) {
		this.moduleDraftId = moduleDraftId;
		this.labelTh = labelTh;
		this.labelPt = labelPt;
		this.compliance = compliance;
		this.labelIdn = labelIdn;
		this.labelEs = labelEs;
		this.labelTr = labelTr;
		this.id = id;
		this.label = label;
		this.labelFr = labelFr;
		this.labelLo = labelLo;
		this.labelVi = labelVi;
	}


}