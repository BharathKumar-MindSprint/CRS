package com.olam.digital.ofis.Common.Constants;

public interface IAPIConstants {
   //REQUEST HELP
   String REQUEST_HELP = "ofismaster/master/requestHelp";
   //TRAINING APIS
   String GET_ALL_COURSES = "ofistraining/ofis/mobile/course/master/allcourses";
   String GET_ALL_MODULES ="ofistraining/ofis/mobile/course/module";
   String GET_COURSE_LANGUAGE = "ofistraining/ofis/mobile/course/master/courselanguage";
   String GET_ALL_DOCUMENTS = "ofistraining/ofis/mobile/course/module/assettype/language/files";
   String GET_ALL_COURSE_ASSET = "ofistraining/ofis/mobile/course/master/assetType";
   String GET_COURSE_FILE = "ofistraining/file/stream/{courseLanguageDocId}";
   String CREATE_TRAINING_RECORDS = "ofistraining/trainingrecords/create";
   //FARM PLAN API
   String GET_FARM_PLAN = "ofisreports/reports/farmplan";
   //Storage Connector API
   String STORAGE_CONNECTOR_DOWNLOAD =  "storageconnect/api/v1/common";
   //DM
   String GET_ALL_DIAGNOSTIC_MODULE_DATA = "ofissurvey/survey/diagnostic/getAllDiagnosticModuleData";
   String SUBMIT_DM_SURVEY = "ofissurvey/survey/diagnosticmodule/submitSurvey";
   String GET_ALL_DM_FOLLOWUP_DATA = "ofissurvey/survey/diagnosticmodule/getAllDiagnosticModuleData";
   //LOCALUNIT MASTER
   String GET_LOCAL_UNIT_MASTER = "ofissurvey/survey/getLocalUnitMasterData";
   //POLYGON HISTORY
   String GET_ALL_PHOTOCHECKBOX_IMAGES = "ofissurvey/survey/getPhotoCheckboxImages";
  //TODO Check if this API call is required
   String EXTERNAL_LOGIN = "ofismaster/master/externalLogin";
   //Farmer Registration
   String REGISTER_FARMER = "ofisfarmer/farmer/registerFarmer";
   String SAVE_FARMER_SIGNATURE="ofisfarmer/farmer/saveFarmerSignature";
   String UPDATE_SUBDISTRICT = "ofisfarmer/farmer/subDistrictForExistingFarmer";
   String CONSENT_STATEMENT = "ofisfarmerhousehold/farmerhousehold/consentStatement";
   String UPDATE_PHONENUMBERS = "ofisfarmer/farmer/updatephonenumbers";
   String UPLOAD_FARMER_FILE ="ofisutility/uploadFarmerFile";
   String GET_CERTIFICATIONS = "ofisfarmer/farmer/updateCertifications";
   String SAVE_NATIONAL_IDS = "ofisfarmer/farmer/saveFarmerUniqueId";
   String UPDATE_NATIONAL_ID = "ofisfarmer/farmer/updateFarmerUniqueId";
   String ADD_FARM = "ofisfarmer/farmer/addfarm";
   //SIP
   String SAVE_SIP_ANSWERS = "ofisfarmer-data/farmerdata/socialinfra/saveAllSipAnswers/";
   //STAKEHOLDER
   String REGISTER_MULTISTAKEHOLDER = "ofisfarmer/multiStakeHolder/farmer/stakeHolderRegistration";
   String UPDATE_STAKEHOLDER = "ofisfarmer/multiStakeHolder/updateStakeholder";
   //CLMRS
   String SAVE_CHILD_SURVEY = "ofischildsurvey/ofischildsurvey/childsurvey/save";
   String SAVE_CHILD_VISIT_DETAILS = "ofischildsurvey/ofischildsurvey/childavailability/saveall";
   String SAVE_CHILD = "ofischild/child/saveall";
   //SYNC TASKS
   String SYNC_TASKS_UPDATE = "ofistasks/activitieEs/tasks/sync_tasks/updates";
   //Household
    String FARMER_HOUSE_HOLD = "ofisfarmerhousehold/farmerhousehold/farmerHouseHold";
    //Enumerator GPS Tracker
    String ENUMERATOR_GPS_TRACKING = "ofisenumeratorgpstracker/gps-tracking/enumeratorgpstracking";
    //DM SURVEY
    String DM_SUBMIT_SURVEY =  "ofissurvey/survey/diagnosticmodule/submitSurvey";
    String DM_SAVE_FOLLOWUP_SURVEY = "ofissurvey/survey/diagnosticmodule/saveFollowUpData";
    //Survey
    String POST_SURVEY_IMAGE = "ofissurvey/image/upload";
    String SUBMIT_SURVEY= "ofissurvey/survey/submitSurveyAppModule";
    //CHECK APP VERSION
    String CHECK_APP_VERSION = "ofismaster/master/checkAppVersion";
    String ENUMERATOR_ASSIGNMENT_CHECK = "ofismaster/master/enumeratorAssignmentsCheck";
    String COUNTRY_SPINNERS ="ofisfarmer-data/farmerdata/getcountryspinners";
     String GET_ALL_QUESTIONNAIRE_DATA =  "ofissurvey/survey/questionnaire/getAllQuestionnaireData";
     String GET_ALL_QUESTIONNAIRE_DATA_OGA =  "ofissurvey/survey/crsSurvey/questionnaire/getAllQuestionnaireData";

