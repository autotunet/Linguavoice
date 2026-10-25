package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.PronunciationResult
import com.example.ui.theme.DuoBlue
import com.example.ui.theme.DuoBlueLight
import com.example.ui.theme.DuoCardBorder
import com.example.ui.theme.DuoGreen
import com.example.ui.theme.DuoGreenLight
import com.example.ui.theme.DuoOrange
import com.example.ui.theme.DuoRed
import com.example.ui.theme.DuoRedDark
import com.example.ui.theme.DuoRedLight
import com.example.ui.viewmodel.LivesState

/**
 * Dedicated 'Lives' Counter component displayed at the top of the UI.
 *
 * Visual features:
 * - Animated Hearts row (filled vs lost hearts).
 * - Decreases dynamically whenever pronunciation accuracy falls below the passing threshold (60%).
 * - Pulse/scale bounce animation on life loss.
 * - Current count badge (e.g., "5/5", "4/5", etc.).
 * - Pronunciation threshold badge ("Umbral mín: 60%").
 * - Critical warning pill when remaining lives <= 1.
 * - Interactive explanation popup explaining game rules.
 */
@Composable
fun LivesCounterComponent(
    currentLives: Int,
    maxLives: Int = LivesState.MAX_LIVES,
    accuracyThreshold: Int = LivesState.PASSING_ACCURACY_THRESHOLD,
    lastScorePercentage: Int? = null,
    onLivesClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    showThresholdBadge: Boolean = true
) {
    var previousLives by remember { mutableIntStateOf(currentLives) }
    var triggerPulse by remember { mutableStateOf(false) }
    var showExplanationDialog by remember { mutableStateOf(false) }

    LaunchedEffect(currentLives) {
        if (currentLives < previousLives) {
            triggerPulse = true
        }
        previousLives = currentLives
    }

    val pulseScale by animateFloatAsState(
        targetValue = if (triggerPulse) 1.14f else 1.0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        finishedListener = { triggerPulse = false },
        label = "livesPulseScale"
    )

    val isCritical = currentLives <= 1

    val containerBgColor by animateColorAsState(
        targetValue = when {
            currentLives == 0 -> DuoRedLight.copy(alpha = 0.9f)
            isCritical -> DuoRedLight.copy(alpha = 0.6f)
            else -> MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(300),
        label = "livesBgColor"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            currentLives == 0 -> DuoRed
            isCritical -> DuoRed
            else -> DuoRed.copy(alpha = 0.35f)
        },
        animationSpec = tween(300),
        label = "livesBorderColor"
    )

    Row(
        modifier = modifier
            .scale(pulseScale)
            .clip(RoundedCornerShape(16.dp))
            .background(containerBgColor)
            .border(BorderStroke(1.5.dp, borderColor), RoundedCornerShape(16.dp))
            .clickable {
                if (onLivesClick != null) {
                    onLivesClick()
                } else {
                    showExplanationDialog = true
                }
            }
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("lives_counter_component")
            .testTag("exercise_lives_indicator"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Hearts Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            for (i in 1..maxLives) {
                val isAlive = i <= currentLives
                val heartColor by animateColorAsState(
                    targetValue = if (isAlive) DuoRed else Color(0xFFC7C7C7),
                    animationSpec = tween(250),
                    label = "heartColor_$i"
                )

                Box(
                    modifier = Modifier.testTag("heart_icon_$i"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isAlive) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isAlive) "Vida activa $i" else "Vida perdida $i",
                        tint = heartColor,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }

        // Numeric Counter
        Text(
            text = "$currentLives/$maxLives",
            fontWeight = FontWeight.Black,
            fontSize = 13.sp,
            color = if (isCritical) DuoRedDark else DuoRed,
            modifier = Modifier
                .testTag("lives_counter_text")
                .testTag("exercise_lives_count_text")
        )

        // Threshold indicator chip (Umbral mín 60%)
        if (showThresholdBadge) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(DuoRed.copy(alpha = 0.12f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                    .testTag("lives_threshold_indicator")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = DuoRedDark,
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = "≥$accuracyThreshold%",
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = DuoRedDark
                    )
                }
            }
        }

        // Critical Warning Pill if 1 life left
        if (isCritical && currentLives > 0) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(DuoOrange.copy(alpha = 0.2f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                    .testTag("lives_critical_warning")
            ) {
                Text(
                    text = "⚠️ ¡Última vida!",
                    fontWeight = FontWeight.Black,
                    fontSize = 9.sp,
                    color = DuoOrange
                )
            }
        }
    }

    if (showExplanationDialog) {
        LivesExplanationDialog(
            currentLives = currentLives,
            maxLives = maxLives,
            accuracyThreshold = accuracyThreshold,
            onDismiss = { showExplanationDialog = false }
        )
    }
}

