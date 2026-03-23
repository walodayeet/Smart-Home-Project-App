package com.example.smarthomedemo2.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

data class UserPreferences(
    val lightStatus: Boolean = false,
    val lockStatus: Boolean = true,
    val curtainStatus: Boolean = false,
    val doorLockTimer: Int = 30, // seconds
    val automaticLocking: Boolean = true,
    val isAlarmArmed: Boolean = false,
    val isAlarmTriggered: Boolean = false,
    val isDarkTheme: Boolean? = null // null means system default
)

class UserPreferencesRepository(private val context: Context) {
    private object PreferencesKeys {
        val LIGHT_STATUS = booleanPreferencesKey("light_status")
        val LOCK_STATUS = booleanPreferencesKey("lock_status")
        val CURTAIN_STATUS = booleanPreferencesKey("curtain_status")
        val DOOR_LOCK_TIMER = intPreferencesKey("door_lock_timer")
        val AUTOMATIC_LOCKING = booleanPreferencesKey("automatic_locking")
        val IS_ALARM_ARMED = booleanPreferencesKey("is_alarm_armed")
        val IS_ALARM_TRIGGERED = booleanPreferencesKey("is_alarm_triggered")
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        val HAS_THEME_PREFERENCE = booleanPreferencesKey("has_theme_preference")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val hasThemePreference = preferences[PreferencesKeys.HAS_THEME_PREFERENCE] ?: false
            UserPreferences(
                lightStatus = preferences[PreferencesKeys.LIGHT_STATUS] ?: false,
                lockStatus = preferences[PreferencesKeys.LOCK_STATUS] ?: true,
                curtainStatus = preferences[PreferencesKeys.CURTAIN_STATUS] ?: false,
                doorLockTimer = preferences[PreferencesKeys.DOOR_LOCK_TIMER] ?: 30,
                automaticLocking = preferences[PreferencesKeys.AUTOMATIC_LOCKING] ?: true,
                isAlarmArmed = preferences[PreferencesKeys.IS_ALARM_ARMED] ?: false,
                isAlarmTriggered = preferences[PreferencesKeys.IS_ALARM_TRIGGERED] ?: false,
                isDarkTheme = if (hasThemePreference) preferences[PreferencesKeys.IS_DARK_THEME] else null
            )
        }

    suspend fun updateLightStatus(isOn: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LIGHT_STATUS] = isOn
        }
    }

    suspend fun updateLockStatus(isLocked: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LOCK_STATUS] = isLocked
        }
    }

    suspend fun updateCurtainStatus(isOpen: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CURTAIN_STATUS] = isOpen
        }
    }

    suspend fun updateDoorLockTimer(seconds: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DOOR_LOCK_TIMER] = seconds
        }
    }

    suspend fun updateAutomaticLocking(isEnabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTOMATIC_LOCKING] = isEnabled
        }
    }

    suspend fun updateAlarmArmed(isArmed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_ALARM_ARMED] = isArmed
        }
    }

    suspend fun updateAlarmTriggered(isTriggered: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_ALARM_TRIGGERED] = isTriggered
        }
    }

    suspend fun updateDarkTheme(isDark: Boolean?) {
        context.dataStore.edit { preferences ->
            if (isDark == null) {
                preferences[PreferencesKeys.HAS_THEME_PREFERENCE] = false
            } else {
                preferences[PreferencesKeys.HAS_THEME_PREFERENCE] = true
                preferences[PreferencesKeys.IS_DARK_THEME] = isDark
            }
        }
    }
}
