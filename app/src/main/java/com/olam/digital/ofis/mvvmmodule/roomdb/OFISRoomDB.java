package com.olam.digital.ofis.mvvmmodule.roomdb;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.olam.digital.ofis.mvvmmodule.model.multistakeholder.MultiStakeHolder;
import com.olam.digital.ofis.mvvmmodule.model.multistakeholder.dao.MultiStakeHolderDao;
import com.olam.digital.ofis.mvvmmodule.model.photocheckbox.Dao.PhotoCheckBoxDataDao;
import com.olam.digital.ofis.mvvmmodule.model.photocheckbox.PhotoCheckBoxData;
import com.olam.digital.ofis.mvvmmodule.model.qwrdata.SurveyDurationType;
import com.olam.digital.ofis.mvvmmodule.model.qwrdata.ValidationOnRange;
import com.olam.digital.ofis.mvvmmodule.model.LocalUnit.Dao.GlobalUnitDao;
import com.olam.digital.ofis.mvvmmodule.model.LocalUnit.Dao.LocalUnitDao;
import com.olam.digital.ofis.mvvmmodule.model.LocalUnit.GlobalUnit;
import com.olam.digital.ofis.mvvmmodule.model.LocalUnit.LocalUnit;
import com.olam.digital.ofis.mvvmmodule.model.assigmenttype.AssignmentType;
import com.olam.digital.ofis.mvvmmodule.roomdb.Dao.AssignmentTypeDao;
import com.olam.digital.ofis.mvvmmodule.roomdb.Dao.DmMasterDataDao;
import com.olam.digital.ofis.mvvmmodule.roomdb.Dao.SurveyDurationTypeDao;
import com.olam.digital.ofis.mvvmmodule.roomdb.Dao.ValidationOnRangeDao;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.Certifications;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.ComplianceIndicator;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DMSurveyReport;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmActionPlanCurrenciesTable;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmActionPlansItem;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmActionPlansItemSubmodule;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmAnswerTypes;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmAssessmentResultType;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmComplaintFileTable;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmCurrenciesItem;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmCurrency;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmLookupValuesISubmodule;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmLookupValuesItem;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmMainModule;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmMasterData;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmModuleAssignments;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmNonComplaintFileTable;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmQuestionTypes;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmQuestionsModule;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmQuestionsSubModule;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmRangeActionPlansItems;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmRangeTextBoxModuleQuestionsTable;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmRecurrence;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmSatisfactoryFileTable;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmSectionsItem;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmSurveyAnswerTable;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmTextBoxModuleQuestions;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.Partners;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.Programme;
import com.olam.digital.ofis.mvvmmodule.roomdb.followuptable.CapturedProofFollowupTable;
import com.olam.digital.ofis.mvvmmodule.roomdb.followuptable.ExistingModuleItemFollowupTable;
import com.olam.digital.ofis.mvvmmodule.roomdb.followuptable.FollowUpAnswerTable;
import com.olam.digital.ofis.mvvmmodule.roomdb.followuptable.FollowUpItemMainTable;
import com.olam.digital.ofis.mvvmmodule.roomdb.followuptable.FollowUpResultTable;
import com.olam.digital.ofis.mvvmmodule.roomdb.followuptable.daos.CapturedProofFollowupTableDao;
import com.olam.digital.ofis.mvvmmodule.roomdb.followuptable.daos.ExistingModuleItemFollowupTableDao;
import com.olam.digital.ofis.mvvmmodule.roomdb.followuptable.daos.FollowUpAnswerTableDao;
import com.olam.digital.ofis.mvvmmodule.roomdb.followuptable.daos.FollowUpItemMainTableDao;
import com.olam.digital.ofis.mvvmmodule.roomdb.followuptable.daos.FollowUpResultTableDao;
import com.olam.digital.ofis.recordpolygon.polygonhistory.roomdbpolygon.PolygonHistoryData;
import com.olam.digital.ofis.recordpolygon.polygonhistory.roomdbpolygon.PolygonPlotHistory;
import com.olam.digital.ofis.recordpolygon.polygonhistory.roomdbpolygon.dao.PolygonHistoryDataDao;
import com.olam.digital.ofis.recordpolygon.polygonhistory.roomdbpolygon.dao.PolygonPlotHistoryDao;


