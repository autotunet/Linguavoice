package com.example

import com.example.audio.PronunciationEvaluator
import com.example.data.local.PracticeDayEntity
import com.example.data.model.Language
import com.example.data.repository.StreakInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ExampleUnitTest {

    @Test
    fun testExactPronunciationMatch() {
        val target = "Hello, nice to meet you!"
        val spoken = "hello nice to meet you"
        val result = PronunciationEvaluator.evaluate(target, spoken, null, Language.ENGLISH)

        assertTrue(result.isPassed)
        assertEquals(100, result.scorePercentage)
        assertTrue(result.wordAccuracies.all { it.isCorrect })
    }

    @Test
    fun testNearPronunciationMatch() {
        val target = "Could I have a hot coffee, please?"
        val spoken = "could i have a hot cafe please"
        val result = PronunciationEvaluator.evaluate(target, spoken, null, Language.ENGLISH)

        assertTrue(result.isPassed)
        assertTrue(result.scorePercentage >= 80)
    }

    @Test
    fun testMispronouncedPhrase() {
        val target = "Bonjour, enchanté de faire votre connaissance."
        val spoken = "goodbye wrong words"
        val result = PronunciationEvaluator.evaluate(target, spoken, null, Language.FRENCH)

        assertFalse(result.isPassed)
        assertTrue(result.scorePercentage < 40)
    }

    @Test
    fun testPracticeDayEntityCreation() {
        val dateStr = "2026-10-21"
        val entity = PracticeDayEntity(
            dateString = dateStr,
            exercisesCompleted = 2,
            xpEarned = 30,
            languageCode = "en",
            accuracy = 92
        )
        assertEquals("2026-10-21", entity.dateString)
        assertEquals(2, entity.exercisesCompleted)
        assertEquals(30, entity.xpEarned)
        assertEquals(92, entity.accuracy)
    }

    @Test
    fun testWeeklyActivityAndAccuracyMetrics() {
        val weekDays = listOf(
            com.example.data.repository.WeekDayStreak("L", "2026-10-19", isToday = false, isPracticed = true, isFuture = false, exercisesCompleted = 3, accuracy = 84, xpEarned = 45),
            com.example.data.repository.WeekDayStreak("M", "2026-10-20", isToday = false, isPracticed = true, isFuture = false, exercisesCompleted = 4, accuracy = 89, xpEarned = 60),
            com.example.data.repository.WeekDayStreak("M", "2026-10-21", isToday = true, isPracticed = true, isFuture = false, exercisesCompleted = 5, accuracy = 95, xpEarned = 75),
            com.example.data.repository.WeekDayStreak("J", "2026-10-22", isToday = false, isPracticed = false, isFuture = true, exercisesCompleted = 0, accuracy = 0, xpEarned = 0)
        )

        val totalExercises = weekDays.sumOf { it.exercisesCompleted }
        val practicedWithAccuracy = weekDays.filter { it.isPracticed && it.accuracy > 0 }
        val avgAccuracy = practicedWithAccuracy.sumOf { it.accuracy } / practicedWithAccuracy.size
        val trendDelta = practicedWithAccuracy.last().accuracy - practicedWithAccuracy.first().accuracy

        assertEquals(12, totalExercises)
        assertEquals(89, avgAccuracy) // (84 + 89 + 95) / 3 = 89
        assertEquals(11, trendDelta) // 95 - 84 = +11% improvement trend
    }

    @Test
    fun testPronunciationMetricsAndWordAccuracies() {
        val target = "Good morning, how are you today?"
        val spoken = "good morning how are you today"
        val result = PronunciationEvaluator.evaluate(target, spoken, null, Language.ENGLISH)

        assertEquals(100, result.scorePercentage)
        assertEquals(100, result.phonemeScore)
        assertEquals(100, result.completenessScore)
        assertTrue(result.fluencyScore >= 90)
        assertTrue(result.wordAccuracies.all { it.isCorrect })
    }

    @Test
    fun testRealTimePartialSpeechWordHighlighting() {
        val target = "Where is the train station?"
        val partialSpoken = "where is the"
        val liveWords = PronunciationEvaluator.evaluateLivePartialWords(target, partialSpoken)

        assertEquals(5, liveWords.size)
        assertTrue(liveWords[0].isCorrect) // Where
        assertTrue(liveWords[1].isCorrect) // is
        assertTrue(liveWords[2].isCorrect) // the
        assertFalse(liveWords[3].isCorrect) // train (not spoken yet)
        assertFalse(liveWords[4].isCorrect) // station? (not spoken yet)
    }

    @Test
    fun testSpeechRecognizerMultiCandidateSelection() {
        val target = "I would like a table for two"
        val candidates = listOf(
            "I wood light a cable four to",
            "I would like a table for two",
            "I would like a table for too"
        )
        val best = PronunciationEvaluator.selectBestCandidate(target, candidates, Language.ENGLISH)
        assertEquals("I would like a table for two", best)
    }
}
