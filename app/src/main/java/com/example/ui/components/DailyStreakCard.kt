package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextOverflow
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
    isHidden: Boolean = false,
    onHide: () -> Unit = {},
    onOpenDetail: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showInternalDetailDialog by remember { mutableStateOf(false) }
    // When daily streak is completed, start compact/collapsed so it takes minimal space
    var isExpanded by remember(streakInfo.practicedToday) {
        mutableStateOf(!streakInfo.practicedToday)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "flame_pulse")
    val flameScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flame_scale"
    )

    AnimatedVisibility(
        visible = !isHidden,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .testTag("daily_streak_card")
                .clickable {
                    if (onOpenDetail != null) {
                        onOpenDetail()
                    } else {
                        showInternalDetailDialog = true
                    }
                },
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                if (streakInfo.practicedToday) DuoOrange.copy(alpha = 0.35f) else DuoCardBorder
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // Compact single-line header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
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
                                    .size(20.dp)
                                    .scale(flameScale)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "${streakInfo.currentStreak} DÍAS DE RACHA",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = DuoOrange,
                                    letterSpacing = 0.3.sp
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (streakInfo.practicedToday) Color(0xFFE8F5E9)
                                            else Color(0xFFFFF3CD)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (streakInfo.practicedToday) "Completada ✅" else "Pendiente ⏳",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (streakInfo.practicedToday) Color(0xFF2E7D32) else Color(0xFFB78103)
                                    )
                                }
                            }

                            Text(
                                text = if (streakInfo.practicedToday) {
                                    "¡Racha del día completada! Toca para ver detalles"
                                } else {
                                    "¡Practica hoy para mantener tu fuego activo!"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (streakInfo.practicedToday) DuoGreen else Color(0xFF777777),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { isExpanded = !isExpanded },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("toggle_streak_expand_button")
                        ) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = if (isExpanded) "Contraer racha" else "Expandir racha",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = onHide,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("hide_streak_card_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Ocultar racha de hoy",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Collapsible Week Days Bar
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
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

    if (showInternalDetailDialog) {
        StreakDetailDialog(
            streakInfo = streakInfo,
            onDismiss = { showInternalDetailDialog = false }
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
    onDismiss: () -> Unit,
    onDismissAndHideBanner: (() -> Unit)? = null
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("streak_emergent_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
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
                        modifier = Modifier.size(38.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (streakInfo.practicedToday) Color(0xFFE8F5E9) else Color(0xFFFFF3CD))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (streakInfo.practicedToday) "¡Racha del día completada! ✅" else "Racha diaria activa 🔥",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (streakInfo.practicedToday) Color(0xFF2E7D32) else Color(0xFFB78103)
                    )
                }

                Text(
                    text = "${streakInfo.currentStreak} Días Consecutivos",
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp,
                    color = DuoOrange
                )

                if (streakInfo.currentWeekDays.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        streakInfo.currentWeekDays.forEach { weekDay ->
                            WeekDayCircle(weekDay = weekDay)
                        }
                    }
                }

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
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Racha Actual", fontSize = 11.sp, color = Color.Gray)
                        Text(
                            text = "${streakInfo.currentStreak} días",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = DuoOrange
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Mejor Racha", fontSize = 11.sp, color = Color.Gray)
                        Text(
                            text = "${streakInfo.bestStreak} días",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = DuoYellow
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Días Totales", fontSize = 11.sp, color = Color.Gray)
                        Text(
                            text = "${streakInfo.totalDaysPracticed}",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = DuoGreen
                        )
                    }
                }

                DuoButton(
                    text = "¡ENTENDIDO!",
                    onClick = onDismiss,
                    colorStyle = DuoButtonColor.ORANGE,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "dismiss_streak_popup_button"
                )

                if (onDismissAndHideBanner != null) {
                    TextButton(
                        onClick = onDismissAndHideBanner,
                        modifier = Modifier.testTag("dismiss_and_hide_streak_banner_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Ocultar tarjeta de racha por hoy",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}
