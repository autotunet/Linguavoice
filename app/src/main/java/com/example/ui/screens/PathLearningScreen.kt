package com.example.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LessonModule
import com.example.data.model.UserLevelInfo
import com.example.data.model.UserProfile
import com.example.data.repository.ExerciseRepository
import com.example.data.repository.StreakInfo
import com.example.ui.components.DailyStreakCard
import com.example.ui.components.DuoButton
import com.example.ui.components.DuoButtonColor
import com.example.ui.components.LevelRoadmapDialog
import com.example.ui.components.LevelSectionHeaderCard
import com.example.ui.components.MascotBubble
import com.example.ui.components.Material3LessonCard
import com.example.ui.components.Material3StreakCounterCard
import com.example.ui.components.UserProgressSummaryCard
import com.example.ui.components.isLessonModuleCompleted
import com.example.ui.theme.DuoCardBorder
import com.example.ui.theme.DuoGreen
import com.example.ui.theme.DuoGreenDark
import com.example.ui.theme.DuoGreenLight
import com.example.ui.theme.DuoOrange
import com.example.ui.theme.DuoPurple
import com.example.ui.theme.DuoYellow

private enum class HomeLessonsDisplayMode {
    LIST_BY_LEVEL,
    INTERACTIVE_PATH
}

@Composable
fun PathLearningScreen(
    userProfile: UserProfile,
    streakInfo: StreakInfo,
    levelInfo: UserLevelInfo,
    isStreakBannerHidden: Boolean = false,
    onHideStreakBanner: () -> Unit = {},
    onOpenStreakPopup: () -> Unit = {},
    onStartLesson: (LessonModule) -> Unit,
    modifier: Modifier = Modifier
) {
    val modules = remember(userProfile.selectedLanguage) {
        ExerciseRepository.getModulesForLanguage(userProfile.selectedLanguage)
    }

    val modulesByLevel = remember(userProfile.selectedLanguage) {
        ExerciseRepository.getModulesGroupedByLevel(userProfile.selectedLanguage)
    }

    val completedLessonsCount = remember(modules, userProfile.completedLessonIds) {
        modules.count { isLessonModuleCompleted(it, userProfile.completedLessonIds) }
    }

    val nextAvailableLesson = remember(modules, userProfile.completedLessonIds, levelInfo.level) {
        modules.firstOrNull { module ->
            val isCompleted = isLessonModuleCompleted(module, userProfile.completedLessonIds)
            val isLevelUnlocked = levelInfo.level >= module.requiredLevel
            !isCompleted && isLevelUnlocked
        } ?: modules.firstOrNull { levelInfo.level >= it.requiredLevel }
    }

    var selectedModuleForSheet by remember { mutableStateOf<LessonModule?>(null) }
    var showRoadmapDialog by remember { mutableStateOf(false) }
    var selectedLevelFilter by remember { mutableStateOf<Int?>(null) }
    var displayMode by remember { mutableStateOf(HomeLessonsDisplayMode.LIST_BY_LEVEL) }

    val filteredLevelsMap = remember(modulesByLevel, selectedLevelFilter) {
        if (selectedLevelFilter == null) {
            modulesByLevel
        } else {
            modulesByLevel.filterKeys { it == selectedLevelFilter }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .testTag("main_home_lazy_column"),
            contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Material 3 User Progress Summary Card ("Tarjeta de resumen de progreso del usuario")
            item {
                UserProgressSummaryCard(
                    userProfile = userProfile,
                    levelInfo = levelInfo,
                    completedLessonsCount = completedLessonsCount,
                    totalLessonsCount = modules.size,
                    nextAvailableLesson = nextAvailableLesson,
                    onContinueLesson = onStartLesson,
                    onOpenLevelRoadmap = { showRoadmapDialog = true }
                )
            }

            // 2. Material 3 Streak Counter ("Contador de rachas")
            item {
                Material3StreakCounterCard(
                    streakInfo = streakInfo,
                    isBannerHidden = isStreakBannerHidden,
                    onHideBanner = onHideStreakBanner,
                    onOpenStreakDetail = onOpenStreakPopup
                )
            }

            // Compact daily streak notification banner if not hidden
            if (!isStreakBannerHidden) {
                item {
                    DailyStreakCard(
                        streakInfo = streakInfo,
                        isHidden = isStreakBannerHidden,
                        onHide = onHideStreakBanner,
                        onOpenDetail = onOpenStreakPopup
                    )
                }
            }

            // 3. Section Title + View Mode Switcher & Level Filter Chips ("Lista de lecciones disponibles por nivel")
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Lecciones por Nivel",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.testTag("lessons_by_level_heading")
                            )
                            Text(
                                text = "${modules.size} lecciones en ${modulesByLevel.size} niveles de dificultad",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.testTag("home_display_mode_toggle")
                        ) {
                            SegmentedButton(
                                selected = displayMode == HomeLessonsDisplayMode.LIST_BY_LEVEL,
                                onClick = { displayMode = HomeLessonsDisplayMode.LIST_BY_LEVEL },
                                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.ViewList,
                                        contentDescription = "Vista de lista por nivel",
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = "Niveles",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            )
                            SegmentedButton(
                                selected = displayMode == HomeLessonsDisplayMode.INTERACTIVE_PATH,
                                onClick = { displayMode = HomeLessonsDisplayMode.INTERACTIVE_PATH },
                                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.AccountTree,
                                        contentDescription = "Vista de ruta",
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = "Ruta",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            )
                        }
                    }

                    // Level Filter Chips Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = selectedLevelFilter == null,
                            onClick = { selectedLevelFilter = null },
                            label = {
                                Text(
                                    text = "Todos (${modules.size})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DuoGreen.copy(alpha = 0.18f),
                                selectedLabelColor = DuoGreenDark
                            ),
                            modifier = Modifier.testTag("level_filter_all")
                        )

                        modulesByLevel.forEach { (levelNumber, levelModules) ->
                            val isLevelUnlocked = levelInfo.level >= levelNumber
                            FilterChip(
                                selected = selectedLevelFilter == levelNumber,
                                onClick = {
                                    selectedLevelFilter =
                                        if (selectedLevelFilter == levelNumber) null else levelNumber
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (isLevelUnlocked) Icons.Default.Check else Icons.Default.Lock,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = "Nivel $levelNumber (${levelModules.size})",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = DuoPurple.copy(alpha = 0.16f),
                                    selectedLabelColor = DuoPurple
                                ),
                                modifier = Modifier.testTag("level_filter_$levelNumber")
                            )
                        }
                    }
                }
            }

            if (displayMode == HomeLessonsDisplayMode.LIST_BY_LEVEL) {
                // Material 3 Grouped Lessons List by Level
                filteredLevelsMap.forEach { (levelNumber, lessonsInLevel) ->
                    item(key = "header_level_$levelNumber") {
                        LevelSectionHeaderCard(
                            level = levelNumber,
                            lessonsInLevel = lessonsInLevel,
                            completedLessonIds = userProfile.completedLessonIds,
                            userLevel = levelInfo.level
                        )
                    }

                    itemsIndexed(
                        items = lessonsInLevel,
                        key = { _, lesson -> "list_${lesson.id}" }
                    ) { indexInLevel, module ->
                        val globalIndex = modules.indexOfFirst { it.id == module.id }.coerceAtLeast(0)
                        val isCompleted = isLessonModuleCompleted(module, userProfile.completedLessonIds)
                        val isLevelUnlocked = levelInfo.level >= module.requiredLevel
                        val isPreviousCompleted = globalIndex == 0 ||
                            isLessonModuleCompleted(modules[globalIndex - 1], userProfile.completedLessonIds)
                        val isUnlocked = isLevelUnlocked && (indexInLevel == 0 || isPreviousCompleted || isCompleted)

                        Material3LessonCard(
                            module = module,
                            lessonOrderNumber = globalIndex + 1,
                            isCompleted = isCompleted,
                            isUnlocked = isUnlocked,
                            isLevelUnlocked = isLevelUnlocked,
                            onCardClick = {
                                if (isUnlocked) {
                                    selectedModuleForSheet = module
                                }
                            },
                            onStartDirectClick = {
                                if (isUnlocked) {
                                    onStartLesson(module)
                                }
                            }
                        )
                    }
                }
            } else {
                // Mascot Cheer for interactive path mode
                item {
                    MascotBubble(
                        message = "¡Hola! Toca cualquier nodo desbloqueado para hablar y ganar EXP.",
                        subMessage = "Pronunciar en voz alta activa las conexiones neuronales del idioma."
                    )
                }

                // Interactive Duolingo-style Winding Path Nodes
                itemsIndexed(
                    items = modules,
                    key = { _, module -> "path_${module.id}" }
                ) { index, module ->
                    val isCompleted = isLessonModuleCompleted(module, userProfile.completedLessonIds)
                    val isLevelUnlocked = levelInfo.level >= module.requiredLevel
                    val isUnlocked = isLevelUnlocked &&
                        (index == 0 || isLessonModuleCompleted(modules[index - 1], userProfile.completedLessonIds) || isCompleted)

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
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
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

        // Level Roadmap Dialog
        if (showRoadmapDialog) {
            LevelRoadmapDialog(
                levelInfo = levelInfo,
                onDismiss = { showRoadmapDialog = false }
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
            if (isUnlocked && !isCompleted) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(DuoGreen.copy(alpha = 0.25f))
                )
            }

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
                        Text(text = "Nivel", fontSize = 12.sp, color = Color.Gray)
                        Text(
                            text = "Nivel ${module.requiredLevel}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = DuoPurple
                        )
                    }
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
                            text = "+${module.exercises.sumOf { it.xpReward }.coerceAtLeast(module.exercises.size * 15)} EXP",
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
