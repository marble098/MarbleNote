package com.marble.shamsa.core.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.marble.shamsa.core.model.DisplayMode
import com.marble.shamsa.core.model.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore("shamsa_settings")

data class AppSettings(
    val onboardingComplete: Boolean = false,
    val language: String = "fa",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val displayMode: DisplayMode = DisplayMode.CARDS,
    val popupReminders: Boolean = true,
    val driveConnected: Boolean = false
)

@Singleton
class SettingsStore @Inject constructor(@ApplicationContext private val context: Context) {
    private object Keys {
        val onboarding = booleanPreferencesKey("onboarding")
        val language = stringPreferencesKey("language")
        val theme = stringPreferencesKey("theme")
        val display = stringPreferencesKey("display")
        val popup = booleanPreferencesKey("popup")
        val driveConnected = booleanPreferencesKey("drive_connected")
        val driveToken = stringPreferencesKey("drive_token")
        val driveTokenAt = longPreferencesKey("drive_token_at")
        val deviceId = stringPreferencesKey("device_id")
        val lastSync = longPreferencesKey("last_sync")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { p ->
        AppSettings(
            onboardingComplete = p[Keys.onboarding] ?: false,
            language = p[Keys.language] ?: "fa",
            themeMode = runCatching {
                ThemeMode.valueOf(p[Keys.theme] ?: ThemeMode.SYSTEM.name)
            }.getOrDefault(ThemeMode.SYSTEM),
            displayMode = runCatching {
                DisplayMode.valueOf(p[Keys.display] ?: DisplayMode.CARDS.name)
            }.getOrDefault(DisplayMode.CARDS),
            popupReminders = p[Keys.popup] ?: true,
            driveConnected = p[Keys.driveConnected] ?: false
        )
    }

    suspend fun setOnboardingComplete(v: Boolean) =
        context.dataStore.edit { it[Keys.onboarding] = v }

    suspend fun setLanguage(v: String) =
        context.dataStore.edit { it[Keys.language] = v }

    suspend fun setTheme(v: ThemeMode) =
        context.dataStore.edit { it[Keys.theme] = v.name }

    suspend fun setDisplay(v: DisplayMode) =
        context.dataStore.edit { it[Keys.display] = v.name }

    suspend fun setPopup(v: Boolean) =
        context.dataStore.edit { it[Keys.popup] = v }

    suspend fun saveDriveToken(token: String) = context.dataStore.edit {
        it[Keys.driveToken] = token
        it[Keys.driveTokenAt] = System.currentTimeMillis()
        it[Keys.driveConnected] = true
    }

    suspend fun cachedDriveToken(): String? {
        val p = context.dataStore.data.first()
        val token = p[Keys.driveToken]
        val at = p[Keys.driveTokenAt] ?: 0L
        return token?.takeIf { System.currentTimeMillis() - at < 45 * 60_000L }
    }

    suspend fun storedDriveToken(): String? =
        context.dataStore.data.first()[Keys.driveToken]

    suspend fun clearDrive() = context.dataStore.edit {
        it.remove(Keys.driveToken)
        it.remove(Keys.driveTokenAt)
        it[Keys.driveConnected] = false
    }

    suspend fun deviceId(): String {
        var result = ""
        context.dataStore.edit { p ->
            result = p[Keys.deviceId]
                ?: UUID.randomUUID().toString().also { p[Keys.deviceId] = it }
        }
        return result
    }

    suspend fun markSynced() =
        context.dataStore.edit { it[Keys.lastSync] = System.currentTimeMillis() }
}
