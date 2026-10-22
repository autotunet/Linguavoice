package com.example.audio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.example.data.model.Language
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class SpeechState {
    object Idle : SpeechState()
    object Ready : SpeechState()
    object Processing : SpeechState()
    data class Listening(val rmsDb: Float = 0f, val partialText: String = "") : SpeechState()
    data class Success(val recognizedText: String) : SpeechState()
    data class Error(val errorMessage: String, val errorCode: Int = -1) : SpeechState()
}

class SpeechManager(private val context: Context) {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var speechRecognizer: SpeechRecognizer? = null

    private val _speechState = MutableStateFlow<SpeechState>(SpeechState.Idle)
    val speechState: StateFlow<SpeechState> = _speechState.asStateFlow()

    val isAvailable: Boolean
        get() = SpeechRecognizer.isRecognitionAvailable(context)

    private fun setupRecognizer() {
        mainHandler.post {
            try {
                speechRecognizer?.destroy()
            } catch (e: Exception) {
                // Ignore
            }

            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                try {
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                        setRecognitionListener(createListener())
                    }
                } catch (e: Exception) {
                    speechRecognizer = null
                }
            }
        }
    }

    private fun createListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _speechState.value = SpeechState.Listening(rmsDb = 1.5f, partialText = "")
            }

            override fun onBeginningOfSpeech() {
                _speechState.value = SpeechState.Listening(rmsDb = 4.0f, partialText = "")
            }

            override fun onRmsChanged(rmsdB: Float) {
                val current = _speechState.value
                val currentPartial = if (current is SpeechState.Listening) current.partialText else ""
                _speechState.value = SpeechState.Listening(
                    rmsDb = rmsdB.coerceAtLeast(0f),
                    partialText = currentPartial
                )
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                val current = _speechState.value
                val partial = if (current is SpeechState.Listening) current.partialText else ""
                if (partial.isNotBlank()) {
                    // We captured partial text, hold onto it
                    _speechState.value = SpeechState.Listening(rmsDb = 0f, partialText = partial)
                }
            }

            override fun onError(error: Int) {
                val current = _speechState.value
                val partial = if (current is SpeechState.Listening) current.partialText else ""

                // If user had spoken some words and we received NO_MATCH or TIMEOUT, use the partial result!
                if (partial.isNotBlank() && (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT)) {
                    _speechState.value = SpeechState.Success(partial)
                    return
                }

                val message = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Error de captura de audio. Verifica el micrófono."
                    SpeechRecognizer.ERROR_CLIENT -> "Error del cliente de voz. Toca para reintentar."
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Se requiere permiso de grabación de audio."
                    SpeechRecognizer.ERROR_NETWORK -> "Error de red al procesar voz."
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Tiempo de red agotado."
                    SpeechRecognizer.ERROR_NO_MATCH -> "No se reconoció ninguna frase. Habla con claridad y acércate al micrófono."
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "El reconocedor de voz estaba ocupado. Reintentando..."
                    SpeechRecognizer.ERROR_SERVER -> "Error en el servidor de reconocimiento."
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No detectamos sonido. Habla cuando el micrófono se ponga rojo."
                    else -> "No logramos captar tu voz. Toca para reintentar."
                }

                _speechState.value = SpeechState.Error(message, error)
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val best = matches?.firstOrNull().orEmpty()
                if (best.isNotBlank()) {
                    _speechState.value = SpeechState.Success(best)
                } else {
                    val current = _speechState.value
                    val partial = if (current is SpeechState.Listening) current.partialText else ""
                    if (partial.isNotBlank()) {
                        _speechState.value = SpeechState.Success(partial)
                    } else {
                        _speechState.value = SpeechState.Error("No se detectó pronunciación clara.", SpeechRecognizer.ERROR_NO_MATCH)
                    }
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val partial = matches?.firstOrNull().orEmpty()
                val currentRms = if (_speechState.value is SpeechState.Listening) {
                    (_speechState.value as SpeechState.Listening).rmsDb
                } else 2.0f

                if (partial.isNotBlank()) {
                    _speechState.value = SpeechState.Listening(rmsDb = currentRms, partialText = partial)
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    fun startListening(language: Language) {
        mainHandler.post {
            try {
                if (speechRecognizer == null) {
                    setupRecognizer()
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, language.locale.toLanguageTag())
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, language.locale.toLanguageTag())
                    putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, language.locale.toLanguageTag())
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 2000L)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1200L)
                }

                speechRecognizer?.cancel()
                _speechState.value = SpeechState.Listening(rmsDb = 1.0f, partialText = "")
                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                _speechState.value = SpeechState.Error("No se pudo iniciar el micrófono: ${e.localizedMessage}", -1)
            }
        }
    }

    fun stopListening() {
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun cancelListening() {
        mainHandler.post {
            try {
                speechRecognizer?.cancel()
            } catch (e: Exception) {
                // Ignore
            }
            _speechState.value = SpeechState.Idle
        }
    }

    fun submitRecognizedText(text: String) {
        _speechState.value = SpeechState.Success(text)
    }

    fun simulateVoiceInput(text: String) {
        _speechState.value = SpeechState.Success(text)
    }

    fun resetState() {
        _speechState.value = SpeechState.Idle
    }

    fun destroy() {
        mainHandler.post {
            try {
                speechRecognizer?.destroy()
            } catch (e: Exception) {
                // Ignore
            }
            speechRecognizer = null
        }
    }

    fun createSpeechIntent(language: Language): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, language.locale.toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, language.locale.toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Pronuncia en ${language.displayName}")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }
    }
}
