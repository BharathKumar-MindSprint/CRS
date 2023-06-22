package com.olam.digital.ofis.mvvmmodule.roomdb;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.ComplianceIndicator;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DMSurveyReport;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmActionPlanCurrenciesTable;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmActionPlansItem;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmActionPlansItemSubmodule;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmAnswerTypes;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmComplaintFileTable;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmCurrenciesItem;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmCurrency;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmLookupValuesISubmodule;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmLookupValuesItem;
import com.olam.digital.ofis.mvvmmodule.roomdb.dbpojo.DmMainModule;
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


@Database(entities ={DmMainModule.class, ComplianceIndicator.class, DmCurrency.class, DmAnswerTypes.class,
        DmQuestionTypes.class, DmCurrenciesItem.class, DmQuestionsModule.class, DmLookupValuesItem.class,
        DmActionPlansItem.class, DmSectionsItem.class, DmQuestionsSubModule.class, DmLookupValuesISubmodule.class,
        DmActionPlansItemSubmodule.class, DmRecurrence.class, DmComplaintFileTable.class,
        DmSurveyAnswerTable.class, DmTextBoxModuleQuestions.class, DmRangeTextBoxModuleQuestionsTable.class,
        DmNonComplaintFileTable.class, DmSatisfactoryFileTable.class, DmActionPlanCurrenciesTable.class,
        DmRangeActionPlansItems.class, DMSurveyReport.class,  FollowUpItemMainTable.class,
        CapturedProofFollowupTable.class, ExistingModuleItemFollowupTable.class, FollowUpAnswerTable.class,
        FollowUpResultTable.class},
        version = 3)
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
    public abstract DmQuestionsSubModuleDao dmQuestionsSubModuleDao();
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

    public static volatile OFISRoomDB INSTANCE = null;

    public static OFISRoomDB getDbInstance(Context context) {

        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(), OFISRoomDB.class, DATABASE_NAME)
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return INSTANCE;
    }
}