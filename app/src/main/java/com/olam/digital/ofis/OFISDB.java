package com.olam.digital.ofis;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by bengallienne on 29/11/14. - update
 * Describes and creates / updates all the tables the app needs in the SQLite database
 */
public class OFISDB extends SQLiteOpenHelper {
    private static volatile OFISDB sInstance;

    private static final int DATABASE_VERSION = 163;

    private static final String DATABASE_NAME = "ofismobile";

    // Child Labour Table Creation
    private static final String OFIS_CHILD_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS "
            + OFISDBContract.ChildEntry.TABLE_NAME
            + " ( " + OFISDBContract.ChildEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + OFISDBContract.ChildEntry.COLUMN_NAME_ONLINE_ID + " INTEGER, "
            + OFISDBContract.ChildEntry.COLUMN_NAME_FARMER_ID + " INTEGER, "
            + OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_NAME + " TEXT, "
            + OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_AGE + " INTEGER, "
            + OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_GENDER + " TEXT, "
            + OFISDBContract.ChildEntry.COLUMN_NAME_ACTION_FLAG + " INTEGER, "
            + OFISDBContract.ChildEntry.COLUMN_NAME_STATUS + " TEXT, "

            + OFISDBContract.ChildEntry.COLUMN_NAME_MODULE_DTO + " TEXT, "

            + OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_CREATEDAT + " TEXT, "
            + OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_SUBMITTED_MODULES + " TEXT, "
            + OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_ACTION_TICKED + " TEXT, "
            + OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_AGE_ON_YEAR + " INTEGER, "
            + OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_SYNCED + " INTEGER DEFAULT 1)";

    private static final String OFIS_CHILD_INDIVIDUAL_SURVEY_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS "
            + OFISDBContract.ChildVisitDetails.TABLE_NAME
            + " ( " + OFISDBContract.ChildVisitDetails.COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + OFISDBContract.ChildVisitDetails.COLUMN_NAME_FARMER_ID + " INTEGER, "
            + OFISDBContract.ChildVisitDetails.COLUMN_NAME_SURVEY_CODE + " INTEGER, "
            + OFISDBContract.ChildVisitDetails.COLUMN_NAME_SURVEY_DESCRIPTION + " TEXT, "
            + OFISDBContract.ChildVisitDetails.COLUMN_NAME_SURVEY_VISIT_SYNCED + " INTEGER DEFAULT 1)";

    private static final String OFIS_GPS_MONITOR_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS "
            + OFISDBContract.GPSDetails.TABLE_NAME
            + " ( " + OFISDBContract.GPSDetails.COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + OFISDBContract.GPSDetails.COLUMN_NAME_LATITUDE + " TEXT, "
            + OFISDBContract.GPSDetails.COLUMN_NAME_LONGITUDE + " TEXT, "
            + OFISDBContract.GPSDetails.COLUMN_NAME_FARMER_ID + " TEXT, "
            + OFISDBContract.GPSDetails.COLUMN_NAME_FARMER_NAME + " TEXT, "
            + OFISDBContract.GPSDetails.COLUMN_NAME_CHILD_ID + " TEXT, "
            + OFISDBContract.GPSDetails.COLUMN_NAME_CHILD_NAME + " TEXT, "
            + OFISDBContract.GPSDetails.COLUMN_NAME_SIP_NAME + " TEXT, "
            + OFISDBContract.GPSDetails.COLUMN_NAME_SIP_TYPE + " TEXT, "
            + OFISDBContract.GPSDetails.COLUMN_NAME_TRAINING_COURSE_NAME + " TEXT, "
            + OFISDBContract.GPSDetails.COLUMN_NAME_TRAINING_MODULE_NAME + " TEXT, "
            + OFISDBContract.GPSDetails.COLUMN_NAME_FARMER_GENDER + " TEXT, "
            + OFISDBContract.GPSDetails.COLUMN_NAME_CHILD_GENDER + " TEXT, "
            + OFISDBContract.GPSDetails.COLUMN_NAME_CHILD_AGE + " TEXT, "
            + OFISDBContract.GPSDetails.COLUMN_NAME_GPS_ACCURACY + " TEXT, "
            + OFISDBContract.GPSDetails.COLUMN_NAME_NATIONAL_ID + " TEXT, "
            + OFISDBContract.GPSDetails.COLUMN_NAME_POLYGON_PLOTS_DETAILS + " TEXT, "
            + OFISDBContract.GPSDetails.COLUMN_NAME_SURVEY_NAME + " TEXT, "
            + OFISDBContract.GPSDetails.COLUMN_NAME_RECORDED_DATE + " TEXT, "
            + OFISDBContract.GPSDetails.COLUMN_NAME_NETWORK_QUEUE_ID + " TEXT, "
            + OFISDBContract.GPSDetails.COLUMN_NAME_SUBMITTED_DATE + " TEXT, "
            + OFISDBContract.GPSDetails.COLUMN_NAME_CAPTURED_MODULE + " TEXT)";


    private static final String OFIS_CHILD_VIST_DETAILS_CREATE = "CREATE TABLE IF NOT EXISTS "
            + OFISDBContract.ChildVisitDetailsEntry.TABLE_NAME
            + " ( " + OFISDBContract.ChildVisitDetailsEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + OFISDBContract.ChildVisitDetailsEntry.COLUMN_NAME_FARMER_ID + " INTEGER, "
            + OFISDBContract.ChildVisitDetailsEntry.COLUMN_HOUSEHOLD + " INTEGER, "
            + OFISDBContract.ChildVisitDetailsEntry.COLUMN_INSPECTION + " INTEGER, "
            + OFISDBContract.ChildVisitDetailsEntry.COLUMN_FOLLOWUP + " INTEGER, "
            + OFISDBContract.ChildEntry.COLUMN_NAME_CHILD_SYNCED + " INTEGER DEFAULT 1)";


    private static final String OFIS_CONSENT_STATEMENT_CREATE = "CREATE TABLE IF NOT EXISTS "
            + OFISDBContract.ConsentStatementEntry.TABLE_NAME
            + " ( " + OFISDBContract.ConsentStatementEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + OFISDBContract.ConsentStatementEntry.COLUMN_NAME_FARMER_ID + " INTEGER, "
            + OFISDBContract.PhoneNumber.COLUMN_NAME_MULTI_STAKEHOLDER_ID + " INTEGER, "
            + OFISDBContract.ConsentStatementEntry.COLUMN_NAME_DISPLAYFARMER_ID + " TEXT, "
            + OFISDBContract.ConsentStatementEntry.COLUMN_NAME_FARMER + " TEXT, "
            + OFISDBContract.ConsentStatementEntry.COLUMN_NAME_OLAM_FARMER_NAME + " TEXT, "
            + OFISDBContract.ConsentStatementEntry.COLUMN_NAME_FARMER_CONFIRMATION + " TEXT, "
            + OFISDBContract.ConsentStatementEntry.COLUMN_NAME_CHILDNAME + " TEXT, "
            + OFISDBContract.ConsentStatementEntry.COLUMN_NAME_OCCUPATION + " TEXT, "
            + OFISDBContract.ConsentStatementEntry.COLUMN_NAME_FARMER_SIGN + " TEXT, "
            + OFISDBContract.ConsentStatementEntry.COLUMN_NAME_CONFIRMED_DATE + " TEXT, "
            + OFISDBContract.ConsentStatementEntry.COLUMN_NAME_ENUM_SIGN + " TEXT, "
            + OFISDBContract.ConsentStatementEntry.COLUMN_NAME_MARITAL + " TEXT, "
            + OFISDBContract.ConsentStatementEntry.COLUMN_NAME_PROFESSION + " TEXT, "
            + OFISDBContract.ConsentStatementEntry.COLUMN_NAME_CPF_NO + " TEXT, "
            + OFISDBContract.ConsentStatementEntry.COLUMN_NAME_RG_NO + " TEXT, "
            + OFISDBContract.ConsentStatementEntry.COLUMN_NAME_FULL_ADDRESS + " TEXT, "
            + OFISDBContract.ConsentStatementEntry.COLUMN_NAME_CITY + " TEXT, "
            + OFISDBContract.ConsentStatementEntry.COLUMN_NAME_STATE + " TEXT, "
            + OFISDBContract.ConsentStatementEntry.COLUMN_NAME_SUBNAME + " TEXT, "
            + OFISDBContract.ConsentStatementEntry.COLUMN_NAME_SUBNATIONALITY + " TEXT, "
            + OFISDBContract.ConsentStatementEntry.COLUMN_NAME_SUBMARITAL + " TEXT, "
            + OFISDBContract.ConsentStatementEntry.COLUMN_NAME_SUBPROFESSION + " TEXT, "
            + OFISDBContract.ConsentStatementEntry.COLUMN_NAME_SUBCPF_NO + " TEXT, "
            + OFISDBContract.ConsentStatementEntry.COLUMN_NAME_SUBRG_NO + " TEXT, "
            + OFISDBContract.ConsentStatementEntry.COLUMN_NAME_SUBFULL_ADDRESS + " TEXT, "
            + OFISDBContract.ConsentStatementEntry.COLUMN_NAME_SUBCITY + " TEXT, "
            + OFISDBContract.ConsentStatementEntry.COLUMN_NAME_SUBSTATE + " TEXT, "
            + OFISDBContract.ConsentStatementEntry.COLUMN_NAME_CONTACT + " TEXT, "
            + OFISDBContract.ConsentStatementEntry.COLUMN_NAME_WIT1_NAME + " TEXT, "
            + OFISDBContract.ConsentStatementEntry.COLUMN_NAME_WIT1_CPF + " TEXT, "
            + OFISDBContract.ConsentStatementEntry.COLUMN_NAME_WIT1_RG + " TEXT, "
            + OFISDBContract.ConsentStatementEntry.COLUMN_NAME_WIT1_SIGN + " TEXT, "
            + OFISDBContract.ConsentStatementEntry.COLUMN_NAME_WIT2_NAME + " TEXT, "
            + OFISDBContract.ConsentStatementEntry.COLUMN_NAME_WIT2_CPF + " TEXT, "
            + OFISDBContract.ConsentStatementEntry.COLUMN_NAME_WIT2_RG + " TEXT, "
            + OFISDBContract.ConsentStatementEntry.COLUMN_NAME_CONSENT_CHECKBOX + " TEXT, "
            + OFISDBContract.ConsentStatementEntry.COLUMN_NAME_WIT2_SIGN + " TEXT)";

    //Child Labour Table end

    // create tables
    private static final String OFIS_MODULES_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.ModuleEntry.TABLE_NAME +
            " ( " + OFISDBContract.ModuleEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY, " + OFISDBContract.ModuleEntry.COLUMN_NAME_NAME + " TEXT, "
            + OFISDBContract.ModuleEntry.COLUMN_NAME_NAME_FR + " TEXT, " + OFISDBContract.ModuleEntry.COLUMN_NAME_NAME_ES + " TEXT, "
            + OFISDBContract.ModuleEntry.COLUMN_NAME_NAME_ID + " TEXT, " + OFISDBContract.ModuleEntry.COLUMN_NAME_NAME_PT + " TEXT, "
            + OFISDBContract.ModuleEntry.COLUMN_NAME_NAME_TR + " TEXT, " + OFISDBContract.ModuleEntry.COLUMN_NAME_NAME_LO + " TEXT, "
            + OFISDBContract.ModuleEntry.COLUMN_NAME_NAME_VI + " TEXT, " + OFISDBContract.ModuleEntry.COLUMN_NAME_NAME_TH + " TEXT, "
            + OFISDBContract.ModuleEntry.COLUMN_NAME_MODULE_CODE + " TEXT, "
            + OFISDBContract.ModuleEntry.COLUMN_NAME_EXPIRES + " TEXT, " + OFISDBContract.ModuleEntry.COLUMN_NAME_ANNUAL_SURVEY + " INTEGER, "
            + OFISDBContract.ModuleEntry.COLUMN_NAME_DESCRIPTION + " TEXT, "
            + OFISDBContract.ModuleEntry.COLUMN_NAME_DESCRIPTION_ES + " TEXT, "
            + OFISDBContract.ModuleEntry.COLUMN_NAME_DESCRIPTION_FR + " TEXT, "
            + OFISDBContract.ModuleEntry.COLUMN_NAME_DESCRIPTION_ID + " TEXT, "
            + OFISDBContract.ModuleEntry.COLUMN_NAME_DESCRIPTION_LO + " TEXT, "
            + OFISDBContract.ModuleEntry.COLUMN_NAME_DESCRIPTION_PT + " TEXT, "
            + OFISDBContract.ModuleEntry.COLUMN_NAME_DESCRIPTION_TH + " TEXT, "
            + OFISDBContract.ModuleEntry.COLUMN_NAME_DESCRIPTION_TR + " TEXT, "
            + OFISDBContract.ModuleEntry.COLUMN_NAME_DESCRIPTION_VI + " TEXT, "
            + OFISDBContract.ModuleEntry.COLUMN_NAME_SURVEY_TYPE + " TEXT, "
            + OFISDBContract.ModuleEntry.COLUMN_NAME_IS_MANDATE + " TEXT, "
            + OFISDBContract.ModuleEntry.COLUMN_NAME_IS_AUTO_SURVEY + " TEXT )";
    // create SubModules
    private static final String OFIS_SUBMODULES_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.SubmoduleEntry.TABLE_NAME
            + " ( " + OFISDBContract.SubmoduleEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY, " + OFISDBContract.SubmoduleEntry.COLUMN_NAME_MODULE_ID + " INTEGER, "
            + OFISDBContract.SubmoduleEntry.COLUMN_NAME_POSITION + " INTEGER, " + OFISDBContract.SubmoduleEntry.COLUMN_NAME_NAME + " TEXT, "
            + OFISDBContract.SubmoduleEntry.COLUMN_NAME_NAME_FR + " TEXT, " + OFISDBContract.SubmoduleEntry.COLUMN_NAME_NAME_ES + " TEXT, "
            + OFISDBContract.SubmoduleEntry.COLUMN_NAME_NAME_ID + " TEXT, " + OFISDBContract.SubmoduleEntry.COLUMN_NAME_NAME_PT + " TEXT, "
            + OFISDBContract.SubmoduleEntry.COLUMN_NAME_NAME_TR + " TEXT, " + OFISDBContract.SubmoduleEntry.COLUMN_NAME_NAME_LO + " TEXT, "
            + OFISDBContract.SubmoduleEntry.COLUMN_NAME_NAME_VI + " TEXT, " + OFISDBContract.SubmoduleEntry.COLUMN_NAME_NAME_TH + " TEXT, "
            + OFISDBContract.SubmoduleEntry.COLUMN_NAME_IS_DEFAULT + " TEXT, "
            + OFISDBContract.SubmoduleEntry.COLUMN_NAME_SHOW_DESCRIPTION + " TEXT, "
            + OFISDBContract.SubmoduleEntry.COLUMN_NAME_DESCRIPTION + " TEXT, "
            + OFISDBContract.SubmoduleEntry.COLUMN_NAME_DESCRIPTION_ES + " TEXT, "
            + OFISDBContract.SubmoduleEntry.COLUMN_NAME_DESCRIPTION_FR + " TEXT, "
            + OFISDBContract.SubmoduleEntry.COLUMN_NAME_DESCRIPTION_ID + " TEXT, "
            + OFISDBContract.SubmoduleEntry.COLUMN_NAME_DESCRIPTION_LO + " TEXT, "
            + OFISDBContract.SubmoduleEntry.COLUMN_NAME_DESCRIPTION_PT + " TEXT, "
            + OFISDBContract.SubmoduleEntry.COLUMN_NAME_DESCRIPTION_TH + " TEXT, "
            + OFISDBContract.SubmoduleEntry.COLUMN_NAME_DESCRIPTION_TR + " TEXT, "
            + OFISDBContract.SubmoduleEntry.COLUMN_NAME_DESCRIPTION_VI + " TEXT)";

