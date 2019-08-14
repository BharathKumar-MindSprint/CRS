package com.olam.digital.ofis;

import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by bengallienne on 29/11/14. - update
 * For access to all required column and table names in the SQLite database
 */
public class OFISDBContract {

    public static abstract class ModuleEntry implements BaseColumns {
        public static final String TABLE_NAME = "modules";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_NAME_FR = "name_fr";
        public static final String COLUMN_NAME_NAME_ES = "name_es";
        public static final String COLUMN_NAME_NAME_ID = "name_id";
        public static final String COLUMN_NAME_NAME_PT = "name_pt";
        public static final String COLUMN_NAME_NAME_TR = "name_tr";
        public static final String COLUMN_NAME_NAME_LO = "name_lo";
        public static final String COLUMN_NAME_NAME_VI = "name_vi";
        public static final String COLUMN_NAME_NAME_TH = "name_th";
        public static final String COLUMN_NAME_EXPIRES = "expires";
        public static final String COLUMN_NAME_ANNUAL_SURVEY = "annual_survey";
        public static final String COLUMN_NAME_MODULE_CODE = "moduleCode";

        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_ID);
                    add(COLUMN_NAME_NAME);
                    add(COLUMN_NAME_NAME_FR);
                    add(COLUMN_NAME_NAME_ES);
                    add(COLUMN_NAME_NAME_ID);
                    add(COLUMN_NAME_NAME_PT);
                    add(COLUMN_NAME_NAME_TR);
                    add(COLUMN_NAME_NAME_LO);
                    add(COLUMN_NAME_NAME_VI);
                    add(COLUMN_NAME_NAME_TH);
                    add(COLUMN_NAME_EXPIRES);
                    add(COLUMN_NAME_ANNUAL_SURVEY);
                    add(COLUMN_NAME_MODULE_CODE);
                }}
        );
    }
    public static abstract class SubmoduleEntry implements BaseColumns {
        public static final String TABLE_NAME = "submodules";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_MODULE_ID = "module_id";
        public static final String COLUMN_NAME_POSITION = "position";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_NAME_FR = "name_fr";
        public static final String COLUMN_NAME_NAME_ES = "name_es";
        public static final String COLUMN_NAME_NAME_ID = "name_id";
        public static final String COLUMN_NAME_NAME_PT = "name_pt";
        public static final String COLUMN_NAME_NAME_TR = "name_tr";
        public static final String COLUMN_NAME_NAME_LO = "name_lo";
        public static final String COLUMN_NAME_NAME_VI = "name_vi";
        public static final String COLUMN_NAME_NAME_TH = "name_th";
        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_ID);
                    add(COLUMN_NAME_MODULE_ID);
                    add(COLUMN_NAME_POSITION);
                    add(COLUMN_NAME_NAME);
                    add(COLUMN_NAME_NAME_FR);
                    add(COLUMN_NAME_NAME_ES);
                    add(COLUMN_NAME_NAME_ID);
                    add(COLUMN_NAME_NAME_PT);
                    add(COLUMN_NAME_NAME_TR);
                    add(COLUMN_NAME_NAME_LO);
                    add(COLUMN_NAME_NAME_VI);
                    add(COLUMN_NAME_NAME_TH);
                }}
        );
    }

    public static abstract class SipGroupEntry implements BaseColumns {
        public static final String TABLE_NAME = "sipGroupMaster"; // sip_group Alies modules_sip
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_NAME = "sip_group_name";
        public static final String COLUMN_NAME_NAME_FR = "sip_group_name_fr";
        public static final String COLUMN_NAME_NAME_ES = "sip_group_name_es";
        public static final String COLUMN_NAME_NAME_ID = "sip_group_name_id";
        public static final String COLUMN_NAME_NAME_PT = "sip_group_name_pt";
        public static final String COLUMN_NAME_NAME_TR = "sip_group_name_tr";
        public static final String COLUMN_NAME_NAME_LO = "sip_group_name_lo";
        public static final String COLUMN_NAME_NAME_VI = "sip_group_name_vi";
        public static final String COLUMN_NAME_NAME_TH = "sip_group_name_th";
        public static final String COLUMN_NAME_EXPIRES = "expires";
        public static final String COLUMN_NAME_ANNUAL_SURVEY = "annual_survey";
        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_ID);
                    add(COLUMN_NAME_NAME);
                    add(COLUMN_NAME_NAME_FR);
                    add(COLUMN_NAME_NAME_ES);
                    add(COLUMN_NAME_NAME_ID);
                    add(COLUMN_NAME_NAME_PT);
                    add(COLUMN_NAME_NAME_TR);
                    add(COLUMN_NAME_NAME_LO);
                    add(COLUMN_NAME_NAME_VI);
                    add(COLUMN_NAME_NAME_TH);
                    add(COLUMN_NAME_EXPIRES);
                    add(COLUMN_NAME_ANNUAL_SURVEY);
                }}
        );
    }

    public static abstract class SipSubGroupEntry implements BaseColumns {
        public static final String TABLE_NAME = "sip_subgroup"; /// alise submodule
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_MODULE_ID = "sipGroupId";
        //        public static final String COLUMN_NAME_POSITION = "position";
        public static final String COLUMN_NAME_NAME = "sipName";
        public static final String COLUMN_NAME_NAME_FR = "sipName_fr";
        public static final String COLUMN_NAME_NAME_ES = "sipName_es";
        public static final String COLUMN_NAME_NAME_ID = "sipName_id";
        public static final String COLUMN_NAME_NAME_PT = "sipName_pt";
        public static final String COLUMN_NAME_NAME_TR = "sipName_tr";
        public static final String COLUMN_NAME_NAME_LO = "sipName_lo";
        public static final String COLUMN_NAME_NAME_VI = "sipName_vi";
        public static final String COLUMN_NAME_NAME_TH = "sipName_th";
        public static final String COLUMN_NAME_PHOTO = "photo";
        public static final String COLUMN_NAME_SHOWIN = "showIn";
        public static final String COLUMN_NAME_IMAGE_ENCODSTR = "imageBase64encodString";
        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_ID);
                    add(COLUMN_NAME_MODULE_ID);
//                    add(COLUMN_NAME_POSITION);
                    add(COLUMN_NAME_NAME);
                    add(COLUMN_NAME_NAME_FR);
                    add(COLUMN_NAME_NAME_ES);
                    add(COLUMN_NAME_NAME_ID);
                    add(COLUMN_NAME_NAME_PT);
                    add(COLUMN_NAME_NAME_TR);
                    add(COLUMN_NAME_NAME_LO);
                    add(COLUMN_NAME_NAME_VI);
                    add(COLUMN_NAME_NAME_TH);
                    add(COLUMN_NAME_PHOTO);
                    add(COLUMN_NAME_SHOWIN);
                    add(COLUMN_NAME_IMAGE_ENCODSTR);
                }}
        );
    }

    public static abstract class QuestionSipEntry implements BaseColumns {
        public static final String TABLE_NAME = "questions_sip";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_SUBMODULE_ID = "sip_group_id";
        public static final String COLUMN_NAME_DATATYPE_ID = "datatype_id";
        public static final String COLUMN_NAME_POSITION = "position";
        public static final String COLUMN_NAME_DISPLAY_TYPE = "display_type";
        public static final String COLUMN_NAME_QUESTION = "question";
        public static final String COLUMN_NAME_QUESTION_FR = "question_fr";
        public static final String COLUMN_NAME_QUESTION_ES = "question_es";
        public static final String COLUMN_NAME_QUESTION_ID = "question_id";
        public static final String COLUMN_NAME_QUESTION_PT = "question_pt";
        public static final String COLUMN_NAME_QUESTION_TR = "question_tr";
        public static final String COLUMN_NAME_QUESTION_LO = "question_lo";
        public static final String COLUMN_NAME_QUESTION_VI = "question_vi";
        public static final String COLUMN_NAME_QUESTION_TH = "question_th";
        public static final String COLUMN_NAME_DISPLAY_CONFIGURATION = "display_configuration";
        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_ID);
                    add(COLUMN_NAME_SUBMODULE_ID);
                    add(COLUMN_NAME_DATATYPE_ID);
                    add(COLUMN_NAME_POSITION);
                    add(COLUMN_NAME_DISPLAY_TYPE);
                    add(COLUMN_NAME_QUESTION);
                    add(COLUMN_NAME_QUESTION_FR);
                    add(COLUMN_NAME_QUESTION_ES);
                    add(COLUMN_NAME_QUESTION_ID);
                    add(COLUMN_NAME_QUESTION_PT);
                    add(COLUMN_NAME_QUESTION_TR);
                    add(COLUMN_NAME_QUESTION_LO);
                    add(COLUMN_NAME_QUESTION_VI);
                    add(COLUMN_NAME_QUESTION_TH);
                    add(COLUMN_NAME_DISPLAY_CONFIGURATION);
                }}
        );
    }


    public static abstract class productSipEntry implements BaseColumns {
        public static final String TABLE_NAME = "products_sip";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_PRODUCT_ID = "productId";
        public static final String COLUMN_NAME_PRODUCT_NAME = "productName";
//        public static final String COLUMN_NAME_DELETED_FLAG = "deletedFlag";
//        public static final String COLUMN_NAME_TRANS_LIST = "translationsList";

        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_PRODUCT_NAME);
                    add(COLUMN_NAME_ID);
                    add(COLUMN_NAME_PRODUCT_ID);
