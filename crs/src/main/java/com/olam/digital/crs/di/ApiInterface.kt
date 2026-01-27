package com.olam.digital.crs.di

import android.content.Context
import androidx.room.Room
import com.olam.digital.crs.room.CRSDao
import com.olam.digital.crs.room.CRSDatabase
import com.olam.digital.crs.viewmodel.repo.CrsHomeRepo
import com.olam.digital.crs.viewmodel.repo.SurveyRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiInterface {
    @Singleton
    @Provides
    fun crsRoomDatabase(@ApplicationContext context: Context): CRSDatabase {
        return Room.databaseBuilder(context, CRSDatabase::class.java, "crs.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideCrsDao(database: CRSDatabase) = database.crsDao()

    @Singleton
    @Provides
    fun provideCrsHomeRepository(crsDao: CRSDao): CrsHomeRepo
    {
        return CrsHomeRepo(crsDao)
    }

    @Singleton
    @Provides
    fun provideCrsRepository(crsDao: CRSDao): SurveyRepository
    {
        return SurveyRepository(crsDao)
    }



}