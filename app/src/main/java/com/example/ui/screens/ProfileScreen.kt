package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.AdMobBanner
import com.example.data.model.LevelSystem
import com.example.data.model.UserProfile
import com.example.data.repository.StreakInfo
import com.example.ui.components.DailyStreakCard
import com.example.ui.components.DuoButton
import com.example.ui.components.DuoButtonColor
import com.example.ui.components.LevelProgressBar
import com.example.ui.components.MascotBubble
import com.example.ui.components.WeeklyActivityChart
import com.example.ui.theme.DuoBlue
import com.example.ui.theme.DuoCardBorder
import com.example.ui.theme.DuoGreen
import com.example.ui.theme.DuoOrange
import com.example.ui.theme.DuoRed

@Composable
fun ProfileScreen(
    userProfile: UserProfile,
    streakInfo: StreakInfo,
    onChangeLanguage: () -> Unit,
    onRefillHearts: () -> Unit,
    modifier: Modifier = Modifier
) {
    val levelInfo = remember(userProfile.totalXp) {
        LevelSystem.calculateLevelInfo(userProfile.totalXp)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(DuoGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🦜", fontSize = 42.sp)
                    }

                    Text(
                        text = "Estudiante Políglota",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(top = 12.dp)
                    )

                    Text(
                        text = "Nivel ${levelInfo.level} • ${levelInfo.levelTitle} ${levelInfo.badgeEmoji}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = DuoOrange,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Row(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.5.dp, DuoCardBorder, RoundedCornerShape(14.dp))
                            .clickable { onChangeLanguage() }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = userProfile.selectedLanguage.flagEmoji, fontSize = 18.sp)
                        Text(
                            text = "Aprendiendo ${userProfile.selectedLanguage.displayName}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = DuoBlue
                        )
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Cambiar",
                            tint = DuoBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Level & Tier Progress
        item {
            LevelProgressBar(
                levelInfo = levelInfo,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        // Weekly Practice Activity & Accuracy Trend Bar Chart
        item {
            WeeklyActivityChart(
                streakInfo = streakInfo,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        // Stats Grid
        item {
            Text(
                text = "ESTADÍSTICAS",
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    icon = Icons.Default.LocalFireDepartment,
                    iconColor = DuoOrange,
                    label = "Racha de días",
                    value = "${userProfile.streakDays}",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    icon = Icons.Default.Star,
                    iconColor = DuoOrange,
                    label = "Total EXP",
                    value = "${userProfile.totalXp}",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    icon = Icons.Default.GraphicEq,
                    iconColor = DuoGreen,
                    label = "Precisión de voz",
                    value = "${userProfile.accuracyAverage}%",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    icon = Icons.Default.Favorite,
                    iconColor = DuoRed,
                    label = "Vidas restantes",
                    value = "${userProfile.hearts}/5",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Refill hearts button if hearts < 5
        if (userProfile.hearts < 5) {
            item {
                DuoButton(
                    text = "Recargar Vidas (+5)",
                    onClick = onRefillHearts,
                    colorStyle = DuoButtonColor.RED,
                    modifier = Modifier.fillMaxWidth(),
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    testTag = "refill_hearts_button"
                )
            }
        }

        // Achievements / Badges
        item {
            Text(
                text = "LOGROS DESBLOQUEADOS",
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(userProfile.unlockedBadges) { badge ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(if (badge.isUnlocked) DuoOrange.copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = if (badge.isUnlocked) DuoOrange else Color.Gray,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 14.dp)
                    ) {
                        Text(
                            text = badge.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = badge.description,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    if (badge.isUnlocked) {
                        Text(text = "✅", fontSize = 16.sp)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun StatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = value,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
