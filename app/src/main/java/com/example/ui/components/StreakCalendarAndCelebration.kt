package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.repository.CalendarDayStreak
import com.example.data.repository.StreakInfo
import com.example.data.repository.StreakRepository
import com.example.ui.theme.DuoBlue
import com.example.ui.theme.DuoCardBorder
import com.example.ui.theme.DuoGreen
import com.example.ui.theme.DuoGreenDark
import com.example.ui.theme.DuoOrange
import com.example.ui.theme.DuoOrangeDark
import com.example.ui.theme.DuoPurple
import com.example.ui.theme.DuoYellow
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * State representing the user's daily goal progress and celebration animation status.
 */
data class DailyGoalCelebrationState(
    val isCelebrationVisible: Boolean = false,
    val celebrationTriggerCount: Int = 0,
    val dailyXpTarget: Int = StreakRepository.DEFAULT_DAILY_XP_GOAL,
    val dailyExercisesTarget: Int = StreakRepository.DEFAULT_DAILY_EXERCISES_GOAL,
    val todayXpEarned: Int = 30,
    val todayExercisesCompleted: Int = 2,
    val isDailyGoalReached: Boolean = false,
    val progressFraction: Float = 0.6f,
    val progressPercentage: Int = 60,
    val bonusGemsAwarded: Int = 25,
    val streakDaysCount: Int = 4
)

/**
 * Material 3 Visual Streak Calendar & Daily Goal Card ("Calendario de rachas del usuario y meta diaria").
 * Displays an interactive monthly grid of practiced days, daily goal completion badges,
 * customizable daily XP goal progress bar, day inspector, and celebration trigger.
 */
