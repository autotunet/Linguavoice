package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PronunciationFeedbackStatus
import com.example.data.model.PronunciationResult
import com.example.data.model.SpokenLanguage
import com.example.data.model.WordAccuracy
import com.example.ui.theme.DuoBlue
import com.example.ui.theme.DuoCardBorder
import com.example.ui.theme.DuoGreen
import com.example.ui.theme.DuoGreenLight
import com.example.ui.theme.DuoOrange
import com.example.ui.theme.DuoPurple
import com.example.ui.theme.DuoRed
import com.example.ui.theme.DuoRedLight
import com.example.ui.theme.LocalAppStrings

/**
 * Localized labels for the Score & Accuracy Comparison Feedback Card.
 */
private data class ScoreComparisonLabels(
    val headerBadge: String,
    val scoreAccuracyLabel: String,
    val overallAccuracyLabel: String,
    val fluencyMetricLabel: String,
    val wordMatchMetricLabel: String,
    val phraseComparisonHeader: String,
    val targetPhraseLabel: String,
    val userSpokenInputLabel: String,
    val wordByWordHeader: String,
    val expectedWordHeader: String,
    val spokenWordHeader: String,
    val accuracyColHeader: String,
    val noSpeechDetected: String,
    val tapWordHint: String
)

private fun getScoreComparisonLabels(spokenLanguage: SpokenLanguage): ScoreComparisonLabels {
    return when (spokenLanguage) {
        SpokenLanguage.SPANISH -> ScoreComparisonLabels(
            headerBadge = "PUNTUACIÓN Y PRECISIÓN (SCORE / ACCURACY)",
            scoreAccuracyLabel = "Precisión",
            overallAccuracyLabel = "Puntuación",
            fluencyMetricLabel = "Fluidez",
            wordMatchMetricLabel = "Palabras",
            phraseComparisonHeader = "COMPARACIÓN: FRASE OBJETIVO VS. TU VOZ",
            targetPhraseLabel = "Frase Objetivo (Target):",
            userSpokenInputLabel = "Tu Voz Reconocida (Input):",
            wordByWordHeader = "PRECISIÓN PALABRA POR PALABRA",
            expectedWordHeader = "Objetivo",
            spokenWordHeader = "Dijiste",
            accuracyColHeader = "Precisión",
            noSpeechDetected = "Sin voz detectada",
            tapWordHint = "Toca cualquier palabra para escuchar su pronunciación nativa"
        )
        SpokenLanguage.ENGLISH -> ScoreComparisonLabels(
            headerBadge = "PRONUNCIATION SCORE & ACCURACY",
            scoreAccuracyLabel = "Accuracy",
            overallAccuracyLabel = "Score",
            fluencyMetricLabel = "Fluency",
            wordMatchMetricLabel = "Word Match",
            phraseComparisonHeader = "COMPARISON: TARGET PHRASE VS. YOUR INPUT",
            targetPhraseLabel = "Target Phrase:",
            userSpokenInputLabel = "Your Spoken Input:",
            wordByWordHeader = "WORD-BY-WORD ACCURACY BREAKDOWN",
            expectedWordHeader = "Target",
            spokenWordHeader = "You Said",
            accuracyColHeader = "Accuracy",
            noSpeechDetected = "No speech detected",
            tapWordHint = "Tap any word to hear its native pronunciation"
        )
        SpokenLanguage.PORTUGUESE -> ScoreComparisonLabels(
            headerBadge = "PONTUAÇÃO E PRECISÃO (SCORE / ACCURACY)",
            scoreAccuracyLabel = "Precisão",
            overallAccuracyLabel = "Pontuação",
            fluencyMetricLabel = "Fluência",
            wordMatchMetricLabel = "Palavras",
            phraseComparisonHeader = "COMPARAÇÃO: FRASE ALVO VS. SUA VOZ",
            targetPhraseLabel = "Frase Alvo (Target):",
            userSpokenInputLabel = "Sua Voz Reconhecida (Input):",
            wordByWordHeader = "PRECISÃO PALAVRA POR PALAVRA",
            expectedWordHeader = "Alvo",
            spokenWordHeader = "Você Disse",
            accuracyColHeader = "Precisão",
            noSpeechDetected = "Nenhuma fala detectada",
            tapWordHint = "Toque em qualquer palavra para ouvir a pronúncia nativa"
        )
        SpokenLanguage.FRENCH -> ScoreComparisonLabels(
            headerBadge = "SCORE ET PRÉCISION (SCORE / ACCURACY)",
            scoreAccuracyLabel = "Précision",
            overallAccuracyLabel = "Score",
            fluencyMetricLabel = "Fluidité",
            wordMatchMetricLabel = "Mots",
            phraseComparisonHeader = "COMPARAISON : PHRASE CIBLE VS. VOTRE VOIX",
            targetPhraseLabel = "Phrase Cible (Target) :",
            userSpokenInputLabel = "Votre Voix Reconnue (Input) :",
            wordByWordHeader = "PRÉCISION MOT PAR MOT",
            expectedWordHeader = "Cible",
            spokenWordHeader = "Vous Avez Dit",
            accuracyColHeader = "Précision",
            noSpeechDetected = "Aucune voix détectée",
            tapWordHint = "Touchez un mot pour écouter sa prononciation native"
        )
        SpokenLanguage.ITALIAN -> ScoreComparisonLabels(
            headerBadge = "PUNTEGGIO E PRECISIONE (SCORE / ACCURACY)",
            scoreAccuracyLabel = "Precisione",
            overallAccuracyLabel = "Punteggio",
            fluencyMetricLabel = "Fluidità",
            wordMatchMetricLabel = "Parole",
            phraseComparisonHeader = "CONFRONTO: FRASE OBIETTIVO VS. LA TUA VOCE",
            targetPhraseLabel = "Frase Obiettivo (Target):",
            userSpokenInputLabel = "La Tua Voce Riconosciuta (Input):",
            wordByWordHeader = "PRECISIONE PAROLA PER PAROLA",
            expectedWordHeader = "Obiettivo",
            spokenWordHeader = "Hai Detto",
            accuracyColHeader = "Precisione",
            noSpeechDetected = "Nessuna voce rilevata",
            tapWordHint = "Tocca qualsiasi parola per ascoltare la pronuncia nativa"
        )
        SpokenLanguage.GERMAN -> ScoreComparisonLabels(
            headerBadge = "PUNKTZAHL & GENAUIGKEIT (SCORE / ACCURACY)",
            scoreAccuracyLabel = "Genauigkeit",
            overallAccuracyLabel = "Score",
            fluencyMetricLabel = "Flüssigkeit",
            wordMatchMetricLabel = "Worttreffer",
            phraseComparisonHeader = "VERGLEICH: ZIELSATZ VS. DEINE EINGABE",
            targetPhraseLabel = "Zielsatz (Target):",
            userSpokenInputLabel = "Erkannte Spracheingabe (Input):",
            wordByWordHeader = "WORT-FÜR-WORT-GENAUIGKEIT",
            expectedWordHeader = "Zielwort",
            spokenWordHeader = "Gesprochen",
            accuracyColHeader = "Genauigkeit",
            noSpeechDetected = "Keine Sprache erkannt",
            tapWordHint = "Tippe auf ein Wort, um die Aussprache zu hören"
        )
    }
}

