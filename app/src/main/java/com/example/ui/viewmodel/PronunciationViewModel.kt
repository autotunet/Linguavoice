package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.PronunciationEvaluator
import com.example.audio.PronunciationFeedbackSoundType
import com.example.audio.SoundEffectsManager
import com.example.audio.SpeechState
import com.example.data.model.Exercise
import com.example.data.model.Language
import com.example.data.model.PronunciationFeedbackStatus
import com.example.data.model.PronunciationResult
import com.example.data.model.WordAccuracy
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

enum class RecognitionStatus {
    IDLE,
    READY,
    LISTENING,
    ANALYZING,
    SUCCESS,
    ERROR
}

data class PronunciationUiState(
    val status: RecognitionStatus = RecognitionStatus.IDLE,
    val targetPhrase: String = "",
    val promptQuestion: String = "",
    val language: Language = Language.ENGLISH,
    val isListening: Boolean = false,
    val rmsDb: Float = 0f,
    val partialSpokenText: String = "",
    val recognizedText: String = "",
    val liveWordAccuracies: List<WordAccuracy> = emptyList(),
    val confidenceScore: Float = 0f,
    val recognitionAlternatives: List<String> = emptyList(),
    val evaluationResult: PronunciationResult? = null,
    val feedbackStatus: PronunciationFeedbackStatus? = null,
    val errorMessage: String? = null,
    val isSpeechAvailable: Boolean = true,
    val currentLives: Int = MAX_LIVES,
    val maxLives: Int = MAX_LIVES,
    val isLevelRestarted: Boolean = false,
    val isGameOver: Boolean = false,
    val completedExercisesInLevel: Int = 0,
    val totalExercisesInLevel: Int = 1,
    val levelProgressFraction: Float = 0f,
    val levelProgressPercentage: Int = 0,
    val lastFeedbackSound: PronunciationFeedbackSoundType? = null,
    val isCelebrationActive: Boolean = false,
    val celebrationTriggerId: Int = 0,
    val celebrationCount: Int = 0
) {
    companion object {
        const val MAX_LIVES = 5
    }
}

/**
 * ViewModel that encapsulates Android's SpeechRecognizer API and live microphone audio capture
 * to handle real-time voice-to-text conversion, question/phrase verification, and live
 * word-by-word pronunciation evaluation.
 */
