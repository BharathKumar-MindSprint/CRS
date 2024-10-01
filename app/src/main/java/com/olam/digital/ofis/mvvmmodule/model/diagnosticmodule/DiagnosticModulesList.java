package com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule;

import androidx.room.ColumnInfo;

import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.annotations.SerializedName;

public class DiagnosticModulesList {

	@SerializedName("followUpDuration")
	private int followUpDuration;

	@SerializedName("assessmentType")
	private String assessmentType;

	@SerializedName("descriptionPt")
	private String descriptionPt;

	@SerializedName("nameFr")
	private String nameFr;

	@SerializedName("questions")
	private List<QuestionsItem> questions;

	@SerializedName("description")
	private String description;

	@SerializedName("nameIdn")
	private String nameIdn;

	@SerializedName("descriptionFr")
	private String descriptionFr;

	public List<AssessmentResultTypeItems> getAssessmentResultType() {
		return assessmentResultType;
	}

	public void setAssessmentResultType(List<AssessmentResultTypeItems> assessmentResultType) {
		this.assessmentResultType = assessmentResultType;
	}

	@SerializedName("complianceIndicator")
	private List<ComplianceIndicatorItem> complianceIndicator;

	@SerializedName("assessmentResultType")
	private List<AssessmentResultTypeItems> assessmentResultType;

	@SerializedName("namePt")
	private String namePt;

	@SerializedName("nameLo")
	private String nameLo;

	@SerializedName("nameTr")
	private String nameTr;

	@SerializedName("currency")
	private List<CurrencyItem> currency;

	@SerializedName("id")
	private int id;

	@SerializedName("surveyType")
	private int surveyType;

	@SerializedName("nameTh")
	private String nameTh;

	@SerializedName("nameVi")
	private String nameVi;

	@SerializedName("followUpAttempts")
	private int followUpAttempts;

	@SerializedName("followUpAttemptsVal")
	private String followUpAttemptsVal;

	@SerializedName("followUpDurationVal")
	private String followUpDurationVal;

	@SerializedName("nameEs")
	private String nameEs;

	@SerializedName("descriptionVi")
	private String descriptionVi;

	@SerializedName("descriptionEs")
	private String descriptionEs;

	@SerializedName("sections")
	private List<SectionsItem> sections;

	@SerializedName("recurrence")
	private Recurrence recurrence;

	@SerializedName("descriptionTh")
	private String descriptionTh;

	@SerializedName("name")
	private String name;

	@SerializedName("descriptionLo")
	private String descriptionLo;

	@SerializedName("descriptionIdn")
	private String descriptionIdn;

	@SerializedName("descriptionTr")
	private String descriptionTr;

	@SerializedName("startDate")
	private String startDate;

	@SerializedName("displayLogic")
	private String displayLogic;

	@SerializedName("recurrenceCount")
	public String recurrenceCount;

	@SerializedName("recurrenceStartDate")
	public String recurrenceStartDate;
	@SerializedName("recurrenceEndDate")
	public String recurrenceEndDate;

	@SerializedName("diagnosticTypeId")
	public Integer diagnosticTypeId;

	@SerializedName("eudrSpecificSurveyTypeId")
	public Integer plotSurveyType;

	@SerializedName("plotIds")
	public List<Integer> submittedPlotIds;

	@SerializedName("captureLocation")
	public String captureLocation;

	public String getRecurrenceStartDate() {
		return recurrenceStartDate;
	}

	public void setRecurrenceStartDate(String recurrenceStartDate) {
		this.recurrenceStartDate = recurrenceStartDate;
	}

	public String getRecurrenceEndDate() {
		return recurrenceEndDate;
	}

	public void setRecurrenceEndDate(String recurrenceEndDate) {
		this.recurrenceEndDate = recurrenceEndDate;
	}

	public String getRecurrenceCount() {
		return recurrenceCount;
	}

	public void setRecurrenceCount(String recurrenceCount) {
		this.recurrenceCount = recurrenceCount;
	}

	public int getMaxScore() {
		return maxScore;
	}


	public void setMaxScore(int maxScore) {
		this.maxScore = maxScore;
	}

	@SerializedName("maxScore")
	private int maxScore;

	public String getDisplayLogic() {
		return displayLogic;
	}

