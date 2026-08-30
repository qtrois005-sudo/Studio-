package com.sleepaudio.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "sleep_audio_prefs")

/**
 * Persiste uniquement des préférences d'usage (pas de données personnelles) :
 * dernier audio, dernière durée, dernier mode de minuterie, préférence de fondu.
 */
class PreferencesRepository(private val context: Context) {

    private object Keys {
        val LAST_AUDIO_URI = stringPreferencesKey("last_audio_uri")
        val LAST_AUDIO_NAME = stringPreferencesKey("last_audio_name")
        val LAST_DURATION_MS = longPreferencesKey("last_duration_ms")
        val LAST_TIMER_MODE = stringPreferencesKey("last_timer_mode")
        val FADE_OUT_SECONDS = intPreferencesKey("fade_out_seconds")
    }

    val lastAudioUri: Flow<String?> =
        context.dataStore.data.map { it[Keys.LAST_AUDIO_URI] }

    val lastAudioName: Flow<String?> =
        context.dataStore.data.map { it[Keys.LAST_AUDIO_NAME] }

    val lastDurationMs: Flow<Long?> =
        context.dataStore.data.map { it[Keys.LAST_DURATION_MS] }

    val fadeOutSeconds: Flow<Int> =
        context.dataStore.data.map { it[Keys.FADE_OUT_SECONDS] ?: 0 }

    suspend fun saveLastAudio(uri: String, displayName: String) {
        context.dataStore.edit {
            it[Keys.LAST_AUDIO_URI] = uri
            it[Keys.LAST_AUDIO_NAME] = displayName
        }
    }

    suspend fun saveLastDuration(durationMs: Long) {
        context.dataStore.edit { it[Keys.LAST_DURATION_MS] = durationMs }
    }

    suspend fun saveFadeOutSeconds(seconds: Int) {
        context.dataStore.edit { it[Keys.FADE_OUT_SECONDS] = seconds }
    }
}
