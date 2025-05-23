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

   //FARM PLAN APIs
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
   String GET_POLYGON_DATA = "ofisfarmer-data/farmerdata/polygondatabyfarmerid/{farmer_id}";
   String GET_ALL_PHOTOCHECKBOX_IMAGES = "ofissurvey/survey/getPhotoCheckboxImages";

  //TODO Check if this API call is required
   String EXTERNAL_LOGIN = "ofismaster/master/externalLogin";
}
