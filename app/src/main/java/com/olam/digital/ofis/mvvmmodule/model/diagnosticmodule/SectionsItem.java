package com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class SectionsItem{

	@SerializedName("nameTh")
	private Object nameTh;

	@SerializedName("nameVi")
	private Object nameVi;

	@SerializedName("descriptionPt")
	private Object descriptionPt;

	@SerializedName("nameEs")
	private Object nameEs;

	@SerializedName("nameFr")
	private Object nameFr;

	@SerializedName("questions")
	private List<QuestionsItem> questions;

	@SerializedName("description")
	private String description;

	@SerializedName("descriptionVi")
	private Object descriptionVi;

	@SerializedName("descriptionFr")
	private Object descriptionFr;

	@SerializedName("descriptionEs")
	private Object descriptionEs;

	@SerializedName("namePt")
	private Object namePt;

	@SerializedName("nameLo")
	private Object nameLo;

	@SerializedName("isDefault")
	private boolean isDefault;

	@SerializedName("showDescription")
	private boolean showDescription;

	@SerializedName("descriptionTh")
	private Object descriptionTh;

	@SerializedName("nameTr")
	private Object nameTr;

	@SerializedName("name")
	private String name;

	@SerializedName("nameId")
	private Object nameId;

	@SerializedName("id")
	private int id;

	@SerializedName("position")
	private int position;

	@SerializedName("descriptionLo")
	private Object descriptionLo;

	@SerializedName("moduleId")
	private int moduleId;

	@SerializedName("descriptionIdn")
	private Object descriptionIdn;

	@SerializedName("descriptionTr")
	private Object descriptionTr;

	public void setNameTh(Object nameTh){
		this.nameTh = nameTh;
	}

	public Object getNameTh(){
		return nameTh;
	}

	public void setNameVi(Object nameVi){
		this.nameVi = nameVi;
	}

	public Object getNameVi(){
		return nameVi;
	}

	public void setDescriptionPt(Object descriptionPt){
		this.descriptionPt = descriptionPt;
	}

	public Object getDescriptionPt(){
		return descriptionPt;
	}

	public void setNameEs(Object nameEs){
		this.nameEs = nameEs;
	}

	public Object getNameEs(){
		return nameEs;
	}

	public void setNameFr(Object nameFr){
		this.nameFr = nameFr;
	}

	public Object getNameFr(){
		return nameFr;
	}

	public void setQuestions(List<QuestionsItem> questions){
		this.questions = questions;
	}

	public List<QuestionsItem> getQuestions(){
		return questions;
	}

	public void setDescription(String description){
		this.description = description;
	}

	public String getDescription(){
		return description;
	}

	public void setDescriptionVi(Object descriptionVi){
		this.descriptionVi = descriptionVi;
	}

	public Object getDescriptionVi(){
		return descriptionVi;
	}

	public void setDescriptionFr(Object descriptionFr){
		this.descriptionFr = descriptionFr;
	}

	public Object getDescriptionFr(){
		return descriptionFr;
	}

	public void setDescriptionEs(Object descriptionEs){
		this.descriptionEs = descriptionEs;
	}

	public Object getDescriptionEs(){
		return descriptionEs;
	}

	public void setNamePt(Object namePt){
		this.namePt = namePt;
	}

	public Object getNamePt(){
		return namePt;
	}

	public void setNameLo(Object nameLo){
		this.nameLo = nameLo;
	}

	public Object getNameLo(){
		return nameLo;
	}

	public void setIsDefault(boolean isDefault){
		this.isDefault = isDefault;
	}

	public boolean isIsDefault(){
		return isDefault;
	}

	public void setShowDescription(boolean showDescription){
		this.showDescription = showDescription;
	}

	public boolean isShowDescription(){
		return showDescription;
	}

	public void setDescriptionTh(Object descriptionTh){
		this.descriptionTh = descriptionTh;
	}

	public Object getDescriptionTh(){
		return descriptionTh;
	}

	public void setNameTr(Object nameTr){
		this.nameTr = nameTr;
	}

	public Object getNameTr(){
		return nameTr;
	}

	public void setName(String name){
		this.name = name;
	}

	public String getName(){
		return name;
	}

	public void setNameId(Object nameId){
		this.nameId = nameId;
	}

	public Object getNameId(){
		return nameId;
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

	public void setDescriptionLo(Object descriptionLo){
		this.descriptionLo = descriptionLo;
	}

	public Object getDescriptionLo(){
		return descriptionLo;
	}

	public void setModuleId(int moduleId){
		this.moduleId = moduleId;
	}

	public int getModuleId(){
		return moduleId;
	}

	public void setDescriptionIdn(Object descriptionIdn){
		this.descriptionIdn = descriptionIdn;
	}

	public Object getDescriptionIdn(){
		return descriptionIdn;
	}

	public void setDescriptionTr(Object descriptionTr){
		this.descriptionTr = descriptionTr;
	}

	public Object getDescriptionTr(){
		return descriptionTr;
	}

	@Override
 	public String toString(){
		return 
			"SectionsItem{" + 
			"nameTh = '" + nameTh + '\'' + 
			",nameVi = '" + nameVi + '\'' + 
			",descriptionPt = '" + descriptionPt + '\'' + 
			",nameEs = '" + nameEs + '\'' + 
			",nameFr = '" + nameFr + '\'' + 
			",questions = '" + questions + '\'' + 
			",description = '" + description + '\'' + 
			",descriptionVi = '" + descriptionVi + '\'' + 
			",descriptionFr = '" + descriptionFr + '\'' + 
			",descriptionEs = '" + descriptionEs + '\'' + 
			",namePt = '" + namePt + '\'' + 
			",nameLo = '" + nameLo + '\'' + 
			",isDefault = '" + isDefault + '\'' + 
			",showDescription = '" + showDescription + '\'' + 
			",descriptionTh = '" + descriptionTh + '\'' + 
			",nameTr = '" + nameTr + '\'' + 
			",name = '" + name + '\'' + 
			",nameId = '" + nameId + '\'' + 
			",id = '" + id + '\'' + 
			",position = '" + position + '\'' + 
			",descriptionLo = '" + descriptionLo + '\'' + 
			",moduleId = '" + moduleId + '\'' + 
			",descriptionIdn = '" + descriptionIdn + '\'' + 
			",descriptionTr = '" + descriptionTr + '\'' + 
			"}";
		}
}