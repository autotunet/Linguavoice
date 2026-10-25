package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PronunciationFeedbackStatus
import com.example.data.model.PronunciationResult
import com.example.data.model.SpokenLanguage
import com.example.data.model.WordAccuracy
import com.example.ui.theme.DuoBlue
import com.example.ui.theme.DuoCardBorder
import com.example.ui.theme.DuoGreen
import com.example.ui.theme.DuoGreenDark
import com.example.ui.theme.DuoGreenLight
import com.example.ui.theme.DuoOrange
import com.example.ui.theme.DuoPurple
import com.example.ui.theme.DuoRed
import com.example.ui.theme.DuoRedLight
import com.example.ui.theme.DuoYellow
import com.example.ui.theme.LocalAppStrings
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Represents the visual indicator state (Correct vs. Incorrect) and motivational tier
 * for a user's pronunciation attempt.
 */
enum class PronunciationVisualIndicator(
    val isCorrect: Boolean
) {
    CORRECT(isCorrect = true),
    INCORRECT(isCorrect = false);

    companion object {
        fun fromResult(result: PronunciationResult): PronunciationVisualIndicator {
            return if (result.isPassed && result.scorePercentage >= 60) {
                CORRECT
            } else {
                INCORRECT
            }
        }
    }
}

/**
 * Localized copy for the pronunciation accuracy & motivational feedback UI component.
 */
data class MotivationalFeedbackCopy(
    val statusBadgeText: String,
    val correctIndicatorLabel: String,
    val incorrectIndicatorLabel: String,
    val accuracyLabel: String,
    val fluencyLabel: String,
    val completenessLabel: String,
    val motivationalHeadline: String,
    val motivationalEncouragement: String,
    val wordBreakdownHeader: String,
    val replayAnimationLabel: String,
    val listenNativeLabel: String,
    val retryAttemptLabel: String
)

fun buildMotivationalFeedbackCopy(
    result: PronunciationResult,
    spokenLanguage: SpokenLanguage
): MotivationalFeedbackCopy {
    val score = result.scorePercentage.coerceIn(0, 100)
    val passed = result.isPassed && score >= 60

    return when (spokenLanguage) {
        SpokenLanguage.ENGLISH -> {
            val headline = when {
                score >= 92 -> "Outstanding Pronunciation! 🌟"
                score >= 80 -> "Great Job! Natural Accent 🔥"
                passed -> "Well Done! Keep the Momentum 🎯"
                score >= 45 -> "So Close! Don't Give Up 💪"
                else -> "Keep Practicing! Every Try Counts 🚀"
            }
            val subtext = when {
                score >= 92 -> "You nailed the rhythm and phonetics with ${score}% accuracy!"
                score >= 80 -> "Strong clarity (${score}% accuracy). You're sounding more fluent every day!"
                passed -> "You passed with ${score}% accuracy! Polish the highlighted words for 100%."
                else -> "You scored ${score}%. Listen to the native speaker and try speaking clearly again!"
            }
            MotivationalFeedbackCopy(
                statusBadgeText = if (passed) "✓ CORRECT" else "✕ INCORRECT",
                correctIndicatorLabel = "Correct (${result.correctWordsCount + result.almostCorrectWordsCount})",
                incorrectIndicatorLabel = "Incorrect (${result.incorrectWordsCount})",
                accuracyLabel = "Accuracy Score",
                fluencyLabel = "Fluency",
                completenessLabel = "Words Matched",
                motivationalHeadline = headline,
                motivationalEncouragement = subtext,
                wordBreakdownHeader = "WORD-BY-WORD VISUAL INDICATORS",
                replayAnimationLabel = "Animate",
                listenNativeLabel = "Listen",
                retryAttemptLabel = "Try Again"
            )
        }
        else -> {
            val headline = when {
                score >= 92 -> "¡Pronunciación Impecable! 🌟"
                score >= 80 -> "¡Excelente Trabajo! Suenas muy natural 🔥"
                passed -> "¡Bien Hecho! Sigue con esa racha 🎯"
                score >= 45 -> "¡Casi lo logras! No te rindas 💪"
                else -> "¡Sigue practicando! Cada intento suma 🚀"
            }
            val subtext = when {
                score >= 92 -> "¡Dominaste la fonética con un $score% de precisión!"
                score >= 80 -> "Gran claridad ($score% de precisión). ¡Tu fluidez avanza rápido!"
                passed -> "Superaste el reto con $score% de precisión. ¡Afina las palabras marcadas para llegar al 100%!"
                else -> "Obtuviste $score% de precisión. ¡Escucha el audio nativo y vuelve a intentarlo con confianza!"
            }
            MotivationalFeedbackCopy(
                statusBadgeText = if (passed) "✓ CORRECTO" else "✕ INCORRECTO",
                correctIndicatorLabel = "Correctas (${result.correctWordsCount + result.almostCorrectWordsCount})",
                incorrectIndicatorLabel = "Incorrectas (${result.incorrectWordsCount})",
                accuracyLabel = "Puntuación de Precisión",
                fluencyLabel = "Fluidez",
                completenessLabel = "Palabras",
                motivationalHeadline = headline,
                motivationalEncouragement = subtext,
                wordBreakdownHeader = "INDICADORES VISUALES POR PALABRA (✓ / ✕)",
                replayAnimationLabel = "Animar",
                listenNativeLabel = "Escuchar",
                retryAttemptLabel = "Reintentar"
            )
        }
    }
}

