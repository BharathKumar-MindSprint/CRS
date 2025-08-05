package com.olam.digital.ofis.mvvmmodule.roomdb;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.olam.digital.ofis.agrisupplier.dao.SupplierDao;
import com.olam.digital.ofis.agrisupplier.dao.SupplierGroupDao;
import com.olam.digital.ofis.agrisupplier.dao.SupplierSubmittedModuleDao;
import com.olam.digital.ofis.agrisupplier.dao.SupplierTypeDao;
import com.olam.digital.ofis.agrisupplier.entity.Supplier;
import com.olam.digital.ofis.agrisupplier.entity.SupplierGroup;
import com.olam.digital.ofis.agrisupplier.entity.SupplierSubmittedModules;
import com.olam.digital.ofis.agrisupplier.entity.SupplierType;
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


@Database(entities =
        {
        DmMainModule.class, ComplianceIndicator.class, DmCurrency.class, DmAnswerTypes.class,
        DmQuestionTypes.class, DmCurrenciesItem.class, DmQuestionsModule.class, DmLookupValuesItem.class,
        DmSectionsItem.class, DmRecurrence.class, DmComplaintFileTable.class,
        DmSurveyAnswerTable.class, DmTextBoxModuleQuestions.class, DmRangeTextBoxModuleQuestionsTable.class,
        DmActionPlanCurrenciesTable.class, DmRangeActionPlansItems.class, DMSurveyReport.class,
        FollowUpItemMainTable.class, FollowUpAnswerTable.class,
        DmAssessmentResultType.class, Certifications.class, ValidationOnRange.class,
        Partners.class, Programme.class, GlobalUnit.class, LocalUnit.class, PolygonHistoryData.class, PolygonPlotHistory.class, AssignmentType.class, SurveyDurationType.class, PhotoCheckBoxData.class,
        DmMasterData.class, DmModuleAssignments.class, MultiStakeHolder.class, OnGroundSurveyNotification.class, Supplier.class, SupplierGroup.class, SupplierType.class, SupplierSubmittedModules.class},
        version = 19,
        autoMigrations = {@AutoMigration(from = 18, to = 19)}
        )

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
    public abstract SupplierTypeDao supplierTypeDao();
    public abstract SupplierDao supplierDao();
    public abstract SupplierGroupDao supplierGroupDao();
    public abstract SupplierSubmittedModuleDao supplierSubmittedModuleDao();

    public static volatile OFISRoomDB INSTANCE = null;


    public static OFISRoomDB getDbInstance(Context context) {
        final Migration MIGRATION_16_17 = new Migration(16, 17) {
            @Override
            public void migrate(SupportSQLiteDatabase database) {

                database.execSQL("ALTER TABLE DmQuestionsModule ADD COLUMN maxSelection INTEGER");
                database.execSQL("ALTER TABLE DmSurveyAnswerTable ADD COLUMN displayType TEXT");

            }
        };

        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(), OFISRoomDB.class, DATABASE_NAME)
                    .allowMainThreadQueries()
                    .addMigrations(MIGRATION_16_17)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return INSTANCE;
    }
}