    private static final String OFIS_QUESTIONTYPE_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.QuestionType.TABLE_NAME
            + " ( " + OFISDBContract.QuestionType.COLUMN_NAME_ID + " INTEGER PRIMARY KEY, "
            + OFISDBContract.QuestionType.COLUMN_NAME_VALUE + " TEXT, "
            + OFISDBContract.QuestionType.COLUMN_NAME_ACTIVE + " TEXT)";

    private static final String OFIS_ANSWERTYPE_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.AnswerType.TABLE_NAME
            + " ( " + OFISDBContract.AnswerType.COLUMN_NAME_ID + " INTEGER PRIMARY KEY, "
            + OFISDBContract.AnswerType.COLUMN_NAME_VALUE + " TEXT, "
            + OFISDBContract.AnswerType.COLUMN_NAME_ACTIVE + " TEXT)";

    private static final String OFIS_RECURRENCETYPE_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.RecurrenceType.TABLE_NAME
            + " ( " + OFISDBContract.RecurrenceType.COLUMN_NAME_ID + " INTEGER PRIMARY KEY, "
            + OFISDBContract.RecurrenceType.COLUMN_NAME_VALUE + " TEXT, "
            + OFISDBContract.RecurrenceType.COLUMN_NAME_ACTIVE + " TEXT)";


    private static final String OFIS_SIP_PLOT_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.SipPlot.TABLE_NAME
            + " ( " + OFISDBContract.SipPlot.COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + OFISDBContract.SipPlot.COLUMN_NAME_SIP_UPLOAD_ID + " TEXT, "
            + OFISDBContract.SipPlot.COLUMN_NAME_SIP_CATEGORY_ID + " TEXT, "
            + OFISDBContract.SipPlot.COLUMN_NAME_SIP_CATEGORY + " TEXT, "
            + OFISDBContract.SipPlot.COLUMN_NAME_SIP_NAME + " TEXT, "
            + OFISDBContract.SipPlot.COLUMN_NAME_IS_SYNCED + " TEXT, "
            + OFISDBContract.SipPlot.COLUMN_NAME_SIP_PLOT + " TEXT)";

    // create NationalidModules
    private static final String OFIS_NATIONALIDS_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.NationalIDEntry.TABLE_NAME
            + " ( " + OFISDBContract.NationalIDEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY, "
            + OFISDBContract.NationalIDEntry.COLUMN_NAME_NID_LIMIT + " INTEGER, " + OFISDBContract.NationalIDEntry.COLUMN_NAME_NID_TYPE + " TEXT, "
            + OFISDBContract.NationalIDEntry.COLUMN_NAME_EXP_DATE_SLIDER + " TEXT, "
            + OFISDBContract.NationalIDEntry.COLUMN_NAME_PHOTO_REQUIRED + " TEXT, " + OFISDBContract.NationalIDEntry.COLUMN_NAME_NID_ACTIVE + " TEXT, "
            + OFISDBContract.NationalIDEntry.COLUMN_NAME_STARTYEAR + " TEXT, " + OFISDBContract.NationalIDEntry.COLUMN_NAME_ENDYEAR + " TEXT, "
            + OFISDBContract.NationalIDEntry.COLUMN_NAME_MONTH + " TEXT, "
            + OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_FR + " TEXT, " + OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_ES + " TEXT, "
            + OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_PT + " TEXT, " + OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_ID + " TEXT, "
            + OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_TR + " TEXT, " + OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_LO + " TEXT, "
            + OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_VI + " TEXT, " + OFISDBContract.NationalIDEntry.COLUMN_NAME_TYPE_TH + " TEXT )";


    // create SIP Modules
    private static final String OFIS_SIPGROUP_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.SipGroupEntry.TABLE_NAME +
            " ( " + OFISDBContract.SipGroupEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY, " + OFISDBContract.SipGroupEntry.COLUMN_NAME_NAME + " TEXT, "
            + OFISDBContract.SipGroupEntry.COLUMN_NAME_NAME_FR + " TEXT, " + OFISDBContract.SipGroupEntry.COLUMN_NAME_NAME_ES + " TEXT, "
            + OFISDBContract.SipGroupEntry.COLUMN_NAME_NAME_ID + " TEXT, " + OFISDBContract.SipGroupEntry.COLUMN_NAME_NAME_PT + " TEXT, "
            + OFISDBContract.SipGroupEntry.COLUMN_NAME_NAME_TR + " TEXT, " + OFISDBContract.SipGroupEntry.COLUMN_NAME_NAME_LO + " TEXT, "
            + OFISDBContract.SipGroupEntry.COLUMN_NAME_NAME_VI + " TEXT, " + OFISDBContract.SipGroupEntry.COLUMN_NAME_NAME_TH + " TEXT, "
            + OFISDBContract.SipGroupEntry.COLUMN_NAME_EXPIRES + " TEXT, " + OFISDBContract.SipGroupEntry.COLUMN_NAME_ANNUAL_SURVEY + " INTEGER )";
    // create SIP SubModules
    private static final String OFIS_SIPSUBGROUP_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.SipSubGroupEntry.TABLE_NAME
            + " ( " + OFISDBContract.SipSubGroupEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY, " + OFISDBContract.SipSubGroupEntry.COLUMN_NAME_MODULE_ID + " INTEGER, "
            /*+ OFISDBContract.SipSubGroupEntry.COLUMN_NAME_POSITION + " INTEGER, " */
            + OFISDBContract.SipSubGroupEntry.COLUMN_NAME_CAPTURE_SIP_POLYGON + " TEXT, "
            + OFISDBContract.SipSubGroupEntry.COLUMN_NAME_NAME + " TEXT, "
            + OFISDBContract.SipSubGroupEntry.COLUMN_NAME_NAME_FR + " TEXT, " + OFISDBContract.SipSubGroupEntry.COLUMN_NAME_NAME_ES + " TEXT, "
            + OFISDBContract.SipSubGroupEntry.COLUMN_NAME_NAME_ID + " TEXT, " + OFISDBContract.SipSubGroupEntry.COLUMN_NAME_NAME_PT + " TEXT, "
            + OFISDBContract.SipSubGroupEntry.COLUMN_NAME_NAME_TR + " TEXT, " + OFISDBContract.SipSubGroupEntry.COLUMN_NAME_NAME_LO + " TEXT, "
            + OFISDBContract.SipSubGroupEntry.COLUMN_NAME_NAME_VI + " TEXT, " + OFISDBContract.SipSubGroupEntry.COLUMN_NAME_NAME_TH + " TEXT,"
            + OFISDBContract.SipSubGroupEntry.COLUMN_NAME_PHOTO + " TEXT, " + OFISDBContract.SipSubGroupEntry.COLUMN_NAME_SHOWIN + " TEXT, "
            + OFISDBContract.SipSubGroupEntry.COLUMN_NAME_VILLAGE + " TEXT, "
            + OFISDBContract.SipSubGroupEntry.COLUMN_NAME_IMAGE_ENCODSTR + " TEXT)";


    private static final String OFIS_QUESTIONSIP_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.QuestionSipEntry.TABLE_NAME +
            " ( " + OFISDBContract.QuestionSipEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY, " + OFISDBContract.QuestionSipEntry.COLUMN_NAME_SUBMODULE_ID + " INTEGER, "
            + OFISDBContract.QuestionSipEntry.COLUMN_NAME_DATATYPE_ID + " INTEGER, " + OFISDBContract.QuestionSipEntry.COLUMN_NAME_POSITION + " INTEGER, "
            + OFISDBContract.QuestionSipEntry.COLUMN_NAME_DISPLAY_TYPE + " TEXT, " + OFISDBContract.QuestionSipEntry.COLUMN_NAME_QUESTION + " TEXT, "
            + OFISDBContract.QuestionSipEntry.COLUMN_NAME_QUESTION_FR + " TEXT, " + OFISDBContract.QuestionSipEntry.COLUMN_NAME_QUESTION_ES + " TEXT, "
            + OFISDBContract.QuestionSipEntry.COLUMN_NAME_QUESTION_ID + " TEXT, " + OFISDBContract.QuestionSipEntry.COLUMN_NAME_QUESTION_PT + " TEXT, "
            + OFISDBContract.QuestionSipEntry.COLUMN_NAME_QUESTION_TR + " TEXT, " + OFISDBContract.QuestionSipEntry.COLUMN_NAME_QUESTION_LO + " TEXT, "
            + OFISDBContract.QuestionSipEntry.COLUMN_NAME_QUESTION_VI + " TEXT, " + OFISDBContract.QuestionSipEntry.COLUMN_NAME_QUESTION_TH + " TEXT, "
            + OFISDBContract.QuestionSipEntry.COLUMN_NAME_DISPLAY_CONFIGURATION + " TEXT)";

    // SIP - product list table
    private static final String OFIS_PRODUCTLIST_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.productSipEntry.TABLE_NAME +
            " ( " + OFISDBContract.productSipEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + OFISDBContract.productSipEntry.COLUMN_NAME_PRODUCT_ID + " INTEGER, "
            + OFISDBContract.productSipEntry.COLUMN_NAME_PRODUCT_NAME + " TEXT)";

    //  Create SIP Answers Table
    private static final String OFIS_ANSWERSIP_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.AnswerSipEntry.TABLE_NAME +
            " ( " + OFISDBContract.AnswerSipEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + OFISDBContract.AnswerSipEntry.COLUMN_NAME_UPLOAD_ID + " TEXT, "
            + OFISDBContract.AnswerSipEntry.COLUMN_NAME_QUESTION_ID + " INTEGER, " + OFISDBContract.AnswerEntry.COLUMN_NAME_PARENT_QUESTION_ID + " INTEGER, "
            + OFISDBContract.AnswerEntry.COLUMN_NAME_REPEATER_ITEM_POSITION + " INTEGER, " + OFISDBContract.AnswerEntry.COLUMN_NAME_SUBMODULE_ID + " INTEGER, "
            + OFISDBContract.AnswerEntry.COLUMN_NAME_FARMER_MODULE_ID + " INTEGER, " + OFISDBContract.AnswerEntry.COLUMN_NAME_FMP_ID + " INTEGER, "
            + OFISDBContract.AnswerEntry.COLUMN_NAME_FARM_ID + " INTEGER, " + OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_INT + " INTEGER, "
            + OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_BOOL + " INTEGER, " + OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_DECIMAL + " NUMERIC, "
            + OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_DATE + " TEXT, " + OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_TEXT + " TEXT, "
            + OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_REPORTING_YEAR + " TEXT )";


