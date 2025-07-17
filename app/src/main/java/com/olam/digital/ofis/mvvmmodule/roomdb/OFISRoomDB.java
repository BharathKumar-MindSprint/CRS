package com.olam.digital.ofis.mvvmmodule.roomdb;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.*;
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
import com.olam.digital.ofis.mvvmmodule.roomdb.Dao.*;
import com.olam.digital.ofis.mvvmmodule.roomdb.entity.*;
import com.olam.digital.ofis.mvvmmodule.roomdb.followuptable.FollowUpAnswerTable;
import com.olam.digital.ofis.mvvmmodule.roomdb.followuptable.FollowUpItemMainTable;
import com.olam.digital.ofis.mvvmmodule.roomdb.followuptable.daos.FollowUpItemMainTableDao;


@Database(entities =
        {
        DmMainModule.class, ComplianceIndicator.class, DmCurrency.class, DmAnswerTypes.class,
        DmQuestionTypes.class, DmCurrenciesItem.class, DmQuestionsModule.class, DmLookupValuesItem.class,
        DmSectionsItem.class, DmRecurrence.class, DmComplaintFileTable.class,
        DmSurveyAnswerTable.class, DmTextBoxModuleQuestions.class, DmRangeTextBoxModuleQuestionsTable.class,
        DmActionPlanCurrenciesTable.class, DmRangeActionPlansItems.class, DMSurveyReport.class,
        FollowUpItemMainTable.class, FollowUpAnswerTable.class,
        DmAssessmentResultType.class, Certifications.class, ValidationOnRange.class,
        Partners.class, Programme.class, GlobalUnit.class, LocalUnit.class, AssignmentType.class, SurveyDurationType.class, PhotoCheckBoxData.class,
        DmMasterData.class, DmModuleAssignments.class, MultiStakeHolder.class, OnGroundSurveyNotification.class, Supplier.class, SupplierGroup.class, SupplierType.class, SupplierSubmittedModules.class, ConditionalLogic.class, ConditionalType.class,
        SubQuestionAnswer.class, AllCountries.class, AnswerType.class, Answers.class,
        AnswersSIP.class, Assignments.class, ChildAnswers.class, ChildVisit.class, ChildVisitDetails.class,
        Children.class, CompletionCriteria.class, ConsentStatement.class, Country.class,CourseAssignment.class,
        Courses.class,DataTypes.class,Districts.class,DownloadedSurveys.class,FarmerRelationship.class,
        FarmerChildModules.class,FarmerModules.class, FarmerGroup.class, SubDistricts.class, Regions.class,
        Sections.class, Places.class, Products.class, Farmer.class,
        },
        version = 19,
        autoMigrations ={@AutoMigration(from =18, to=19)}
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
    public abstract CertificationsDao certificationsDao();
    public abstract ValidationOnRangeDao validationOnRangeDao();
    public abstract ProgrammeDao programmeDao();
    public abstract PartnersDao partnersDao();
    public abstract GlobalUnitDao globalUnitDao();
    public abstract LocalUnitDao localUnitDao();
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
    //Migrated from Sqlite
    public abstract ICountryDao countryDao();
    public abstract IRegionsDao regionsDao();
    public abstract IDistrictsDao districtsDao();
    public abstract ISubDistrictsDao subDistrictsDao();
    public abstract IPlacesDao placesDao();
    public abstract IFarmerGroupDao farmerGroupDao();
    public abstract ISectionsDao sectionsDao();
    public abstract IProductsDao productsDao();
    public abstract ConditionalLogicDao conditionalLogicDao();
    public abstract ConditionalTypeDao conditionalTypeDao();

    public static volatile OFISRoomDB INSTANCE = null;


    public static OFISRoomDB getDbInstance(Context context) {

        final Migration MIGRATION_16_17 = new Migration(16, 17) {
            @Override
            public void migrate(SupportSQLiteDatabase database) {
                database.execSQL("ALTER TABLE DmQuestionsModule ADD COLUMN maxSelection INTEGER");
                database.execSQL("ALTER TABLE DmSurveyAnswerTable ADD COLUMN displayType TEXT");
            }
        };

        final Migration MIGRATION_18_19 = new Migration(18, 19) {
            @Override
            public void migrate(@NonNull SupportSQLiteDatabase database) {
                database.execSQL("DROP TABLE IF EXISTS 'PolygonHistoryData'");
            }
        };

        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(), OFISRoomDB.class, DATABASE_NAME)
                    .allowMainThreadQueries()
                    .addMigrations(MIGRATION_16_17, MIGRATION_18_19)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return INSTANCE;
    }
}