@Database(entities ={DmMainModule.class, ComplianceIndicator.class, DmCurrency.class, DmAnswerTypes.class,
        DmQuestionTypes.class, DmCurrenciesItem.class, DmQuestionsModule.class, DmLookupValuesItem.class,
        DmActionPlansItem.class, DmSectionsItem.class, DmQuestionsSubModule.class, DmLookupValuesISubmodule.class,
        DmActionPlansItemSubmodule.class, DmRecurrence.class, DmComplaintFileTable.class,
        DmSurveyAnswerTable.class, DmTextBoxModuleQuestions.class, DmRangeTextBoxModuleQuestionsTable.class,
        DmNonComplaintFileTable.class, DmSatisfactoryFileTable.class, DmActionPlanCurrenciesTable.class,
        DmRangeActionPlansItems.class, DMSurveyReport.class,  FollowUpItemMainTable.class,
        CapturedProofFollowupTable.class, ExistingModuleItemFollowupTable.class, FollowUpAnswerTable.class,
        FollowUpResultTable.class, DmAssessmentResultType.class, Certifications.class, ValidationOnRange.class,
        Partners.class, Programme.class, GlobalUnit.class, LocalUnit.class, PolygonHistoryData.class, PolygonPlotHistory.class, AssignmentType.class, SurveyDurationType.class, PhotoCheckBoxData.class,
        DmMasterData.class, DmModuleAssignments.class, MultiStakeHolder.class},
        version = 16)

public abstract class OFISRoomDB extends RoomDatabase {
    private static final String DATABASE_NAME = "ofismobileroom";
    public abstract DmMainModuleDao dmMainModuleDao();
    public abstract ComplianceIndicatorDao complianceIndicatorDao();
    public abstract DmCurrencyDao dmCurrencyDao();
    public abstract DmAnswerTypesDao dmAnswerTypesDao();
    public abstract DmQuestionTypesDao dmQuestionTypesDao();
    public abstract DmCurrenciesItemDao dmCurrenciesItemDao();
    public abstract DmQuestionsModuleDao dmQuestionsModuleDao();
    public abstract DmLookupValuesItemDao dmLookupValuesItemDao();
    public abstract DmActionPlansItemDao dmActionPlansItemDao();
    public abstract DmSectionsItemDao dmSectionsItemDao();
    public abstract DmLookupValuesISubmoduleDao dmLookupValuesISubmoduleDao();
    public abstract DmActionPlansItemSubmoduleDao dmActionPlansItemSubmoduleDao();
    public abstract DmRecurrenceDao dmRecurrenceDao();
    public abstract DmComplaintFileTableDao dmComplaintFileTableDao();
    public abstract DmSurveyAnswerTableDao dmSurveyAnswerTableDao();
    public abstract DmTextBoxModuleQuestionsDao dmTextBoxModuleQuestionsDao();
    public abstract DmRangeTextBoxModuleQuestionsTableDao dmRangeTextBoxModuleQuestionsTableDao();
    public abstract DmNonComplaintFileTableDao dmNonComplaintFileTableDao();
    public abstract DmSatisfactoryFileTableDao dmSatisfactoryFileTableDao();
    public abstract DmActionPlanCurrenciesTableDao dmActionPlanCurrenciesTableDao();
    public abstract DmRangeActionPlansItemsDao dmRangeActionPlansItemsDao();
    public abstract DMSurveyReportDao dmSurveyReportDao();
    public abstract FollowUpItemMainTableDao followUpItemMainTableDao();
    public abstract CapturedProofFollowupTableDao capturedProofFollowupTableDao();
    public abstract ExistingModuleItemFollowupTableDao existingModuleItemFollowupTableDao();
    public abstract FollowUpAnswerTableDao followUpAnswerTableDao();
    public abstract FollowUpResultTableDao followUpResultTableDao();

    public abstract DmAssessmentResultTypeDao dmAssessmentResultTypeDao();

    //CBU dao
    public abstract CertificationsDao certificationsDao();

    public abstract ValidationOnRangeDao validationOnRangeDao();

    public abstract ProgrammeDao programmeDao();

    public abstract PartnersDao partnersDao();

    public abstract GlobalUnitDao globalUnitDao();

    public abstract LocalUnitDao localUnitDao();

    public abstract PolygonHistoryDataDao polygonHistoryDataDao();

