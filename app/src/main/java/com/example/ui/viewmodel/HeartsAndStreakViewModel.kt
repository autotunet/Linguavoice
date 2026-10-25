package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.repository.StreakRepository
import com.example.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Represents the latest lesson or exercise event processed by [HeartsAndStreakViewModel].
 */
enum class LessonOutcome {
    NONE,
    LESSON_COMPLETED,
    EXERCISE_FAILED,
    LESSON_FAILED_OUT_OF_HEARTS,
    HEARTS_REFILLED,
    STREAK_RESET
}

/**
 * Immutable UI state holding the user's current 'hearts' (lives) and 'streak' count.
 */
data class HeartsAndStreakUiState(
    val hearts: Int = MAX_HEARTS,
    val maxHearts: Int = MAX_HEARTS,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val practicedToday: Boolean = false,
    val completedLessonsCount: Int = 0,
    val failedLessonsCount: Int = 0,
    val failedExercisesInCurrentLesson: Int = 0,
    val isOutOfHearts: Boolean = false,
    val lastOutcome: LessonOutcome = LessonOutcome.NONE,
    val streakIncrementedOnLastCompletion: Boolean = false,
    val bonusHeartAwardedOnLastCompletion: Boolean = false,
    val lastStatusMessage: String = ""
) {
    val heartsFraction: Float
        get() = if (maxHearts > 0) (hearts.toFloat() / maxHearts.toFloat()).coerceIn(0f, 1f) else 0f

    val isLowOnHearts: Boolean
        get() = hearts in 1..2

    companion object {
        const val MAX_HEARTS = 5
        const val PASSING_ACCURACY_THRESHOLD = 60
        const val BONUS_HEART_ACCURACY_THRESHOLD = 85
    }
}

/**
 * ViewModel dedicated to managing the user's 'hearts' (lives) and current 'streak' count,
 * updating them reactively as the user completes or fails lessons and exercises.
 */
class HeartsAndStreakViewModel(application: Application) : AndroidViewModel(application) {

    private val userPrefsRepo = UserPreferencesRepository(application)
    private val streakRepository = StreakRepository(AppDatabase.getInstance(application).practiceDayDao())

    private val initialProfile = userPrefsRepo.userProfile.value
    private val initialHearts = initialProfile.hearts.coerceIn(0, HeartsAndStreakUiState.MAX_HEARTS)
    private val initialStreak = initialProfile.streakDays.coerceAtLeast(0)
    private val initialBestStreak = maxOf(userPrefsRepo.getBestStreakDays(), initialStreak)

    private val _uiState = MutableStateFlow(
        HeartsAndStreakUiState(
            hearts = initialHearts,
            maxHearts = HeartsAndStreakUiState.MAX_HEARTS,
            currentStreak = initialStreak,
            bestStreak = initialBestStreak,
            practicedToday = false,
            completedLessonsCount = initialProfile.completedLessonIds.size,
            isOutOfHearts = initialHearts == 0
        )
    )
    val uiState: StateFlow<HeartsAndStreakUiState> = _uiState.asStateFlow()

    val hearts: StateFlow<Int> = _uiState
        .map { it.hearts }
        .stateIn(viewModelScope, SharingStarted.Eagerly, initialHearts)

    val currentStreak: StateFlow<Int> = _uiState
        .map { it.currentStreak }
        .stateIn(viewModelScope, SharingStarted.Eagerly, initialStreak)

    val isOutOfHearts: StateFlow<Boolean> = _uiState
        .map { it.isOutOfHearts }
        .stateIn(viewModelScope, SharingStarted.Eagerly, initialHearts == 0)

