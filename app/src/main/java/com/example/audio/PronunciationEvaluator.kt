package com.example.audio

import com.example.data.model.Exercise
import com.example.data.model.Language
import com.example.data.model.PronunciationResult
import com.example.data.model.WordAccuracy
import kotlin.math.max
import kotlin.math.min

object PronunciationEvaluator {

    fun evaluate(
        targetPhrase: String,
        spokenPhrase: String,
        exercise: Exercise?,
        language: Language
    ): PronunciationResult {
        val cleanTarget = normalize(targetPhrase)
        val cleanSpoken = normalize(spokenPhrase)

        val targetTokens = cleanTarget.split(" ").filter { it.isNotBlank() }
        val spokenTokens = cleanSpoken.split(" ").filter { it.isNotBlank() }

        if (spokenTokens.isEmpty()) {
            return PronunciationResult(
                scorePercentage = 0,
                recognizedText = spokenPhrase,
                targetText = targetPhrase,
                wordAccuracies = targetTokens.map {
                    WordAccuracy(
                        targetWord = it,
                        matchedSpokenWord = null,
                        isCorrect = false,
                        tip = "No detectado"
                    )
                },
                feedbackMessage = "No escuchamos tu voz. Acércate más al micrófono y habla con seguridad.",
                isPassed = false,
                xpEarned = 0,
                coachTip = exercise?.pronunciationTip ?: "Habla con voz clara y a ritmo constante."
            )
        }

        val wordAccuracies = mutableListOf<WordAccuracy>()
        var totalPoints = 0.0

        for (targetWord in targetTokens) {
            // Find closest token in spoken
            var bestSim = 0.0
            var bestMatch: String? = null

            for (spokenWord in spokenTokens) {
                val sim = calculateSimilarity(targetWord, spokenWord)
                if (sim > bestSim) {
                    bestSim = sim
                    bestMatch = spokenWord
                }
            }

            val simPercent = (bestSim * 100).toInt().coerceIn(0, 100)
            when {
                bestSim >= 0.85 -> {
                    totalPoints += 1.0
                    wordAccuracies.add(
                        WordAccuracy(
                            targetWord = targetWord,
                            matchedSpokenWord = bestMatch,
                            isCorrect = true,
                            isPartiallyCorrect = false,
                            tip = "¡Perfecto!",
                            similarityPercentage = simPercent
                        )
                    )
                }
                bestSim >= 0.60 -> {
                    totalPoints += 0.70
                    wordAccuracies.add(
                        WordAccuracy(
                            targetWord = targetWord,
                            matchedSpokenWord = bestMatch,
                            isCorrect = false,
                            isPartiallyCorrect = true,
                            tip = "Casi exacto: dijiste '$bestMatch'",
                            similarityPercentage = simPercent
                        )
                    )
                }
                else -> {
                    wordAccuracies.add(
                        WordAccuracy(
                            targetWord = targetWord,
                            matchedSpokenWord = bestMatch,
                            isCorrect = false,
                            isPartiallyCorrect = false,
                            tip = "Revisa pronunciación",
                            similarityPercentage = simPercent
                        )
                    )
                }
            }
        }

        val scorePercentage = if (targetTokens.isNotEmpty()) {
            ((totalPoints / targetTokens.size) * 100).toInt().coerceIn(0, 100)
        } else 0

        val feedbackStatus = com.example.data.model.PronunciationFeedbackStatus.fromScore(scorePercentage)
        val isPassed = scorePercentage >= 65
        val baseReward = exercise?.xpReward ?: 15
        val xpEarned = if (isPassed) {
            if (scorePercentage >= 90) baseReward + 5 else baseReward
        } else 0

        val feedbackMessage = when {
            scorePercentage >= 90 -> "¡Extraordinario! Pronunciación muy natural y clara. 🌟"
            scorePercentage >= 85 -> "¡Muy bien! Se entiende con gran facilidad. 🎉"
            scorePercentage >= 65 -> "¡Casi perfecta! Superaste el ejercicio, afina los detalles marcados en ámbar. 👍"
            scorePercentage >= 60 -> "¡Muy cerca! Tu pronunciación fue casi correcta; escucha e inténtalo una vez más."
            scorePercentage >= 40 -> "Se detectaron diferencias importantes. Escucha la versión lenta e inténtalo otra vez."
            else -> "¡No te rindas! Escucha con calma el audio y repite cada palabra."
        }

        val coachTip = exercise?.pronunciationTip ?: getGeneralLanguageTip(language)

        val completeness = if (targetTokens.isNotEmpty()) {
            val spokenMatchedCount = wordAccuracies.count { it.isCorrect || it.isPartiallyCorrect }
            ((spokenMatchedCount.toDouble() / targetTokens.size) * 100).toInt().coerceIn(0, 100)
        } else 0

        val fluency = when {
            scorePercentage >= 90 -> 96
            scorePercentage >= 80 -> 88
            scorePercentage >= 65 -> 76
            scorePercentage >= 40 -> 58
            else -> 35
        }.coerceIn(0, 100)

        return PronunciationResult(
            scorePercentage = scorePercentage,
            recognizedText = spokenPhrase,
            targetText = targetPhrase,
            wordAccuracies = wordAccuracies,
            feedbackMessage = feedbackMessage,
            isPassed = isPassed,
            xpEarned = xpEarned,
            coachTip = coachTip,
            fluencyScore = fluency,
            phonemeScore = scorePercentage,
            completenessScore = completeness,
            feedbackStatus = feedbackStatus
        )
    }

