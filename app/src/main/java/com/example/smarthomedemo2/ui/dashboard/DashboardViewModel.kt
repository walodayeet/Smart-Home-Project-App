package com.example.smarthomedemo2.ui.dashboard

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.BatteryManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smarthomedemo2.data.LogRepository
import com.example.smarthomedemo2.data.UserPreferences
import com.example.smarthomedemo2.data.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DashboardViewModel(
    application: Application,
    private val repository: UserPreferencesRepository,
    private val logRepository: LogRepository
) : AndroidViewModel(application) {

    private val context get() = getApplication<Application>()

    val uiState: StateFlow<UserPreferences> = repository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    private val _batteryLevel = MutableStateFlow(0)
    val batteryLevel: StateFlow<Int> = _batteryLevel.asStateFlow()

    private val _isOnline = MutableStateFlow(false)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            if (level != -1 && scale != -1) {
                _batteryLevel.value = (level * 100 / scale.toFloat()).toInt()
            }
        }
    }

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _isOnline.value = true
        }

        override fun onLost(network: Network) {
            _isOnline.value = false
        }
    }

    init {
        viewModelScope.launch {
            // Initial Battery Check (off-thread just in case)
            val batteryStatus = withContext(Dispatchers.IO) {
                context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            }
            val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            if (level != -1 && scale != -1) {
                _batteryLevel.value = (level * 100 / scale.toFloat()).toInt()
            }
            
            context.registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

            // Connectivity Check
            val activeNetwork = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            _isOnline.value = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
            
            val networkRequest = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        }
    }

    fun toggleLights() {
        viewModelScope.launch {
            if (!uiState.value.isOwnerAuthenticated) {
                logRepository.insertLog("Protected action blocked: identity verification required", "Living Room")
                return@launch
            }

            val newState = !uiState.value.lightStatus
            repository.updateLightStatus(newState)
            logRepository.insertLog("Lights turned ${if (newState) "ON" else "OFF"}", "Living Room")
        }
    }

    fun toggleLock() {
        viewModelScope.launch {
            val currentState = uiState.value.lockStatus
            val wantsUnlock = currentState
            if (wantsUnlock && !uiState.value.isOwnerAuthenticated) {
                logRepository.insertLog("Protected action blocked: identity verification required", "Entrance")
                return@launch
            }

            val newState = !currentState
            repository.updateLockStatus(newState)
            logRepository.insertLog("Main Door ${if (newState) "LOCKED" else "UNLOCKED"}", "Entrance")
        }
    }

    fun toggleCurtains() {
        viewModelScope.launch {
            if (!uiState.value.isOwnerAuthenticated) {
                logRepository.insertLog("Protected action blocked: identity verification required", "Windows")
                return@launch
            }

            val newState = !uiState.value.curtainStatus
            repository.updateCurtainStatus(newState)
            logRepository.insertLog("Curtains ${if (newState) "OPENED" else "CLOSED"}", "Bedroom")
        }
    }

    fun toggleAlarm() {
        viewModelScope.launch {
            val currentState = uiState.value.isAlarmArmed
            val wantsDisarm = currentState
            if (wantsDisarm && !uiState.value.isOwnerAuthenticated) {
                logRepository.insertLog("Protected action blocked: identity verification required", "System")
                return@launch
            }

            val newState = !currentState
            repository.updateAlarmArmed(newState)
            logRepository.insertLog("Security Alarm ${if (newState) "ARMED" else "DISARMED"}", "System")
        }
    }

    fun triggerManualAlarm() {
        viewModelScope.launch {
            if (!uiState.value.isOwnerAuthenticated) {
                logRepository.insertLog("Protected action blocked: identity verification required", "Panic Button")
                return@launch
            }

            val newState = !uiState.value.isAlarmTriggered
            repository.updateAlarmTriggered(newState)
            logRepository.insertLog(
                if (newState) "MANUAL ALARM TRIGGERED" else "ALARM DEACTIVATED", 
                "Panic Button"
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        context.unregisterReceiver(batteryReceiver)
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    class Factory(
        private val application: Application, 
        private val repository: UserPreferencesRepository,
        private val logRepository: LogRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return DashboardViewModel(application, repository, logRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