    /**
     * Called when the user successfully completes a lesson.
     * - Increments the current streak count and updates best streak.
     * - Marks `practicedToday = true`.
     * - Optionally awards +1 bonus heart (up to [HeartsAndStreakUiState.MAX_HEARTS]) when completed with high accuracy.
     * - Persists changes to [UserPreferencesRepository] and [StreakRepository].
     */
    fun onLessonCompleted(
        lessonId: String? = null,
        xpEarned: Int = 25,
        accuracyScore: Int = 95,
        languageCode: String = userPrefsRepo.userProfile.value.selectedLanguage.code,
        awardBonusHeartOnHighAccuracy: Boolean = true
    ): HeartsAndStreakUiState {
        val current = _uiState.value

        if (!lessonId.isNullOrBlank()) {
            userPrefsRepo.markLessonCompleted(lessonId)
        }
        if (xpEarned > 0) {
            userPrefsRepo.addXp(xpEarned)
        }
        userPrefsRepo.updateAccuracy(accuracyScore)

        // Update streak count
        val updatedStreak = current.currentStreak + 1
        val updatedBestStreak = maxOf(current.bestStreak, updatedStreak)
        userPrefsRepo.setStreakDays(updatedStreak)
        userPrefsRepo.setBestStreakDays(updatedBestStreak)

        // Award a bonus heart if the user completed the lesson with high accuracy and had lost hearts
        val canAwardHeart = awardBonusHeartOnHighAccuracy &&
            accuracyScore >= HeartsAndStreakUiState.BONUS_HEART_ACCURACY_THRESHOLD &&
            current.hearts < current.maxHearts
        val updatedHearts = if (canAwardHeart) {
            userPrefsRepo.addHeart(current.maxHearts)
        } else {
            current.hearts.coerceAtLeast(1)
        }
        userPrefsRepo.setHearts(updatedHearts)

        val updatedCompletedCount = if (!lessonId.isNullOrBlank()) {
            maxOf(
                userPrefsRepo.userProfile.value.completedLessonIds.size,
                current.completedLessonsCount + 1
            )
        } else {
            current.completedLessonsCount + 1
        }

        val statusMsg = buildString {
            append("¡Lección completada! Racha aumentada a $updatedStreak días 🔥")
            if (canAwardHeart) {
                append(" • +1 vida recuperada ❤️ ($updatedHearts/${current.maxHearts})")
            }
        }

        val nextState = current.copy(
            hearts = updatedHearts,
            currentStreak = updatedStreak,
            bestStreak = updatedBestStreak,
            practicedToday = true,
            completedLessonsCount = updatedCompletedCount,
            failedExercisesInCurrentLesson = 0,
            isOutOfHearts = false,
            lastOutcome = LessonOutcome.LESSON_COMPLETED,
            streakIncrementedOnLastCompletion = true,
            bonusHeartAwardedOnLastCompletion = canAwardHeart,
            lastStatusMessage = statusMsg
        )
        _uiState.value = nextState

        viewModelScope.launch {
            streakRepository.recordPracticeSession(
                xpEarned = xpEarned.coerceAtLeast(10),
                languageCode = languageCode,
                accuracy = accuracyScore
            )
        }

        return nextState
    }

    /**
     * Called when the user fails an individual exercise or pronunciation attempt during a lesson.
     * - Deducts 1 heart.
     * - If hearts reach 0, marks [HeartsAndStreakUiState.isOutOfHearts] = true and records a failed lesson.
     * @return true if the user has run out of hearts (0 remaining / Game Over), false otherwise.
     */
    fun onExerciseFailed(accuracyScore: Int = 40): Boolean {
        val current = _uiState.value
        val remainingHearts = (current.hearts - 1).coerceAtLeast(0)
        val outOfHearts = remainingHearts == 0

        userPrefsRepo.setHearts(remainingHearts)

        val nextFailedLessons = if (outOfHearts) current.failedLessonsCount + 1 else current.failedLessonsCount
        val outcome = if (outOfHearts) {
            LessonOutcome.LESSON_FAILED_OUT_OF_HEARTS
        } else {
            LessonOutcome.EXERCISE_FAILED
        }

        val message = if (outOfHearts) {
            "¡Te quedaste sin vidas (0/${current.maxHearts})! La lección debe reiniciarse."
        } else {
            "Precisión baja ($accuracyScore%). Perdiste 1 vida • Quedan $remainingHearts/${current.maxHearts} ❤️"
        }

        _uiState.value = current.copy(
            hearts = remainingHearts,
            failedExercisesInCurrentLesson = current.failedExercisesInCurrentLesson + 1,
            failedLessonsCount = nextFailedLessons,
            isOutOfHearts = outOfHearts,
            lastOutcome = outcome,
            streakIncrementedOnLastCompletion = false,
            bonusHeartAwardedOnLastCompletion = false,
            lastStatusMessage = message
        )

        return outOfHearts
    }

