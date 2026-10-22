package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
 * Visual feedback component showing animated progress bars for pronunciation accuracy
 * and color-coded text highlighting based on SpeechRecognizer results.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PronunciationFeedbackCard(
    result: PronunciationResult,
    modifier: Modifier = Modifier,
    onWordClick: ((String) -> Unit)? = null,
    onRetry: (() -> Unit)? = null,
    onContinue: (() -> Unit)? = null
) {
    var selectedWordAcc by remember { mutableStateOf<WordAccuracy?>(null) }

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

    val accentColor = when {
        result.scorePercentage >= 80 -> DuoGreen
        result.scorePercentage >= 60 -> DuoOrange
        else -> DuoRed
    }

    val headerBgColor = when {
        result.scorePercentage >= 80 -> DuoGreenLight
        result.scorePercentage >= 60 -> Color(0xFFFFF8E1)
        else -> DuoRedLight
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("pronunciation_feedback_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, accentColor.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header: Overall Accuracy Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerBgColor)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(accentColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when {
                                    result.scorePercentage >= 80 -> Icons.Default.AutoAwesome
                                    result.scorePercentage >= 60 -> Icons.Default.RecordVoiceOver
                                    else -> Icons.Default.Hearing
                                },
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column {
                            Text(
                                text = when {
                                    result.scorePercentage >= 90 -> "¡Pronunciación Excelente!"
                                    result.scorePercentage >= 80 -> "¡Muy Buena Pronunciación!"
                                    result.scorePercentage >= 60 -> "Buen Intento, Sigue Así"
                                    else -> "Necesita Más Práctica"
                                },
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = accentColor
                            )
                            Text(
                                text = result.feedbackMessage,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Score Pill Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        shadowElevation = 2.dp,
                        modifier = Modifier.testTag("accuracy_score_badge")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${result.scorePercentage}%",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = accentColor
                            )
                        }
                    }
                }
            }

            // Body content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 1: Progress Bars for Accuracy and Sub-metrics
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "MÉTRICAS DE VOZ Y PRECISIÓN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = Color.Gray
                    )

                    // Primary Overall Accuracy Progress Bar
                    AccuracyProgressBar(
                        label = "Precisión General",
                        percentage = result.scorePercentage,
                        animatedFraction = animatedOverallProgress,
                        color = accentColor,
                        testTag = "overall_accuracy_progress_bar"
                    )

                    // Secondary Sub-metric: Fluency & Cadence
                    AccuracyProgressBar(
                        label = "Fluidez y Ritmo",
                        percentage = result.fluencyScore,
                        animatedFraction = animatedFluencyProgress,
                        color = DuoBlue,
                        testTag = "fluency_progress_bar"
                    )

                    // Secondary Sub-metric: Word Coverage
                    AccuracyProgressBar(
                        label = "Cobertura de Palabras",
                        percentage = result.completenessScore,
                        animatedFraction = animatedCompletenessProgress,
                        color = if (result.completenessScore >= 75) DuoGreen else DuoOrange,
                        testTag = "completeness_progress_bar"
                    )
                }

                // Section 2: Color-Coded Text Highlighting
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "RESALTADO POR PALABRA (TOCA PARA ESCUCHAR)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = Color.Gray
                        )
                    }

                    // Flow of color-coded word chips
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
                        LegendItem(color = DuoGreen, label = "Correcto (≥85%)")
                        LegendItem(color = DuoOrange, label = "Parcial (60-84%)")
                        LegendItem(color = DuoRed, label = "Revisar (<60%)")
                    }
                }

                // Section 3: SpeechRecognizer Captured Text vs Target
                if (result.recognizedText.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFF3F7FA))
                            .border(1.dp, DuoBlue.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                            .padding(12.dp)
                            .testTag("recognized_speech_comparison")
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = null,
                                    tint = DuoBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Captura de micrófono (SpeechRecognizer):",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DuoBlue
                                )
                            }
                            Text(
                                text = "\"${result.recognizedText}\"",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
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

                // Action buttons if provided
                if (onRetry != null || onContinue != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (onRetry != null && !result.isPassed) {
                            DuoButton(
                                text = "Reintentar",
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
                                modifier = Modifier.weight(if (result.isPassed) 1f else 1.2f),
                                colorStyle = if (result.isPassed) DuoButtonColor.GREEN else DuoButtonColor.ORANGE,
                                testTag = "feedback_continue_button"
                            )
                        }
                    }
                }
            }
        }
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
 * Individual color-coded word chip highlighting phonetic accuracy
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
                wordAccuracy.isPartiallyCorrect -> Icons.Default.VolumeUp
                else -> Icons.Default.Close
            },
            contentDescription = null,
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
                        color = when {
                            wordAccuracy.isCorrect -> DuoGreenLight
                            wordAccuracy.isPartiallyCorrect -> Color(0xFFFFF3CD)
                            else -> DuoRedLight
                        }
                    ) {
                        Text(
                            text = when {
                                wordAccuracy.isCorrect -> "Correcto"
                                wordAccuracy.isPartiallyCorrect -> "Casi exacto"
                                else -> "Por mejorar"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                wordAccuracy.isCorrect -> Color(0xFF2E7D32)
                                wordAccuracy.isPartiallyCorrect -> Color(0xFFB78103)
                                else -> Color(0xFFC62828)
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