/**
 * Feedback UI component that displays a prominent 'Score' and 'Accuracy' percentage after a user
 * finishes speaking, directly comparing their spoken input against the target phrase along with
 * word-by-word accuracy percentages.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PronunciationScoreComparisonCard(
    result: PronunciationResult,
    modifier: Modifier = Modifier,
    isPlayingUserAudio: Boolean = false,
    onListenTargetClick: (() -> Unit)? = null,
    onPlayUserAudioClick: (() -> Unit)? = null,
    onStopUserAudioClick: (() -> Unit)? = null,
    onWordClick: ((String) -> Unit)? = null,
    onRetryClick: (() -> Unit)? = null
) {
    val strings = LocalAppStrings.current
    val spokenLanguage = strings.spokenLanguage
    val labels = remember(spokenLanguage) { getScoreComparisonLabels(spokenLanguage) }
    val status = result.feedbackStatus
    val accentColor = pronunciationStatusAccentColor(status)
    val softContainerColor = pronunciationStatusContainerColor(status)

    var selectedWordIndex by remember(result) { mutableStateOf<Int?>(null) }

    val animatedScoreFraction by animateFloatAsState(
        targetValue = (result.scorePercentage.coerceIn(0, 100) / 100f),
        animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing),
        label = "score_fraction_anim"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("pronunciation_score_comparison_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, accentColor)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 1. Top Hero Header: Animated Circular Score / Accuracy Gauge + Verdict Summary
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                softContainerColor,
                                softContainerColor.copy(alpha = 0.45f)
                            )
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = accentColor.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = labels.headerBadge,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = accentColor,
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        if (result.xpEarned > 0) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFFFF3CD),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DuoOrange.copy(alpha = 0.5f)),
                                modifier = Modifier.testTag("score_comparison_xp_badge")
                            ) {
                                Text(
                                    text = "+${result.xpEarned} EXP",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = DuoOrange,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Large Circular Score / Accuracy Gauge
                        LargeScoreAccuracyGauge(
                            scorePercentage = result.scorePercentage,
                            animatedFraction = animatedScoreFraction,
                            accentColor = accentColor,
                            label = labels.scoreAccuracyLabel
                        )

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = accentColor,
                                    modifier = Modifier.testTag("score_comparison_status_badge")
                                ) {
                                    Text(
                                        text = status.getBadgeLabelFor(spokenLanguage),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = "${result.scorePercentage}% ${labels.scoreAccuracyLabel}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = accentColor,
                                    modifier = Modifier.testTag("score_accuracy_headline_text")
                                )
                            }

                            Text(
                                text = status.getTitleFor(spokenLanguage),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF1F2937)
                            )

                            Text(
                                text = status.getGuidanceFor(spokenLanguage),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF4B5563)
                            )
                        }
                    }

                    // 3-Column Sub-Metrics Strip (Score / Fluency / Word Match)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("score_accuracy_metrics_row"),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ScoreSubMetricPill(
                            label = labels.overallAccuracyLabel,
                            percentage = result.scorePercentage,
                            color = accentColor,
                            testTag = "metric_overall_accuracy_pct",
                            modifier = Modifier.weight(1f)
                        )
                        ScoreSubMetricPill(
                            label = labels.fluencyMetricLabel,
                            percentage = result.fluencyScore,
                            color = DuoBlue,
                            testTag = "metric_fluency_pct",
                            modifier = Modifier.weight(1f)
                        )
                        ScoreSubMetricPill(
                            label = labels.wordMatchMetricLabel,
                            percentage = result.completenessScore,
                            color = DuoPurple,
                            testTag = "metric_word_match_pct",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 2. Side-by-Side / Stacked Comparison: Target Phrase vs. User Spoken Input
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                        contentDescription = null,
                        tint = DuoBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = labels.phraseComparisonHeader,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF4B5563),
                        letterSpacing = 0.5.sp
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("target_vs_input_comparison_box"),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Target Phrase Box
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFF0F7FF),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DuoBlue.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("comparison_target_phrase_box")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = labels.targetPhraseLabel,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DuoBlue
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "\"${result.targetText}\"",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF1E293B),
                                    modifier = Modifier.testTag("comparison_target_phrase_text")
                                )
                            }

                            if (onListenTargetClick != null) {
                                Surface(
                                    shape = CircleShape,
                                    color = DuoBlue.copy(alpha = 0.14f),
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .size(40.dp)
                                        .clickable { onListenTargetClick() }
                                        .testTag("comparison_listen_target_button")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.VolumeUp,
                                            contentDescription = strings.listenNativeBtn,
                                            tint = DuoBlue,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // User Spoken Input Box
                    val spokenDisplay = result.recognizedText.takeIf { it.isNotBlank() } ?: labels.noSpeechDetected
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = softContainerColor.copy(alpha = 0.55f),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, accentColor.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("comparison_user_input_box")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = null,
                                        tint = accentColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = labels.userSpokenInputLabel,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = accentColor
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "\"$spokenDisplay\"",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF1E293B),
                                    modifier = Modifier.testTag("comparison_user_spoken_text")
                                )
                            }

                            if (onPlayUserAudioClick != null) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isPlayingUserAudio) DuoPurple else accentColor.copy(alpha = 0.16f),
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .size(40.dp)
                                        .clickable {
                                            if (isPlayingUserAudio) {
                                                onStopUserAudioClick?.invoke()
                                            } else {
                                                onPlayUserAudioClick()
                                            }
                                        }
                                        .testTag("comparison_listen_user_button")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (isPlayingUserAudio) Icons.Default.Stop else Icons.Default.PlayArrow,
                                            contentDescription = strings.playMyRecordingBtn,
                                            tint = if (isPlayingUserAudio) Color.White else accentColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Word-by-Word Comparison & Individual Word Accuracy Percentage
                if (result.wordAccuracies.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("word_by_word_comparison_list"),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = labels.wordByWordHeader,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF4B5563),
                            letterSpacing = 0.5.sp
                        )

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            result.wordAccuracies.forEachIndexed { index, wordAcc ->
                                WordAccuracyComparisonChip(
                                    wordAccuracy = wordAcc,
                                    index = index,
                                    isSelected = selectedWordIndex == index,
                                    onClick = {
                                        selectedWordIndex = if (selectedWordIndex == index) null else index
                                        onWordClick?.invoke(wordAcc.targetWord)
                                    }
                                )
                            }
                        }

                        Text(
                            text = labels.tapWordHint,
                            fontSize = 10.sp,
                            color = Color.Gray
                        )

                        AnimatedVisibility(
                            visible = selectedWordIndex != null,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            selectedWordIndex?.let { idx ->
                                result.wordAccuracies.getOrNull(idx)?.let { wordAcc ->
                                    WordComparisonDetailRow(
                                        wordAccuracy = wordAcc,
                                        labels = labels,
                                        onPlayWord = { onWordClick?.invoke(wordAcc.targetWord) }
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Quick Retry / Listen Action Row
                if (onRetryClick != null || onListenTargetClick != null) {
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
                                    .testTag("score_comparison_listen_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VolumeUp,
                                        contentDescription = null,
                                        tint = DuoBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = strings.listenNativeBtn,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = DuoBlue,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        if (onRetryClick != null) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = accentColor.copy(alpha = 0.12f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .clickable { onRetryClick() }
                                    .testTag("score_comparison_retry_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = accentColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = strings.tryAgainMicBtn,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = accentColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
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

@Composable
private fun LargeScoreAccuracyGauge(
    scorePercentage: Int,
    animatedFraction: Float,
    accentColor: Color,
    label: String
) {
    val clampedScore = scorePercentage.coerceIn(0, 100)
    Box(
        modifier = Modifier
            .size(78.dp)
            .clip(CircleShape)
            .background(Color.White)
            .padding(4.dp)
            .testTag("score_accuracy_circular_gauge"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(68.dp)) {
            val stroke = 7.dp.toPx()
            drawArc(
                color = accentColor.copy(alpha = 0.18f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            drawArc(
                color = accentColor,
                startAngle = -90f,
                sweepAngle = animatedFraction * 360f,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$clampedScore%",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = accentColor,
                modifier = Modifier.testTag("score_accuracy_percentage_text")
            )
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4B5563),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ScoreSubMetricPill(
    label: String,
    percentage: Int,
    color: Color,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val clamped = percentage.coerceIn(0, 100)
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.92f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.35f)),
        modifier = modifier.testTag(testTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
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
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$clamped%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = color
                )
            }
            LinearProgressIndicator(
                progress = { clamped / 100f },
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

@Composable
private fun WordAccuracyComparisonChip(
    wordAccuracy: WordAccuracy,
    index: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val chipColor = when {
        wordAccuracy.isCorrect -> DuoGreen
        wordAccuracy.isPartiallyCorrect -> DuoOrange
        else -> DuoRed
    }
    val chipBg = when {
        wordAccuracy.isCorrect -> DuoGreenLight.copy(alpha = 0.65f)
        wordAccuracy.isPartiallyCorrect -> Color(0xFFFFF4D6)
        else -> DuoRedLight.copy(alpha = 0.65f)
    }
    val textColor = when {
        wordAccuracy.isCorrect -> Color(0xFF1B5E20)
        wordAccuracy.isPartiallyCorrect -> Color(0xFF9A6700)
        else -> Color(0xFFB71C1C)
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = chipBg,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.2.dp,
            color = chipColor
        ),
        modifier = Modifier
            .clickable { onClick() }
            .testTag("comparison_word_item_$index")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = when {
                    wordAccuracy.isCorrect -> Icons.Default.CheckCircle
                    wordAccuracy.isPartiallyCorrect -> Icons.Default.GraphicEq
                    else -> Icons.Default.Close
                },
                contentDescription = null,
                tint = chipColor,
                modifier = Modifier.size(14.dp)
            )

            Column {
                Text(
                    text = wordAccuracy.targetWord,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = textColor
                )
                val spokenWord = wordAccuracy.matchedSpokenWord
                if (!spokenWord.isNullOrBlank() && !spokenWord.equals(wordAccuracy.targetWord, ignoreCase = true)) {
                    Text(
                        text = "🎙️ $spokenWord",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF4B5563)
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = chipColor,
                modifier = Modifier.testTag("comparison_word_score_$index")
            ) {
                Text(
                    text = "${wordAccuracy.similarityPercentage}%",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun WordComparisonDetailRow(
    wordAccuracy: WordAccuracy,
    labels: ScoreComparisonLabels,
    onPlayWord: () -> Unit
) {
    val accent = pronunciationStatusAccentColor(wordAccuracy.feedbackStatus)
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF8FAFC),
        border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("comparison_word_detail_panel")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${labels.expectedWordHeader}: \"${wordAccuracy.targetWord}\"  ↔  ${labels.spokenWordHeader}: \"${wordAccuracy.matchedSpokenWord ?: "—"}\"",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = "${labels.accuracyColHeader}: ${wordAccuracy.similarityPercentage}% • ${wordAccuracy.tip.orEmpty()}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = accent
                )
            }
            Icon(
                imageVector = Icons.Default.VolumeUp,
                contentDescription = null,
                tint = DuoBlue,
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onPlayWord() }
            )
        }
    }
}
