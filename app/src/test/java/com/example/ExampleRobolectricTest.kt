package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.audio.PronunciationFeedbackSoundType
import com.example.data.model.Language
import com.example.data.repository.ExerciseRepository
import com.example.ui.viewmodel.LanguageGameViewModel
import com.example.ui.viewmodel.LivesState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("LinguaVoice", appName)
    }

    @Test
    fun `lives manager deducts life on pronunciation error and restarts level at zero`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = LanguageGameViewModel(app)
        val module = ExerciseRepository.getModulesForLanguage(Language.ENGLISH).first()

        // Start lesson: index = 0, lives = 5
        viewModel.startLesson(module)
        assertEquals(0, viewModel.currentExerciseIndex.value)
        assertEquals(LivesState.MAX_LIVES, viewModel.livesState.value.currentLives)

        // Advance to exercise index 1 (second exercise)
        viewModel.onContinueNextExercise()
        assertEquals(1, viewModel.currentExerciseIndex.value)
        assertEquals(module.exercises[1].id, viewModel.currentExercise.value?.id)

        // Make 4 pronunciation errors: lives go 5 -> 4 -> 3 -> 2 -> 1, level does not restart yet
        for (expectedRemaining in 4 downTo 1) {
            val restarted = viewModel.loseLifeOnPronunciationError()
            assertFalse(restarted)
            assertEquals(expectedRemaining, viewModel.livesState.value.currentLives)
            assertEquals(1, viewModel.currentExerciseIndex.value)
        }

        // 5th pronunciation error brings lives to 0 -> restarts level back to exercise index 0
        val restartedOnZero = viewModel.loseLifeOnPronunciationError()
        assertTrue(restartedOnZero)
        assertEquals(0, viewModel.currentExerciseIndex.value)
        assertEquals(module.exercises.first().id, viewModel.currentExercise.value?.id)
        assertTrue(viewModel.livesState.value.isLevelRestartedByZeroLives)
        assertEquals(1, viewModel.livesState.value.levelRestartCount)
        assertEquals(LivesState.MAX_LIVES, viewModel.livesState.value.currentLives)
        assertEquals(0, viewModel.levelProgressState.value.completionPercentage)
    }

    @Test
    fun `level progress state tracks current level completion percentage and resets on level restart`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = LanguageGameViewModel(app)
        val module = ExerciseRepository.getModulesForLanguage(Language.ENGLISH).first()
        val total = module.exercises.size

        // Start lesson: 0% completed
        viewModel.startLesson(module)
        assertEquals(0, viewModel.levelProgressState.value.completedExercises)
        assertEquals(total, viewModel.levelProgressState.value.totalExercises)
        assertEquals(0, viewModel.levelProgressState.value.completionPercentage)
        assertEquals(0, viewModel.pronunciationUiState.value.levelProgressPercentage)

        // Advance through exercises and verify percentage calculation
        viewModel.onContinueNextExercise()
        val expectedPctAfter1 = ((1f / total.toFloat()) * 100f).toInt()
        assertEquals(1, viewModel.levelProgressState.value.completedExercises)
        assertEquals(expectedPctAfter1, viewModel.levelProgressState.value.completionPercentage)

        // Complete all remaining exercises to reach 100%
        repeat(total - 1) {
            viewModel.onContinueNextExercise()
        }
        assertTrue(viewModel.isLessonCompleted.value)
        assertEquals(100, viewModel.levelProgressState.value.completionPercentage)
        assertEquals(1.0f, viewModel.levelProgressState.value.progressFraction, 0.001f)
    }

    @Test
    fun `audio feedback plays success chime on correct pronunciation and subtle error sound on mistakes`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = LanguageGameViewModel(app)
        val module = ExerciseRepository.getModulesForLanguage(Language.ENGLISH).first()

        viewModel.startLesson(module)
        val currentTargetPhrase = viewModel.currentExercise.value?.targetPhrase
        assertNotNull(currentTargetPhrase)

        // 1. Correct pronunciation triggers SUCCESS_CHIME and synthesizes non-empty chime PCM audio
        viewModel.onSpeechRecognized(currentTargetPhrase!!)
        val feedbackAfterSuccess = viewModel.audioFeedbackState.value
        assertEquals(PronunciationFeedbackSoundType.SUCCESS_CHIME, feedbackAfterSuccess.lastPlayedSound)
        assertEquals(1, feedbackAfterSuccess.successChimeCount)
        assertEquals(0, feedbackAfterSuccess.errorSoundCount)
        assertEquals(
            PronunciationFeedbackSoundType.SUCCESS_CHIME,
            viewModel.pronunciationUiState.value.lastFeedbackSound
        )
        assertTrue(viewModel.soundEffectsManager.lastGeneratedPcmSamples.isNotEmpty())
        assertTrue(viewModel.soundEffectsManager.lastGeneratedPcmSamples.any { it.toInt() != 0 })

        // 2. Pronunciation error triggers subtle ERROR_SOUND and synthesizes non-empty error PCM audio
        viewModel.simulatePronunciationError()
        val feedbackAfterError = viewModel.audioFeedbackState.value
        assertEquals(PronunciationFeedbackSoundType.ERROR_SOUND, feedbackAfterError.lastPlayedSound)
        assertEquals(1, feedbackAfterError.successChimeCount)
        assertEquals(1, feedbackAfterError.errorSoundCount)
        assertEquals(
            PronunciationFeedbackSoundType.ERROR_SOUND,
            viewModel.pronunciationUiState.value.lastFeedbackSound
        )
        assertTrue(viewModel.soundEffectsManager.lastGeneratedPcmSamples.isNotEmpty())
        assertTrue(viewModel.soundEffectsManager.lastGeneratedPcmSamples.any { it.toInt() != 0 })
    }

    @Test
    fun `celebration particle and lottie effect triggers on passed pronunciation and deactivates on error or next exercise`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = LanguageGameViewModel(app)
        val module = ExerciseRepository.getModulesForLanguage(Language.ENGLISH).first()

        viewModel.startLesson(module)
        assertFalse(viewModel.celebrationState.value.isActive)
        assertFalse(viewModel.pronunciationUiState.value.isCelebrationActive)

        val targetPhrase = viewModel.currentExercise.value?.targetPhrase
        assertNotNull(targetPhrase)

        // Pass the pronunciation exercise -> celebration activates
        viewModel.onSpeechRecognized(targetPhrase!!)
        assertTrue(viewModel.celebrationState.value.isActive)
        assertEquals(1, viewModel.celebrationState.value.triggerId)
        assertEquals(1, viewModel.celebrationState.value.totalCelebrationsTriggered)
        assertTrue(viewModel.pronunciationUiState.value.isCelebrationActive)
        assertEquals(1, viewModel.pronunciationUiState.value.celebrationCount)

        // Replay celebration -> increments triggerId while staying active
        viewModel.replayCelebrationEffect()
        assertTrue(viewModel.celebrationState.value.isActive)
        assertEquals(2, viewModel.celebrationState.value.triggerId)

        // Continue to next exercise -> celebration deactivates
        viewModel.onContinueNextExercise()
        assertFalse(viewModel.celebrationState.value.isActive)
        assertFalse(viewModel.pronunciationUiState.value.isCelebrationActive)

        // Pronunciation error -> celebration stays inactive
        viewModel.simulatePronunciationError()
        assertFalse(viewModel.celebrationState.value.isActive)
        assertFalse(viewModel.pronunciationUiState.value.isCelebrationActive)
    }

    @Test
    fun `admob manager configures all eight production ad unit ids in order`() {
        val expectedUnits = listOf(
            "ca-app-pub-1495262574338316/2803163788",
            "ca-app-pub-1495262574338316/2903409205",
            "ca-app-pub-1495262574338316/2452733430",
            "ca-app-pub-1495262574338316/8970040322",
            "ca-app-pub-1495262574338316/1182179137",
            "ca-app-pub-1495262574338316/4962596335",
            "ca-app-pub-1495262574338316/2714752655",
            "ca-app-pub-1495262574338316/3503687777"
        )
        assertEquals(expectedUnits, com.example.ads.AdMobManager.ALL_AD_UNIT_IDS)
        expectedUnits.forEachIndexed { index, expectedId ->
            assertEquals(expectedId, com.example.ads.AdMobManager.getAdUnitIdByNumber(index + 1))
        }
    }

    @Test
    fun `main screen groups available lessons by level and tracks user progress and streak counter`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = LanguageGameViewModel(app)

        val groupedEnglishLessons = ExerciseRepository.getModulesGroupedByLevel(Language.ENGLISH)
        assertTrue(groupedEnglishLessons.containsKey(1))
        assertTrue(groupedEnglishLessons.containsKey(2))
        assertTrue(groupedEnglishLessons.containsKey(3))
        assertTrue(groupedEnglishLessons.containsKey(4))
        groupedEnglishLessons.forEach { (level, lessons) ->
            assertTrue("Expected lessons for level $level", lessons.isNotEmpty())
            assertTrue(lessons.all { it.requiredLevel == level })
        }

        val profile = viewModel.userProfile.value
        val levelInfo = viewModel.userLevelInfo.value
        val streakInfo = viewModel.streakInfo.value

        assertTrue(levelInfo.level >= 1)
        assertTrue(profile.accuracyAverage in 0..100)
        assertTrue(streakInfo.currentStreak >= 0)

        val firstModule = groupedEnglishLessons[1]!!.first()
        viewModel.startLesson(firstModule)
        repeat(firstModule.exercises.size) {
            viewModel.onContinueNextExercise()
        }
        assertTrue(
            com.example.ui.components.isLessonModuleCompleted(
                firstModule,
                viewModel.userProfile.value.completedLessonIds
            )
        )
    }

    @Test
    fun `streak calendar builds monthly grid and daily goal triggers celebration animation when reached`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = LanguageGameViewModel(app)

        // 1. Verify monthly streak calendar grid is populated and 7-column aligned
        val streakInfo = viewModel.streakInfo.value
        assertTrue(streakInfo.currentMonthTitle.isNotBlank())
        assertTrue(streakInfo.calendarDays.isNotEmpty())
        assertEquals(0, streakInfo.calendarDays.size % 7)
        assertTrue(streakInfo.calendarDays.any { it.isToday })
        assertTrue(streakInfo.calendarDays.any { it.isCurrentMonth })

        // 2. Verify daily goal progress and automatic celebration trigger when goal is reached
        viewModel.setDailyXpGoal(100)
        viewModel.dismissDailyGoalCelebration()
        assertFalse(viewModel.dailyGoalState.value.isCelebrationVisible)
        assertEquals(100, viewModel.dailyGoalState.value.dailyXpTarget)

        // Record daily practice progress to reach the 100 EXP goal -> celebration animation activates
        viewModel.recordDailyPracticeProgress(xpAdded = 80, exercisesAdded = 2)
        val celebratedState = viewModel.dailyGoalState.value
        assertTrue(celebratedState.isDailyGoalReached)
        assertEquals(100, celebratedState.progressPercentage)
        assertTrue(celebratedState.isCelebrationVisible)
        assertTrue(celebratedState.celebrationTriggerCount >= 1)

        // Replay celebration increments trigger count
        val countBeforeReplay = celebratedState.celebrationTriggerCount
        viewModel.replayDailyGoalCelebration()
        assertEquals(countBeforeReplay + 1, viewModel.dailyGoalState.value.celebrationTriggerCount)
        assertTrue(viewModel.dailyGoalState.value.isCelebrationVisible)

        // Dismiss celebration hides the overlay while keeping the daily goal marked as reached
        viewModel.dismissDailyGoalCelebration()
        assertFalse(viewModel.dailyGoalState.value.isCelebrationVisible)
        assertTrue(viewModel.dailyGoalState.value.isDailyGoalReached)
    }

    @Test
    fun `visual pronunciation feedback classifies voice recording into correcta casi correcta and incorrecta`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = LanguageGameViewModel(app)
        val module = ExerciseRepository.getModulesForLanguage(Language.ENGLISH).first()

        viewModel.startLesson(module)
        val targetPhrase = viewModel.currentExercise.value?.targetPhrase
        assertNotNull(targetPhrase)

        // 1. Exact pronunciation -> CORRECT ("Correcta", >= 85%)
        viewModel.onSpeechRecognized(targetPhrase!!)
        val correctResult = viewModel.evaluationResult.value
        assertNotNull(correctResult)
        assertEquals(
            com.example.data.model.PronunciationFeedbackStatus.CORRECT,
            correctResult!!.feedbackStatus
        )
        assertEquals(
            com.example.data.model.PronunciationFeedbackStatus.CORRECT,
            viewModel.pronunciationUiState.value.feedbackStatus
        )
        assertTrue(correctResult.scorePercentage >= 85)
        assertEquals("¡Pronunciación Correcta!", correctResult.feedbackStatus.titleEs)
        assertTrue(correctResult.correctWordsCount > 0)

        // 2. Almost correct pronunciation -> ALMOST_CORRECT ("Casi correcta", 60%..84%)
        viewModel.simulateAlmostCorrectPronunciation()
        val almostResult = viewModel.evaluationResult.value
        assertNotNull(almostResult)
        assertEquals(
            com.example.data.model.PronunciationFeedbackStatus.ALMOST_CORRECT,
            almostResult!!.feedbackStatus
        )
        assertEquals(
            com.example.data.model.PronunciationFeedbackStatus.ALMOST_CORRECT,
            viewModel.pronunciationUiState.value.feedbackStatus
        )
        assertTrue(almostResult.scorePercentage in 60..84)
        assertEquals("¡Casi Correcta!", almostResult.feedbackStatus.titleEs)
        assertTrue(almostResult.almostCorrectWordsCount > 0)

        // 3. Incorrect pronunciation -> INCORRECT ("Incorrecta", < 60%)
        viewModel.simulatePronunciationError()
        val incorrectResult = viewModel.evaluationResult.value
        assertNotNull(incorrectResult)
        assertEquals(
            com.example.data.model.PronunciationFeedbackStatus.INCORRECT,
            incorrectResult!!.feedbackStatus
        )
        assertEquals(
            com.example.data.model.PronunciationFeedbackStatus.INCORRECT,
            viewModel.pronunciationUiState.value.feedbackStatus
        )
        assertTrue(incorrectResult.scorePercentage < 60)
        assertEquals("Pronunciación Incorrecta", incorrectResult.feedbackStatus.titleEs)
        assertTrue(incorrectResult.incorrectWordsCount > 0)
    }

    @Test
    fun `target language selection screen saves preference in Room and personalizes lessons`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val database = com.example.data.local.AppDatabase.getInstance(app)
        val viewModel = LanguageGameViewModel(app)

        // 1. Open Target Language Selection Screen
        viewModel.openLanguageSelectionScreen()
        assertEquals(
            com.example.ui.viewmodel.AppScreen.LANGUAGE_SELECTION,
            viewModel.currentScreen.value
        )

        // 2. Select French with Travel & Culture learning focus and confirm
        viewModel.selectLanguage(
            language = Language.FRENCH,
            learningFocusGoal = com.example.data.model.LearningFocusGoal.TRAVEL_AND_CULTURE,
            navigateToHome = true
        )

        // Verify navigation back to home and immediate state updates
        assertEquals(com.example.ui.viewmodel.AppScreen.PATH_LEARN, viewModel.currentScreen.value)
        assertEquals(Language.FRENCH, viewModel.selectedTargetLanguage.value)
        assertEquals(Language.FRENCH, viewModel.userProfile.value.selectedLanguage)
        assertEquals(
            com.example.data.model.LearningFocusGoal.TRAVEL_AND_CULTURE,
            viewModel.selectedLearningFocus.value
        )

        // 3. Verify preference is persisted in Room database
        val roomEntity = database.languagePreferenceDao().getTargetLanguagePreferenceSync()
        assertNotNull(roomEntity)
        assertEquals(Language.FRENCH.code, roomEntity!!.languageCode)
        assertEquals(Language.FRENCH.displayName, roomEntity.displayName)
        assertEquals(
            com.example.data.model.LearningFocusGoal.TRAVEL_AND_CULTURE.id,
            roomEntity.learningFocusId
        )

        // 4. Verify lessons, levels, and dialogues are personalized for French
        val frenchModules = viewModel.personalizedModules.value
        assertTrue(frenchModules.isNotEmpty())
        assertTrue(frenchModules.all { it.language == Language.FRENCH })
        assertTrue(frenchModules.all { it.exercises.isNotEmpty() })
        // With TRAVEL_AND_CULTURE focus, travel/restaurant modules are prioritized first
        assertTrue(frenchModules.first().iconName in listOf("restaurant", "flight"))

        val frenchDialogues = viewModel.personalizedDialogues.value
        assertTrue(frenchDialogues.isNotEmpty())
        assertTrue(frenchDialogues.all { it.language == Language.FRENCH })

        // 5. Instantiate a new ViewModel to verify Room preference survives restart
        val reloadedViewModel = LanguageGameViewModel(app)
        assertEquals(Language.FRENCH, reloadedViewModel.selectedTargetLanguage.value)
        assertEquals(Language.FRENCH, reloadedViewModel.userProfile.value.selectedLanguage)
        assertTrue(reloadedViewModel.personalizedModules.value.all { it.language == Language.FRENCH })

        // Reset back to English for test isolation
        reloadedViewModel.selectLanguage(
            language = Language.ENGLISH,
            learningFocusGoal = com.example.data.model.LearningFocusGoal.DAILY_CONVERSATION,
            navigateToHome = true
        )
        assertEquals(Language.ENGLISH, reloadedViewModel.selectedTargetLanguage.value)
    }

    @Test
    fun `native speaker audio file is generated played and compared with user voice waveform`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = LanguageGameViewModel(app)
        val module = ExerciseRepository.getModulesForLanguage(Language.ENGLISH).first()

        // 1. Start lesson -> prepares a real .wav native speaker audio file for the exercise phrase
        viewModel.startLesson(module)
        val initialComparisonState = viewModel.nativeAudioComparisonState.value
        assertEquals(module.exercises.first().targetPhrase, initialComparisonState.targetPhrase)
        assertTrue(initialComparisonState.nativeAudioFileName.endsWith(".wav"))
        assertTrue(initialComparisonState.nativeAudioFilePath.isNotBlank())
        val wavFile = java.io.File(initialComparisonState.nativeAudioFilePath)
        assertTrue("Expected generated native speaker .wav file to exist on disk", wavFile.exists())
        assertTrue("Expected .wav file to contain RIFF WAVE header + PCM samples", wavFile.length() > 44L)
        assertEquals(
            com.example.audio.NativeAudioComparisonManager.WAVEFORM_BAR_COUNT,
            initialComparisonState.nativeWaveformAmplitudes.size
        )
        assertFalse(initialComparisonState.hasListenedToNativeAudio)
        assertFalse(initialComparisonState.hasUserRecordedAudio)

        // 2. Play the native speaker audio file before recording user version
        viewModel.playNativeReferenceAudio(speedRate = 1.0f)
        val afterPlayState = viewModel.nativeAudioComparisonState.value
        assertTrue(afterPlayState.hasListenedToNativeAudio)
        assertEquals(1, afterPlayState.nativePlayCount)

        // 3. Record user's own version (start listening -> stream RMS samples -> complete recognition)
        viewModel.startListeningForExercise()
        viewModel.nativeAudioComparisonManager.onUserRmsSample(5.5f)
        viewModel.nativeAudioComparisonManager.onUserRmsSample(7.2f)
        viewModel.nativeAudioComparisonManager.onUserRmsSample(6.4f)
        viewModel.onSpeechRecognized(module.exercises.first().targetPhrase)

        val comparedState = viewModel.nativeAudioComparisonState.value
        assertTrue(comparedState.hasUserRecordedAudio)
        assertEquals(
            com.example.audio.NativeAudioComparisonManager.WAVEFORM_BAR_COUNT,
            comparedState.userWaveformAmplitudes.size
        )
        val analysis = comparedState.comparisonAnalysis
        assertNotNull(analysis)
        assertTrue(analysis!!.waveformSimilarityPercentage >= 75)
        assertTrue(analysis.rhythmMatchPercentage in 1..100)
        assertTrue(analysis.intonationMatchPercentage in 1..100)
        assertEquals(
            com.example.audio.NativeAudioComparisonManager.WAVEFORM_BAR_COUNT,
            analysis.pointComparisons.size
        )
        assertTrue(analysis.coachingSummaryEs.isNotBlank())
    }

    @Test
    fun `google admob sdk initializes and exposes sdk version and all eight ad units`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        com.example.ads.AdMobManager.initialize(context)
        val sdkState = com.example.ads.AdMobManager.sdkState.value
        assertTrue(sdkState.isSdkInitialized)
        assertTrue(sdkState.sdkVersion.isNotBlank())
        assertEquals("ca-app-pub-1495262574338316", sdkState.publisherId)
        assertEquals(8, sdkState.configuredAdUnits.size)
    }

    @Test
    fun `app asks for spoken language and target language and persists pair in Room`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val database = com.example.data.local.AppDatabase.getInstance(app)
        val viewModel = LanguageGameViewModel(app)

        // 1. Open the language pair onboarding prompt that asks what language the user speaks and wants to learn
        viewModel.openLanguagePairOnboarding()
        assertTrue(viewModel.showLanguagePairOnboarding.value)

        // 2. Select spoken language (Portuguese) and target language to learn (German)
        viewModel.selectLanguagePair(
            spokenLanguage = com.example.data.model.SpokenLanguage.PORTUGUESE,
            targetLanguage = Language.GERMAN,
            learningFocusGoal = com.example.data.model.LearningFocusGoal.DAILY_CONVERSATION,
            navigateToHome = true
        )

        assertFalse(viewModel.showLanguagePairOnboarding.value)
        assertEquals(com.example.data.model.SpokenLanguage.PORTUGUESE, viewModel.selectedSpokenLanguage.value)
        assertEquals(Language.GERMAN, viewModel.selectedTargetLanguage.value)
        assertEquals(com.example.data.model.SpokenLanguage.PORTUGUESE, viewModel.userProfile.value.spokenLanguage)
        assertEquals(Language.GERMAN, viewModel.userProfile.value.selectedLanguage)

        // 3. Verify both spokenLanguage and targetLanguage are persisted in Room
        val roomEntity = database.languagePreferenceDao().getTargetLanguagePreferenceSync()
        assertNotNull(roomEntity)
        assertEquals(com.example.data.model.SpokenLanguage.PORTUGUESE.code, roomEntity!!.spokenLanguageCode)
        assertEquals(Language.GERMAN.code, roomEntity.languageCode)
        assertTrue(roomEntity.hasCompletedLanguageOnboarding)

        // 4. Verify greeting translation adapts to the spoken language
        val germanGreetingInPt = Language.GERMAN.getGreetingTranslationFor(com.example.data.model.SpokenLanguage.PORTUGUESE)
        assertTrue(germanGreetingInPt.contains("Bom dia"))

        // 5. Verify selecting the same spoken language as current target automatically resolves to a distinct target
        viewModel.selectSpokenLanguage(com.example.data.model.SpokenLanguage.GERMAN)
        assertEquals(com.example.data.model.SpokenLanguage.GERMAN, viewModel.selectedSpokenLanguage.value)
        assertEquals(Language.ENGLISH, viewModel.selectedTargetLanguage.value)

        // Reset back to Spanish -> English for test isolation
        viewModel.selectLanguagePair(
            spokenLanguage = com.example.data.model.SpokenLanguage.SPANISH,
            targetLanguage = Language.ENGLISH
        )
    }

    @Test
    fun `spanish target language curriculum and full app spoken language localization adapt completely`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = LanguageGameViewModel(app)

        // 1. Verify Spanish (Language.SPANISH) is available to learn with full modules, exercises, and dialogues
        val spanishModules = ExerciseRepository.getModulesForLanguage(Language.SPANISH)
        assertTrue(spanishModules.size >= 5)
        val spanishExercises = ExerciseRepository.getExercisesForLanguage(Language.SPANISH)
        assertTrue(spanishExercises.size >= 12)
        val spanishDialogues = ExerciseRepository.getDialogueScenarios(Language.SPANISH)
        assertTrue(spanishDialogues.size >= 2)

        // 2. Select English as spoken language and Spanish as target language to learn
        viewModel.selectLanguagePair(
            spokenLanguage = com.example.data.model.SpokenLanguage.ENGLISH,
            targetLanguage = Language.SPANISH
        )
        assertEquals(com.example.data.model.SpokenLanguage.ENGLISH, viewModel.selectedSpokenLanguage.value)
        assertEquals(Language.SPANISH, viewModel.selectedTargetLanguage.value)
        assertTrue(viewModel.personalizedModules.value.all { it.language == Language.SPANISH })

        // 3. Verify AppStrings changes across all 6 spoken languages
        com.example.data.model.SpokenLanguage.entries.forEach { spoken ->
            val strings = com.example.ui.theme.AppStrings.forSpokenLanguage(spoken)
            assertTrue(strings.navPath.isNotBlank())
            assertTrue(strings.whatLanguageDoYouSpeak.isNotBlank())
            assertTrue(strings.appAskedOrSaidLabel.isNotBlank())
            assertTrue(strings.playMyRecordingBtn.isNotBlank())
        }
        val enStrings = com.example.ui.theme.AppStrings.forSpokenLanguage(com.example.data.model.SpokenLanguage.ENGLISH)
        val esStrings = com.example.ui.theme.AppStrings.forSpokenLanguage(com.example.data.model.SpokenLanguage.SPANISH)
        assertEquals("Path", enStrings.navPath)
        assertEquals("Ruta", esStrings.navPath)

        // Reset back to Spanish -> English
        viewModel.selectLanguagePair(
            spokenLanguage = com.example.data.model.SpokenLanguage.SPANISH,
            targetLanguage = Language.ENGLISH
        )
    }

    @Test
    fun `microphone records user voice wav file plays it back and verifies spoken answer against prompt question`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = LanguageGameViewModel(app)
        val spanishModule = ExerciseRepository.getModulesForLanguage(Language.SPANISH).first()

        // 1. Start a Spanish lesson and begin microphone listening
        viewModel.startLesson(spanishModule)
        val activeExercise = viewModel.currentExercise.value
        assertNotNull(activeExercise)
        val targetWords = activeExercise!!.targetPhrase

        viewModel.startListeningForExercise()
        viewModel.nativeAudioComparisonManager.onUserRmsSample(6.5f)
        viewModel.nativeAudioComparisonManager.onUserRmsSample(8.1f)
        // Simulate raw PCM buffer from microphone
        val samplePcmBytes = ByteArray(640) { index -> ((index * 17) % 120).toByte() }
        viewModel.nativeAudioComparisonManager.onUserPcmBufferReceived(samplePcmBytes)

        // 2. Stop microphone listening -> automatically recognizes speech, saves user .wav recording, and evaluates
        viewModel.stopListening()
        val evalResult = viewModel.evaluationResult.value
        assertNotNull("Expected speech recognition evaluation after microphone stop", evalResult)
        assertEquals(targetWords, evalResult!!.targetText)
        assertTrue(evalResult.recognizedText.isNotBlank())
        assertEquals(
            com.example.data.model.PronunciationFeedbackStatus.CORRECT,
            evalResult.feedbackStatus
        )

        // 3. Verify user microphone recording .wav file was written and can be played back
        val comparisonState = viewModel.nativeAudioComparisonState.value
        assertTrue(comparisonState.hasUserRecordedAudio)
        assertTrue(comparisonState.userAudioFileName.endsWith(".wav"))
        assertTrue(comparisonState.userAudioFilePath.isNotBlank())
        val userWavFile = java.io.File(comparisonState.userAudioFilePath)
        assertTrue("Expected user recorded .wav file to exist on disk", userWavFile.exists())
        assertTrue("Expected user recorded .wav file to have RIFF header and PCM audio", userWavFile.length() > 44L)

        // 4. Play back the user's recorded voice
        viewModel.playUserRecordedAudio()
        val afterUserPlayback = viewModel.nativeAudioComparisonState.value
        assertTrue(afterUserPlayback.userPlayCount >= 1)
    }

    @Test
    fun `score and accuracy feedback compares spoken input against target phrase with word level percentages`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = LanguageGameViewModel(app)
        val module = ExerciseRepository.getModulesForLanguage(Language.ENGLISH).first()

        viewModel.startLesson(module)
        val targetPhrase = viewModel.currentExercise.value!!.targetPhrase

        // 1. User speaks the exact target phrase -> 100% Score / Accuracy
        viewModel.onSpeechRecognized(targetPhrase)
        val exactResult = viewModel.evaluationResult.value
        assertNotNull(exactResult)
        assertEquals(100, exactResult!!.scorePercentage)
        assertEquals(targetPhrase, exactResult.targetText)
        assertEquals(targetPhrase, exactResult.recognizedText)
        assertEquals(100, exactResult.completenessScore)
        assertTrue(exactResult.fluencyScore in 90..100)
        assertTrue(exactResult.wordAccuracies.isNotEmpty())
        assertTrue(exactResult.wordAccuracies.all { it.similarityPercentage >= 85 && it.isCorrect })

        // 2. User speaks an almost-correct variation -> intermediate Score / Accuracy (60%..84%)
        viewModel.simulateAlmostCorrectPronunciation()
        val almostResult = viewModel.evaluationResult.value
        assertNotNull(almostResult)
        assertTrue(almostResult!!.scorePercentage in 60..84)
        assertEquals(targetPhrase, almostResult.targetText)
        assertTrue(almostResult.recognizedText.isNotBlank())
        assertTrue(almostResult.wordAccuracies.any { it.similarityPercentage in 60..84 })

        // 3. User speaks an unrelated phrase -> low Score / Accuracy (< 60%)
        viewModel.simulatePronunciationError()
        val lowResult = viewModel.evaluationResult.value
        assertNotNull(lowResult)
        assertTrue(lowResult!!.scorePercentage < 60)
        assertEquals(targetPhrase, lowResult.targetText)
        assertEquals("pronunciacion incorrecta error de prueba", lowResult.recognizedText)
    }

    @Test
    fun `lives counter decreases when pronunciation accuracy falls below threshold and triggers game over at zero`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = LanguageGameViewModel(app)
        val module = ExerciseRepository.getModulesForLanguage(Language.ENGLISH).first()

        // 1. Start lesson with 5 full lives
        viewModel.startLesson(module)
        assertEquals(5, viewModel.livesState.value.currentLives)
        assertEquals(LivesState.MAX_LIVES, viewModel.livesState.value.maxLives)
        assertEquals(60, viewModel.livesState.value.accuracyThreshold)
        assertFalse(viewModel.livesState.value.isGameOver)

        // 2. Pronunciation with accuracy below threshold (e.g., 35% < 60%) deducts a life
        val failedResult1 = com.example.data.model.PronunciationResult(
            scorePercentage = 35,
            recognizedText = "bad audio",
            targetText = viewModel.currentExercise.value!!.targetPhrase,
            wordAccuracies = emptyList(),
            feedbackMessage = "Incorrecto",
            isPassed = false,
            xpEarned = 0,
            coachTip = "Habla más despacio"
        )
        val passed1 = viewModel.checkAccuracyAndDeductLifeIfBelowThreshold(failedResult1)
        assertFalse(passed1)
        assertEquals(4, viewModel.livesState.value.currentLives)
        assertFalse(viewModel.livesState.value.isGameOver)

        // 3. Three more failures below threshold reduce lives 4 -> 3 -> 2 -> 1
        for (expectedLives in 3 downTo 1) {
            val passed = viewModel.checkAccuracyAndDeductLifeIfBelowThreshold(failedResult1)
            assertFalse(passed)
            assertEquals(expectedLives, viewModel.livesState.value.currentLives)
            assertFalse(viewModel.livesState.value.isGameOver)
        }

        // 4. Passing pronunciation (accuracy >= 60%) does NOT decrease lives
        val passedResult = com.example.data.model.PronunciationResult(
            scorePercentage = 75,
            recognizedText = viewModel.currentExercise.value!!.targetPhrase,
            targetText = viewModel.currentExercise.value!!.targetPhrase,
            wordAccuracies = emptyList(),
            feedbackMessage = "¡Casi perfecta!",
            isPassed = true,
            xpEarned = 15,
            coachTip = "Bien hecho"
        )
        val passedCheck = viewModel.checkAccuracyAndDeductLifeIfBelowThreshold(passedResult)
        assertTrue(passedCheck)
        assertEquals(1, viewModel.livesState.value.currentLives) // Lives remain at 1
        assertFalse(viewModel.livesState.value.isGameOver)

        // 5. 5th error below threshold drops lives to 0 and explicitly triggers GAME OVER
        val gameOverTriggered = viewModel.loseLifeOnPronunciationError(failedResult1)
        assertTrue(gameOverTriggered)
        assertTrue(viewModel.livesState.value.isGameOver)
        assertTrue(viewModel.livesState.value.isLevelRestartedByZeroLives)
        assertEquals(0, viewModel.currentExerciseIndex.value)

        // 6. Confirming / restarting from Game Over clears game over and restores 5 lives
        viewModel.restartLevelFromGameOver()
        assertFalse(viewModel.livesState.value.isGameOver)
        assertFalse(viewModel.livesState.value.isLevelRestartedByZeroLives)
        assertEquals(LivesState.MAX_LIVES, viewModel.livesState.value.currentLives)
    }

    @Test
    fun `HeartsAndStreakViewModel updates hearts and streak when completing or failing lessons`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = com.example.ui.viewmodel.HeartsAndStreakViewModel(app)
        vm.refillHearts()
        val initialStreak = vm.currentStreak.value
        val initialCompletedCount = vm.uiState.value.completedLessonsCount

        // 1. Verify initial state has max 5 hearts
        assertEquals(5, vm.hearts.value)
        assertFalse(vm.isOutOfHearts.value)
        assertEquals(5, vm.uiState.value.maxHearts)

        // 2. Failing exercises within a lesson deducts hearts
        val outOfHeartsAfterFirstFail = vm.onExerciseFailed()
        assertFalse(outOfHeartsAfterFirstFail)
        assertEquals(4, vm.hearts.value)
        assertFalse(vm.isOutOfHearts.value)
        assertEquals(
            com.example.ui.viewmodel.LessonOutcome.EXERCISE_FAILED,
            vm.uiState.value.lastOutcome
        )

        // 3. Completing a lesson with high accuracy (>= 85%) increments streak and awards +1 bonus heart
        val completedState = vm.onLessonCompleted(accuracyScore = 92, xpEarned = 25)
        assertEquals(initialStreak + 1, completedState.currentStreak)
        assertEquals(initialStreak + 1, vm.currentStreak.value)
        assertTrue(completedState.streakIncrementedOnLastCompletion)
        assertTrue(completedState.practicedToday)
        assertEquals(5, completedState.hearts) // 4 + 1 bonus heart restored
        assertTrue(completedState.bonusHeartAwardedOnLastCompletion)
        assertEquals(initialCompletedCount + 1, completedState.completedLessonsCount)
        assertEquals(
            com.example.ui.viewmodel.LessonOutcome.LESSON_COMPLETED,
            completedState.lastOutcome
        )

        // 4. Failing a lesson depletes hearts and increments failedLessonsCount
        val failedLessonState = vm.onLessonFailed(depleteAllHearts = true, resetStreakOnLessonFailure = false)
        assertEquals(0, failedLessonState.hearts)
        assertTrue(failedLessonState.isOutOfHearts)
        assertTrue(vm.isOutOfHearts.value)
        assertEquals(1, failedLessonState.failedLessonsCount)
        assertEquals(initialStreak + 1, failedLessonState.currentStreak) // streak preserved when resetStreakOnLessonFailure = false
        assertEquals(
            com.example.ui.viewmodel.LessonOutcome.LESSON_FAILED_OUT_OF_HEARTS,
            failedLessonState.lastOutcome
        )

        // 5. Refilling hearts restores all 5 hearts and clears isOutOfHearts
        val refilledState = vm.refillHearts()
        assertEquals(5, refilledState.hearts)
        assertFalse(refilledState.isOutOfHearts)
        assertEquals(
            com.example.ui.viewmodel.LessonOutcome.HEARTS_REFILLED,
            refilledState.lastOutcome
        )

        // 6. Failing a lesson with resetStreakOnLessonFailure = true resets currentStreak to 0
        val resetStreakFailureState = vm.onLessonFailed(depleteAllHearts = false, resetStreakOnLessonFailure = true)
        assertEquals(4, resetStreakFailureState.hearts)
        assertEquals(0, resetStreakFailureState.currentStreak)
        assertEquals(0, vm.currentStreak.value)
    }

    @Test
    fun `LanguageGameViewModel synchronizes with HeartsAndStreakViewModel on lesson completion and failure`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val gameViewModel = LanguageGameViewModel(app)
        gameViewModel.refillHearts()

        val initialStreak = gameViewModel.heartsAndStreakState.value.currentStreak
        val initialCompletedCount = gameViewModel.heartsAndStreakState.value.completedLessonsCount
        assertEquals(5, gameViewModel.heartsAndStreakState.value.hearts)

        // 1. Completing a lesson via LanguageGameViewModel increments streak
        gameViewModel.onLessonCompleted(accuracyScore = 90, xpEarned = 30)
        assertEquals(initialStreak + 1, gameViewModel.heartsAndStreakState.value.currentStreak)
        assertEquals(initialCompletedCount + 1, gameViewModel.heartsAndStreakState.value.completedLessonsCount)

        // 2. Failing a lesson via LanguageGameViewModel updates hearts and failed count
        gameViewModel.onLessonFailed(depleteAllHearts = true, resetStreakOnLessonFailure = false)
        assertEquals(0, gameViewModel.heartsAndStreakState.value.hearts)
        assertTrue(gameViewModel.heartsAndStreakState.value.isOutOfHearts)
        assertEquals(1, gameViewModel.heartsAndStreakState.value.failedLessonsCount)
    }

    @Test
    fun `pronunciation accuracy motivational feedback displays correct and incorrect visual indicators and motivational copy`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = LanguageGameViewModel(app)
        val module = ExerciseRepository.getModulesForLanguage(Language.ENGLISH).first()
        viewModel.startLesson(module)
        val targetPhrase = viewModel.currentExercise.value!!.targetPhrase

        // 1. Evaluate a high-accuracy (CORRECT) attempt
        viewModel.onSpeechRecognized(targetPhrase)
        val correctResult = viewModel.evaluationResult.value!!
        assertTrue(correctResult.isPassed)
        assertTrue(correctResult.scorePercentage >= 85)

        val correctIndicator = com.example.ui.components.PronunciationVisualIndicator.fromResult(correctResult)
        assertEquals(com.example.ui.components.PronunciationVisualIndicator.CORRECT, correctIndicator)
        assertTrue(correctIndicator.isCorrect)

        val correctCopyEs = com.example.ui.components.buildMotivationalFeedbackCopy(
            result = correctResult,
            spokenLanguage = com.example.data.model.SpokenLanguage.SPANISH
        )
        assertEquals("✓ CORRECTO", correctCopyEs.statusBadgeText)
        assertTrue(correctCopyEs.motivationalEncouragement.contains("${correctResult.scorePercentage}%"))

        val correctCopyEn = com.example.ui.components.buildMotivationalFeedbackCopy(
            result = correctResult,
            spokenLanguage = com.example.data.model.SpokenLanguage.ENGLISH
        )
        assertEquals("✓ CORRECT", correctCopyEn.statusBadgeText)
        assertTrue(correctCopyEn.motivationalEncouragement.contains("${correctResult.scorePercentage}%"))

        // 2. Evaluate a low-accuracy (INCORRECT) attempt
        viewModel.simulatePronunciationError()
        val incorrectResult = viewModel.evaluationResult.value!!
        assertFalse(incorrectResult.isPassed)
        assertTrue(incorrectResult.scorePercentage < 60)

        val incorrectIndicator = com.example.ui.components.PronunciationVisualIndicator.fromResult(incorrectResult)
        assertEquals(com.example.ui.components.PronunciationVisualIndicator.INCORRECT, incorrectIndicator)
        assertFalse(incorrectIndicator.isCorrect)

        val incorrectCopyEs = com.example.ui.components.buildMotivationalFeedbackCopy(
            result = incorrectResult,
            spokenLanguage = com.example.data.model.SpokenLanguage.SPANISH
        )
        assertEquals("✕ INCORRECTO", incorrectCopyEs.statusBadgeText)
        assertTrue(incorrectCopyEs.motivationalEncouragement.contains("${incorrectResult.scorePercentage}%"))
    }

    @Test
    fun `top linear progress bar fills incrementally as user successfully completes pronunciation tasks within a lesson`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = LanguageGameViewModel(app)
        val module = ExerciseRepository.getModulesForLanguage(Language.ENGLISH).first()
        val totalTasks = module.exercises.size
        assertTrue(totalTasks >= 2)

        // 1. Start lesson: progress bar starts at 0% (0/totalTasks)
        viewModel.startLesson(module)
        assertEquals(0, viewModel.levelProgressState.value.completedExercises)
        assertEquals(totalTasks, viewModel.levelProgressState.value.totalExercises)
        assertEquals(0f, viewModel.levelProgressState.value.progressFraction, 0.001f)
        assertEquals(0, viewModel.levelProgressState.value.completionPercentage)
        assertTrue(viewModel.levelProgressState.value.completedTaskIndices.isEmpty())
        assertEquals(0, viewModel.levelProgressState.value.lastIncrementDeltaPercentage)

        // 2. Failed pronunciation attempt does NOT fill the linear progress bar
        viewModel.simulatePronunciationError()
        assertEquals(0, viewModel.levelProgressState.value.completedExercises)
        assertEquals(0f, viewModel.levelProgressState.value.progressFraction, 0.001f)
        assertEquals(0, viewModel.levelProgressState.value.completionPercentage)

        // 3. Successfully complete each pronunciation task one by one and verify incremental filling
        var previousFraction = 0f
        for (taskIndex in 0 until totalTasks) {
            val currentPhrase = viewModel.currentExercise.value!!.targetPhrase
            viewModel.onSpeechRecognized(currentPhrase)

            val progressState = viewModel.levelProgressState.value
            val expectedCompleted = taskIndex + 1
            val expectedFraction = expectedCompleted.toFloat() / totalTasks.toFloat()
            val expectedPct = kotlin.math.round(expectedFraction * 100f).toInt()

            assertEquals(expectedCompleted, progressState.completedExercises)
            assertEquals(expectedFraction, progressState.progressFraction, 0.001f)
            assertEquals(expectedPct, progressState.completionPercentage)
            assertTrue(progressState.progressFraction > previousFraction)
            assertTrue(progressState.completedTaskIndices.contains(taskIndex))
            assertTrue(progressState.lastTaskPassed)

            previousFraction = progressState.progressFraction

            if (taskIndex < totalTasks - 1) {
                viewModel.onContinueNextExercise()
            }
        }

        // 4. After all pronunciation tasks are completed, progress bar is at 100% (1.0f)
        assertEquals(1.0f, viewModel.levelProgressState.value.progressFraction, 0.001f)
        assertEquals(100, viewModel.levelProgressState.value.completionPercentage)
        assertEquals(totalTasks, viewModel.levelProgressState.value.completedTaskIndices.size)
    }
}

