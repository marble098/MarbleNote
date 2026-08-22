package com.marble.shamsa.ui.viewmodel

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.marble.shamsa.core.cloud.DriveSyncManager
import com.marble.shamsa.core.cloud.SyncResult
import com.marble.shamsa.core.data.AppSettings
import com.marble.shamsa.core.data.ReminderRepository
import com.marble.shamsa.core.data.SettingsStore
import com.marble.shamsa.core.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DriveUiState(
    val working: Boolean = false,
    val message: String? = null,
    val successful: Boolean = false,
    val oauthIdentity: String = ""
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: ReminderRepository,
    private val settingsStore: SettingsStore,
    private val drive: DriveSyncManager
) : ViewModel() {
    val reminders = repository.reminders.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val categories = repository.categories.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val notes = repository.notes.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val settings = settingsStore.settings.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        AppSettings()
    )

    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    private val _filter = MutableStateFlow(ReminderFilter.ALL)
    val filter = _filter.asStateFlow()

    private val _sort = MutableStateFlow(ReminderSort.DUE)
    val sort = _sort.asStateFlow()

    private val _syncState = MutableStateFlow<SyncResult?>(null)
    val syncState = _syncState.asStateFlow()

    private fun driveState(
        working: Boolean = false,
        message: String? = null,
        successful: Boolean = false
    ) = DriveUiState(
        working = working,
        message = message,
        successful = successful,
        oauthIdentity = drive.oauthIdentitySummary()
    )

    private val _driveUi = MutableStateFlow(driveState())
    val driveUi = _driveUi.asStateFlow()

    fun setQuery(v: String) {
        _query.value = v
    }

    fun setFilter(v: ReminderFilter) {
        _filter.value = v
    }

    fun setSort(v: ReminderSort) {
        _sort.value = v
    }

    fun saveReminder(v: Reminder) =
        viewModelScope.launch { repository.saveReminder(v) }

    fun complete(id: String) =
        viewModelScope.launch { repository.complete(id) }

    fun cancel(id: String) =
        viewModelScope.launch { repository.cancel(id) }

    fun delete(id: String) =
        viewModelScope.launch { repository.delete(id) }

    fun saveCategory(v: Category) =
        viewModelScope.launch { repository.saveCategory(v) }

    fun saveNote(v: Note) =
        viewModelScope.launch { repository.saveNote(v) }

    fun deleteNote(id: String) =
        viewModelScope.launch { repository.deleteNote(id) }

    suspend fun getReminder(id: String) = repository.get(id)

    suspend fun getNote(id: String) = repository.getNote(id)

    fun setLanguage(v: String) =
        viewModelScope.launch { settingsStore.setLanguage(v) }

    fun setTheme(v: ThemeMode) =
        viewModelScope.launch { settingsStore.setTheme(v) }

    fun setDisplay(v: DisplayMode) =
        viewModelScope.launch { settingsStore.setDisplay(v) }

    fun setCountdownStyle(v: CountdownStyle) =
        viewModelScope.launch { settingsStore.setCountdownStyle(v) }

    fun setPopup(v: Boolean) =
        viewModelScope.launch { settingsStore.setPopup(v) }

    fun finishOnboarding() =
        viewModelScope.launch {
            settingsStore.setOnboardingComplete(true)
        }

    fun driveAuthorizationStarted() {
        _driveUi.value = driveState(working = true)
    }

    fun driveAuthorizationCancelled() {
        _driveUi.value = driveState(
            message = "Google Drive authorization was cancelled by the user."
        )
    }

    fun driveAuthorizationFailed(error: Throwable) {
        _driveUi.value = driveState(
            message = drive.describeAuthorizationError(error)
        )
    }

    suspend fun beginDriveAuthorization(
        activity: Activity
    ): AuthorizationResult =
        drive.beginAuthorization(activity)

    fun authorizationFromIntent(
        activity: Activity,
        data: Intent?
    ): AuthorizationResult? =
        drive.authorizationFromIntent(activity, data)

    fun acceptAuthorization(result: AuthorizationResult) =
        viewModelScope.launch {
            _driveUi.value = driveState(working = true)
            presentDriveResult(drive.acceptAuthorization(result))
        }

    fun syncNow() = viewModelScope.launch {
        _driveUi.value = driveState(working = true)
        presentDriveResult(drive.syncCached())
    }

    fun disconnectDrive() = viewModelScope.launch {
        _driveUi.value = driveState(working = true)
        try {
            drive.disconnect()
            _syncState.value = null
            _driveUi.value = driveState(
                message = "Google Drive disconnected."
            )
        } catch (e: CancellationException) {
            throw e
        } catch (t: Throwable) {
            _driveUi.value = driveState(
                message = drive.describeAuthorizationError(t)
            )
        }
    }

    private fun presentDriveResult(result: SyncResult) {
        _syncState.value = result
        _driveUi.value = when (result) {
            SyncResult.Success -> driveState(
                message = "Google Drive is connected; reminders and notes are synced.",
                successful = true
            )

            SyncResult.NeedsAuthorization -> driveState(
                message = "Google Drive authorization is required. Tap Connect / Reconnect."
            )

            is SyncResult.Failure -> driveState(
                message = result.message
            )
        }
    }
}
