package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.StreakInfo
import com.example.data.repository.WeekDayStreak
import com.example.ui.theme.DuoBlue
import com.example.ui.theme.DuoCardBorder
import com.example.ui.theme.DuoGreen
import com.example.ui.theme.DuoGreenDark
import com.example.ui.theme.DuoGreenLight
import com.example.ui.theme.DuoOrange
import com.example.ui.theme.DuoPurple
import com.example.ui.theme.DuoRed
import com.example.ui.theme.DuoYellow

enum class ChartViewMode(val label: String, val icon: String) {
    ACTIVITY("Actividad", "🎙️"),
    ACCURACY("Precisión", "🎯"),
    DUAL("Tendencia", "📈")
}

@Composable
fun WeeklyActivityChart(
    streakInfo: StreakInfo,
    modifier: Modifier = Modifier
) {
    var selectedMode by remember { mutableStateOf(ChartViewMode.ACTIVITY) }
    val days = streakInfo.currentWeekDays

    // Default selected day index: today if in week, or the last practiced day
    val todayIndex = remember(days) {
        val idx = days.indexOfFirst { it.isToday }
        if (idx >= 0) idx else (days.indexOfLast { it.isPracticed }.takeIf { it >= 0 } ?: 0)
    }
    var selectedDayIndex by remember { mutableIntStateOf(todayIndex) }
    val selectedDay = days.getOrNull(selectedDayIndex) ?: days.firstOrNull()

    // Daily target baseline
    val targetGoalExercises = 3
    val targetGoalAccuracy = 85

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("weekly_activity_chart"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, DuoCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header: Title and Trend badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "ACTIVIDAD SEMANAL",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Progreso de Pronunciación",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Trend badge
                val delta = streakInfo.accuracyTrendDelta
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (delta >= 0) DuoGreenLight.copy(alpha = 0.5f) else DuoRed.copy(alpha = 0.12f)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = if (delta >= 0) DuoGreenDark else DuoRed,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = if (delta >= 0) "+$delta% precisión" else "$delta% precisión",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        color = if (delta >= 0) DuoGreenDark else DuoRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Mode Selector Tabs (Actividad / Precisión / Tendencia)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ChartViewMode.values().forEach { mode ->
                    val isSelected = selectedMode == mode
                    val tabColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                        animationSpec = tween(200),
                        label = "tab_bg"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(tabColor)
                            .clickable { selectedMode = mode }
                            .padding(vertical = 8.dp)
                            .testTag("chart_mode_${mode.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(text = mode.icon, fontSize = 13.sp)
                            Text(
                                text = mode.label,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                fontSize = 12.sp,
                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick KPI Banner (Total exercises & Average Accuracy)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DuoBlue.copy(alpha = 0.08f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
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
                        text = "${streakInfo.weeklyTotalExercises} ejercicios esta semana",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = DuoBlue
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = DuoGreenDark,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${streakInfo.weeklyAverageAccuracy}% acierto medio",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = DuoGreenDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Main Bar Chart visualization
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                // Background goal dashed line
                GoalReferenceLine(
                    mode = selectedMode,
                    targetExercises = targetGoalExercises,
                    targetAccuracy = targetGoalAccuracy
                )

                // The 7 Day Columns
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val maxExercises = maxOf(5, days.maxOfOrNull { it.exercisesCompleted } ?: 5)

                    days.forEachIndexed { index, day ->
                        DayBarColumn(
                            day = day,
                            mode = selectedMode,
                            maxExercises = maxExercises,
                            targetExercises = targetGoalExercises,
                            isSelected = index == selectedDayIndex,
                            onClick = { selectedDayIndex = index },
                            modifier = Modifier.testTag("chart_day_bar_$index")
                        )
                    }
                }

                // Smooth trend curve overlay in ACCURACY & DUAL modes
                if (selectedMode == ChartViewMode.ACCURACY || selectedMode == ChartViewMode.DUAL) {
                    AccuracyTrendCurveOverlay(
                        days = days,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Selected Day Detail Pill
            if (selectedDay != null) {
                DayDetailCard(
                    day = selectedDay,
                    targetExercises = targetGoalExercises
                )
            }
        }
    }
}

@Composable
private fun GoalReferenceLine(
    mode: ChartViewMode,
    targetExercises: Int,
    targetAccuracy: Int
) {
    val goalRatio = when (mode) {
        ChartViewMode.ACTIVITY -> (targetExercises.toFloat() / 5f).coerceIn(0.2f, 0.9f)
        ChartViewMode.ACCURACY -> (targetAccuracy.toFloat() / 100f)
        ChartViewMode.DUAL -> (targetAccuracy.toFloat() / 100f)
    }

    val goalLabel = when (mode) {
        ChartViewMode.ACTIVITY -> "Meta: $targetExercises"
        ChartViewMode.ACCURACY, ChartViewMode.DUAL -> "Meta: $targetAccuracy%"
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 28.dp, top = 22.dp)
    ) {
        val y = size.height * (1f - goalRatio)
        val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

        drawLine(
            color = Color.LightGray.copy(alpha = 0.8f),
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 2.dp.toPx(),
            pathEffect = pathEffect
        )
    }

    // Goal tag on top right
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 28.dp, top = 22.dp),
        contentAlignment = Alignment.TopEnd
    ) {
        Text(
            text = goalLabel,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                    RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 4.dp, vertical = 1.dp)
        )
    }
}

