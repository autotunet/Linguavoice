package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PronunciationFeedbackStatus
import com.example.data.model.PronunciationResult
import com.example.data.model.WordAccuracy
import com.example.ui.theme.DuoBlue
import com.example.ui.theme.DuoCardBorder
import com.example.ui.theme.DuoGreen
import com.example.ui.theme.DuoGreenLight
import com.example.ui.theme.DuoOrange
import com.example.ui.theme.DuoRed
import com.example.ui.theme.DuoRedLight

/**
 * Maps a [PronunciationFeedbackStatus] to its primary accent color.
 */
fun pronunciationStatusAccentColor(status: PronunciationFeedbackStatus): Color {
    return when (status) {
        PronunciationFeedbackStatus.CORRECT -> DuoGreen
        PronunciationFeedbackStatus.ALMOST_CORRECT -> DuoOrange
        PronunciationFeedbackStatus.INCORRECT -> DuoRed
    }
}

/**
 * Maps a [PronunciationFeedbackStatus] to its soft container background color.
 */
fun pronunciationStatusContainerColor(status: PronunciationFeedbackStatus): Color {
    return when (status) {
        PronunciationFeedbackStatus.CORRECT -> DuoGreenLight
        PronunciationFeedbackStatus.ALMOST_CORRECT -> Color(0xFFFFF4D6)
        PronunciationFeedbackStatus.INCORRECT -> DuoRedLight
    }
}

/**
 * Compact inline visual feedback banner displayed right after the user records their voice,
 * clearly indicating whether the pronunciation was Correcta, Casi correcta, or Incorrecta.
 */
@Composable
fun VoicePronunciationStatusBanner(
    result: PronunciationResult,
    modifier: Modifier = Modifier,
    onListenTargetClick: (() -> Unit)? = null,
    onRetryClick: (() -> Unit)? = null
) {
    val status = result.feedbackStatus
    val accentColor = pronunciationStatusAccentColor(status)
    val containerColor = pronunciationStatusContainerColor(status)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("voice_pronunciation_status_banner"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = androidx.compose.foundation.BorderStroke(2.dp, accentColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    AnimatedPronunciationScoreRing(
                        scorePercentage = result.scorePercentage,
                        status = status,
                        sizeDp = 50
                    )

                    Column {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = accentColor,
                            modifier = Modifier.testTag("inline_feedback_status_badge")
                        ) {
                            Text(
                                text = status.badgeLabelEs,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 0.6.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = status.titleEs,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1F2937)
                        )
                        Text(
                            text = status.guidanceEs,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF4B5563)
                        )
                    }
                }

                if (onListenTargetClick != null || onRetryClick != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (onListenTargetClick != null) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White,
                                border = androidx.compose.foundation.BorderStroke(1.dp, DuoBlue.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .size(38.dp)
                                    .clickable { onListenTargetClick() }
                                    .testTag("inline_listen_native_button")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.VolumeUp,
                                        contentDescription = "Escuchar pronunciación correcta",
                                        tint = DuoBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                        if (onRetryClick != null) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White,
                                border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .size(38.dp)
                                    .clickable { onRetryClick() }
                                    .testTag("inline_retry_voice_button")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Grabar voz de nuevo",
                                        tint = accentColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3-State Visual Semáforo Bar (Correcta | Casi correcta | Incorrecta)
            PronunciationTriStateIndicatorBar(
                activeStatus = status,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * 3-Segment visual indicator ("Semáforo de Pronunciación") showing the 3 tiers:
 * Correcta (Green), Casi correcta (Amber), and Incorrecta (Red), highlighting the active state.
 */
@Composable
fun PronunciationTriStateIndicatorBar(
    activeStatus: PronunciationFeedbackStatus,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.9f))
            .border(1.dp, DuoCardBorder, RoundedCornerShape(14.dp))
            .padding(6.dp)
            .testTag("pronunciation_tri_state_bar"),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PronunciationStatusSegmentPill(
            status = PronunciationFeedbackStatus.CORRECT,
            isActive = activeStatus == PronunciationFeedbackStatus.CORRECT,
            modifier = Modifier.weight(1f),
            testTag = "status_pill_correct"
        )
        PronunciationStatusSegmentPill(
            status = PronunciationFeedbackStatus.ALMOST_CORRECT,
            isActive = activeStatus == PronunciationFeedbackStatus.ALMOST_CORRECT,
            modifier = Modifier.weight(1f),
            testTag = "status_pill_almost_correct"
        )
        PronunciationStatusSegmentPill(
            status = PronunciationFeedbackStatus.INCORRECT,
            isActive = activeStatus == PronunciationFeedbackStatus.INCORRECT,
            modifier = Modifier.weight(1f),
            testTag = "status_pill_incorrect"
        )
    }
}

