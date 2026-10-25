package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.Language
import com.example.data.model.SpokenLanguage
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserPreferencesRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("linguavoice_prefs", Context.MODE_PRIVATE)

    private val _userProfile = MutableStateFlow(loadProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private fun loadProfile(): UserProfile {
        val spokenCode = prefs.getString("spoken_lang", SpokenLanguage.SPANISH.code) ?: SpokenLanguage.SPANISH.code
        val spokenLanguage = SpokenLanguage.fromCode(spokenCode)
        val langCode = prefs.getString("selected_lang", Language.ENGLISH.code) ?: Language.ENGLISH.code
        val selectedLanguage = Language.values().firstOrNull { it.code == langCode } ?: Language.ENGLISH
        val hasConfiguredPair = prefs.getBoolean("has_configured_language_pair", false)
        val streak = prefs.getInt("streak_days", 4)
        val xp = prefs.getInt("total_xp", 280)
        val gems = prefs.getInt("gems", 150)
        val hearts = prefs.getInt("hearts", 5)
        val completedCount = prefs.getInt("completed_count", 18)
        val accuracy = prefs.getInt("accuracy_avg", 88)
        val completedSet = prefs.getStringSet("completed_lessons", setOf("lesson_en_1")) ?: setOf("lesson_en_1")

        return UserProfile(
            spokenLanguage = spokenLanguage,
            selectedLanguage = selectedLanguage,
            hasConfiguredLanguagePair = hasConfiguredPair,
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

    fun setSpokenLanguage(spokenLanguage: SpokenLanguage) {
        prefs.edit().putString("spoken_lang", spokenLanguage.code).apply()
        _userProfile.value = _userProfile.value.copy(spokenLanguage = spokenLanguage)
    }

    fun setLanguagePair(
        spokenLanguage: SpokenLanguage,
        targetLanguage: Language,
        hasConfigured: Boolean = true
    ) {
        prefs.edit()
            .putString("spoken_lang", spokenLanguage.code)
            .putString("selected_lang", targetLanguage.code)
            .putBoolean("has_configured_language_pair", hasConfigured)
            .apply()
        _userProfile.value = _userProfile.value.copy(
            spokenLanguage = spokenLanguage,
            selectedLanguage = targetLanguage,
            hasConfiguredLanguagePair = hasConfigured
        )
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

    fun addHeart(maxHearts: Int = 5): Int {
        val current = _userProfile.value.hearts
        val updated = (current + 1).coerceIn(0, maxHearts)
        prefs.edit().putInt("hearts", updated).apply()
        _userProfile.value = _userProfile.value.copy(hearts = updated)
        return updated
    }

    fun setHearts(hearts: Int) {
        val clamped = hearts.coerceIn(0, 5)
        prefs.edit().putInt("hearts", clamped).apply()
        _userProfile.value = _userProfile.value.copy(hearts = clamped)
    }

    fun refillHearts() {
        prefs.edit().putInt("hearts", 5).apply()
        _userProfile.value = _userProfile.value.copy(hearts = 5)
    }

    fun setStreakDays(streakDays: Int) {
        val safeStreak = streakDays.coerceAtLeast(0)
        val best = maxOf(getBestStreakDays(), safeStreak)
        prefs.edit()
            .putInt("streak_days", safeStreak)
            .putInt("best_streak_days", best)
            .apply()
        _userProfile.value = _userProfile.value.copy(streakDays = safeStreak)
    }

    fun incrementStreakDays(): Int {
        val updated = (_userProfile.value.streakDays + 1).coerceAtLeast(1)
        setStreakDays(updated)
        return updated
    }

    fun getBestStreakDays(): Int {
        return prefs.getInt("best_streak_days", _userProfile.value.streakDays.coerceAtLeast(4))
    }

    fun setBestStreakDays(bestStreak: Int) {
        prefs.edit().putInt("best_streak_days", bestStreak.coerceAtLeast(0)).apply()
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

    fun hasShownDailyStreakPopup(dateString: String): Boolean {
        return prefs.getString("last_streak_popup_date", "") == dateString
    }

    fun markDailyStreakPopupShown(dateString: String) {
        prefs.edit().putString("last_streak_popup_date", dateString).apply()
    }

    fun isStreakBannerHiddenForDate(dateString: String): Boolean {
        return prefs.getString("streak_banner_hidden_date", "") == dateString
    }

    fun setStreakBannerHiddenForDate(dateString: String, hidden: Boolean) {
        if (hidden) {
            prefs.edit().putString("streak_banner_hidden_date", dateString).apply()
        } else {
            prefs.edit().remove("streak_banner_hidden_date").apply()
        }
    }

    fun getDailyXpGoal(): Int {
        return prefs.getInt("daily_xp_goal", StreakRepository.DEFAULT_DAILY_XP_GOAL).coerceIn(15, 300)
    }

    fun setDailyXpGoal(goalXp: Int) {
        prefs.edit().putInt("daily_xp_goal", goalXp.coerceIn(15, 300)).apply()
    }

    fun hasCelebratedDailyGoalForDate(dateString: String): Boolean {
        return prefs.getString("daily_goal_celebrated_date", "") == dateString
    }

    fun markDailyGoalCelebratedForDate(dateString: String) {
        prefs.edit().putString("daily_goal_celebrated_date", dateString).apply()
    }
}
