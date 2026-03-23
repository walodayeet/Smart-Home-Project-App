package com.example.smarthomedemo2.ui.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smarthomedemo2.data.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

data class VoiceUiState(
    val isListening: Boolean = false,
    val recognizedText: String = "Tap the mic and speak a command...",
    val lastCommand: String? = null,
    val isExecuting: Boolean = false
)

class VoiceViewModel(private val repository: UserPreferencesRepository) : ViewModel() {
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
        }

        speechRecognizer?.startListening(intent)
        _uiState.value = _uiState.value.copy(isListening = true, recognizedText = "Listening...")
    }

    private fun createRecognitionListener() = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {
            _uiState.value = _uiState.value.copy(isListening = false)
        }
        override fun onError(error: Int) {
            _uiState.value = _uiState.value.copy(isListening = false, recognizedText = "Error: $error")
        }
        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val text = matches?.firstOrNull() ?: ""
            _uiState.value = _uiState.value.copy(recognizedText = text)
            processCommand(text)
        }
        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    private fun processCommand(command: String) {
        val lowerCommand = command.lowercase()
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExecuting = true)
            when {
                lowerCommand.contains("light") && lowerCommand.contains("on") -> {
                    repository.updateLightStatus(true)
                    _uiState.value = _uiState.value.copy(lastCommand = "Lights Turned On")
                }
                lowerCommand.contains("light") && lowerCommand.contains("off") -> {
                    repository.updateLightStatus(false)
                    _uiState.value = _uiState.value.copy(lastCommand = "Lights Turned Off")
                }
                (lowerCommand.contains("window") || lowerCommand.contains("curtain")) &&
                    (lowerCommand.contains("open") || lowerCommand.contains("on")) -> {
                    repository.updateCurtainStatus(true)
                    _uiState.value = _uiState.value.copy(lastCommand = "Windows Opened")
                }
                (lowerCommand.contains("window") || lowerCommand.contains("curtain")) &&
                    (lowerCommand.contains("close") || lowerCommand.contains("off")) -> {
                    repository.updateCurtainStatus(false)
                    _uiState.value = _uiState.value.copy(lastCommand = "Windows Closed")
                }
                lowerCommand.contains("unlock") -> {
                    repository.updateLockStatus(false)
                    _uiState.value = _uiState.value.copy(lastCommand = "Door Unlocked")
                }
                lowerCommand.contains("lock") -> {
                    repository.updateLockStatus(true)
                    _uiState.value = _uiState.value.copy(lastCommand = "Door Locked")
                }
                else -> {
                    _uiState.value = _uiState.value.copy(lastCommand = "Command Not Recognized")
                }
            }
            _uiState.value = _uiState.value.copy(isExecuting = false)
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizer?.destroy()
    }
}