@Composable
private fun DayBarColumn(
    day: WeekDayStreak,
    mode: ChartViewMode,
    maxExercises: Int,
    targetExercises: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val barRatio = when (mode) {
        ChartViewMode.ACTIVITY -> {
            if (maxExercises > 0) (day.exercisesCompleted.toFloat() / maxExercises.toFloat()).coerceIn(0f, 1f) else 0f
        }
        ChartViewMode.ACCURACY -> {
            if (day.isPracticed && day.accuracy > 0) (day.accuracy.toFloat() / 100f).coerceIn(0f, 1f) else 0f
        }
        ChartViewMode.DUAL -> {
            val actWeight = if (maxExercises > 0) (day.exercisesCompleted.toFloat() / maxExercises.toFloat()) * 0.5f else 0f
            val accWeight = if (day.isPracticed && day.accuracy > 0) (day.accuracy.toFloat() / 100f) * 0.5f else 0f
            (actWeight + accWeight).coerceIn(0f, 1f)
        }
    }

    val animatedHeightRatio by animateFloatAsState(
        targetValue = barRatio,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "bar_height"
    )

    // Bar colors based on performance
    val barColor = when (mode) {
        ChartViewMode.ACTIVITY -> {
            when {
                day.exercisesCompleted >= targetExercises -> DuoGreen
                day.exercisesCompleted > 0 -> DuoBlue
                day.isFuture -> Color.LightGray.copy(alpha = 0.35f)
                else -> Color.LightGray.copy(alpha = 0.45f)
            }
        }
        ChartViewMode.ACCURACY, ChartViewMode.DUAL -> {
            when {
                !day.isPracticed || day.accuracy == 0 -> Color.LightGray.copy(alpha = 0.35f)
                day.accuracy >= 90 -> DuoGreen
                day.accuracy >= 75 -> DuoOrange
                else -> DuoRed
            }
        }
    }

    val displayValue = when (mode) {
        ChartViewMode.ACTIVITY -> if (day.exercisesCompleted > 0) "${day.exercisesCompleted}" else ""
        ChartViewMode.ACCURACY -> if (day.isPracticed && day.accuracy > 0) "${day.accuracy}%" else ""
        ChartViewMode.DUAL -> if (day.isPracticed) "${day.accuracy}%" else ""
    }

    Column(
        modifier = modifier
            .width(36.dp)
            .fillMaxHeight()
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        // Value label above bar
        Text(
            text = displayValue,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) DuoGreenDark else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.height(16.dp)
        )

        Spacer(modifier = Modifier.height(3.dp))

        // Vertical Bar Container
        Box(
            modifier = Modifier
                .width(if (isSelected) 18.dp else 14.dp)
                .height(100.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.LightGray.copy(alpha = 0.2f))
                .then(
                    if (isSelected) Modifier.border(2.dp, DuoOrange, RoundedCornerShape(10.dp))
                    else Modifier
                ),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Filled portion
            if (animatedHeightRatio > 0.05f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(animatedHeightRatio)
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    barColor.copy(alpha = 0.9f),
                                    barColor
                                )
                            )
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Day Name and Today indicator
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(
                    when {
                        day.isToday -> DuoGreen
                        isSelected -> DuoOrange.copy(alpha = 0.25f)
                        else -> Color.Transparent
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = day.dayName,
                fontSize = 11.sp,
                fontWeight = if (day.isToday || isSelected) FontWeight.Black else FontWeight.Bold,
                color = if (day.isToday) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun AccuracyTrendCurveOverlay(
    days: List<WeekDayStreak>,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .padding(bottom = 28.dp, top = 22.dp)
    ) {
        val totalDays = days.size
        if (totalDays < 2) return@Canvas

        val stepX = size.width / totalDays
        val practicedPoints = mutableListOf<Offset>()

        days.forEachIndexed { i, day ->
            if (day.isPracticed && day.accuracy > 0) {
                val cx = (i * stepX) + (stepX / 2f)
                val ratio = (day.accuracy.toFloat() / 100f).coerceIn(0f, 1f)
                val cy = size.height * (1f - ratio)
                practicedPoints.add(Offset(cx, cy))
            }
        }

        if (practicedPoints.size >= 2) {
            val path = Path()
            path.moveTo(practicedPoints.first().x, practicedPoints.first().y)

            for (k in 1 until practicedPoints.size) {
                val pPrev = practicedPoints[k - 1]
                val pCurr = practicedPoints[k]
                val midX = (pPrev.x + pCurr.x) / 2f
                path.cubicTo(
                    midX, pPrev.y,
                    midX, pCurr.y,
                    pCurr.x, pCurr.y
                )
            }

            // Draw glowing trend curve
            drawPath(
                path = path,
                color = DuoPurple.copy(alpha = 0.85f),
                style = Stroke(
                    width = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )

            // Draw trend dots
            practicedPoints.forEach { point ->
                drawCircle(
                    color = DuoPurple,
                    radius = 4.5.dp.toPx(),
                    center = point
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.dp.toPx(),
                    center = point
                )
            }
        }
    }
}

@Composable
private fun DayDetailCard(
    day: WeekDayStreak,
    targetExercises: Int
) {
    val isGoalMet = day.exercisesCompleted >= targetExercises

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("day_detail_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (day.isToday) DuoGreen.copy(alpha = 0.5f) else DuoCardBorder
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Día ${day.dayName} (${day.fullDateString})",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (day.isToday) {
                        Text(
                            text = "HOY",
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp,
                            color = DuoGreen,
                            modifier = Modifier
                                .background(DuoGreenLight, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                if (day.isPracticed) {
                    Text(
                        text = "🎙️ ${day.exercisesCompleted} ejercicios • ⚡ +${day.xpEarned} EXP • 🎯 ${day.accuracy}% precisión",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else if (day.isFuture) {
                    Text(
                        text = "Próximo día programado en tu racha",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "Sin práctica registrada este día",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Status Badge
            if (day.isPracticed) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (isGoalMet) DuoGreenLight else DuoOrange.copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isGoalMet) Icons.Default.CheckCircle else Icons.Default.Star,
                        contentDescription = null,
                        tint = if (isGoalMet) DuoGreenDark else DuoOrange,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
