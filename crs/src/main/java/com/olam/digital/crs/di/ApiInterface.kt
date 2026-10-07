package com.olam.digital.crs.di

import android.content.Context
import androidx.room.Room
import com.olam.digital.crs.di.endpoints.WebEndPoints
import com.olam.digital.crs.room.CRSDao
import com.olam.digital.crs.room.CRSDatabase
import com.olam.digital.crs.viewmodel.api.CrsApi
import com.olam.digital.crs.viewmodel.repo.CrsHomeRepo
import com.olam.digital.crs.viewmodel.repo.SurveyRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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

    @Provides
    @Singleton
    fun provideCrsHomeRepository(crsDao: CRSDao, webEndPoints: WebEndPoints): CrsHomeRepo {
        return CrsHomeRepo(crsDao, webEndPoints)
    }

    @Singleton
    @Provides
    fun provideCrsRepository(crsDao: CRSDao): SurveyRepository {
        return SurveyRepository(crsDao)
    }

    private  var IMAGE_URL = CrsApi.IMAGE_URL

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(IMAGE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): WebEndPoints {
        return retrofit.create(WebEndPoints::class.java)
    }


}