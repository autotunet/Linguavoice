package com.example.ui.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioFeedbackState
import com.example.audio.NativeAudioComparisonManager
import com.example.audio.NativeAudioComparisonState
import com.example.audio.PronunciationEvaluator
import com.example.audio.SoundEffectsManager
import com.example.audio.SpeechState
import com.example.audio.TtsManager
import com.example.data.local.AppDatabase
import com.example.data.local.LanguagePreferenceEntity
import com.example.data.model.DialogueScenario
import com.example.data.model.Exercise
import com.example.data.model.Language
import com.example.data.model.LearningFocusGoal
import com.example.data.model.LessonModule
import com.example.data.model.LevelSystem
import com.example.data.model.PronunciationResult
import com.example.data.model.SpokenLanguage
import com.example.data.model.UserLevelInfo
import com.example.data.model.UserProfile
import com.example.data.repository.ExerciseRepository
import com.example.data.repository.LanguagePreferenceRepository
import com.example.data.repository.StreakInfo
import com.example.data.repository.StreakRepository
import com.example.data.repository.UserPreferencesRepository
import com.example.ui.components.CelebrationEffectState
import com.example.ui.components.CelebrationTier
import com.example.ui.components.DailyGoalCelebrationState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.roundToInt

enum class AppScreen {
    PATH_LEARN,
    EXERCISE_SESSION,
    FREE_PRACTICE,
    DIALOGUE,
    PROFILE,
    LANGUAGE_SELECTION
}

data class LivesState(
    val currentLives: Int = MAX_LIVES,
    val maxLives: Int = MAX_LIVES,
    val errorsInCurrentLevel: Int = 0,
    val levelRestartCount: Int = 0,
    val isLevelRestartedByZeroLives: Boolean = false,
    val isGameOver: Boolean = false,
    val lastFailedResultBeforeRestart: PronunciationResult? = null,
    val accuracyThreshold: Int = PASSING_ACCURACY_THRESHOLD
) {
    companion object {
        const val MAX_LIVES = 5
        const val PASSING_ACCURACY_THRESHOLD = 60
    }
}

data class CurrentLevelProgressState(
    val completedExercises: Int = 0,
    val totalExercises: Int = 1,
    val currentStepNumber: Int = 1,
    val progressFraction: Float = 0f,
    val completionPercentage: Int = 0,
    val levelTitle: String = "",
    val lastIncrementDeltaPercentage: Int = 0,
    val completedTaskIndices: Set<Int> = emptySet(),
    val lastTaskPassed: Boolean = false
)

class LanguageGameViewModel(application: Application) : AndroidViewModel(application) {

    private val userPrefsRepo = UserPreferencesRepository(application)
    val userProfile: StateFlow<UserProfile> = userPrefsRepo.userProfile

    private val appDatabase = AppDatabase.getInstance(application)
    val streakRepository = StreakRepository(appDatabase.practiceDayDao())
    val languagePreferenceRepository = LanguagePreferenceRepository(appDatabase.languagePreferenceDao())

    private val initialRoomPref: LanguagePreferenceEntity =
        languagePreferenceRepository.getOrSeedPreferenceSync(
            fallbackLanguage = userPrefsRepo.userProfile.value.selectedLanguage,
            fallbackSpokenLanguage = userPrefsRepo.userProfile.value.spokenLanguage
        )

    private val _targetLanguagePreference = MutableStateFlow(initialRoomPref)
    val targetLanguagePreference: StateFlow<LanguagePreferenceEntity> = _targetLanguagePreference.asStateFlow()

    private val _selectedSpokenLanguage = MutableStateFlow(initialRoomPref.toSpokenLanguage())
    val selectedSpokenLanguage: StateFlow<SpokenLanguage> = _selectedSpokenLanguage.asStateFlow()

    private val _selectedTargetLanguage = MutableStateFlow(initialRoomPref.toLanguage())
    val selectedTargetLanguage: StateFlow<Language> = _selectedTargetLanguage.asStateFlow()

    private val _selectedLearningFocus = MutableStateFlow(initialRoomPref.toLearningFocusGoal())
    val selectedLearningFocus: StateFlow<LearningFocusGoal> = _selectedLearningFocus.asStateFlow()

    private val _showLanguagePairOnboarding = MutableStateFlow(!initialRoomPref.hasCompletedLanguageOnboarding)
    val showLanguagePairOnboarding: StateFlow<Boolean> = _showLanguagePairOnboarding.asStateFlow()

