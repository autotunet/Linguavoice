package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.repository.StreakInfo
import com.example.data.repository.WeekDayStreak
import com.example.ui.theme.DuoCardBorder
import com.example.ui.theme.DuoGreen
import com.example.ui.theme.DuoOrange
import com.example.ui.theme.DuoOrangeDark
import com.example.ui.theme.DuoYellow

@Composable
fun DailyStreakCard(
    streakInfo: StreakInfo,
    modifier: Modifier = Modifier
) {
    var showStreakDetailDialog by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "flame_pulse")
    val flameScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flame_scale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("daily_streak_card")
            .clickable { showStreakDetailDialog = true },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, if (streakInfo.practicedToday) DuoOrange.copy(alpha = 0.4f) else DuoCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header with animated flame and streak count
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
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
                            contentDescription = "Fuego de racha",
                            tint = Color.White,
                            modifier = Modifier
                                .size(34.dp)
                                .scale(flameScale)
                        )
                    }

                    Column {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${streakInfo.currentStreak}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = DuoOrange
                            )
                            Text(
                                text = " DÍAS DE RACHA",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = DuoOrange,
                                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
                                letterSpacing = 0.5.sp
                            )
                        }

                        Text(
                            text = if (streakInfo.practicedToday) {
                                "¡Racha de hoy completada! 🔥"
                            } else {
                                "¡Practica hoy para mantener tu fuego activo!"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (streakInfo.practicedToday) DuoGreen else Color(0xFF777777)
                        )
                    }
                }

                // Status Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (streakInfo.practicedToday) Color(0xFFE8F5E9) else Color(0xFFFFF3CD))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (streakInfo.practicedToday) "Activo ✅" else "Pendiente ⏳",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (streakInfo.practicedToday) Color(0xFF2E7D32) else Color(0xFFB78103)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Week Days Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                streakInfo.currentWeekDays.forEach { weekDay ->
                    WeekDayCircle(weekDay = weekDay)
                }
            }
        }
    }

    if (showStreakDetailDialog) {
        StreakDetailDialog(
            streakInfo = streakInfo,
            onDismiss = { showStreakDetailDialog = false }
        )
    }
}

@Composable
fun WeekDayCircle(weekDay: WeekDayStreak) {
    val infiniteTransition = rememberInfiniteTransition(label = "today_ring")
    val ringPulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring_pulse"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = weekDay.dayName,
            fontSize = 12.sp,
            fontWeight = if (weekDay.isToday) FontWeight.Black else FontWeight.Bold,
            color = if (weekDay.isToday) DuoOrange else MaterialTheme.colorScheme.onSurfaceVariant
        )

        Box(
            modifier = Modifier
                .size(36.dp),
            contentAlignment = Alignment.Center
        ) {
            if (weekDay.isToday && !weekDay.isPracticed) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .scale(ringPulse)
                        .clip(CircleShape)
                        .border(2.dp, DuoOrange.copy(alpha = 0.6f), CircleShape)
                )
            }

            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            weekDay.isPracticed -> DuoOrange
                            weekDay.isToday -> Color(0xFFFFF3CD)
                            weekDay.isFuture -> Color.Transparent
                            else -> Color(0xFFEEEEEE)
                        }
                    )
                    .border(
                        width = if (weekDay.isToday) 2.dp else 1.dp,
                        color = when {
                            weekDay.isPracticed -> DuoOrange
                            weekDay.isToday -> DuoOrange
                            else -> DuoCardBorder
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                when {
                    weekDay.isPracticed -> {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Día completado",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    weekDay.isToday -> {
                        Text(
                            text = "Hoy",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = DuoOrange
                        )
                    }
                    weekDay.isFuture -> {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray)
                        )
                    }
                    else -> {
                        Text(
                            text = "—",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StreakDetailDialog(
    streakInfo: StreakInfo,
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
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFFFFD54F), DuoOrange, DuoOrangeDark)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Text(
                    text = "${streakInfo.currentStreak} Días Consecutivos",
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp,
                    color = DuoOrange
                )

                Text(
                    text = "Tu progreso se almacena en Room. Revisa tu actividad semanal y el avance de tu precisión de voz:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                // Weekly Activity & Accuracy Trend Bar Chart
                WeeklyActivityChart(
                    streakInfo = streakInfo,
                    modifier = Modifier.fillMaxWidth()
                )

                // Stats Grid
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Racha Actual", fontSize = 11.sp, color = Color.Gray)
                        Text(
                            text = "${streakInfo.currentStreak} días",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = DuoOrange
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Mejor Racha", fontSize = 11.sp, color = Color.Gray)
                        Text(
                            text = "${streakInfo.bestStreak} días",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = DuoYellow
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Días Totales", fontSize = 11.sp, color = Color.Gray)
                        Text(
                            text = "${streakInfo.totalDaysPracticed}",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = DuoGreen
                        )
                    }
                }

                DuoButton(
                    text = "¡ENTENDIDO!",
                    onClick = onDismiss,
                    colorStyle = DuoButtonColor.ORANGE,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
