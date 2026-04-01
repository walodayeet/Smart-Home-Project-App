package com.example.smarthomedemo2.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smarthomedemo2.data.UserPreferences
import com.example.smarthomedemo2.data.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: UserPreferencesRepository) : ViewModel() {

    val uiState: StateFlow<UserPreferences> = repository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    private fun isOwnerVerified(): Boolean = uiState.value.isOwnerAuthenticated

    fun updateDoorLockTimer(seconds: Int) {
        if (!isOwnerVerified()) return
        viewModelScope.launch {
            repository.updateDoorLockTimer(seconds)
        }
    }

    fun toggleAutomaticLocking(isEnabled: Boolean) {
        if (!isOwnerVerified()) return
        viewModelScope.launch {
            repository.updateAutomaticLocking(isEnabled)
        }
    }

    fun updateDarkTheme(isDark: Boolean?) {
        if (!isOwnerVerified()) return
        viewModelScope.launch {
            repository.updateDarkTheme(isDark)
        }
    }

    class Factory(private val repository: UserPreferencesRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SettingsViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