/**
 * Feedback UI component that displays accuracy scores for user pronunciation attempts
 * with clear visual indicators (correct / incorrect) and motivational animations
 * (celebratory star burst & trophy spring bounce on correct attempts; damped shake &
 * pulsing acoustic wave coach on incorrect attempts).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PronunciationAccuracyMotivationalFeedback(
    result: PronunciationResult,
    modifier: Modifier = Modifier,
    onWordClick: ((String) -> Unit)? = null,
    onListenTargetClick: (() -> Unit)? = null,
    onRetryClick: (() -> Unit)? = null,
    onReplayMotivationalAnimation: (() -> Unit)? = null
) {
    val strings = LocalAppStrings.current
    val spokenLanguage = strings.spokenLanguage
    val visualIndicator = remember(result) { PronunciationVisualIndicator.fromResult(result) }
    val isCorrectAttempt = visualIndicator.isCorrect
    val copy = remember(result, spokenLanguage) { buildMotivationalFeedbackCopy(result, spokenLanguage) }

    var animationTriggerCount by remember(result) { mutableIntStateOf(0) }

    val primaryAccent = when {
        result.feedbackStatus == PronunciationFeedbackStatus.CORRECT -> DuoGreen
        isCorrectAttempt -> DuoGreen
        result.scorePercentage >= 45 -> DuoOrange
        else -> DuoRed
    }

    val containerBg = when {
        isCorrectAttempt -> DuoGreenLight
        result.scorePercentage >= 45 -> Color(0xFFFFF4D6)
        else -> DuoRedLight
    }

    // Animated score counter & radial gauge progress
    val animatedScoreInt by animateIntAsState(
        targetValue = result.scorePercentage.coerceIn(0, 100),
        animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing),
        label = "motivational_score_counter"
    )
    val animatedGaugeFraction by animateFloatAsState(
        targetValue = (result.scorePercentage.coerceIn(0, 100) / 100f),
        animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing),
        label = "motivational_gauge_fraction"
    )

    // Keyed physics animations triggered on new result or manual replay
    val emblemScale = remember(result, animationTriggerCount) { Animatable(0.45f) }
    val burstProgress = remember(result, animationTriggerCount) { Animatable(0f) }
    val shakeOffsetPx = remember(result, animationTriggerCount) { Animatable(0f) }

    LaunchedEffect(result, animationTriggerCount) {
        coroutineScope {
            launch {
                emblemScale.snapTo(0.45f)
                emblemScale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
            }
            launch {
                burstProgress.snapTo(0f)
                burstProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 1250, easing = FastOutSlowInEasing)
                )
            }
            if (!isCorrectAttempt) {
                launch {
                    shakeOffsetPx.snapTo(0f)
                    val keyframes = listOf(-14f, 14f, -10f, 10f, -5f, 5f, 0f)
                    for (target in keyframes) {
                        shakeOffsetPx.animateTo(
                            targetValue = target,
                            animationSpec = tween(durationMillis = 55, easing = LinearEasing)
                        )
                    }
                }
            } else {
                shakeOffsetPx.snapTo(0f)
            }
        }
    }

    // Continuous subtle motivational pulse and rotation
    val infiniteTransition = rememberInfiniteTransition(label = "motivational_feedback_infinite")
    val continuousPulse by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "continuous_pulse"
    )
    val orbitAngleDeg by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit_angle_deg"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .offset { IntOffset(shakeOffsetPx.value.roundToInt(), 0) }
            .testTag("pronunciation_motivational_feedback_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        border = androidx.compose.foundation.BorderStroke(2.5.dp, primaryAccent)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 1. Top Motivational Header with Animated Emblem, Accuracy Score & Correct/Incorrect Badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                containerBg,
                                containerBg.copy(alpha = 0.35f)
                            )
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Status Badge Row (Correct / Incorrect Indicator + XP Reward + Replay Button)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Explicit Visual Indicator Badge (✓ CORRECT / ✕ INCORRECT)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isCorrectAttempt) DuoGreen else DuoRed,
                                shadowElevation = 2.dp,
                                modifier = Modifier.testTag("motivational_visual_indicator_badge")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isCorrectAttempt) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                        contentDescription = copy.statusBadgeText,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = copy.statusBadgeText,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                        letterSpacing = 0.6.sp
                                    )
                                }
                            }

                            if (result.xpEarned > 0 && isCorrectAttempt) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFFFF3CD),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, DuoOrange),
                                    modifier = Modifier
                                        .scale(continuousPulse)
                                        .testTag("motivational_xp_reward_badge")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.EmojiEvents,
                                            contentDescription = null,
                                            tint = DuoOrange,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = "+${result.xpEarned} XP",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFFB78103)
                                        )
                                    }
                                }
                            }
                        }

                        // Replay Motivational Animation Button (48dp minimum touch target)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.9f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, primaryAccent.copy(alpha = 0.45f)),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .clickable {
                                    animationTriggerCount++
                                    onReplayMotivationalAnimation?.invoke()
                                }
                                .testTag("motivational_animation_replay_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = copy.replayAnimationLabel,
                                    tint = primaryAccent,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = copy.replayAnimationLabel,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = primaryAccent
                                )
                            }
                        }
                    }

                    // Main Hero Row: Motivational Animated Canvas Emblem + Accuracy Score Gauge + Headline
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Custom Animated Motivational Emblem & Accuracy Gauge
                        MotivationalAnimatedAccuracyEmblem(
                            scorePercentage = animatedScoreInt,
                            gaugeFraction = animatedGaugeFraction,
                            isCorrectAttempt = isCorrectAttempt,
                            accentColor = primaryAccent,
                            emblemScale = emblemScale.value * continuousPulse,
                            burstProgress = burstProgress.value,
                            orbitAngleDeg = orbitAngleDeg
                        )

                        // Motivational Headline & Encouragement Banner
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .testTag("motivational_message_banner"),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = copy.motivationalHeadline,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF1F2937),
                                modifier = Modifier.testTag("motivational_headline_text")
                            )
                            Text(
                                text = copy.motivationalEncouragement,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF374151),
                                lineHeight = 16.sp,
                                modifier = Modifier.testTag("motivational_encouragement_text")
                            )
                        }
                    }

                    // Binary Correct / Incorrect Visual Indicator Strip
                    CorrectIncorrectVisualIndicatorStrip(
                        isCorrectAttempt = isCorrectAttempt,
                        correctLabel = copy.correctIndicatorLabel,
                        incorrectLabel = copy.incorrectIndicatorLabel
                    )
                }
            }

            // 2. Accuracy Sub-Scores & Word-by-Word Correct/Incorrect Indicators
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Accuracy Sub-Metric Bars (Accuracy / Fluency / Completeness)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("motivational_submetrics_row"),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MotivationalMiniScoreBar(
                        label = copy.accuracyLabel,
                        percentage = result.scorePercentage,
                        color = primaryAccent,
                        testTag = "motivational_metric_accuracy",
                        modifier = Modifier.weight(1f)
                    )
                    MotivationalMiniScoreBar(
                        label = copy.fluencyLabel,
                        percentage = result.fluencyScore,
                        color = DuoBlue,
                        testTag = "motivational_metric_fluency",
                        modifier = Modifier.weight(1f)
                    )
                    MotivationalMiniScoreBar(
                        label = copy.completenessLabel,
                        percentage = result.completenessScore,
                        color = DuoPurple,
                        testTag = "motivational_metric_completeness",
                        modifier = Modifier.weight(1f)
                    )
                }

                // Word-by-Word Correct (✓) / Incorrect (✕) Visual Indicators
                if (result.wordAccuracies.isNotEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = copy.wordBreakdownHeader,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF4B5563),
                            letterSpacing = 0.6.sp
                        )

                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("motivational_word_indicators_row"),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            result.wordAccuracies.forEachIndexed { index, wordAcc ->
                                AnimatedVisibility(
                                    visible = true,
                                    enter = fadeIn(tween(220, delayMillis = index * 45)) +
                                        scaleIn(initialScale = 0.85f, animationSpec = spring()) +
                                        expandVertically()
                                ) {
                                    WordCorrectIncorrectIndicatorChip(
                                        wordAccuracy = wordAcc,
                                        index = index,
                                        onClick = { onWordClick?.invoke(wordAcc.targetWord) }
                                    )
                                }
                            }
                        }
                    }
                }

                // Optional Quick Action Row (Listen Native & Retry)
                if (onListenTargetClick != null || onRetryClick != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (onListenTargetClick != null) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF0F7FF),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DuoBlue.copy(alpha = 0.45f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .clickable { onListenTargetClick() }
                                    .testTag("motivational_listen_native_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VolumeUp,
                                        contentDescription = copy.listenNativeLabel,
                                        tint = DuoBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = copy.listenNativeLabel,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = DuoBlue
                                    )
                                }
                            }
                        }

                        if (onRetryClick != null) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = primaryAccent.copy(alpha = 0.12f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, primaryAccent.copy(alpha = 0.55f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .clickable { onRetryClick() }
                                    .testTag("motivational_retry_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = copy.retryAttemptLabel,
                                        tint = primaryAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = copy.retryAttemptLabel,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = primaryAccent
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Animated radial gauge + motivational particle/starburst canvas showing the user's accuracy percentage
 * and visual correct/incorrect emblem.
 */
