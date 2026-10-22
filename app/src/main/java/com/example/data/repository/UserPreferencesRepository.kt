package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.Language
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserPreferencesRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("linguavoice_prefs", Context.MODE_PRIVATE)

    private val _userProfile = MutableStateFlow(loadProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private fun loadProfile(): UserProfile {
        val langCode = prefs.getString("selected_lang", Language.ENGLISH.code) ?: Language.ENGLISH.code
        val selectedLanguage = Language.values().firstOrNull { it.code == langCode } ?: Language.ENGLISH
        val streak = prefs.getInt("streak_days", 4)
        val xp = prefs.getInt("total_xp", 280)
        val gems = prefs.getInt("gems", 150)
        val hearts = prefs.getInt("hearts", 5)
        val completedCount = prefs.getInt("completed_count", 18)
        val accuracy = prefs.getInt("accuracy_avg", 88)
        val completedSet = prefs.getStringSet("completed_lessons", setOf("lesson_en_1")) ?: setOf("lesson_en_1")

        return UserProfile(
            selectedLanguage = selectedLanguage,
            streakDays = streak,
            totalXp = xp,
            gems = gems,
            hearts = hearts,
            completedExercisesCount = completedCount,
            accuracyAverage = accuracy,
            completedLessonIds = completedSet
        )
    }

    fun setSelectedLanguage(language: Language) {
        prefs.edit().putString("selected_lang", language.code).apply()
        _userProfile.value = _userProfile.value.copy(selectedLanguage = language)
    }

    fun addXp(amount: Int) {
        val newXp = _userProfile.value.totalXp + amount
        val newGems = _userProfile.value.gems + (amount / 5)
        val newCount = _userProfile.value.completedExercisesCount + 1
        prefs.edit()
            .putInt("total_xp", newXp)
            .putInt("gems", newGems)
            .putInt("completed_count", newCount)
            .apply()
        _userProfile.value = _userProfile.value.copy(
            totalXp = newXp,
            gems = newGems,
            completedExercisesCount = newCount
        )
    }

    fun deductHeart(): Int {
        val current = _userProfile.value.hearts
        val updated = (current - 1).coerceAtLeast(0)
        prefs.edit().putInt("hearts", updated).apply()
        _userProfile.value = _userProfile.value.copy(hearts = updated)
        return updated
    }

    fun refillHearts() {
        prefs.edit().putInt("hearts", 5).apply()
        _userProfile.value = _userProfile.value.copy(hearts = 5)
    }

    fun markLessonCompleted(lessonId: String) {
        val updatedSet = _userProfile.value.completedLessonIds.toMutableSet()
        updatedSet.add(lessonId)
        prefs.edit().putStringSet("completed_lessons", updatedSet).apply()
        _userProfile.value = _userProfile.value.copy(completedLessonIds = updatedSet)
    }

    fun updateAccuracy(latestScore: Int) {
        val currentAvg = _userProfile.value.accuracyAverage
        val newAvg = ((currentAvg * 4 + latestScore) / 5).coerceIn(0, 100)
        prefs.edit().putInt("accuracy_avg", newAvg).apply()
        _userProfile.value = _userProfile.value.copy(accuracyAverage = newAvg)
    }
}
