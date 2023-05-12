package com.olam.digital.ofis.mvvmmodule.model.diagnosticmodule;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class QuestionsItem{

	@SerializedName("mandate")
	private boolean mandate;

	@SerializedName("textBox")
	private Object textBox;

	@SerializedName("question")
	private String question;

	@SerializedName("questionTr")
	private Object questionTr;

	@SerializedName("lookupValues")
	private List<LookupValuesItem> lookupValues;

	@SerializedName("questionPt")
	private Object questionPt;

	@SerializedName("questionLo")
	private Object questionLo;

	@SerializedName("questionTypeId")
	private int questionTypeId;

	@SerializedName("datatypeId")
	private Object datatypeId;

	@SerializedName("isCriticalQuest")
	private boolean isCriticalQuest;

	@SerializedName("questionFr")
	private Object questionFr;

	@SerializedName("displayType")
	private String displayType;

	@SerializedName("questionIdn")
	private Object questionIdn;

	@SerializedName("questionEs")
	private Object questionEs;

	@SerializedName("questionTh")
	private Object questionTh;

	@SerializedName("id")
	private int id;

	@SerializedName("position")
	private int position;

	@SerializedName("questionVi")
	private Object questionVi;

	@SerializedName("moduleId")
	private int moduleId;

	@SerializedName("submoduleId")
	private int submoduleId;

	public void setMandate(boolean mandate){
		this.mandate = mandate;
	}

	public boolean isMandate(){
		return mandate;
	}

	public void setTextBox(Object textBox){
		this.textBox = textBox;
	}

	public Object getTextBox(){
		return textBox;
	}

	public void setQuestion(String question){
		this.question = question;
	}

	public String getQuestion(){
		return question;
	}

	public void setQuestionTr(Object questionTr){
		this.questionTr = questionTr;
	}

	public Object getQuestionTr(){
		return questionTr;
	}

	public void setLookupValues(List<LookupValuesItem> lookupValues){
		this.lookupValues = lookupValues;
	}

	public List<LookupValuesItem> getLookupValues(){
		return lookupValues;
	}

	public void setQuestionPt(Object questionPt){
		this.questionPt = questionPt;
	}

	public Object getQuestionPt(){
		return questionPt;
	}

	public void setQuestionLo(Object questionLo){
		this.questionLo = questionLo;
	}

	public Object getQuestionLo(){
		return questionLo;
	}

	public void setQuestionTypeId(int questionTypeId){
		this.questionTypeId = questionTypeId;
	}

	public int getQuestionTypeId(){
		return questionTypeId;
	}

	public void setDatatypeId(Object datatypeId){
		this.datatypeId = datatypeId;
	}

	public Object getDatatypeId(){
		return datatypeId;
	}

	public void setIsCriticalQuest(boolean isCriticalQuest){
		this.isCriticalQuest = isCriticalQuest;
	}

	public boolean isIsCriticalQuest(){
		return isCriticalQuest;
	}

	public void setQuestionFr(Object questionFr){
		this.questionFr = questionFr;
	}

	public Object getQuestionFr(){
		return questionFr;
	}

	public void setDisplayType(String displayType){
		this.displayType = displayType;
	}

	public String getDisplayType(){
		return displayType;
	}

	public void setQuestionIdn(Object questionIdn){
		this.questionIdn = questionIdn;
	}

	public Object getQuestionIdn(){
		return questionIdn;
	}

	public void setQuestionEs(Object questionEs){
		this.questionEs = questionEs;
	}

	public Object getQuestionEs(){
		return questionEs;
	}

	public void setQuestionTh(Object questionTh){
		this.questionTh = questionTh;
	}

	public Object getQuestionTh(){
		return questionTh;
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

	public void setQuestionVi(Object questionVi){
		this.questionVi = questionVi;
	}

	public Object getQuestionVi(){
		return questionVi;
	}

	public void setModuleId(int moduleId){
		this.moduleId = moduleId;
	}

	public int getModuleId(){
		return moduleId;
	}

	public void setSubmoduleId(int submoduleId){
		this.submoduleId = submoduleId;
	}

	public int getSubmoduleId(){
		return submoduleId;
	}

	@Override
 	public String toString(){
		return 
			"QuestionsItem{" + 
			"mandate = '" + mandate + '\'' + 
			",textBox = '" + textBox + '\'' + 
			",question = '" + question + '\'' + 
			",questionTr = '" + questionTr + '\'' + 
			",lookupValues = '" + lookupValues + '\'' + 
			",questionPt = '" + questionPt + '\'' + 
			",questionLo = '" + questionLo + '\'' + 
			",questionTypeId = '" + questionTypeId + '\'' + 
			",datatypeId = '" + datatypeId + '\'' + 
			",isCriticalQuest = '" + isCriticalQuest + '\'' + 
			",questionFr = '" + questionFr + '\'' + 
			",displayType = '" + displayType + '\'' + 
			",questionIdn = '" + questionIdn + '\'' + 
			",questionEs = '" + questionEs + '\'' + 
			",questionTh = '" + questionTh + '\'' + 
			",id = '" + id + '\'' + 
			",position = '" + position + '\'' + 
			",questionVi = '" + questionVi + '\'' + 
			",moduleId = '" + moduleId + '\'' + 
			",submoduleId = '" + submoduleId + '\'' + 
			"}";
		}
}