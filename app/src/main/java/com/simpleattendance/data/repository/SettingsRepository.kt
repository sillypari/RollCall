package com.simpleattendance.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class UserSettings(
    val theme: String = "system",
    val hapticsEnabled: Boolean = true,
    val numberingMode: String = "relative", // "absolute" or "relative"
    val reportTemplate: String = "detailed" // "detailed" or "compact"
)

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private object Keys {
        val THEME = stringPreferencesKey("theme")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val NUMBERING_MODE = stringPreferencesKey("numbering_mode")
        val REPORT_TEMPLATE = stringPreferencesKey("report_template")
    }
    
    val settings: Flow<UserSettings> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences ->
            UserSettings(
                theme = preferences[Keys.THEME] ?: "system",
                hapticsEnabled = preferences[Keys.HAPTICS_ENABLED] ?: true,
                numberingMode = preferences[Keys.NUMBERING_MODE] ?: "relative",
                reportTemplate = preferences[Keys.REPORT_TEMPLATE] ?: "detailed"
            )
        }
    
    suspend fun setTheme(theme: String) {
        dataStore.edit { it[Keys.THEME] = theme }
    }
    
    suspend fun setHapticsEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.HAPTICS_ENABLED] = enabled }
    }
    
    suspend fun setNumberingMode(mode: String) {
        dataStore.edit { it[Keys.NUMBERING_MODE] = mode }
    }
    
    suspend fun setReportTemplate(template: String) {
        dataStore.edit { it[Keys.REPORT_TEMPLATE] = template }
    }
}