@Composable
fun UserStreakCalendarCard(
    streakInfo: StreakInfo,
    dailyGoalState: DailyGoalCelebrationState,
    onSelectDailyGoalXp: (Int) -> Unit,
    onTriggerCelebration: () -> Unit,
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean = true
) {
    var isCalendarExpanded by remember { mutableStateOf(initiallyExpanded) }

    val calendarDays = remember(streakInfo.calendarDays, dailyGoalState.dailyXpTarget, dailyGoalState.todayXpEarned) {
        streakInfo.calendarDays.map { day ->
            if (day.isToday) {
                val updatedXp = maxOf(day.xpEarned, dailyGoalState.todayXpEarned)
                val updatedEx = maxOf(day.exercisesCompleted, dailyGoalState.todayExercisesCompleted)
                day.copy(
                    isPracticed = day.isPracticed || updatedXp > 0 || updatedEx > 0,
                    xpEarned = updatedXp,
                    exercisesCompleted = updatedEx,
                    isDailyGoalMet = updatedXp >= dailyGoalState.dailyXpTarget ||
                        updatedEx >= dailyGoalState.dailyExercisesTarget
                )
            } else {
                day.copy(
                    isDailyGoalMet = day.isPracticed &&
                        (day.xpEarned >= dailyGoalState.dailyXpTarget ||
                            day.exercisesCompleted >= dailyGoalState.dailyExercisesTarget)
                )
            }
        }
    }

    var selectedDay by remember(calendarDays) {
        mutableStateOf(
            calendarDays.firstOrNull { it.isToday }
                ?: calendarDays.lastOrNull { it.isPracticed }
                ?: calendarDays.firstOrNull { it.isCurrentMonth }
        )
    }

    val animatedGoalProgress by animateFloatAsState(
        targetValue = dailyGoalState.progressFraction.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "dailyGoalProgressAnim"
    )

    val practicedInMonthCount = remember(calendarDays) {
        calendarDays.count { it.isCurrentMonth && it.isPracticed }
    }
    val goalsMetInMonthCount = remember(calendarDays) {
        calendarDays.count { it.isCurrentMonth && it.isDailyGoalMet }
    }

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("user_streak_calendar_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Header: Calendar Icon + Month Title + Expand Toggle
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
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = DuoOrange.copy(alpha = 0.15f),
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Calendario de rachas",
                                tint = DuoOrange,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "CALENDARIO DE RACHAS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = DuoOrange,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = streakInfo.currentMonthTitle.ifBlank { "Mes Actual" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.testTag("streak_calendar_month_title")
                        )
                        Text(
                            text = "$practicedInMonthCount días activos • $goalsMetInMonthCount metas cumplidas",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = { isCalendarExpanded = !isCalendarExpanded },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("toggle_streak_calendar_button")
                ) {
                    Icon(
                        imageVector = if (isCalendarExpanded) {
                            Icons.Default.KeyboardArrowUp
                        } else {
                            Icons.Default.KeyboardArrowDown
                        },
                        contentDescription = if (isCalendarExpanded) {
                            "Contraer calendario"
                        } else {
                            "Expandir calendario"
                        },
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 2. Daily Goal Progress Section ("Meta Diaria")
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = if (dailyGoalState.isDailyGoalReached) {
                    DuoGreen.copy(alpha = 0.10f)
                } else {
                    DuoOrange.copy(alpha = 0.08f)
                },
                border = BorderStroke(
                    1.2.dp,
                    if (dailyGoalState.isDailyGoalReached) {
                        DuoGreen.copy(alpha = 0.35f)
                    } else {
                        DuoOrange.copy(alpha = 0.28f)
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("daily_goal_progress_section")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (dailyGoalState.isDailyGoalReached) {
                                    Icons.Default.EmojiEvents
                                } else {
                                    Icons.Default.AutoAwesome
                                },
                                contentDescription = null,
                                tint = if (dailyGoalState.isDailyGoalReached) DuoGreen else DuoOrange,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = if (dailyGoalState.isDailyGoalReached) {
                                    "¡Meta diaria alcanzada!"
                                } else {
                                    "Meta diaria de práctica"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (dailyGoalState.isDailyGoalReached) {
                                DuoGreen.copy(alpha = 0.18f)
                            } else {
                                DuoOrange.copy(alpha = 0.18f)
                            }
                        ) {
                            Text(
                                text = "${dailyGoalState.todayXpEarned}/${dailyGoalState.dailyXpTarget} EXP (${dailyGoalState.progressPercentage}%)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = if (dailyGoalState.isDailyGoalReached) DuoGreenDark else DuoOrangeDark,
                                modifier = Modifier
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .testTag("daily_goal_xp_status_text")
                            )
                        }
                    }

                    LinearProgressIndicator(
                        progress = { animatedGoalProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .testTag("daily_goal_progress_bar"),
                        color = if (dailyGoalState.isDailyGoalReached) DuoGreen else DuoOrange,
                        trackColor = DuoCardBorder,
                        strokeCap = StrokeCap.Round
                    )

                    // Goal Preset Selector Chips + Celebrate Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf(30 to "Suave", 50 to "Normal", 100 to "Intensa").forEach { (goalXp, label) ->
                                val isSelected = dailyGoalState.dailyXpTarget == goalXp
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onSelectDailyGoalXp(goalXp) },
                                    label = {
                                        Text(
                                            text = "$goalXp EXP",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = DuoOrange.copy(alpha = 0.20f),
                                        selectedLabelColor = DuoOrangeDark
                                    ),
                                    modifier = Modifier.testTag("daily_goal_chip_$goalXp")
                                )
                            }
                        }

                        FilledTonalButton(
                            onClick = onTriggerCelebration,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (dailyGoalState.isDailyGoalReached) {
                                    DuoGreen
                                } else {
                                    DuoOrange
                                },
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("trigger_daily_goal_celebration_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Celebration,
                                contentDescription = "Celebrar meta diaria",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (dailyGoalState.isDailyGoalReached) "¡Celebrar!" else "Meta ✨",
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // 3. Monthly Streak Calendar Grid
            AnimatedVisibility(
                visible = isCalendarExpanded && calendarDays.isNotEmpty(),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("streak_calendar_month_grid"),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Weekday Header Row (L, M, X, J, V, S, D)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("L", "M", "X", "J", "V", "S", "D").forEach { dayLabel ->
                            Box(
                                modifier = Modifier.width(38.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dayLabel,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Calendar Weeks Rows
                    val weeks = remember(calendarDays) { calendarDays.chunked(7) }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                            .padding(vertical = 10.dp, horizontal = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        weeks.forEach { weekRow ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                weekRow.forEach { day ->
                                    CalendarDayCell(
                                        day = day,
                                        isSelected = selectedDay?.fullDateString == day.fullDateString,
                                        onClick = { selectedDay = day }
                                    )
                                }
                            }
                        }
                    }

                    // Legend Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CalendarLegendItem(
                            color = DuoOrange,
                            label = "Racha activa"
                        )
                        CalendarLegendItem(
                            color = DuoYellow,
                            label = "Meta superada ⭐"
                        )
                        CalendarLegendItem(
                            color = DuoGreen,
                            label = "Hoy"
                        )
                    }

                    // Selected Day Inspector Pill
                    selectedDay?.let { inspected ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("selected_calendar_day_detail")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = when {
                                            inspected.isDailyGoalMet -> Icons.Default.Star
                                            inspected.isPracticed -> Icons.Default.LocalFireDepartment
                                            else -> Icons.Default.CalendarMonth
                                        },
                                        contentDescription = null,
                                        tint = when {
                                            inspected.isDailyGoalMet -> DuoYellow
                                            inspected.isPracticed -> DuoOrange
                                            else -> Color.Gray
                                        },
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column {
                                        Text(
                                            text = if (inspected.isToday) {
                                                "Hoy (${inspected.fullDateString})"
                                            } else {
                                                "Fecha: ${inspected.fullDateString}"
                                            },
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (inspected.isPracticed) {
                                                "${inspected.exercisesCompleted} ejercicios • +${inspected.xpEarned} EXP • ${inspected.accuracy}% precisión"
                                            } else if (inspected.isFuture) {
                                                "Día futuro — ¡mantén tu racha hasta llegar aquí!"
                                            } else {
                                                "Sin actividad registrada en esta fecha"
                                            },
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                if (inspected.isDailyGoalMet) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = DuoGreen.copy(alpha = 0.16f)
                                    ) {
                                        Text(
                                            text = "Meta ✅",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            color = DuoGreenDark,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
}

@Composable
private fun CalendarDayCell(
    day: CalendarDayStreak,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "calendar_today_pulse")
    val todayScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(950, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "calendar_today_scale"
    )

    val backgroundColor = when {
        !day.isCurrentMonth -> Color.Transparent
        day.isDailyGoalMet -> DuoYellow
        day.isPracticed -> DuoOrange
        day.isToday -> DuoGreen.copy(alpha = 0.18f)
        else -> Color.Transparent
    }

    val borderColor = when {
        isSelected -> DuoPurple
        day.isToday -> DuoGreen
        day.isDailyGoalMet -> DuoOrange
        day.isPracticed -> DuoOrangeDark
        else -> Color.Transparent
    }

    Box(
        modifier = Modifier
            .size(38.dp)
            .scale(if (day.isToday) todayScale else 1f)
            .clip(CircleShape)
            .background(backgroundColor)
            .then(
                if (borderColor != Color.Transparent) {
                    Modifier.border(
                        width = if (isSelected || day.isToday) 2.dp else 1.dp,
                        color = borderColor,
                        shape = CircleShape
                    )
                } else {
                    Modifier
                }
            )
            .clickable(enabled = day.isCurrentMonth) { onClick() }
            .testTag("calendar_day_cell_${day.fullDateString}"),
        contentAlignment = Alignment.Center
    ) {
        if (!day.isCurrentMonth) {
            Text(
                text = "${day.dayOfMonth}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.28f)
            )
        } else if (day.isPracticed) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${day.dayOfMonth}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = if (day.isDailyGoalMet) Color(0xFF4E342E) else Color.White
                )
                Icon(
                    imageVector = if (day.isDailyGoalMet) Icons.Default.Star else Icons.Default.LocalFireDepartment,
                    contentDescription = if (day.isDailyGoalMet) "Meta diaria cumplida" else "Día practicado",
                    tint = if (day.isDailyGoalMet) DuoOrangeDark else Color.White,
                    modifier = Modifier.size(11.dp)
                )
            }
        } else {
            Text(
                text = "${day.dayOfMonth}",
                fontSize = 12.sp,
                fontWeight = if (day.isToday) FontWeight.Black else FontWeight.SemiBold,
                color = when {
                    day.isToday -> DuoGreenDark
                    day.isFuture -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}

@Composable
private fun CalendarLegendItem(
    color: Color,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private data class GoalConfettiParticle(
    val angleRad: Float,
    val speedMultiplier: Float,
    val sizeDp: Float,
    val color: Color,
    val rotationSpeed: Float,
    val isRibbon: Boolean
)

/**
 * Animated Celebration Dialog/Overlay when the user reaches their Daily Goal ("Animación de celebración al alcanzar metas diarias").
 * Combines a multi-layered Canvas confetti/firework particle burst, rotating sunburst rays,
 * spring-animated trophy & flame badge, and daily goal reward summary.
 */
@Composable
fun DailyGoalCelebrationOverlay(
    state: DailyGoalCelebrationState,
    onReplayCelebration: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = state.isCelebrationVisible,
        enter = fadeIn(tween(220)) + scaleIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            initialScale = 0.82f
        ),
        exit = fadeOut(tween(180)) + scaleOut(targetScale = 0.9f)
    ) {
        val burstProgress = remember(state.celebrationTriggerCount) { Animatable(0f) }
        val badgeScale = remember(state.celebrationTriggerCount) { Animatable(0.4f) }

        LaunchedEffect(state.celebrationTriggerCount) {
            burstProgress.snapTo(0f)
            burstProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1600, easing = FastOutSlowInEasing)
            )
        }

        LaunchedEffect(state.celebrationTriggerCount) {
            badgeScale.snapTo(0.4f)
            badgeScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }

        val infiniteTransition = rememberInfiniteTransition(label = "daily_goal_celebration_infinite")
        val rayRotation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 8000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "sunburst_rotation"
        )
        val trophyPulse by infiniteTransition.animateFloat(
            initialValue = 0.96f,
            targetValue = 1.07f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 850, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "trophy_pulse"
        )

        val confettiColors = remember {
            listOf(
                DuoYellow,
                DuoOrange,
                DuoGreen,
                DuoBlue,
                DuoPurple,
                Color(0xFFFF4081)
            )
        }

        val particles = remember {
            List(40) { index ->
                val angle = (index.toFloat() / 40f) * (2f * PI.toFloat()) +
                    ((index % 5) * 0.08f)
                GoalConfettiParticle(
                    angleRad = angle,
                    speedMultiplier = 0.45f + (index % 7) * 0.11f,
                    sizeDp = 6f + (index % 4) * 2.5f,
                    color = confettiColors[index % confettiColors.size],
                    rotationSpeed = 120f + (index % 6) * 65f,
                    isRibbon = index % 2 == 0
                )
            }
        }

        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.62f))
                    .padding(horizontal = 22.dp)
                    .testTag("daily_goal_celebration_overlay"),
                contentAlignment = Alignment.Center
            ) {
                // Background Confetti & Firework Canvas
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("daily_goal_celebration_canvas")
                ) {
                    val center = Offset(size.width / 2f, size.height * 0.38f)
                    val maxRadius = size.minDimension * 0.56f
                    val progress = burstProgress.value

                    // Expanding golden shockwave rings
                    val ringAlpha = (1f - progress).coerceIn(0f, 1f)
                    drawCircle(
                        color = DuoYellow.copy(alpha = ringAlpha * 0.45f),
                        radius = maxRadius * progress * 0.95f,
                        center = center,
                        style = Stroke(width = 4.dp.toPx())
                    )
                    drawCircle(
                        color = DuoOrange.copy(alpha = ringAlpha * 0.30f),
                        radius = maxRadius * (progress * 0.7f),
                        center = center,
                        style = Stroke(width = 2.5.dp.toPx())
                    )

                    // Confetti & Star burst particles
                    particles.forEach { p ->
                        val distance = maxRadius * p.speedMultiplier * progress
                        val gravityDrop = (progress * progress) * 95.dp.toPx()
                        val px = center.x + cos(p.angleRad) * distance
                        val py = center.y + sin(p.angleRad) * distance + gravityDrop
                        val alpha = (1f - (progress * 0.72f)).coerceIn(0f, 1f)
                        val pSize = p.sizeDp.dp.toPx()

                        withTransform({
                            rotate(
                                degrees = rayRotation + progress * p.rotationSpeed,
                                pivot = Offset(px, py)
                            )
                        }) {
                            if (p.isRibbon) {
                                drawRoundRect(
                                    color = p.color.copy(alpha = alpha),
                                    topLeft = Offset(px - pSize / 2f, py - pSize),
                                    size = Size(pSize, pSize * 2f),
                                    cornerRadius = CornerRadius(3f, 3f)
                                )
                            } else {
                                drawCircle(
                                    color = p.color.copy(alpha = alpha),
                                    radius = pSize * 0.65f,
                                    center = Offset(px, py)
                                )
                            }
                        }
                    }
                }

                // Celebration Card
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(badgeScale.value),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Animated Trophy + Sunburst Medallion
                        Box(
                            modifier = Modifier.size(116.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val c = Offset(size.width / 2f, size.height / 2f)
                                val outerR = size.minDimension / 2f
                                for (i in 0 until 12) {
                                    val angleDeg = rayRotation + i * 30f
                                    val rad = (angleDeg * PI / 180.0).toFloat()
                                    val start = Offset(
                                        c.x + cos(rad) * (outerR * 0.55f),
                                        c.y + sin(rad) * (outerR * 0.55f)
                                    )
                                    val end = Offset(
                                        c.x + cos(rad) * outerR,
                                        c.y + sin(rad) * outerR
                                    )
                                    drawLine(
                                        color = DuoYellow.copy(alpha = 0.45f),
                                        start = start,
                                        end = end,
                                        strokeWidth = 5.dp.toPx(),
                                        cap = StrokeCap.Round
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(84.dp)
                                    .scale(trophyPulse)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color(0xFFFFE082),
                                                DuoYellow,
                                                DuoOrange
                                            )
                                        )
                                    )
                                    .border(3.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = "Trofeo de meta diaria",
                                    tint = Color.White,
                                    modifier = Modifier.size(46.dp)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(50),
                            color = DuoGreen.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, DuoGreen.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = DuoGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "¡META DIARIA ALCANZADA!",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = DuoGreenDark,
                                    letterSpacing = 0.6.sp
                                )
                            }
                        }

                        Text(
                            text = "¡Excelente constancia hoy!",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.testTag("daily_goal_celebration_title")
                        )

                        Text(
                            text = "Has completado tu objetivo diario de pronunciación y asegurado tu racha de ${state.streakDaysCount} días seguidos.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Reward Stats Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CelebrationStatChip(
                                emoji = "⚡",
                                value = "+${ maxOf(state.todayXpEarned, state.dailyXpTarget) } EXP",
                                label = "Meta de Hoy",
                                accentColor = DuoOrange,
                                modifier = Modifier.weight(1f)
                            )
                            CelebrationStatChip(
                                emoji = "🔥",
                                value = "${state.streakDaysCount} días",
                                label = "Racha Activa",
                                accentColor = DuoYellow,
                                modifier = Modifier.weight(1f)
                            )
                            CelebrationStatChip(
                                emoji = "💎",
                                value = "+${state.bonusGemsAwarded}",
                                label = "Bono Gemas",
                                accentColor = DuoBlue,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Action Buttons
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onReplayCelebration,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .testTag("replay_daily_goal_celebration_button"),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.5.dp, DuoOrange)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Replay,
                                    contentDescription = "Repetir animación",
                                    tint = DuoOrange,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Celebrar",
                                    fontWeight = FontWeight.Black,
                                    color = DuoOrange
                                )
                            }

                            Button(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(50.dp)
                                    .testTag("dismiss_daily_goal_celebration_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DuoGreen,
                                    contentColor = Color.White
                                )
                            ) {
                                Text(
                                    text = "¡CONTINUAR!",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CelebrationStatChip(
    emoji: String,
    value: String,
    label: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = accentColor.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.28f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(text = emoji, fontSize = 18.sp)
            Text(
                text = value,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}
