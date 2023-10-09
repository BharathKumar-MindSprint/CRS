package com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class SectionsItem{

	@SerializedName("nameTh")
	private String nameTh;

	@SerializedName("nameVi")
	private String nameVi;

	@SerializedName("namePt")
	private String namePt;

	@SerializedName("nameLo")
	private String nameLo;

	@SerializedName("nameEs")
	private String nameEs;

	@SerializedName("nameFr")
	private String nameFr;

	@SerializedName("nameTr")
	private String nameTr;

	@SerializedName("name")
	private String name;

	@SerializedName("nameId")
	private String nameId;

	@SerializedName("questions")
	private List<QuestionsItem> questions;

	@SerializedName("description")
	private String description;

	@SerializedName("descriptionPt")
	private String descriptionPt;

	@SerializedName("descriptionVi")
	private String descriptionVi;

	@SerializedName("descriptionFr")
	private String descriptionFr;

	@SerializedName("descriptionEs")
	private String descriptionEs;

	@SerializedName("descriptionTh")
	private String descriptionTh;

	@SerializedName("descriptionLo")
	private String descriptionLo;
	@SerializedName("descriptionIdn")
	private String descriptionIdn;

	@SerializedName("descriptionTr")
	private String descriptionTr;

	@SerializedName("isDefault")
	private boolean isDefault;

	@SerializedName("showDescription")
	private boolean showDescription;

	@SerializedName("id")
	private int id;

	@SerializedName("position")
	private int position;


	@SerializedName("moduleId")
	private int moduleId;

	public String getNameTh() {
		return nameTh;
	}

	public void setNameTh(String nameTh) {
		this.nameTh = nameTh;
	}

	public String getNameVi() {
		return nameVi;
	}

	public void setNameVi(String nameVi) {
		this.nameVi = nameVi;
	}

	public String getNamePt() {
		return namePt;
	}

	public void setNamePt(String namePt) {
		this.namePt = namePt;
	}

	public String getNameLo() {
		return nameLo;
	}

	public void setNameLo(String nameLo) {
		this.nameLo = nameLo;
	}

	public String getNameEs() {
		return nameEs;
	}

	public void setNameEs(String nameEs) {
		this.nameEs = nameEs;
	}

	public String getNameFr() {
		return nameFr;
	}

	public void setNameFr(String nameFr) {
		this.nameFr = nameFr;
	}

	public String getNameTr() {
		return nameTr;
	}

	public void setNameTr(String nameTr) {
		this.nameTr = nameTr;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNameId() {
		return nameId;
	}

	public void setNameId(String nameId) {
		this.nameId = nameId;
	}

	public List<QuestionsItem> getQuestions() {
		return questions;
	}

	public void setQuestions(List<QuestionsItem> questions) {
		this.questions = questions;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescriptionPt() {
		return descriptionPt;
	}

	public void setDescriptionPt(String descriptionPt) {
		this.descriptionPt = descriptionPt;
	}

	public String getDescriptionVi() {
		return descriptionVi;
	}

	public void setDescriptionVi(String descriptionVi) {
		this.descriptionVi = descriptionVi;
	}

	public String getDescriptionFr() {
		return descriptionFr;
	}

	public void setDescriptionFr(String descriptionFr) {
		this.descriptionFr = descriptionFr;
	}

	public String getDescriptionEs() {
		return descriptionEs;
	}

	public void setDescriptionEs(String descriptionEs) {
		this.descriptionEs = descriptionEs;
	}

	public String getDescriptionTh() {
		return descriptionTh;
	}

	public void setDescriptionTh(String descriptionTh) {
		this.descriptionTh = descriptionTh;
	}

	public String getDescriptionLo() {
		return descriptionLo;
	}

	public void setDescriptionLo(String descriptionLo) {
		this.descriptionLo = descriptionLo;
	}

	public String getDescriptionIdn() {
		return descriptionIdn;
	}

	public void setDescriptionIdn(String descriptionIdn) {
		this.descriptionIdn = descriptionIdn;
	}

	public String getDescriptionTr() {
		return descriptionTr;
	}

	public void setDescriptionTr(String descriptionTr) {
		this.descriptionTr = descriptionTr;
	}

	public boolean isDefault() {
		return isDefault;
	}

	public void setDefault(boolean aDefault) {
		isDefault = aDefault;
	}

	public boolean isShowDescription() {
		return showDescription;
	}

	public void setShowDescription(boolean showDescription) {
		this.showDescription = showDescription;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public int getModuleId() {
		return moduleId;
	}

	public void setModuleId(int moduleId) {
		this.moduleId = moduleId;
	}

	public SectionsItem(String nameTh, String nameVi, String namePt, String nameLo, String nameEs, String nameFr, String nameTr, String name, String nameId, List<QuestionsItem> questions, String description, String descriptionPt, String descriptionVi, String descriptionFr, String descriptionEs, String descriptionTh, String descriptionLo, String descriptionIdn, String descriptionTr, boolean isDefault, boolean showDescription, int id, int position, int moduleId) {
		this.nameTh = nameTh;
		this.nameVi = nameVi;
		this.namePt = namePt;
		this.nameLo = nameLo;
		this.nameEs = nameEs;
		this.nameFr = nameFr;
		this.nameTr = nameTr;
		this.name = name;
		this.nameId = nameId;
		this.questions = questions;
		this.description = description;
		this.descriptionPt = descriptionPt;
		this.descriptionVi = descriptionVi;
		this.descriptionFr = descriptionFr;
		this.descriptionEs = descriptionEs;
		this.descriptionTh = descriptionTh;
		this.descriptionLo = descriptionLo;
		this.descriptionIdn = descriptionIdn;
		this.descriptionTr = descriptionTr;
		this.isDefault = isDefault;
		this.showDescription = showDescription;
		this.id = id;
		this.position = position;
		this.moduleId = moduleId;
	}




}