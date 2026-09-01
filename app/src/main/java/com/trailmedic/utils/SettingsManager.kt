package com.trailmedic.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "trailmedic_settings")

@Singleton
class SettingsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val KEY_IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
        val KEY_EMERGENCY_CONTACT_NAME = stringPreferencesKey("emergency_contact_name")
        val KEY_EMERGENCY_CONTACT_PHONE = stringPreferencesKey("emergency_contact_phone")
        val KEY_TTS_ENABLED = booleanPreferencesKey("tts_enabled")
        val KEY_TTS_SPEECH_RATE = floatPreferencesKey("tts_speech_rate")
        val KEY_TEXT_SIZE = stringPreferencesKey("text_size") // "Normal", "Large", "Extra Large"
        val KEY_KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
        val KEY_WIFI_ONLY = booleanPreferencesKey("wifi_only")
        val KEY_USE_LLM = booleanPreferencesKey("use_llm")
    }

    val isFirstLaunch: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_IS_FIRST_LAUNCH] ?: true
    }

    val useLLM: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_USE_LLM] ?: true
    }

    val emergencyContactName: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_EMERGENCY_CONTACT_NAME] ?: ""
    }

    val emergencyContactPhone: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_EMERGENCY_CONTACT_PHONE] ?: ""
    }

    val isTTSEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_TTS_ENABLED] ?: true
    }

    val ttsSpeechRate: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[KEY_TTS_SPEECH_RATE] ?: 0.85f
    }

    val textSize: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_TEXT_SIZE] ?: "Normal"
    }

    val keepScreenOn: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_KEEP_SCREEN_ON] ?: true
    }

    val wifiOnly: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_WIFI_ONLY] ?: true
    }

    suspend fun setFirstLaunchCompleted() {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_FIRST_LAUNCH] = false
        }
    }

    suspend fun setEmergencyContact(name: String, phone: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_EMERGENCY_CONTACT_NAME] = name
            prefs[KEY_EMERGENCY_CONTACT_PHONE] = phone
        }
    }

    suspend fun setTTSEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TTS_ENABLED] = enabled
        }
    }

    suspend fun setTTSSpeechRate(rate: Float) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TTS_SPEECH_RATE] = rate
        }
    }

    suspend fun setTextSize(size: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TEXT_SIZE] = size
        }
    }

    suspend fun setKeepScreenOn(keep: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_KEEP_SCREEN_ON] = keep
        }
    }

    suspend fun setWifiOnly(wifiOnly: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_WIFI_ONLY] = wifiOnly
        }
    }

    suspend fun setUseLLM(useLLM: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USE_LLM] = useLLM
        }
    }
}
