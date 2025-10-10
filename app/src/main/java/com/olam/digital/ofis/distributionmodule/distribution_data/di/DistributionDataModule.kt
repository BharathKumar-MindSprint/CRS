package com.olam.digital.ofis.distributionmodule.distribution_data.di

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.olam.digital.ofis.Utilities
import com.olam.digital.ofis.distributionmodule.distribution_data.repository.DistributionModuleRepositoryImpl
import com.olam.digital.ofis.distributionmodule.distribution_domain.repository.DistributionModuleRepository
import com.olam.digital.ofis.mvvmmodule.roomdb.OFISRoomDB
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DistributionDataModule {

    @Provides
    fun provideSQLiteDatabase(@ApplicationContext context : Context) : SQLiteDatabase {
        return Utilities.getWriteableDb(context)
    }

    @Provides
    fun provideRoomDataBase(@ApplicationContext context: Context) : OFISRoomDB {
        return OFISRoomDB.getDbInstance(context)
    }

    @Provides
    fun provideDistributionModuleRepository(
        sqLiteDatabase: SQLiteDatabase,
        roomDb: OFISRoomDB
    ) : DistributionModuleRepository {
        return DistributionModuleRepositoryImpl(sqLiteDatabase, roomDb)
    }
}