	public void setDisplayLogic(String displayLogic) {
		this.displayLogic = displayLogic;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public int getFollowUpDuration() {
		return followUpDuration;
	}

	public void setFollowUpDuration(int followUpDuration) {
		this.followUpDuration = followUpDuration;
	}

	public String getAssessmentType() {
		return assessmentType;
	}

	public void setAssessmentType(String assessmentType) {
		this.assessmentType = assessmentType;
	}

	public String getDescriptionPt() {
		return descriptionPt;
	}

	public void setDescriptionPt(String descriptionPt) {
		this.descriptionPt = descriptionPt;
	}

	public String getNameFr() {
		return nameFr;
	}

	public void setNameFr(String nameFr) {
		this.nameFr = nameFr;
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

	public String getNameIdn() {
		return nameIdn;
	}

	public void setNameIdn(String nameIdn) {
		this.nameIdn = nameIdn;
	}

	public String getDescriptionFr() {
		return descriptionFr;
	}

	public void setDescriptionFr(String descriptionFr) {
		this.descriptionFr = descriptionFr;
	}

	public List<ComplianceIndicatorItem> getComplianceIndicator() {
		return complianceIndicator;
	}

	public void setComplianceIndicator(List<ComplianceIndicatorItem> complianceIndicator) {
		this.complianceIndicator = complianceIndicator;
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

	public String getNameTr() {
		return nameTr;
	}

	public void setNameTr(String nameTr) {
		this.nameTr = nameTr;
	}

	public List<CurrencyItem> getCurrency() {
		return currency;
	}

	public void setCurrency(List<CurrencyItem> currency) {
		this.currency = currency;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getSurveyType() {
		return surveyType;
	}

	public void setSurveyType(int surveyType) {
		this.surveyType = surveyType;
	}

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

	public int getFollowUpAttempts() {
		return followUpAttempts;
	}

	public void setFollowUpAttempts(int followUpAttempts) {
		this.followUpAttempts = followUpAttempts;
	}

	public String getNameEs() {
		return nameEs;
	}

	public void setNameEs(String nameEs) {
		this.nameEs = nameEs;
	}

	public String getDescriptionVi() {
		return descriptionVi;
	}

	public void setDescriptionVi(String descriptionVi) {
		this.descriptionVi = descriptionVi;
	}

	public String getDescriptionEs() {
		return descriptionEs;
	}

	public void setDescriptionEs(String descriptionEs) {
		this.descriptionEs = descriptionEs;
	}

	public List<SectionsItem> getSections() {
		return sections;
	}

	public void setSections(List<SectionsItem> sections) {
		this.sections = sections;
	}

	public Recurrence getRecurrence() {
		return recurrence;
	}

	public void setRecurrence(Recurrence recurrence) {
		this.recurrence = recurrence;
	}

	public String getDescriptionTh() {
		return descriptionTh;
	}

	public void setDescriptionTh(String descriptionTh) {
		this.descriptionTh = descriptionTh;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public String getFollowUpAttemptsVal() {
		return followUpAttemptsVal;
	}

	public void setFollowUpAttemptsVal(String followUpAttemptsVal) {
		this.followUpAttemptsVal = followUpAttemptsVal;
	}

	public String getFollowUpDurationVal() {
		return followUpDurationVal;
	}

	public void setFollowUpDurationVal(String followUpDurationVal) {
		this.followUpDurationVal = followUpDurationVal;
	}

	public Integer getDiagnosticTypeId() {
		return diagnosticTypeId;
	}

	public void setDiagnosticTypeId(Integer diagnosticTypeId) {
		this.diagnosticTypeId = diagnosticTypeId;
	}

	public Integer getPlotSurveyType() {
		return plotSurveyType;
	}

	public void setPlotSurveyType(Integer plotSurveyType) {
		this.plotSurveyType = plotSurveyType;
	}

	public List<Integer> getSubmittedPlotIds() {
		return submittedPlotIds;
	}

	public void setSubmittedPlotIds(List<Integer> submittedPlotIds) {
		this.submittedPlotIds = submittedPlotIds;
	}

	public String getCaptureLocation() {
		return captureLocation;
	}

	public void setCaptureLocation(String captureLocation) {
		this.captureLocation = captureLocation;
	}

	public DiagnosticModulesList(int followUpDuration, String assessmentType, String descriptionPt, String nameFr, List<QuestionsItem> questions, String description, String nameIdn, String descriptionFr, List<ComplianceIndicatorItem> complianceIndicator, String namePt, String nameLo, String nameTr, List<CurrencyItem> currency, int id, int surveyType, String nameTh, String nameVi, int followUpAttempts, String nameEs, String descriptionVi, String descriptionEs, List<SectionsItem> sections, Recurrence recurrence, String descriptionTh, String name, String descriptionLo, String descriptionIdn, String descriptionTr , String recurrenceCount , String recurrenceStartDate , String recurrenceEndDate , String followUpAttemptsVal, String followUpDurationVal, int diagnosticType, int plotSurveyType, List<Integer> submittedPlotIds, String captureLocation) {
		this.followUpDuration = followUpDuration;
		this.assessmentType = assessmentType;
		this.descriptionPt = descriptionPt;
		this.nameFr = nameFr;
		this.questions = questions;
		this.description = description;
		this.nameIdn = nameIdn;
		this.descriptionFr = descriptionFr;
		this.complianceIndicator = complianceIndicator;
		this.namePt = namePt;
		this.nameLo = nameLo;
		this.nameTr = nameTr;
		this.currency = currency;
		this.id = id;
		this.surveyType = surveyType;
		this.nameTh = nameTh;
		this.nameVi = nameVi;
		this.followUpAttempts = followUpAttempts;
		this.nameEs = nameEs;
		this.descriptionVi = descriptionVi;
		this.descriptionEs = descriptionEs;
		this.sections = sections;
		this.recurrence = recurrence;
		this.descriptionTh = descriptionTh;
		this.name = name;
		this.descriptionLo = descriptionLo;
		this.descriptionIdn = descriptionIdn;
		this.descriptionTr = descriptionTr;
		this.recurrenceCount = recurrenceCount;
		this.recurrenceStartDate = recurrenceStartDate;
		this.recurrenceEndDate = recurrenceEndDate;
		this.followUpAttemptsVal = followUpAttemptsVal;
		this.followUpDurationVal = followUpDurationVal;
		this.diagnosticTypeId = diagnosticType;
		this.plotSurveyType = plotSurveyType;
		this.submittedPlotIds = submittedPlotIds;
		this.captureLocation = captureLocation;
	}




}