@Composable
private fun MotivationalAnimatedAccuracyEmblem(
    scorePercentage: Int,
    gaugeFraction: Float,
    isCorrectAttempt: Boolean,
    accentColor: Color,
    emblemScale: Float,
    burstProgress: Float,
    orbitAngleDeg: Float
) {
    Box(
        modifier = Modifier
            .size(92.dp)
            .scale(emblemScale)
            .testTag("motivational_animation_canvas"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxR = size.minDimension / 2f

            // 1. Expanding motivational shockwave / acoustic ring
            if (burstProgress in 0.01f..0.99f) {
                val ringAlpha = (1f - burstProgress).coerceIn(0f, 1f)
                drawCircle(
                    color = accentColor.copy(alpha = ringAlpha * 0.45f),
                    radius = maxR * (0.68f + 0.32f * burstProgress),
                    center = center,
                    style = Stroke(width = 3.dp.toPx())
                )
            }

            // 2. Orbiting motivational stars (for Correct) or pulsing target rays (for Incorrect)
            if (isCorrectAttempt) {
                val starCount = 6
                for (i in 0 until starCount) {
                    val angleRad = ((i * (360f / starCount)) + orbitAngleDeg) * (PI.toFloat() / 180f)
                    val orbitR = maxR * (0.84f + 0.10f * sin(burstProgress * PI.toFloat()))
                    val sx = center.x + cos(angleRad) * orbitR
                    val sy = center.y + sin(angleRad) * orbitR
                    withTransform({
                        translate(left = sx, top = sy)
                        rotate(degrees = orbitAngleDeg * 1.5f, pivot = Offset.Zero)
                    }) {
                        val r = 4.dp.toPx()
                        val starPath = Path().apply {
                            moveTo(0f, -r)
                            lineTo(r * 0.35f, -r * 0.35f)
                            lineTo(r, 0f)
                            lineTo(r * 0.35f, r * 0.35f)
                            lineTo(0f, r)
                            lineTo(-r * 0.35f, r * 0.35f)
                            lineTo(-r, 0f)
                            lineTo(-r * 0.35f, -r * 0.35f)
                            close()
                        }
                        drawPath(
                            path = starPath,
                            color = if (i % 2 == 0) DuoYellow else DuoGreen
                        )
                    }
                }
            } else {
                // Subtle coaching wave arcs for incorrect attempts
                val waveCount = 4
                for (i in 0 until waveCount) {
                    val startAngle = i * 90f + orbitAngleDeg * 0.35f
                    drawArc(
                        color = accentColor.copy(alpha = 0.28f),
                        startAngle = startAngle,
                        sweepAngle = 40f,
                        useCenter = false,
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            // 3. Inner white circular badge background + radial accuracy progress arc
            val gaugeRadius = maxR * 0.72f
            drawCircle(
                color = Color.White,
                radius = gaugeRadius,
                center = center
            )

            val strokeWidth = 6.5.dp.toPx()
            val arcTopLeft = Offset(center.x - gaugeRadius, center.y - gaugeRadius)
            val arcSize = androidx.compose.ui.geometry.Size(gaugeRadius * 2f, gaugeRadius * 2f)

            drawArc(
                color = accentColor.copy(alpha = 0.18f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            drawArc(
                color = accentColor,
                startAngle = -90f,
                sweepAngle = gaugeFraction * 360f,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // 4. Mini corner indicator badge (✓ or ✕) drawn in bottom-right of gauge
            val badgeCenter = Offset(center.x + gaugeRadius * 0.68f, center.y + gaugeRadius * 0.68f)
            val badgeR = 9.dp.toPx()
            drawCircle(
                color = Color.White,
                radius = badgeR + 1.5.dp.toPx(),
                center = badgeCenter
            )
            drawCircle(
                color = if (isCorrectAttempt) DuoGreen else DuoRed,
                radius = badgeR,
                center = badgeCenter
            )
            if (isCorrectAttempt) {
                val checkPath = Path().apply {
                    moveTo(badgeCenter.x - badgeR * 0.42f, badgeCenter.y)
                    lineTo(badgeCenter.x - badgeR * 0.1f, badgeCenter.y + badgeR * 0.34f)
                    lineTo(badgeCenter.x + badgeR * 0.46f, badgeCenter.y - badgeR * 0.32f)
                }
                drawPath(
                    path = checkPath,
                    color = Color.White,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            } else {
                val d = badgeR * 0.34f
                drawLine(
                    color = Color.White,
                    start = Offset(badgeCenter.x - d, badgeCenter.y - d),
                    end = Offset(badgeCenter.x + d, badgeCenter.y + d),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = Color.White,
                    start = Offset(badgeCenter.x + d, badgeCenter.y - d),
                    end = Offset(badgeCenter.x - d, badgeCenter.y + d),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${scorePercentage.coerceIn(0, 100)}%",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = accentColor,
                modifier = Modifier.testTag("motivational_accuracy_score_text")
            )
            Text(
                text = if (isCorrectAttempt) "PASS" else "RETRY",
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = if (isCorrectAttempt) DuoGreenDark else DuoRed,
                letterSpacing = 0.6.sp
            )
        }
    }
}

/**
 * Dual-pill visual indicator clearly contrasting Correct vs. Incorrect status.
 */
@Composable
private fun CorrectIncorrectVisualIndicatorStrip(
    isCorrectAttempt: Boolean,
    correctLabel: String,
    incorrectLabel: String
) {
    val correctBg by animateColorAsState(
        targetValue = if (isCorrectAttempt) DuoGreen else Color(0xFFF3F4F6),
        animationSpec = tween(250),
        label = "correct_indicator_bg"
    )
    val correctText by animateColorAsState(
        targetValue = if (isCorrectAttempt) Color.White else Color(0xFF4B5563),
        animationSpec = tween(250),
        label = "correct_indicator_text"
    )
    val incorrectBg by animateColorAsState(
        targetValue = if (!isCorrectAttempt) DuoRed else Color(0xFFF3F4F6),
        animationSpec = tween(250),
        label = "incorrect_indicator_bg"
    )
    val incorrectText by animateColorAsState(
        targetValue = if (!isCorrectAttempt) Color.White else Color(0xFF4B5563),
        animationSpec = tween(250),
        label = "incorrect_indicator_text"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.92f))
            .border(1.dp, DuoCardBorder, RoundedCornerShape(14.dp))
            .padding(5.dp)
            .testTag("motivational_correct_incorrect_strip"),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Correct Visual Indicator Pill
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(correctBg)
                .padding(horizontal = 8.dp, vertical = 7.dp)
                .testTag("motivational_indicator_correct"),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = correctText,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = correctLabel,
                fontSize = 11.sp,
                fontWeight = if (isCorrectAttempt) FontWeight.Black else FontWeight.SemiBold,
                color = correctText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Incorrect Visual Indicator Pill
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(incorrectBg)
                .padding(horizontal = 8.dp, vertical = 7.dp)
                .testTag("motivational_indicator_incorrect"),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Cancel,
                contentDescription = null,
                tint = incorrectText,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = incorrectLabel,
                fontSize = 11.sp,
                fontWeight = if (!isCorrectAttempt) FontWeight.Black else FontWeight.SemiBold,
                color = incorrectText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun MotivationalMiniScoreBar(
    label: String,
    percentage: Int,
    color: Color,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val clamped = percentage.coerceIn(0, 100)
    val animatedProgress by animateFloatAsState(
        targetValue = clamped / 100f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "${testTag}_progress"
    )
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF8FAFC),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.35f)),
        modifier = modifier.testTag(testTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4B5563),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "$clamped%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = color
                )
            }
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = color,
                trackColor = DuoCardBorder.copy(alpha = 0.4f)
            )
        }
    }
}