     String GET_MODULE_IDS_BY_ENUMERATOR_ID = "ofissurvey/survey/questionnaire/getModuleIdsByEnumeratorId";

     String GET_ASSIGNED_FARMER_AGAINST_SURVEYS = "ofissurvey/survey/asd/getAssignedFarmerAgainstSurveys";

     //TODO: REmove the below two APIs
     String IS_CHILD_ENUMERATOR = "ofismaster/master/ischildenumerator";
     String IS_FARMER_HH_ENUMERATOR = "ofismaster/master/isFarmerHHEnumerator";
     String GET_ALL_FARM_ROLES =  "ofisfarmer/multifarm/roles";
     String GET_STAKEHOLDER_BY_ENUMERATORID ="ofisfarmer/multiStakeHolder/getStakeholderByenumeratorId";
     String GET_NATIONALIDS_BY_ENUMERATOR = "ofismaster/master/nationalId/getNationalIDsByEnumerator";
     String GET_ENUMERATOR_UNIQUEID = "ofisfarmer/farmer/getEnumeratorFarmersUniqueId";
     String SYNC_TRAINING_DATA = "ofistraining/training/synctrainingdata";
     String SYNC_TASKS ="ofistasks/activities/tasks/sync_tasks";
     String GET_FARMER_DETAILS_ENUMERATOR_ID =  "ofisfarmer-data/farmerdata/getfarmerdetailsbyenumeratorid";
     String FIND_CHILD_SUBMITTEDMODULES="ofischild/child/findsubmittedmodulesbyenumeter";
     String GET_CHILD_DETAILS = "ofischild/child/getdetailsbyenumaratorid?";
     String SAVE_DEVICE_INFO = "ofismaster/master/saveDeviceInfo";
     String SAVE_SURVEY_ANSWERS = "ofissurvey/survey/getRecentSurveyAnswers";
     String GET_ALL_SIP_QUESTIONNAIRE_DATA = "ofisfarmer-data/farmerdata/sipquestionnaire/getAllSIPQuestionnaireData";
     String GET_CONDITIONAL_DETAILS = "ofissurvey/survey/questionnaire/conditionalQuestionDetails";
     String GET_POLYGON_DATA_BY_PLOT_ID = "ofisfarmer-data/farmerdata/polygonDataByPlotIds";
    String GET_DISTRIBUTION_MODULE_DATA = "ofismaster/distribution-group/getDistributionDetails";
    String SAVE_DISTRIBUTION_FORM = "ofismaster/distribution-form/saveDistributionForm";
    String POST_UPDATE_FARMER = "ofisfarmer/farmer/updateFarmerDetailsFromApp";
    String SAVE_UPDATE_PHONE_NATIONAL = "ofisfarmer/farmer/saveOrUpdatePhNumAndNatId";

}
