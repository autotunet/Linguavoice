package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WavingHand
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.DifficultyTier
import com.example.data.model.LessonModule
import com.example.data.model.LevelSystem
import com.example.ui.theme.DuoBlue
import com.example.ui.theme.DuoBlueDark
import com.example.ui.theme.DuoBlueLight
import com.example.ui.theme.DuoCardBorder
import com.example.ui.theme.DuoGreen
import com.example.ui.theme.DuoGreenDark
import com.example.ui.theme.DuoGreenLight
import com.example.ui.theme.DuoOrange
import com.example.ui.theme.DuoOrangeDark
import com.example.ui.theme.DuoOrangeLight
import com.example.ui.theme.DuoPurple
import com.example.ui.theme.DuoPurpleDark
import com.example.ui.theme.DuoRed
import com.example.ui.theme.DuoRedDark
import com.example.ui.theme.DuoRedLight
import com.example.ui.theme.DuoYellow
import kotlin.math.roundToInt

/**
 * Returns the distinctive Duolingo color palette for each difficulty level / unit.
 */
fun getDuolingoLevelColors(level: Int): Pair<Color, Color> {
    return when (level) {
        1 -> DuoGreen to DuoGreenDark       // Section 1: Principiante (A1)
        2 -> DuoBlue to DuoBlueDark         // Section 2: Básico / Restaurante (A2)
        3 -> DuoPurple to DuoPurpleDark     // Section 3: Intermedio / Viajes (B1)
        4 -> DuoOrange to DuoOrangeDark     // Section 4: Avanzado / Fonemas (B2)
        5 -> DuoRed to DuoRedDark           // Section 5: Élite / Trabalenguas (C1)
        else -> DuoBlue to DuoBlueDark
    }
}

/**
 * Returns difficulty stars and text descriptor for each level.
 */
fun getDifficultyDescriptor(level: Int): Triple<String, String, Int> {
    return when (level) {
        1 -> Triple("Principiante (A1)", "★☆☆ Fácil", 1)
        2 -> Triple("Elemental (A2)", "★★☆ Básico", 2)
        3 -> Triple("Intermedio (B1)", "★★★ Intermedio", 3)
        4 -> Triple("Avanzado (B2)", "★★★★ Desafiante", 4)
        5 -> Triple("Élite y Fluidez (C1)", "★★★★★ Maestría", 5)
        else -> Triple("Nivel $level", "★★☆", 2)
    }
}

/**
 * Duolingo Unit Header Banner Card ("Banner de Sección y Nivel de Dificultad").
 *
 * Visual characteristics (Authentic Duolingo Unit Header):
 * - Large colorful 3D banner matching Duolingo's section color scheme.
 * - 3D bottom bevel/shadow.
 * - Section tag ("SECCIÓN 1 • PRINCIPIANTE A1").
 * - Unit title and phonetic/speaking goal description.
 * - "GUÍA 📖" button opening the Unit Guidebook with vocabulary and pronunciation tips.
 * - Progress bar showing completed lessons in this difficulty level.
 * - Unlocked/Locked status indicator.
 */
