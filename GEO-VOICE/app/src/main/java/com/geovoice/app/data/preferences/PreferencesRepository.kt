package com.geovoice.app.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.geovoice.app.core.location.BatteryProfile
import com.geovoice.app.domain.voice.AnnouncementMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "geovoice_preferences")

/**
 * Préférences persistées de l'utilisateur (section 55). Aucune donnée n'est envoyée
 * en ligne : tout reste local sur l'appareil (DataStore chiffré par le sandboxing Android).
 */
class PreferencesRepository(private val context: Context) {

    private object Keys {
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val VOICE_IS_MALE = booleanPreferencesKey("voice_is_male")
        val VOICE_SPEED = floatPreferencesKey("voice_speed")
        val VOICE_VOLUME = floatPreferencesKey("voice_volume")
        val ANNOUNCEMENT_MODE = stringPreferencesKey("announcement_mode")
        val CUSTOM_THRESHOLD_METERS = longPreferencesKey("custom_threshold_meters")
        val BATTERY_PROFILE = stringPreferencesKey("battery_profile")
        val HISTORY_ENABLED = booleanPreferencesKey("history_enabled")
        val SILENT_MODE = booleanPreferencesKey("silent_mode")
        val WAS_TRACKING_ACTIVE = booleanPreferencesKey("was_tracking_active")
        val ANNOUNCE_LEVEL_MASK = intPreferencesKey("announce_level_mask")
    }

    val onboardingDone: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.ONBOARDING_DONE] ?: false }

    suspend fun setOnboardingDone(done: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDING_DONE] = done }
    }

    val voiceIsMale: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.VOICE_IS_MALE] ?: true }

    suspend fun setVoiceIsMale(isMale: Boolean) {
        context.dataStore.edit { it[Keys.VOICE_IS_MALE] = isMale }
    }

    val voiceSpeed: Flow<Float> =
        context.dataStore.data.map { it[Keys.VOICE_SPEED] ?: 1.0f }

    suspend fun setVoiceSpeed(speed: Float) {
        context.dataStore.edit { it[Keys.VOICE_SPEED] = speed }
    }

    val voiceVolume: Flow<Float> =
        context.dataStore.data.map { it[Keys.VOICE_VOLUME] ?: 1.0f }

    suspend fun setVoiceVolume(volume: Float) {
        context.dataStore.edit { it[Keys.VOICE_VOLUME] = volume }
    }

    val announcementMode: Flow<AnnouncementMode> =
        context.dataStore.data.map {
            AnnouncementMode.valueOf(it[Keys.ANNOUNCEMENT_MODE] ?: AnnouncementMode.STANDARD.name)
        }

    suspend fun setAnnouncementMode(mode: AnnouncementMode) {
        context.dataStore.edit { it[Keys.ANNOUNCEMENT_MODE] = mode.name }
    }

    val batteryProfile: Flow<BatteryProfile> =
        context.dataStore.data.map {
            BatteryProfile.valueOf(it[Keys.BATTERY_PROFILE] ?: BatteryProfile.NORMAL.name)
        }

    suspend fun setBatteryProfile(profile: BatteryProfile) {
        context.dataStore.edit { it[Keys.BATTERY_PROFILE] = profile.name }
    }

    val historyEnabled: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.HISTORY_ENABLED] ?: true }

    suspend fun setHistoryEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.HISTORY_ENABLED] = enabled }
    }

    val silentMode: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.SILENT_MODE] ?: false }

    suspend fun setSilentMode(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SILENT_MODE] = enabled }
    }

    /** Utilisé uniquement pour proposer une reprise après redémarrage — jamais pour relancer seul (section 41). */
    val wasTrackingActive: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.WAS_TRACKING_ACTIVE] ?: false }

    suspend fun setWasTrackingActive(active: Boolean) {
        context.dataStore.edit { it[Keys.WAS_TRACKING_ACTIVE] = active }
    }
}
