package com.olam.digital.ofis.mvvmmodule.roomdb;

import android.content.Context;

import androidx.annotation.NonNull;
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
import com.olam.digital.ofis.mvvmmodule.roomdb.Dao.OnGroundSurveyNotificationDao;
import com.olam.digital.ofis.mvvmmodule.roomdb.Dao.SurveyDurationTypeDao;
import com.olam.digital.ofis.mvvmmodule.roomdb.Dao.ValidationOnRangeDao;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.Certifications;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.ComplianceIndicator;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DMSurveyReport;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmActionPlanCurrenciesTable;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmAnswerTypes;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmAssessmentResultType;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmComplaintFileTable;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmCurrenciesItem;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmCurrency;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmLookupValuesItem;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmMainModule;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmMasterData;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmModuleAssignments;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmQuestionTypes;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmQuestionsModule;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmRangeActionPlansItems;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmRangeTextBoxModuleQuestionsTable;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmRecurrence;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmSectionsItem;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmSurveyAnswerTable;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.DmTextBoxModuleQuestions;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.OnGroundSurveyNotification;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.Partners;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.Programme;
import com.olam.digital.ofis.mvvmmodule.roomdb.followuptable.FollowUpAnswerTable;
import com.olam.digital.ofis.mvvmmodule.roomdb.followuptable.FollowUpItemMainTable;
import com.olam.digital.ofis.mvvmmodule.roomdb.followuptable.daos.FollowUpItemMainTableDao;
import com.olam.digital.ofis.recordpolygon.polygonhistory.roomdbpolygon.PolygonHistoryData;
import com.olam.digital.ofis.recordpolygon.polygonhistory.roomdbpolygon.PolygonPlotHistory;
import com.olam.digital.ofis.recordpolygon.polygonhistory.roomdbpolygon.dao.PolygonHistoryDataDao;
import com.olam.digital.ofis.recordpolygon.polygonhistory.roomdbpolygon.dao.PolygonPlotHistoryDao;