    private val _personalizedModules = MutableStateFlow(
        ExerciseRepository.getPersonalizedModulesForLanguage(
            initialRoomPref.toLanguage(),
            initialRoomPref.toLearningFocusGoal()
        )
    )
    val personalizedModules: StateFlow<List<LessonModule>> = _personalizedModules.asStateFlow()

    private val _personalizedModulesByLevel = MutableStateFlow(
        ExerciseRepository.getPersonalizedModulesGroupedByLevel(
            initialRoomPref.toLanguage(),
            initialRoomPref.toLearningFocusGoal()
        )
    )
    val personalizedModulesByLevel: StateFlow<Map<Int, List<LessonModule>>> = _personalizedModulesByLevel.asStateFlow()

    private val _personalizedDialogues = MutableStateFlow(
        ExerciseRepository.getDialogueScenarios(initialRoomPref.toLanguage())
    )
    val personalizedDialogues: StateFlow<List<DialogueScenario>> = _personalizedDialogues.asStateFlow()

    private val initialCal = Calendar.getInstance()
    private val initialDailyXpGoal = userPrefsRepo.getDailyXpGoal()

    val streakInfo: StateFlow<StreakInfo> = streakRepository.streakInfoFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = StreakInfo(
                currentStreak = 4,
                bestStreak = 4,
                practicedToday = true,
                totalDaysPracticed = 5,
                currentWeekDays = emptyList(),
                currentMonthTitle = StreakRepository.formatMonthTitleEs(
                    initialCal.get(Calendar.YEAR),
                    initialCal.get(Calendar.MONTH)
                ),
                calendarDays = streakRepository.buildMonthCalendarDays(
                    year = initialCal.get(Calendar.YEAR),
                    monthZeroBased = initialCal.get(Calendar.MONTH),
                    daysMap = emptyMap(),
                    dailyXpGoal = initialDailyXpGoal
                ),
                todayXpEarned = 30,
                todayExercisesCompleted = 2,
                dailyXpGoal = initialDailyXpGoal,
                dailyExercisesGoal = StreakRepository.DEFAULT_DAILY_EXERCISES_GOAL,
                dailyGoalProgressFraction = (30f / initialDailyXpGoal.toFloat()).coerceIn(0f, 1f),
                dailyGoalProgressPercent = ((30f / initialDailyXpGoal.toFloat()) * 100f).roundToInt().coerceIn(0, 100),
                isDailyGoalReached = 30 >= initialDailyXpGoal
            )
        )

    // Daily Goal Progress & Celebration State ("Meta diaria y animación de celebración")
    private val _dailyGoalState = MutableStateFlow(
        DailyGoalCelebrationState(
            isCelebrationVisible = false,
            celebrationTriggerCount = 0,
            dailyXpTarget = initialDailyXpGoal,
            dailyExercisesTarget = StreakRepository.DEFAULT_DAILY_EXERCISES_GOAL,
            todayXpEarned = 30,
            todayExercisesCompleted = 2,
            isDailyGoalReached = 30 >= initialDailyXpGoal,
            progressFraction = (30f / initialDailyXpGoal.toFloat()).coerceIn(0f, 1f),
            progressPercentage = ((30f / initialDailyXpGoal.toFloat()) * 100f).roundToInt().coerceIn(0, 100),
            bonusGemsAwarded = 25,
            streakDaysCount = 4
        )
    )
    val dailyGoalState: StateFlow<DailyGoalCelebrationState> = _dailyGoalState.asStateFlow()

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

    // Native speaker audio file (.wav) generator, player, and waveform comparison manager
    val nativeAudioComparisonManager = NativeAudioComparisonManager(application)
    val nativeAudioComparisonState: StateFlow<NativeAudioComparisonState> = nativeAudioComparisonManager.state

    private val _currentScreen = MutableStateFlow(AppScreen.PATH_LEARN)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Dedicated ViewModel managing user 'hearts' (lives) and 'streak' count
    val heartsAndStreakViewModel = HeartsAndStreakViewModel(application)
    val heartsAndStreakState: StateFlow<HeartsAndStreakUiState> = heartsAndStreakViewModel.uiState

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

        // Ensure Room language pair preference is synchronized with UserProfile on startup
        userPrefsRepo.setLanguagePair(
            spokenLanguage = initialRoomPref.toSpokenLanguage(),
            targetLanguage = initialRoomPref.toLanguage(),
            hasConfigured = initialRoomPref.hasCompletedLanguageOnboarding
        )

        // Observe Room language preference Flow to keep lessons personalized reactively
        viewModelScope.launch {
            languagePreferenceRepository.targetLanguagePreferenceFlow.collect { entity ->
                val spoken = entity.toSpokenLanguage()
                val lang = entity.toLanguage()
                val focus = entity.toLearningFocusGoal()
                _targetLanguagePreference.value = entity
                _selectedSpokenLanguage.value = spoken
                _selectedTargetLanguage.value = lang
                _selectedLearningFocus.value = focus
                if (userPrefsRepo.userProfile.value.selectedLanguage != lang ||
                    userPrefsRepo.userProfile.value.spokenLanguage != spoken
                ) {
                    userPrefsRepo.setLanguagePair(
                        spokenLanguage = spoken,
                        targetLanguage = lang,
                        hasConfigured = entity.hasCompletedLanguageOnboarding
                    )
                }
                _personalizedModules.value = ExerciseRepository.getPersonalizedModulesForLanguage(lang, focus)
                _personalizedModulesByLevel.value = ExerciseRepository.getPersonalizedModulesGroupedByLevel(lang, focus)
                _personalizedDialogues.value = ExerciseRepository.getDialogueScenarios(lang)
            }
        }

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

        // Sync DailyGoalCelebrationState with live StreakInfo updates from Room
        viewModelScope.launch {
            streakRepository.streakInfoFlow.collect { info ->
                val currentGoal = _dailyGoalState.value
                val targetXp = currentGoal.dailyXpTarget.coerceAtLeast(15)
                val syncedTodayXp = maxOf(currentGoal.todayXpEarned, info.todayXpEarned)
                val syncedTodayExercises = maxOf(currentGoal.todayExercisesCompleted, info.todayExercisesCompleted)
                val fraction = (syncedTodayXp.toFloat() / targetXp.toFloat()).coerceIn(0f, 1f)
                val pct = ((fraction * 100f).roundToInt()).coerceIn(0, 100)
                val reached = syncedTodayXp >= targetXp || syncedTodayExercises >= currentGoal.dailyExercisesTarget

                _dailyGoalState.value = currentGoal.copy(
                    todayXpEarned = syncedTodayXp,
                    todayExercisesCompleted = syncedTodayExercises,
                    isDailyGoalReached = reached,
                    progressFraction = fraction,
                    progressPercentage = pct,
                    streakDaysCount = info.currentStreak.coerceAtLeast(1)
                )
            }
        }

        // Direct synchronous callback from PronunciationViewModel for instant evaluation + audio feedback
        pronunciationViewModel.onEvaluationCompleted = { evalResult, recognized ->
            if (evalResult !== lastProcessedResult) {
                lastProcessedResult = evalResult
                handlePronunciationEvaluation(evalResult, recognized)
            }
        }

        pronunciationViewModel.onBufferCaptured = { buffer ->
            nativeAudioComparisonManager.onUserPcmBufferReceived(buffer)
        }

        // Collect real-time SpeechRecognizer results and live RMS amplitude from PronunciationViewModel
        viewModelScope.launch {
            pronunciationViewModel.uiState.collect { state ->
                if (state.isListening) {
                    nativeAudioComparisonManager.onUserRmsSample(state.rmsDb)
                }
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
        nativeAudioComparisonManager.stopNativePlayback()
        _currentScreen.value = screen
    }

    private fun updateCurrentLevelProgress(
        completedCount: Int,
        totalCount: Int,
        currentStep: Int,
        title: String,
        taskPassed: Boolean = completedCount > _levelProgressState.value.completedExercises
    ) {
        val previousPercentage = if (_levelProgressState.value.levelTitle == title) {
            _levelProgressState.value.completionPercentage
        } else {
            0
        }
        val safeTotal = totalCount.coerceAtLeast(1)
        val safeCompleted = completedCount.coerceIn(0, safeTotal)
        val safeStep = currentStep.coerceIn(1, safeTotal)
        val fraction = (safeCompleted.toFloat() / safeTotal.toFloat()).coerceIn(0f, 1f)
        val percentage = ((fraction * 100f).roundToInt()).coerceIn(0, 100)
        val deltaPct = (percentage - previousPercentage).coerceAtLeast(0)
        val completedIndices = (0 until safeCompleted).toSet()
        _levelProgressState.value = CurrentLevelProgressState(
            completedExercises = safeCompleted,
            totalExercises = safeTotal,
            currentStepNumber = safeStep,
            progressFraction = fraction,
            completionPercentage = percentage,
            levelTitle = title,
            lastIncrementDeltaPercentage = if (safeCompleted == 0) 0 else deltaPct,
            completedTaskIndices = completedIndices,
            lastTaskPassed = taskPassed && safeCompleted > 0
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
        heartsAndStreakViewModel.refillHearts()
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
            val language = userProfile.value.selectedLanguage
            pronunciationViewModel.setTarget(
                targetPhrase = firstExercise.targetPhrase,
                language = language,
                exercise = firstExercise
            )
            nativeAudioComparisonManager.prepareForPhrase(
                targetPhrase = firstExercise.targetPhrase,
                language = language,
                exerciseId = firstExercise.id
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
        heartsAndStreakViewModel.refillHearts()
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
        val language = userProfile.value.selectedLanguage
        pronunciationViewModel.setTarget(
            targetPhrase = exercise.targetPhrase,
            language = language,
            exercise = exercise
        )
        nativeAudioComparisonManager.prepareForPhrase(
            targetPhrase = exercise.targetPhrase,
            language = language,
            exerciseId = exercise.id
        )
        _currentScreen.value = AppScreen.EXERCISE_SESSION
        speakTargetPhrase()
    }

    fun speakTargetPhrase(speedRate: Float = 1.0f) {
        val exercise = _currentExercise.value ?: return
        val language = userProfile.value.selectedLanguage
        ttsManager.speak(exercise.targetPhrase, language, speedRate)
    }

    /**
     * Plays the native speaker `.wav` audio file for the active phrase before the user records
     * their own voice, enabling side-by-side waveform inspection and comparison.
     */
    fun playNativeReferenceAudio(speedRate: Float = 1.0f) {
        nativeAudioComparisonManager.playNativeSpeakerAudio(speedRate = speedRate) { phrase, lang, rate ->
            ttsManager.speak(phrase, lang, rate)
        }
    }

    fun stopNativeReferenceAudio() {
        nativeAudioComparisonManager.stopNativePlayback()
        ttsManager.stop()
    }

    /**
     * Plays back the user's recorded microphone `.wav` audio file and speaks the recognized text
     * so the user and the app can listen to what was spoken into the microphone.
     */
    fun playUserRecordedAudio() {
        nativeAudioComparisonManager.playUserRecordedAudio { spokenPhrase, lang, rate ->
            ttsManager.speak(spokenPhrase, lang, rate)
        }
    }

    fun stopUserRecordedAudio() {
        nativeAudioComparisonManager.stopUserPlayback()
        ttsManager.stop()
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
        nativeAudioComparisonManager.onUserRecordingStarted()
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
     * Simulates an "Almost Correct" (Casi correcta, 60%-84%) spoken phrase so the user can see
     * the intermediate visual feedback tier and word-level coaching.
     */
    fun simulateAlmostCorrectPronunciation() {
        val targetPhrase = when (_currentScreen.value) {
            AppScreen.DIALOGUE -> {
                val scenario = _activeDialogue.value
                val currentTurn = scenario?.turns?.getOrNull(_dialogueTurnIndex.value)
                currentTurn?.expectedUserReply.orEmpty()
            }
            else -> _currentExercise.value?.targetPhrase.orEmpty()
        }
        val sampleAlmost = PronunciationEvaluator.buildAlmostCorrectSample(targetPhrase)
        simulateSpeakingDemo(sampleAlmost)
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
            nativeAudioComparisonManager.onUserRecordingCompleted(result, spokenText)

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
                recordDailyPracticeProgress(xpAdded = result.xpEarned, exercisesAdded = 1)
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
            nativeAudioComparisonManager.onUserRecordingCompleted(result, spokenText)
            _evaluationResult.value = result
            if (result.isPassed) {
                soundEffectsManager.playSuccessChime(result.scorePercentage)
                triggerCelebrationEffect(
                    scorePercentage = result.scorePercentage,
                    xpEarned = result.xpEarned
                )
                recordDailyPracticeProgress(xpAdded = result.xpEarned, exercisesAdded = 1)
            } else {
                dismissCelebrationEffect()
                soundEffectsManager.playErrorSound(scorePercentage = result.scorePercentage, isLevelRestart = false)
            }
        }
    }

    /**
     * Updates daily goal progress when the user earns XP or completes an exercise,
     * and automatically triggers the Daily Goal Celebration animation when the target is reached.
     */
    fun recordDailyPracticeProgress(xpAdded: Int, exercisesAdded: Int = 1) {
        val current = _dailyGoalState.value
        val wasReachedBefore = current.isDailyGoalReached
        val newTodayXp = (current.todayXpEarned + xpAdded.coerceAtLeast(0))
        val newTodayExercises = (current.todayExercisesCompleted + exercisesAdded.coerceAtLeast(0))
        val targetXp = current.dailyXpTarget.coerceAtLeast(15)
        val fraction = (newTodayXp.toFloat() / targetXp.toFloat()).coerceIn(0f, 1f)
        val pct = ((fraction * 100f).roundToInt()).coerceIn(0, 100)
        val isNowReached = newTodayXp >= targetXp

        val shouldCelebrateNow = isNowReached && !wasReachedBefore
        if (shouldCelebrateNow) {
            userPrefsRepo.markDailyGoalCelebratedForDate(getTodayDateKey())
        }

        _dailyGoalState.value = current.copy(
            todayXpEarned = newTodayXp,
            todayExercisesCompleted = newTodayExercises,
            isDailyGoalReached = isNowReached,
            progressFraction = fraction,
            progressPercentage = pct,
            isCelebrationVisible = if (shouldCelebrateNow) true else current.isCelebrationVisible,
            celebrationTriggerCount = if (shouldCelebrateNow) {
                current.celebrationTriggerCount + 1
            } else {
                current.celebrationTriggerCount
            }
        )
    }

    /**
     * Sets the user's daily XP goal target (e.g., 30, 50, 100 EXP) and triggers celebration if met.
     */
    fun setDailyXpGoal(targetXp: Int) {
        val safeTarget = targetXp.coerceIn(15, 300)
        userPrefsRepo.setDailyXpGoal(safeTarget)
        val current = _dailyGoalState.value
        val wasReached = current.isDailyGoalReached
        val fraction = (current.todayXpEarned.toFloat() / safeTarget.toFloat()).coerceIn(0f, 1f)
        val pct = ((fraction * 100f).roundToInt()).coerceIn(0, 100)
        val isNowReached = current.todayXpEarned >= safeTarget

        val newlyReached = isNowReached && !wasReached

        _dailyGoalState.value = current.copy(
            dailyXpTarget = safeTarget,
            isDailyGoalReached = isNowReached,
            progressFraction = fraction,
            progressPercentage = pct,
            isCelebrationVisible = if (newlyReached) true else current.isCelebrationVisible,
            celebrationTriggerCount = if (newlyReached) {
                current.celebrationTriggerCount + 1
            } else {
                current.celebrationTriggerCount
            }
        )
    }

    /**
     * Triggers the Daily Goal Celebration animation and plays the celebratory chime.
     */
    fun triggerDailyGoalCelebration() {
        val current = _dailyGoalState.value
        soundEffectsManager.playSuccessChime(100)
        val updatedXp = maxOf(current.todayXpEarned, current.dailyXpTarget)
        _dailyGoalState.value = current.copy(
            todayXpEarned = updatedXp,
            isDailyGoalReached = true,
            progressFraction = 1f,
            progressPercentage = 100,
            isCelebrationVisible = true,
            celebrationTriggerCount = current.celebrationTriggerCount + 1
        )
    }

    /**
     * Replays the Daily Goal Celebration animation and chime.
     */
    fun replayDailyGoalCelebration() {
        val current = _dailyGoalState.value
        soundEffectsManager.playSuccessChime(100)
        _dailyGoalState.value = current.copy(
            isCelebrationVisible = true,
            celebrationTriggerCount = current.celebrationTriggerCount + 1
        )
    }

    /**
     * Dismisses the Daily Goal Celebration overlay.
     */
    fun dismissDailyGoalCelebration() {
        if (_dailyGoalState.value.isCelebrationVisible) {
            _dailyGoalState.value = _dailyGoalState.value.copy(isCelebrationVisible = false)
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

        heartsAndStreakViewModel.onExerciseFailed(failedResult?.scorePercentage ?: 40)

        return if (reachedZero) {
            restartCurrentLevelOnZeroLives(failedResult)
            true
        } else {
            userPrefsRepo.setHearts(remainingLives)
            _livesState.value = _livesState.value.copy(
                currentLives = remainingLives,
                errorsInCurrentLevel = _livesState.value.errorsInCurrentLevel + 1,
                isLevelRestartedByZeroLives = false,
                isGameOver = false
            )
            pronunciationViewModel.syncLives(
                currentLives = remainingLives,
                maxLives = _livesState.value.maxLives,
                isLevelRestarted = false,
                isGameOver = false
            )
            if (failedResult != null) {
                _evaluationResult.value = failedResult
            }
            false
        }
    }

    /**
     * Resets the current level progress back to exercise 0 and restores lives when lives reach 0,
     * triggering the Game Over state.
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
        heartsAndStreakViewModel.refillHearts()
        val updatedRestartCount = _livesState.value.levelRestartCount + 1
        _livesState.value = _livesState.value.copy(
            currentLives = LivesState.MAX_LIVES,
            errorsInCurrentLevel = 0,
            levelRestartCount = updatedRestartCount,
            isLevelRestartedByZeroLives = true,
            isGameOver = true,
            lastFailedResultBeforeRestart = failedResult
        )
        pronunciationViewModel.syncLives(
            currentLives = LivesState.MAX_LIVES,
            maxLives = LivesState.MAX_LIVES,
            isLevelRestarted = true,
            isGameOver = true
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
            nativeAudioComparisonManager.prepareForPhrase(
                targetPhrase = firstExercise.targetPhrase,
                language = language,
                exerciseId = firstExercise.id
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
                nativeAudioComparisonManager.prepareForPhrase(
                    targetPhrase = ex.targetPhrase,
                    language = language,
                    exerciseId = ex.id
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
                nativeAudioComparisonManager.prepareForPhrase(
                    targetPhrase = firstTurn.expectedUserReply,
                    language = scenario.language,
                    exerciseId = "${scenario.id}_turn_0"
                )
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
        heartsAndStreakViewModel.refillHearts()
        _livesState.value = _livesState.value.copy(
            currentLives = LivesState.MAX_LIVES,
            isLevelRestartedByZeroLives = false,
            isGameOver = false,
            lastFailedResultBeforeRestart = null
        )
        pronunciationViewModel.acknowledgeLevelRestart()
        if (_currentScreen.value == AppScreen.EXERCISE_SESSION) {
            speakTargetPhrase()
        } else if (_currentScreen.value == AppScreen.DIALOGUE) {
            playCurrentDialogueTurnAiVoice()
        }
    }

    /**
     * Handles Game Over dismissal, resets zero lives flag, and restarts level attempt.
     */
    fun acknowledgeGameOver() {
        acknowledgeLevelRestart()
    }

    /**
     * Restarts the level from Game Over dialog.
     */
    fun restartLevelFromGameOver() {
        acknowledgeLevelRestart()
    }

    /**
     * Exits to the learning map from the Game Over dialog.
     */
    fun exitGameOverToMap() {
        _livesState.value = _livesState.value.copy(
            isLevelRestartedByZeroLives = false,
            isGameOver = false,
            lastFailedResultBeforeRestart = null
        )
        pronunciationViewModel.acknowledgeLevelRestart()
        navigateTo(AppScreen.PATH_LEARN)
    }

    /**
     * Checks if pronunciation accuracy is below the passing threshold.
     * If accuracy < threshold or !isPassed, deducts 1 life and triggers Game Over if lives reach 0.
     * Returns true if passed (accuracy >= threshold), false if a life was deducted.
     */
    fun checkAccuracyAndDeductLifeIfBelowThreshold(
        result: PronunciationResult,
        threshold: Int = LivesState.PASSING_ACCURACY_THRESHOLD
    ): Boolean {
        return if (result.scorePercentage < threshold || !result.isPassed) {
            loseLifeOnPronunciationError(result)
            false
        } else {
            true
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
            val language = userProfile.value.selectedLanguage
            pronunciationViewModel.setTarget(
                targetPhrase = nextEx.targetPhrase,
                language = language,
                exercise = nextEx
            )
            nativeAudioComparisonManager.prepareForPhrase(
                targetPhrase = nextEx.targetPhrase,
                language = language,
                exerciseId = nextEx.id
            )
            speakTargetPhrase()
        } else {
            // Module completed! Update hearts & streak via HeartsAndStreakViewModel
            updateCurrentLevelProgress(
                completedCount = totalExercises,
                totalCount = totalExercises,
                currentStep = totalExercises,
                title = module.title
            )
            _isLessonCompleted.value = true
            val updatedHeartsAndStreak = heartsAndStreakViewModel.onLessonCompleted(
                lessonId = module.id,
                xpEarned = 25,
                accuracyScore = userProfile.value.accuracyAverage,
                languageCode = userProfile.value.selectedLanguage.code
            )
            _livesState.value = _livesState.value.copy(
                currentLives = updatedHeartsAndStreak.hearts
            )
            recordDailyPracticeProgress(xpAdded = 25, exercisesAdded = 1)
        }
    }

    /**
     * Explicitly marks a lesson as completed and updates hearts and streak count.
     */
    fun onLessonCompleted(
        lessonId: String? = _activeModule.value?.id,
        xpEarned: Int = 25,
        accuracyScore: Int = 95
    ): HeartsAndStreakUiState {
        val state = heartsAndStreakViewModel.onLessonCompleted(
            lessonId = lessonId,
            xpEarned = xpEarned,
            accuracyScore = accuracyScore,
            languageCode = userProfile.value.selectedLanguage.code
        )
        _livesState.value = _livesState.value.copy(
            currentLives = state.hearts,
            isGameOver = state.isOutOfHearts
        )
        recordDailyPracticeProgress(xpAdded = xpEarned, exercisesAdded = 1)
        return state
    }

    /**
     * Explicitly processes a failed lesson or exercise attempt, deducting hearts and updating streak.
     */
    fun onLessonFailed(
        depleteAllHearts: Boolean = false,
        resetStreakOnLessonFailure: Boolean = false
    ): HeartsAndStreakUiState {
        val state = heartsAndStreakViewModel.onLessonFailed(
            depleteAllHearts = depleteAllHearts,
            resetStreakOnLessonFailure = resetStreakOnLessonFailure
        )
        _livesState.value = _livesState.value.copy(
            currentLives = state.hearts,
            isGameOver = state.isOutOfHearts,
            isLevelRestartedByZeroLives = state.isOutOfHearts
        )
        return state
    }

    fun retryCurrentExercise() {
        _evaluationResult.value = null
        lastProcessedResult = null
        dismissCelebrationEffect()
        val ex = _currentExercise.value
        if (ex != null) {
            val language = userProfile.value.selectedLanguage
            pronunciationViewModel.setTarget(
                targetPhrase = ex.targetPhrase,
                language = language,
                exercise = ex
            )
            nativeAudioComparisonManager.prepareForPhrase(
                targetPhrase = ex.targetPhrase,
                language = language,
                exerciseId = ex.id
            )
        } else {
            pronunciationViewModel.reset()
        }
        speakTargetPhrase(0.8f)
    }

    /**
     * Navigates to the dedicated Target Language Selection screen.
     */
    fun openLanguageSelectionScreen() {
        navigateTo(AppScreen.LANGUAGE_SELECTION)
    }

    /**
     * Speaks a sample greeting phrase for the given [language] so the user can preview its phonetics.
     */
    fun speakLanguageGreeting(language: Language) {
        ttsManager.speak(language.greeting, language, speedRate = 0.95f)
    }

    /**
     * Saves the user's selected target [language], optional [spokenLanguage], and [learningFocusGoal] in Room
     * and personalizes the lessons, exercises, and dialogues for that language pair.
     */
    fun selectLanguage(
        language: Language,
        learningFocusGoal: LearningFocusGoal = _selectedLearningFocus.value,
        navigateToHome: Boolean = true,
        spokenLanguage: SpokenLanguage = _selectedSpokenLanguage.value
    ) {
        val resolvedTarget = Language.resolveDistinctTargetLanguage(spokenLanguage, language)
        val savedEntity = languagePreferenceRepository.saveTargetLanguagePreferenceSync(
            language = resolvedTarget,
            learningFocusGoal = learningFocusGoal,
            spokenLanguage = spokenLanguage,
            hasCompletedOnboarding = true
        )
        viewModelScope.launch {
            languagePreferenceRepository.saveTargetLanguagePreference(
                language = resolvedTarget,
                learningFocusGoal = learningFocusGoal,
                spokenLanguage = spokenLanguage,
                hasCompletedOnboarding = true
            )
        }
        userPrefsRepo.setLanguagePair(
            spokenLanguage = spokenLanguage,
            targetLanguage = resolvedTarget,
            hasConfigured = true
        )
        _targetLanguagePreference.value = savedEntity
        _selectedSpokenLanguage.value = spokenLanguage
        _selectedTargetLanguage.value = resolvedTarget
        _selectedLearningFocus.value = learningFocusGoal
        _showLanguagePairOnboarding.value = false
        _personalizedModules.value = ExerciseRepository.getPersonalizedModulesForLanguage(resolvedTarget, learningFocusGoal)
        _personalizedModulesByLevel.value = ExerciseRepository.getPersonalizedModulesGroupedByLevel(resolvedTarget, learningFocusGoal)
        _personalizedDialogues.value = ExerciseRepository.getDialogueScenarios(resolvedTarget)

        _showLanguageDialog.value = false
        if (navigateToHome) {
            _currentScreen.value = AppScreen.PATH_LEARN
        }
        _activeModule.value = null
        _currentExercise.value = null
        _evaluationResult.value = null
        lastProcessedResult = null
        pronunciationViewModel.reset()
    }

    /**
     * Saves both the language the user speaks ([spokenLanguage]) and the language they want to learn
     * ([targetLanguage]) in Room, completing the language onboarding flow.
     */
    fun selectLanguagePair(
        spokenLanguage: SpokenLanguage,
        targetLanguage: Language,
        learningFocusGoal: LearningFocusGoal = _selectedLearningFocus.value,
        navigateToHome: Boolean = true
    ) {
        selectLanguage(
            language = targetLanguage,
            learningFocusGoal = learningFocusGoal,
            navigateToHome = navigateToHome,
            spokenLanguage = spokenLanguage
        )
    }

    /**
     * Updates the language the user speaks ("Idioma que hablas") in Room and automatically adjusts
     * the target language if both would be identical.
     */
    fun selectSpokenLanguage(
        spokenLanguage: SpokenLanguage,
        navigateToHome: Boolean = false
    ) {
        val distinctTarget = Language.resolveDistinctTargetLanguage(spokenLanguage, _selectedTargetLanguage.value)
        selectLanguage(
            language = distinctTarget,
            learningFocusGoal = _selectedLearningFocus.value,
            navigateToHome = navigateToHome,
            spokenLanguage = spokenLanguage
        )
    }

    /**
     * Opens the interactive dialog asking the user which language they speak and which language they want to learn.
     */
    fun openLanguagePairOnboarding() {
        _showLanguagePairOnboarding.value = true
    }

    /**
     * Dismisses the language pair onboarding dialog and marks the current pair as confirmed.
     */
    fun dismissLanguagePairOnboarding() {
        _showLanguagePairOnboarding.value = false
        selectLanguage(
            language = _selectedTargetLanguage.value,
            learningFocusGoal = _selectedLearningFocus.value,
            navigateToHome = false,
            spokenLanguage = _selectedSpokenLanguage.value
        )
    }

    /**
     * Updates the user's learning focus goal in Room and re-personalizes lessons for the active target language.
     */
    fun selectLearningFocus(learningFocusGoal: LearningFocusGoal) {
        selectLanguage(
            language = _selectedTargetLanguage.value,
            learningFocusGoal = learningFocusGoal,
            navigateToHome = false,
            spokenLanguage = _selectedSpokenLanguage.value
        )
    }

    fun setLanguageDialogVisible(visible: Boolean) {
        _showLanguageDialog.value = visible
    }

    fun setHeartsDialogVisible(visible: Boolean) {
        _showHeartsDialog.value = visible
    }

    fun refillHearts() {
        userPrefsRepo.refillHearts()
        heartsAndStreakViewModel.refillHearts()
        _livesState.value = _livesState.value.copy(
            currentLives = LivesState.MAX_LIVES,
            errorsInCurrentLevel = 0,
            isLevelRestartedByZeroLives = false,
            isGameOver = false
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
        heartsAndStreakViewModel.refillHearts()
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
            nativeAudioComparisonManager.prepareForPhrase(
                targetPhrase = firstTurn.expectedUserReply,
                language = scenario.language,
                exerciseId = "${scenario.id}_turn_0"
            )
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
        nativeAudioComparisonManager.onUserRecordingStarted()
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
        nativeAudioComparisonManager.onUserRecordingCompleted(result, spokenText)

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
            recordDailyPracticeProgress(xpAdded = 20, exercisesAdded = 1)
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
                nativeAudioComparisonManager.prepareForPhrase(
                    targetPhrase = nextTurn.expectedUserReply,
                    language = scenario.language,
                    exerciseId = "${scenario.id}_turn_$next"
                )
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
        nativeAudioComparisonManager.shutdown()
        ttsManager.shutdown()
        soundEffectsManager.shutdown()
    }
}
