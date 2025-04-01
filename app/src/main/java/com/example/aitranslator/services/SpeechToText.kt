package com.example.aitranslator.services

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


data class SpeechToTextState(
    val text: String = "",
    val loading: Boolean = false,
    val error: String = ""
)

class SpeechToText(
    private val app: Application
) : RecognitionListener {

    private var _state = MutableStateFlow(SpeechToTextState())
    var state = _state.asStateFlow()

    private val recognizer = SpeechRecognizer.createSpeechRecognizer(app)

    fun startListening(code: String = "en") {
        if (!SpeechRecognizer.isRecognitionAvailable(app)) {
            _state.update {
                it.copy(
                    error = "Recognition is not available"
                )
            }
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, code)
        }
        recognizer.setRecognitionListener(this)
        recognizer.startListening(intent)
        _state.update {
            it.copy(
                loading = true
            )
        }
    }

    fun stopListening() {
        _state.update {
            it.copy(
                loading = false
            )
        }
        recognizer.stopListening()
    }

    override fun onReadyForSpeech(p0: Bundle?) {
        _state.update {
            it.copy(
                error = ""
            )
        }
    }


    override fun onEndOfSpeech() {
        _state.update {
            it.copy(
                loading = false
            )
        }
    }

    override fun onError(error: Int) {
        _state.update {
            it.copy(
                error = "Error: $error"
            )
        }
    }

    override fun onResults(result: Bundle?) {
        result?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.getOrNull(0)
            ?.let { results ->
                _state.update {
                    it.copy(
                        text = results
                    )
                }
            }
    }

    override fun onBeginningOfSpeech() = Unit
    override fun onRmsChanged(p0: Float) = Unit
    override fun onBufferReceived(p0: ByteArray?) = Unit
    override fun onPartialResults(p0: Bundle?) = Unit
    override fun onEvent(p0: Int, p1: Bundle?) = Unit
}