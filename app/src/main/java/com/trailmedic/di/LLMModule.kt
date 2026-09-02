package com.trailmedic.di

import android.content.Context
import com.google.gson.Gson
import com.trailmedic.data.llm.LLMInferenceEngine
import com.trailmedic.data.repository.ChatRepositoryImpl
import com.trailmedic.data.repository.SymptomTreeRepositoryImpl
import com.trailmedic.domain.repository.ChatRepository
import com.trailmedic.domain.repository.SymptomTreeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LLMModule {

    @Provides
    @Singleton
    fun provideLLMInferenceEngine(
        @ApplicationContext context: Context,
        batteryAwareManager: com.trailmedic.utils.BatteryAwareManager
    ): LLMInferenceEngine = LLMInferenceEngine(context, batteryAwareManager)

    @Provides
    @Singleton
    fun provideChatRepository(
        llmEngine: LLMInferenceEngine
    ): ChatRepository = ChatRepositoryImpl(llmEngine)

    @Provides
    @Singleton
    fun provideSymptomTreeRepository(
        @ApplicationContext context: Context,
        gson: Gson,
        clinicalKnowledgeExtractor: com.trailmedic.domain.ai.ClinicalKnowledgeExtractor
    ): SymptomTreeRepository = SymptomTreeRepositoryImpl(context, gson, clinicalKnowledgeExtractor)
}

