package com.example.aitranslator.appui.speechtotext

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aitranslator.services.SpeechToText
import com.example.aitranslator.services.SpeechToTextState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainScreenViewModel(private val app: Application = Application()) : ViewModel() {
    private val speechToText = SpeechToText(app)

    private val _state = MutableStateFlow(SpeechToTextState())
    val state = _state.asStateFlow()


    init {
        getState()
    }

    private fun getState() {
        viewModelScope.launch {
            speechToText.state.collect { sttUiState ->
                _state.update {
                    it.copy(
                        text = sttUiState.text,
                        loading = sttUiState.loading,
                        error = sttUiState.error
                    )
                }
            }
        }
    }

    fun startRecording() {
        viewModelScope.launch {
            speechToText.startListening()
        }
    }

    fun stopRecording() {
        viewModelScope.launch {
            speechToText.stopListening()
        }
    }

}