/**
 * Top bar header that embeds the LivesCounterComponent alongside exercise navigation.
 */
@Composable
fun LivesCounterTopBar(
    currentLives: Int,
    maxLives: Int = LivesState.MAX_LIVES,
    accuracyThreshold: Int = LivesState.PASSING_ACCURACY_THRESHOLD,
    onCloseClick: () -> Unit,
    onLivesClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("lives_top_bar"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = onCloseClick,
            modifier = Modifier
                .size(40.dp)
                .testTag("lives_top_bar_close")
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Cerrar sesión",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        LivesCounterComponent(
            currentLives = currentLives,
            maxLives = maxLives,
            accuracyThreshold = accuracyThreshold,
            onLivesClick = onLivesClick
        )
    }
}

/**
 * Prominent Game Over dialog triggered when remaining lives hit 0.
 *
 * Displays:
 * - Broken heart badge and Game Over alert.
 * - Accuracy recap (achieved score vs minimum threshold required).
 * - "Reiniciar Nivel" button to reset the current level to exercise 1 with full lives.
 * - "Volver al Mapa" button to safely exit to the learning path.
 */
@Composable
fun GameOverDialog(
    lastFailedResult: PronunciationResult?,
    levelRestartCount: Int = 1,
    accuracyThreshold: Int = LivesState.PASSING_ACCURACY_THRESHOLD,
    onConfirmRestart: () -> Unit,
    onExitToMap: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "gameOverPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gameOverBadgePulse"
    )

    Dialog(onDismissRequest = onConfirmRestart) {
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            border = BorderStroke(2.dp, DuoRed.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("game_over_dialog")
                .testTag("level_restarted_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Game Over Icon Badge
                Box(
                    modifier = Modifier
                        .scale(pulseScale)
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(DuoRedLight)
                        .border(2.dp, DuoRed, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "💔",
                        fontSize = 38.sp
                    )
                }

                // Title
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "¡JUEGO TERMINADO!",
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp,
                        color = DuoRed,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("game_over_title")
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Te has quedado sin vidas (0/5)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                // Performance and Accuracy Recap Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("game_over_accuracy_recap"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DuoRedLight.copy(alpha = 0.35f)),
                    border = BorderStroke(1.dp, DuoRed.copy(alpha = 0.25f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Precisión obtenida:",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val score = lastFailedResult?.scorePercentage ?: 0
                            Text(
                                text = "$score%",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = DuoRedDark,
                                modifier = Modifier.testTag("game_over_last_score")
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Umbral mínimo de pase:",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "≥$accuracyThreshold%",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = DuoGreen,
                                modifier = Modifier.testTag("game_over_threshold_text")
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "Tu pronunciación quedó por debajo del umbral del $accuracyThreshold%. El nivel se reinicia desde el primer ejercicio con tus 5 vidas restauradas.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }

                // Action Buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DuoButton(
                        text = "REINICIAR NIVEL",
                        onClick = onConfirmRestart,
                        colorStyle = DuoButtonColor.RED,
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("game_over_restart_button")
                            .testTag("confirm_level_restart_button")
                    )

                    DuoButton(
                        text = "VOLVER AL MAPA",
                        onClick = onExitToMap,
                        colorStyle = DuoButtonColor.WHITE,
                        icon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("game_over_exit_button")
                            .testTag("exit_after_restart_button")
                    )
                }
            }
        }
    }
}

/**
 * Educational dialog explaining how lives and pronunciation accuracy thresholds work.
 */
@Composable
fun LivesExplanationDialog(
    currentLives: Int,
    maxLives: Int,
    accuracyThreshold: Int,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("lives_explanation_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(DuoRedLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "❤️", fontSize = 24.sp)
                    }
                    Column {
                        Text(
                            text = "Sistema de Vidas",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tienes $currentLives de $maxLives vidas disponibles",
                            fontSize = 12.sp,
                            color = DuoRedDark,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DuoBlueLight.copy(alpha = 0.4f)),
                    border = BorderStroke(1.dp, DuoBlue.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "📋 Reglas de Pronunciación:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "• Cada nivel comienza con 5 vidas completas.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "• Si tu precisión cae por debajo del $accuracyThreshold%, pierdes 1 vida.",
                            fontSize = 12.sp,
                            color = DuoRedDark,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "• Si tus vidas llegan a 0, se activa Game Over y deberás reiniciar el nivel.",
                            fontSize = 12.sp,
                            color = DuoRedDark,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "• Al reiniciar se restauran automáticamente las 5 vidas.",
                            fontSize = 12.sp,
                            color = DuoGreen
                        )
                    }
                }

                DuoButton(
                    text = "¡ENTENDIDO!",
                    onClick = onDismiss,
                    colorStyle = DuoButtonColor.BLUE,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
