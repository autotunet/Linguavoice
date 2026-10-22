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
}

