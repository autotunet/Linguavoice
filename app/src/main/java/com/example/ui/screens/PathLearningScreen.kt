package com.example.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WavingHand
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ads.AdMobBanner
import com.example.data.model.LessonModule
import com.example.data.model.UserLevelInfo
import com.example.data.model.UserProfile
import com.example.data.repository.ExerciseRepository
import com.example.data.repository.StreakInfo
import com.example.ui.components.DailyStreakCard
import com.example.ui.components.DuoButton
import com.example.ui.components.DuoButtonColor
import com.example.ui.components.LevelProgressBar
import com.example.ui.components.MascotBubble
import com.example.ui.theme.DuoBlue
import com.example.ui.theme.DuoCardBorder
import com.example.ui.theme.DuoGreen
import com.example.ui.theme.DuoGreenDark
import com.example.ui.theme.DuoGreenLight
import com.example.ui.theme.DuoOrange
import com.example.ui.theme.DuoYellow

@Composable
fun PathLearningScreen(
    userProfile: UserProfile,
    streakInfo: StreakInfo,
    levelInfo: UserLevelInfo,
    onStartLesson: (LessonModule) -> Unit,
    modifier: Modifier = Modifier
) {
    val modules = remember(userProfile.selectedLanguage) {
        ExerciseRepository.getModulesForLanguage(userProfile.selectedLanguage)
    }

    var selectedModuleForSheet by remember { mutableStateOf<LessonModule?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Prominent Daily Streak Tracker Card from Room Database
            item {
                DailyStreakCard(
                    streakInfo = streakInfo,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            // Prominent Level & XP Progress Tracker
            item {
                LevelProgressBar(
                    levelInfo = levelInfo,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Unit Header Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = DuoGreen
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "SECCIÓN 1 • HABLA FLUIDA",
                                    color = DuoGreenLight,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Entrena tu pronunciación en ${userProfile.selectedLanguage.displayName}",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Text(
                                text = userProfile.selectedLanguage.flagEmoji,
                                fontSize = 32.sp
                            )
                        }

                        Text(
                            text = "¡Habla con el micrófono y recibe retroalimentación instantánea palabra por palabra!",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }

            // Mascot Cheer
            item {
                MascotBubble(
                    message = "¡Hola! Toca cualquier nodo para hablar y ganar EXP hoy.",
                    subMessage = "Pronunciar en voz alta activa las conexiones neuronales del idioma.",
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Path Nodes (Winding left and right)
            itemsIndexed(modules) { index, module ->
                val isCompleted = userProfile.completedLessonIds.contains(module.id)
                val isLevelUnlocked = levelInfo.level >= module.requiredLevel
                val isUnlocked = isLevelUnlocked && (index == 0 || userProfile.completedLessonIds.contains(modules.getOrNull(index - 1)?.id))

                // Sinusoidal horizontal offset for the Duolingo winding path
                val horizontalOffset = when (index % 4) {
                    0 -> 0.dp
                    1 -> (-48).dp
                    2 -> 0.dp
                    3 -> 48.dp
                    else -> 0.dp
                }

                PathNodeItem(
                    module = module,
                    nodeNumber = index + 1,
                    isCompleted = isCompleted,
                    isUnlocked = isUnlocked,
                    isLevelUnlocked = isLevelUnlocked,
                    horizontalOffset = horizontalOffset,
                    onClick = {
                        if (isUnlocked) {
                            selectedModuleForSheet = module
                        }
                    }
                )

                // Small connector dots between nodes
                if (index < modules.size - 1) {
                    Column(
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .offset(x = horizontalOffset * 0.5f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        repeat(3) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(DuoCardBorder)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Module Selection Dialog / Sheet
        selectedModuleForSheet?.let { module ->
            ModuleDetailDialog(
                module = module,
                onDismiss = { selectedModuleForSheet = null },
                onStart = {
                    val m = module
                    selectedModuleForSheet = null
                    onStartLesson(m)
                }
            )
        }
    }
}

@Composable
fun PathNodeItem(
    module: LessonModule,
    nodeNumber: Int,
    isCompleted: Boolean,
    isUnlocked: Boolean,
    isLevelUnlocked: Boolean = true,
    horizontalOffset: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "beacon")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val nodeColor = when {
        isCompleted -> DuoYellow
        isUnlocked -> DuoGreen
        else -> Color(0xFFE5E5E5)
    }

    val shadowColor = when {
        isCompleted -> Color(0xFFD69A00)
        isUnlocked -> DuoGreenDark
        else -> Color(0xFFCECECE)
    }

    Column(
        modifier = Modifier
            .offset(x = horizontalOffset)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .testTag("path_node_${module.id}")
                .clickable(enabled = isUnlocked) { onClick() },
            contentAlignment = Alignment.Center
        ) {
            // Animated halo for the current active unlocked node
            if (isUnlocked && !isCompleted) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(DuoGreen.copy(alpha = 0.25f))
                )
            }

            // 3D chunky circle button
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(shadowColor),
                contentAlignment = Alignment.TopCenter
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .height(70.dp)
                        .clip(CircleShape)
                        .background(nodeColor),
                    contentAlignment = Alignment.Center
                ) {
                    val iconVector = when {
                        !isUnlocked -> Icons.Default.Lock
                        isCompleted -> Icons.Default.Check
                        module.iconName == "restaurant" -> Icons.Default.Restaurant
                        module.iconName == "flight" -> Icons.Default.Flight
                        module.iconName == "local_fire_department" -> Icons.Default.LocalFireDepartment
                        else -> Icons.Default.Mic
                    }

                    Icon(
                        imageVector = iconVector,
                        contentDescription = module.title,
                        tint = if (isUnlocked) Color.White else Color(0xFFAAAAAA),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            // Crown or Star badge for completed
            if (isCompleted) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 6.dp, y = (-4).dp)
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(DuoOrange)
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Completado",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Text(
            text = module.title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isUnlocked) MaterialTheme.colorScheme.onBackground else Color.Gray,
            modifier = Modifier
                .padding(top = 8.dp)
                .width(130.dp),
            textAlign = TextAlign.Center,
            maxLines = 2
        )

        if (!isLevelUnlocked) {
            Text(
                text = "🔒 Nivel ${module.requiredLevel}",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = DuoOrange,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun ModuleDetailDialog(
    module: LessonModule,
    onDismiss: () -> Unit,
    onStart: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(DuoGreenLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = DuoGreen,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Text(
                    text = module.title,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = module.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Ejercicios", fontSize = 12.sp, color = Color.Gray)
                        Text(
                            text = "${module.exercises.size}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Recompensa", fontSize = 12.sp, color = Color.Gray)
                        Text(
                            text = "+${module.exercises.size * 15} EXP",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = DuoOrange
                        )
                    }
                }

                DuoButton(
                    text = "EMPEZAR LECCIÓN",
                    onClick = onStart,
                    colorStyle = DuoButtonColor.GREEN,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "dialog_start_lesson"
                )
            }
        }
    }
}