@Composable
private fun PronunciationStatusSegmentPill(
    status: PronunciationFeedbackStatus,
    isActive: Boolean,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val statusColor = pronunciationStatusAccentColor(status)
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.03f else 0.96f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "status_pill_scale"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isActive) statusColor else Color(0xFFF3F4F6),
        animationSpec = tween(250),
        label = "status_pill_bg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isActive) Color.White else Color(0xFF6B7280),
        animationSpec = tween(250),
        label = "status_pill_content"
    )

    val icon = when (status) {
        PronunciationFeedbackStatus.CORRECT -> Icons.Default.CheckCircle
        PronunciationFeedbackStatus.ALMOST_CORRECT -> Icons.Default.GraphicEq
        PronunciationFeedbackStatus.INCORRECT -> Icons.Default.Cancel
    }

    Row(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(
                width = if (isActive) 1.5.dp else 1.dp,
                color = if (isActive) statusColor else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 6.dp, vertical = 7.dp)
            .testTag(testTag),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = status.shortLabelEs,
            tint = contentColor,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = status.shortLabelEs,
            fontSize = 11.sp,
            fontWeight = if (isActive) FontWeight.Black else FontWeight.SemiBold,
            color = contentColor,
            maxLines = 1
        )
    }
}

/**
 * Animated radial score ring showing the exact pronunciation percentage and status color.
 */
