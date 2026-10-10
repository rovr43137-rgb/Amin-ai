package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.aetherDataStore by preferencesDataStore(name = "aether_preferences")

class SettingsRepository(private val context: Context) {

    private object Keys {
        val PROVIDER = stringPreferencesKey("provider")
        val GEMINI_KEY = stringPreferencesKey("gemini_key")
        val OPENROUTER_KEY = stringPreferencesKey("openrouter_key")
        val CUSTOM_BASE_URL = stringPreferencesKey("custom_base_url")
        val CUSTOM_KEY = stringPreferencesKey("custom_key")
        val CUSTOM_MODEL = stringPreferencesKey("custom_model")
        val DIALECT = stringPreferencesKey("dialect")
        val INTERVAL_SEC = intPreferencesKey("interval_sec")
        val SPEECH_RATE = floatPreferencesKey("speech_rate")
        val SPEECH_PITCH = floatPreferencesKey("speech_pitch")
        val SPOKEN_ENABLED = booleanPreferencesKey("spoken_enabled")
        val AUTO_NARRATE = booleanPreferencesKey("auto_narrate")
        val CONSENT_ACCEPTED = booleanPreferencesKey("consent_accepted")
    }

    val settingsFlow: Flow<AetherSettings> = context.aetherDataStore.data.map { prefs ->
        val providerStr = prefs[Keys.PROVIDER] ?: AiProvider.GEMINI.id
        val provider = AiProvider.values().find { it.id == providerStr } ?: AiProvider.GEMINI

        val dialectStr = prefs[Keys.DIALECT] ?: ArabicDialect.MSA.id
        val dialect = ArabicDialect.values().find { it.id == dialectStr } ?: ArabicDialect.MSA

        AetherSettings(
            provider = provider,
            geminiCustomApiKey = prefs[Keys.GEMINI_KEY] ?: "",
            openRouterApiKey = prefs[Keys.OPENROUTER_KEY] ?: "",
            customBaseUrl = prefs[Keys.CUSTOM_BASE_URL] ?: "https://api.openai.com/v1/",
            customApiKey = prefs[Keys.CUSTOM_KEY] ?: "",
            customModel = prefs[Keys.CUSTOM_MODEL] ?: "gpt-4o-mini",
            dialect = dialect,
            liveStreamIntervalSec = prefs[Keys.INTERVAL_SEC] ?: 6,
            speechRate = prefs[Keys.SPEECH_RATE] ?: 1.05f,
            speechPitch = prefs[Keys.SPEECH_PITCH] ?: 1.0f,
            spokenResponseEnabled = prefs[Keys.SPOKEN_ENABLED] ?: true,
            isAutoNarrateEnabled = prefs[Keys.AUTO_NARRATE] ?: true,
            hasAcceptedConsent = prefs[Keys.CONSENT_ACCEPTED] ?: false
        )
    }

    suspend fun updateAutoNarrate(enabled: Boolean) {
        context.aetherDataStore.edit { prefs ->
            prefs[Keys.AUTO_NARRATE] = enabled
        }
    }

    suspend fun updateProvider(provider: AiProvider) {
        context.aetherDataStore.edit { prefs ->
            prefs[Keys.PROVIDER] = provider.id
        }
    }

    suspend fun updateDialect(dialect: ArabicDialect) {
        context.aetherDataStore.edit { prefs ->
            prefs[Keys.DIALECT] = dialect.id
        }
    }

    suspend fun updateIntervalSec(seconds: Int) {
        context.aetherDataStore.edit { prefs ->
            prefs[Keys.INTERVAL_SEC] = seconds
        }
    }

    suspend fun updateApiKeys(geminiKey: String, openRouterKey: String, customBaseUrl: String, customKey: String, customModel: String) {
        context.aetherDataStore.edit { prefs ->
            prefs[Keys.GEMINI_KEY] = geminiKey.trim()
            prefs[Keys.OPENROUTER_KEY] = openRouterKey.trim()
            prefs[Keys.CUSTOM_BASE_URL] = customBaseUrl.trim()
            prefs[Keys.CUSTOM_KEY] = customKey.trim()
            prefs[Keys.CUSTOM_MODEL] = customModel.trim()
        }
    }

    suspend fun updateSpeechSettings(rate: Float, pitch: Float, spokenEnabled: Boolean) {
        context.aetherDataStore.edit { prefs ->
            prefs[Keys.SPEECH_RATE] = rate
            prefs[Keys.SPEECH_PITCH] = pitch
            prefs[Keys.SPOKEN_ENABLED] = spokenEnabled
        }
    }

    suspend fun acceptConsent() {
        context.aetherDataStore.edit { prefs ->
            prefs[Keys.CONSENT_ACCEPTED] = true
        }
    }
}
