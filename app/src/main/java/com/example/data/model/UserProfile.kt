package com.example.data.model

data class UserProfile(
    val selectedLanguage: Language = Language.ENGLISH,
    val streakDays: Int = 4,
    val totalXp: Int = 340,
    val gems: Int = 180,
    val hearts: Int = 5,
    val maxHearts: Int = 5,
    val completedExercisesCount: Int = 28,
    val accuracyAverage: Int = 92,
    val completedLessonIds: Set<String> = setOf("lesson_en_1"),
    val unlockedBadges: List<Badge> = listOf(
        Badge("1", "🔥 Primer Fuego", "Mantén 3 días de racha consecutivos", isUnlocked = true),
        Badge("2", "🎙️ Voz de Oro", "Alcanza 95% de pronunciación en una lección", isUnlocked = true),
        Badge("3", "🌍 Políglota", "Prueba ejercicios en más de 2 idiomas", isUnlocked = false),
        Badge("4", "⚡ Velocista", "Completa un desafío en menos de 1 minuto", isUnlocked = false)
    )
)

data class Badge(
    val id: String,
    val title: String,
    val description: String,
    val isUnlocked: Boolean
)