//                    add(COLUMN_NAME_DELETED_FLAG);
//                    add(COLUMN_NAME_TRANS_LIST);

                }}
        );
    }

    public static abstract class AnswerSipEntry implements BaseColumns {
        public static final String TABLE_NAME = "answers_sip";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_UPLOAD_ID = "upload_id";
        public static final String COLUMN_NAME_QUESTION_ID = "question_id";
        public static final String COLUMN_NAME_PARENT_QUESTION_ID = "parent_question_id";
        public static final String COLUMN_NAME_REPEATER_ITEM_POSITION = "repeater_item_position";
        public static final String COLUMN_NAME_SUBMODULE_ID = "submodule_id";
        public static final String COLUMN_NAME_FARMER_MODULE_ID = "farmer_module_id";
        public static final String COLUMN_NAME_FMP_ID = "fmp_id";
        public static final String COLUMN_NAME_FARM_ID = "farm_id";
        public static final String COLUMN_NAME_ANSWER_INT = "answer_int";
        public static final String COLUMN_NAME_ANSWER_BOOL = "answer_boolean";
        public static final String COLUMN_NAME_ANSWER_DECIMAL = "answer_decimal";
        public static final String COLUMN_NAME_ANSWER_DATE = "answer_date";
        public static final String COLUMN_NAME_ANSWER_TEXT = "answer_text";
        public static final String COLUMN_NAME_ANSWER_REPORTING_YEAR = "reporting_year";
        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_ID);
                    add(COLUMN_NAME_UPLOAD_ID);
                    add(COLUMN_NAME_QUESTION_ID);
                    add(COLUMN_NAME_PARENT_QUESTION_ID);
                    add(COLUMN_NAME_REPEATER_ITEM_POSITION);
                    add(COLUMN_NAME_SUBMODULE_ID);
                    add(COLUMN_NAME_FARMER_MODULE_ID);
                    add(COLUMN_NAME_FMP_ID);
                    add(COLUMN_NAME_FARM_ID);
                    add(COLUMN_NAME_ANSWER_INT);
                    add(COLUMN_NAME_ANSWER_BOOL);
                    add(COLUMN_NAME_ANSWER_DECIMAL);
                    add(COLUMN_NAME_ANSWER_DATE);
                    add(COLUMN_NAME_ANSWER_TEXT);
                    add(COLUMN_NAME_ANSWER_REPORTING_YEAR);
                }}
        );
    }

    public static abstract class ModuleAssignmentsEntry implements BaseColumns {
        public static final String TABLE_NAME = "moduleassignments";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_MODULE_ID = "module_id";
        public static final String COLUMN_NAME_MODULEASSIGNMENT_ID = "moduleassignment_id";
        public static final String COLUMN_NAME_MODULEASSIGNMENT_TYPE = "moduleassignment_type";
        public static final String COLUMN_NAME_RECURRENCE_FREQUENCY = "recurrence_frequency";
        public static final String COLUMN_NAME_RECURRENCE_TYPE = "recurrence_type";
        public static final String COLUMN_NAME_DISABLED_MONTHS = "disabled_months";
        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_ID);
                    add(COLUMN_NAME_MODULE_ID);
                    add(COLUMN_NAME_MODULEASSIGNMENT_ID);
                    add(COLUMN_NAME_MODULEASSIGNMENT_TYPE);
                    add(COLUMN_NAME_RECURRENCE_FREQUENCY);
                    add(COLUMN_NAME_RECURRENCE_TYPE);
                    add(COLUMN_NAME_DISABLED_MONTHS);
                }}
        );
    }

    public static abstract class FarmerModulesEntry implements BaseColumns {
        public static final String TABLE_NAME = "farmer_modules";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_MODULE_ID = "module_id";
        public static final String COLUMN_NAME_FARMER_ID = "farmer_id";
        public static final String COLUMN_NAME_OLAM_FARMER_ID = "olam_farmer_id";
        public static final String COLUMN_NAME_FARM_ID = "farm_id";
        public static final String COLUMN_NAME_CURRENT_SUBMODULE_ID = "current_submodule_id";
        public static final String COLUMN_NAME_STATUS_STARTED = "status_started";
        public static final String COLUMN_NAME_STATUS_FINISHED = "status_finished";
        public static final String COLUMN_NAME_RECORDED_LAT = "recorded_lat";
        public static final String COLUMN_NAME_RECORDED_LNG = "recorded_lng";
        public static final String COLUMN_NAME_STARTED_DATE = "started_date";
        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_ID);
                    add(COLUMN_NAME_MODULE_ID);
                    add(COLUMN_NAME_FARMER_ID);
                    add(COLUMN_NAME_OLAM_FARMER_ID);
                    add(COLUMN_NAME_FARM_ID);
                    add(COLUMN_NAME_CURRENT_SUBMODULE_ID);
                    add(COLUMN_NAME_STATUS_STARTED);
                    add(COLUMN_NAME_STATUS_FINISHED);
                    add(COLUMN_NAME_RECORDED_LAT);
                    add(COLUMN_NAME_RECORDED_LNG);
                    add(COLUMN_NAME_STARTED_DATE);
                }}
        );
    }

    public static abstract class FarmerChildModulesEntry implements BaseColumns {
        public static final String TABLE_NAME = "farmer_child_modules";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_MODULE_ID = "module_id";
        public static final String COLUMN_NAME_FARMER_ID = "farmer_id";
        public static final String COLUMN_NAME_OLAM_FARMER_ID = "olam_farmer_id";
        public static final String COLUMN_NAME_FARM_ID = "farm_id";
        public static final String COLUMN_NAME_CURRENT_SUBMODULE_ID = "current_submodule_id";
        public static final String COLUMN_NAME_STATUS_STARTED = "status_started";
        public static final String COLUMN_NAME_STATUS_FINISHED = "status_finished";
        public static final String COLUMN_NAME_RECORDED_LAT = "recorded_lat";
        public static final String COLUMN_NAME_RECORDED_LNG = "recorded_lng";
        public static final String COLUMN_NAME_STARTED_DATE = "started_date";
        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_ID);
                    add(COLUMN_NAME_MODULE_ID);
                    add(COLUMN_NAME_FARMER_ID);
                    add(COLUMN_NAME_OLAM_FARMER_ID);
                    add(COLUMN_NAME_FARM_ID);
                    add(COLUMN_NAME_CURRENT_SUBMODULE_ID);
                    add(COLUMN_NAME_STATUS_STARTED);
                    add(COLUMN_NAME_STATUS_FINISHED);
                    add(COLUMN_NAME_RECORDED_LAT);
                    add(COLUMN_NAME_RECORDED_LNG);
                    add(COLUMN_NAME_STARTED_DATE);
                }}
        );
    }

    public static abstract class DatatypesEntry implements BaseColumns {
        public static final String TABLE_NAME = "datatypes";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_DATATYPE_NAME = "name";
        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_ID);
                    add(COLUMN_NAME_DATATYPE_NAME);
                }}
        );
    }
    public static abstract class DatatypesSipEntry implements BaseColumns {
        public static final String TABLE_NAME = "datatypes_sip";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_DATATYPE_NAME = "name";
        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_ID);
                    add(COLUMN_NAME_DATATYPE_NAME);
                }}
        );
    }


    public static abstract class LookupValuesSipEntry implements BaseColumns {
        public static final String TABLE_NAME = "siplookupvalues";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_QUESTION_ID = "question_id";
        public static final String COLUMN_NAME_POSITION = "position";
        public static final String COLUMN_NAME_VALUE = "value";
        public static final String COLUMN_NAME_VALUE_FR = "value_fr";
        public static final String COLUMN_NAME_VALUE_ES = "value_es";
        public static final String COLUMN_NAME_VALUE_ID = "value_id";
        public static final String COLUMN_NAME_VALUE_PT = "value_pt";
        public static final String COLUMN_NAME_VALUE_TR = "value_tr";
        public static final String COLUMN_NAME_VALUE_LO = "value_lo";
        public static final String COLUMN_NAME_VALUE_VI = "value_vi";
        public static final String COLUMN_NAME_VALUE_TH = "value_th";
        public static final String COLUMN_NAME_PHOTO = "photo";
        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_ID);
                    add(COLUMN_NAME_QUESTION_ID);
                    add(COLUMN_NAME_POSITION);
                    add(COLUMN_NAME_VALUE);
                    add(COLUMN_NAME_VALUE_FR);
                    add(COLUMN_NAME_VALUE_ES);
                    add(COLUMN_NAME_VALUE_ID);
                    add(COLUMN_NAME_VALUE_PT);
                    add(COLUMN_NAME_VALUE_TR);
                    add(COLUMN_NAME_VALUE_LO);
                    add(COLUMN_NAME_VALUE_VI);
                    add(COLUMN_NAME_VALUE_TH);
                    add(COLUMN_NAME_PHOTO);
                }}
        );
    }


    public static abstract class LookupValuesEntry implements BaseColumns {
        public static final String TABLE_NAME = "lookupvalues";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_QUESTION_ID = "question_id";
        public static final String COLUMN_NAME_POSITION = "position";
        public static final String COLUMN_NAME_VALUE = "value";
        public static final String COLUMN_NAME_VALUE_FR = "value_fr";
        public static final String COLUMN_NAME_VALUE_ES = "value_es";
        public static final String COLUMN_NAME_VALUE_ID = "value_id";
        public static final String COLUMN_NAME_VALUE_PT = "value_pt";
        public static final String COLUMN_NAME_VALUE_TR = "value_tr";
        public static final String COLUMN_NAME_VALUE_LO = "value_lo";
        public static final String COLUMN_NAME_VALUE_VI = "value_vi";
        public static final String COLUMN_NAME_VALUE_TH = "value_th";
        public static final String COLUMN_NAME_PHOTO = "photo";
        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_ID);
                    add(COLUMN_NAME_QUESTION_ID);
                    add(COLUMN_NAME_POSITION);
                    add(COLUMN_NAME_VALUE);
                    add(COLUMN_NAME_VALUE_FR);
                    add(COLUMN_NAME_VALUE_ES);
                    add(COLUMN_NAME_VALUE_ID);
                    add(COLUMN_NAME_VALUE_PT);
                    add(COLUMN_NAME_VALUE_TR);
                    add(COLUMN_NAME_VALUE_LO);
                    add(COLUMN_NAME_VALUE_VI);
                    add(COLUMN_NAME_VALUE_TH);
                    add(COLUMN_NAME_PHOTO);
                }}
        );
    }

    public static abstract class QuestionEntry implements BaseColumns {
        public static final String TABLE_NAME = "questions";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_SUBMODULE_ID = "submodule_id";
        public static final String COLUMN_NAME_DATATYPE_ID = "datatype_id";
        public static final String COLUMN_NAME_POSITION = "position";
        public static final String COLUMN_NAME_DISPLAY_TYPE = "display_type";
        public static final String COLUMN_NAME_QUESTION = "question";
        public static final String COLUMN_NAME_QUESTION_FR = "question_fr";
        public static final String COLUMN_NAME_QUESTION_ES = "question_es";
        public static final String COLUMN_NAME_QUESTION_ID = "question_id";
        public static final String COLUMN_NAME_QUESTION_PT = "question_pt";
        public static final String COLUMN_NAME_QUESTION_TR = "question_tr";
        public static final String COLUMN_NAME_QUESTION_LO = "question_lo";
        public static final String COLUMN_NAME_QUESTION_VI = "question_vi";
        public static final String COLUMN_NAME_QUESTION_TH = "question_th";
        public static final String COLUMN_NAME_DISPLAY_CONFIGURATION = "display_configuration";
        public static final String COLUMN_NAME_STOREGE_MODEL = "storage_model";
        public static final String COLUMN_NAME_STOREGE_COLUM = "storage_column";
        public static final String COLUMN_NAME_Active = "active";
        public static final String COLUMN_NAME_CREATED_AT = "created_at";
        public static final String COLUMN_NAME_UPDATED_AT = "updated_at";
        public static final String COLUMN_NAME_APP_ID = "app_id";
        public static final String COLUMN_NAME_IS_SUBSET_QUESTION_REQUIRED = "is_subset_questions_required";
        public static final String COLUMN_NAME_CONDITIONAL_QUESTIONS = "conditional_questions";
        public static final String COLUMN_NAME_SIPGROUPID = "sipGroupId";
        public static final String COLUMN_NAME_SIPKEY = "sipKey";

        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_ID);
                    add(COLUMN_NAME_SUBMODULE_ID);
                    add(COLUMN_NAME_DATATYPE_ID);
                    add(COLUMN_NAME_POSITION);
                    add(COLUMN_NAME_DISPLAY_TYPE);
                    add(COLUMN_NAME_QUESTION);
                    add(COLUMN_NAME_QUESTION_FR);
                    add(COLUMN_NAME_QUESTION_ES);
                    add(COLUMN_NAME_QUESTION_ID);
                    add(COLUMN_NAME_QUESTION_PT);
                    add(COLUMN_NAME_QUESTION_TR);
                    add(COLUMN_NAME_QUESTION_LO);
                    add(COLUMN_NAME_QUESTION_VI);
                    add(COLUMN_NAME_QUESTION_TH);
                    add(COLUMN_NAME_DISPLAY_CONFIGURATION);
                    add(COLUMN_NAME_STOREGE_MODEL);
                    add(COLUMN_NAME_STOREGE_COLUM);
                    add(COLUMN_NAME_Active);
                    add(COLUMN_NAME_CREATED_AT);
                    add(COLUMN_NAME_UPDATED_AT);
                    add(COLUMN_NAME_APP_ID);
                    add(COLUMN_NAME_IS_SUBSET_QUESTION_REQUIRED);
                    add(COLUMN_NAME_CONDITIONAL_QUESTIONS);
                    add(COLUMN_NAME_SIPGROUPID);
                    add(COLUMN_NAME_SIPKEY);

                }}
        );
    }
    public static abstract class countryLookUpValuesEntry implements BaseColumns {
        public static final String TABLE_NAME = "allCountries";
        public static final String COLUMN_NAME_ID = "country_id";
        public static final String COLUMN_NAME_CODE = "code";
        public static final String COLUMN_NAME_COUNTRY_NAME = "countryName";
        public static final String COLUMN_NAME_DIALLING_CODE = "dialling_code";
        public static final String COLUMN_NAME_OLAM_COUNTRY = "olam_country";
        public static final String COLUMN_NAME_ENUMERATOR_COUNTRY = "enumerator_country";
        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_COUNTRY_NAME);
                    add(COLUMN_NAME_ID);
                    add(COLUMN_NAME_CODE);
                    add(COLUMN_NAME_OLAM_COUNTRY);
                    add(COLUMN_NAME_ENUMERATOR_COUNTRY);
                    add(COLUMN_NAME_DIALLING_CODE);
                }}
        );
    }

    public static abstract class AnswerEntry implements BaseColumns {
        public static final String TABLE_NAME = "answers";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_QUESTION_ID = "question_id";
        public static final String COLUMN_NAME_PARENT_QUESTION_ID = "parent_question_id";
        public static final String COLUMN_NAME_REPEATER_ITEM_POSITION = "repeater_item_position";
        public static final String COLUMN_NAME_SUBMODULE_ID = "submodule_id";
        public static final String COLUMN_NAME_FARMER_MODULE_ID = "farmer_module_id";
        public static final String COLUMN_NAME_FMP_ID = "fmp_id";
        public static final String COLUMN_NAME_FARMER_ID = "farmer_id";
        public static final String COLUMN_NAME_OLAM_FARMER_ID = "olam_farmer_id";
        public static final String COLUMN_NAME_FARM_ID = "farm_id";
        public static final String COLUMN_NAME_ANSWER_INT = "answer_int";
        public static final String COLUMN_NAME_ANSWER_BOOL = "answer_boolean";
        public static final String COLUMN_NAME_ANSWER_DECIMAL = "answer_decimal";
        public static final String COLUMN_NAME_ANSWER_DATE = "answer_date";
        public static final String COLUMN_NAME_ANSWER_TEXT = "answer_text";
        public static final String COLUMN_NAME_ANSWER_REPORTING_YEAR = "reporting_year";
        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_ID);
                    add(COLUMN_NAME_QUESTION_ID);
                    add(COLUMN_NAME_PARENT_QUESTION_ID);
                    add(COLUMN_NAME_REPEATER_ITEM_POSITION);
                    add(COLUMN_NAME_SUBMODULE_ID);
                    add(COLUMN_NAME_FARMER_MODULE_ID);
                    add(COLUMN_NAME_FMP_ID);
                    add(COLUMN_NAME_FARMER_ID);
                    add(COLUMN_NAME_OLAM_FARMER_ID);
                    add(COLUMN_NAME_FARM_ID);
                    add(COLUMN_NAME_ANSWER_INT);
                    add(COLUMN_NAME_ANSWER_BOOL);
                    add(COLUMN_NAME_ANSWER_DECIMAL);
                    add(COLUMN_NAME_ANSWER_DATE);
                    add(COLUMN_NAME_ANSWER_TEXT);
                    add(COLUMN_NAME_ANSWER_REPORTING_YEAR);
                }}
        );
    }


    public static abstract class AnswerChildEntry implements BaseColumns {
        public static final String TABLE_NAME = "child_answers";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_QUESTION_ID = "question_id";
        public static final String COLUMN_NAME_PARENT_QUESTION_ID = "parent_question_id";
        public static final String COLUMN_NAME_REPEATER_ITEM_POSITION = "repeater_item_position";
        public static final String COLUMN_NAME_SUBMODULE_ID = "submodule_id";
        public static final String COLUMN_NAME_FARMER_MODULE_ID = "farmer_module_id";
        public static final String COLUMN_NAME_FMP_ID = "fmp_id";
        public static final String COLUMN_NAME_FARMER_ID = "farmer_id";
        public static final String COLUMN_NAME_OLAM_FARMER_ID = "olam_farmer_id";
        public static final String COLUMN_NAME_FARM_ID = "farm_id";
        public static final String COLUMN_NAME_ANSWER_INT = "answer_int";
        public static final String COLUMN_NAME_ANSWER_BOOL = "answer_boolean";
        public static final String COLUMN_NAME_ANSWER_DECIMAL = "answer_decimal";
        public static final String COLUMN_NAME_ANSWER_DATE = "answer_date";
        public static final String COLUMN_NAME_ANSWER_TEXT = "answer_text";
        public static final String COLUMN_NAME_ANSWER_REPORTING_YEAR = "reporting_year";
        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_ID);
                    add(COLUMN_NAME_QUESTION_ID);
                    add(COLUMN_NAME_PARENT_QUESTION_ID);
                    add(COLUMN_NAME_REPEATER_ITEM_POSITION);
                    add(COLUMN_NAME_SUBMODULE_ID);
                    add(COLUMN_NAME_FARMER_MODULE_ID);
                    add(COLUMN_NAME_FMP_ID);
                    add(COLUMN_NAME_FARMER_ID);
                    add(COLUMN_NAME_OLAM_FARMER_ID);
                    add(COLUMN_NAME_FARM_ID);
                    add(COLUMN_NAME_ANSWER_INT);
                    add(COLUMN_NAME_ANSWER_BOOL);
                    add(COLUMN_NAME_ANSWER_DECIMAL);
                    add(COLUMN_NAME_ANSWER_DATE);
                    add(COLUMN_NAME_ANSWER_TEXT);
                    add(COLUMN_NAME_ANSWER_REPORTING_YEAR);
                }}
        );
    }

    // Child Registration Table Start
    public static abstract class ChildEntry implements BaseColumns {
        public static final String TABLE_NAME = "children";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_FARMER_ID = "farmer_id";
        public static final String COLUMN_NAME_CHILD_NAME = "name";
        public static final String COLUMN_NAME_CHILD_AGE = "age";
        public static final String COLUMN_NAME_CHILD_GENDER = "gender";
        public static final String COLUMN_NAME_ACTION_FLAG = "action_flag";
        public static final String COLUMN_NAME_STATUS = "status";
        public static final String COLUMN_NAME_CHILD_AGE_ON_YEAR = "age_on_year";
        public static final String COLUMN_NAME_CHILD_SYNCED = "synced";
        public static final String COLUMN_NAME_CHILD_SUBMITTED_MODULES = "submitted_modules";

        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_ID);
                    add(COLUMN_NAME_FARMER_ID);
                    add(COLUMN_NAME_CHILD_NAME);
                    add(COLUMN_NAME_CHILD_AGE);
                    add(COLUMN_NAME_CHILD_GENDER);
                    add(COLUMN_NAME_ACTION_FLAG);
                    add(COLUMN_NAME_STATUS);
                    add(COLUMN_NAME_CHILD_AGE_ON_YEAR);
                    add(COLUMN_NAME_CHILD_SYNCED);
                    add(COLUMN_NAME_CHILD_SUBMITTED_MODULES);
                }}
        );
    }

    public static abstract class ChildVisitDetails implements BaseColumns {
        public static final String TABLE_NAME = "child_visit";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_FARMER_ID = "farmerId";
        public static final String COLUMN_NAME_SURVEY_CODE = "surveyCode";
        public static final String COLUMN_NAME_SURVEY_DESCRIPTION = "surveyDescription";
        public static final String COLUMN_NAME_SURVEY_VISIT_SYNCED = "synced";

        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_ID);
                    add(COLUMN_NAME_FARMER_ID);
                    add(COLUMN_NAME_SURVEY_CODE);
                    add(COLUMN_NAME_SURVEY_DESCRIPTION);
                    add(COLUMN_NAME_SURVEY_VISIT_SYNCED);
                }}
        );
    }



    public static abstract class ChildVisitDetailsEntry implements BaseColumns {
        public static final String TABLE_NAME = "child_visit_details";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_FARMER_ID = "farmer_id";
        public static final String COLUMN_HOUSEHOLD = "household";
        public static final String COLUMN_INSPECTION = "inspection";
        public static final String COLUMN_FOLLOWUP = "followup";
        public static final String COLUMN_NAME_VISIT_DETAIL_SYNCED = "synced";

        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_ID);
                    add(COLUMN_NAME_FARMER_ID);
                    add(COLUMN_HOUSEHOLD);
                    add(COLUMN_INSPECTION);
                    add(COLUMN_FOLLOWUP);
                    add(COLUMN_NAME_VISIT_DETAIL_SYNCED);
                }}
        );
    }
    // Child Registration Table End



    public static abstract class FarmerEntry implements BaseColumns {
        public static final String TABLE_NAME = "farmers";

        // newly added parameters
        public static final String COLUMN_NAME_FARMERNAME = "farmerName";
        public static final String COLUMN_NAME_PLACEID = "placeId";
        public static final String COLUMN_NAME_SECTIONID = "sectionId";
        public static final String COLUMN_NAME_COUNTRYID = "countryId";
        public static final String COLUMN_NAME_DISTRICTID = "districtId";
        public static final String COLUMN_NAME_FARMERGROUPID = "farmerGroupId";
        public static final String COLUMN_NAME_REGIONID = "regionId";
        public static final String COLUMN_NAME_CONSENTFARMER = "consentFarmer";
        public static final String COLUMN_NAME_CONSENTOTHERS = "consentOthers";
        public static final String COLUMN_NAME_DATE_OF_BIRTH = "dateOfBirth";
        public static final String COLUMN_NAME_PRODUCT_ID = "productId";

        public static final String BACKUP_TABLE_NAME = "farmers_backup";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_FARM_ID = "farm_id";
        public static final String COLUMN_NAME_PLACE_ID = "place_id";
        public static final String COLUMN_NAME_SECTION_ID = "section_id";
        public static final String COLUMN_NAME_COUNTRY_ID = "country_id";

        public static final String COLUMN_NAME_FARMER_NAME = "name";
        public static final String COLUMN_NAME_GENDER = "gender";
        public static final String COLUMN_NAME_DOB = "dob";
        public static final String COLUMN_NAME_OLAM_FARMER_ID = "olam_farmer_id";
        public static final String COLUMN_NAME_PHOTO = "photo";
        public static final String COLUMN_NAME_SIGNATURE = "signature";
        public static final String COLUMN_NAME_SYNCED = "synced";
        public static final String COLUMN_NAME_CREATED_LOCALLY = "created_on_this_phone";
        public static final String COLUMN_NAME_WEBSITE_FARMER_ID = "website_farmer_id";
        public static final String COLUMN_NAME_WEBSITE_SUBMITTED_MODULES = "submitted_modules";
        public static final String COLUMN_NAME_FARM_PRIMARY_LAT = "primary_lat";
        public static final String COLUMN_NAME_FARM_PRIMARY_LNG = "primary_lng";
        public static final String COLUMN_NAME_FARM_PLOTS = "location";
        public static final String COLUMN_NAME_CONSENT_FARMER = "consent_farmer";
        public static final String COLUMN_NAME_CONSENT_OTHERS = "consent_others";
        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_ID);
                    add(COLUMN_NAME_FARM_ID);
                    add(COLUMN_NAME_PLACE_ID);
                    add(COLUMN_NAME_SECTION_ID);
                    add(COLUMN_NAME_FARMER_NAME);
                    add(COLUMN_NAME_GENDER);
                    add(COLUMN_NAME_DOB);
                    add(COLUMN_NAME_OLAM_FARMER_ID);
                    add(COLUMN_NAME_PHOTO);
                    add(COLUMN_NAME_SIGNATURE);
                    add(COLUMN_NAME_SYNCED);
                    add(COLUMN_NAME_CREATED_LOCALLY);
                    add(COLUMN_NAME_WEBSITE_FARMER_ID);
                    add(COLUMN_NAME_WEBSITE_SUBMITTED_MODULES);
                    add(COLUMN_NAME_FARM_PRIMARY_LAT);
                    add(COLUMN_NAME_FARM_PRIMARY_LNG);
                    add(COLUMN_NAME_FARM_PLOTS);
                    add(COLUMN_NAME_CONSENT_FARMER);
                    add(COLUMN_NAME_CONSENT_OTHERS);
                    add(COLUMN_NAME_COUNTRY_ID);
                    add(COLUMN_NAME_DISTRICTID);
                    add(COLUMN_NAME_FARMERGROUPID);
                    add(COLUMN_NAME_REGIONID);
                    add(COLUMN_NAME_PRODUCT_ID);

                }}
        );
        public static final List<String> SYNC_COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_ID);
                    add(COLUMN_NAME_FARM_ID);
                    add(COLUMN_NAME_PLACE_ID);
                    add(COLUMN_NAME_SECTION_ID);
                    add(COLUMN_NAME_FARMER_NAME);
                    add(COLUMN_NAME_OLAM_FARMER_ID);
                    add(COLUMN_NAME_WEBSITE_SUBMITTED_MODULES);
                    add(COLUMN_NAME_FARM_PRIMARY_LAT);
                    add(COLUMN_NAME_FARM_PRIMARY_LNG);
                    add(COLUMN_NAME_FARM_PLOTS);
                    add(COLUMN_NAME_COUNTRY_ID);
                    add(COLUMN_NAME_DISTRICTID);
                    add(COLUMN_NAME_FARMERGROUPID);
                    add(COLUMN_NAME_REGIONID);
                    add(COLUMN_NAME_PRODUCT_ID);
                }}
        );
        public static final String COLUMN_NAME_OLAM_FARMER_ID_DISPLAY = "displayFarmerId";
    }

    public static abstract class PlotGeoData implements BaseColumns {
        public static final String TABLE_NAME = "plotgeodata";
        public static final String COLUMN_NAME_PLOT_ID = "plot_id";
        public static final String COLUMN_NAME_PLOT_NAME = "plot_name";
        public static final String COLUMN_NAME_FARM_ID = "farm_id";
        public static final String COLUMN_NAME_SERVER_ID = "server_id";
        public static final String COLUMN_NAME_PLOT_JSON = "plotjson";
        public static final String COLUMN_NAME_PLOT_SYNC_FLAG = "plotsyncflag";
        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_PLOT_ID);
                    add(COLUMN_NAME_PLOT_NAME);
                    add(COLUMN_NAME_FARM_ID);
                    add(COLUMN_NAME_SERVER_ID);
                    add(COLUMN_NAME_PLOT_JSON);
                    add(COLUMN_NAME_PLOT_SYNC_FLAG);
                }}
        );
    }

    public static abstract class PhoneNumber implements BaseColumns {
        public static final String TABLE_NAME = "phonenumbers";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_FARMER_ID = "farmer_id";
        public static final String COLUMN_NAME_COUNTRY_ID = "country_id";
        public static final String COLUMN_NAME_NUMBER = "phonenumber";
        public static final String COLUMN_NAME_PRIMARY = "primary_number";
        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_ID);
                    add(COLUMN_NAME_FARMER_ID);
                    add(COLUMN_NAME_COUNTRY_ID);
                    add(COLUMN_NAME_NUMBER);
                    add(COLUMN_NAME_PRIMARY);
                }}
        );
    }

    public static abstract class Subquestionanswer implements BaseColumns {
        public static final String TABLE_NAME = "Subquestionanswer";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_PARENR_ID = "parent_id";
        public static final String COLUMN_NAME_QUESTION_ID = "question_id";
        public static final String COLUMN_NAME_QUESTION_ANSWER = "question_answer";
        public static final String COLUMN_NAME_POSITION = "question_answer_position";
        public static final String COLUMN_NAME_FARMER_ID = "farmer_id";
        public static final String COLUMN_NAME_MODULE_ID = "module_id";
        public static final String COLUMN_NAME_REPORT_YEAR = "report_year";

        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_ID);
                    add(COLUMN_NAME_PARENR_ID);
                    add(COLUMN_NAME_QUESTION_ID);
                    add(COLUMN_NAME_QUESTION_ANSWER);
                    add(COLUMN_NAME_POSITION);
                    add(COLUMN_NAME_FARMER_ID);
                    add(COLUMN_NAME_MODULE_ID);
                    add(COLUMN_NAME_REPORT_YEAR);

                }}
        );
    }

    public static abstract class CountriesEntry implements BaseColumns {
        public static final String TABLE_NAME = "countries";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_CODE = "code";
        public static final String COLUMN_NAME_COUNTRY_NAME = "name";
        public static final String COLUMN_NAME_DIALLING_CODE = "dialling_code";
        public static final String COLUMN_NAME_OLAM_COUNTRY = "olam_country";
        public static final String COLUMN_NAME_ENUMERATOR_COUNTRY = "enumerator_country";
        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_COUNTRY_NAME);
                    add(COLUMN_NAME_ID);
                    add(COLUMN_NAME_CODE);
                    add(COLUMN_NAME_OLAM_COUNTRY);
                    add(COLUMN_NAME_ENUMERATOR_COUNTRY);
                    add(COLUMN_NAME_DIALLING_CODE);
                }}
        );
    }

    public static abstract class RegionsEntry implements BaseColumns {
        public static final String TABLE_NAME = "regions";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_COUNTRY_ID = "country_id";
        public static final String COLUMN_NAME_REGION_NAME = "name";
        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_REGION_NAME);
                    add(COLUMN_NAME_ID);
                    add(COLUMN_NAME_COUNTRY_ID);
                }}
        );
    }

    public static abstract class DistrictsEntry implements BaseColumns {
        public static final String TABLE_NAME = "districts";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_REGION_ID = "region_id";
        public static final String COLUMN_NAME_DISTRICT_NAME = "name";
        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_DISTRICT_NAME);
                    add(COLUMN_NAME_ID);
                    add(COLUMN_NAME_REGION_ID);
                }}
        );
    }

    public static abstract class PlacesEntry implements BaseColumns {
        public static final String TABLE_NAME = "places";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_DISTRICT_ID = "district_id";
        public static final String COLUMN_NAME_PLACE_TYPE = "type";
        public static final String COLUMN_NAME_PLACE_NAME = "name";
        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_PLACE_NAME);
                    add(COLUMN_NAME_ID);
                    add(COLUMN_NAME_DISTRICT_ID);
                    add(COLUMN_NAME_PLACE_TYPE);
                }}
        );
    }

    public static abstract class FarmerGroupsEntry implements BaseColumns {
        public static final String TABLE_NAME = "farmergroups";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_PROGRAMME_ID = "programme_id";
        public static final String COLUMN_NAME_PARTNER_ID = "partner_id";
        public static final String COLUMN_NAME_PRODUCT_ID = "product_id";
        public static final String COLUMN_NAME_CODE = "code";
        public static final String COLUMN_NAME_FARMERGROUP_NAME = "name";
        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_FARMERGROUP_NAME);
                    add(COLUMN_NAME_ID);
                    add(COLUMN_NAME_CODE);
                    add(COLUMN_NAME_PROGRAMME_ID);
                    add(COLUMN_NAME_PARTNER_ID);
                    add(COLUMN_NAME_PRODUCT_ID);
                }}
        );
    }

    public static abstract class SectionsEntry implements BaseColumns {
        public static final String TABLE_NAME = "sections";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_FARMERGROUP_ID = "farmergroup_id";
        public static final String COLUMN_NAME_CODE = "code";
        public static final String COLUMN_NAME_SECTION_NAME = "name";
        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_SECTION_NAME);
                    add(COLUMN_NAME_ID);
                    add(COLUMN_NAME_CODE);
                    add(COLUMN_NAME_FARMERGROUP_ID);
                }}
        );
    }

    public static abstract class ProductsEntry implements BaseColumns {
        public static final String TABLE_NAME = "products";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_PRODUCT_NAME = "name";
        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_PRODUCT_NAME);
                    add(COLUMN_NAME_ID);
                }}
        );
    }

    public static abstract class NetworkQueueEntry implements BaseColumns {
        public static final String TABLE_NAME = "networkqueue";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_UPLOAD_ID = "upload_id";
        public static final String COLUMN_NAME_SUBMISSION_TYPE = "submission_type";
        public static final String COLUMN_NAME_POI_TYPE_ID = "poi_type_id";
        public static final String COLUMN_NAME_POI_MODULE_ID = "poi_module_id";
        public static final String COLUMN_NAME_POI_TYPE = "poi_type";
        public static final String COLUMN_NAME_POI_SUBGROUP_TYPE_ID = "poi_subgroup_type_id";
        public static final String COLUMN_NAME_POI_SUBMODULE_ID = "poi_submodule_id";
        public static final String COLUMN_NAME_POI_SUBGROUP_TYPE = "poi_subgroup_type";
        public static final String COLUMN_NAME_POI_NAME = "name";
        public static final String COLUMN_NAME_FARMER_ID = "farmer_id";
        public static final String COLUMN_NAME_OLAM_FARMER_ID = "olam_farmer_id";
        public static final String COLUMN_NAME_FARM_ID = "farm_id";
        public static final String COLUMN_NAME_FARMER_MODULE_ID = "farmer_module_id";
        public static final String COLUMN_NAME_FMP_ID = "fmp_id";
        public static final String COLUMN_NAME_TRANSACTION_ITEM_ID = "transactionitem_id";
        public static final String COLUMN_NAME_TRANSACTEE_TYPE = "transactee_type";
        public static final String COLUMN_NAME_TRAINING_RECORD_ID = "trainingrecord_id";
        public static final String COLUMN_NAME_RECORDED_LAT = "recorded_lat";
        public static final String COLUMN_NAME_RECORDED_LNG = "recorded_lng";
        public static final String COLUMN_NAME_RECORDED_PLOTS= "recorded_plots";
        public static final String COLUMN_NAME_COUNTRY_ID = "country_id";
        public static final String COLUMN_NAME_REGION_ID = "region_id";
        public static final String COLUMN_NAME_DISTRICT_ID = "district_id";
        public static final String COLUMN_NAME_PLACE_ID = "place_id";
        public static final String COLUMN_NAME_PRODUCT_ID = "product_id";
        public static final String COLUMN_NAME_FARMERGROUP_ID = "farmergroup_id";
        public static final String COLUMN_NAME_CAPACITY = "capacity";
        public static final String COLUMN_NAME_NUM_CLASSROOMS = "num_classrooms";
        public static final String COLUMN_NAME_YEAR_OPENED = "year_opened";
        public static final String COLUMN_NAME_GENDER_MIX = "gender_mix";
        public static final String COLUMN_NAME_SCHOOL_TYPE = "school_type";
        public static final String COLUMN_NAME_CWS_TYPE = "cws_type";
        public static final String COLUMN_NAME_OLAM_FUNDED = "olam_funded";
        public static final String COLUMN_NAME_SERVERITEM_ID = "server_item_id";
        public static final String COLUMN_NAME_FULLY_SYNCED = "fully_synced";
        public static final String COLUMN_NAME_UPLOAD_ATTEMPTS = "upload_attempts";
        public static final String COLUMN_NAME_CREATED_AT = "created_at";
        public static final String COLUMN_NAME_UPDATED_AT = "updated_at";
        //JIRA req OW-505
        public static final String COLUMN_NAME_FARMER_PHOTO = "farmer_photourl";
        public static final String COLUMN_NAME_FARMER_CHILD_ID = "farmer_child_id";


        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_ID);
                    add(COLUMN_NAME_UPLOAD_ID);
                    add(COLUMN_NAME_SUBMISSION_TYPE);
                    add(COLUMN_NAME_POI_TYPE_ID);
                    add(COLUMN_NAME_POI_MODULE_ID);
                    add(COLUMN_NAME_POI_TYPE);
                    add(COLUMN_NAME_POI_SUBGROUP_TYPE_ID);
                    add(COLUMN_NAME_POI_SUBMODULE_ID);
                    add(COLUMN_NAME_POI_SUBGROUP_TYPE);
                    add(COLUMN_NAME_POI_NAME);
                    add(COLUMN_NAME_FARMER_ID);
                    add(COLUMN_NAME_OLAM_FARMER_ID);
                    add(COLUMN_NAME_FARM_ID);
                    add(COLUMN_NAME_FARMER_MODULE_ID);
                    add(COLUMN_NAME_FMP_ID);
                    add(COLUMN_NAME_TRANSACTION_ITEM_ID);
                    add(COLUMN_NAME_TRANSACTEE_TYPE);
                    add(COLUMN_NAME_TRAINING_RECORD_ID);
                    add(COLUMN_NAME_RECORDED_LAT);
                    add(COLUMN_NAME_RECORDED_LNG);
                    add(COLUMN_NAME_RECORDED_PLOTS);
                    add(COLUMN_NAME_COUNTRY_ID);
                    add(COLUMN_NAME_REGION_ID);
                    add(COLUMN_NAME_DISTRICT_ID);
                    add(COLUMN_NAME_PLACE_ID);
                    add(COLUMN_NAME_PRODUCT_ID);
                    add(COLUMN_NAME_FARMERGROUP_ID);
                    add(COLUMN_NAME_CAPACITY);
                    add(COLUMN_NAME_NUM_CLASSROOMS);
                    add(COLUMN_NAME_YEAR_OPENED);
                    add(COLUMN_NAME_GENDER_MIX);
                    add(COLUMN_NAME_SCHOOL_TYPE);
                    add(COLUMN_NAME_CWS_TYPE);
                    add(COLUMN_NAME_OLAM_FUNDED);
                    add(COLUMN_NAME_SERVERITEM_ID);
                    add(COLUMN_NAME_FULLY_SYNCED);
                    add(COLUMN_NAME_UPLOAD_ATTEMPTS);
                    add(COLUMN_NAME_CREATED_AT);
                    add(COLUMN_NAME_UPDATED_AT);
                    add(COLUMN_NAME_FARMER_PHOTO);
                    add(COLUMN_NAME_FARMER_CHILD_ID);
                }}
        );

    }

    // farm management plans
    public static abstract class FarmManagementPlanEntry implements BaseColumns {
        public static final String TABLE_NAME = "farm_management_plans";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_FARMER_ID = "farmer_id";
        public static final String COLUMN_NAME_FARM_ID = "farm_id";
        public static final String COLUMN_NAME_STARTING_YEAR = "starting_year";
        public static final String COLUMN_NAME_HAS_FEEDBACK = "has_feedback";
        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_ID);
                    add(COLUMN_NAME_FARMER_ID);
                    add(COLUMN_NAME_FARM_ID);
                    add(COLUMN_NAME_STARTING_YEAR);
                    add(COLUMN_NAME_HAS_FEEDBACK);
                }}
        );
    }

    public static abstract class FarmManagementPlanSectionsEntry implements BaseColumns {
        public static final String TABLE_NAME = "fmp_sections";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_DISPLAY_TITLE = "display_title";
        public static final String COLUMN_NAME_DISPLAY_TITLE_FR = "display_title_fr";
        public static final String COLUMN_NAME_DISPLAY_TITLE_ES = "display_title_es";
        public static final String COLUMN_NAME_DISPLAY_TITLE_ID = "display_title_id";
        public static final String COLUMN_NAME_DISPLAY_TITLE_PT = "display_title_pt";
        public static final String COLUMN_NAME_DISPLAY_TITLE_TR = "display_title_tr";
        public static final String COLUMN_NAME_DISPLAY_TITLE_LO = "display_title_lo";
        public static final String COLUMN_NAME_DISPLAY_TITLE_VI = "display_title_vi";
        public static final String COLUMN_NAME_DISPLAY_TITLE_TH = "display_title_th";

        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_ID);
                    add(COLUMN_NAME_NAME);
                    add(COLUMN_NAME_DISPLAY_TITLE);
                    add(COLUMN_NAME_DISPLAY_TITLE_FR);
                    add(COLUMN_NAME_DISPLAY_TITLE_ES);
                    add(COLUMN_NAME_DISPLAY_TITLE_ID);
                    add(COLUMN_NAME_DISPLAY_TITLE_PT);
                    add(COLUMN_NAME_DISPLAY_TITLE_TR);
                    add(COLUMN_NAME_DISPLAY_TITLE_LO);
                    add(COLUMN_NAME_DISPLAY_TITLE_VI);
                    add(COLUMN_NAME_DISPLAY_TITLE_TH);
                }}
        );
    }

    public static abstract class FarmManagementPlanEntriesEntry implements BaseColumns {
        public static final String TABLE_NAME = "fmp_entries";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_SECTION_ID = "fmp_section_id";
        public static final String COLUMN_NAME_POSITION = "position";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_DISPLAY_TITLE = "display_title";
        public static final String COLUMN_NAME_DISPLAY_TITLE_FR = "display_title_fr";
        public static final String COLUMN_NAME_DISPLAY_TITLE_ES = "display_title_es";
        public static final String COLUMN_NAME_DISPLAY_TITLE_ID = "display_title_id";
        public static final String COLUMN_NAME_DISPLAY_TITLE_PT = "display_title_pt";
        public static final String COLUMN_NAME_DISPLAY_TITLE_TR = "display_title_tr";
        public static final String COLUMN_NAME_DISPLAY_TITLE_LO = "display_title_lo";
        public static final String COLUMN_NAME_DISPLAY_TITLE_VI = "display_title_vi";
        public static final String COLUMN_NAME_DISPLAY_TITLE_TH = "display_title_th";
        public static final String COLUMN_NAME_SHOW_IN_APP = "show_in_app";
        public static final String COLUMN_NAME_CALCULATED_FIELD = "app_calculated_field";
        public static final String COLUMN_NAME_CALCULATION_VARIABLE = "app_calculation_variable";

        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_ID);
                    add(COLUMN_NAME_SECTION_ID);
                    add(COLUMN_NAME_POSITION);
                    add(COLUMN_NAME_NAME);
                    add(COLUMN_NAME_DISPLAY_TITLE);
                    add(COLUMN_NAME_DISPLAY_TITLE_FR);
                    add(COLUMN_NAME_DISPLAY_TITLE_ES);
                    add(COLUMN_NAME_DISPLAY_TITLE_ID);
                    add(COLUMN_NAME_DISPLAY_TITLE_PT);
                    add(COLUMN_NAME_DISPLAY_TITLE_TR);
                    add(COLUMN_NAME_DISPLAY_TITLE_LO);
                    add(COLUMN_NAME_DISPLAY_TITLE_VI);
                    add(COLUMN_NAME_DISPLAY_TITLE_TH);
                    add(COLUMN_NAME_SHOW_IN_APP);
                    add(COLUMN_NAME_CALCULATED_FIELD);
                    add(COLUMN_NAME_CALCULATION_VARIABLE);
                }}
        );
    }

    public static abstract class FarmManagementPlanValuesEntry implements BaseColumns {
        public static final String TABLE_NAME = "fmp_values";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_FMP_ID = "fmp_id";
        public static final String COLUMN_NAME_FMP_ENTRY_ID = "fmp_entry_id";
        public static final String COLUMN_NAME_YEAR = "year";
        public static final String COLUMN_NAME_VALUE = "value";

        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_ID);
                    add(COLUMN_NAME_FMP_ID);
                    add(COLUMN_NAME_FMP_ENTRY_ID);
                    add(COLUMN_NAME_YEAR);
                    add(COLUMN_NAME_VALUE);
                }}
        );
    }

    public static abstract class FarmManagementPlanCurrentValuesEntry implements BaseColumns {
        public static final String TABLE_NAME = "fmp_current_values";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_FMP_ID = "fmp_id";
        public static final String COLUMN_NAME_FMP_ENTRY_ID = "fmp_entry_id";
        public static final String COLUMN_NAME_YEAR = "year";
        public static final String COLUMN_NAME_VALUE = "value";

        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_ID);
                    add(COLUMN_NAME_FMP_ID);
                    add(COLUMN_NAME_FMP_ENTRY_ID);
                    add(COLUMN_NAME_YEAR);
                    add(COLUMN_NAME_VALUE);
                }}
        );
    }

    // transactions
    public static abstract class Currency implements BaseColumns {
        public static final String TABLE_NAME = "currencies";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_SYMBOL = "symbol";
        public static final String COLUMN_NAME_CODE = "code";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_NAME_FR = "name_fr";
        public static final String COLUMN_NAME_NAME_ES = "name_es";
        public static final String COLUMN_NAME_NAME_ID = "name_id";
        public static final String COLUMN_NAME_NAME_PT = "name_pt";
        public static final String COLUMN_NAME_NAME_TR = "name_tr";
        public static final String COLUMN_NAME_NAME_LO = "name_lo";
        public static final String COLUMN_NAME_NAME_VI = "name_vi";
        public static final String COLUMN_NAME_NAME_TH = "name_th";
        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_ID);
                    add(COLUMN_NAME_SYMBOL);
                    add(COLUMN_NAME_CODE);
                    add(COLUMN_NAME_NAME);
                    add(COLUMN_NAME_NAME_FR);
                    add(COLUMN_NAME_NAME_ES);
                    add(COLUMN_NAME_NAME_ID);
                    add(COLUMN_NAME_NAME_PT);
                    add(COLUMN_NAME_NAME_TR);
                    add(COLUMN_NAME_NAME_LO);
                    add(COLUMN_NAME_NAME_VI);
                    add(COLUMN_NAME_NAME_TH);
                }}
        );

    }

    public static abstract class CountryCurrency implements BaseColumns {
        public static final String TABLE_NAME = "country_currency";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_COUNTRY_ID = "country_id";
        public static final String COLUMN_CURRENCY_ID = "currency_id";
        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_ID);
                    add(COLUMN_COUNTRY_ID);
                    add(COLUMN_CURRENCY_ID);
                }}
        );
    }

    public static abstract class Direction implements BaseColumns {
        public static final String TABLE_NAME = "directions";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_NAME_FR = "name_fr";
        public static final String COLUMN_NAME_NAME_ES = "name_es";
        public static final String COLUMN_NAME_NAME_ID = "name_id";
        public static final String COLUMN_NAME_NAME_PT = "name_pt";
        public static final String COLUMN_NAME_NAME_TR = "name_tr";
        public static final String COLUMN_NAME_NAME_LO = "name_lo";
        public static final String COLUMN_NAME_NAME_VI = "name_vi";
        public static final String COLUMN_NAME_NAME_TH = "name_th";
        public static final String COLUMN_NAME_IS_GIVING_DIRECTION = "is_giving_direction";
        public static final String COLUMN_NAME_IS_RECEIVING_DIRECTION = "is_receiving_direction";
        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_ID);
                    add(COLUMN_NAME_NAME);
                    add(COLUMN_NAME_NAME_FR);
                    add(COLUMN_NAME_NAME_ES);
                    add(COLUMN_NAME_NAME_ID);
                    add(COLUMN_NAME_NAME_PT);
                    add(COLUMN_NAME_NAME_TR);
                    add(COLUMN_NAME_NAME_LO);
                    add(COLUMN_NAME_NAME_VI);
                    add(COLUMN_NAME_NAME_TH);
                    add(COLUMN_NAME_IS_GIVING_DIRECTION);
                    add(COLUMN_NAME_IS_RECEIVING_DIRECTION);
                }}
        );

    }

    public static abstract class DirectionTransactionItemGroup implements BaseColumns {
        public static final String TABLE_NAME = "direction_transactionitemgroup";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_DIRECTION_ID = "direction_id";
        public static final String COLUMN_NAME_TRANSACTIONITEMGROUP_ID = "transactionitemgroup_id";
        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_ID);
                    add(COLUMN_NAME_DIRECTION_ID);
                    add(COLUMN_NAME_TRANSACTIONITEMGROUP_ID);
                }}
        );
    }

    public static abstract class DirectionTransactionItemType implements BaseColumns {
        public static final String TABLE_NAME = "direction_transactionitemtype";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_DIRECTION_ID = "direction_id";
        public static final String COLUMN_NAME_TRANSACTIONITEMTYPE_ID = "transactionitemtype_id";
        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_ID);
                    add(COLUMN_NAME_DIRECTION_ID);
                    add(COLUMN_NAME_TRANSACTIONITEMTYPE_ID);
                }}
        );
    }

    public static abstract class TransactionItemGroup implements BaseColumns {
        public static final String TABLE_NAME = "transactionitemgroups";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_NAME_FR = "name_fr";
        public static final String COLUMN_NAME_NAME_ES = "name_es";
        public static final String COLUMN_NAME_NAME_ID = "name_id";
        public static final String COLUMN_NAME_NAME_PT = "name_pt";
        public static final String COLUMN_NAME_NAME_TR = "name_tr";
        public static final String COLUMN_NAME_NAME_LO = "name_lo";
        public static final String COLUMN_NAME_NAME_VI = "name_vi";
        public static final String COLUMN_NAME_NAME_TH = "name_th";
        public static final String COLUMN_NAME_DISPLAY_CONFIGURATION = "display_configuration";
        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_ID);
                    add(COLUMN_NAME_NAME);
                    add(COLUMN_NAME_NAME_FR);
                    add(COLUMN_NAME_NAME_ES);
                    add(COLUMN_NAME_NAME_ID);
                    add(COLUMN_NAME_NAME_PT);
                    add(COLUMN_NAME_NAME_TR);
                    add(COLUMN_NAME_NAME_LO);
                    add(COLUMN_NAME_NAME_VI);
                    add(COLUMN_NAME_NAME_TH);
                    add(COLUMN_NAME_DISPLAY_CONFIGURATION);
                }}
        );

    }

    public static abstract class TransactionItemType implements BaseColumns {
        public static final String TABLE_NAME = "transactionitemtypes";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_TRANSACTIONITEMGROUP_ID = "transactionitemgroup_id";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_NAME_FR = "name_fr";
        public static final String COLUMN_NAME_NAME_ES = "name_es";
        public static final String COLUMN_NAME_NAME_ID = "name_id";
        public static final String COLUMN_NAME_NAME_PT = "name_pt";
        public static final String COLUMN_NAME_NAME_TR = "name_tr";
        public static final String COLUMN_NAME_NAME_LO = "name_lo";
        public static final String COLUMN_NAME_NAME_VI = "name_vi";
        public static final String COLUMN_NAME_NAME_TH = "name_th";
        public static final String COLUMN_NAME_DISPLAY_CONFIGURATION = "display_configuration";
        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_ID);
                    add(COLUMN_NAME_TRANSACTIONITEMGROUP_ID);
                    add(COLUMN_NAME_NAME);
                    add(COLUMN_NAME_NAME_FR);
                    add(COLUMN_NAME_NAME_ES);
                    add(COLUMN_NAME_NAME_ID);
                    add(COLUMN_NAME_NAME_PT);
                    add(COLUMN_NAME_NAME_TR);
                    add(COLUMN_NAME_NAME_LO);
                    add(COLUMN_NAME_NAME_VI);
                    add(COLUMN_NAME_NAME_TH);
                    add(COLUMN_NAME_DISPLAY_CONFIGURATION);
                }}
        );

    }

    public static abstract class ProductTransactionItemType implements BaseColumns {
        public static final String TABLE_NAME = "product_transactionitemtype";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_PRODUCT_ID = "product_id";
        public static final String COLUMN_NAME_TRANSACTIONITEMTYPE_ID = "transactionitemtype_id";
        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_ID);
                    add(COLUMN_NAME_TRANSACTIONITEMTYPE_ID);
                    add(COLUMN_NAME_PRODUCT_ID);
                }}
        );
    }

    public static abstract class TransactionItemTypeUnit implements BaseColumns {
        public static final String TABLE_NAME = "transactionitemtype_unit";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_TRANSACTIONITEMTYPE_ID = "transactionitemtype_id";
        public static final String COLUMN_NAME_UNIT_ID = "unit_id";
        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_ID);
                    add(COLUMN_NAME_TRANSACTIONITEMTYPE_ID);
                    add(COLUMN_NAME_UNIT_ID);
                }}
        );
    }

    public static abstract class TransactionItem implements BaseColumns {
        public static final String TABLE_NAME = "transactionitems";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_TRANSACTIONITEMGROUP_ID = "transactionitemgroup_id";
        public static final String COLUMN_NAME_TRANSACTIONITEMTYPE_ID = "transactionitemtype_id";
        public static final String COLUMN_NAME_BUYINGAGENT_ID = "buyingagent_id";
        public static final String COLUMN_NAME_TRANSACTEE_ID = "transactee_id";
        public static final String COLUMN_NAME_ENUMERATOR_ID = "enumerator_id";
        public static final String COLUMN_NAME_TRANSACTION_DATE = "transaction_date";
        public static final String COLUMN_NAME_TRANSACTEE_TYPE = "transactee_type";
        public static final String COLUMN_NAME_DIRECTION_ID = "direction_id";
        public static final String COLUMN_NAME_UNIT_ID = "unit_id";
        public static final String COLUMN_NAME_PRODUCT_ID = "product_id";
        public static final String COLUMN_NAME_PRODUCT_GRADE_ID = "productgrade_id";
        public static final String COLUMN_NAME_CURRENCY_ID = "currency_id";
        public static final String COLUMN_NAME_MOBILEMONEYCASH_ID = "mmcash_id";
        public static final String COLUMN_NAME_QUANTITY = "quantity";
        public static final String COLUMN_NAME_PRICE_PER_UNIT = "price_per_unit";
        public static final String COLUMN_NAME_TOTAL_PRICE = "total_price";
        public static final String COLUMN_NAME_AMOUNT_PAID = "amount_paid";
        public static final String COLUMN_NAME_AMOUNT_MOBILEMONEY = "amount_mobilemoney";
        public static final String COLUMN_NAME_AMOUNT_LOANED = "amount_loaned";
        public static final String COLUMN_NAME_AMOUNT_GIFTED = "amount_gifted";
        public static final String COLUMN_NAME_TRANSACTION_BALANCE_ID = "transactionbalance_id";
        public static final String COLUMN_NAME_IN_QUEUE = "in_network_queue";
        public static final String COLUMN_NAME_AMOUNT_LOANED_TEXT = "amount_loaned_text";
        public static final String COLUMN_NAME_TRANSACTION_NOTES = "transaction_notes";
        public static final String COLUMN_NAME_TRANSACTION_STATUS = "transaction_status";
        public static final String COLUMN_NAME_TRANSACTION_MMPP_TRANSACTION_ID = "MMPP_transactionid";
        public static final String COLUMN_NAME_TRANSACTION_MMPP_REQUEST_ID = "MMPP_requestid";
        public static final String COLUMN_NAME_TRANSACTION_MMPP_RECIPIENT_NUMBER = "MMPP_recipientnumber";
        public static final String COLUMN_NAME_TRANSACTION_MMPP_RECIPIENT_DIALLINGCODE = "MMPP_recipientdiallingcode";
        public static final String COLUMN_NAME_TRANSACTION_SIGNAL_STRENGTH = "signal_strength";
        public static final String COLUMN_NAME_SERVERITEM_ID = "server_item_id";
        public static final String COLUMN_NAME_UPLOAD_ID = "upload_id";
        public static final String COLUMN_NAME_FULLY_SYNCED = "fully_synced";
        public static final String COLUMN_NAME_UPLOAD_ATTEMPTS = "upload_attempts";
        public static final String COLUMN_NAME_CREATED_AT = "created_at";
        public static final String COLUMN_NAME_UPDATED_AT = "updated_at";
        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_ID);
                    add(COLUMN_NAME_TRANSACTIONITEMGROUP_ID);
                    add(COLUMN_NAME_TRANSACTIONITEMTYPE_ID);
                    add(COLUMN_NAME_BUYINGAGENT_ID);
                    add(COLUMN_NAME_TRANSACTEE_ID);
                    add(COLUMN_NAME_ENUMERATOR_ID);
                    add(COLUMN_NAME_TRANSACTION_DATE);
                    add(COLUMN_NAME_TRANSACTEE_TYPE);
                    add(COLUMN_NAME_DIRECTION_ID);
                    add(COLUMN_NAME_UNIT_ID);
                    add(COLUMN_NAME_PRODUCT_ID);
                    add(COLUMN_NAME_PRODUCT_GRADE_ID);
                    add(COLUMN_NAME_CURRENCY_ID);
                    add(COLUMN_NAME_MOBILEMONEYCASH_ID);
                    add(COLUMN_NAME_QUANTITY);
                    add(COLUMN_NAME_PRICE_PER_UNIT);
                    add(COLUMN_NAME_TOTAL_PRICE);
                    add(COLUMN_NAME_AMOUNT_PAID);
                    add(COLUMN_NAME_AMOUNT_MOBILEMONEY);
                    add(COLUMN_NAME_AMOUNT_LOANED);
                    add(COLUMN_NAME_AMOUNT_GIFTED);
                    add(COLUMN_NAME_TRANSACTION_BALANCE_ID);
                    add(COLUMN_NAME_IN_QUEUE);
                    add(COLUMN_NAME_AMOUNT_LOANED_TEXT);
                    add(COLUMN_NAME_TRANSACTION_NOTES);
                    add(COLUMN_NAME_TRANSACTION_STATUS);
                    add(COLUMN_NAME_TRANSACTION_MMPP_TRANSACTION_ID);
                    add(COLUMN_NAME_TRANSACTION_MMPP_REQUEST_ID);
                    add(COLUMN_NAME_TRANSACTION_MMPP_RECIPIENT_NUMBER);
                    add(COLUMN_NAME_TRANSACTION_MMPP_RECIPIENT_DIALLINGCODE);
                    add(COLUMN_NAME_TRANSACTION_SIGNAL_STRENGTH);
                    add(COLUMN_NAME_SERVERITEM_ID);
                    add(COLUMN_NAME_UPLOAD_ID);
                    add(COLUMN_NAME_FULLY_SYNCED);
                    add(COLUMN_NAME_UPLOAD_ATTEMPTS);
                    add(COLUMN_NAME_CREATED_AT);
                    add(COLUMN_NAME_UPDATED_AT);
                }}
        );

    }

    public static abstract class ProductGrade implements BaseColumns {
        public static final String TABLE_NAME = "productgrades";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_PRODUCT_ID = "product_id";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_NAME_FR = "name_fr";
        public static final String COLUMN_NAME_NAME_ES = "name_es";
        public static final String COLUMN_NAME_NAME_ID = "name_id";
        public static final String COLUMN_NAME_NAME_PT = "name_pt";
        public static final String COLUMN_NAME_NAME_TR = "name_tr";
        public static final String COLUMN_NAME_NAME_LO = "name_lo";
        public static final String COLUMN_NAME_NAME_VI = "name_vi";
        public static final String COLUMN_NAME_NAME_TH = "name_th";
        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_ID);
                    add(COLUMN_NAME_PRODUCT_ID);
                    add(COLUMN_NAME_NAME);
                    add(COLUMN_NAME_NAME_FR);
                    add(COLUMN_NAME_NAME_ES);
                    add(COLUMN_NAME_NAME_ID);
                    add(COLUMN_NAME_NAME_PT);
                    add(COLUMN_NAME_NAME_TR);
                    add(COLUMN_NAME_NAME_LO);
                    add(COLUMN_NAME_NAME_VI);
                    add(COLUMN_NAME_NAME_TH);
                }}
        );

    }

    public static abstract class ProductPrice implements BaseColumns {
        public static final String TABLE_NAME = "productprices";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_PRODUCT_ID = "product_id";
        public static final String COLUMN_NAME_CURRENCY_ID = "currency_id";
        public static final String COLUMN_NAME_DIRECTION_ID = "direction_id";
        public static final String COLUMN_NAME_FIXED_PRICE = "fixed_price";
        public static final String COLUMN_NAME_MIN_PRICE = "min_price";
        public static final String COLUMN_NAME_MAX_PRICE = "max_price";
        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_ID);
                    add(COLUMN_NAME_PRODUCT_ID);
                    add(COLUMN_NAME_CURRENCY_ID);
                    add(COLUMN_NAME_DIRECTION_ID);
                    add(COLUMN_NAME_FIXED_PRICE);
                    add(COLUMN_NAME_MIN_PRICE);
                    add(COLUMN_NAME_MAX_PRICE);
                }}
        );

    }

    public static abstract class Unit implements BaseColumns {
        public static final String TABLE_NAME = "units";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_NAME = "name";
        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_ID);
                    add(COLUMN_NAME_NAME);
                }}
        );
    }

    public static abstract class BuyingAgent implements BaseColumns {
        public static final String TABLE_NAME = "buyingagents";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_NAME = "name";
        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_ID);
                    add(COLUMN_NAME_NAME);
                }}
        );
    }

    public static abstract class Transactee implements BaseColumns {
        public static final String TABLE_NAME = "transactees";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_NAME = "name";
        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_ID);
                    add(COLUMN_NAME_NAME);
                }}
        );
    }

    public static abstract class TransactionBalance implements BaseColumns {
        public static final String TABLE_NAME = "transactionbalances";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_BUYINGAGENT_ID = "buyingagent_id";
        public static final String COLUMN_NAME_BUYINGAGENT2_ID = "buyingagent2_id";
        public static final String COLUMN_NAME_FARMER_ID = "farmer_id";
        public static final String COLUMN_NAME_CURRENCY_ID = "currency_id";
        public static final String COLUMN_NAME_BALANCE = "balance";
        public static final String COLUMN_NAME_OUT_OF_SYNC = "out_of_sync";
        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_ID);
                    add(COLUMN_NAME_BUYINGAGENT_ID);
                    add(COLUMN_NAME_BUYINGAGENT2_ID);
                    add(COLUMN_NAME_FARMER_ID);
                    add(COLUMN_NAME_CURRENCY_ID);
                    add(COLUMN_NAME_BALANCE);
                    add(COLUMN_NAME_OUT_OF_SYNC);
                }}
        );
    }

    public static abstract class BuyingRule implements BaseColumns {
        public static final String TABLE_NAME = "buyingrules";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_BUYINGAGENT_ID = "buyingagent_id";
        public static final String COLUMN_NAME_DIRECTION_ID = "direction_id";
        public static final String COLUMN_NAME_TRANSACTEE_TYPE = "transactee_type";
        public static final String COLUMN_NAME_TRANSACTIONITEMGROUP_ID = "transactionitemgroup_id";
        public static final String COLUMN_NAME_TRANSACTIONITEMTYPE_ID = "transactionitemtype_id";
        public static final String COLUMN_NAME_CAN_MOBILEMONEY = "can_mobilemoney";
        public static final String COLUMN_NAME_CAN_PAY = "can_pay";
        public static final String COLUMN_NAME_CAN_LOAN = "can_loan";
        public static final String COLUMN_NAME_CAN_GIFT = "can_gift";
        public static final String COLUMN_NAME_CAN_REPAY = "can_repay";
        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_ID);
                    add(COLUMN_NAME_BUYINGAGENT_ID);
                    add(COLUMN_NAME_DIRECTION_ID);
                    add(COLUMN_NAME_TRANSACTEE_TYPE);
                    add(COLUMN_NAME_TRANSACTIONITEMGROUP_ID);
                    add(COLUMN_NAME_TRANSACTIONITEMTYPE_ID);
                    add(COLUMN_NAME_CAN_MOBILEMONEY);
                    add(COLUMN_NAME_CAN_PAY);
                    add(COLUMN_NAME_CAN_LOAN);
                    add(COLUMN_NAME_CAN_GIFT);
                    add(COLUMN_NAME_CAN_REPAY);
                }}
        );
    }

    public static abstract class BuyingAgentBuyingRule implements BaseColumns {
        public static final String TABLE_NAME = "buyingagent_buyingrule";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_BUYINGAGENT_ID = "buyingagent_id";
        public static final String COLUMN_NAME_BUYINGRULE_ID = "buyingrule_id";
        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_ID);
                    add(COLUMN_NAME_BUYINGAGENT_ID);
                    add(COLUMN_NAME_BUYINGRULE_ID);
                }}
        );
    }

    public static abstract class BuyingAgentCurrency implements BaseColumns {
        public static final String TABLE_NAME = "buyingagent_currency";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_BUYINGAGENT_ID = "buyingagent_id";
        public static final String COLUMN_NAME_CURRENCY_ID = "currency_id";
        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_ID);
                    add(COLUMN_NAME_BUYINGAGENT_ID);
                    add(COLUMN_NAME_CURRENCY_ID);
                }}
        );
    }

    // recording training
    public static abstract class CompletionCriteria implements BaseColumns {
        public static final String TABLE_NAME = "completioncriteria";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_SCORING_COLUMNS = "scoring_columns";
        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_ID);
                    add(COLUMN_NAME_NAME);
                    add(COLUMN_NAME_SCORING_COLUMNS);
                }}
        );
    }

    public static abstract class Course implements BaseColumns {
        public static final String TABLE_NAME = "courses";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_NAME_FR = "name_fr";
        public static final String COLUMN_NAME_NAME_ES = "name_es";
        public static final String COLUMN_NAME_NAME_ID = "name_id";
        public static final String COLUMN_NAME_NAME_PT = "name_pt";
        public static final String COLUMN_NAME_NAME_TR = "name_tr";
        public static final String COLUMN_NAME_NAME_LO = "name_lo";
        public static final String COLUMN_NAME_NAME_VI = "name_vi";
        public static final String COLUMN_NAME_NAME_TH = "name_th";
        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_ID);
                    add(COLUMN_NAME_NAME);
                    add(COLUMN_NAME_NAME_FR);
                    add(COLUMN_NAME_NAME_ES);
                    add(COLUMN_NAME_NAME_ID);
                    add(COLUMN_NAME_NAME_PT);
                    add(COLUMN_NAME_NAME_TR);
                    add(COLUMN_NAME_NAME_LO);
                    add(COLUMN_NAME_NAME_VI);
                    add(COLUMN_NAME_NAME_TH);
                }}
        );

    }

    public static abstract class CourseAssignment implements BaseColumns {
        public static final String TABLE_NAME = "courseassignments";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_COURSE_ID = "course_id";
        public static final String COLUMN_NAME_COURSE_ASSIGNMENT_ID = "courseassignment_id";
        public static final String COLUMN_NAME_COURSE_ASSIGNMENT_TYPE = "courseassignment_type";
        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_ID);
                    add(COLUMN_NAME_COURSE_ID);
                    add(COLUMN_NAME_COURSE_ASSIGNMENT_ID);
                    add(COLUMN_NAME_COURSE_ASSIGNMENT_TYPE);
                }}
        );
    }

    public static abstract class TrainingAttendee implements BaseColumns {
        public static final String TABLE_NAME = "trainingattendees";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_FARMER_ID = "farmer_id";
        public static final String COLUMN_NAME_TRAINING_RECORD_ID = "trainingrecord_id";
        public static final String COLUMN_NAME_ATTENDED = "attended";
        public static final String COLUMN_NAME_PASSED = "passed";
        public static final String COLUMN_NAME_PRESCORE = "prescore";
        public static final String COLUMN_NAME_POSTSCORE = "postscore";
        public static final String COLUMN_NAME_SCORE = "score";
        public static final String COLUMN_NAME_PERCENTAGE = "percentage";
        public static final String COLUMN_NAME_GRADE = "grade";
        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_ID);
                    add(COLUMN_NAME_FARMER_ID);
                    add(COLUMN_NAME_TRAINING_RECORD_ID);
                    add(COLUMN_NAME_ATTENDED);
                    add(COLUMN_NAME_PASSED);
                    add(COLUMN_NAME_PRESCORE);
                    add(COLUMN_NAME_POSTSCORE);
                    add(COLUMN_NAME_SCORE);
                    add(COLUMN_NAME_PERCENTAGE);
                    add(COLUMN_NAME_GRADE);
                }}
        );

    }

    public static abstract class TrainingModule implements BaseColumns {
        public static final String TABLE_NAME = "trainingmodules";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_COURSE_ID = "course_id";
        public static final String COLUMN_COMPLETIONCRITERION_ID = "completioncriterion_id";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_NAME_FR = "name_fr";
        public static final String COLUMN_NAME_NAME_ES = "name_es";
        public static final String COLUMN_NAME_NAME_ID = "name_id";
        public static final String COLUMN_NAME_NAME_PT = "name_pt";
        public static final String COLUMN_NAME_NAME_TR = "name_tr";
        public static final String COLUMN_NAME_NAME_LO = "name_lo";
        public static final String COLUMN_NAME_NAME_VI = "name_vi";
        public static final String COLUMN_NAME_NAME_TH = "name_th";
        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_ID);
                    add(COLUMN_NAME_COURSE_ID);
                    add(COLUMN_COMPLETIONCRITERION_ID);
                    add(COLUMN_NAME_NAME);
                    add(COLUMN_NAME_NAME_FR);
                    add(COLUMN_NAME_NAME_ES);
                    add(COLUMN_NAME_NAME_ID);
                    add(COLUMN_NAME_NAME_PT);
                    add(COLUMN_NAME_NAME_TR);
                    add(COLUMN_NAME_NAME_LO);
                    add(COLUMN_NAME_NAME_VI);
                    add(COLUMN_NAME_NAME_TH);
                }}
        );

    }

    public static abstract class TrainingRecord implements BaseColumns {
        public static final String TABLE_NAME = "trainingrecords";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_TRAININGMODULE_ID = "trainingmodule_id";
        public static final String COLUMN_NAME_SUBMISSION_TIME = "submission_time";
        public static final String COLUMN_NAME_PHOTO = "photo";
        public static final String COLUMN_NAME_IN_QUEUE = "in_network_queue";
        public static final String COLUMN_NAME_COURSE_ID="course_id";
        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_ID);
                    add(COLUMN_TRAININGMODULE_ID);
                    add(COLUMN_NAME_SUBMISSION_TIME);
                    add(COLUMN_NAME_PHOTO);
                    add(COLUMN_NAME_IN_QUEUE);
                    add(COLUMN_NAME_COURSE_ID);
                }}
        );

    }

    // tasks
    public static abstract class Task implements BaseColumns {
        public static final String TABLE_NAME = "tasks";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_TASKTEXT = "tasktext";
        public static final String COLUMN_NAME_TASK_CREATOR = "task_creator";
        public static final String COLUMN_NAME_COMPLETED = "completed";
        public static final String COLUMN_NAME_CREATED_DATE = "created_date";
        public static final String COLUMN_NAME_ENUMERATORS_TASK_ID = "enumeratorTasksId";
        public static final String COLUMN_NAME_COMPLETED_DATE = "completed_date";
        public static final String COLUMN_NAME_SYNCED = "synced";
        public static final String COLUMN_NAME_DELIVERY_CONFIRMED = "delivery_confirmed";
        public static final List<String> COLUMN_LIST = Collections.unmodifiableList(
                new ArrayList<String>() {{
                    add(COLUMN_NAME_ID);
                    add(COLUMN_NAME_TASKTEXT);
                    add(COLUMN_NAME_TASK_CREATOR);
                    add(COLUMN_NAME_COMPLETED);
                    add(COLUMN_NAME_CREATED_DATE);
                    add(COLUMN_NAME_COMPLETED_DATE);
                    add(COLUMN_NAME_SYNCED);
                    add(COLUMN_NAME_DELIVERY_CONFIRMED);
                    add(COLUMN_NAME_ENUMERATORS_TASK_ID);
                }}
        );
    }

    public static final LinkedHashMap<String, List<String>> ALL_TABLES =
            new LinkedHashMap<String, List<String>>() {{
                put(Currency.TABLE_NAME, Currency.COLUMN_LIST);
                put(CountryCurrency.TABLE_NAME, CountryCurrency.COLUMN_LIST);
                put(TransactionItemGroup.TABLE_NAME, TransactionItemGroup.COLUMN_LIST);
                put(TransactionItemType.TABLE_NAME, TransactionItemType.COLUMN_LIST);
                put(ProductTransactionItemType.TABLE_NAME, ProductTransactionItemType.COLUMN_LIST);
                put(TransactionItemTypeUnit.TABLE_NAME, TransactionItemTypeUnit.COLUMN_LIST);
                put(TransactionItem.TABLE_NAME, TransactionItem.COLUMN_LIST);
                put(Unit.TABLE_NAME, Unit.COLUMN_LIST);
                put(Direction.TABLE_NAME, Direction.COLUMN_LIST);
                put(DirectionTransactionItemGroup.TABLE_NAME, DirectionTransactionItemGroup.COLUMN_LIST);
                put(DirectionTransactionItemType.TABLE_NAME, DirectionTransactionItemType.COLUMN_LIST);
                put(ProductGrade.TABLE_NAME, ProductGrade.COLUMN_LIST);
                put(ProductPrice.TABLE_NAME, ProductPrice.COLUMN_LIST);
                put(BuyingAgent.TABLE_NAME, BuyingAgent.COLUMN_LIST);
                put(Transactee.TABLE_NAME, Transactee.COLUMN_LIST);
                put(TransactionBalance.TABLE_NAME, TransactionBalance.COLUMN_LIST);
                put(BuyingRule.TABLE_NAME, BuyingRule.COLUMN_LIST);
                put(BuyingAgentBuyingRule.TABLE_NAME, BuyingAgentBuyingRule.COLUMN_LIST);
                put(BuyingAgentCurrency.TABLE_NAME, BuyingAgentCurrency.COLUMN_LIST);
                put(CompletionCriteria.TABLE_NAME, CompletionCriteria.COLUMN_LIST);
                put(Course.TABLE_NAME, Course.COLUMN_LIST);
                put(CourseAssignment.TABLE_NAME, CourseAssignment.COLUMN_LIST);
                put(TrainingModule.TABLE_NAME, TrainingModule.COLUMN_LIST);
                put(TrainingAttendee.TABLE_NAME, TrainingAttendee.COLUMN_LIST);
                put(TrainingRecord.TABLE_NAME, TrainingRecord.COLUMN_LIST);
                put(ModuleEntry.TABLE_NAME, ModuleEntry.COLUMN_LIST);
                put(SubmoduleEntry.TABLE_NAME, SubmoduleEntry.COLUMN_LIST);
                put(ModuleAssignmentsEntry.TABLE_NAME, ModuleAssignmentsEntry.COLUMN_LIST);
                put(FarmerModulesEntry.TABLE_NAME, FarmerModulesEntry.COLUMN_LIST);
                put(DatatypesEntry.TABLE_NAME, DatatypesEntry.COLUMN_LIST);
                put(LookupValuesEntry.TABLE_NAME, LookupValuesEntry.COLUMN_LIST);
                put(LookupValuesSipEntry.TABLE_NAME, LookupValuesSipEntry.COLUMN_LIST);
                put(QuestionEntry.TABLE_NAME, QuestionEntry.COLUMN_LIST);
                put(AnswerEntry.TABLE_NAME, AnswerEntry.COLUMN_LIST);
                put(FarmerEntry.TABLE_NAME, FarmerEntry.COLUMN_LIST);
                put(PhoneNumber.TABLE_NAME, PhoneNumber.COLUMN_LIST);
                put(CountriesEntry.TABLE_NAME, CountriesEntry.COLUMN_LIST);
                put(RegionsEntry.TABLE_NAME, RegionsEntry.COLUMN_LIST);
                put(DistrictsEntry.TABLE_NAME, DistrictsEntry.COLUMN_LIST);
                put(PlacesEntry.TABLE_NAME, PlacesEntry.COLUMN_LIST);
                put(FarmerGroupsEntry.TABLE_NAME, FarmerGroupsEntry.COLUMN_LIST);
                put(SectionsEntry.TABLE_NAME, SectionsEntry.COLUMN_LIST);
                put(ProductsEntry.TABLE_NAME, ProductsEntry.COLUMN_LIST);
                put(NetworkQueueEntry.TABLE_NAME, NetworkQueueEntry.COLUMN_LIST);
                put(FarmManagementPlanEntry.TABLE_NAME, FarmManagementPlanEntry.COLUMN_LIST);
                put(FarmManagementPlanSectionsEntry.TABLE_NAME, FarmManagementPlanSectionsEntry.COLUMN_LIST);
                put(FarmManagementPlanEntriesEntry.TABLE_NAME, FarmManagementPlanEntriesEntry.COLUMN_LIST);
                put(FarmManagementPlanValuesEntry.TABLE_NAME, FarmManagementPlanValuesEntry.COLUMN_LIST);
                put(FarmManagementPlanCurrentValuesEntry.TABLE_NAME, FarmManagementPlanCurrentValuesEntry.COLUMN_LIST);
                put(Subquestionanswer.TABLE_NAME, Subquestionanswer.COLUMN_LIST);
                put(countryLookUpValuesEntry.TABLE_NAME, countryLookUpValuesEntry.COLUMN_LIST);
                put(productSipEntry.TABLE_NAME, productSipEntry.COLUMN_LIST);
                put(ChildEntry.TABLE_NAME,ChildEntry.COLUMN_LIST);
                put(ChildVisitDetailsEntry.TABLE_NAME,ChildVisitDetailsEntry.COLUMN_LIST);
                put(FarmerChildModulesEntry.TABLE_NAME,FarmerChildModulesEntry.COLUMN_LIST);
                put(AnswerChildEntry.TABLE_NAME,AnswerChildEntry.COLUMN_LIST);
            }};



}