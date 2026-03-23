package com.example.smarthomedemo2.ui.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smarthomedemo2.data.LogEntry
import com.example.smarthomedemo2.data.LogRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LogViewModel(private val logRepository: LogRepository) : ViewModel() {

    val logs: StateFlow<List<LogEntry>> = logRepository.allLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun clearLogs() {
        viewModelScope.launch {
            logRepository.clearLogs()
        }
    }

    class Factory(private val logRepository: LogRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LogViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return LogViewModel(logRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
