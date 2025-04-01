package com.example.aitranslator.services

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class TextToSpeech(
    private val context: Context,
    private val onInit: (isSuccess: Boolean) -> Unit
) : TextToSpeech.OnInitListener {

    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false

    init {
        initialize()
    }

    private fun initialize() {
        textToSpeech = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
//            setLanguage(Locale.UK)
            onInit(true)
        } else {
            Log.e("TTS", "Initialization failed")
            onInit(false)
        }
    }

    fun speak(text: String) {
        if (isInitialized && text.isNotBlank())
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun stop() {
        textToSpeech?.stop()
    }

    fun shutDown() {
        textToSpeech?.shutdown()
    }

    fun setLanguage(locale: Locale) {
        textToSpeech?.language = locale
    }
}