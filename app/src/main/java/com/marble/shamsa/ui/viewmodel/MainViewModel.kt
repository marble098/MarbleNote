package com.marble.shamsa.ui.viewmodel

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.marble.shamsa.core.cloud.DriveSyncManager
import com.marble.shamsa.core.cloud.SyncResult
import com.marble.shamsa.core.data.*
import com.marble.shamsa.core.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: ReminderRepository,
    private val settingsStore: SettingsStore,
    private val drive: DriveSyncManager
) : ViewModel() {
    val reminders = repository.reminders.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val categories = repository.categories.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val settings = settingsStore.settings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()
    private val _filter = MutableStateFlow(ReminderFilter.ALL)
    val filter = _filter.asStateFlow()
    private val _sort = MutableStateFlow(ReminderSort.DUE)
    val sort = _sort.asStateFlow()
    private val _syncState = MutableStateFlow<SyncResult?>(null)
    val syncState = _syncState.asStateFlow()

    fun setQuery(v: String) { _query.value = v }
    fun setFilter(v: ReminderFilter) { _filter.value = v }
    fun setSort(v: ReminderSort) { _sort.value = v }
    fun saveReminder(v: Reminder) = viewModelScope.launch { repository.saveReminder(v) }
    fun complete(id: String) = viewModelScope.launch { repository.complete(id) }
    fun delete(id: String) = viewModelScope.launch { repository.delete(id) }
    fun saveCategory(v: Category) = viewModelScope.launch { repository.saveCategory(v) }
    suspend fun getReminder(id: String) = repository.get(id)

    fun setLanguage(v: String) = viewModelScope.launch { settingsStore.setLanguage(v) }
    fun setTheme(v: ThemeMode) = viewModelScope.launch { settingsStore.setTheme(v) }
    fun setDisplay(v: DisplayMode) = viewModelScope.launch { settingsStore.setDisplay(v) }
    fun setPopup(v: Boolean) = viewModelScope.launch { settingsStore.setPopup(v) }
    fun finishOnboarding() = viewModelScope.launch { settingsStore.setOnboardingComplete(true) }

    suspend fun beginDriveAuthorization(activity: Activity): AuthorizationResult = drive.beginAuthorization(activity)
    fun authorizationFromIntent(activity: Activity, data: Intent?): AuthorizationResult? = drive.authorizationFromIntent(activity, data)
    fun acceptAuthorization(result: AuthorizationResult) = viewModelScope.launch { _syncState.value = drive.acceptAuthorization(result) }
    fun syncNow() = viewModelScope.launch { _syncState.value = drive.syncCached() }
    fun disconnectDrive() = viewModelScope.launch { drive.disconnect() }
}