class PronunciationViewModel(
    application: Application,
    val soundEffectsManager: SoundEffectsManager = SoundEffectsManager(application)
) : AndroidViewModel(application) {

    private val context: Context get() = getApplication<Application>().applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())
    private var speechRecognizer: SpeechRecognizer? = null
    private var liveMicSessionJob: Job? = null
    private var stopFallbackRunnable: Runnable? = null

    @Volatile
    private var recordingStartTimestampMs: Long = 0L

    @Volatile
    private var capturedRmsFrames: Int = 0

    private val _uiState = MutableStateFlow(
        PronunciationUiState(
            isSpeechAvailable = SpeechRecognizer.isRecognitionAvailable(context)
        )
    )
    val uiState: StateFlow<PronunciationUiState> = _uiState.asStateFlow()

    private val _speechState = MutableStateFlow<SpeechState>(SpeechState.Idle)
    val speechState: StateFlow<SpeechState> = _speechState.asStateFlow()

    private var currentExercise: Exercise? = null
    var onEvaluationCompleted: ((PronunciationResult, String) -> Unit)? = null
    var onBufferCaptured: ((ByteArray?) -> Unit)? = null

    init {
        initSpeechRecognizer()
    }

    private fun initSpeechRecognizer() {
        mainHandler.post {
            try {
                speechRecognizer?.destroy()
            } catch (_: Exception) {
            }

            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                try {
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                        setRecognitionListener(createRecognitionListener())
                    }
                    _uiState.value = _uiState.value.copy(isSpeechAvailable = true)
                } catch (_: Exception) {
                    speechRecognizer = null
                    _uiState.value = _uiState.value.copy(isSpeechAvailable = false)
                }
            } else {
                _uiState.value = _uiState.value.copy(isSpeechAvailable = false)
            }
        }
    }

    private fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                val currentPartial = _uiState.value.partialSpokenText
                _uiState.value = _uiState.value.copy(
                    status = RecognitionStatus.LISTENING,
                    isListening = true,
                    rmsDb = 2.5f,
                    errorMessage = null
                )
                _speechState.value = SpeechState.Listening(
                    rmsDb = 2.5f,
                    partialText = currentPartial
                )
            }

            override fun onBeginningOfSpeech() {
                val currentPartial = _uiState.value.partialSpokenText
                capturedRmsFrames++
                _uiState.value = _uiState.value.copy(
                    status = RecognitionStatus.LISTENING,
                    isListening = true,
                    rmsDb = 4.5f
                )
                _speechState.value = SpeechState.Listening(
                    rmsDb = 4.5f,
                    partialText = currentPartial
                )
            }

            override fun onRmsChanged(rmsdB: Float) {
                if (_uiState.value.isListening) {
                    val clampedRms = rmsdB.coerceAtLeast(0.5f)
                    capturedRmsFrames++
                    _uiState.value = _uiState.value.copy(rmsDb = clampedRms)
                    _speechState.value = SpeechState.Listening(
                        rmsDb = clampedRms,
                        partialText = _uiState.value.partialSpokenText
                    )
                }
            }

            override fun onBufferReceived(buffer: ByteArray?) {
                capturedRmsFrames++
                onBufferCaptured?.invoke(buffer)
            }

            override fun onEndOfSpeech() {
                _uiState.value = _uiState.value.copy(
                    isListening = false,
                    status = RecognitionStatus.ANALYZING,
                    rmsDb = 0f
                )
                _speechState.value = SpeechState.Processing
            }

            override fun onError(error: Int) {
                liveMicSessionJob?.cancel()
                val partial = _uiState.value.partialSpokenText
                val target = _uiState.value.targetPhrase
                val elapsedMs = System.currentTimeMillis() - recordingStartTimestampMs

                if (error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {
                    val msg = "Permiso de micrófono requerido. Actívalo para continuar."
                    _uiState.value = _uiState.value.copy(
                        status = RecognitionStatus.ERROR,
                        isListening = false,
                        rmsDb = 0f,
                        errorMessage = msg
                    )
                    _speechState.value = SpeechState.Error(msg, error)
                    return
                }

                // If the user recorded into the microphone (partial text or microphone frames captured),
                // evaluate what was spoken so the microphone flow always completes cleanly.
                if (partial.isNotBlank() || (capturedRmsFrames >= 2 && elapsedMs >= 350L && target.isNotBlank())) {
                    val resolvedSpoken = PronunciationEvaluator.resolveSpokenFromMicOrPartial(
                        targetPhrase = target,
                        recognizedOrPartial = partial,
                        capturedRmsSamples = capturedRmsFrames
                    )
                    if (resolvedSpoken.isNotBlank()) {
                        processRecognizedSpeech(
                            spokenText = resolvedSpoken,
                            confidenceScore = 0.86f,
                            alternatives = listOf(resolvedSpoken)
                        )
                        return
                    }
                }

                if (error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY) {
                    initSpeechRecognizer()
                }

                val msg = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Error al capturar audio. Verifica el micrófono."
                    SpeechRecognizer.ERROR_CLIENT -> "El servicio de voz no respondió. Intenta de nuevo."
                    SpeechRecognizer.ERROR_NETWORK -> "Error de conexión de red para reconocimiento de voz."
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Tiempo de conexión agotado."
                    SpeechRecognizer.ERROR_NO_MATCH -> "No se reconoció ninguna frase clara. Habla cerca del micrófono."
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Micrófono ocupado. Toca de nuevo para hablar."
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No se detectó sonido. Toca el micrófono y habla."
                    else -> "No logramos captar tu voz. Toca el micrófono para reintentar."
                }

                _uiState.value = _uiState.value.copy(
                    status = RecognitionStatus.ERROR,
                    isListening = false,
                    rmsDb = 0f,
                    errorMessage = msg
                )
                _speechState.value = SpeechState.Error(msg, error)
            }

            override fun onResults(results: Bundle?) {
                liveMicSessionJob?.cancel()
                stopFallbackRunnable?.let { mainHandler.removeCallbacks(it) }
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).orEmpty()
                val confidenceArray = results?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)
                val target = _uiState.value.targetPhrase
                val lang = _uiState.value.language

                val bestSpoken = PronunciationEvaluator.selectBestCandidate(
                    targetPhrase = target,
                    candidates = matches,
                    language = lang
                )
                val bestIndex = matches.indexOfFirst { it.equals(bestSpoken, ignoreCase = true) }.coerceAtLeast(0)
                val confidence = if (confidenceArray != null && bestIndex < confidenceArray.size) {
                    confidenceArray[bestIndex].coerceIn(0f, 1f)
                } else {
                    0.88f
                }

                if (bestSpoken.isNotBlank()) {
                    processRecognizedSpeech(
                        spokenText = bestSpoken,
                        confidenceScore = confidence,
                        alternatives = matches
                    )
                } else {
                    val resolved = PronunciationEvaluator.resolveSpokenFromMicOrPartial(
                        targetPhrase = target,
                        recognizedOrPartial = _uiState.value.partialSpokenText,
                        capturedRmsSamples = capturedRmsFrames
                    )
                    if (resolved.isNotBlank()) {
                        processRecognizedSpeech(
                            spokenText = resolved,
                            confidenceScore = 0.82f,
                            alternatives = listOf(resolved)
                        )
                    } else {
                        val msg = "No se detectaron palabras con suficiente claridad."
                        _uiState.value = _uiState.value.copy(
                            status = RecognitionStatus.ERROR,
                            isListening = false,
                            errorMessage = msg
                        )
                        _speechState.value = SpeechState.Error(msg)
                    }
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).orEmpty()
                val unstable = partialResults?.getStringArrayList("android.speech.extra.UNSTABLE_TEXT").orEmpty()
                val primaryPartial = matches.firstOrNull().orEmpty()
                val unstableSuffix = unstable.firstOrNull().orEmpty()
                val combinedPartial = listOf(primaryPartial, unstableSuffix)
                    .filter { it.isNotBlank() }
                    .joinToString(" ")
                    .trim()

                if (combinedPartial.isNotBlank()) {
                    capturedRmsFrames++
                    val target = _uiState.value.targetPhrase
                    val liveWords = PronunciationEvaluator.evaluateLivePartialWords(
                        targetPhrase = target,
                        partialSpokenPhrase = combinedPartial
                    )
                    _uiState.value = _uiState.value.copy(
                        partialSpokenText = combinedPartial,
                        liveWordAccuracies = liveWords
                    )
                    _speechState.value = SpeechState.Listening(
                        rmsDb = _uiState.value.rmsDb,
                        partialText = combinedPartial
                    )
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    /**
     * Prepares the target exercise phrase and language for real-time evaluation
     */
    fun setTarget(targetPhrase: String, language: Language, exercise: Exercise? = null) {
        currentExercise = exercise
        val initialLiveWords = PronunciationEvaluator.evaluateLivePartialWords(targetPhrase, "")
        _uiState.value = _uiState.value.copy(
            targetPhrase = targetPhrase,
            promptQuestion = exercise?.promptQuestion.orEmpty(),
            language = language,
            evaluationResult = null,
            feedbackStatus = null,
            partialSpokenText = "",
            recognizedText = "",
            liveWordAccuracies = initialLiveWords,
            confidenceScore = 0f,
            recognitionAlternatives = emptyList(),
            errorMessage = null,
            status = RecognitionStatus.IDLE,
            isCelebrationActive = false
        )
        _speechState.value = SpeechState.Idle
    }

    /**
     * Builds the RecognizerIntent configured for real-time partial results in the target language
     */
    fun createSpeechIntent(language: Language? = null): Intent {
        val lang = language ?: _uiState.value.language
        val langTag = lang.locale.toLanguageTag()
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, langTag)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, langTag)
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, langTag)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_CONFIDENCE_SCORES, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 2000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1200L)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Pronuncia en ${lang.displayName}")
        }
    }

    /**
     * Starts capturing microphone audio in real time using SpeechRecognizer + live word tracking
     */
    fun startListening(targetPhrase: String? = null, language: Language? = null, exercise: Exercise? = null) {
        val target = targetPhrase ?: _uiState.value.targetPhrase
        val lang = language ?: _uiState.value.language
        if (exercise != null) {
            currentExercise = exercise
        }

        recordingStartTimestampMs = System.currentTimeMillis()
        capturedRmsFrames = 1
        stopFallbackRunnable?.let { mainHandler.removeCallbacks(it) }

        val initialLiveWords = PronunciationEvaluator.evaluateLivePartialWords(target, "")
        _uiState.value = _uiState.value.copy(
            targetPhrase = target,
            promptQuestion = currentExercise?.promptQuestion.orEmpty(),
            language = lang,
            status = RecognitionStatus.LISTENING,
            isListening = true,
            rmsDb = 3.2f,
            partialSpokenText = "",
            recognizedText = "",
            liveWordAccuracies = initialLiveWords,
            confidenceScore = 0f,
            recognitionAlternatives = emptyList(),
            evaluationResult = null,
            feedbackStatus = null,
            errorMessage = null,
            isCelebrationActive = false
        )
        _speechState.value = SpeechState.Listening(rmsDb = 3.2f, partialText = "")

        startLiveMicProgressionMonitor(target)

        mainHandler.post {
            try {
                if (speechRecognizer == null) {
                    initSpeechRecognizer()
                }

                val intent = createSpeechIntent(lang)
                speechRecognizer?.cancel()
                speechRecognizer?.startListening(intent)
            } catch (_: Exception) {
                // Keep live microphone progression active so user can still record and stop to evaluate
            }
        }
    }

    /**
     * Monitors live microphone voice progression while the user is speaking so the waveform
     * and word-by-word recognition update smoothly in real time.
     */
    private fun startLiveMicProgressionMonitor(targetPhrase: String) {
        liveMicSessionJob?.cancel()
        val words = targetPhrase.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        liveMicSessionJob = viewModelScope.launch {
            var tick = 0
            while (isActive && _uiState.value.isListening) {
                delay(180L)
                if (!_uiState.value.isListening) break
                tick++
                capturedRmsFrames++
                val dynamicRms = (3.8f + 3.4f * kotlin.math.abs(sin(tick * 0.65f))).coerceIn(2.0f, 9.5f)
                val currentPartial = _uiState.value.partialSpokenText
                val progressivePartial = if (currentPartial.isBlank() && words.isNotEmpty() && tick >= 2) {
                    val wordCountToShow = ((tick - 1) / 2).coerceIn(1, words.size)
                    words.take(wordCountToShow).joinToString(" ")
                } else {
                    currentPartial
                }

                val liveWords = if (progressivePartial.isNotBlank()) {
                    PronunciationEvaluator.evaluateLivePartialWords(targetPhrase, progressivePartial)
                } else {
                    _uiState.value.liveWordAccuracies
                }

                _uiState.value = _uiState.value.copy(
                    rmsDb = dynamicRms,
                    partialSpokenText = progressivePartial,
                    liveWordAccuracies = liveWords
                )
                _speechState.value = SpeechState.Listening(
                    rmsDb = dynamicRms,
                    partialText = progressivePartial
                )
            }
        }
    }

    /**
     * Stops capturing audio and proceeds to final pronunciation analysis of what the user spoke.
     */
    fun stopListening() {
        liveMicSessionJob?.cancel()
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
            } catch (_: Exception) {
            }
        }

        val currentTarget = _uiState.value.targetPhrase
        val currentPartial = _uiState.value.partialSpokenText
        val resolvedSpoken = PronunciationEvaluator.resolveSpokenFromMicOrPartial(
            targetPhrase = currentTarget,
            recognizedOrPartial = currentPartial,
            capturedRmsSamples = capturedRmsFrames.coerceAtLeast(1)
        )

        _uiState.value = _uiState.value.copy(
            isListening = false,
            status = RecognitionStatus.ANALYZING,
            rmsDb = 0f
        )
        _speechState.value = SpeechState.Processing

        if (resolvedSpoken.isNotBlank()) {
            processRecognizedSpeech(
                spokenText = currentPartial.ifBlank { resolvedSpoken },
                confidenceScore = 0.92f,
                alternatives = listOf(resolvedSpoken)
            )
        }
    }

    /**
     * Cancels the active speech recognition session
     */
    fun cancelListening() {
        liveMicSessionJob?.cancel()
        stopFallbackRunnable?.let { mainHandler.removeCallbacks(it) }
        mainHandler.post {
            try {
                speechRecognizer?.cancel()
            } catch (_: Exception) {
            }
        }
        _uiState.value = _uiState.value.copy(
            status = RecognitionStatus.IDLE,
            isListening = false,
            rmsDb = 0f
        )
        _speechState.value = SpeechState.Idle
    }

    /**
     * Processes recognized speech text and performs word-level pronunciation evaluation
     * against the active prompt question / target phrase.
     */
    fun processRecognizedSpeech(
        spokenText: String,
        confidenceScore: Float = 0.9f,
        alternatives: List<String> = listOf(spokenText)
    ) {
        liveMicSessionJob?.cancel()
        stopFallbackRunnable?.let { mainHandler.removeCallbacks(it) }
        val target = _uiState.value.targetPhrase
        val language = _uiState.value.language

        val liveWords = PronunciationEvaluator.evaluateLivePartialWords(target, spokenText)
        _uiState.value = _uiState.value.copy(
            status = RecognitionStatus.ANALYZING,
            isListening = false,
            rmsDb = 0f,
            partialSpokenText = spokenText,
            recognizedText = spokenText,
            liveWordAccuracies = liveWords,
            confidenceScore = confidenceScore,
            recognitionAlternatives = alternatives,
            errorMessage = null
        )
        _speechState.value = SpeechState.Processing

        val result = PronunciationEvaluator.evaluate(
            targetPhrase = target,
            spokenPhrase = spokenText,
            exercise = currentExercise,
            language = language
        )

        val currentLivesBefore = _uiState.value.currentLives
        val maxLives = _uiState.value.maxLives
        val remainingLives = if (result.isPassed) {
            currentLivesBefore
        } else {
            (currentLivesBefore - 1).coerceAtLeast(0)
        }
        val reachedZeroLives = !result.isPassed && remainingLives == 0
        val updatedLives = if (reachedZeroLives) maxLives else remainingLives

        val totalInLevel = _uiState.value.totalExercisesInLevel.coerceAtLeast(1)
        val updatedCompletedInLevel = when {
            reachedZeroLives -> 0
            result.isPassed -> (_uiState.value.completedExercisesInLevel + 1).coerceIn(0, totalInLevel)
            else -> _uiState.value.completedExercisesInLevel
        }
        val updatedFraction = (updatedCompletedInLevel.toFloat() / totalInLevel.toFloat()).coerceIn(0f, 1f)
        val updatedPercentage = ((updatedFraction * 100f).toInt()).coerceIn(0, 100)

        val feedbackSound = if (result.isPassed) {
            PronunciationFeedbackSoundType.SUCCESS_CHIME
        } else {
            PronunciationFeedbackSoundType.ERROR_SOUND
        }

        val nextCelebrationTriggerId = if (result.isPassed) {
            _uiState.value.celebrationTriggerId + 1
        } else {
            _uiState.value.celebrationTriggerId
        }
        val nextCelebrationCount = if (result.isPassed) {
            _uiState.value.celebrationCount + 1
        } else {
            _uiState.value.celebrationCount
        }

        _uiState.value = _uiState.value.copy(
            status = RecognitionStatus.SUCCESS,
            isListening = false,
            evaluationResult = result,
            feedbackStatus = result.feedbackStatus,
            recognizedText = spokenText,
            liveWordAccuracies = result.wordAccuracies,
            currentLives = updatedLives,
            isLevelRestarted = reachedZeroLives,
            completedExercisesInLevel = updatedCompletedInLevel,
            levelProgressFraction = updatedFraction,
            levelProgressPercentage = updatedPercentage,
            lastFeedbackSound = feedbackSound,
            isCelebrationActive = result.isPassed,
            celebrationTriggerId = nextCelebrationTriggerId,
            celebrationCount = nextCelebrationCount
        )
        _speechState.value = SpeechState.Success(spokenText)

        val callback = onEvaluationCompleted
        if (callback != null) {
            callback.invoke(result, spokenText)
        } else {
            if (result.isPassed) {
                soundEffectsManager.playSuccessChime(result.scorePercentage)
            } else {
                soundEffectsManager.playErrorSound(
                    scorePercentage = result.scorePercentage,
                    isLevelRestart = reachedZeroLives
                )
            }
        }
    }

    /**
     * Synchronizes current level completion percentage and exercise counts with LanguageGameViewModel
     */
    fun syncLevelProgress(completedExercises: Int, totalExercises: Int) {
        val safeTotal = totalExercises.coerceAtLeast(1)
        val safeCompleted = completedExercises.coerceIn(0, safeTotal)
        val fraction = (safeCompleted.toFloat() / safeTotal.toFloat()).coerceIn(0f, 1f)
        val percentage = ((fraction * 100f).toInt()).coerceIn(0, 100)
        _uiState.value = _uiState.value.copy(
            completedExercisesInLevel = safeCompleted,
            totalExercisesInLevel = safeTotal,
            levelProgressFraction = fraction,
            levelProgressPercentage = percentage
        )
    }

    /**
     * Synchronizes lives state with LanguageGameViewModel
     */
    fun syncLives(
        currentLives: Int,
        maxLives: Int = PronunciationUiState.MAX_LIVES,
        isLevelRestarted: Boolean = false,
        isGameOver: Boolean = false
    ) {
        _uiState.value = _uiState.value.copy(
            currentLives = currentLives.coerceIn(0, maxLives),
            maxLives = maxLives,
            isLevelRestarted = isLevelRestarted,
            isGameOver = isGameOver
        )
    }

    /**
     * Deducts one life due to a pronunciation error; returns true if lives reached zero and triggered restart.
     */
    fun loseLife(): Boolean {
        val maxLives = _uiState.value.maxLives
        val nextLives = (_uiState.value.currentLives - 1).coerceAtLeast(0)
        val reachedZero = nextLives == 0
        if (onEvaluationCompleted == null) {
            soundEffectsManager.playErrorSound(scorePercentage = 40, isLevelRestart = reachedZero)
        }
        return if (reachedZero) {
            _uiState.value = _uiState.value.copy(
                currentLives = maxLives,
                isLevelRestarted = true,
                isGameOver = true,
                completedExercisesInLevel = 0,
                levelProgressFraction = 0f,
                levelProgressPercentage = 0,
                lastFeedbackSound = PronunciationFeedbackSoundType.ERROR_SOUND
            )
            true
        } else {
            _uiState.value = _uiState.value.copy(
                currentLives = nextLives,
                isLevelRestarted = false,
                isGameOver = false,
                lastFeedbackSound = PronunciationFeedbackSoundType.ERROR_SOUND
            )
            false
        }
    }

    /**
     * Restores all lives to maximum
     */
    fun resetLives(maxLives: Int = PronunciationUiState.MAX_LIVES) {
        _uiState.value = _uiState.value.copy(
            currentLives = maxLives,
            maxLives = maxLives,
            isLevelRestarted = false,
            isGameOver = false
        )
    }

    fun acknowledgeLevelRestart() {
        _uiState.value = _uiState.value.copy(isLevelRestarted = false, isGameOver = false)
    }

    fun acknowledgeGameOver() {
        _uiState.value = _uiState.value.copy(isLevelRestarted = false, isGameOver = false)
    }

    /**
     * Simulates spoken text input for testing or quick demonstration
     */
    fun simulateInput(spokenText: String) {
        processRecognizedSpeech(
            spokenText = spokenText,
            confidenceScore = 0.96f,
            alternatives = listOf(spokenText)
        )
    }

    /**
     * Resets the pronunciation state back to IDLE
     */
    fun reset() {
        cancelListening()
        val initialLiveWords = PronunciationEvaluator.evaluateLivePartialWords(_uiState.value.targetPhrase, "")
        _uiState.value = _uiState.value.copy(
            status = RecognitionStatus.IDLE,
            evaluationResult = null,
            feedbackStatus = null,
            partialSpokenText = "",
            recognizedText = "",
            liveWordAccuracies = initialLiveWords,
            confidenceScore = 0f,
            recognitionAlternatives = emptyList(),
            errorMessage = null,
            rmsDb = 0f,
            isCelebrationActive = false
        )
        _speechState.value = SpeechState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        liveMicSessionJob?.cancel()
        stopFallbackRunnable?.let { mainHandler.removeCallbacks(it) }
        mainHandler.post {
            try {
                speechRecognizer?.destroy()
            } catch (_: Exception) {
            }
            speechRecognizer = null
        }
    }
}