/**
 * Word-level visual indicator chip displaying the word, its Correct (✓) / Incorrect (✕) icon,
 * and its pronunciation accuracy percentage.
 */
@Composable
private fun WordCorrectIncorrectIndicatorChip(
    wordAccuracy: WordAccuracy,
    index: Int,
    onClick: () -> Unit
) {
    val isWordCorrect = wordAccuracy.isCorrect || wordAccuracy.isPartiallyCorrect
    val chipAccent = when {
        wordAccuracy.isCorrect -> DuoGreen
        wordAccuracy.isPartiallyCorrect -> DuoOrange
        else -> DuoRed
    }
    val chipBg = when {
        wordAccuracy.isCorrect -> DuoGreenLight.copy(alpha = 0.65f)
        wordAccuracy.isPartiallyCorrect -> Color(0xFFFFF4D6)
        else -> DuoRedLight.copy(alpha = 0.65f)
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = chipBg,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, chipAccent),
        modifier = Modifier
            .clickable { onClick() }
            .testTag("motivational_word_indicator_$index")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = when {
                    wordAccuracy.isCorrect -> Icons.Default.CheckCircle
                    wordAccuracy.isPartiallyCorrect -> Icons.Default.GraphicEq
                    else -> Icons.Default.Cancel
                },
                contentDescription = if (isWordCorrect) "Correct" else "Incorrect",
                tint = chipAccent,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = wordAccuracy.targetWord,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF1F2937)
            )
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = chipAccent
            ) {
                Text(
                    text = "${wordAccuracy.similarityPercentage}%",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                )
            }
        }
    }
}