@Composable
fun AnimatedPronunciationScoreRing(
    scorePercentage: Int,
    status: PronunciationFeedbackStatus,
    sizeDp: Int = 56,
    modifier: Modifier = Modifier
) {
    val accentColor = pronunciationStatusAccentColor(status)
    val animatedSweep by animateFloatAsState(
        targetValue = (scorePercentage.coerceIn(0, 100) / 100f) * 360f,
        animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing),
        label = "score_ring_sweep"
    )

    Box(
        modifier = modifier
            .size(sizeDp.dp)
            .testTag("pronunciation_score_ring"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(sizeDp.dp)) {
            val strokeWidth = 6.dp.toPx()
            drawArc(
                color = accentColor.copy(alpha = 0.2f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            drawArc(
                color = accentColor,
                startAngle = -90f,
                sweepAngle = animatedSweep,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        Text(
            text = "${scorePercentage.coerceIn(0, 100)}%",
            fontSize = if (sizeDp >= 54) 13.sp else 11.sp,
            fontWeight = FontWeight.Black,
            color = accentColor
        )
    }
}

/**
 * Complete visual feedback component showing the 3-tier pronunciation result
 * (Correcta, Casi correcta, Incorrecta), animated accuracy meters, word-level breakdown,
 * and coaching actions after the user records their voice.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PronunciationFeedbackCard(
    result: PronunciationResult,
    modifier: Modifier = Modifier,
    onWordClick: ((String) -> Unit)? = null,
    onRetry: (() -> Unit)? = null,
    onContinue: (() -> Unit)? = null,
    remainingLives: Int? = null,
    maxLives: Int = 5,
    onReplayFeedbackSound: (() -> Unit)? = null
) {
    var selectedWordAcc by remember(result) { mutableStateOf<WordAccuracy?>(null) }
    val status = result.feedbackStatus

    val animatedOverallProgress by animateFloatAsState(
        targetValue = (result.scorePercentage / 100f).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "overall_progress"
    )

    val animatedFluencyProgress by animateFloatAsState(
        targetValue = (result.fluencyScore / 100f).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800, delayMillis = 150, easing = FastOutSlowInEasing),
        label = "fluency_progress"
    )

    val animatedCompletenessProgress by animateFloatAsState(
        targetValue = (result.completenessScore / 100f).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800, delayMillis = 300, easing = FastOutSlowInEasing),
        label = "completeness_progress"
    )

    val accentColor = pronunciationStatusAccentColor(status)
    val headerBgColor = pronunciationStatusContainerColor(status)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("pronunciation_feedback_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, accentColor.copy(alpha = 0.55f))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header: 3-Tier Visual Status Banner (Correcta / Casi correcta / Incorrecta)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerBgColor)
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(accentColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (status) {
                                    PronunciationFeedbackStatus.CORRECT -> Icons.Default.AutoAwesome
                                    PronunciationFeedbackStatus.ALMOST_CORRECT -> Icons.Default.RecordVoiceOver
                                    PronunciationFeedbackStatus.INCORRECT -> Icons.Default.Hearing
                                },
                                contentDescription = status.titleEs,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = accentColor,
                                modifier = Modifier.testTag("pronunciation_status_tier_badge")
                            ) {
                                Text(
                                    text = status.badgeLabelEs,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    letterSpacing = 0.7.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = status.titleEs,
                                fontWeight = FontWeight.Black,
                                fontSize = 17.sp,
                                color = Color(0xFF1F2937),
                                modifier = Modifier.testTag("pronunciation_status_title")
                            )
                            Text(
                                text = result.feedbackMessage,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Animated Circular Score Ring + Badge
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        shadowElevation = 2.dp,
                        modifier = Modifier.testTag("accuracy_score_badge")
                    ) {
                        Box(modifier = Modifier.padding(6.dp)) {
                            AnimatedPronunciationScoreRing(
                                scorePercentage = result.scorePercentage,
                                status = status,
                                sizeDp = 52
                            )
                        }
                    }
                }

                // 3-State Visual Semáforo Bar (Correcta | Casi correcta | Incorrecta)
                PronunciationTriStateIndicatorBar(
                    activeStatus = status,
                    modifier = Modifier.fillMaxWidth()
                )

                // Audio Feedback Indicator (Success Chime vs Subtle Error Sound)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.88f))
                        .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                        .clickable(enabled = onReplayFeedbackSound != null) {
                            onReplayFeedbackSound?.invoke()
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("audio_feedback_indicator"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Audio feedback",
                            tint = accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = when (status) {
                                PronunciationFeedbackStatus.CORRECT ->
                                    "✨ Pronunciación correcta • Sonido de éxito reproducido"
                                PronunciationFeedbackStatus.ALMOST_CORRECT ->
                                    if (result.isPassed) "🎯 Casi correcta • Ejercicio superado"
                                    else "🎯 Casi correcta • Afina un poco más para superar"
                                PronunciationFeedbackStatus.INCORRECT ->
                                    "🎵 Pronunciación incorrecta • Escucha e inténtalo de nuevo"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    }
                    if (onReplayFeedbackSound != null) {
                        Text(
                            text = "Repetir",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = DuoBlue,
                            modifier = Modifier.testTag("replay_feedback_sound_button")
                        )
                    }
                }

                // Life Lost Indicator when pronunciation failed
                if (!result.isPassed && remainingLives != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.88f))
                            .border(1.dp, DuoRed.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("life_lost_feedback_banner"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "💔 -1 vida por error de pronunciación",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = DuoRed
                        )
                        Text(
                            text = "Vidas: $remainingLives/$maxLives",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = DuoRed
                        )
                    }
                }
            }

            // Scrollable Body + Sticky Action Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Word Count Breakdown Summary (Correctas / Casi correctas / Incorrectas)
                WordTierSummaryRow(result = result)

                // Section 1: Color-Coded Text Highlighting
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "DIAGNÓSTICO PALABRA POR PALABRA (TOCA PARA OÍR)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = Color.Gray
                    )

                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .padding(12.dp)
                            .testTag("color_coded_words_flow"),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        result.wordAccuracies.forEachIndexed { index, wordAcc ->
                            ColorCodedWordChip(
                                wordAccuracy = wordAcc,
                                isSelected = selectedWordAcc == wordAcc,
                                onClick = {
                                    selectedWordAcc = if (selectedWordAcc == wordAcc) null else wordAcc
                                    onWordClick?.invoke(wordAcc.targetWord)
                                },
                                testTag = "word_chip_$index"
                            )
                        }
                    }

                    // Word detail card if a word chip is tapped
                    AnimatedVisibility(
                        visible = selectedWordAcc != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        selectedWordAcc?.let { wordAcc ->
                            WordDetailFeedbackCard(
                                wordAccuracy = wordAcc,
                                onPlayWord = { onWordClick?.invoke(wordAcc.targetWord) },
                                onClose = { selectedWordAcc = null }
                            )
                        }
                    }

                    // Color Legend
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LegendItem(color = DuoGreen, label = "Correcta (≥85%)")
                        LegendItem(color = DuoOrange, label = "Casi correcta (60-84%)")
                        LegendItem(color = DuoRed, label = "Incorrecta (<60%)")
                    }
                }

                // Section 2: Progress Bars for Accuracy and Sub-metrics
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "MÉTRICAS DE VOZ Y PRECISIÓN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = Color.Gray
                    )

                    AccuracyProgressBar(
                        label = "Precisión General (${status.shortLabelEs})",
                        percentage = result.scorePercentage,
                        animatedFraction = animatedOverallProgress,
                        color = accentColor,
                        testTag = "overall_accuracy_progress_bar"
                    )

                    AccuracyProgressBar(
                        label = "Fluidez y Ritmo",
                        percentage = result.fluencyScore,
                        animatedFraction = animatedFluencyProgress,
                        color = DuoBlue,
                        testTag = "fluency_progress_bar"
                    )

                    AccuracyProgressBar(
                        label = "Cobertura de Palabras",
                        percentage = result.completenessScore,
                        animatedFraction = animatedCompletenessProgress,
                        color = if (result.completenessScore >= 75) DuoGreen else DuoOrange,
                        testTag = "completeness_progress_bar"
                    )
                }

                // Section 3: SpeechRecognizer Captured Text vs Target Phrase Comparison
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFF3F7FA))
                        .border(1.dp, DuoBlue.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                        .testTag("recognized_speech_comparison")
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "COMPARACIÓN DE ENTRADA VS. OBJETIVO",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = DuoBlue,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Score / Accuracy: ${result.scorePercentage}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = accentColor,
                                modifier = Modifier.testTag("sheet_comparison_score_label")
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Frase Objetivo (Target):",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            Text(
                                text = "\"${result.targetText}\"",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1F2937)
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(headerBgColor.copy(alpha = 0.55f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Tu voz grabada (Input):",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = accentColor
                                )
                            }
                            Text(
                                text = "\"${result.recognizedText.ifBlank { "—" }}\"",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF1F2937)
                            )
                        }
                    }
                }

                // Section 4: Coach Tip
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFFFF9E6))
                        .border(1.dp, Color(0xFFFFD54F), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Outlined.Lightbulb,
                            contentDescription = null,
                            tint = Color(0xFFF57F17),
                            modifier = Modifier
                                .size(20.dp)
                                .padding(end = 4.dp)
                        )
                        Column {
                            Text(
                                text = "Consejo del Entrenador",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFFE65100)
                            )
                            Text(
                                text = result.coachTip,
                                fontSize = 12.sp,
                                color = Color(0xFF5D4037),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }

            // Action buttons always visible at bottom of card
            if (onRetry != null || onContinue != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (onRetry != null && (!result.isPassed || status == PronunciationFeedbackStatus.ALMOST_CORRECT)) {
                        DuoButton(
                            text = if (status == PronunciationFeedbackStatus.ALMOST_CORRECT) "Afinar Voz" else "Reintentar",
                            onClick = onRetry,
                            modifier = Modifier.weight(1f),
                            colorStyle = DuoButtonColor.WHITE,
                            testTag = "feedback_retry_button"
                        )
                    }
                    if (onContinue != null) {
                        DuoButton(
                            text = if (result.isPassed) "¡Continuar!" else "Continuar",
                            onClick = onContinue,
                            modifier = Modifier.weight(if (result.isPassed) 1.2f else 1f),
                            colorStyle = when (status) {
                                PronunciationFeedbackStatus.CORRECT -> DuoButtonColor.GREEN
                                PronunciationFeedbackStatus.ALMOST_CORRECT ->
                                    if (result.isPassed) DuoButtonColor.GREEN else DuoButtonColor.ORANGE
                                PronunciationFeedbackStatus.INCORRECT -> DuoButtonColor.ORANGE
                            },
                            testTag = "feedback_continue_button"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WordTierSummaryRow(result: PronunciationResult) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("word_tier_summary_row"),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        WordTierCountPill(
            count = result.correctWordsCount,
            label = "Correctas",
            color = DuoGreen,
            bgColor = DuoGreenLight.copy(alpha = 0.6f),
            modifier = Modifier.weight(1f),
            testTag = "summary_correct_words_count"
        )
        WordTierCountPill(
            count = result.almostCorrectWordsCount,
            label = "Casi correctas",
            color = DuoOrange,
            bgColor = Color(0xFFFFF4D6),
            modifier = Modifier.weight(1f),
            testTag = "summary_almost_words_count"
        )
        WordTierCountPill(
            count = result.incorrectWordsCount,
            label = "Incorrectas",
            color = DuoRed,
            bgColor = DuoRedLight.copy(alpha = 0.6f),
            modifier = Modifier.weight(1f),
            testTag = "summary_incorrect_words_count"
        )
    }
}

@Composable
private fun WordTierCountPill(
    count: Int,
    label: String,
    color: Color,
    bgColor: Color,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(vertical = 8.dp, horizontal = 6.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$count",
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            color = color
        )
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF374151),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

/**
 * Reusable Accuracy Progress Bar with label, percentage, and animated indicator
 */
@Composable
fun AccuracyProgressBar(
    label: String,
    percentage: Int,
    animatedFraction: Float,
    color: Color,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$percentage%",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }

        LinearProgressIndicator(
            progress = { animatedFraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp)),
            color = color,
            trackColor = DuoCardBorder.copy(alpha = 0.5f)
        )
    }
}

/**
 * Individual color-coded word chip highlighting phonetic accuracy (Correcta, Casi correcta, Incorrecta)
 */
@Composable
fun ColorCodedWordChip(
    wordAccuracy: WordAccuracy,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val bg = when {
        wordAccuracy.isCorrect -> Color(0xFFD7FFB8)
        wordAccuracy.isPartiallyCorrect -> Color(0xFFFFF3CD)
        else -> Color(0xFFFFDFDF)
    }

    val border = when {
        wordAccuracy.isCorrect -> DuoGreen
        wordAccuracy.isPartiallyCorrect -> DuoOrange
        else -> DuoRed
    }

    val textColor = when {
        wordAccuracy.isCorrect -> Color(0xFF2E7D32)
        wordAccuracy.isPartiallyCorrect -> Color(0xFFB78103)
        else -> Color(0xFFC62828)
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (isSelected) 2.5.dp else 1.5.dp,
                color = if (isSelected) border else border.copy(alpha = 0.8f),
                shape = RoundedCornerShape(12.dp)
            )
            .background(if (isSelected) bg.copy(alpha = 0.95f) else bg)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 7.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = when {
                wordAccuracy.isCorrect -> Icons.Default.Check
                wordAccuracy.isPartiallyCorrect -> Icons.Default.GraphicEq
                else -> Icons.Default.Close
            },
            contentDescription = wordAccuracy.feedbackStatus.shortLabelEs,
            tint = textColor,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = wordAccuracy.targetWord,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = textColor
        )
    }
}

