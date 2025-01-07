package com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class QuestionsItem{

	@SerializedName("mandate")
	private boolean mandate;

	@SerializedName("maxSelection")
	private Integer maxSelection;

	@SerializedName("textBox")
	private TextBoxModuleQuestions textBoxModuleQuestions;

	@SerializedName("question")
	private String question;

	@SerializedName("questionTr")
	private String questionTr;

	@SerializedName("lookupValues")
	private List<LookupValuesItem> lookupValues;


	@SerializedName("questionPt")
	private String questionPt;

	@SerializedName("questionLo")
	private String questionLo;

	@SerializedName("displaytypeId")
	private int displaytypeId;

	@SerializedName("datatypeId")
	private int datatypeId;

	@SerializedName("isCriticalQuest")
	private boolean isCriticalQuest;

	@SerializedName("questionFr")
	private String questionFr;

	@SerializedName("displayType")
	private String displayType;

	@SerializedName("questionIdn")
	private String questionIdn;

	@SerializedName("questionEs")
	private String questionEs;

	@SerializedName("questionTh")
	private String questionTh;

	@SerializedName("id")
	private int id;

	@SerializedName("position")
	private int position;

	@SerializedName("questionVi")
	private String questionVi;

	@SerializedName("moduleId")
	private int moduleId;

	@SerializedName("submoduleId")
	private int submoduleId;

	public Integer getMaxSelection() {
		return maxSelection;
	}

	public void setMaxSelection(Integer maxSelection) {
		this.maxSelection = maxSelection;
	}

	public QuestionsItem(boolean mandate, TextBoxModuleQuestions textBoxModuleQuestions, String question, String questionTr, List<LookupValuesItem> lookupValues, String questionPt, String questionLo, int displaytypeId, int datatypeId, boolean isCriticalQuest, String questionFr, String displayType, String questionIdn, String questionEs, String questionTh, int id, int position, String questionVi, int moduleId, int submoduleId, Integer maxSelection) {
		this.mandate = mandate;
		this.textBoxModuleQuestions = textBoxModuleQuestions;
		this.question = question;
		this.questionTr = questionTr;
		this.lookupValues = lookupValues;
		this.questionPt = questionPt;
		this.questionLo = questionLo;
		this.displaytypeId = displaytypeId;
		this.datatypeId = datatypeId;
		this.isCriticalQuest = isCriticalQuest;
		this.questionFr = questionFr;
		this.displayType = displayType;
		this.questionIdn = questionIdn;
		this.questionEs = questionEs;
		this.questionTh = questionTh;
		this.id = id;
		this.position = position;
		this.questionVi = questionVi;
		this.moduleId = moduleId;
		this.submoduleId = submoduleId;
		this.maxSelection = maxSelection;
	}


	public boolean isMandate() {
		return mandate;
	}

	public void setMandate(boolean mandate) {
		this.mandate = mandate;
	}


	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public String getQuestionTr() {
		return questionTr;
	}

	public void setQuestionTr(String questionTr) {
		this.questionTr = questionTr;
	}

	public TextBoxModuleQuestions getTextBoxModuleQuestions() {
		return textBoxModuleQuestions;
	}

	public void setTextBoxModuleQuestions(TextBoxModuleQuestions textBoxModuleQuestions) {
		this.textBoxModuleQuestions = textBoxModuleQuestions;
	}

	public List<LookupValuesItem> getLookupValues() {
		return lookupValues;
	}

	public void setLookupValues(List<LookupValuesItem> lookupValues) {
		this.lookupValues = lookupValues;
	}

	public String getQuestionPt() {
		return questionPt;
	}

	public void setQuestionPt(String questionPt) {
		this.questionPt = questionPt;
	}

	public String getQuestionLo() {
		return questionLo;
	}

	public void setQuestionLo(String questionLo) {
		this.questionLo = questionLo;
	}

	public int getDisplaytypeId() {
		return displaytypeId;
	}

	public void setDisplaytypeId(int displaytypeId) {
		this.displaytypeId = displaytypeId;
	}

	public int getDatatypeId() {
		return datatypeId;
	}

	public void setDatatypeId(int datatypeId) {
		this.datatypeId = datatypeId;
	}

	public boolean isCriticalQuest() {
		return isCriticalQuest;
	}

	public void setCriticalQuest(boolean criticalQuest) {
		isCriticalQuest = criticalQuest;
	}

	public String getQuestionFr() {
		return questionFr;
	}

	public void setQuestionFr(String questionFr) {
		this.questionFr = questionFr;
	}

	public String getDisplayType() {
		return displayType;
	}

	public void setDisplayType(String displayType) {
		this.displayType = displayType;
	}

	public String getQuestionIdn() {
		return questionIdn;
	}

	public void setQuestionIdn(String questionIdn) {
		this.questionIdn = questionIdn;
	}

	public String getQuestionEs() {
		return questionEs;
	}

	public void setQuestionEs(String questionEs) {
		this.questionEs = questionEs;
	}

	public String getQuestionTh() {
		return questionTh;
	}

	public void setQuestionTh(String questionTh) {
		this.questionTh = questionTh;
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

	public String getQuestionVi() {
		return questionVi;
	}

	public void setQuestionVi(String questionVi) {
		this.questionVi = questionVi;
	}

	public int getModuleId() {
		return moduleId;
	}

	public void setModuleId(int moduleId) {
		this.moduleId = moduleId;
	}

	public int getSubmoduleId() {
		return submoduleId;
	}

	public void setSubmoduleId(int submoduleId) {
		this.submoduleId = submoduleId;
	}

}