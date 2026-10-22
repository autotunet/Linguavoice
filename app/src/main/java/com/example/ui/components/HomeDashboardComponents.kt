package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WavingHand
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DifficultyTier
import com.example.data.model.LessonModule
import com.example.data.model.LevelSystem
import com.example.data.model.UserLevelInfo
import com.example.data.model.UserProfile
import com.example.data.repository.StreakInfo
import com.example.ui.theme.DuoBlue
import com.example.ui.theme.DuoCardBorder
import com.example.ui.theme.DuoGreen
import com.example.ui.theme.DuoGreenDark
import com.example.ui.theme.DuoOrange
import com.example.ui.theme.DuoOrangeDark
import com.example.ui.theme.DuoPurple
import com.example.ui.theme.DuoYellow
import kotlin.math.roundToInt

/**
 * Helper to determine if a LessonModule is completed by checking both its module ID
 * and any underlying exercise lessonId present in [UserProfile.completedLessonIds].
 */
fun isLessonModuleCompleted(module: LessonModule, completedIds: Set<String>): Boolean {
    return completedIds.contains(module.id) ||
        module.exercises.any { completedIds.contains(it.lessonId) }
}

/**
 * Material 3 User Progress Summary Card ("Tarjeta de resumen de progreso del usuario").
 * Displays current level, XP progress bar toward next level, completed lessons ratio,
 * average pronunciation accuracy, total XP, and a quick-start CTA for the next lesson.
 */
