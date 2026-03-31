package com.example.smarthomedemo2.ui.camera

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smarthomedemo2.data.FaceRecognitionRepository
import com.example.smarthomedemo2.data.LogRepository
import com.example.smarthomedemo2.data.UserPreferencesRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CameraUiState(
    val isScanning: Boolean = false,
    val recognitionStatus: String = "Ready for identity verification",
    val recognizedName: String? = null,
    val showBoundingBox: Boolean = false,
    val serviceMessage: String = "Verify owner to enable protected controls",
)

class CameraViewModel(
    private val repository: UserPreferencesRepository,
    private val logRepository: LogRepository,
    private val faceRecognitionRepository: FaceRecognitionRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()
    private var authenticationResetJob: Job? = null

    fun startScan(imageBytes: ByteArray) {
        if (_uiState.value.isScanning) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isScanning = true,
                recognitionStatus = "Verifying identity...",
                showBoundingBox = true,
                recognizedName = null,
                serviceMessage = "Uploading frame to recognition service",
            )

            val result = faceRecognitionRepository.recognizeFace(imageBytes)
            result.onSuccess { response ->
                val primaryMatch = response.primaryMatch
                if (response.ownerRecognized && primaryMatch != null) {
                    repository.updateOwnerAuthenticated(true)
                    repository.updateAlarmTriggered(false)
                    logRepository.insertLog(
                        action = "Owner verified by face recognition: ${primaryMatch.name}",
                        location = "Main Entrance Camera",
                    )
                    logRepository.insertLog(
                        action = "Protected controls enabled",
                        location = "System",
                    )
                    scheduleAuthenticationReset()
                    _uiState.value = _uiState.value.copy(
                        isScanning = false,
                        recognitionStatus = "Owner verified - access granted",
                        recognizedName = primaryMatch.name,
                        showBoundingBox = true,
                        serviceMessage = "Confidence ${(primaryMatch.confidence * 100).toInt()}% · Access enabled for 60 seconds",
                    )
                } else {
                    repository.updateOwnerAuthenticated(false)
                    logRepository.insertLog(
                        action = "Unknown visitor detected",
                        location = "Main Entrance Camera",
                    )
                    repository.updateAlarmTriggered(true)
                    _uiState.value = _uiState.value.copy(
                        isScanning = false,
                        recognitionStatus = "Identity verification failed",
                        recognizedName = null,
                        showBoundingBox = true,
                        serviceMessage = response.message,
                    )
                }
            }.onFailure { throwable ->
                _uiState.value = _uiState.value.copy(
                    isScanning = false,
                    recognitionStatus = "Recognition service unavailable",
                    recognizedName = null,
                    showBoundingBox = true,
                    serviceMessage = throwable.message ?: "Unable to reach laptop recognition service",
                )
            }

            delay(3500)
            _uiState.value = _uiState.value.copy(
                showBoundingBox = false,
                recognitionStatus = "Ready for identity verification",
                serviceMessage = "Verify owner to enable protected controls",
            )
        }
    }

    private fun scheduleAuthenticationReset() {
        authenticationResetJob?.cancel()
        authenticationResetJob = viewModelScope.launch {
            delay(60_000)
            repository.updateOwnerAuthenticated(false)
            logRepository.insertLog(
                action = "Owner verification expired",
                location = "System",
            )
        }
    }

    class Factory(
        private val repository: UserPreferencesRepository,
        private val logRepository: LogRepository,
        private val faceRecognitionRepository: FaceRecognitionRepository,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CameraViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return CameraViewModel(
                    repository = repository,
                    logRepository = logRepository,
                    faceRecognitionRepository = faceRecognitionRepository,
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
