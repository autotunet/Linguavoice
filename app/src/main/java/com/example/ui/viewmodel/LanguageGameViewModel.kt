package com.example.ui.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioFeedbackState
import com.example.audio.PronunciationEvaluator
import com.example.audio.SoundEffectsManager
import com.example.audio.SpeechState
import com.example.audio.TtsManager
import com.example.data.local.AppDatabase
import com.example.data.model.DialogueScenario
import com.example.data.model.Exercise
import com.example.data.model.Language
import com.example.data.model.LessonModule
import com.example.data.model.LevelSystem
import com.example.data.model.PronunciationResult
import com.example.data.model.UserLevelInfo
import com.example.data.model.UserProfile
import com.example.data.repository.StreakInfo
import com.example.data.repository.StreakRepository
import com.example.data.repository.UserPreferencesRepository
import com.example.ui.components.CelebrationEffectState
import com.example.ui.components.CelebrationTier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class AppScreen {
    PATH_LEARN,
    EXERCISE_SESSION,
    FREE_PRACTICE,
    DIALOGUE,
    PROFILE
}

data class LivesState(
    val currentLives: Int = MAX_LIVES,
    val maxLives: Int = MAX_LIVES,
    val errorsInCurrentLevel: Int = 0,
    val levelRestartCount: Int = 0,
    val isLevelRestartedByZeroLives: Boolean = false,
    val lastFailedResultBeforeRestart: PronunciationResult? = null
) {
    companion object {
        const val MAX_LIVES = 5
    }
}

data class CurrentLevelProgressState(
    val completedExercises: Int = 0,
    val totalExercises: Int = 1,
    val currentStepNumber: Int = 1,
    val progressFraction: Float = 0f,
    val completionPercentage: Int = 0,
    val levelTitle: String = ""
)

class LanguageGameViewModel(application: Application) : AndroidViewModel(application) {

    private val userPrefsRepo = UserPreferencesRepository(application)
    val userProfile: StateFlow<UserProfile> = userPrefsRepo.userProfile

    private val appDatabase = AppDatabase.getInstance(application)
    val streakRepository = StreakRepository(appDatabase.practiceDayDao())

