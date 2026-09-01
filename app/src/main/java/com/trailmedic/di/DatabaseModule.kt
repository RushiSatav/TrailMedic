package com.trailmedic.di

import android.content.Context
import androidx.room.Room
import com.google.gson.Gson
import com.trailmedic.data.local.AppDatabase
import com.trailmedic.data.local.dao.SessionDao
import com.trailmedic.data.repository.SessionRepositoryImpl
import com.trailmedic.domain.repository.SessionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "trailmedic_database"
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideSessionDao(database: AppDatabase): SessionDao {
        return database.sessionDao()
    }

    @Provides
    @Singleton
    fun provideSessionRepository(
        sessionDao: SessionDao,
        gson: Gson
    ): SessionRepository {
        return SessionRepositoryImpl(sessionDao, gson)
    }
}
