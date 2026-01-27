package com.olam.digital.crs.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        CRSQuestionnaireDataEntity::class,
        FarmerEntity::class,
        CRSModuleEntity::class,
        ModuleAssignmentEntity::class,
        CRSResponseTypeDTO::class,
        CRSQuestionEntity::class,
        FarmerModuleEntity::class,
        CrsQuestionModuleXref::class,
        CrsConsent::class,
        CrsAnswerEntity::class,
        CrsSurveyInstance::class,
        CrsOptionEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CRSDatabase : RoomDatabase() {
    abstract fun crsDao(): CRSDao


    companion object {
        @Volatile
        private var INSTANCE: CRSDatabase? = null

        @JvmStatic
        fun getInstance(context: Context): CRSDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    CRSDatabase::class.java,
                    "crs.db"
                ).fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}