@Composable
fun DuolingoUnitBannerCard(
    level: Int,
    lessonsInLevel: List<LessonModule>,
    completedLessonIds: Set<String>,
    userLevel: Int,
    onOpenGuidebook: (DifficultyTier?) -> Unit,
    modifier: Modifier = Modifier
) {
    val tier = LevelSystem.DIFFICULTY_TIERS.getOrNull(level - 1)
    val isLevelUnlocked = userLevel >= level
    val (primaryColor, shadowColor) = getDuolingoLevelColors(level)
    val (difficultyName, difficultyStars, _) = getDifficultyDescriptor(level)

    val completedCount = lessonsInLevel.count { isLessonModuleCompleted(it, completedLessonIds) }
    val totalLessons = lessonsInLevel.size.coerceAtLeast(1)
    val progressFraction = (completedCount.toFloat() / totalLessons.toFloat()).coerceIn(0f, 1f)
    val progressPercent = (progressFraction * 100f).roundToInt()

    val animatedFraction by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "unitProgressAnim"
    )

    // Outer 3D Container with Bottom Shadow Bevel
    Box(
        modifier = modifier
            .fillMaxWidth()
            .testTag("level_section_header_$level")
    ) {
        // Bottom 3D shadow layer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(138.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(if (isLevelUnlocked) shadowColor else Color(0xFF757575))
        )

        // Top raised surface card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
                .clip(RoundedCornerShape(20.dp)),
            color = if (isLevelUnlocked) primaryColor else Color(0xFF9E9E9E),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Top Tag Row: Section Number + Guidebook Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = tier?.badgeEmoji ?: "🎙️",
                            fontSize = 18.sp
                        )
                        Text(
                            text = "SECCIÓN $level • ${difficultyName.uppercase()}",
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.92f),
                            letterSpacing = 0.5.sp
                        )
                    }

                    // Duolingo "GUÍA" Button (Guidebook)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.22f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.45f)),
                        modifier = Modifier
                            .clickable { onOpenGuidebook(tier) }
                            .testTag("unit_guidebook_button_$level")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = "Ver guía de la sección",
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "GUÍA",
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                color = Color.White
                            )
                        }
                    }
                }

                // Middle: Title & Description
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = tier?.title ?: "Nivel $level",
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        color = Color.White
                    )
                    Text(
                        text = tier?.description ?: "Frases y pronunciación recomendadas para este nivel.",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.88f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Bottom: Progress bar, completed counter, and difficulty stars
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isLevelUnlocked) {
                                "$completedCount/$totalLessons lecciones completadas ($progressPercent%)"
                            } else {
                                "🔒 Desbloquea en Nivel $level"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.95f)
                        )

                        Text(
                            text = difficultyStars,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White.copy(alpha = 0.95f)
                        )
                    }

                    // Progress track with white/translucent gloss
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.Black.copy(alpha = 0.20f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(if (isLevelUnlocked) animatedFraction.coerceAtLeast(0.04f) else 0f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Material 3 Duolingo-Style Lesson Item Card ("Tarjeta de Lección Disponible").
 *
 * Visual characteristics:
 * - 3D tactile card with raised top surface and darker bottom rim.
 * - Circular 3D Lesson Avatar / Icon with colored background and bottom bevel.
 * - Glowing animated pulsating halo when this is the current next available lesson!
 * - Lesson Title, subtitle context, exercise count, and EXP reward pill.
 * - Duolingo 3D Action Button ("¡EMPEZAR!", "REPASAR", or "BLOQUEADO").
 * - Golden crown/star badge if completed.
 */
@Composable
fun DuolingoLessonItemCard(
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

    val infiniteTransition = rememberInfiniteTransition(label = "haloAnim")
    val haloScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseHalo"
    )

    val isCurrentActive = isUnlocked && !isCompleted

    // Outer card container
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("lesson_card_${module.id}")
            .clickable(enabled = isUnlocked) { onCardClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrentActive) 4.dp else 2.dp),
        border = BorderStroke(
            width = if (isCurrentActive) 2.dp else 1.2.dp,
            color = when {
                isCompleted -> DuoYellow.copy(alpha = 0.8f)
                isCurrentActive -> DuoGreen
                else -> DuoCardBorder
            }
        )
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
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 3D Circular Lesson Avatar / Disc
                Box(
                    modifier = Modifier.size(62.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Pulsing animated ring if current active lesson
                    if (isCurrentActive) {
                        Box(
                            modifier = Modifier
                                .size(62.dp)
                                .scale(haloScale)
                                .clip(CircleShape)
                                .background(DuoGreenLight.copy(alpha = 0.6f))
                        )
                    }

                    // 3D Shadow Disc
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isCompleted -> Color(0xFFD69A00)
                                    isUnlocked -> DuoGreenDark
                                    else -> Color(0xFFB0B0B0)
                                }
                            ),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        // Raised Disc Face
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .height(48.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isCompleted -> DuoYellow
                                        isUnlocked -> DuoGreen
                                        else -> Color(0xFFCCCCCC)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = iconVector,
                                contentDescription = module.title,
                                tint = if (isUnlocked) Color.White else Color(0xFF757575),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    // Golden Star Badge if completed
                    if (isCompleted) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(DuoOrange)
                                .border(1.5.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Completada",
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }

                // Lesson Text & Tags
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "LECCIÓN $lessonOrderNumber",
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            color = when {
                                isCompleted -> DuoOrangeDark
                                isUnlocked -> DuoGreenDark
                                else -> Color.Gray
                            }
                        )

                        if (isCompleted) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = DuoYellow.copy(alpha = 0.22f)
                            ) {
                                Text(
                                    text = "COMPLETADA ⭐",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = DuoOrangeDark,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        } else if (isCurrentActive) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = DuoGreenLight
                            ) {
                                Text(
                                    text = "DISPONIBLE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = DuoGreenDark,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = module.title,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = module.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 16.sp
                    )

                    // Pills row: Exercises + XP Reward
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = "🎙️ ${module.exercises.size} frases",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = DuoOrange.copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = "+$totalXpReward EXP ⚡",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = DuoOrangeDark,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // CTA Button (Duolingo 3D Button)
            if (isUnlocked) {
                if (isCompleted) {
                    DuoButton(
                        text = "REPASAR LECCIÓN",
                        onClick = onStartDirectClick,
                        colorStyle = DuoButtonColor.WHITE,
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = DuoBlue,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    DuoButton(
                        text = "¡EMPEZAR A HABLAR!",
                        onClick = onStartDirectClick,
                        colorStyle = DuoButtonColor.GREEN,
                        icon = {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFEEEEEE),
                    border = BorderStroke(1.dp, Color(0xFFDDDDDD)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (!isLevelUnlocked) {
                                "Bloqueado • Requiere Nivel ${module.requiredLevel}"
                            } else {
                                "Completa la lección anterior para desbloquear"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

/**
 * Duolingo Reward Chest Milestone ("Cofre de Recompensas de Unidad").
 * Appears as a reward checkpoint at the end of a difficulty section.
 */
@Composable
fun DuolingoRewardChestCard(
    level: Int,
    isUnitCompleted: Boolean,
    onOpenChest: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onOpenChest() }
            .testTag("unit_chest_button_$level"),
        shape = RoundedCornerShape(18.dp),
        color = if (isUnitCompleted) DuoYellow.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(
            width = 1.5.dp,
            color = if (isUnitCompleted) DuoYellow else DuoCardBorder
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (isUnitCompleted) DuoYellow else Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isUnitCompleted) "🎁" else "🔒",
                    fontSize = 22.sp
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isUnitCompleted) {
                        "¡Cofre de Nivel $level Desbloqueado!"
                    } else {
                        "Cofre de Recompensas de la Sección $level"
                    },
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isUnitCompleted) {
                        "¡Toca para reclamar +50 EXP y +20 Gemas!"
                    } else {
                        "Supera todas las lecciones del nivel para abrirlo"
                    },
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isUnitCompleted) DuoOrange else Color.Gray.copy(alpha = 0.2f)
            ) {
                Text(
                    text = if (isUnitCompleted) "ABRIR" else "+50 EXP",
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    color = if (isUnitCompleted) Color.White else Color.Gray,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    }
}

/**
 * Unit Guidebook Modal Dialog ("Guía de Fonética y Vocabulario de la Sección").
 */
@Composable
fun SectionGuidebookDialog(
    tier: DifficultyTier?,
    onDismiss: () -> Unit
) {
    if (tier == null) return

    val (primaryColor, _) = getDuolingoLevelColors(tier.tierNumber)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier.fillMaxWidth()
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
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(primaryColor.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = tier.badgeEmoji, fontSize = 26.sp)
                    }
                    Column {
                        Text(
                            text = "Guía: ${tier.title}",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Objetivos y consejos de pronunciación",
                            fontSize = 12.sp,
                            color = primaryColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = primaryColor.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.25f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "🎯 Enfoque de la Sección:",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = tier.description,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "💡 Consejos de Voz:",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Text(
                            text = "• Habla a un ritmo constante sin acelerar.\n• Apunta a una precisión superior al 60% para no perder vidas.\n• Escucha la versión nativa y compárala con tu onda de voz.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 17.sp
                        )
                    }
                }

                DuoButton(
                    text = "¡ENTENDIDO!",
                    onClick = onDismiss,
                    colorStyle = DuoButtonColor.GREEN,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Dialog celebrating opening a section's reward chest.
 */
@Composable
fun UnitChestRewardDialog(
    level: Int,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(26.dp),
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
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(DuoYellow.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🎁", fontSize = 40.sp)
                }

                Text(
                    text = "¡Cofre Reclamado!",
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = DuoOrangeDark,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "¡Completaste con éxito la Sección de Dificultad $level! Has ganado:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = DuoOrange.copy(alpha = 0.15f),
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Text(
                            text = "⚡ +50 EXP",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = DuoOrangeDark,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = DuoBlue.copy(alpha = 0.15f),
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Text(
                            text = "💎 +20 Gemas",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = DuoBlueDark,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }

                DuoButton(
                    text = "¡GENIAL!",
                    onClick = onDismiss,
                    colorStyle = DuoButtonColor.GREEN,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