/**
 * Detailed feedback breakdown when a specific word chip is clicked
 */
@Composable
private fun WordDetailFeedbackCard(
    wordAccuracy: WordAccuracy,
    onPlayWord: () -> Unit,
    onClose: () -> Unit
) {
    val wordStatus = wordAccuracy.feedbackStatus
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("word_detail_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, DuoCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Palabra: \"${wordAccuracy.targetWord}\"",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = pronunciationStatusContainerColor(wordStatus)
                    ) {
                        Text(
                            text = "${wordStatus.shortLabelEs} (${wordAccuracy.similarityPercentage}%)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (wordStatus) {
                                PronunciationFeedbackStatus.CORRECT -> Color(0xFF2E7D32)
                                PronunciationFeedbackStatus.ALMOST_CORRECT -> Color(0xFFB78103)
                                PronunciationFeedbackStatus.INCORRECT -> Color(0xFFC62828)
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                if (!wordAccuracy.matchedSpokenWord.isNullOrBlank()) {
                    Text(
                        text = "Micrófono detectó: \"${wordAccuracy.matchedSpokenWord}\"",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                if (!wordAccuracy.tip.isNullOrBlank()) {
                    Text(
                        text = "Consejo: ${wordAccuracy.tip}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = DuoBlue,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Surface(
                    shape = CircleShape,
                    color = DuoBlue.copy(alpha = 0.15f),
                    modifier = Modifier
                        .size(36.dp)
                        .clickable { onPlayWord() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Escuchar palabra",
                            tint = DuoBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = Color.LightGray.copy(alpha = 0.3f),
                    modifier = Modifier
                        .size(36.dp)
                        .clickable { onClose() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color.DarkGray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray
        )
    }
}