    public abstract PolygonPlotHistoryDao polygonPlotHistoryDao();

    public abstract AssignmentTypeDao assignmentTypeDao();
    public abstract SurveyDurationTypeDao surveyDurationTypeDao();

    public abstract PhotoCheckBoxDataDao photoCheckBoxDataDao();

    public abstract DmMasterDataDao dmMasterDataDao();

    public abstract DmModuleAssignmentsDao dmModuleAssignmentsDao();
    public abstract MultiStakeHolderDao multiStakeHolderDao();

    public static volatile OFISRoomDB INSTANCE = null;

    public static OFISRoomDB getDbInstance(Context context) {

        final Migration MIGRATION_12_13 = new Migration(12, 13) {
            @Override
            public void migrate(SupportSQLiteDatabase database) {
                database.execSQL("ALTER TABLE DmComplaintFileTable ADD COLUMN fileSize INTEGER default 0");
                database.execSQL("CREATE TABLE IF NOT EXISTS PhotoCheckBoxData ( id INTEGER PRIMARY KEY NOT NULL, questionId TEXT, lookupId TEXT, fileName TEXT, isDownload INTEGER DEFAULT 0 NOT NULL, photoLocation TEXT, active INTEGER NOT NULL, moduleId TEXT)");
            }
        };

        final Migration MIGRATION_13_14 = new Migration(13, 14) {
            @Override
            public void migrate(SupportSQLiteDatabase database) {
                database.execSQL("ALTER TABLE DmMainModule ADD COLUMN diagnosticTypeId INTEGER");
                database.execSQL("ALTER TABLE DmMainModule ADD COLUMN plotSurveyType INTEGER");
                database.execSQL("ALTER TABLE DmMainModule ADD COLUMN captureLocation TEXT");
                database.execSQL("ALTER TABLE DmSurveyAnswerTable ADD COLUMN plotId INTEGER DEFAULT 0");
                database.execSQL("ALTER TABLE DmComplaintFileTable ADD COLUMN plotId INTEGER DEFAULT 0");
                database.execSQL("ALTER TABLE DmActionPlanCurrenciesTable ADD COLUMN plotId INTEGER DEFAULT 0");
                database.execSQL("ALTER TABLE DMSurveyReport ADD COLUMN plotId INTEGER DEFAULT 0");
                database.execSQL("CREATE TABLE IF NOT EXISTS DmMasterData ( uid INTEGER PRIMARY KEY NOT NULL, id INTEGER, type TEXT, value TEXT)");
            }
        };

        final Migration MIGRATION_14_15 = new Migration(14, 15) {
            @Override
            public void migrate(SupportSQLiteDatabase database) {
                database.execSQL("CREATE TABLE IF NOT EXISTS DmModuleAssignments ( uid INTEGER PRIMARY KEY NOT NULL, moduleId INTEGER, assignmentType TEXT, assignmentId INTEGER)");
            }
        };

        final Migration MIGRATION_15_16 = new Migration(15, 16) {
            @Override
            public void migrate(SupportSQLiteDatabase database) {
                database.execSQL("CREATE TABLE IF NOT EXISTS MultiStakeHolder (" +
                "msId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                        "msTempId INTEGER," +
                        "farmerId INTEGER," +
                        "msName TEXT," +
                        "farmHoldingName TEXT," +
                        "farmId TEXT," +
                        "msGender TEXT," +
                        "age TEXT," +
                        "msPhoto TEXT," +
                        "msDOB TEXT," +
                        "consentFarmer INTEGER," +
                        "consentOthers INTEGER," +
                        "msSignature TEXT," +
                        "msDisplayId TEXT," +
                        "olamMultiStakeholderId TEXT," +
                        "farmHoldingCode TEXT," +
                        "msRoleId TEXT," +
                        "synced INTEGER," +
                        "primary_lat TEXT," +
                        "primary_lng TEXT," +
                        "registeredDate TEXT," +
                        "isConsentAvailable TEXT," +
                        "submitted_modules TEXT," +
                        "created_locally INTEGER" +
                        ")");
            }
        };

        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(), OFISRoomDB.class, DATABASE_NAME)
                    .allowMainThreadQueries()
                    .addMigrations(MIGRATION_12_13, MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return INSTANCE;
    }
}