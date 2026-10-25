package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Language
import com.example.data.model.LearningFocusGoal
import com.example.data.model.SpokenLanguage

/**
 * Room Entity storing the user's spoken language ("Idioma que hablas"),
 * selected target language ("Idioma que deseas aprender"), and learning focus
 * to personalize lessons across the application.
 */
@Entity(tableName = "target_language_preferences")
data class LanguagePreferenceEntity(
    @PrimaryKey
    val id: Int = SINGLETON_ID,
    val spokenLanguageCode: String = SpokenLanguage.SPANISH.code,
    val spokenLanguageDisplayName: String = SpokenLanguage.SPANISH.displayName,
    val languageCode: String = Language.ENGLISH.code,
    val displayName: String = Language.ENGLISH.displayName,
    val learningFocusId: String = LearningFocusGoal.DAILY_CONVERSATION.id,
    val hasCompletedLanguageOnboarding: Boolean = false,
    val updatedAtTimestamp: Long = System.currentTimeMillis()
) {
    fun toSpokenLanguage(): SpokenLanguage = SpokenLanguage.fromCode(spokenLanguageCode)

    fun toLanguage(): Language = Language.fromCode(languageCode)

    fun toLearningFocusGoal(): LearningFocusGoal = LearningFocusGoal.fromId(learningFocusId)

    companion object {
        const val SINGLETON_ID = 1

        fun fromLanguage(
            language: Language,
            learningFocusGoal: LearningFocusGoal = LearningFocusGoal.DAILY_CONVERSATION,
            spokenLanguage: SpokenLanguage = SpokenLanguage.SPANISH,
            hasCompletedLanguageOnboarding: Boolean = true,
            updatedAtTimestamp: Long = System.currentTimeMillis()
        ): LanguagePreferenceEntity {
            val resolvedTarget = Language.resolveDistinctTargetLanguage(spokenLanguage, language)
            return LanguagePreferenceEntity(
                id = SINGLETON_ID,
                spokenLanguageCode = spokenLanguage.code,
                spokenLanguageDisplayName = spokenLanguage.displayName,
                languageCode = resolvedTarget.code,
                displayName = resolvedTarget.displayName,
                learningFocusId = learningFocusGoal.id,
                hasCompletedLanguageOnboarding = hasCompletedLanguageOnboarding,
                updatedAtTimestamp = updatedAtTimestamp
            )
        }
    }
}