@Composable
fun UserProgressSummaryCard(
    userProfile: UserProfile,
    levelInfo: UserLevelInfo,
    completedLessonsCount: Int,
    totalLessonsCount: Int,
    nextAvailableLesson: LessonModule?,
    onContinueLesson: (LessonModule) -> Unit,
    onOpenLevelRoadmap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val safeTotalLessons = totalLessonsCount.coerceAtLeast(1)
    val safeCompletedLessons = completedLessonsCount.coerceIn(0, safeTotalLessons)
    val courseCompletionPercent = ((safeCompletedLessons.toFloat() / safeTotalLessons.toFloat()) * 100f)
        .roundToInt()
        .coerceIn(0, 100)

    val levelProgressPercent = ((levelInfo.progressFraction * 100f).roundToInt()).coerceIn(0, 100)
    val animatedLevelProgress by animateFloatAsState(
        targetValue = levelInfo.progressFraction.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "summaryLevelProgressAnim"
    )

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("user_progress_summary_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Header: User Level Badge + Language Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenLevelRoadmap() }
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = DuoPurple.copy(alpha = 0.15f),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = levelInfo.badgeEmoji,
                                fontSize = 26.sp
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "RESUMEN DE PROGRESO",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = DuoPurple,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = "Nivel ${levelInfo.level} • ${levelInfo.levelTitle}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Curso de ${userProfile.selectedLanguage.displayName} ${userProfile.selectedLanguage.flagEmoji}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                AssistChip(
                    onClick = onOpenLevelRoadmap,
                    label = {
                        Text(
                            text = "Nv. ${levelInfo.level}",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Ver mapa de niveles",
                            tint = DuoPurple,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = DuoPurple.copy(alpha = 0.10f),
                        labelColor = DuoPurple
                    ),
                    border = BorderStroke(1.dp, DuoPurple.copy(alpha = 0.3f)),
                    modifier = Modifier.testTag("summary_roadmap_chip")
                )
            }

            // XP Progress Bar Section
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth()
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
                            text = "Progreso al Nivel ${levelInfo.level + 1}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${levelInfo.xpInCurrentLevel} / ${levelInfo.nextLevelTargetXp - levelInfo.currentLevelBaseXp} EXP ($levelProgressPercent%)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = DuoPurple,
                            modifier = Modifier.testTag("summary_level_xp_text")
                        )
                    }

                    LinearProgressIndicator(
                        progress = { animatedLevelProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .testTag("summary_level_progress_indicator"),
                        color = DuoPurple,
                        trackColor = DuoCardBorder,
                        strokeCap = StrokeCap.Round
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Faltan ${levelInfo.xpNeededForNextLevel} EXP para subir de nivel",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$courseCompletionPercent% del curso",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = DuoGreenDark
                        )
                    }
                }
            }

            // 3 Material 3 Metric Tiles: Lecciones, Precisión, EXP Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ProgressMetricTile(
                    icon = Icons.Default.MenuBook,
                    iconTint = DuoGreen,
                    value = "$safeCompletedLessons/$safeTotalLessons",
                    label = "Lecciones",
                    modifier = Modifier
                        .weight(1f)
                        .testTag("summary_metric_lessons")
                )
                ProgressMetricTile(
                    icon = Icons.Default.GraphicEq,
                    iconTint = DuoBlue,
                    value = "${userProfile.accuracyAverage}%",
                    label = "Precisión",
                    modifier = Modifier
                        .weight(1f)
                        .testTag("summary_metric_accuracy")
                )
                ProgressMetricTile(
                    icon = Icons.Default.AutoAwesome,
                    iconTint = DuoOrange,
                    value = "${userProfile.totalXp}",
                    label = "EXP Total",
                    modifier = Modifier
                        .weight(1f)
                        .testTag("summary_metric_xp")
                )
            }

            // Quick Continue Action Button
            if (nextAvailableLesson != null) {
                Button(
                    onClick = { onContinueLesson(nextAvailableLesson) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("continue_next_lesson_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DuoGreen,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Continuar: ${nextAvailableLesson.title}",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressMetricTile(
    icon: ImageVector,
    iconTint: Color,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = iconTint.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, iconTint.copy(alpha = 0.22f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

/**
 * Material 3 Streak Counter Card ("Contador de rachas") for the main home screen.
 * Displays current streak days, best streak record, today's practice status, and
 * the 7-day weekly activity tracker.
 */
@Composable
fun Material3StreakCounterCard(
    streakInfo: StreakInfo,
    isBannerHidden: Boolean,
    onHideBanner: () -> Unit,
    onOpenStreakDetail: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "m3_streak_flame")
    val flameScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "m3_flame_scale"
    )

    var showWeekTracker by remember(streakInfo.practicedToday) {
        mutableStateOf(true)
    }

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("home_streak_counter_card")
            .clickable { onOpenStreakDetail() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Animated Flame Counter Badge
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFFFFD54F),
                                        DuoOrange,
                                        DuoOrangeDark
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Contador de racha",
                            tint = Color.White,
                            modifier = Modifier
                                .size(28.dp)
                                .scale(flameScale)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "${streakInfo.currentStreak} ${if (streakInfo.currentStreak == 1) "día" else "días"} de racha",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = DuoOrange,
                                modifier = Modifier.testTag("streak_counter_days_text")
                            )

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (streakInfo.practicedToday) {
                                    DuoGreen.copy(alpha = 0.15f)
                                } else {
                                    DuoYellow.copy(alpha = 0.22f)
                                }
                            ) {
                                Text(
                                    text = if (streakInfo.practicedToday) "Hoy ✅" else "Activa 🔥",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (streakInfo.practicedToday) DuoGreenDark else DuoOrangeDark,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        val todayXp = streakInfo.currentWeekDays.find { it.isToday }?.xpEarned ?: 0
                        Text(
                            text = "Mejor racha: ${streakInfo.bestStreak} días • Total: ${streakInfo.totalDaysPracticed} días • Hoy: +$todayXp EXP",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = { showWeekTracker = !showWeekTracker },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("streak_counter_toggle_week_button")
                ) {
                    Icon(
                        imageVector = if (showWeekTracker) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (showWeekTracker) "Ocultar semana" else "Mostrar semana",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(
                visible = showWeekTracker && streakInfo.currentWeekDays.isNotEmpty(),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        streakInfo.currentWeekDays.forEach { weekDay ->
                            WeekDayCircle(weekDay = weekDay)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Material 3 Level Group Header ("Encabezado de Nivel") for the lessons list grouped by level.
 */
@Composable
fun LevelSectionHeaderCard(
    level: Int,
    lessonsInLevel: List<LessonModule>,
    completedLessonIds: Set<String>,
    userLevel: Int,
    modifier: Modifier = Modifier
) {
    val tier: DifficultyTier? = remember(level) {
        LevelSystem.DIFFICULTY_TIERS.find { it.requiredLevel == level }
    }
    val isLevelUnlocked = userLevel >= level
    val completedInLevel = lessonsInLevel.count { isLessonModuleCompleted(it, completedLessonIds) }
    val totalInLevel = lessonsInLevel.size.coerceAtLeast(1)
    val levelFraction = (completedInLevel.toFloat() / totalInLevel.toFloat()).coerceIn(0f, 1f)

    val headerAccent = when {
        !isLevelUnlocked -> Color(0xFF8E8E93)
        completedInLevel == lessonsInLevel.size -> DuoGreen
        level == 1 -> DuoGreen
        level == 2 -> DuoBlue
        level == 3 -> DuoPurple
        else -> DuoOrange
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("level_section_header_$level"),
        shape = RoundedCornerShape(18.dp),
        color = headerAccent.copy(alpha = 0.10f),
        border = BorderStroke(1.5.dp, headerAccent.copy(alpha = 0.30f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                    Surface(
                        shape = CircleShape,
                        color = headerAccent.copy(alpha = 0.20f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = tier?.badgeEmoji ?: "🎙️",
                                fontSize = 20.sp
                            )
                        }
                    }

                    Column {
                        Text(
                            text = tier?.title ?: "Nivel $level",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = tier?.description ?: "Lecciones de pronunciación de nivel $level",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Level status badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isLevelUnlocked) headerAccent.copy(alpha = 0.18f) else Color(0xFFEEEEEE)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isLevelUnlocked) Icons.Default.CheckCircle else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (isLevelUnlocked) headerAccent else Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (isLevelUnlocked) {
                                "$completedInLevel/${lessonsInLevel.size}"
                            } else {
                                "Nv. $level"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isLevelUnlocked) headerAccent else Color.Gray
                        )
                    }
                }
            }

            LinearProgressIndicator(
                progress = { levelFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = headerAccent,
                trackColor = DuoCardBorder.copy(alpha = 0.6f),
                strokeCap = StrokeCap.Round
            )
        }
    }
}

/**
 * Material 3 Lesson Item Card ("Tarjeta de lección disponible por nivel").
 * Shows the lesson's icon, title, description, exercise count, XP reward,
 * unlock/completion status, and action button.
 */
@Composable
fun Material3LessonCard(
    module: LessonModule,
    lessonOrderNumber: Int,
    isCompleted: Boolean,
    isUnlocked: Boolean,
    isLevelUnlocked: Boolean,
    onCardClick: () -> Unit,
    onStartDirectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalXpReward = remember(module) {
        module.exercises.sumOf { it.xpReward }.coerceAtLeast(module.exercises.size * 15)
    }

    val iconVector = when {
        !isUnlocked -> Icons.Default.Lock
        module.iconName == "waving_hand" -> Icons.Default.WavingHand
        module.iconName == "restaurant" -> Icons.Default.Restaurant
        module.iconName == "flight" -> Icons.Default.Flight
        module.iconName == "local_fire_department" -> Icons.Default.LocalFireDepartment
        else -> Icons.Default.Mic
    }

    val statusColor = when {
        isCompleted -> DuoYellow
        isUnlocked -> DuoGreen
        else -> Color(0xFF9E9E9E)
    }

    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("lesson_card_${module.id}")
            .clickable(enabled = isUnlocked) { onCardClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (isUnlocked) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            }
        ),
        border = BorderStroke(
            width = if (isUnlocked && !isCompleted) 2.dp else 1.2.dp,
            color = when {
                isCompleted -> DuoYellow.copy(alpha = 0.65f)
                isUnlocked -> DuoGreen.copy(alpha = 0.65f)
                else -> DuoCardBorder
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Lesson Icon Container (also carries path_node_${module.id} testTag when in list mode)
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isUnlocked) statusColor.copy(alpha = 0.16f)
                        else Color(0xFFE5E5E5)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = module.title,
                    tint = if (isUnlocked) statusColor else Color(0xFF9E9E9E),
                    modifier = Modifier.size(28.dp)
                )

                if (isCompleted) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(2.dp)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(DuoOrange),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Completada",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            // Lesson Details Column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = statusColor.copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = when {
                                isCompleted -> "COMPLETADA"
                                isUnlocked -> "DISPONIBLE"
                                !isLevelUnlocked -> "NIVEL ${module.requiredLevel}"
                                else -> "BLOQUEADA"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isUnlocked) {
                                if (isCompleted) DuoOrangeDark else DuoGreenDark
                            } else {
                                Color.Gray
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = "• Lección $lessonOrderNumber",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = module.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    color = if (isUnlocked) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )

                Text(
                    text = module.description,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = "🎙️ ${module.exercises.size} ejercicios",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "⚡ +$totalXpReward EXP",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isUnlocked) DuoOrange else Color.Gray
                    )
                }
            }

            // Action Button (minimum 48dp touch target)
            if (isUnlocked) {
                FilledTonalButton(
                    onClick = onStartDirectClick,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isCompleted) {
                            DuoYellow.copy(alpha = 0.22f)
                        } else {
                            DuoGreen
                        },
                        contentColor = if (isCompleted) DuoOrangeDark else Color.White
                    ),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("start_lesson_btn_${module.id}")
                ) {
                    Icon(
                        imageVector = if (isCompleted) Icons.Default.Replay else Icons.Default.PlayArrow,
                        contentDescription = if (isCompleted) "Repasar lección" else "Empezar lección",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isCompleted) "Repasar" else "Hablar",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEEEEEE)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Lección bloqueada",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
