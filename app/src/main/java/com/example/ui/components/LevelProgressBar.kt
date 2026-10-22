package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.LevelSystem
import com.example.data.model.UserLevelInfo
import com.example.ui.theme.DuoBlue
import com.example.ui.theme.DuoCardBorder
import com.example.ui.theme.DuoGreen
import com.example.ui.theme.DuoGreenDark
import com.example.ui.theme.DuoGreenLight
import com.example.ui.theme.DuoOrange
import com.example.ui.theme.DuoPurple
import com.example.ui.theme.DuoYellow
import com.example.ui.viewmodel.CurrentLevelProgressState
import com.example.ui.viewmodel.LivesState
import kotlin.math.roundToInt

/**
 * Visual progress bar displayed at the top of the interface during an active level/exercise or dialogue.
 * Shows the exact percentage (0% - 100%) of the current level completed by the user,
 * segmented exercise milestones, current step counter, and the lives indicator.
 */
@Composable
fun CurrentLevelTopProgressBar(
    levelProgress: CurrentLevelProgressState,
    livesState: LivesState,
    onCloseClick: () -> Unit,
    onLivesClick: () -> Unit,
    modifier: Modifier = Modifier,
    closeButtonTestTag: String = "exercise_close_button"
) {
    val animatedFraction by animateFloatAsState(
        targetValue = levelProgress.progressFraction.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
        label = "levelProgressFractionAnim"
    )

    val percentage = levelProgress.completionPercentage.coerceIn(0, 100)

    val accentColor by animateColorAsState(
        targetValue = when {
            percentage >= 100 -> DuoGreen
            percentage >= 50 -> DuoGreen
            percentage > 0 -> DuoBlue
            else -> DuoGreen
        },
        animationSpec = tween(300),
        label = "levelProgressAccentColor"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("top_level_progress_container"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, DuoCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            // Top Row: Close button, Level Title + Percentage Badge, and Lives Manager
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onCloseClick,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag(closeButtonTestTag)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Salir del nivel",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = levelProgress.levelTitle.ifBlank { "Nivel Actual" },
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        // Percentage badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (percentage > 0) DuoGreenLight.copy(alpha = 0.7f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (percentage > 0) DuoGreen else DuoCardBorder,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                .testTag("level_progress_percentage")
                        ) {
                            Text(
                                text = "$percentage% completado",
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                color = if (percentage > 0) DuoGreenDark else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Animated Visual Progress Bar with 3D Glossy Highlight
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(14.dp)
                                .clip(RoundedCornerShape(7.dp))
                                .background(DuoCardBorder.copy(alpha = 0.65f))
                                .testTag("level_progress_bar")
                        ) {
                            // Filled progress track
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(animatedFraction.coerceAtLeast(if (percentage == 0) 0.03f else 0f))
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(7.dp))
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                accentColor,
                                                DuoGreen
                                            )
                                        )
                                    )
                            ) {
                                // Top glossy reflection bar (Duolingo 3D style)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 6.dp, vertical = 2.5.dp)
                                        .height(3.5.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(Color.White.copy(alpha = 0.38f))
                                        .align(Alignment.TopCenter)
                                )
                            }

                            // Step milestone dividers if totalExercises > 1
                            if (levelProgress.totalExercises in 2..10) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    repeat(levelProgress.totalExercises - 1) {
                                        Box(
                                            modifier = Modifier
                                                .width(1.5.dp)
                                                .fillMaxHeight()
                                                .background(Color.White.copy(alpha = 0.55f))
                                        )
                                    }
                                }
                            }
                        }

                        // Step fraction counter (e.g., 2/5)
                        Text(
                            text = "${levelProgress.currentStepNumber}/${levelProgress.totalExercises}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.testTag("exercise_progress_text")
                        )
                    }
                }

                LivesBarIndicator(
                    currentLives = livesState.currentLives,
                    maxLives = livesState.maxLives,
                    onClick = onLivesClick
                )
            }
        }
    }
}

@Composable
fun LevelProgressBar(
    levelInfo: UserLevelInfo,
    modifier: Modifier = Modifier
) {
    var showRoadmapDialog by remember { mutableStateOf(false) }
    val levelPercentage = ((levelInfo.progressFraction * 100f).roundToInt()).coerceIn(0, 100)
    val animatedLevelFraction by animateFloatAsState(
        targetValue = levelInfo.progressFraction.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "xpLevelBarAnim"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("level_progress_card")
            .clickable { showRoadmapDialog = true },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, DuoCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Level badge, Title and Percentage Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(DuoPurple.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = levelInfo.badgeEmoji, fontSize = 20.sp)
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "NIVEL ${levelInfo.level}",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = DuoPurple
                            )
                            Text(
                                text = " • ${levelInfo.levelTitle}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "${levelInfo.totalXp} EXP Total • $levelPercentage% del nivel completado",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Percentage + Roadmap pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(DuoPurple.copy(alpha = 0.14f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("overall_level_percentage_badge")
                    ) {
                        Text(
                            text = "$levelPercentage%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = DuoPurple
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF3E5F5))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Niveles 🗺️",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = DuoPurple
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { animatedLevelFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = DuoPurple,
                trackColor = DuoCardBorder
            )

            // Footer note
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${levelInfo.xpInCurrentLevel} / ${levelInfo.nextLevelTargetXp - levelInfo.currentLevelBaseXp} EXP ($levelPercentage%)",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Faltan ${levelInfo.xpNeededForNextLevel} EXP para Nivel ${levelInfo.level + 1}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = DuoPurple
                )
            }
        }
    }

    if (showRoadmapDialog) {
        LevelRoadmapDialog(
            levelInfo = levelInfo,
            onDismiss = { showRoadmapDialog = false }
        )
    }
}

@Composable
fun LevelRoadmapDialog(
    levelInfo: UserLevelInfo,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(DuoPurple.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🏆", fontSize = 24.sp)
                    }
                    Column {
                        Text(
                            text = "Niveles & Categorías",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Gana EXP hablando para desbloquear nuevos temas",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Tiers list
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LevelSystem.DIFFICULTY_TIERS.forEach { tier ->
                        val isUnlocked = levelInfo.level >= tier.requiredLevel

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .border(
                                    width = 1.5.dp,
                                    color = if (isUnlocked) DuoGreen else DuoCardBorder,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .background(if (isUnlocked) DuoGreen.copy(alpha = 0.08f) else Color(0xFFF9F9F9))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = tier.badgeEmoji, fontSize = 24.sp, modifier = Modifier.padding(end = 10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = tier.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else Color.Gray
                                    )
                                }
                                Text(
                                    text = tier.description,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (isUnlocked) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(DuoGreen),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Desbloqueado",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Bloqueado",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Nv. ${tier.requiredLevel}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
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
