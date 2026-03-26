package com.example.smarthomedemo2.ui.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarthomedemo2.data.LogRepository
import com.example.smarthomedemo2.data.UserPreferencesRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

data class VoiceUiState(
    val isListening: Boolean = false,
    val recognizedText: String = "Tap and hold the mic, then speak...",
    val lastCommand: String? = null,
    val isExecuting: Boolean = false
)

class VoiceViewModel(
    private val repository: UserPreferencesRepository,
    private val logRepository: LogRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(VoiceUiState())
    val uiState: StateFlow<VoiceUiState> = _uiState.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null

    fun startListening(context: Context) {
        if (_uiState.value.isListening) return

        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer?.setRecognitionListener(createRecognitionListener())
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        speechRecognizer?.startListening(intent)
        _uiState.update {
            it.copy(isListening = true, recognizedText = "Listening...")
        }
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        _uiState.update {
            it.copy(isListening = false)
        }
    }

    private fun createRecognitionListener() = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {
            _uiState.update { it.copy(isListening = false) }
        }

        override fun onError(error: Int) {
            val errorMessage = when (error) {
                SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected"
                SpeechRecognizer.ERROR_NETWORK -> "Network error"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout"
                else -> "Voice error: $error"
            }
            _uiState.update {
                it.copy(isListening = false, recognizedText = errorMessage)
            }
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val text = matches?.firstOrNull() ?: ""
            _uiState.update { it.copy(recognizedText = text) }
            processCommand(text)
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            matches?.firstOrNull()?.let { partialText ->
                _uiState.update { it.copy(recognizedText = partialText) }
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    private fun processCommand(command: String) {
        val normalizedCommand = command
            .lowercase()
            .replace(Regex("[^a-z0-9\\s]"), "")
            .trim()

        viewModelScope.launch {
            _uiState.update { it.copy(isExecuting = true) }

            val (commandLabel, logLocation) = when {
                normalizedCommand.contains("light") &&
                    (normalizedCommand.contains("on") || normalizedCommand.contains("activate")) -> {
                    repository.updateLightStatus(true)
                    "Lights On ✅" to "Living Room"
                }
                normalizedCommand.contains("light") &&
                    (normalizedCommand.contains("off") || normalizedCommand.contains("dark")) -> {
                    repository.updateLightStatus(false)
                    "Lights Off 🌑" to "Living Room"
                }
                (normalizedCommand.contains("window") || normalizedCommand.contains("curtain")) &&
                    (normalizedCommand.contains("open") || normalizedCommand.contains("on")) -> {
                    repository.updateCurtainStatus(true)
                    "Windows Opened 🪟" to "Windows"
                }
                (normalizedCommand.contains("window") || normalizedCommand.contains("curtain")) &&
                    (normalizedCommand.contains("close") || normalizedCommand.contains("off")) -> {
                    repository.updateCurtainStatus(false)
                    "Windows Closed 🪟" to "Windows"
                }
                normalizedCommand.contains("unlock") || normalizedCommand.contains("open door") -> {
                    repository.updateLockStatus(false)
                    "Door Unlocked 🔓" to "Main Entrance"
                }
                normalizedCommand.contains("lock") || normalizedCommand.contains("secure") -> {
                    repository.updateLockStatus(true)
                    "Door Locked 🔒" to "Main Entrance"
                }
                else -> {
                    "Command Not Recognized" to "Voice Control"
                }
            }

            val voiceCommandForLog = command.ifBlank { "(empty)" }
            logRepository.insertLog(
                action = "Voice command: \"$voiceCommandForLog\" -> $commandLabel",
                location = logLocation,
            )

            _uiState.update {
                it.copy(lastCommand = commandLabel, isExecuting = false)
            }

            delay(3000)
            _uiState.update { it.copy(lastCommand = null) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizer?.destroy()
    }
}
