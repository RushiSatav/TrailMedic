package com.trailmedic.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.trailmedic.utils.BatteryAwareManager
import com.trailmedic.utils.ModelDownloadManager
import com.trailmedic.utils.SettingsManager
import com.trailmedic.utils.TTSManager
import com.trailmedic.utils.VoiceInputManager
import com.trailmedic.utils.dataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context = context

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(45, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideModelDownloadManager(
        @ApplicationContext context: Context
    ): ModelDownloadManager = ModelDownloadManager(context)

    @Provides
    @Singleton
    fun provideVoiceInputManager(
        @ApplicationContext context: Context
    ): VoiceInputManager = VoiceInputManager(context)

    @Provides
    @Singleton
    fun provideTTSManager(
        @ApplicationContext context: Context
    ): TTSManager = TTSManager(context)

    @Provides
    @Singleton
    fun provideBatteryAwareManager(
        @ApplicationContext context: Context
    ): BatteryAwareManager = BatteryAwareManager(context)

    @Provides
    @Singleton
    fun provideSettingsManager(
        @ApplicationContext context: Context
    ): SettingsManager = SettingsManager(context)
}
