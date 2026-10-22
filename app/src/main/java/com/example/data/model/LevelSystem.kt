package com.example.data.model

data class DifficultyTier(
    val tierNumber: Int,
    val title: String,
    val description: String,
    val requiredLevel: Int,
    val requiredXp: Int,
    val categoryKey: String,
    val badgeEmoji: String
)

data class UserLevelInfo(
    val level: Int,
    val levelTitle: String,
    val badgeEmoji: String,
    val totalXp: Int,
    val currentLevelBaseXp: Int,
    val nextLevelTargetXp: Int,
    val xpInCurrentLevel: Int,
    val xpNeededForNextLevel: Int,
    val progressFraction: Float,
    val unlockedTiers: List<DifficultyTier>
)

object LevelSystem {

    val DIFFICULTY_TIERS = listOf(
        DifficultyTier(
            tierNumber = 1,
            title = "Nivel 1: Saludos & Básico",
            description = "Frases cotidianas, presentaciones y cortesía básica.",
            requiredLevel = 1,
            requiredXp = 0,
            categoryKey = "Basico",
            badgeEmoji = "🐣"
        ),
        DifficultyTier(
            tierNumber = 2,
            title = "Nivel 2: Café & Restaurante",
            description = "Pedidos, comida, ingredientes y peticiones de cuenta.",
            requiredLevel = 2,
            requiredXp = 100,
            categoryKey = "Cafe",
            badgeEmoji = "☕"
        ),
        DifficultyTier(
            tierNumber = 3,
            title = "Nivel 3: Viajes & Ciudad",
            description = "Aeropuertos, billetes, direcciones y transporte urbano.",
            requiredLevel = 3,
            requiredXp = 250,
            categoryKey = "Viajes",
            badgeEmoji = "✈️"
        ),
        DifficultyTier(
            tierNumber = 4,
            title = "Nivel 4: Fonemas Difíciles",
            description = "Contraste de sonidos difíciles (TH, R, vocales nasales y abiertas).",
            requiredLevel = 4,
            requiredXp = 450,
            categoryKey = "Fonemas",
            badgeEmoji = "🎯"
        ),
        DifficultyTier(
            tierNumber = 5,
            title = "Nivel 5: Trabalenguas Élite",
            description = "Articulación veloz y agilidad muscular de lengua y labios.",
            requiredLevel = 5,
            requiredXp = 700,
            categoryKey = "Trabalenguas",
            badgeEmoji = "🔥"
        )
    )

    fun calculateLevelInfo(totalXp: Int): UserLevelInfo {
        val level: Int
        val title: String
        val emoji: String
        val baseXp: Int
        val nextXp: Int

        when {
            totalXp < 100 -> {
                level = 1
                title = "Novato del Habla"
                emoji = "🐣"
                baseXp = 0
                nextXp = 100
            }
            totalXp < 250 -> {
                level = 2
                title = "Voz Confiada"
                emoji = "🗣️"
                baseXp = 100
                nextXp = 250
            }
            totalXp < 450 -> {
                level = 3
                title = "Viajero Articulado"
                emoji = "✈️"
                baseXp = 250
                nextXp = 450
            }
            totalXp < 700 -> {
                level = 4
                title = "Acróbata Fonético"
                emoji = "🎯"
                baseXp = 450
                nextXp = 700
            }
            else -> {
                level = 5 + ((totalXp - 700) / 300)
                title = "Maestro de la Fluidez"
                emoji = "👑"
                baseXp = 700 + (level - 5) * 300
                nextXp = baseXp + 300
            }
        }

        val range = (nextXp - baseXp).coerceAtLeast(1)
        val currentProgress = (totalXp - baseXp).coerceIn(0, range)
        val fraction = (currentProgress.toFloat() / range.toFloat()).coerceIn(0f, 1f)

        val unlocked = DIFFICULTY_TIERS.filter { totalXp >= it.requiredXp || level >= it.requiredLevel }

        return UserLevelInfo(
            level = level,
            levelTitle = title,
            badgeEmoji = emoji,
            totalXp = totalXp,
            currentLevelBaseXp = baseXp,
            nextLevelTargetXp = nextXp,
            xpInCurrentLevel = currentProgress,
            xpNeededForNextLevel = (nextXp - totalXp).coerceAtLeast(0),
            progressFraction = fraction,
            unlockedTiers = unlocked
        )
    }

    fun isTierUnlocked(tierNumber: Int, userLevel: Int): Boolean {
        return userLevel >= tierNumber
    }

    fun isCategoryUnlocked(category: String, userLevel: Int): Boolean {
        return when (category) {
            "Todos", "Básico", "Saludos" -> true
            "Café" -> userLevel >= 2
            "Viajes" -> userLevel >= 3
            "Fonemas" -> userLevel >= 4
            "Trabalenguas" -> userLevel >= 4
            else -> true
        }
    }

    fun getRequiredLevelForCategory(category: String): Int {
        return when (category) {
            "Café" -> 2
            "Viajes" -> 3
            "Fonemas" -> 4
            "Trabalenguas" -> 4
            else -> 1
        }
    }
}