    private static final String OFIS_DATATYPESSIP_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.DatatypesSipEntry.TABLE_NAME
            + " ( " + OFISDBContract.DatatypesSipEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY, " + OFISDBContract.DatatypesSipEntry.COLUMN_NAME_DATATYPE_NAME + " TEXT )";

    private static final String OFIS_LOOKUPVALUESSIP_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.LookupValuesSipEntry.TABLE_NAME
            + " ( " + OFISDBContract.LookupValuesSipEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY, " + OFISDBContract.LookupValuesSipEntry.COLUMN_NAME_QUESTION_ID + " INTEGER, "
            + OFISDBContract.LookupValuesSipEntry.COLUMN_NAME_POSITION + " INTEGER, " + OFISDBContract.LookupValuesSipEntry.COLUMN_NAME_VALUE + " TEXT, "
            + OFISDBContract.LookupValuesSipEntry.COLUMN_NAME_VALUE_FR + " TEXT, " + OFISDBContract.LookupValuesSipEntry.COLUMN_NAME_VALUE_ES + " TEXT, "
            + OFISDBContract.LookupValuesSipEntry.COLUMN_NAME_VALUE_ID + " TEXT, " + OFISDBContract.LookupValuesSipEntry.COLUMN_NAME_VALUE_PT + " TEXT, "
            + OFISDBContract.LookupValuesSipEntry.COLUMN_NAME_VALUE_TR + " TEXT, " + OFISDBContract.LookupValuesSipEntry.COLUMN_NAME_VALUE_LO + " TEXT, "
            + OFISDBContract.LookupValuesSipEntry.COLUMN_NAME_VALUE_VI + " TEXT, " + OFISDBContract.LookupValuesSipEntry.COLUMN_NAME_VALUE_TH + " TEXT, "
            + OFISDBContract.LookupValuesSipEntry.COLUMN_NAME_PHOTO + " TEXT )";


    private static final String OFIS_MODULEASSIGNMENTS_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.ModuleAssignmentsEntry.TABLE_NAME
            + " ( " + OFISDBContract.ModuleAssignmentsEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY, " + OFISDBContract.ModuleAssignmentsEntry.COLUMN_NAME_MODULE_ID + " INTEGER, "
            + OFISDBContract.ModuleAssignmentsEntry.COLUMN_NAME_MODULEASSIGNMENT_ID + " INTEGER, " + OFISDBContract.ModuleAssignmentsEntry.COLUMN_NAME_MODULEASSIGNMENT_TYPE + " TEXT, "
            + OFISDBContract.ModuleAssignmentsEntry.COLUMN_NAME_IS_CHILD_LABOUR + " TEXT, " + OFISDBContract.ModuleAssignmentsEntry.COLUMN_NAME_SURVEY_CODE + " TEXT, "
            + OFISDBContract.ModuleAssignmentsEntry.COLUMN_NAME_IS_FARM_SURVEY + " TEXT, " + OFISDBContract.ModuleAssignmentsEntry.COLUMN_NAME_ROLE_ID + " TEXT, "
            + OFISDBContract.ModuleAssignmentsEntry.COLUMN_NAME_RECURRENCE_FREQUENCY + " INTEGER, " + OFISDBContract.ModuleAssignmentsEntry.COLUMN_NAME_RECURRENCE_TYPE + " TEXT, " +
            OFISDBContract.ModuleAssignmentsEntry.COLUMN_NAME_START_DATE + " TEXT, " +
            OFISDBContract.ModuleAssignmentsEntry.COLUMN_NAME_VIEW_MODULE + " INTEGER, " +
            OFISDBContract.ModuleAssignmentsEntry.COLUMN_NAME_IS_PLOT_SURVEY + " TEXT, "
            + OFISDBContract.ModuleAssignmentsEntry.COLUMN_NAME_COUNTRY_ID + " INTEGER, "
            + OFISDBContract.ModuleAssignmentsEntry.COLUMN_NAME_FARMER_ROLE_ID + " TEXT, "
            + OFISDBContract.ModuleAssignmentsEntry.COLUMN_NAME_SURVEY_DURATION + " INTEGER, "
            + OFISDBContract.ModuleAssignmentsEntry.COLUMN_NAME_DISABLED_MONTHS + " TEXT)";

    private static final String OFIS_FARMERMODULES_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.FarmerModulesEntry.TABLE_NAME
            + " ( " + OFISDBContract.FarmerModulesEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + OFISDBContract.FarmerModulesEntry.COLUMN_NAME_MODULE_ID + " INTEGER, "
            + OFISDBContract.FarmerModulesEntry.COLUMN_NAME_FARMER_ID + " INTEGER, " + OFISDBContract.FarmerModulesEntry.COLUMN_NAME_OLAM_FARMER_ID + " TEXT, "
            + OFISDBContract.FarmerModulesEntry.COLUMN_NAME_FARM_ID + " INTEGER, " + OFISDBContract.FarmerModulesEntry.COLUMN_NAME_CURRENT_SUBMODULE_ID + " INTEGER, "
            + OFISDBContract.FarmerModulesEntry.COLUMN_NAME_STATUS_STARTED + " INTEGER, " + OFISDBContract.FarmerModulesEntry.COLUMN_NAME_STATUS_FINISHED + " INTEGER, "
            + OFISDBContract.FarmerModulesEntry.COLUMN_NAME_RECORDED_LAT + " TEXT, " + OFISDBContract.FarmerModulesEntry.COLUMN_NAME_RECORDED_LNG + " TEXT, "
            + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_PLOT_ID + " INTEGER, "
            + OFISDBContract.FarmerModulesEntry.COLUMN_NAME_STARTED_DATE + " TEXT, "
            + OFISDBContract.FarmerModulesEntry.COLUMN_NAME_STAKEHOLDER_ID + " TEXT )";

    private static final String OFIS_FARMERCHILD_MODULES_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.FarmerChildModulesEntry.TABLE_NAME
            + " ( " + OFISDBContract.FarmerChildModulesEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + OFISDBContract.FarmerChildModulesEntry.COLUMN_NAME_MODULE_ID + " INTEGER, "
            + OFISDBContract.FarmerChildModulesEntry.COLUMN_NAME_FARMER_ID + " INTEGER, "
            + OFISDBContract.FarmerChildModulesEntry.COLUMN_NAME_OLAM_FARMER_ID + " TEXT, "
            + OFISDBContract.FarmerChildModulesEntry.COLUMN_NAME_CHILD_ID + " TEXT, "
            + OFISDBContract.FarmerChildModulesEntry.COLUMN_NAME_FARM_ID + " INTEGER, "
            + OFISDBContract.FarmerChildModulesEntry.COLUMN_NAME_CURRENT_SUBMODULE_ID + " INTEGER, "
            + OFISDBContract.FarmerChildModulesEntry.COLUMN_NAME_STATUS_STARTED + " INTEGER, "
            + OFISDBContract.FarmerChildModulesEntry.COLUMN_NAME_STATUS_FINISHED + " INTEGER, "
            + OFISDBContract.FarmerChildModulesEntry.COLUMN_NAME_RECORDED_LAT + " TEXT, " + OFISDBContract.FarmerChildModulesEntry.COLUMN_NAME_RECORDED_LNG + " TEXT, "
            + OFISDBContract.FarmerChildModulesEntry.COLUMN_NAME_STARTED_DATE + " TEXT )";


    private static final String OFIS_PLOT_GEO_DATA_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.PlotGeoData.TABLE_NAME
            + " ( " + OFISDBContract.PlotGeoData.COLUMN_NAME_PLOT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + OFISDBContract.PlotGeoData.COLUMN_NAME_FARMER_ID + " TEXT, "
            + OFISDBContract.PlotGeoData.COLUMN_NAME_PLOT_NAME + " TEXT, "
            + OFISDBContract.PlotGeoData.COLUMN_NAME_ENTEREDPLOT_NAME + " TEXT, "
            + OFISDBContract.PlotGeoData.COLUMN_NAME_FARM_ID + " TEXT, "
            + OFISDBContract.PlotGeoData.COLUMN_NAME_SERVER_ID + " TEXT, "
            + OFISDBContract.PlotGeoData.COLUMN_NAME_PLOT_JSON + " TEXT, "
            + OFISDBContract.PlotGeoData.COLUMN_NAME_PLOT_SYNC_FLAG + " INTEGER, "
            + OFISDBContract.PlotGeoData.COLUMN_NAME_CENTER_PLOT + " TEXT default null, "
            + OFISDBContract.PlotEntry.COLUMN_NAME_PLOT_SIZE + " REAL )";

    private static final String OFIS_DATATYPES_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.DatatypesEntry.TABLE_NAME
            + " ( " + OFISDBContract.DatatypesEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY, " + OFISDBContract.DatatypesEntry.COLUMN_NAME_DATATYPE_NAME + " TEXT )";

    private static final String OFIS_LOOKUPVALUES_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.LookupValuesEntry.TABLE_NAME
            + " ( " + OFISDBContract.LookupValuesEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY, " + OFISDBContract.LookupValuesEntry.COLUMN_NAME_QUESTION_ID + " INTEGER, "
            + OFISDBContract.LookupValuesEntry.COLUMN_NAME_POSITION + " INTEGER, " + OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE + " TEXT, "
            + OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE_FR + " TEXT, " + OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE_ES + " TEXT, "
            + OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE_ID + " TEXT, " + OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE_PT + " TEXT, "
            + OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE_TR + " TEXT, " + OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE_LO + " TEXT, "
            + OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE_VI + " TEXT, " + OFISDBContract.LookupValuesEntry.COLUMN_NAME_VALUE_TH + " TEXT, "
            + OFISDBContract.LookupValuesEntry.COLUMN_NAME_COUNTRYLOOKUPVALUE + " TEXT, " + OFISDBContract.LookupValuesEntry.COLUMN_NAME_PHOTO + " TEXT )";

    private static final String OFIS_QUESTION_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.QuestionEntry.TABLE_NAME +
            " ( " + OFISDBContract.QuestionEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY, " + OFISDBContract.QuestionEntry.COLUMN_NAME_SUBMODULE_ID + " INTEGER, "
            + OFISDBContract.QuestionEntry.COLUMN_NAME_DATATYPE_ID + " INTEGER, " + OFISDBContract.QuestionEntry.COLUMN_NAME_POSITION + " INTEGER, "
            + OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_TYPE + " TEXT, " + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION + " TEXT, "
            + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_FR + " TEXT, " + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_ES + " TEXT, "
            + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_ID + " TEXT, " + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_PT + " TEXT, "
            + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_TR + " TEXT, " + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_LO + " TEXT, "
            + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_VI + " TEXT, " + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_TH + " TEXT, "
            + OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_CONFIGURATION + " TEXT, " + OFISDBContract.QuestionEntry.COLUMN_NAME_STOREGE_MODEL + " TEXT, "
            + OFISDBContract.QuestionEntry.COLUMN_NAME_STOREGE_COLUM + " TEXT, " + OFISDBContract.QuestionEntry.COLUMN_NAME_Active + " TEXT, "
            + OFISDBContract.QuestionEntry.COLUMN_NAME_CREATED_AT + " TEXT, " + OFISDBContract.QuestionEntry.COLUMN_NAME_UPDATED_AT + " TEXT, "
            + OFISDBContract.QuestionEntry.COLUMN_NAME_APP_ID + " TEXT, " + OFISDBContract.QuestionEntry.COLUMN_NAME_IS_SUBSET_QUESTION_REQUIRED + " TEXT, "
            + OFISDBContract.QuestionEntry.COLUMN_NAME_CONDITIONAL_QUESTIONS + " TEXT, " + OFISDBContract.QuestionEntry.COLUMN_NAME_SIPGROUPID + " TEXT, "
            + OFISDBContract.QuestionEntry.COLUMN_NAME_SIPKEY + " TEXT, "
            + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_INFO + " TEXT, "
            + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_INFO_ES + " TEXT, "
            + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_INFO_FR + " TEXT, "
            + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_INFO_ID + " TEXT, "
            + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_INFO_PT + " TEXT, "
            + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_INFO_TR + " TEXT, "
            + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_INFO_LO + " TEXT, "
            + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_INFO_VI + " TEXT, "
            + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_INFO_TH + " TEXT, "
            + OFISDBContract.QuestionEntry.COLUMN_NAME_DISPLAY_TYPE_ID + " TEXT, "
            + OFISDBContract.QuestionEntry.COLUMN_NAME_ANSWER_TYPE + " TEXT, "
            + OFISDBContract.QuestionEntry.COLUMN_NAME_OTHER_CHOICE + " TEXT, "
            + OFISDBContract.QuestionEntry.COLUMN_NAME_METRIC + " TEXT, "
            + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_ASSIGNMENT_TYPE + " TEXT, "
            + OFISDBContract.QuestionEntry.COLUMN_NAME_QUESTION_ASSIGNMENT_FIELD + " TEXT, "
            + OFISDBContract.QuestionEntry.COLUMN_NAME_CALCULATION_LOGIC + " TEXT )";


    private static final String OFIS_ANSWER_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.AnswerEntry.TABLE_NAME +
            " ( " + OFISDBContract.AnswerEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + OFISDBContract.AnswerEntry.COLUMN_NAME_QUESTION_ID + " INTEGER, "
            + OFISDBContract.AnswerEntry.COLUMN_NAME_PARENT_QUESTION_ID + " INTEGER, " + OFISDBContract.AnswerEntry.COLUMN_NAME_REPEATER_ITEM_POSITION + " INTEGER, "
            + OFISDBContract.AnswerEntry.COLUMN_NAME_SUBMODULE_ID + " INTEGER, " + OFISDBContract.AnswerEntry.COLUMN_NAME_FARMER_MODULE_ID + " INTEGER, "
            + OFISDBContract.AnswerEntry.COLUMN_NAME_FMP_ID + " INTEGER, " + OFISDBContract.AnswerEntry.COLUMN_NAME_FARMER_ID + " INTEGER, "
            + OFISDBContract.AnswerEntry.COLUMN_NAME_OLAM_FARMER_ID + " TEXT, " + OFISDBContract.AnswerEntry.COLUMN_NAME_FARM_ID + " INTEGER, "
            + OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_INT + " INTEGER, " + OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_BOOL + " INTEGER, "
            + OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_DECIMAL + " NUMERIC, " + OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_DATE + " TEXT, "
            + OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_TEXT + " TEXT, " + OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_REPORTING_YEAR + " TEXT, "
            + OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_METRIC_GLOBAL_ID + " TEXT, "
            + OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_METRIC_LOCAL_ID + " TEXT, "
            + OFISDBContract.AnswerEntry.COLUMN_NAME_ANSWER_METRIC + " TEXT, "
            + OFISDBContract.AnswerEntry.COLUMN_NAME_STAKEHOLDER_ID + " TEXT )";

    private static final String OFIS_SAVED_ANSWER_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.SavedAnswerEntry.TABLE_NAME +
            " ( " + OFISDBContract.SavedAnswerEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + OFISDBContract.SavedAnswerEntry.COLUMN_NAME_QUESTION_ID + " INTEGER, "
            + OFISDBContract.SavedAnswerEntry.COLUMN_NAME_PARENT_QUESTION_ID + " INTEGER, " + OFISDBContract.SavedAnswerEntry.COLUMN_NAME_REPEATER_ITEM_POSITION + " INTEGER, "
            + OFISDBContract.SavedAnswerEntry.COLUMN_NAME_SUBMODULE_ID + " INTEGER, " + OFISDBContract.SavedAnswerEntry.COLUMN_NAME_FARMER_MODULE_ID + " INTEGER, "
            + OFISDBContract.SavedAnswerEntry.COLUMN_NAME_FMP_ID + " INTEGER, " + OFISDBContract.SavedAnswerEntry.COLUMN_NAME_FARMER_ID + " INTEGER, "
            + OFISDBContract.SavedAnswerEntry.COLUMN_NAME_OLAM_FARMER_ID + " TEXT, " + OFISDBContract.SavedAnswerEntry.COLUMN_NAME_FARM_ID + " INTEGER, "
            + OFISDBContract.SavedAnswerEntry.COLUMN_NAME_ANSWER_INT + " INTEGER, " + OFISDBContract.SavedAnswerEntry.COLUMN_NAME_ANSWER_BOOL + " INTEGER, "
            + OFISDBContract.SavedAnswerEntry.COLUMN_NAME_ANSWER_DECIMAL + " NUMERIC, " + OFISDBContract.SavedAnswerEntry.COLUMN_NAME_ANSWER_DATE + " TEXT, "
            + OFISDBContract.SavedAnswerEntry.COLUMN_NAME_ANSWER_TEXT + " TEXT, "
            + OFISDBContract.SavedAnswerEntry.COLUMN_NAME_ANSWER_REPORTING_YEAR + " TEXT, "
            + OFISDBContract.SavedAnswerEntry.COLUMN_NAME_STAKEHOLDER_ID + " TEXT )";

    private static final String OFIS_ANSWER_CHILD_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.AnswerChildEntry.TABLE_NAME +
            " ( " + OFISDBContract.AnswerChildEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + OFISDBContract.AnswerChildEntry.COLUMN_NAME_QUESTION_ID + " INTEGER, "
            + OFISDBContract.AnswerChildEntry.COLUMN_NAME_PARENT_QUESTION_ID + " INTEGER, " + OFISDBContract.AnswerChildEntry.COLUMN_NAME_REPEATER_ITEM_POSITION + " INTEGER, "
            + OFISDBContract.AnswerChildEntry.COLUMN_NAME_SUBMODULE_ID + " INTEGER, " + OFISDBContract.AnswerChildEntry.COLUMN_NAME_FARMER_MODULE_ID + " INTEGER, "
            + OFISDBContract.AnswerChildEntry.COLUMN_NAME_FMP_ID + " INTEGER, " + OFISDBContract.AnswerChildEntry.COLUMN_NAME_FARMER_ID + " INTEGER, "
            + OFISDBContract.AnswerChildEntry.COLUMN_NAME_OLAM_FARMER_ID + " TEXT, " + OFISDBContract.AnswerChildEntry.COLUMN_NAME_FARM_ID + " INTEGER, "
            + OFISDBContract.AnswerChildEntry.COLUMN_NAME_ANSWER_INT + " INTEGER, " + OFISDBContract.AnswerChildEntry.COLUMN_NAME_ANSWER_BOOL + " INTEGER, "
            + OFISDBContract.AnswerChildEntry.COLUMN_NAME_ANSWER_DECIMAL + " NUMERIC, " + OFISDBContract.AnswerChildEntry.COLUMN_NAME_ANSWER_DATE + " TEXT, "
            + OFISDBContract.AnswerChildEntry.COLUMN_NAME_ANSWER_TEXT + " TEXT, " + OFISDBContract.AnswerChildEntry.COLUMN_NAME_ANSWER_REPORTING_YEAR + " TEXT )";


    private static final String OFIS_FARMER_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.FarmerEntry.TABLE_NAME +
            " ( " + OFISDBContract.FarmerEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + OFISDBContract.FarmerEntry.COLUMN_NAME_FARM_ID + " INTEGER, "
            + OFISDBContract.FarmerEntry.COLUMN_NAME_PLACE_ID + " INTEGER, " + OFISDBContract.FarmerEntry.COLUMN_NAME_SECTION_ID + " INTEGER, "
            + OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME + " TEXT, " + OFISDBContract.FarmerEntry.COLUMN_NAME_GENDER + " TEXT, "
            + OFISDBContract.FarmerEntry.COLUMN_NAME_COUNTRY_ID + " TEXT, "
            + OFISDBContract.FarmerEntry.COLUMN_NAME_REGIONID + " TEXT, "
            + OFISDBContract.FarmerEntry.COLUMN_NAME_FARMERGROUPID + " TEXT, "
            + OFISDBContract.FarmerEntry.COLUMN_NAME_DISTRICTID + " TEXT, "
            + OFISDBContract.FarmerEntry.COLUMN_NAME_SUBDISTRICTID + " TEXT, "
            + OFISDBContract.FarmerEntry.COLUMN_NAME_SUBDISID + " TEXT, "
            + OFISDBContract.FarmerEntry.COLUMN_NAME_ISCONSENTAVAILABLE + " TEXT, "
            + OFISDBContract.FarmerEntry.COLUMN_NAME_PRODUCT_ID + " TEXT, "
            + OFISDBContract.FarmerEntry.COLUMN_NAME_PARTNER_ID + " TEXT, "
            + OFISDBContract.FarmerEntry.COLUMN_NAME_PROGRAMME_ID + " TEXT, "
            + OFISDBContract.FarmerEntry.COLUMN_NAME_ODFARMERID + " TEXT, "
            + OFISDBContract.FarmerEntry.COLUMN_NAME_DOB + " TEXT, " + OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID + " TEXT, "
            + OFISDBContract.FarmerEntry.COLUMN_NAME_PHOTO + " TEXT, " + OFISDBContract.FarmerEntry.COLUMN_NAME_SIGNATURE + " TEXT, "
            + OFISDBContract.FarmerEntry.COLUMN_NAME_SYNCED + " INTEGER DEFAULT 1, " + OFISDBContract.FarmerEntry.COLUMN_NAME_CREATED_LOCALLY + " INTEGER, "
            + OFISDBContract.FarmerEntry.COLUMN_NAME_WEBSITE_FARMER_ID + " INTEGER, " + OFISDBContract.FarmerEntry.COLUMN_NAME_WEBSITE_SUBMITTED_MODULES + " TEXT, "
            + OFISDBContract.FarmerEntry.COLUMN_NAME_FARM_PRIMARY_LAT + " TEXT, " + OFISDBContract.FarmerEntry.COLUMN_NAME_FARM_PRIMARY_LNG + " TEXT, "
            + OFISDBContract.FarmerEntry.COLUMN_NAME_FARM_HOUSEHOLD_LAT + " TEXT, " + OFISDBContract.FarmerEntry.COLUMN_NAME_FARM_HOUSEHOLD_LNG + " TEXT, "
            + OFISDBContract.FarmerEntry.COLUMN_NAME_FARM_HOUSEHOLD_ID + " TEXT, "
            + OFISDBContract.FarmerEntry.COLUMN_NAME_FARM_PLOTS + " TEXT, "
            + OFISDBContract.FarmerEntry.COLUMN_NAME_REGISTER_DATE + " TEXT, "
            + OFISDBContract.FarmerEntry.COLUMN_NAME_MODERATION_STATUS + " TEXT, "
            + OFISDBContract.FarmerEntry.COLUMN_NAME_FARMERLEAD + " TEXT, "
            + OFISDBContract.FarmerEntry.COLUMN_NAME_FARMERLEADNAME + " TEXT, "
            + OFISDBContract.FarmerEntry.COLUMN_NAME_PLOT + " TEXT, "
            + OFISDBContract.FarmerEntry.COLUMN_NAME_CONSENT_FARMER + " INTEGER DEFAULT 1, " + OFISDBContract.FarmerEntry.COLUMN_NAME_CONSENT_OTHERS + " INTEGER DEFAULT 1, "
            + OFISDBContract.FarmerEntry.COLUMN_NAME_CERTIFICATION_ID + " TEXT, "
            + OFISDBContract.FarmerEntry.COLUMN_NAME_CERTIFICATION_TEXT + " TEXT,"
            + OFISDBContract.FarmerEntry.COLUMN_NAME_AGE + " TEXT,"
            + OFISDBContract.FarmerEntry.COLUMN_NAME_COMPLAINCE_FLAG + " INTEGER DEFAULT 0,"
            + OFISDBContract.FarmerEntry.COLUMN_NAME_CONDUCT_SURVEY_FOR_NCF + " INTEGER DEFAULT 0,"
            + OFISDBContract.FarmerEntry.COLUMN_NAME_CONDUCT_TRAINING_FOR_NCF + " INTEGER DEFAULT 0,"
            + OFISDBContract.FarmerEntry.COLUMN_NAME_POLYGON_CAPTURE_FOR_NCF + " TEXT DEFAULT 0,"
            + OFISDBContract.FarmerEntry.COLUMN_NAME_FARM_CODE + " TEXT,"
            + OFISDBContract.FarmerEntry.COLUMN_NAME_FARM_NAME + " TEXT, "
            + OFISDBContract.FarmerEntry.COLUMN_NAME_DUPLICATE_FARMER_CODE + " INTEGER DEFAULT 0,"
            + OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_CODE + " TEXT default null, "
            + OFISDBContract.FarmerEntry.COLUMN_NAME_PLOT_AVAILABLE + " TEXT, "
            + OFISDBContract.FarmerEntry.COLUMN_NAME_IS_NO_NATIONAL_ID + " TEXT, "
            + OFISDBContract.FarmerEntry.COLUMN_NAME_IS_NO_PHONE_NUMBER + " TEXT )";

    private static final String OFIS_BACKUP_FARMER_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.FarmerEntry.BACKUP_TABLE_NAME +
            " ( " + OFISDBContract.FarmerEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + OFISDBContract.FarmerEntry.COLUMN_NAME_FARM_ID + " INTEGER, "
            + OFISDBContract.FarmerEntry.COLUMN_NAME_PLACE_ID + " INTEGER, " + OFISDBContract.FarmerEntry.COLUMN_NAME_SECTION_ID + " INTEGER, "
            + OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_NAME + " TEXT, " + OFISDBContract.FarmerEntry.COLUMN_NAME_GENDER + " TEXT, "
            + OFISDBContract.FarmerEntry.COLUMN_NAME_COUNTRY_ID + " TEXT, "
            + OFISDBContract.FarmerEntry.COLUMN_NAME_REGIONID + " TEXT, "
            + OFISDBContract.FarmerEntry.COLUMN_NAME_FARMERGROUPID + " TEXT, "
            + OFISDBContract.FarmerEntry.COLUMN_NAME_ISCONSENTAVAILABLE + " TEXT, "
            + OFISDBContract.FarmerEntry.COLUMN_NAME_DISTRICTID + " TEXT, "
            + OFISDBContract.FarmerEntry.COLUMN_NAME_SUBDISTRICTID + " TEXT, "
            + OFISDBContract.FarmerEntry.COLUMN_NAME_SUBDISID + " TEXT, "
            + OFISDBContract.FarmerEntry.COLUMN_NAME_ODFARMERID + " TEXT, "
            + OFISDBContract.FarmerEntry.COLUMN_NAME_DOB + " TEXT, " + OFISDBContract.FarmerEntry.COLUMN_NAME_OLAM_FARMER_ID + " TEXT, "
            + OFISDBContract.FarmerEntry.COLUMN_NAME_PHOTO + " TEXT, " + OFISDBContract.FarmerEntry.COLUMN_NAME_SIGNATURE + " TEXT, "
            + OFISDBContract.FarmerEntry.COLUMN_NAME_SYNCED + " INTEGER DEFAULT 1, " + OFISDBContract.FarmerEntry.COLUMN_NAME_CREATED_LOCALLY + " INTEGER, "
            + OFISDBContract.FarmerEntry.COLUMN_NAME_WEBSITE_FARMER_ID + " INTEGER, " + OFISDBContract.FarmerEntry.COLUMN_NAME_WEBSITE_SUBMITTED_MODULES + " TEXT, "
            + OFISDBContract.FarmerEntry.COLUMN_NAME_FARM_PRIMARY_LAT + " TEXT, " + OFISDBContract.FarmerEntry.COLUMN_NAME_FARM_PRIMARY_LNG + " TEXT, "
            + OFISDBContract.FarmerEntry.COLUMN_NAME_FARM_PLOTS + " TEXT, "
            + OFISDBContract.FarmerEntry.COLUMN_NAME_REGISTER_DATE + " TEXT, "
            + OFISDBContract.FarmerEntry.COLUMN_NAME_MODERATION_STATUS + " TEXT, "
            + OFISDBContract.FarmerEntry.COLUMN_NAME_FARMERLEAD + " TEXT, "
            + OFISDBContract.FarmerEntry.COLUMN_NAME_FARMERLEADNAME + " TEXT, "
            + OFISDBContract.FarmerEntry.COLUMN_NAME_CONSENT_FARMER + " INTEGER DEFAULT 1, " + OFISDBContract.FarmerEntry.COLUMN_NAME_CONSENT_OTHERS + " INTEGER DEFAULT 1,"
            + OFISDBContract.FarmerEntry.COLUMN_NAME_CERTIFICATION_ID + " TEXT, "
            + OFISDBContract.FarmerEntry.COLUMN_NAME_CERTIFICATION_TEXT + " TEXT,"
            + OFISDBContract.FarmerEntry.COLUMN_NAME_AGE + " TEXT,"
            + OFISDBContract.FarmerEntry.COLUMN_NAME_PRODUCT_ID + " TEXT,"
            + OFISDBContract.FarmerEntry.COLUMN_NAME_PROGRAMME_ID + " TEXT,"
            + OFISDBContract.FarmerEntry.COLUMN_NAME_COMPLAINCE_FLAG + " INTEGER DEFAULT 0,"
            + OFISDBContract.FarmerEntry.COLUMN_NAME_CONDUCT_SURVEY_FOR_NCF + " INTEGER DEFAULT 0,"
            + OFISDBContract.FarmerEntry.COLUMN_NAME_CONDUCT_TRAINING_FOR_NCF + " INTEGER DEFAULT 0,"
            + OFISDBContract.FarmerEntry.COLUMN_NAME_POLYGON_CAPTURE_FOR_NCF + " TEXT DEFAULT 0,"
            + OFISDBContract.FarmerEntry.COLUMN_NAME_FARM_CODE + " TEXT,"
            + OFISDBContract.FarmerEntry.COLUMN_NAME_FARM_NAME + " TEXT, "
            + OFISDBContract.FarmerEntry.COLUMN_NAME_DUPLICATE_FARMER_CODE + " INTEGER DEFAULT 0,"
            + OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_CODE + " TEXT default null, "
            + OFISDBContract.FarmerEntry.COLUMN_NAME_PLOT_AVAILABLE + " TEXT, "
            + OFISDBContract.FarmerEntry.COLUMN_NAME_IS_NO_NATIONAL_ID + " TEXT, "
            + OFISDBContract.FarmerEntry.COLUMN_NAME_IS_NO_PHONE_NUMBER + " TEXT )";

    private static final String OFIS_FARMER_INDEX_CREATE = "CREATE INDEX IF NOT EXISTS idx_farmer_locally_created ON " + OFISDBContract.FarmerEntry.TABLE_NAME + "(" + OFISDBContract.FarmerEntry.COLUMN_NAME_CREATED_LOCALLY + ")";

    private static final String OFIS_PHONENUMBERS_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.PhoneNumber.TABLE_NAME
            + " ( " + OFISDBContract.PhoneNumber.COLUMN_NAME_PHONE_NUMBER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + OFISDBContract.PhoneNumber.COLUMN_NAME_ID + " INTEGER , " + OFISDBContract.PhoneNumber.COLUMN_NAME_FARMER_ID + " INTEGER, " + OFISDBContract.PhoneNumber.COLUMN_NAME_MULTI_STAKEHOLDER_ID + " INTEGER default null, "
            + OFISDBContract.PhoneNumber.COLUMN_NAME_COUNTRY_ID + " INTEGER, " + OFISDBContract.PhoneNumber.COLUMN_NAME_NUMBER + " TEXT, "
            + OFISDBContract.PhoneNumber.COLUMN_NAME_PRIMARY + " INTEGER DEFAULT 0,"
            + OFISDBContract.PhoneNumber.COLUMN_NAME_IS_DELETED + " INTEGER DEFAULT 0,"
            + OFISDBContract.PhoneNumber.COLUMN_NAME_IS_UPDATED + " INTEGER DEFAULT 0,"
            + OFISDBContract.PhoneNumber.COLUMN_NAME_IS_DUPLICATE + " TEXT )";

    private static final String OFIS_FAMRERNATIONALIDS_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.FarmerNationalIDs.TABLE_NAME
            + " ( " + OFISDBContract.FarmerNationalIDs.COLUMN_NATIONAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + OFISDBContract.FarmerNationalIDs.COLUMN_NAME_ID + " INTEGER, "
            + OFISDBContract.FarmerNationalIDs.COLUMN_NAME_FARMER_ID + " INTEGER, " + OFISDBContract.FarmerNationalIDs.COLUMN_NAME_STAKEHOLDER_ID + " INTEGER, "
            + OFISDBContract.FarmerNationalIDs.COLUMN_NID_TYPE + " INTEGER, " + OFISDBContract.FarmerNationalIDs.COLUMN_NID_NUMBER + " TEXT, "
            + OFISDBContract.FarmerNationalIDs.COLUMN_IS_SYNCED + " TEXT, "
            + OFISDBContract.FarmerNationalIDs.COLUMN_NID_EXPIRY_DATE + " TEXT, "
            + OFISDBContract.FarmerNationalIDs.COLUMN_NID_PHOTO + " TEXT, "
            + OFISDBContract.FarmerNationalIDs.COLUMN_NAME_IS_DELETED + " TEXT, "
            + OFISDBContract.FarmerNationalIDs.COLUMN_NAME_IS_UPDATED + " TEXT, "
            + OFISDBContract.FarmerNationalIDs.COLUMN_NAME_IS_DUPLICATE + " TEXT )";

    private static final String OFIS_LOCALFAMRERNATIONALIDS_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.LocalFarmerNationalIDs.TABLE_NAME
            + " ( " + OFISDBContract.LocalFarmerNationalIDs.COLUMN_NAME_ID + " INTEGER PRIMARY KEY, " + OFISDBContract.LocalFarmerNationalIDs.COLUMN_NAME_FARMER_ID + " INTEGER, "
            + OFISDBContract.LocalFarmerNationalIDs.COLUMN_NID_TYPE + " INTEGER, " + OFISDBContract.LocalFarmerNationalIDs.COLUMN_NID_NUMBER + " TEXT, "
            + OFISDBContract.LocalFarmerNationalIDs.COLUMN_NID_EXPIRY_DATE + " TEXT, "
            + OFISDBContract.LocalFarmerNationalIDs.COLUMN_BACKEND_DB_ID + " TEXT, "
            + OFISDBContract.LocalFarmerNationalIDs.COLUMN_IS_DELETED + " TEXT, "
            + OFISDBContract.LocalFarmerNationalIDs.COLUMN_IS_UPDATED + " TEXT, "
            + OFISDBContract.LocalFarmerNationalIDs.COLUMN_NID_PHOTO + " TEXT )";

    private static final String OFIS_LOCALNATIONALIDIMAGES_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.LocalNationalIDImages.TABLE_NAME
            + " ( " + OFISDBContract.LocalNationalIDImages.COLUMN_NID_TYPE + " INTEGER, "
            + OFISDBContract.LocalNationalIDImages.COLUMN_NID_PHOTO + " TEXT )";

    private static final String OFIS_FARMERLEADS_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.FarmerLeadDetails.TABLE_NAME
            + " ( " + OFISDBContract.FarmerLeadDetails.COLUMN_FL_ID + " INTEGER, " + OFISDBContract.FarmerLeadDetails.COLUMN_COUNTRY_ISOCODE + " TEXT, "
            + OFISDBContract.FarmerLeadDetails.COLUMN_FL_DISPLAYID + " TEXT, " + OFISDBContract.FarmerLeadDetails.COLUMN_FL_FULLNAME + " TEXT, " + OFISDBContract.FarmerLeadDetails.COLUMN_COUNTRY_ID + " INTEGER, "
            + OFISDBContract.FarmerLeadDetails.COLUMN_FL_NUMBER + " TEXT )";

    private static final String OFIS_FARMERASSIGNMENTS_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.FarmerAssignments.TABLE_NAME
            + " ( " + OFISDBContract.FarmerAssignments.COLUMN_PRODUCT_ID + " INTEGER, "
            + OFISDBContract.FarmerAssignments.COLUMN_COUNTRY_iD + " INTEGER, " + OFISDBContract.FarmerAssignments.COLUMN_ISODORIGIN + " TEXT, "
            + OFISDBContract.FarmerAssignments.COLUMN_COUNTRYISOCODE + " TEXT,"
            + OFISDBContract.FarmerAssignments.CALCULATE_COMPOSITE_FLAG + " TEXT )";

    private static final String OFIS_COUNTRIES_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.CountriesEntry.TABLE_NAME
            + " ( " + OFISDBContract.CountriesEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY, " + OFISDBContract.CountriesEntry.COLUMN_NAME_CODE + " TEXT, " + OFISDBContract.CountriesEntry.COLUMN_NAME_ISOCODE + " TEXT, "
            + OFISDBContract.CountriesEntry.COLUMN_NAME_COUNTRY_NAME + " TEXT, " + OFISDBContract.CountriesEntry.COLUMN_NAME_DIALLING_CODE + " TEXT, " + OFISDBContract.CountriesEntry.COLUMN_NAME_ISSUBDISTCAPTURE + " TEXT, "
            + OFISDBContract.CountriesEntry.COLUMN_NAME_OLAM_COUNTRY + " INTEGER, " + OFISDBContract.CountriesEntry.COLUMN_NAME_ENUMERATOR_COUNTRY + " INTEGER DEFAULT 0)";

    private static final String OFIS_All_COUNTRIES_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.countryLookUpValuesEntry.TABLE_NAME
            + " ( " + OFISDBContract.countryLookUpValuesEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY, " + OFISDBContract.countryLookUpValuesEntry.COLUMN_NAME_CODE + " TEXT, "
            + OFISDBContract.countryLookUpValuesEntry.COLUMN_NAME_COUNTRY_NAME + " TEXT, " + OFISDBContract.countryLookUpValuesEntry.COLUMN_NAME_DIALLING_CODE + " TEXT, "
            + OFISDBContract.countryLookUpValuesEntry.COLUMN_NAME_OLAM_COUNTRY + " INTEGER, " + OFISDBContract.countryLookUpValuesEntry.COLUMN_NAME_ENUMERATOR_COUNTRY + " INTEGER DEFAULT 0)";

    private static final String OFIS_REGIONS_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.RegionsEntry.TABLE_NAME
            + " ( " + OFISDBContract.RegionsEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY, " + OFISDBContract.RegionsEntry.COLUMN_NAME_COUNTRY_ID + " INTEGER, "
            + OFISDBContract.RegionsEntry.COLUMN_NAME_REGION_NAME + " TEXT )";

    private static final String OFIS_DISTRICTS_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.DistrictsEntry.TABLE_NAME
            + " ( " + OFISDBContract.DistrictsEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY, " + OFISDBContract.DistrictsEntry.COLUMN_NAME_REGION_ID + " INTEGER, "
            + OFISDBContract.DistrictsEntry.COLUMN_NAME_DISTRICT_NAME + " TEXT )";

    //    adding sub district
    private static final String OFIS_SUBDISTRICTS_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.SubDistrictsEntry.TABLE_NAME
            + " ( " + OFISDBContract.SubDistrictsEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY, " + OFISDBContract.SubDistrictsEntry.COLUMN_NAME_DISTRICT_ID + " INTEGER, "
            + OFISDBContract.SubDistrictsEntry.COLUMN_NAME_SUBDISTRICT_NAME + " TEXT )";

    private static final String OFIS_SIGNATURE_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.SignatureEntry.TABLE_NAME
            + " ( " + OFISDBContract.SignatureEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY, " + OFISDBContract.SignatureEntry.COLUMN_NAME_FAMRERID + " INTEGER, "
            + OFISDBContract.SignatureEntry.COLUMN_NAME_SIGNATURE + " TEXT )";


    private static final String OFIS_PLACES_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.PlacesEntry.TABLE_NAME
            + " ( " + OFISDBContract.PlacesEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + OFISDBContract.PlacesEntry.COLUMN_NAME_DISTRICT_ID + " INTEGER, " + OFISDBContract.PlacesEntry.COLUMN_NAME_SUBDISTRICT_ID + " INTEGER, "
            + OFISDBContract.PlacesEntry.COLUMN_NAME_PLACE_TYPE + " TEXT, " + OFISDBContract.PlacesEntry.COLUMN_NAME_PLACE_NAME + " TEXT )";

    private static final String OFIS_FARMERGROUPS_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.FarmerGroupsEntry.TABLE_NAME
            + " ( " + OFISDBContract.FarmerGroupsEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY, " + OFISDBContract.FarmerGroupsEntry.COLUMN_NAME_PROGRAMME_ID + " INTEGER, "
            + OFISDBContract.FarmerGroupsEntry.COLUMN_NAME_PARTNER_ID + " INTEGER, " + OFISDBContract.FarmerGroupsEntry.COLUMN_NAME_PRODUCT_ID + " INTEGER, "
            + OFISDBContract.FarmerGroupsEntry.COLUMN_NAME_CODE + " TEXT, " + OFISDBContract.FarmerGroupsEntry.COLUMN_NAME_FARMERGROUP_NAME + " TEXT )";

    private static final String OFIS_SECTIONS_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.SectionsEntry.TABLE_NAME
            + " ( " + OFISDBContract.SectionsEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY, " + OFISDBContract.SectionsEntry.COLUMN_NAME_FARMERGROUP_ID + " INTEGER, "
            + OFISDBContract.SectionsEntry.COLUMN_NAME_CODE + " TEXT, " + OFISDBContract.SectionsEntry.COLUMN_NAME_SECTION_NAME + " TEXT )";

    private static final String OFIS_PRODUCTS_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.ProductsEntry.TABLE_NAME
            + " ( " + OFISDBContract.ProductsEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY, " + OFISDBContract.ProductsEntry.COLUMN_NAME_PRODUCT_NAME + " TEXT )";

    private static final String OFIS_PLOT_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.PlotEntry.TABLE_NAME
            + " ( " + OFISDBContract.PlotEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY, " +
            OFISDBContract.PlotEntry.COLUMN_NAME_FARMER_ID + " INTEGER, " +
            OFISDBContract.PlotEntry.COLUMN_IS_SYNCED + " TEXT, " +
            OFISDBContract.PlotEntry.COLUMN_NAME_PLOTTED_NAME + " TEXT, " +
            OFISDBContract.PlotEntry.COLUMN_NAME_PLOT_NAME + " TEXT, " +
            OFISDBContract.PlotEntry.COLUMN_NAME_CREATED_AT + " TEXT ," +
            OFISDBContract.PlotEntry.COLUMN_NAME_CENTER_PLOT + " TEXT default null," +
            OFISDBContract.PlotEntry.COLUMN_NAME_PLOT_SIZE + " REAL ," +
            OFISDBContract.PlotEntry.COLUMN_NAME_IS_DEFORESTATION_PLOT + " TEXT DEFAULT 'false' ," +
            OFISDBContract.PlotEntry.COLUMN_NAME_IS_PA_LEGALITY_PLOT + " TEXT DEFAULT 'false' ," +
            OFISDBContract.PlotEntry.COLUMN_NAME_IS_CLEANSED_STATUS + " TEXT DEFAULT 'Open' ," +
            OFISDBContract.PlotEntry.COLUMN_NAME_GEO_DB_FLAG_STATUS + " TEXT ," +
            OFISDBContract.PlotEntry.COLUMN_NAME_IS_EUDR_SURVEY_COMPLETED + " TEXT DEFAULT 'false' ," +
            OFISDBContract.PlotEntry.COLUMN_NAME_IS_PA_LEGALITY_SURVEY_COMPLETED + " TEXT DEFAULT 'false' , " +
            OFISDBContract.PlotEntry.COLUMN_NAME_POLYGON_SIZE + " TEXT ," +
            OFISDBContract.PlotEntry.COLUMN_NAME_MOB_DEACTIVATE_FLAG + " TEXT DEFAULT 'false' ," +
            OFISDBContract.PlotEntry.COLUMN_NAME_MOB_DEACTIVATE_REASON + " TEXT ," +
            OFISDBContract.PlotEntry.COLUMN_NAME_POSITION_DATA + " TEXT ," +
            OFISDBContract.PlotEntry.COLUMN_NAME_IS_DE_ACTIVATION_FLAG_SYNCED + " TEXT, " +
            OFISDBContract.PlotEntry.COLUMN_NAME_IS_POLYGON_DOWNLOADED_DATA + " TEXT )";

    private static final String OFIS_NETWORKQUEUE_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.NetworkQueueEntry.TABLE_NAME +
            " ( " + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ID + " TEXT, "
            + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBMISSION_TYPE + " TEXT, "
            + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_TYPE_ID + " INTEGER, "
            + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_MODULE_ID + " INTEGER, "
            + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_TYPE + " TEXT, "
            + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_SUBGROUP_TYPE_ID + " INTEGER, "
            + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_SUBMODULE_ID + " INTEGER, "
            + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_SUBGROUP_TYPE + " TEXT, "
            + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_POI_NAME + " TEXT, "
            + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_ACCURACY + " TEXT, "
            + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_CHILD_ID + " TEXT, "
            + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_SURVEY_CODE + " TEXT, "
            + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_IS_VILLAGE + " TEXT, "
            + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SIGNATURE + " TEXT, "
            + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_ID + " INTEGER, "
            + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_STAKEHOLDER_ID + " INTEGER, "
            + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_OLAM_FARMER_ID + " TEXT, "
            + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARM_ID + " INTEGER, " + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_MODULE_ID + " INTEGER, "
            + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FMP_ID + " INTEGER, " + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_TRANSACTION_ITEM_ID + " INTEGER, "
            + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_TRANSACTEE_TYPE + " TEXT, " + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_TRAINING_RECORD_ID + " INTEGER, "
            + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LAT + " TEXT, " + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_LNG + " TEXT, "
            + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_RECORDED_PLOTS + " TEXT, "
            + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_COUNTRY_ID + " INTEGER, " + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_REGION_ID + " INTEGER, "
            + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_DISTRICT_ID + " INTEGER, " + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_PLOT_ID + " INTEGER, "
            + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SUBDISTRICT_ID + " INTEGER, " + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_PLACE_ID + " INTEGER, "
            + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_PRODUCT_ID + " INTEGER, " + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMERGROUP_ID + " INTEGER, "
            + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_CAPACITY + " INTEGER," + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_NUM_CLASSROOMS + " INTEGER, "
            + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_YEAR_OPENED + " INTEGER, " + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_GENDER_MIX + " TEXT, "
            + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SCHOOL_TYPE + " TEXT, " + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_CWS_TYPE + " TEXT, "
            + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_OLAM_FUNDED + " INTEGER, " + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SERVERITEM_ID + " INTEGER DEFAULT 0, "
            + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FULLY_SYNCED + " INTEGER DEFAULT 0, " + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPLOAD_ATTEMPTS + " INTEGER DEFAULT 0, "
            + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_CREATED_AT + " TEXT, " + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_UPDATED_AT + " TEXT, "
            + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_FARMER_PHOTO + " TEXT, "
            + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_CERTIFICATION_ID + " TEXT, "
            + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_CERTIFICATION_TEXT + " TEXT, " + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_PLOT_NAME + " TEXT, " + OFISDBContract.NetworkQueueEntry.COLUMN_NAME_SIP_PLOT + " TEXT)";

    private static final String OFIS_FMP_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.FarmManagementPlanEntry.TABLE_NAME +
            " ( " + OFISDBContract.FarmManagementPlanEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY, " + OFISDBContract.FarmManagementPlanEntry.COLUMN_NAME_FARMER_ID + " INTEGER, "
            + OFISDBContract.FarmManagementPlanEntry.COLUMN_NAME_FARM_ID + " INTEGER, " + OFISDBContract.FarmManagementPlanEntry.COLUMN_NAME_STARTING_YEAR + " INTEGER, "
            + OFISDBContract.FarmManagementPlanEntry.COLUMN_NAME_HAS_FEEDBACK + " INTEGER DEFAULT 0 )";

    private static final String OFIS_FMP_SECTIONS_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.FarmManagementPlanSectionsEntry.TABLE_NAME +
            " ( " + OFISDBContract.FarmManagementPlanSectionsEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY, " + OFISDBContract.FarmManagementPlanSectionsEntry.COLUMN_NAME_NAME + " TEXT, "
            + OFISDBContract.FarmManagementPlanSectionsEntry.COLUMN_NAME_DISPLAY_TITLE + " TEXT, " + OFISDBContract.FarmManagementPlanSectionsEntry.COLUMN_NAME_DISPLAY_TITLE_FR + " TEXT,"
            + OFISDBContract.FarmManagementPlanSectionsEntry.COLUMN_NAME_DISPLAY_TITLE_ES + " TEXT, " + OFISDBContract.FarmManagementPlanSectionsEntry.COLUMN_NAME_DISPLAY_TITLE_ID + " TEXT, "
            + OFISDBContract.FarmManagementPlanSectionsEntry.COLUMN_NAME_DISPLAY_TITLE_PT + " TEXT, " + OFISDBContract.FarmManagementPlanSectionsEntry.COLUMN_NAME_DISPLAY_TITLE_TR + " TEXT, "
            + OFISDBContract.FarmManagementPlanSectionsEntry.COLUMN_NAME_DISPLAY_TITLE_LO + " TEXT, " + OFISDBContract.FarmManagementPlanSectionsEntry.COLUMN_NAME_DISPLAY_TITLE_VI + " TEXT, "
            + OFISDBContract.FarmManagementPlanSectionsEntry.COLUMN_NAME_DISPLAY_TITLE_TH + " TEXT )";

    private static final String OFIS_FMP_ENTRIES_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.FarmManagementPlanEntriesEntry.TABLE_NAME +
            " ( " + OFISDBContract.FarmManagementPlanEntriesEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY, " + OFISDBContract.FarmManagementPlanEntriesEntry.COLUMN_NAME_SECTION_ID + " INTEGER, "
            + OFISDBContract.FarmManagementPlanEntriesEntry.COLUMN_NAME_POSITION + " INTEGER, " + OFISDBContract.FarmManagementPlanEntriesEntry.COLUMN_NAME_NAME + " TEXT, "
            + OFISDBContract.FarmManagementPlanEntriesEntry.COLUMN_NAME_DISPLAY_TITLE + " TEXT, " + OFISDBContract.FarmManagementPlanEntriesEntry.COLUMN_NAME_DISPLAY_TITLE_FR + " TEXT, "
            + OFISDBContract.FarmManagementPlanEntriesEntry.COLUMN_NAME_DISPLAY_TITLE_ES + " TEXT, " + OFISDBContract.FarmManagementPlanEntriesEntry.COLUMN_NAME_DISPLAY_TITLE_ID + " TEXT, "
            + OFISDBContract.FarmManagementPlanEntriesEntry.COLUMN_NAME_DISPLAY_TITLE_PT + " TEXT, " + OFISDBContract.FarmManagementPlanEntriesEntry.COLUMN_NAME_DISPLAY_TITLE_TR + " TEXT, "
            + OFISDBContract.FarmManagementPlanEntriesEntry.COLUMN_NAME_DISPLAY_TITLE_LO + " TEXT, " + OFISDBContract.FarmManagementPlanEntriesEntry.COLUMN_NAME_DISPLAY_TITLE_VI + " TEXT, "
            + OFISDBContract.FarmManagementPlanEntriesEntry.COLUMN_NAME_DISPLAY_TITLE_TH + " TEXT, " + OFISDBContract.FarmManagementPlanEntriesEntry.COLUMN_NAME_SHOW_IN_APP + " INTEGER, "
            + OFISDBContract.FarmManagementPlanEntriesEntry.COLUMN_NAME_CALCULATED_FIELD + " INTEGER, " + OFISDBContract.FarmManagementPlanEntriesEntry.COLUMN_NAME_CALCULATION_VARIABLE + " INTEGER )";

    private static final String OFIS_FMP_VALUES_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.FarmManagementPlanValuesEntry.TABLE_NAME +
            " ( " + OFISDBContract.FarmManagementPlanValuesEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + OFISDBContract.FarmManagementPlanValuesEntry.COLUMN_NAME_FMP_ID + " INTEGER, "
            + OFISDBContract.FarmManagementPlanValuesEntry.COLUMN_NAME_FMP_ENTRY_ID + " INTEGER, " + OFISDBContract.FarmManagementPlanValuesEntry.COLUMN_NAME_YEAR + " INTEGER, "
            + OFISDBContract.FarmManagementPlanValuesEntry.COLUMN_NAME_VALUE + " TEXT )";

    private static final String OFIS_FMP_CURRENT_VALUES_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.FarmManagementPlanCurrentValuesEntry.TABLE_NAME +
            " ( " + OFISDBContract.FarmManagementPlanCurrentValuesEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + OFISDBContract.FarmManagementPlanCurrentValuesEntry.COLUMN_NAME_FMP_ID + " INTEGER, "
            + OFISDBContract.FarmManagementPlanCurrentValuesEntry.COLUMN_NAME_FMP_ENTRY_ID + " INTEGER, " + OFISDBContract.FarmManagementPlanCurrentValuesEntry.COLUMN_NAME_YEAR + " INTEGER, "
            + OFISDBContract.FarmManagementPlanCurrentValuesEntry.COLUMN_NAME_VALUE + " TEXT )";

    // transaction tables
    private static final String OFIS_CURRENCY_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.Currency.TABLE_NAME
            + " ( " + OFISDBContract.Currency.COLUMN_NAME_ID + " INTEGER PRIMARY KEY, " + OFISDBContract.Currency.COLUMN_NAME_SYMBOL + " TEXT, "
            + OFISDBContract.Currency.COLUMN_NAME_CODE + " TEXT, " + OFISDBContract.Currency.COLUMN_NAME_NAME + " TEXT, "
            + OFISDBContract.Currency.COLUMN_NAME_NAME_FR + " TEXT, " + OFISDBContract.Currency.COLUMN_NAME_NAME_ES + " TEXT, "
            + OFISDBContract.Currency.COLUMN_NAME_NAME_ID + " TEXT, " + OFISDBContract.Currency.COLUMN_NAME_NAME_PT + " TEXT, "
            + OFISDBContract.Currency.COLUMN_NAME_NAME_TR + " TEXT, " + OFISDBContract.Currency.COLUMN_NAME_NAME_LO + " TEXT, "
            + OFISDBContract.Currency.COLUMN_NAME_NAME_VI + " TEXT, " + OFISDBContract.Currency.COLUMN_NAME_NAME_TH + " TEXT )";

    private static final String OFIS_COUNTRY_CURRENCY_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.CountryCurrency.TABLE_NAME
            + " ( " + OFISDBContract.CountryCurrency.COLUMN_NAME_ID + " INTEGER PRIMARY KEY, " + OFISDBContract.CountryCurrency.COLUMN_COUNTRY_ID
            + " INTEGER, " + OFISDBContract.CountryCurrency.COLUMN_CURRENCY_ID + " INTEGER )";

    private static final String OFIS_UNIT_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.Unit.TABLE_NAME
            + " ( " + OFISDBContract.Unit.COLUMN_NAME_ID + " INTEGER PRIMARY KEY, " + OFISDBContract.Unit.COLUMN_NAME_NAME
            + " TEXT )";

    // training tables
    private static final String OFIS_COMPLETIONCRITERIA_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.CompletionCriteria.TABLE_NAME
            + " ( " + OFISDBContract.CompletionCriteria.COLUMN_NAME_ID + " INTEGER PRIMARY KEY, " + OFISDBContract.CompletionCriteria.COLUMN_NAME_NAME + " TEXT, "
            + OFISDBContract.CompletionCriteria.COLUMN_NAME_SCORING_COLUMNS + " TEXT )";

    private static final String OFIS_COURSE_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.Course.TABLE_NAME
            + " ( " + OFISDBContract.Course.COLUMN_NAME_ID + " INTEGER PRIMARY KEY, "
            + OFISDBContract.Course.COLUMN_NAME_NAME + " TEXT, " + OFISDBContract.Course.COLUMN_NAME_NAME_FR + " TEXT, "
            + OFISDBContract.Course.COLUMN_NAME_NAME_ES + " TEXT, " + OFISDBContract.Course.COLUMN_NAME_NAME_ID + " TEXT, "
            + OFISDBContract.Course.COLUMN_NAME_NAME_PT + " TEXT, " + OFISDBContract.Course.COLUMN_NAME_NAME_TR + " TEXT, "
            + OFISDBContract.Course.COLUMN_NAME_NAME_LO + " TEXT, " + OFISDBContract.Course.COLUMN_NAME_NAME_VI + " TEXT, "
            + OFISDBContract.Course.COLUMN_NAME_NAME_TH + " TEXT )";

    private static final String OFIS_COURSEASSIGNMENT_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.CourseAssignment.TABLE_NAME
            + " ( " + OFISDBContract.CourseAssignment.COLUMN_NAME_ID + " INTEGER PRIMARY KEY, "
            + OFISDBContract.CourseAssignment.COLUMN_NAME_COURSE_ID + " INTEGER, " + OFISDBContract.CourseAssignment.COLUMN_NAME_COURSE_ASSIGNMENT_ID + " INTEGER, "
            + OFISDBContract.CourseAssignment.COLUMN_NAME_COURSE_ASSIGNMENT_TYPE + " TEXT )";

    private static final String OFIS_TRAININGATTENDEE_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.TrainingAttendee.TABLE_NAME
            + " ( " + OFISDBContract.TrainingAttendee.COLUMN_NAME_ID + " INTEGER PRIMARY KEY, " + OFISDBContract.TrainingAttendee.COLUMN_NAME_FARMER_ID + " INTEGER, "
            + OFISDBContract.TrainingAttendee.COLUMN_NAME_TRAINING_RECORD_ID + " INTEGER, " + OFISDBContract.TrainingAttendee.COLUMN_NAME_ATTENDED + " INTEGER DEFAULT 1, "
            + OFISDBContract.TrainingAttendee.COLUMN_NAME_PASSED + " INTEGER, " + OFISDBContract.TrainingAttendee.COLUMN_NAME_PRESCORE + " TEXT, "
            + OFISDBContract.TrainingAttendee.COLUMN_NAME_POSTSCORE + " TEXT, " + OFISDBContract.TrainingAttendee.COLUMN_NAME_SCORE + " TEXT, "
            + OFISDBContract.TrainingAttendee.COLUMN_NAME_PERCENTAGE + " TEXT, "
            + OFISDBContract.TrainingAttendee.COLUMN_NAME_GRADE + " TEXT, "
            + OFISDBContract.TrainingAttendee.COLUMN_NAME_TRAINEE_SIGNATURE + " TEXT, "
            + OFISDBContract.TrainingAttendee.COLUMN_NAME_STAKEHOLDER_ID + " TEXT, "
            + OFISDBContract.TrainingAttendee.COLUMN_NAME_TRAINEE_NAME + " TEXT )";

    private static final String OFIS_TRAINING_RELATIONSHIP_ATTENDEE_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.TrainingAttendeeRelations.TABLE_NAME
            + " ( " + OFISDBContract.TrainingAttendeeRelations.COLUMN_NAME_ID + " INTEGER PRIMARY KEY, " + OFISDBContract.TrainingAttendeeRelations.COLUMN_NAME_FARMER_ID + " INTEGER, "
            + OFISDBContract.TrainingAttendeeRelations.COLUMN_NAME_TRAINING_RECORD_ID + " INTEGER, " + OFISDBContract.TrainingAttendeeRelations.COLUMN_NAME_TRAINEE_NAME + " TEXT, "
            + OFISDBContract.TrainingAttendeeRelations.COLUMN_NAME_TRAINEE_AGE + " INTEGER, " + OFISDBContract.TrainingAttendeeRelations.COLUMN_NAME_TRAINEE_RELATIONSHIP + " TEXT, "
            + OFISDBContract.TrainingAttendeeRelations.COLUMN_NAME_TRAINEE_RELATIONSHIP_ID + " TEXT, "
            + OFISDBContract.TrainingAttendeeRelations.COLUMN_NAME_TRAINEE_GENDER + " TEXT, "
            + OFISDBContract.TrainingAttendeeRelations.COLUMN_NAME_TRAINEE_GENDER_ID + " TEXT, "
            + OFISDBContract.TrainingAttendeeRelations.COLUMN_NAME_TRAINEE_RELATION_SIGNATURE + " TEXT, "
            + OFISDBContract.TrainingAttendeeRelations.COLUMN_NAME_STAKEHOLDER_ID + " TEXT )";

    private static final String OFIS_TRAININGMODULE_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.TrainingModule.TABLE_NAME
            + " ( " + OFISDBContract.TrainingModule.COLUMN_NAME_ID + " INTEGER PRIMARY KEY, " + OFISDBContract.TrainingModule.COLUMN_NAME_COURSE_ID + " INTEGER, "
            + OFISDBContract.TrainingModule.COLUMN_COMPLETIONCRITERION_ID + " INTEGER, " + OFISDBContract.TrainingModule.COLUMN_NAME_NAME + " TEXT, "
            + OFISDBContract.TrainingModule.COLUMN_NAME_NAME_FR + " TEXT, "
            + OFISDBContract.TrainingModule.COLUMN_NAME_NAME_ES + " TEXT, "
            + OFISDBContract.TrainingModule.COLUMN_NAME_CAPTURE_ATTENDEE + " TEXT, "
            + OFISDBContract.TrainingModule.COLUMN_NAME_NAME_ID + " TEXT, " + OFISDBContract.TrainingModule.COLUMN_NAME_NAME_PT + " TEXT, "
            + OFISDBContract.TrainingModule.COLUMN_NAME_NAME_TR + " TEXT, " + OFISDBContract.TrainingModule.COLUMN_NAME_NAME_LO + " TEXT, "
            + OFISDBContract.TrainingModule.COLUMN_NAME_NAME_VI + " TEXT, "
            + OFISDBContract.TrainingModule.COLUMN_NAME_NAME_TH + " TEXT, "
            + OFISDBContract.TrainingModule.COLUMN_NAME_SIGNATURE + " TEXT )";

    private static final String OFIS_TRAININGRECORD_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.TrainingRecord.TABLE_NAME
            + " ( " + OFISDBContract.TrainingRecord.COLUMN_NAME_ID + " INTEGER PRIMARY KEY, " + OFISDBContract.TrainingRecord.COLUMN_TRAININGMODULE_ID + " INTEGER, "
            + OFISDBContract.TrainingRecord.COLUMN_NAME_SUBMISSION_TIME + " TEXT, " + OFISDBContract.TrainingRecord.COLUMN_NAME_PHOTO + " TEXT, " + OFISDBContract.TrainingRecord.COLUMN_NAME_COURSE_ID + " INTEGER, "
            + OFISDBContract.TrainingRecord.COLUMN_NAME_IN_QUEUE + " INTEGER DEFAULT 0)";

    private static final String OFIS_TASK_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.Task.TABLE_NAME
            + " ( " + OFISDBContract.Task.COLUMN_NAME_ID + " INTEGER PRIMARY KEY, " + OFISDBContract.Task.COLUMN_NAME_TASKTEXT + " TEXT, "
            + OFISDBContract.Task.COLUMN_NAME_TASK_CREATOR + " TEXT, " + OFISDBContract.Task.COLUMN_NAME_COMPLETED + " INTEGER DEFAULT 0, "
            + OFISDBContract.Task.COLUMN_NAME_CREATED_DATE + " TEXT, "
            + OFISDBContract.Task.COLUMN_NAME_COMPLETED_DATE + " TEXT, "
            + OFISDBContract.Task.COLUMN_NAME_ENUMERATORS_TASK_ID + " INTEGER DEFAULT 0, "
            + OFISDBContract.Task.COLUMN_NAME_SYNCED + " INTEGER DEFAULT 0, " + OFISDBContract.Task.COLUMN_NAME_DELIVERY_CONFIRMED + " INTEGER DEFAULT 0)";

    private static final String OFIS_SUBQUESTION_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.Subquestionanswer.TABLE_NAME
            + " ( " + OFISDBContract.Subquestionanswer.COLUMN_NAME_ID + " INTEGER , " + OFISDBContract.Subquestionanswer.COLUMN_NAME_PARENR_ID + " INTEGER, "
            + OFISDBContract.Subquestionanswer.COLUMN_NAME_QUESTION_ID + " INTEGER, " + OFISDBContract.Subquestionanswer.COLUMN_NAME_MODULE_ID + " TEXT, " +
            OFISDBContract.Subquestionanswer.COLUMN_NAME_FARMER_ID + " TEXT, " + OFISDBContract.Subquestionanswer.COLUMN_NAME_QUESTION_ANSWER + " TEXT, " +
            OFISDBContract.Subquestionanswer.COLUMN_NAME_REPORT_YEAR + " TEXT, " + OFISDBContract.Subquestionanswer.COLUMN_NAME_POSITION + " TEXT)";

    // create SubModules
    private static final String OFIS_MULTIFARM_ROLES_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.MultiFarmRoles.TABLE_NAME
            + " ( " + OFISDBContract.MultiFarmRoles.COLUMN_NAME_ID + " INTEGER PRIMARY KEY, " + OFISDBContract.MultiFarmRoles.COLUMN_NAME_NAME + " TEXT, "
            + OFISDBContract.MultiFarmRoles.COLUMN_NAME_NAME_FR + " TEXT, " + OFISDBContract.MultiFarmRoles.COLUMN_NAME_NAME_ES + " TEXT, "
            + OFISDBContract.MultiFarmRoles.COLUMN_NAME_NAME_ID + " TEXT, " + OFISDBContract.MultiFarmRoles.COLUMN_NAME_NAME_PT + " TEXT, "
            + OFISDBContract.MultiFarmRoles.COLUMN_NAME_NAME_TR + " TEXT, " + OFISDBContract.MultiFarmRoles.COLUMN_NAME_NAME_LO + " TEXT, "
            + OFISDBContract.MultiFarmRoles.COLUMN_NAME_NAME_VI + " TEXT, " + OFISDBContract.MultiFarmRoles.COLUMN_NAME_NAME_TH + " TEXT )";

    private static final String OFIS_DOWNLOADED_SURVEYLIST_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.DownloadedSurveyList.TABLE_NAME
            + " ( " + OFISDBContract.DownloadedSurveyList.COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + OFISDBContract.DownloadedSurveyList.COLUMN_NAME_MODULE_ID + " TEXT, "
            + OFISDBContract.DownloadedSurveyList.COLUMN_NAME_MODULE_NAME + " TEXT, "
            + OFISDBContract.DownloadedSurveyList.COLUMN_NAME_FARMER_GROUP + " TEXT, "
            + OFISDBContract.DownloadedSurveyList.COLUMN_NAME_SECTION + " TEXT, "
            + OFISDBContract.DownloadedSurveyList.COLUMN_NAME_SAVED_DATE + " TEXT)";


    private static final String OFIS_TRAINING_RELATIONS_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.TrainingAttendeeRelationShip.TABLE_NAME
            + " ( " + OFISDBContract.TrainingAttendeeRelationShip.COLUMN_NAME_ID + " INTEGER PRIMARY KEY, " + OFISDBContract.TrainingAttendeeRelationShip.COLUMN_NAME_NAME + " TEXT, "
            + OFISDBContract.TrainingAttendeeRelationShip.COLUMN_NAME_NAME_FR + " TEXT, " + OFISDBContract.TrainingAttendeeRelationShip.COLUMN_NAME_NAME_ES + " TEXT, "
            + OFISDBContract.TrainingAttendeeRelationShip.COLUMN_NAME_NAME_ID + " TEXT, " + OFISDBContract.TrainingAttendeeRelationShip.COLUMN_NAME_NAME_PT + " TEXT, "
            + OFISDBContract.TrainingAttendeeRelationShip.COLUMN_NAME_NAME_TR + " TEXT, " + OFISDBContract.TrainingAttendeeRelationShip.COLUMN_NAME_NAME_LO + " TEXT, "
            + OFISDBContract.TrainingAttendeeRelationShip.COLUMN_NAME_NAME_VI + " TEXT, " + OFISDBContract.TrainingAttendeeRelationShip.COLUMN_NAME_NAME_TH + " TEXT )";

    private static final String OFIS_NOTIFICATION_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.NotificationAssignments.TABLE_NAME
            + " ( " + OFISDBContract.NotificationAssignments.COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            OFISDBContract.NotificationAssignments.COLUMN_FARMER_ID + " INTEGER, " +
            OFISDBContract.NotificationAssignments.COLUMN_CHILD_ID + " INTEGER, " +
            OFISDBContract.NotificationAssignments.COLUMN_MODULEID + " INTEGER, " +
            OFISDBContract.NotificationAssignments.COLUMN_STATUS + " TEXT, " +
            OFISDBContract.NotificationAssignments.COLUMN_CREATEDAT + " TEXT )";

    private static final String OFIS_NOTIFICATION_MODULELIST_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + OFISDBContract.NotificationModuleAssignments.TABLE_NAME
            + " ( " + OFISDBContract.NotificationModuleAssignments.COLUMN_NAME_ID + " INTEGER PRIMARY KEY, " +
            OFISDBContract.NotificationModuleAssignments.COLUMN_MODULENAME + " TEXT, " +
            OFISDBContract.NotificationModuleAssignments.COLUMN_TOTALCOUNT + " INTEGER, " +
            OFISDBContract.NotificationModuleAssignments.COLUMN_NAME_MSG + " TEXT )";


    public static OFISDB getInstance(Context context) {
        if (sInstance == null) {
            synchronized (OFISDB.class) {
                if (sInstance == null) {
                    sInstance = new OFISDB(context.getApplicationContext());
                }
            }
        }
        return sInstance;
    }

    private OFISDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(OFIS_MODULES_TABLE_CREATE);
        sqLiteDatabase.execSQL(OFIS_SUBMODULES_TABLE_CREATE);
        sqLiteDatabase.execSQL(OFIS_MODULEASSIGNMENTS_TABLE_CREATE);
        sqLiteDatabase.execSQL(OFIS_FARMERMODULES_TABLE_CREATE);
        sqLiteDatabase.execSQL(OFIS_PLOT_GEO_DATA_TABLE_CREATE);
        sqLiteDatabase.execSQL(OFIS_DATATYPES_TABLE_CREATE);
        sqLiteDatabase.execSQL(OFIS_LOOKUPVALUES_TABLE_CREATE);
        sqLiteDatabase.execSQL(OFIS_QUESTION_TABLE_CREATE);
        sqLiteDatabase.execSQL(OFIS_ANSWER_TABLE_CREATE);
        sqLiteDatabase.execSQL(OFIS_FARMER_TABLE_CREATE);
        sqLiteDatabase.execSQL(OFIS_BACKUP_FARMER_TABLE_CREATE);
        sqLiteDatabase.execSQL(OFIS_FARMER_INDEX_CREATE);
        sqLiteDatabase.execSQL(OFIS_PHONENUMBERS_TABLE_CREATE);
        sqLiteDatabase.execSQL(OFIS_COUNTRIES_TABLE_CREATE);
        sqLiteDatabase.execSQL(OFIS_REGIONS_TABLE_CREATE);
        sqLiteDatabase.execSQL(OFIS_DISTRICTS_TABLE_CREATE);
        sqLiteDatabase.execSQL(OFIS_PLACES_TABLE_CREATE);
        sqLiteDatabase.execSQL(OFIS_FARMERGROUPS_TABLE_CREATE);
        sqLiteDatabase.execSQL(OFIS_SECTIONS_TABLE_CREATE);
        sqLiteDatabase.execSQL(OFIS_PRODUCTS_TABLE_CREATE);
        sqLiteDatabase.execSQL(OFIS_NETWORKQUEUE_TABLE_CREATE);
        sqLiteDatabase.execSQL(OFIS_FMP_TABLE_CREATE);
        sqLiteDatabase.execSQL(OFIS_FMP_SECTIONS_TABLE_CREATE);
        sqLiteDatabase.execSQL(OFIS_FMP_ENTRIES_TABLE_CREATE);
        sqLiteDatabase.execSQL(OFIS_FMP_VALUES_TABLE_CREATE);
        sqLiteDatabase.execSQL(OFIS_FMP_CURRENT_VALUES_TABLE_CREATE);
        sqLiteDatabase.execSQL(OFIS_CURRENCY_TABLE_CREATE);
        sqLiteDatabase.execSQL(OFIS_COUNTRY_CURRENCY_TABLE_CREATE);
        sqLiteDatabase.execSQL(OFIS_UNIT_TABLE_CREATE);
        sqLiteDatabase.execSQL(OFIS_COMPLETIONCRITERIA_TABLE_CREATE);
        sqLiteDatabase.execSQL(OFIS_COURSE_TABLE_CREATE);
        sqLiteDatabase.execSQL(OFIS_COURSEASSIGNMENT_TABLE_CREATE);
        sqLiteDatabase.execSQL(OFIS_TRAININGATTENDEE_TABLE_CREATE);
        sqLiteDatabase.execSQL(OFIS_TRAININGMODULE_TABLE_CREATE);
        sqLiteDatabase.execSQL(OFIS_TRAININGRECORD_TABLE_CREATE);
        sqLiteDatabase.execSQL(OFIS_TASK_TABLE_CREATE);
        sqLiteDatabase.execSQL(OFIS_SUBQUESTION_TABLE_CREATE);
        sqLiteDatabase.execSQL(OFIS_SIPGROUP_TABLE_CREATE);
        sqLiteDatabase.execSQL(OFIS_SIPSUBGROUP_TABLE_CREATE);
        sqLiteDatabase.execSQL(OFIS_ANSWERSIP_TABLE_CREATE);
        sqLiteDatabase.execSQL(OFIS_QUESTIONSIP_TABLE_CREATE);
        sqLiteDatabase.execSQL(OFIS_PRODUCTLIST_TABLE_CREATE);
        sqLiteDatabase.execSQL(OFIS_DATATYPESSIP_TABLE_CREATE);
        sqLiteDatabase.execSQL(OFIS_LOOKUPVALUESSIP_TABLE_CREATE);
        sqLiteDatabase.execSQL(OFIS_All_COUNTRIES_TABLE_CREATE);

        // child lab
        sqLiteDatabase.execSQL(OFIS_CHILD_TABLE_CREATE);
        sqLiteDatabase.execSQL(OFIS_CHILD_VIST_DETAILS_CREATE);
        sqLiteDatabase.execSQL(OFIS_FARMERCHILD_MODULES_TABLE_CREATE);
        sqLiteDatabase.execSQL(OFIS_ANSWER_CHILD_TABLE_CREATE);
        sqLiteDatabase.execSQL(OFIS_CHILD_INDIVIDUAL_SURVEY_TABLE_CREATE);
        sqLiteDatabase.execSQL(OFIS_GPS_MONITOR_TABLE_CREATE);