@Database(entities ={DmMainModule.class, ComplianceIndicator.class, DmCurrency.class, DmAnswerTypes.class,
        DmQuestionTypes.class, DmCurrenciesItem.class, DmQuestionsModule.class, DmLookupValuesItem.class,
        DmSectionsItem.class, DmRecurrence.class, DmComplaintFileTable.class,
        DmSurveyAnswerTable.class, DmTextBoxModuleQuestions.class, DmRangeTextBoxModuleQuestionsTable.class,
        DmActionPlanCurrenciesTable.class, DmRangeActionPlansItems.class, DMSurveyReport.class,
        FollowUpItemMainTable.class, FollowUpAnswerTable.class,
        DmAssessmentResultType.class, Certifications.class, ValidationOnRange.class,
        Partners.class, Programme.class, GlobalUnit.class, LocalUnit.class, PolygonHistoryData.class, PolygonPlotHistory.class, AssignmentType.class, SurveyDurationType.class, PhotoCheckBoxData.class,
        DmMasterData.class, DmModuleAssignments.class, MultiStakeHolder.class, OnGroundSurveyNotification.class},
        version = 17)

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
    public abstract DmSectionsItemDao dmSectionsItemDao();
    public abstract DmRecurrenceDao dmRecurrenceDao();
    public abstract DmComplaintFileTableDao dmComplaintFileTableDao();
    public abstract DmSurveyAnswerTableDao dmSurveyAnswerTableDao();
    public abstract DmTextBoxModuleQuestionsDao dmTextBoxModuleQuestionsDao();
    public abstract DmRangeTextBoxModuleQuestionsTableDao dmRangeTextBoxModuleQuestionsTableDao();
    public abstract DmActionPlanCurrenciesTableDao dmActionPlanCurrenciesTableDao();
    public abstract DmRangeActionPlansItemsDao dmRangeActionPlansItemsDao();
    public abstract DMSurveyReportDao dmSurveyReportDao();
    public abstract FollowUpItemMainTableDao followUpItemMainTableDao();


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

    public abstract OnGroundSurveyNotificationDao onGroundSurveyNotificationDao();

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
                database.execSQL("ALTER TABLE DmRangeTextBoxModuleQuestionsTable ADD COLUMN id INTEGER");
                database.execSQL("ALTER TABLE DmRangeActionPlansItems ADD COLUMN actionValueId INTEGER");
                database.execSQL("CREATE TABLE IF NOT EXISTS DmMasterData ( uid INTEGER PRIMARY KEY NOT NULL, id INTEGER, type TEXT, value TEXT)");
                database.execSQL("CREATE TABLE IF NOT EXISTS DmModuleAssignments ( uid INTEGER PRIMARY KEY NOT NULL, moduleId INTEGER, assignmentType TEXT, assignmentId INTEGER)");
            }
        };

        final Migration MIGRATION_14_15 = new Migration(14, 15) {
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
                database.execSQL("DROP TABLE IF EXISTS DmActionPlansItem");
                database.execSQL("DROP TABLE IF EXISTS DmActionPlansItemSubmodule");
                database.execSQL("DROP TABLE IF EXISTS DmLookupValuesISubmodule");
                database.execSQL("DROP TABLE IF EXISTS DmNonComplaintFileTable");
                database.execSQL("DROP TABLE IF EXISTS DmQuestionsSubModule");
                database.execSQL("DROP TABLE IF EXISTS DmSatisfactoryFileTable");
                database.execSQL("DROP TABLE IF EXISTS DmSavePartialSurveyTable");
                database.execSQL("DROP TABLE IF EXISTS CapturedProofFollowupTable");
                database.execSQL("DROP TABLE IF EXISTS ExistingModuleItemFollowupTable");
                database.execSQL("DROP TABLE IF EXISTS FollowUpResultTable");

                database.execSQL("ALTER TABLE DmRangeActionPlansItems ADD COLUMN actionPlanFr TEXT");
                database.execSQL("ALTER TABLE DmRangeActionPlansItems ADD COLUMN actionPlanEs TEXT");
                database.execSQL("ALTER TABLE DmRangeActionPlansItems ADD COLUMN actionPlanPt TEXT");
                database.execSQL("ALTER TABLE DmRangeActionPlansItems ADD COLUMN actionPlanId TEXT");
                database.execSQL("ALTER TABLE DmRangeActionPlansItems ADD COLUMN actionPlanLo TEXT");
                database.execSQL("ALTER TABLE DmRangeActionPlansItems ADD COLUMN actionPlanTh TEXT");
                database.execSQL("ALTER TABLE DmRangeActionPlansItems ADD COLUMN actionPlanTr TEXT");
                database.execSQL("ALTER TABLE DmRangeActionPlansItems ADD COLUMN actionPlanVi TEXT");

            }
        };

        final Migration MIGRATION_15_16 = new Migration(15, 16) {

            @Override
            public void migrate(@NonNull SupportSQLiteDatabase database) {
                database.execSQL("CREATE TABLE IF NOT EXISTS OnGroundSurveyNotification (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "farmerGroupId INTEGER, " +
                        "farmerId INTEGER, " +
                        "farmerName TEXT, " +
                        "photo TEXT, " +
                        "olamFarmerId TEXT, " +
                        "createdOnPhone TEXT, " +
                        "countryId INTEGER, " +
                        "odFarmerId TEXT, " +
                        "plotId INTEGER, " +
                        "sectionId INTEGER, " +
                        "surveyType TEXT)");
            }
        };

        final Migration MIGRATION_16_17 = new Migration(16, 17) {
            @Override
            public void migrate(SupportSQLiteDatabase database) {

                database.execSQL("ALTER TABLE DmQuestionsModule ADD COLUMN maxSelection INTEGER");
                database.execSQL("ALTER TABLE DmSurveyAnswerTable ADD COLUMN displayType STRING");

            }
        };


        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(), OFISRoomDB.class, DATABASE_NAME)
                    .allowMainThreadQueries()
                    .addMigrations(MIGRATION_12_13, MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16, MIGRATION_16_17)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return INSTANCE;
    }
}