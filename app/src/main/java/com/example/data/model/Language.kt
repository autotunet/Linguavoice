package com.example.data.model

import java.util.Locale

enum class Language(
    val code: String,
    val displayName: String,
    val flagEmoji: String,
    val locale: Locale,
    val greeting: String
) {
    ENGLISH(
        code = "en",
        displayName = "Inglés",
        flagEmoji = "🇺🇸",
        locale = Locale.US,
        greeting = "Hello, how are you today?"
    ),
    FRENCH(
        code = "fr",
        displayName = "Francés",
        flagEmoji = "🇫🇷",
        locale = Locale.FRANCE,
        greeting = "Bonjour, enchanté de faire votre connaissance !"
    ),
    GERMAN(
        code = "de",
        displayName = "Alemán",
        flagEmoji = "🇩🇪",
        locale = Locale.GERMANY,
        greeting = "Guten Tag, wie geht es Ihnen?"
    ),
    ITALIAN(
        code = "it",
        displayName = "Italiano",
        flagEmoji = "🇮🇹",
        locale = Locale.ITALY,
        greeting = "Ciao, piacere di conoscerti!"
    ),
    PORTUGUESE(
        code = "pt",
        displayName = "Portugués",
        flagEmoji = "🇧🇷",
        locale = Locale("pt", "BR"),
        greeting = "Olá, tudo bem com você?"
    )
}

enum class ExerciseType {
    REPEAT_PHRASE,        // Duolingo speak phrase
    LISTENING_AND_SPEAK, // Listen first, then reproduce
    FILL_IN_BLANK_SPEAK, // Speak the missing word
    TONGUE_TWISTER,      // High-speed pronunciation challenge
    DIALOGUE_REPLY       // Conversational response
}

data class Exercise(
    val id: String,
    val language: Language,
    val lessonId: String,
    val type: ExerciseType,
    val promptQuestion: String,
    val targetPhrase: String,
    val translation: String,
    val phoneticGuide: String,
    val pronunciationTip: String,
    val xpReward: Int = 15
)

data class LessonModule(
    val id: String,
    val language: Language,
    val title: String,
    val description: String,
    val iconName: String,
    val requiredLevel: Int,
    val totalExercises: Int,
    val exercises: List<Exercise>
)

data class WordAccuracy(
    val targetWord: String,
    val matchedSpokenWord: String?,
    val isCorrect: Boolean,
    val isPartiallyCorrect: Boolean = false,
    val tip: String? = null
)

data class PronunciationResult(
    val scorePercentage: Int, // 0 - 100
    val recognizedText: String,
    val targetText: String,
    val wordAccuracies: List<WordAccuracy>,
    val feedbackMessage: String,
    val isPassed: Boolean,
    val xpEarned: Int,
    val coachTip: String,
    val fluencyScore: Int = ((scorePercentage * 0.95f) + 5).toInt().coerceIn(0, 100),
    val phonemeScore: Int = scorePercentage,
    val completenessScore: Int = if (wordAccuracies.isNotEmpty()) {
        ((wordAccuracies.count { it.isCorrect || it.isPartiallyCorrect }.toFloat() / wordAccuracies.size) * 100).toInt()
    } else 0
)

data class DialogueMessage(
    val sender: String, // "AI" or "USER"
    val avatarName: String,
    val targetText: String,
    val translation: String,
    val expectedUserReply: String? = null,
    val userSpokenReply: String? = null,
    val isCompleted: Boolean = false
)

data class DialogueScenario(
    val id: String,
    val language: Language,
    val title: String,
    val description: String,
    val situation: String,
    val turns: List<DialogueMessage>
)