//        nationalis
        sqLiteDatabase.execSQL(OFIS_NATIONALIDS_TABLE_CREATE);
        sqLiteDatabase.execSQL(OFIS_FAMRERNATIONALIDS_TABLE_CREATE);
        sqLiteDatabase.execSQL(OFIS_LOCALFAMRERNATIONALIDS_TABLE_CREATE);
        sqLiteDatabase.execSQL(OFIS_LOCALNATIONALIDIMAGES_TABLE_CREATE);

//        updatesignature
        sqLiteDatabase.execSQL(OFIS_SIGNATURE_TABLE_CREATE);
//        Update consent statement
        sqLiteDatabase.execSQL(OFIS_CONSENT_STATEMENT_CREATE);
//        subdistrict
        sqLiteDatabase.execSQL(OFIS_SUBDISTRICTS_TABLE_CREATE);
//      farmerleads
        sqLiteDatabase.execSQL(OFIS_FARMERLEADS_TABLE_CREATE);
        sqLiteDatabase.execSQL(OFIS_FARMERASSIGNMENTS_TABLE_CREATE);

        sqLiteDatabase.execSQL(OFIS_MULTIFARM_ROLES_TABLE_CREATE);
        // OW-2949
        sqLiteDatabase.execSQL(OFIS_TRAINING_RELATIONSHIP_ATTENDEE_TABLE_CREATE);
        sqLiteDatabase.execSQL(OFIS_TRAINING_RELATIONS_TABLE_CREATE);
        sqLiteDatabase.execSQL(OFIS_DOWNLOADED_SURVEYLIST_TABLE_CREATE);

        sqLiteDatabase.execSQL(OFIS_SAVED_ANSWER_TABLE_CREATE);
        sqLiteDatabase.execSQL(OFIS_PLOT_TABLE_CREATE);
        sqLiteDatabase.execSQL(OFIS_NOTIFICATION_TABLE_CREATE);
        sqLiteDatabase.execSQL(OFIS_NOTIFICATION_MODULELIST_TABLE_CREATE);

        // QWR
        sqLiteDatabase.execSQL(OFIS_QUESTIONTYPE_TABLE_CREATE);
        sqLiteDatabase.execSQL(OFIS_ANSWERTYPE_TABLE_CREATE);
        sqLiteDatabase.execSQL(OFIS_RECURRENCETYPE_TABLE_CREATE);
        // SIP
        sqLiteDatabase.execSQL(OFIS_SIP_PLOT_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        switch(oldVersion){
            case 158:
                migrateFromVersion158To159(sqLiteDatabase);
                migrateFrom159To160(sqLiteDatabase);
                migrateFrom160To161(sqLiteDatabase);
                migrateFrom162To163(sqLiteDatabase);
                break;
            case 159:
                migrateFrom159To160(sqLiteDatabase);
                migrateFrom160To161(sqLiteDatabase);
                migrateFrom162To163(sqLiteDatabase);
                break;
            case 160:
                migrateFrom160To161(sqLiteDatabase);
                migrateFrom162To163(sqLiteDatabase);
                break;
            case 161:
                migrateFrom161To162(sqLiteDatabase);
                migrateFrom162To163(sqLiteDatabase);
                break;
            case 162:
                migrateFrom162To163(sqLiteDatabase);
            default:
                migrateFromVersionBelow157(sqLiteDatabase);
                migrateFromVersion158To159(sqLiteDatabase);
                migrateFrom159To160(sqLiteDatabase);
                migrateFrom160To161(sqLiteDatabase);
                migrateFrom162To163(sqLiteDatabase);
                break;
        }
    }

    private void migrateFromVersionBelow157(SQLiteDatabase sqLiteDatabase) {
        try {
            sqLiteDatabase.execSQL(" ALTER TABLE " + OFISDBContract.ModuleEntry.TABLE_NAME + " ADD COLUMN " + OFISDBContract.ModuleEntry.COLUMN_NAME_SURVEY_TYPE + " TEXT ");
            sqLiteDatabase.execSQL(" ALTER TABLE " + OFISDBContract.AnswerEntry.TABLE_NAME + " ADD COLUMN " + OFISDBContract.AnswerEntry.COLUMN_NAME_STAKEHOLDER_ID + " TEXT ");
            sqLiteDatabase.execSQL(" ALTER TABLE " + OFISDBContract.FarmerModulesEntry.TABLE_NAME + " ADD COLUMN " + OFISDBContract.FarmerModulesEntry.COLUMN_NAME_STAKEHOLDER_ID + " TEXT ");
            sqLiteDatabase.execSQL(" ALTER TABLE " + OFISDBContract.SavedAnswerEntry.TABLE_NAME + " ADD COLUMN " + OFISDBContract.SavedAnswerEntry.COLUMN_NAME_STAKEHOLDER_ID + " TEXT ");
            sqLiteDatabase.execSQL(" ALTER TABLE " + OFISDBContract.ModuleEntry.TABLE_NAME + " ADD COLUMN " + OFISDBContract.ModuleEntry.COLUMN_NAME_IS_MANDATE + " TEXT ");
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void migrateFromVersion158To159(SQLiteDatabase sqLiteDatabase) {
        try {
            sqLiteDatabase.execSQL(" ALTER TABLE " + OFISDBContract.TrainingModule.TABLE_NAME + " ADD COLUMN " + OFISDBContract.TrainingModule.COLUMN_NAME_SIGNATURE + " TEXT ");
            sqLiteDatabase.execSQL(" ALTER TABLE " + OFISDBContract.TrainingAttendee.TABLE_NAME + " ADD COLUMN " + OFISDBContract.TrainingAttendee.COLUMN_NAME_TRAINEE_SIGNATURE + " TEXT ");
            sqLiteDatabase.execSQL(" ALTER TABLE " + OFISDBContract.TrainingAttendeeRelations.TABLE_NAME + " ADD COLUMN " + OFISDBContract.TrainingAttendeeRelations.COLUMN_NAME_TRAINEE_RELATION_SIGNATURE + " TEXT ");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        migrateFrom159To160(sqLiteDatabase);
    }

    private void migrateFrom159To160(SQLiteDatabase sqLiteDatabase) {
        try {
            sqLiteDatabase.execSQL("DROP TABLE buyingagent_buyingrule");
            sqLiteDatabase.execSQL("DROP TABLE buyingagent_currency");
            sqLiteDatabase.execSQL("DROP TABLE buyingagents");
            sqLiteDatabase.execSQL("DROP TABLE buyingrules");
            sqLiteDatabase.execSQL("DROP TABLE direction_transactionitemgroup");
            sqLiteDatabase.execSQL("DROP TABLE direction_transactionitemtype");
            sqLiteDatabase.execSQL("DROP TABLE directions");
            sqLiteDatabase.execSQL("DROP TABLE transactees");
            sqLiteDatabase.execSQL("DROP TABLE transactionbalances");
            sqLiteDatabase.execSQL("DROP TABLE transactionitemgroups");
            sqLiteDatabase.execSQL("DROP TABLE transactionitems");
            sqLiteDatabase.execSQL("DROP TABLE transactionitemtype_unit");
            sqLiteDatabase.execSQL("DROP TABLE transactionitemtypes");
            sqLiteDatabase.execSQL("DROP TABLE productgrades");
            sqLiteDatabase.execSQL("DROP TABLE productprices");
            sqLiteDatabase.execSQL("DROP TABLE product_transactionitemtype");

            sqLiteDatabase.execSQL(" ALTER TABLE " + OFISDBContract.FarmerEntry.TABLE_NAME + " ADD COLUMN " + OFISDBContract.FarmerEntry.COLUMN_NAME_DUPLICATE_FARMER_CODE + " TEXT ");
            sqLiteDatabase.execSQL(" ALTER TABLE " + OFISDBContract.FarmerEntry.TABLE_NAME + " ADD COLUMN " + OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_CODE + " TEXT default null");
            sqLiteDatabase.execSQL(" ALTER TABLE " + OFISDBContract.FarmerEntry.BACKUP_TABLE_NAME + " ADD COLUMN " + OFISDBContract.FarmerEntry.COLUMN_NAME_DUPLICATE_FARMER_CODE + " TEXT ");
            sqLiteDatabase.execSQL(" ALTER TABLE " + OFISDBContract.FarmerEntry.BACKUP_TABLE_NAME + " ADD COLUMN " + OFISDBContract.FarmerEntry.COLUMN_NAME_FARMER_CODE + " TEXT ");


            sqLiteDatabase.execSQL(" ALTER TABLE " + OFISDBContract.PhoneNumber.TABLE_NAME + " ADD COLUMN " + OFISDBContract.PhoneNumber.COLUMN_NAME_IS_DUPLICATE + " TEXT ");
            sqLiteDatabase.execSQL(" ALTER TABLE " + OFISDBContract.FarmerNationalIDs.TABLE_NAME + " ADD COLUMN " + OFISDBContract.FarmerNationalIDs.COLUMN_NAME_IS_DUPLICATE + " TEXT ");
            sqLiteDatabase.execSQL(" ALTER TABLE " + OFISDBContract.LocalFarmerNationalIDs.TABLE_NAME + " ADD COLUMN " + OFISDBContract.LocalFarmerNationalIDs.COLUMN_BACKEND_DB_ID + " TEXT ");

            sqLiteDatabase.execSQL(" ALTER TABLE " + OFISDBContract.FarmerEntry.TABLE_NAME + " ADD COLUMN " + OFISDBContract.FarmerEntry.COLUMN_NAME_PLOT_AVAILABLE + " TEXT ");
            sqLiteDatabase.execSQL(" ALTER TABLE " + OFISDBContract.FarmerEntry.BACKUP_TABLE_NAME + " ADD COLUMN " + OFISDBContract.FarmerEntry.COLUMN_NAME_PLOT_AVAILABLE + " TEXT ");
            sqLiteDatabase.execSQL(" ALTER TABLE " + OFISDBContract.PlotEntry.TABLE_NAME + " ADD COLUMN " + OFISDBContract.PlotEntry.COLUMN_NAME_POLYGON_SIZE + " TEXT ");
            sqLiteDatabase.execSQL(" ALTER TABLE " + OFISDBContract.PlotEntry.TABLE_NAME + " ADD COLUMN " + OFISDBContract.PlotEntry.COLUMN_NAME_MOB_DEACTIVATE_FLAG + " TEXT DEFAULT 'false' ");
            sqLiteDatabase.execSQL(" ALTER TABLE " + OFISDBContract.PlotEntry.TABLE_NAME + " ADD COLUMN " + OFISDBContract.PlotEntry.COLUMN_NAME_MOB_DEACTIVATE_REASON + " TEXT ");
            sqLiteDatabase.execSQL(" ALTER TABLE " + OFISDBContract.PlotEntry.TABLE_NAME + " ADD COLUMN " + OFISDBContract.PlotEntry.COLUMN_NAME_POSITION_DATA + " TEXT ");
            sqLiteDatabase.execSQL(" ALTER TABLE " + OFISDBContract.PlotEntry.TABLE_NAME + " ADD COLUMN " + OFISDBContract.PlotEntry.COLUMN_NAME_IS_DE_ACTIVATION_FLAG_SYNCED + " TEXT ");
            sqLiteDatabase.execSQL(" ALTER TABLE " + OFISDBContract.PlotEntry.TABLE_NAME + " ADD COLUMN " + OFISDBContract.PlotEntry.COLUMN_NAME_IS_POLYGON_DOWNLOADED_DATA + " TEXT ");

            sqLiteDatabase.execSQL(" ALTER TABLE " + OFISDBContract.FarmerEntry.TABLE_NAME + " ADD COLUMN " + OFISDBContract.FarmerEntry.COLUMN_NAME_IS_NO_NATIONAL_ID + " TEXT ");
            sqLiteDatabase.execSQL(" ALTER TABLE " + OFISDBContract.FarmerEntry.TABLE_NAME + " ADD COLUMN " + OFISDBContract.FarmerEntry.COLUMN_NAME_IS_NO_PHONE_NUMBER + " TEXT ");
            sqLiteDatabase.execSQL(" ALTER TABLE " + OFISDBContract.FarmerEntry.BACKUP_TABLE_NAME + " ADD COLUMN " + OFISDBContract.FarmerEntry.COLUMN_NAME_IS_NO_NATIONAL_ID + " TEXT ");
            sqLiteDatabase.execSQL(" ALTER TABLE " + OFISDBContract.FarmerEntry.BACKUP_TABLE_NAME + " ADD COLUMN " + OFISDBContract.FarmerEntry.COLUMN_NAME_IS_NO_PHONE_NUMBER + " TEXT ");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        migrateFrom160To161(sqLiteDatabase);
    }

    private void migrateFrom160To161(SQLiteDatabase sqLiteDatabase){
        try {
            sqLiteDatabase.execSQL(" ALTER TABLE " + OFISDBContract.PhoneNumber.TABLE_NAME + " ADD COLUMN " + OFISDBContract.PhoneNumber.COLUMN_NAME_IS_DELETED +  " TEXT ");
            sqLiteDatabase.execSQL(" ALTER TABLE " + OFISDBContract.PhoneNumber.TABLE_NAME + " ADD COLUMN " + OFISDBContract.PhoneNumber.COLUMN_NAME_IS_UPDATED +  " TEXT ");
            sqLiteDatabase.execSQL(" ALTER TABLE " + OFISDBContract.LocalFarmerNationalIDs.TABLE_NAME + " ADD COLUMN " + OFISDBContract.LocalFarmerNationalIDs.COLUMN_IS_DELETED +  " TEXT ");
            sqLiteDatabase.execSQL(" ALTER TABLE " + OFISDBContract.LocalFarmerNationalIDs.TABLE_NAME + " ADD COLUMN " + OFISDBContract.LocalFarmerNationalIDs.COLUMN_IS_UPDATED +  " TEXT ");
            sqLiteDatabase.execSQL(" ALTER TABLE " + OFISDBContract.FarmerNationalIDs.TABLE_NAME + " ADD COLUMN " + OFISDBContract.FarmerNationalIDs.COLUMN_NAME_IS_UPDATED +  " TEXT ");
            sqLiteDatabase.execSQL(" ALTER TABLE " + OFISDBContract.FarmerNationalIDs.TABLE_NAME + " ADD COLUMN " + OFISDBContract.FarmerNationalIDs.COLUMN_NAME_IS_DELETED +  " TEXT ");
            sqLiteDatabase.execSQL(" ALTER TABLE " + OFISDBContract.TrainingAttendee.TABLE_NAME + " ADD COLUMN " + OFISDBContract.TrainingAttendee.COLUMN_NAME_STAKEHOLDER_ID + " TEXT ");
            sqLiteDatabase.execSQL(" ALTER TABLE " + OFISDBContract.TrainingAttendee.TABLE_NAME + " ADD COLUMN " + OFISDBContract.TrainingAttendee.COLUMN_NAME_TRAINEE_NAME + " TEXT ");
            sqLiteDatabase.execSQL(" ALTER TABLE " + OFISDBContract.TrainingAttendeeRelations.TABLE_NAME + " ADD COLUMN " + OFISDBContract.TrainingAttendeeRelations.COLUMN_NAME_STAKEHOLDER_ID + " TEXT ");
            sqLiteDatabase.execSQL(" ALTER TABLE " + OFISDBContract.QuestionEntry.TABLE_NAME + " ADD COLUMN " + OFISDBContract.QuestionEntry.COLUMN_NAME_CALCULATION_LOGIC + " TEXT ");
            sqLiteDatabase.execSQL(" ALTER TABLE " + OFISDBContract.ConsentStatementEntry.TABLE_NAME + " ADD COLUMN " + OFISDBContract.ConsentStatementEntry.COLUMN_NAME_CONSENT_CHECKBOX + " TEXT ");
        }catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void migrateFrom161To162(SQLiteDatabase sqLiteDatabase){
        try {
            boolean columnExists = addColumnIfNotExists(sqLiteDatabase);
            if (columnExists) {
                sqLiteDatabase.execSQL("CREATE TABLE trainingmodules_new (" +
                        "id INTEGER PRIMARY KEY, " +
                        "course_id INTEGER, " +
                        "completioncriterion_id INTEGER, " +
                        "name TEXT, " +
                        "name_fr TEXT, " +
                        "name_es TEXT, " +
                        "name_id TEXT, " +
                        "name_pt TEXT, " +
                        "name_tr TEXT, " +
                        "name_lo TEXT, " +
                        "name_vi TEXT, " +
                        "name_th TEXT, " +
                        "captureAdditionalAttendees TEXT, " +
                        "captureSignature TEXT" +
                        ");");

                sqLiteDatabase.execSQL("INSERT INTO trainingmodules_new SELECT " +
                        "id, course_id, completioncriterion_id, name, name_fr, name_es, name_id, name_pt, name_tr, name_lo, name_vi, name_th, captureAdditionalAttendees, captureSignature " +
                        "FROM trainingmodules;");

                sqLiteDatabase.execSQL("DROP TABLE trainingmodules;");

                sqLiteDatabase.execSQL("ALTER TABLE trainingmodules_new RENAME TO trainingmodules;");

                sqLiteDatabase.execSQL(" ALTER TABLE " + OFISDBContract.TrainingAttendee.TABLE_NAME + " ADD COLUMN " + OFISDBContract.TrainingAttendee.COLUMN_NAME_STAKEHOLDER_ID + " TEXT ");
                sqLiteDatabase.execSQL(" ALTER TABLE " + OFISDBContract.TrainingAttendee.TABLE_NAME + " ADD COLUMN " + OFISDBContract.TrainingAttendee.COLUMN_NAME_TRAINEE_NAME + " TEXT ");
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private boolean addColumnIfNotExists(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("PRAGMA table_info(" + OFISDBContract.TrainingModule.TABLE_NAME + ")", null);
        boolean columnExists = false;

        while (cursor.moveToNext()) {
            String existingColumn = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            if (existingColumn.equals(OFISDBContract.TrainingAttendee.COLUMN_NAME_STAKEHOLDER_ID)) {
                columnExists = true;
                break;
            }
        }
        cursor.close();

        return columnExists;
    }

    private void migrateFrom162To163(SQLiteDatabase sqLiteDatabase) {
        try {
            sqLiteDatabase.execSQL(" ALTER TABLE " + OFISDBContract.ModuleEntry.TABLE_NAME + " ADD COLUMN " + OFISDBContract.ModuleEntry.COLUMN_NAME_IS_AUTO_SURVEY +  " TEXT DEFAULT 'false'");
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