    val streakInfo: StateFlow<StreakInfo> = streakRepository.streakInfoFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = StreakInfo(
                currentStreak = 4,
                bestStreak = 4,
                practicedToday = false,
                totalDaysPracticed = 3,
                currentWeekDays = emptyList()
            )
        )

    val userLevelInfo: StateFlow<UserLevelInfo> = userProfile.map { profile ->
        LevelSystem.calculateLevelInfo(profile.totalXp)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LevelSystem.calculateLevelInfo(280)
    )

    // Audio feedback manager for pronunciation results (success chime & subtle error tone)
    val soundEffectsManager = SoundEffectsManager(application)
    val audioFeedbackState: StateFlow<AudioFeedbackState> = soundEffectsManager.state

    // Real-time SpeechRecognizer ViewModel managing voice-to-text conversion
    val pronunciationViewModel = PronunciationViewModel(application, soundEffectsManager)
    val pronunciationUiState: StateFlow<PronunciationUiState> = pronunciationViewModel.uiState
    val speechState: StateFlow<SpeechState> = pronunciationViewModel.speechState

    val ttsManager = TtsManager(application)
    val isTtsSpeaking: StateFlow<Boolean> = ttsManager.isSpeaking

    private val _currentScreen = MutableStateFlow(AppScreen.PATH_LEARN)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Lives Manager State (Gestor de Vidas)
    private val _livesState = MutableStateFlow(
        LivesState(
            currentLives = userProfile.value.hearts.coerceIn(1, LivesState.MAX_LIVES),
            maxLives = LivesState.MAX_LIVES
        )
    )
    val livesState: StateFlow<LivesState> = _livesState.asStateFlow()

    // Exercise Session State
    private val _activeModule = MutableStateFlow<LessonModule?>(null)
    val activeModule: StateFlow<LessonModule?> = _activeModule.asStateFlow()

    private val _currentExerciseIndex = MutableStateFlow(0)
    val currentExerciseIndex: StateFlow<Int> = _currentExerciseIndex.asStateFlow()

    private val _currentExercise = MutableStateFlow<Exercise?>(null)
    val currentExercise: StateFlow<Exercise?> = _currentExercise.asStateFlow()

    private val _evaluationResult = MutableStateFlow<PronunciationResult?>(null)
    val evaluationResult: StateFlow<PronunciationResult?> = _evaluationResult.asStateFlow()

    private val _isLessonCompleted = MutableStateFlow(false)
    val isLessonCompleted: StateFlow<Boolean> = _isLessonCompleted.asStateFlow()

    // Current Level Progress Bar State (0% to 100%)
    private val _levelProgressState = MutableStateFlow(CurrentLevelProgressState())
    val levelProgressState: StateFlow<CurrentLevelProgressState> = _levelProgressState.asStateFlow()

    // Lottie-style & Particle Celebration State for passed pronunciation exercises
    private val _celebrationState = MutableStateFlow(CelebrationEffectState())
    val celebrationState: StateFlow<CelebrationEffectState> = _celebrationState.asStateFlow()

    // Dialogue State
    private val _activeDialogue = MutableStateFlow<DialogueScenario?>(null)
    val activeDialogue: StateFlow<DialogueScenario?> = _activeDialogue.asStateFlow()

    private val _dialogueTurnIndex = MutableStateFlow(0)
    val dialogueTurnIndex: StateFlow<Int> = _dialogueTurnIndex.asStateFlow()

    private val _showLanguageDialog = MutableStateFlow(false)
    val showLanguageDialog: StateFlow<Boolean> = _showLanguageDialog.asStateFlow()

    private val _showHeartsDialog = MutableStateFlow(false)
    val showHeartsDialog: StateFlow<Boolean> = _showHeartsDialog.asStateFlow()

    private val _showDailyStreakPopup = MutableStateFlow(false)
    val showDailyStreakPopup: StateFlow<Boolean> = _showDailyStreakPopup.asStateFlow()

    private val _isStreakBannerHidden = MutableStateFlow(false)
    val isStreakBannerHidden: StateFlow<Boolean> = _isStreakBannerHidden.asStateFlow()

    private var lastProcessedResult: PronunciationResult? = null

    private fun getTodayDateKey(): String {
        return java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
    }

    init {
        val todayKey = getTodayDateKey()
        _isStreakBannerHidden.value = userPrefsRepo.isStreakBannerHiddenForDate(todayKey)

        // Ensure initial hearts are synced with LivesState
        if (userProfile.value.hearts <= 0) {
            userPrefsRepo.refillHearts()
        }
        val initialHearts = userProfile.value.hearts.coerceIn(1, LivesState.MAX_LIVES)
        _livesState.value = _livesState.value.copy(currentLives = initialHearts)
        pronunciationViewModel.syncLives(initialHearts, LivesState.MAX_LIVES, false)

        // Seed initial streak baseline if needed and check once-per-day emergent popup
        viewModelScope.launch {
            streakRepository.seedInitialStreakIfEmpty()
            if (!userPrefsRepo.hasShownDailyStreakPopup(todayKey)) {
                userPrefsRepo.markDailyStreakPopupShown(todayKey)
                _showDailyStreakPopup.value = true
            }
        }

        // Direct synchronous callback from PronunciationViewModel for instant evaluation + audio feedback
        pronunciationViewModel.onEvaluationCompleted = { evalResult, recognized ->
            if (evalResult !== lastProcessedResult) {
                lastProcessedResult = evalResult
                handlePronunciationEvaluation(evalResult, recognized)
            }
        }

        // Collect real-time SpeechRecognizer results from PronunciationViewModel
        viewModelScope.launch {
            pronunciationViewModel.uiState.collect { state ->
                val evalResult = state.evaluationResult
                if (state.status == RecognitionStatus.SUCCESS && evalResult != null && evalResult !== lastProcessedResult) {
                    lastProcessedResult = evalResult
                    handlePronunciationEvaluation(evalResult, state.recognizedText)
                }
            }
        }
    }

    fun dismissDailyStreakPopup(hideBannerToo: Boolean = false) {
        val todayKey = getTodayDateKey()
        userPrefsRepo.markDailyStreakPopupShown(todayKey)
        _showDailyStreakPopup.value = false
        if (hideBannerToo) {
            setStreakBannerHidden(true)
        }
    }

    fun openDailyStreakPopup() {
        _showDailyStreakPopup.value = true
    }

    fun setStreakBannerHidden(hidden: Boolean) {
        val todayKey = getTodayDateKey()
        userPrefsRepo.setStreakBannerHiddenForDate(todayKey, hidden)
        _isStreakBannerHidden.value = hidden
    }

    fun navigateTo(screen: AppScreen) {
        pronunciationViewModel.cancelListening()
        ttsManager.stop()
        _currentScreen.value = screen
    }

    private fun updateCurrentLevelProgress(
        completedCount: Int,
        totalCount: Int,
        currentStep: Int,
        title: String
    ) {
        val safeTotal = totalCount.coerceAtLeast(1)
        val safeCompleted = completedCount.coerceIn(0, safeTotal)
        val safeStep = currentStep.coerceIn(1, safeTotal)
        val fraction = (safeCompleted.toFloat() / safeTotal.toFloat()).coerceIn(0f, 1f)
        val percentage = ((fraction * 100f).roundToInt()).coerceIn(0, 100)
        _levelProgressState.value = CurrentLevelProgressState(
            completedExercises = safeCompleted,
            totalExercises = safeTotal,
            currentStepNumber = safeStep,
            progressFraction = fraction,
            completionPercentage = percentage,
            levelTitle = title
        )
        pronunciationViewModel.syncLevelProgress(
            completedExercises = safeCompleted,
            totalExercises = safeTotal
        )
    }

    fun startLesson(module: LessonModule) {
        _activeModule.value = module
        _currentExerciseIndex.value = 0
        _isLessonCompleted.value = false
        _evaluationResult.value = null
        lastProcessedResult = null
        dismissCelebrationEffect()

        // Initialize full lives for the level attempt
        userPrefsRepo.refillHearts()
        _livesState.value = LivesState(
            currentLives = LivesState.MAX_LIVES,
            maxLives = LivesState.MAX_LIVES,
            errorsInCurrentLevel = 0,
            levelRestartCount = 0,
            isLevelRestartedByZeroLives = false,
            lastFailedResultBeforeRestart = null
        )
        pronunciationViewModel.resetLives(LivesState.MAX_LIVES)

        updateCurrentLevelProgress(
            completedCount = 0,
            totalCount = module.exercises.size.coerceAtLeast(1),
            currentStep = 1,
            title = module.title
        )

        if (module.exercises.isNotEmpty()) {
            val firstExercise = module.exercises.first()
            _currentExercise.value = firstExercise
            pronunciationViewModel.setTarget(
                targetPhrase = firstExercise.targetPhrase,
                language = userProfile.value.selectedLanguage,
                exercise = firstExercise
            )
            _currentScreen.value = AppScreen.EXERCISE_SESSION
            speakTargetPhrase()
        }
    }

    fun startCustomExercise(exercise: Exercise) {
        _activeModule.value = null
        _currentExerciseIndex.value = 0
        _isLessonCompleted.value = false
        _evaluationResult.value = null
        lastProcessedResult = null
        dismissCelebrationEffect()

        userPrefsRepo.refillHearts()
        _livesState.value = LivesState(
            currentLives = LivesState.MAX_LIVES,
            maxLives = LivesState.MAX_LIVES,
            errorsInCurrentLevel = 0,
            levelRestartCount = 0,
            isLevelRestartedByZeroLives = false,
            lastFailedResultBeforeRestart = null
        )
        pronunciationViewModel.resetLives(LivesState.MAX_LIVES)

        updateCurrentLevelProgress(
            completedCount = 0,
            totalCount = 1,
            currentStep = 1,
            title = "Práctica de Pronunciación"
        )

        _currentExercise.value = exercise
        pronunciationViewModel.setTarget(
            targetPhrase = exercise.targetPhrase,
            language = userProfile.value.selectedLanguage,
            exercise = exercise
        )
        _currentScreen.value = AppScreen.EXERCISE_SESSION
        speakTargetPhrase()
    }

    fun speakTargetPhrase(speedRate: Float = 1.0f) {
        val exercise = _currentExercise.value ?: return
        val language = userProfile.value.selectedLanguage
        ttsManager.speak(exercise.targetPhrase, language, speedRate)
    }

    fun speakWord(word: String) {
        val language = userProfile.value.selectedLanguage
        ttsManager.speak(word, language, speedRate = 0.85f)
    }

    fun startListeningForExercise() {
        val exercise = _currentExercise.value ?: return
        val language = userProfile.value.selectedLanguage
        _evaluationResult.value = null
        lastProcessedResult = null
        dismissCelebrationEffect()
        ttsManager.stop()
        pronunciationViewModel.startListening(
            targetPhrase = exercise.targetPhrase,
            language = language,
            exercise = exercise
        )
    }

    fun stopListening() {
        pronunciationViewModel.stopListening()
    }

    fun simulateSpeakingDemo(sampleText: String) {
        val language = userProfile.value.selectedLanguage
        if (_currentScreen.value == AppScreen.EXERCISE_SESSION) {
            val exercise = _currentExercise.value
            if (exercise != null) {
                pronunciationViewModel.setTarget(exercise.targetPhrase, language, exercise)
            }
        } else if (_currentScreen.value == AppScreen.DIALOGUE) {
            val scenario = _activeDialogue.value
            val currentTurn = scenario?.turns?.getOrNull(_dialogueTurnIndex.value)
            val expected = currentTurn?.expectedUserReply.orEmpty()
            if (scenario != null && expected.isNotBlank()) {
                pronunciationViewModel.setTarget(expected, scenario.language, null)
            }
        }
        pronunciationViewModel.simulateInput(sampleText)
    }

    /**
     * Simulates a mispronounced phrase so the user can test losing a life and level restart
     */
    fun simulatePronunciationError() {
        simulateSpeakingDemo("pronunciacion incorrecta error de prueba")
    }

    fun onSpeechRecognized(spokenText: String) {
        val language = userProfile.value.selectedLanguage
        if (_currentScreen.value == AppScreen.EXERCISE_SESSION) {
            val exercise = _currentExercise.value
            if (exercise != null) {
                pronunciationViewModel.setTarget(exercise.targetPhrase, language, exercise)
            }
        } else if (_currentScreen.value == AppScreen.DIALOGUE) {
            val scenario = _activeDialogue.value
            val currentTurn = scenario?.turns?.getOrNull(_dialogueTurnIndex.value)
            val expected = currentTurn?.expectedUserReply.orEmpty()
            if (scenario != null && expected.isNotBlank()) {
                pronunciationViewModel.setTarget(expected, scenario.language, null)
            }
        }
        pronunciationViewModel.processRecognizedSpeech(spokenText)
    }

    fun createSpeechIntent(): Intent {
        return pronunciationViewModel.createSpeechIntent(userProfile.value.selectedLanguage)
    }

    private fun handlePronunciationEvaluation(result: PronunciationResult, spokenText: String) {
        if (_currentScreen.value == AppScreen.EXERCISE_SESSION) {
            val language = userProfile.value.selectedLanguage

            if (result.isPassed) {
                soundEffectsManager.playSuccessChime(result.scorePercentage)
                triggerCelebrationEffect(
                    scorePercentage = result.scorePercentage,
                    xpEarned = result.xpEarned
                )
                _evaluationResult.value = result
                val module = _activeModule.value
                val totalExercises = module?.exercises?.size ?: 1
                val completedExercises = (_currentExerciseIndex.value + 1).coerceIn(0, totalExercises)
                updateCurrentLevelProgress(
                    completedCount = completedExercises,
                    totalCount = totalExercises,
                    currentStep = (_currentExerciseIndex.value + 1).coerceIn(1, totalExercises),
                    title = module?.title ?: "Práctica de Pronunciación"
                )
                userPrefsRepo.addXp(result.xpEarned)
                userPrefsRepo.updateAccuracy(result.scorePercentage)
                viewModelScope.launch {
                    streakRepository.recordPracticeSession(result.xpEarned, language.code, result.scorePercentage)
                }
            } else {
                dismissCelebrationEffect()
                loseLifeOnPronunciationError(result)
            }
        } else if (_currentScreen.value == AppScreen.DIALOGUE) {
            processDialogueSpokenText(spokenText)
        } else {
            // Fallback if evaluated outside main session screen
            if (result.isPassed) {
                soundEffectsManager.playSuccessChime(result.scorePercentage)
                triggerCelebrationEffect(
                    scorePercentage = result.scorePercentage,
                    xpEarned = result.xpEarned
                )
            } else {
                dismissCelebrationEffect()
                soundEffectsManager.playErrorSound(scorePercentage = result.scorePercentage, isLevelRestart = false)
            }
        }
    }

    /**
     * Triggers the Lottie-style vector and particle celebration burst for a passed pronunciation exercise.
     */
    fun triggerCelebrationEffect(scorePercentage: Int, xpEarned: Int = 15) {
        val current = _celebrationState.value
        _celebrationState.value = CelebrationEffectState(
            isActive = true,
            triggerId = current.triggerId + 1,
            totalCelebrationsTriggered = current.totalCelebrationsTriggered + 1,
            scorePercentage = scorePercentage,
            xpEarned = xpEarned,
            tier = CelebrationTier.fromScore(scorePercentage)
        )
    }

    /**
     * Replays the celebration particle burst and sound chime on user tap.
     */
    fun replayCelebrationEffect() {
        val current = _celebrationState.value
        val resultScore = _evaluationResult.value?.scorePercentage ?: current.scorePercentage.coerceAtLeast(80)
        val resultXp = _evaluationResult.value?.xpEarned ?: current.xpEarned.coerceAtLeast(15)
        soundEffectsManager.playSuccessChime(resultScore)
        _celebrationState.value = current.copy(
            isActive = true,
            triggerId = current.triggerId + 1,
            totalCelebrationsTriggered = current.totalCelebrationsTriggered + 1,
            scorePercentage = resultScore,
            xpEarned = resultXp,
            tier = CelebrationTier.fromScore(resultScore)
        )
    }

    fun dismissCelebrationEffect() {
        if (_celebrationState.value.isActive) {
            _celebrationState.value = _celebrationState.value.copy(isActive = false)
        }
    }

    /**
     * Replays the last pronunciation feedback sound effect (chime or subtle error tone).
     */
    fun replayLastFeedbackSound() {
        soundEffectsManager.replayLastFeedbackSound()
    }

    fun toggleSoundEffects(): Boolean {
        return soundEffectsManager.toggleSoundEnabled()
    }

    fun setSoundEffectsEnabled(enabled: Boolean) {
        soundEffectsManager.setSoundEnabled(enabled)
    }

    /**
     * Deducts one life when the user makes a pronunciation error and plays a subtle 'wrong' sound.
     * If remaining lives reach zero (0), restarts the current level from the first exercise.
     */
    fun loseLifeOnPronunciationError(failedResult: PronunciationResult? = null): Boolean {
        val currentLives = _livesState.value.currentLives
        val remainingLives = (currentLives - 1).coerceAtLeast(0)
        val reachedZero = remainingLives == 0

        soundEffectsManager.playErrorSound(
            scorePercentage = failedResult?.scorePercentage ?: 40,
            isLevelRestart = reachedZero
        )

        return if (reachedZero) {
            restartCurrentLevelOnZeroLives(failedResult)
            true
        } else {
            userPrefsRepo.setHearts(remainingLives)
            _livesState.value = _livesState.value.copy(
                currentLives = remainingLives,
                errorsInCurrentLevel = _livesState.value.errorsInCurrentLevel + 1,
                isLevelRestartedByZeroLives = false
            )
            pronunciationViewModel.syncLives(
                currentLives = remainingLives,
                maxLives = _livesState.value.maxLives,
                isLevelRestarted = false
            )
            if (failedResult != null) {
                _evaluationResult.value = failedResult
            }
            false
        }
    }

    /**
     * Resets the current level progress back to exercise 0 and restores lives when lives reach 0.
     */
    fun restartCurrentLevelOnZeroLives(failedResult: PronunciationResult? = null) {
        ttsManager.stop()
        pronunciationViewModel.cancelListening()
        _evaluationResult.value = null
        lastProcessedResult = null
        _isLessonCompleted.value = false
        dismissCelebrationEffect()

        // Restore lives to max for the restarted level attempt
        userPrefsRepo.refillHearts()
        val updatedRestartCount = _livesState.value.levelRestartCount + 1
        _livesState.value = _livesState.value.copy(
            currentLives = LivesState.MAX_LIVES,
            errorsInCurrentLevel = 0,
            levelRestartCount = updatedRestartCount,
            isLevelRestartedByZeroLives = true,
            lastFailedResultBeforeRestart = failedResult
        )
        pronunciationViewModel.syncLives(
            currentLives = LivesState.MAX_LIVES,
            maxLives = LivesState.MAX_LIVES,
            isLevelRestarted = true
        )

        // Reset level index to 0 and load the first exercise
        _currentExerciseIndex.value = 0
        val module = _activeModule.value
        val language = userProfile.value.selectedLanguage
        if (module != null && module.exercises.isNotEmpty()) {
            val firstExercise = module.exercises.first()
            _currentExercise.value = firstExercise
            pronunciationViewModel.setTarget(
                targetPhrase = firstExercise.targetPhrase,
                language = language,
                exercise = firstExercise
            )
            pronunciationViewModel.syncLives(
                currentLives = LivesState.MAX_LIVES,
                maxLives = LivesState.MAX_LIVES,
                isLevelRestarted = true
            )
            updateCurrentLevelProgress(
                completedCount = 0,
                totalCount = module.exercises.size.coerceAtLeast(1),
                currentStep = 1,
                title = module.title
            )
        } else {
            val ex = _currentExercise.value
            if (ex != null) {
                pronunciationViewModel.setTarget(
                    targetPhrase = ex.targetPhrase,
                    language = language,
                    exercise = ex
                )
                pronunciationViewModel.syncLives(
                    currentLives = LivesState.MAX_LIVES,
                    maxLives = LivesState.MAX_LIVES,
                    isLevelRestarted = true
                )
            }
            updateCurrentLevelProgress(
                completedCount = 0,
                totalCount = 1,
                currentStep = 1,
                title = "Práctica de Pronunciación"
            )
        }

        // Also reset dialogue turn if in a dialogue scenario
        _dialogueTurnIndex.value = 0
        val scenario = _activeDialogue.value
        if (scenario != null) {
            val firstTurn = scenario.turns.firstOrNull()
            if (firstTurn?.expectedUserReply != null) {
                pronunciationViewModel.setTarget(firstTurn.expectedUserReply, scenario.language, null)
                pronunciationViewModel.syncLives(
                    currentLives = LivesState.MAX_LIVES,
                    maxLives = LivesState.MAX_LIVES,
                    isLevelRestarted = true
                )
            }
            updateCurrentLevelProgress(
                completedCount = 0,
                totalCount = scenario.turns.size.coerceAtLeast(1),
                currentStep = 1,
                title = scenario.title
            )
        }
    }

    /**
     * Dismisses the zero-lives level restart notification and plays the first exercise audio.
     */
    fun acknowledgeLevelRestart() {
        _livesState.value = _livesState.value.copy(
            isLevelRestartedByZeroLives = false,
            lastFailedResultBeforeRestart = null
        )
        pronunciationViewModel.acknowledgeLevelRestart()
        if (_currentScreen.value == AppScreen.EXERCISE_SESSION) {
            speakTargetPhrase()
        } else if (_currentScreen.value == AppScreen.DIALOGUE) {
            playCurrentDialogueTurnAiVoice()
        }
    }

    fun onContinueNextExercise() {
        _evaluationResult.value = null
        lastProcessedResult = null
        dismissCelebrationEffect()
        pronunciationViewModel.reset()

        val module = _activeModule.value
        if (module == null) {
            // Standalone exercise finished
            updateCurrentLevelProgress(
                completedCount = 1,
                totalCount = 1,
                currentStep = 1,
                title = "Práctica de Pronunciación"
            )
            _currentScreen.value = AppScreen.PATH_LEARN
            return
        }

        val totalExercises = module.exercises.size.coerceAtLeast(1)
        val nextIndex = _currentExerciseIndex.value + 1
        if (nextIndex < module.exercises.size) {
            val nextEx = module.exercises[nextIndex]
            _currentExerciseIndex.value = nextIndex
            _currentExercise.value = nextEx
            updateCurrentLevelProgress(
                completedCount = nextIndex,
                totalCount = totalExercises,
                currentStep = nextIndex + 1,
                title = module.title
            )
            pronunciationViewModel.setTarget(
                targetPhrase = nextEx.targetPhrase,
                language = userProfile.value.selectedLanguage,
                exercise = nextEx
            )
            speakTargetPhrase()
        } else {
            // Module completed!
            updateCurrentLevelProgress(
                completedCount = totalExercises,
                totalCount = totalExercises,
                currentStep = totalExercises,
                title = module.title
            )
            _isLessonCompleted.value = true
            userPrefsRepo.markLessonCompleted(module.id)
            userPrefsRepo.addXp(25) // Completion bonus
        }
    }

    fun retryCurrentExercise() {
        _evaluationResult.value = null
        lastProcessedResult = null
        dismissCelebrationEffect()
        val ex = _currentExercise.value
        if (ex != null) {
            pronunciationViewModel.setTarget(
                targetPhrase = ex.targetPhrase,
                language = userProfile.value.selectedLanguage,
                exercise = ex
            )
        } else {
            pronunciationViewModel.reset()
        }
        speakTargetPhrase(0.8f)
    }

    fun selectLanguage(language: Language) {
        userPrefsRepo.setSelectedLanguage(language)
        _showLanguageDialog.value = false
        // Return to path when language changes
        _currentScreen.value = AppScreen.PATH_LEARN
        _activeModule.value = null
        _currentExercise.value = null
        _evaluationResult.value = null
        lastProcessedResult = null
        pronunciationViewModel.reset()
    }

    fun setLanguageDialogVisible(visible: Boolean) {
        _showLanguageDialog.value = visible
    }

    fun setHeartsDialogVisible(visible: Boolean) {
        _showHeartsDialog.value = visible
    }

    fun refillHearts() {
        userPrefsRepo.refillHearts()
        _livesState.value = _livesState.value.copy(
            currentLives = LivesState.MAX_LIVES,
            errorsInCurrentLevel = 0,
            isLevelRestartedByZeroLives = false
        )
        pronunciationViewModel.resetLives(LivesState.MAX_LIVES)
        _showHeartsDialog.value = false
    }

    // Dialogue Scenarios
    fun startDialogueScenario(scenario: DialogueScenario) {
        _activeDialogue.value = scenario
        _dialogueTurnIndex.value = 0
        _evaluationResult.value = null
        lastProcessedResult = null

        userPrefsRepo.refillHearts()
        _livesState.value = LivesState(
            currentLives = LivesState.MAX_LIVES,
            maxLives = LivesState.MAX_LIVES,
            errorsInCurrentLevel = 0,
            levelRestartCount = 0,
            isLevelRestartedByZeroLives = false,
            lastFailedResultBeforeRestart = null
        )
        pronunciationViewModel.resetLives(LivesState.MAX_LIVES)

        updateCurrentLevelProgress(
            completedCount = 0,
            totalCount = scenario.turns.size.coerceAtLeast(1),
            currentStep = 1,
            title = scenario.title
        )

        val firstTurn = scenario.turns.firstOrNull()
        if (firstTurn?.expectedUserReply != null) {
            pronunciationViewModel.setTarget(firstTurn.expectedUserReply, scenario.language, null)
        }
        _currentScreen.value = AppScreen.DIALOGUE
        playCurrentDialogueTurnAiVoice()
    }

    fun playCurrentDialogueTurnAiVoice() {
        val scenario = _activeDialogue.value ?: return
        val currentTurn = scenario.turns.getOrNull(_dialogueTurnIndex.value) ?: return
        if (currentTurn.sender == "AI") {
            ttsManager.speak(currentTurn.targetText, scenario.language)
        }
    }

    fun startListeningForDialogue() {
        val scenario = _activeDialogue.value ?: return
        val currentTurn = scenario.turns.getOrNull(_dialogueTurnIndex.value)
        val expected = currentTurn?.expectedUserReply.orEmpty()
        _evaluationResult.value = null
        lastProcessedResult = null
        ttsManager.stop()
        pronunciationViewModel.startListening(
            targetPhrase = expected,
            language = scenario.language,
            exercise = null
        )
    }

    private fun processDialogueSpokenText(spokenText: String) {
        val scenario = _activeDialogue.value ?: return
        val currentTurn = scenario.turns.getOrNull(_dialogueTurnIndex.value) ?: return
        val expected = currentTurn.expectedUserReply ?: return

        val result = PronunciationEvaluator.evaluate(
            targetPhrase = expected,
            spokenPhrase = spokenText,
            exercise = null,
            language = scenario.language
        )

        if (result.isPassed) {
            soundEffectsManager.playSuccessChime(result.scorePercentage)
            triggerCelebrationEffect(
                scorePercentage = result.scorePercentage,
                xpEarned = 20
            )
            _evaluationResult.value = result
            val totalTurns = scenario.turns.size.coerceAtLeast(1)
            val completedTurns = (_dialogueTurnIndex.value + 1).coerceIn(0, totalTurns)
            updateCurrentLevelProgress(
                completedCount = completedTurns,
                totalCount = totalTurns,
                currentStep = (_dialogueTurnIndex.value + 1).coerceIn(1, totalTurns),
                title = scenario.title
            )
            userPrefsRepo.addXp(20)
            userPrefsRepo.updateAccuracy(result.scorePercentage)
            viewModelScope.launch {
                streakRepository.recordPracticeSession(20, scenario.language.code, result.scorePercentage)
            }
        } else {
            dismissCelebrationEffect()
            loseLifeOnPronunciationError(result)
        }
    }

    fun advanceDialogueTurn() {
        _evaluationResult.value = null
        lastProcessedResult = null
        dismissCelebrationEffect()
        val scenario = _activeDialogue.value ?: return
        val totalTurns = scenario.turns.size.coerceAtLeast(1)
        val next = _dialogueTurnIndex.value + 1
        if (next < scenario.turns.size) {
            _dialogueTurnIndex.value = next
            updateCurrentLevelProgress(
                completedCount = next,
                totalCount = totalTurns,
                currentStep = next + 1,
                title = scenario.title
            )
            val nextTurn = scenario.turns.getOrNull(next)
            if (nextTurn?.expectedUserReply != null) {
                pronunciationViewModel.setTarget(nextTurn.expectedUserReply, scenario.language, null)
            } else {
                pronunciationViewModel.reset()
            }
            playCurrentDialogueTurnAiVoice()
        } else {
            // Dialogue completed!
            updateCurrentLevelProgress(
                completedCount = totalTurns,
                totalCount = totalTurns,
                currentStep = totalTurns,
                title = scenario.title
            )
            pronunciationViewModel.reset()
            userPrefsRepo.addXp(30)
            _currentScreen.value = AppScreen.PATH_LEARN
        }
    }

    override fun onCleared() {
        super.onCleared()
        pronunciationViewModel.reset()
        ttsManager.shutdown()
        soundEffectsManager.shutdown()
    }
}
