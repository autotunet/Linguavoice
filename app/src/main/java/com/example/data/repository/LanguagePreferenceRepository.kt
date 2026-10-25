package com.example.data.repository

import com.example.data.local.LanguagePreferenceDao
import com.example.data.local.LanguagePreferenceEntity
import com.example.data.model.Language
import com.example.data.model.LearningFocusGoal
import com.example.data.model.SpokenLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository wrapping [LanguagePreferenceDao] to persist and observe the user's spoken language
 * ("Idioma que hablas") and target language ("Idioma que deseas aprender") in Room,
 * personalizing lessons across the application.
 */
class LanguagePreferenceRepository(
    private val languagePreferenceDao: LanguagePreferenceDao
) {

    /**
     * Reactive stream of the user's language pair preference stored in Room.
     */
    val targetLanguagePreferenceFlow: Flow<LanguagePreferenceEntity> =
        languagePreferenceDao.observeTargetLanguagePreference().map { entity ->
            entity ?: LanguagePreferenceEntity.fromLanguage(
                language = Language.ENGLISH,
                spokenLanguage = SpokenLanguage.SPANISH,
                hasCompletedLanguageOnboarding = false
            )
        }

    /**
     * Reactive stream of the user's selected target [Language] from Room.
     */
    val selectedLanguageFlow: Flow<Language> =
        targetLanguagePreferenceFlow.map { it.toLanguage() }

    /**
     * Reactive stream of the user's spoken [SpokenLanguage] from Room.
     */
    val spokenLanguageFlow: Flow<SpokenLanguage> =
        targetLanguagePreferenceFlow.map { it.toSpokenLanguage() }

    /**
     * Seeds the initial language preference in Room if not yet stored.
     */
    suspend fun ensureDefaultPreferenceSeeded(
        fallbackLanguage: Language = Language.ENGLISH,
        fallbackFocus: LearningFocusGoal = LearningFocusGoal.DAILY_CONVERSATION,
        fallbackSpokenLanguage: SpokenLanguage = SpokenLanguage.SPANISH
    ): LanguagePreferenceEntity {
        val existing = languagePreferenceDao.getTargetLanguagePreference()
        if (existing != null) {
            return existing
        }
        val initial = LanguagePreferenceEntity.fromLanguage(
            language = fallbackLanguage,
            learningFocusGoal = fallbackFocus,
            spokenLanguage = fallbackSpokenLanguage,
            hasCompletedLanguageOnboarding = false
        )
        languagePreferenceDao.upsertTargetLanguagePreference(initial)
        return initial
    }

    /**
     * Reads the current language pair preference synchronously from Room (or seeds default).
     */
    fun getOrSeedPreferenceSync(
        fallbackLanguage: Language = Language.ENGLISH,
        fallbackFocus: LearningFocusGoal = LearningFocusGoal.DAILY_CONVERSATION,
        fallbackSpokenLanguage: SpokenLanguage = SpokenLanguage.SPANISH
    ): LanguagePreferenceEntity {
        val existing = languagePreferenceDao.getTargetLanguagePreferenceSync()
        if (existing != null) {
            return existing
        }
        val initial = LanguagePreferenceEntity.fromLanguage(
            language = fallbackLanguage,
            learningFocusGoal = fallbackFocus,
            spokenLanguage = fallbackSpokenLanguage,
            hasCompletedLanguageOnboarding = false
        )
        languagePreferenceDao.upsertTargetLanguagePreferenceSync(initial)
        return initial
    }

    /**
     * Saves the user's chosen spoken language, target language, and learning focus in Room via coroutine.
     */
    suspend fun saveTargetLanguagePreference(
        language: Language,
        learningFocusGoal: LearningFocusGoal = LearningFocusGoal.DAILY_CONVERSATION,
        spokenLanguage: SpokenLanguage? = null,
        hasCompletedOnboarding: Boolean = true
    ): LanguagePreferenceEntity {
        val currentSpoken = spokenLanguage
            ?: languagePreferenceDao.getTargetLanguagePreference()?.toSpokenLanguage()
            ?: SpokenLanguage.SPANISH
        val entity = LanguagePreferenceEntity.fromLanguage(
            language = language,
            learningFocusGoal = learningFocusGoal,
            spokenLanguage = currentSpoken,
            hasCompletedLanguageOnboarding = hasCompletedOnboarding,
            updatedAtTimestamp = System.currentTimeMillis()
        )
        languagePreferenceDao.upsertTargetLanguagePreference(entity)
        return entity
    }

    /**
     * Saves the user's chosen spoken language, target language, and learning focus in Room synchronously,
     * ensuring immediate persistence before reactive collectors and navigation transitions run.
     */
    fun saveTargetLanguagePreferenceSync(
        language: Language,
        learningFocusGoal: LearningFocusGoal = LearningFocusGoal.DAILY_CONVERSATION,
        spokenLanguage: SpokenLanguage? = null,
        hasCompletedOnboarding: Boolean = true
    ): LanguagePreferenceEntity {
        val currentSpoken = spokenLanguage
            ?: languagePreferenceDao.getTargetLanguagePreferenceSync()?.toSpokenLanguage()
            ?: SpokenLanguage.SPANISH
        val entity = LanguagePreferenceEntity.fromLanguage(
            language = language,
            learningFocusGoal = learningFocusGoal,
            spokenLanguage = currentSpoken,
            hasCompletedLanguageOnboarding = hasCompletedOnboarding,
            updatedAtTimestamp = System.currentTimeMillis()
        )
        languagePreferenceDao.upsertTargetLanguagePreferenceSync(entity)
        return entity
    }
}
