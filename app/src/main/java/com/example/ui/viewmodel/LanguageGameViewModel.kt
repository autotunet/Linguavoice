package com.example.ui.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.PronunciationEvaluator
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
    PATH_LEARN,
    EXERCISE_SESSION,
    FREE_PRACTICE,
    DIALOGUE,
    PROFILE
}

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

    // Real-time SpeechRecognizer ViewModel managing voice-to-text conversion
    val pronunciationViewModel = PronunciationViewModel(application)
    val pronunciationUiState: StateFlow<PronunciationUiState> = pronunciationViewModel.uiState
    val speechState: StateFlow<SpeechState> = pronunciationViewModel.speechState

    val ttsManager = TtsManager(application)
    val isTtsSpeaking: StateFlow<Boolean> = ttsManager.isSpeaking

    private val _currentScreen = MutableStateFlow(AppScreen.PATH_LEARN)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

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

    // Dialogue State
    private val _activeDialogue = MutableStateFlow<DialogueScenario?>(null)
    val activeDialogue: StateFlow<DialogueScenario?> = _activeDialogue.asStateFlow()

    private val _dialogueTurnIndex = MutableStateFlow(0)
    val dialogueTurnIndex: StateFlow<Int> = _dialogueTurnIndex.asStateFlow()

    private val _showLanguageDialog = MutableStateFlow(false)
    val showLanguageDialog: StateFlow<Boolean> = _showLanguageDialog.asStateFlow()

    private val _showHeartsDialog = MutableStateFlow(false)
    val showHeartsDialog: StateFlow<Boolean> = _showHeartsDialog.asStateFlow()

    private var lastProcessedResult: PronunciationResult? = null

    init {
        // Seed initial streak baseline if needed
        viewModelScope.launch {
            streakRepository.seedInitialStreakIfEmpty()
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

    fun navigateTo(screen: AppScreen) {
        pronunciationViewModel.cancelListening()
        ttsManager.stop()
        _currentScreen.value = screen
    }

    fun startLesson(module: LessonModule) {
        _activeModule.value = module
        _currentExerciseIndex.value = 0
        _isLessonCompleted.value = false
        _evaluationResult.value = null
        lastProcessedResult = null
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
            _evaluationResult.value = result

            if (result.isPassed) {
                userPrefsRepo.addXp(result.xpEarned)
                userPrefsRepo.updateAccuracy(result.scorePercentage)
                viewModelScope.launch {
                    streakRepository.recordPracticeSession(result.xpEarned, language.code, result.scorePercentage)
                }
            } else {
                userPrefsRepo.deductHeart()
            }
        } else if (_currentScreen.value == AppScreen.DIALOGUE) {
            processDialogueSpokenText(spokenText)
        }
    }

    fun onContinueNextExercise() {
        _evaluationResult.value = null
        lastProcessedResult = null
        pronunciationViewModel.reset()

        val module = _activeModule.value
        if (module == null) {
            // Standalone exercise finished
            _currentScreen.value = AppScreen.PATH_LEARN
            return
        }

        val nextIndex = _currentExerciseIndex.value + 1
        if (nextIndex < module.exercises.size) {
            val nextEx = module.exercises[nextIndex]
            _currentExerciseIndex.value = nextIndex
            _currentExercise.value = nextEx
            pronunciationViewModel.setTarget(
                targetPhrase = nextEx.targetPhrase,
                language = userProfile.value.selectedLanguage,
                exercise = nextEx
            )
            speakTargetPhrase()
        } else {
            // Module completed!
            _isLessonCompleted.value = true
            userPrefsRepo.markLessonCompleted(module.id)
            userPrefsRepo.addXp(25) // Completion bonus
        }
    }

    fun retryCurrentExercise() {
        _evaluationResult.value = null
        lastProcessedResult = null
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
        _showHeartsDialog.value = false
    }

    // Dialogue Scenarios
    fun startDialogueScenario(scenario: DialogueScenario) {
        _activeDialogue.value = scenario
        _dialogueTurnIndex.value = 0
        _evaluationResult.value = null
        lastProcessedResult = null
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
        _evaluationResult.value = result

        if (result.isPassed) {
            userPrefsRepo.addXp(20)
            userPrefsRepo.updateAccuracy(result.scorePercentage)
            viewModelScope.launch {
                streakRepository.recordPracticeSession(20, scenario.language.code, result.scorePercentage)
            }
        }
    }

    fun advanceDialogueTurn() {
        _evaluationResult.value = null
        lastProcessedResult = null
        val scenario = _activeDialogue.value ?: return
        val next = _dialogueTurnIndex.value + 1
        if (next < scenario.turns.size) {
            _dialogueTurnIndex.value = next
            val nextTurn = scenario.turns.getOrNull(next)
            if (nextTurn?.expectedUserReply != null) {
                pronunciationViewModel.setTarget(nextTurn.expectedUserReply, scenario.language, null)
            } else {
                pronunciationViewModel.reset()
            }
            playCurrentDialogueTurnAiVoice()
        } else {
            // Dialogue completed!
            pronunciationViewModel.reset()
            userPrefsRepo.addXp(30)
            _currentScreen.value = AppScreen.PATH_LEARN
        }
    }

    override fun onCleared() {
        super.onCleared()
        pronunciationViewModel.reset()
        ttsManager.shutdown()
    }
}
