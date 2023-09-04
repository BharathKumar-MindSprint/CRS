/*
 API URL constants
 */
package com.olam.digital.ofis.constants;

public interface IAPIConstants {
    String SAVE_DEVICE_INFO = "ofismaster/master/saveDeviceInfo";
    String SIP_QUESTIONAIRE_DATA = "ofisfarmer-data/farmerdata/sipquestionnaire/getAllSIPQuestionnaireData";
    String CHECK_APP_VERSION = "ofismaster/master/checkAppVersion";
    String ENUMERATOR_ASSIGNMENTS_CHECK = "ofismaster/master/enumeratorAssignmentsCheck";

    String EXTERNAL_LOGIN="ofismaster/master/externalLogin";

    String GET_COUNTRY_SPINNERS= "ofisfarmer-data/farmerdata/getcountryspinners";
    String GET_ALL_FL_BASIC_DETAILS = "farmer/getAllFlBasicDetails";

    String GET_ALL_QUESTIONNAIRE_DATA = "ofissurvey/survey/questionnaire/getAllQuestionnaireData";
    String IS_CHILD_ENUMERATOR = "ofismaster/master/ischildenumerator";

    String IS_FARMER_HH_ENUMERATOR = "ofismaster/master/isFarmerHHEnumerator";

    String ENABLE_MULTI_FARM_BY_ENUMERATOR="ofisfarmer/multifarm/enableMultiFarmByEnumerator";

    String MULTI_FARM_ROLES="ofisfarmer/multifarm/roles";

    String FARMER_INFO = "ofisfarmer/multifarm/farmerinfo";

    String IS_FL_ENUMERATOR ="ofismaster/master/isFLEnumerator";

    String GET_NATIONAL_IDS_BY_ENUMERATOR = "ofismaster/master/nationalId/getNationalIDsByEnumerator";

    String GET_ENUMERATOR_FARMERS_UNIQUE_ID = "ofisfarmer/farmer/getEnumeratorFarmersUniqueId";

    String SYNC_TRAINING_DATA = "ofistraining/training/synctrainingdata";

    String SYNC_TASKS = "ofistasks/activities/tasks/sync_tasks";

    String GET_FARMER_DETAILS_BY_ENUMERATOR_ID = "ofisfarmer-data/farmerdata/getfarmerdetailsbyenumeratorid";

    String FIND_SUBMITTED_MODULES_BY_ENUMERATOR = "ofischild/child/findsubmittedmodulesbyenumeter";

    String GET_RECENT_SURVEY_ANSWERS = "ofissurvey/survey/getRecentSurveyAnswers";

    String GET_DETAILS_BY_ENUMERATOR_ID = "ofischild/child/getdetailsbyenumaratorid";

    String MIGRATE_ROLE = "ofisoauth/api/v1/oauth/migrateRole";


    //NETWORK LAYER
    String SYNC_TASKS_UPDATES = "ofistasks/activities/tasks/sync_tasks/updates";

    String REQUEST_HELP = "ofismaster/master/requestHelp";

    String CREATE_TRAINING_RECORDS = "ofistraining/trainingrecords/create";

    String SUBMIT_SURVEY_APP_MODULE = "ofissurvey/survey/submitSurveyAppModule";

    String ADD_FARM ="ofisfarmer/farmer/addfarm";

    String FARMER_HOUSEHOLD = "ofisfarmerhousehold/farmerhousehold/farmerHouseHold";
    String SUB_DISTRICT_FOR_EXISTING_FARMER = "ofisfarmer/farmer/subDistrictForExistingFarmer";

    String CONSENT_STATEMENT ="ofisfarmerhousehold/farmerhousehold/consentStatement";

    String UPDATE_PHONE_NUMBERS = "ofisfarmer/farmer/updatephonenumbers";

    String UPLOAD_FARMER_UTILITY = "ofisutility/uploadFarmerFile";

    String CHILD_SAVE_ALL="ofischild/child/saveall";

    String REGISTER_FARMER = "ofisfarmer/farmer/registerFarmer";

    //TODO Change to QueryParameter
    String REGISTER_FARMER_RETRY = "ofisfarmer/farmer/registerFarmer?retry=true";

    String CHILD_SURVEY_SAVE = "ofischildsurvey/ofischildsurvey/childsurvey/save";

    String CHILD_AVAILABILITY_SAVE_ALL = "ofischildsurvey/ofischildsurvey/childavailability/saveall";

    String SAVE_FARMER_SIGNATURE = "ofisfarmer/farmer/saveFarmerSignature";

    String SAVE_FARMER_UNIQUE_ID = "ofisfarmer/farmer/saveFarmerUniqueId";

    String ENUMERATOR_GPS_TRACKING = "ofisenumeratorgpstracker/gps-tracking/enumeratorgpstracking";

    String MULTI_FARM_UPDATE = "ofisfarmer/multifarm/update";

    String DIAGNOSTIC_MODULE_SUBMIT_SURVEY = "ofissurvey/survey/diagnosticmodule/submitSurvey";

    String DIAGNOSTIC_MODULE_SAVE_FOLLOWUP_DATA= "ofissurvey/survey/diagnosticmodule/saveFollowUpData";

    String UPLOAD_IMAGE = "ofissurvey/image/upload";

    String SAVE_ALL_SIP_DATA = "ofisfarmer-data/farmerdata/socialinfra/saveAllSipAnswers";

}
