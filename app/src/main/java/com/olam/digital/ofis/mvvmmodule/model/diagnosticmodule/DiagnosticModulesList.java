package com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class DiagnosticModulesList {

	@SerializedName("followUpDuration")
	private int followUpDuration;

	@SerializedName("assessmentType")
	private String assessmentType;

	@SerializedName("descriptionPt")
	private Object descriptionPt;

	@SerializedName("nameFr")
	private Object nameFr;

	@SerializedName("questions")
	private List<QuestionsItem> questions;

	@SerializedName("description")
	private String description;

	@SerializedName("nameIdn")
	private Object nameIdn;

	@SerializedName("descriptionFr")
	private Object descriptionFr;

	@SerializedName("complianceIndicator")
	private List<ComplianceIndicatorItem> complianceIndicator;

	@SerializedName("answerTypes")
	private List<AnswerTypesItem> answerTypes;

	@SerializedName("questionTypes")
	private List<QuestionTypesItem> questionTypes;

	@SerializedName("namePt")
	private Object namePt;

	@SerializedName("nameLo")
	private Object nameLo;

	@SerializedName("nameTr")
	private Object nameTr;

	@SerializedName("currency")
	private List<CurrencyItem> currency;

	@SerializedName("id")
	private int id;

	@SerializedName("surveyType")
	private int surveyType;

	@SerializedName("nameTh")
	private Object nameTh;

	@SerializedName("nameVi")
	private Object nameVi;

	@SerializedName("followUpAttempts")
	private int followUpAttempts;

	@SerializedName("nameEs")
	private Object nameEs;

	@SerializedName("descriptionVi")
	private Object descriptionVi;

	@SerializedName("descriptionEs")
	private Object descriptionEs;

	@SerializedName("sections")
	private List<SectionsItem> sections;

	@SerializedName("recurrence")
	private Recurrence recurrence;

	@SerializedName("descriptionTh")
	private Object descriptionTh;

	@SerializedName("name")
	private String name;

	@SerializedName("descriptionLo")
	private Object descriptionLo;

	@SerializedName("descriptionIdn")
	private Object descriptionIdn;

	@SerializedName("descriptionTr")
	private Object descriptionTr;

	@SerializedName("currencies")
	private List<CurrenciesItem> currencies;

	public void setFollowUpDuration(int followUpDuration){
		this.followUpDuration = followUpDuration;
	}

	public int getFollowUpDuration(){
		return followUpDuration;
	}

	public void setAssessmentType(String assessmentType){
		this.assessmentType = assessmentType;
	}

	public String getAssessmentType(){
		return assessmentType;
	}

	public void setDescriptionPt(Object descriptionPt){
		this.descriptionPt = descriptionPt;
	}

	public Object getDescriptionPt(){
		return descriptionPt;
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

	public void setNameIdn(Object nameIdn){
		this.nameIdn = nameIdn;
	}

	public Object getNameIdn(){
		return nameIdn;
	}

	public void setDescriptionFr(Object descriptionFr){
		this.descriptionFr = descriptionFr;
	}

	public Object getDescriptionFr(){
		return descriptionFr;
	}

	public void setComplianceIndicator(List<ComplianceIndicatorItem> complianceIndicator){
		this.complianceIndicator = complianceIndicator;
	}

	public List<ComplianceIndicatorItem> getComplianceIndicator(){
		return complianceIndicator;
	}

	public void setAnswerTypes(List<AnswerTypesItem> answerTypes){
		this.answerTypes = answerTypes;
	}

	public List<AnswerTypesItem> getAnswerTypes(){
		return answerTypes;
	}

	public void setQuestionTypes(List<QuestionTypesItem> questionTypes){
		this.questionTypes = questionTypes;
	}

	public List<QuestionTypesItem> getQuestionTypes(){
		return questionTypes;
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

	public void setNameTr(Object nameTr){
		this.nameTr = nameTr;
	}

	public Object getNameTr(){
		return nameTr;
	}

	public void setCurrency(List<CurrencyItem> currency){
		this.currency = currency;
	}

	public List<CurrencyItem> getCurrency(){
		return currency;
	}

	public void setId(int id){
		this.id = id;
	}

	public int getId(){
		return id;
	}

	public void setSurveyType(int surveyType){
		this.surveyType = surveyType;
	}

	public int getSurveyType(){
		return surveyType;
	}

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

	public void setFollowUpAttempts(int followUpAttempts){
		this.followUpAttempts = followUpAttempts;
	}

	public int getFollowUpAttempts(){
		return followUpAttempts;
	}

	public void setNameEs(Object nameEs){
		this.nameEs = nameEs;
	}

	public Object getNameEs(){
		return nameEs;
	}

	public void setDescriptionVi(Object descriptionVi){
		this.descriptionVi = descriptionVi;
	}

	public Object getDescriptionVi(){
		return descriptionVi;
	}

	public void setDescriptionEs(Object descriptionEs){
		this.descriptionEs = descriptionEs;
	}

	public Object getDescriptionEs(){
		return descriptionEs;
	}

	public void setSections(List<SectionsItem> sections){
		this.sections = sections;
	}

	public List<SectionsItem> getSections(){
		return sections;
	}

	public void setRecurrence(Recurrence recurrence){
		this.recurrence = recurrence;
	}

	public Recurrence getRecurrence(){
		return recurrence;
	}

	public void setDescriptionTh(Object descriptionTh){
		this.descriptionTh = descriptionTh;
	}

	public Object getDescriptionTh(){
		return descriptionTh;
	}

	public void setName(String name){
		this.name = name;
	}

	public String getName(){
		return name;
	}

	public void setDescriptionLo(Object descriptionLo){
		this.descriptionLo = descriptionLo;
	}

	public Object getDescriptionLo(){
		return descriptionLo;
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

	public void setCurrencies(List<CurrenciesItem> currencies){
		this.currencies = currencies;
	}

	public List<CurrenciesItem> getCurrencies(){
		return currencies;
	}

	@Override
 	public String toString(){
		return 
			"ResponseItem{" + 
			"followUpDuration = '" + followUpDuration + '\'' + 
			",assessmentType = '" + assessmentType + '\'' + 
			",descriptionPt = '" + descriptionPt + '\'' + 
			",nameFr = '" + nameFr + '\'' + 
			",questions = '" + questions + '\'' + 
			",description = '" + description + '\'' + 
			",nameIdn = '" + nameIdn + '\'' + 
			",descriptionFr = '" + descriptionFr + '\'' + 
			",complianceIndicator = '" + complianceIndicator + '\'' + 
			",answerTypes = '" + answerTypes + '\'' + 
			",questionTypes = '" + questionTypes + '\'' + 
			",namePt = '" + namePt + '\'' + 
			",nameLo = '" + nameLo + '\'' + 
			",nameTr = '" + nameTr + '\'' + 
			",currency = '" + currency + '\'' + 
			",id = '" + id + '\'' + 
			",surveyType = '" + surveyType + '\'' + 
			",nameTh = '" + nameTh + '\'' + 
			",nameVi = '" + nameVi + '\'' + 
			",followUpAttempts = '" + followUpAttempts + '\'' + 
			",nameEs = '" + nameEs + '\'' + 
			",descriptionVi = '" + descriptionVi + '\'' + 
			",descriptionEs = '" + descriptionEs + '\'' + 
			",sections = '" + sections + '\'' + 
			",recurrence = '" + recurrence + '\'' + 
			",descriptionTh = '" + descriptionTh + '\'' + 
			",name = '" + name + '\'' + 
			",descriptionLo = '" + descriptionLo + '\'' + 
			",descriptionIdn = '" + descriptionIdn + '\'' + 
			",descriptionTr = '" + descriptionTr + '\'' + 
			",currencies = '" + currencies + '\'' + 
			"}";
		}
}