    /**
     * Called when an entire lesson fails (either by losing the last heart or abandoning after critical errors).
     * - Deducts a heart (or depletes all hearts if [depleteAllHearts] is true).
     * - Optionally resets the current streak if [resetStreakOnLessonFailure] is true.
     */
    fun onLessonFailed(
        depleteAllHearts: Boolean = false,
        resetStreakOnLessonFailure: Boolean = false
    ): HeartsAndStreakUiState {
        val current = _uiState.value
        val remainingHearts = if (depleteAllHearts) {
            0
        } else {
            (current.hearts - 1).coerceAtLeast(0)
        }
        val outOfHearts = remainingHearts == 0

        userPrefsRepo.setHearts(remainingHearts)

        val updatedStreak = if (resetStreakOnLessonFailure) {
            userPrefsRepo.setStreakDays(0)
            0
        } else {
            current.currentStreak
        }

        val outcome = if (outOfHearts) {
            LessonOutcome.LESSON_FAILED_OUT_OF_HEARTS
        } else {
            LessonOutcome.EXERCISE_FAILED
        }

        val message = buildString {
            if (outOfHearts) {
                append("Lección fallida: 0/${current.maxHearts} vidas restantes.")
            } else {
                append("Intento fallido: $remainingHearts/${current.maxHearts} vidas restantes.")
            }
            if (resetStreakOnLessonFailure) {
                append(" Tu racha se ha reiniciado a 0.")
            }
        }

        val nextState = current.copy(
            hearts = remainingHearts,
            currentStreak = updatedStreak,
            practicedToday = if (resetStreakOnLessonFailure) false else current.practicedToday,
            failedLessonsCount = current.failedLessonsCount + 1,
            failedExercisesInCurrentLesson = current.failedExercisesInCurrentLesson + 1,
            isOutOfHearts = outOfHearts,
            lastOutcome = outcome,
            streakIncrementedOnLastCompletion = false,
            bonusHeartAwardedOnLastCompletion = false,
            lastStatusMessage = message
        )
        _uiState.value = nextState
        return nextState
    }

    /**
     * Evaluates an exercise's pronunciation accuracy against [threshold] and updates hearts/streak accordingly.
     * - If `accuracyScore < threshold`, deducts 1 heart.
     * - Returns true if passed, false if a heart was deducted.
     */
    fun evaluateAttempt(
        accuracyScore: Int,
        threshold: Int = HeartsAndStreakUiState.PASSING_ACCURACY_THRESHOLD
    ): Boolean {
        return if (accuracyScore < threshold) {
            onExerciseFailed(accuracyScore)
            false
        } else {
            true
        }
    }

    /**
     * Refills all hearts back to [HeartsAndStreakUiState.MAX_HEARTS] (e.g., when restarting a level or practicing).
     */
    fun refillHearts(): HeartsAndStreakUiState {
        userPrefsRepo.refillHearts()
        val max = _uiState.value.maxHearts
        val nextState = _uiState.value.copy(
            hearts = max,
            failedExercisesInCurrentLesson = 0,
            isOutOfHearts = false,
            lastOutcome = LessonOutcome.HEARTS_REFILLED,
            lastStatusMessage = "¡Vidas restauradas al máximo ($max/$max) ❤️!"
        )
        _uiState.value = nextState
        return nextState
    }

    /**
     * Restores a specific number of hearts up to [HeartsAndStreakUiState.MAX_HEARTS].
     */
    fun restoreHearts(count: Int = 1): HeartsAndStreakUiState {
        val current = _uiState.value
        val updated = (current.hearts + count.coerceAtLeast(1)).coerceIn(0, current.maxHearts)
        userPrefsRepo.setHearts(updated)
        val nextState = current.copy(
            hearts = updated,
            isOutOfHearts = updated == 0,
            lastOutcome = LessonOutcome.HEARTS_REFILLED,
            lastStatusMessage = "Recuperaste vidas: $updated/${current.maxHearts} ❤️"
        )
        _uiState.value = nextState
        return nextState
    }

    /**
     * Resets the user's current streak to 0 (e.g., when a daily streak is missed).
     */
    fun resetStreak(): HeartsAndStreakUiState {
        userPrefsRepo.setStreakDays(0)
        val nextState = _uiState.value.copy(
            currentStreak = 0,
            practicedToday = false,
            lastOutcome = LessonOutcome.STREAK_RESET,
            streakIncrementedOnLastCompletion = false,
            lastStatusMessage = "La racha volvió a 0 días. ¡Completa una lección para iniciar una nueva racha!"
        )
        _uiState.value = nextState
        return nextState
    }

    /**
     * Synchronizes internal state with the latest values from [UserPreferencesRepository].
     */
    fun syncFromPreferences(heartsOverride: Int? = null, streakOverride: Int? = null) {
        val profile = userPrefsRepo.userProfile.value
        val targetHearts = (heartsOverride ?: profile.hearts).coerceIn(0, _uiState.value.maxHearts)
        val targetStreak = (streakOverride ?: profile.streakDays).coerceAtLeast(0)
        val targetBest = maxOf(_uiState.value.bestStreak, userPrefsRepo.getBestStreakDays(), targetStreak)

        _uiState.value = _uiState.value.copy(
            hearts = targetHearts,
            currentStreak = targetStreak,
            bestStreak = targetBest,
            completedLessonsCount = profile.completedLessonIds.size,
            isOutOfHearts = targetHearts == 0
        )
    }
}