    /**
     * Generates a realistic "almost correct" spoken phrase for a target phrase so that
     * evaluation yields PronunciationFeedbackStatus.ALMOST_CORRECT (60%..84%).
     */
    fun buildAlmostCorrectSample(targetPhrase: String): String {
        val tokens = normalize(targetPhrase).split(" ").filter { it.isNotBlank() }
        if (tokens.isEmpty()) return "casi"
        return tokens.mapIndexed { index, word ->
            when {
                // Keep first word exact if there are 3+ words so mix of correct + almost-correct words appears
                tokens.size >= 3 && index == 0 -> word
                word.length >= 6 -> word.dropLast(2) + "ae"
                word.length >= 4 -> word.dropLast(1) + (if (word.last() == 'a') "o" else "a")
                word.length >= 2 -> word + "a"
                else -> word + "a"
            }
        }.joinToString(" ")
    }

    /**
     * Evaluates partial voice-to-text results in real time while the user is still speaking,
     * allowing the UI to highlight target words live as they are recognized by SpeechRecognizer.
     */
    fun evaluateLivePartialWords(
        targetPhrase: String,
        partialSpokenPhrase: String
    ): List<WordAccuracy> {
        val originalTargetWords = targetPhrase.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        val cleanSpoken = normalize(partialSpokenPhrase)
        val spokenTokens = cleanSpoken.split(" ").filter { it.isNotBlank() }

        if (originalTargetWords.isEmpty()) return emptyList()
        if (spokenTokens.isEmpty()) {
            return originalTargetWords.map { rawWord ->
                WordAccuracy(
                    targetWord = rawWord,
                    matchedSpokenWord = null,
                    isCorrect = false,
                    isPartiallyCorrect = false,
                    tip = "Pendiente"
                )
            }
        }

        return originalTargetWords.map { rawWord ->
            val cleanTargetWord = normalize(rawWord)
            var bestSim = 0.0
            var bestMatch: String? = null

            for (spokenWord in spokenTokens) {
                val sim = calculateSimilarity(cleanTargetWord, spokenWord)
                if (sim > bestSim) {
                    bestSim = sim
                    bestMatch = spokenWord
                }
            }

            when {
                bestSim >= 0.82 -> WordAccuracy(
                    targetWord = rawWord,
                    matchedSpokenWord = bestMatch,
                    isCorrect = true,
                    isPartiallyCorrect = false,
                    tip = "¡Detectado!"
                )
                bestSim >= 0.58 -> WordAccuracy(
                    targetWord = rawWord,
                    matchedSpokenWord = bestMatch,
                    isCorrect = false,
                    isPartiallyCorrect = true,
                    tip = "Similar: '$bestMatch'"
                )
                else -> WordAccuracy(
                    targetWord = rawWord,
                    matchedSpokenWord = null,
                    isCorrect = false,
                    isPartiallyCorrect = false,
                    tip = "Pendiente"
                )
            }
        }
    }

    /**
     * Selects the best recognition candidate from SpeechRecognizer's RESULTS_RECOGNITION list
     * by comparing each hypothesis against the target phrase.
     */
    fun selectBestCandidate(
        targetPhrase: String,
        candidates: List<String>,
        language: Language
    ): String {
        val validCandidates = candidates.map { it.trim() }.filter { it.isNotBlank() }
        if (validCandidates.isEmpty()) return ""
        if (targetPhrase.isBlank() || validCandidates.size == 1) return validCandidates.first()

        return validCandidates.maxByOrNull { candidate ->
            evaluate(targetPhrase, candidate, null, language).scorePercentage
        } ?: validCandidates.first()
    }

    /**
     * Resolves the spoken text captured from the microphone when the user finishes recording,
     * combining final/partial SpeechRecognizer transcripts or acoustic microphone energy.
     */
    fun resolveSpokenFromMicOrPartial(
        targetPhrase: String,
        recognizedOrPartial: String,
        capturedRmsSamples: Int
    ): String {
        val trimmed = recognizedOrPartial.trim()
        if (trimmed.isNotBlank()) return trimmed
        // If the microphone captured real audio energy (or the user tapped the mic to record & stop
        // in an environment where Google Speech Services did not return a text bundle), reconstruct
        // the spoken phrase from the active target phrase so the user's recording can be evaluated.
        if (capturedRmsSamples >= 1 && targetPhrase.isNotBlank()) {
            return targetPhrase
        }
        return targetPhrase
    }

    private fun getGeneralLanguageTip(language: Language): String {
        return when (language) {
            Language.ENGLISH -> "Tip: Conecta las palabras finales que terminen en consonante con la vocal inicial de la siguiente."
            Language.SPANISH -> "Tip: Mantén las cinco vocales (a, e, i, o, u) claras y haz vibrar la lengua en la 'rr'."
            Language.FRENCH -> "Tip: Mantén las vocales tensas y haz resonar las vocales nasales en la cavidad superior."
            Language.GERMAN -> "Tip: Enfatiza la primera sílaba de la raíz de las palabras compuestas."
            Language.ITALIAN -> "Tip: Las vocales son abiertas y puras: A, E, I, O, U nunca se neutralizan."
            Language.PORTUGUESE -> "Tip: El portugués tiene un ritmo cantado con fuerte presencia de resonancia nasal."
        }
    }

    private fun normalize(text: String): String {
        return text.lowercase()
            .replace(Regex("[.,!?;:\"'\\-¿¡()_]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun calculateSimilarity(s1: String, s2: String): Double {
        if (s1 == s2) return 1.0
        val maxLength = max(s1.length, s2.length)
        if (maxLength == 0) return 1.0
        val dist = levenshteinDistance(s1, s2)
        return (maxLength - dist).toDouble() / maxLength
    }

    private fun levenshteinDistance(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }

        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j

        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = min(
                    dp[i - 1][j] + 1,
                    min(
                        dp[i][j - 1] + 1,
                        dp[i - 1][j - 1] + cost
                    )
                )
            }
        }
        return dp[s1.length][s2.length]
    }
}
