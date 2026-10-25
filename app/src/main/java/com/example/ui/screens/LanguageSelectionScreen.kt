package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.LanguagePreferenceEntity
import com.example.data.model.Language
import com.example.data.model.LearningFocusGoal
import com.example.data.model.LessonModule
import com.example.data.model.SpokenLanguage
import com.example.data.repository.ExerciseRepository
import com.example.ui.components.DuoButton
import com.example.ui.components.DuoButtonColor
import com.example.ui.theme.AppStrings
import com.example.ui.theme.DuoBlue
import com.example.ui.theme.DuoCardBorder
import com.example.ui.theme.DuoGreen
import com.example.ui.theme.DuoGreenDark
import com.example.ui.theme.DuoOrange
import com.example.ui.theme.DuoPurple

/**
 * Language Selection Screen ("Pantalla de selección de idioma que habla e idioma objetivo").
 * Asks the user which language they speak ("¿Qué idioma hablas?") and which language they want
 * to learn ("¿Qué idioma deseas aprender?"), saving both preferences in Room to personalize
 * lessons, pronunciation exercises, translations, and dialogues.
 */
@Composable
fun LanguageSelectionScreen(
    currentLanguage: Language,
    currentLearningFocus: LearningFocusGoal,
    roomPreferenceEntity: LanguagePreferenceEntity,
    currentSpokenLanguage: SpokenLanguage = roomPreferenceEntity.toSpokenLanguage(),
    onSelectTargetLanguage: (Language, LearningFocusGoal, Boolean) -> Unit,
    onSelectLanguagePair: (SpokenLanguage, Language, LearningFocusGoal, Boolean) -> Unit = { _, lang, focus, goHome ->
        onSelectTargetLanguage(lang, focus, goHome)
    },
    onPlayLanguageSample: (Language) -> Unit,
    onStartPersonalizedLesson: (LessonModule) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onBack()
    }

    var pendingSpokenLanguage by remember(currentSpokenLanguage) { mutableStateOf(currentSpokenLanguage) }
    var pendingLanguage by remember(currentLanguage) { mutableStateOf(currentLanguage) }
    var pendingFocus by remember(currentLearningFocus) { mutableStateOf(currentLearningFocus) }
    val strings = remember(pendingSpokenLanguage) {
        AppStrings.forSpokenLanguage(pendingSpokenLanguage)
    }

    val personalizedPreviewModules = remember(pendingLanguage, pendingFocus) {
        ExerciseRepository.getPersonalizedModulesForLanguage(pendingLanguage, pendingFocus)
    }

    val totalExercisesForSelected = remember(pendingLanguage) {
        ExerciseRepository.getExercisesForLanguage(pendingLanguage).size
    }

    val totalDialoguesForSelected = remember(pendingLanguage) {
        ExerciseRepository.getDialogueScenarios(pendingLanguage).size
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("language_selection_screen")
    ) {
        // Top Header Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("language_screen_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver a las lecciones"
                        )
                    }
                    Column {
                        Text(
                            text = strings.personalizedCourseTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = strings.whatLanguageDoYouSpeak,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Active Room Persistence Badge showing Spoken -> Target
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = DuoGreen.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, DuoGreen.copy(alpha = 0.4f)),
                    modifier = Modifier.testTag("room_saved_language_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = "Guardado en Room",
                            tint = DuoGreenDark,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${roomPreferenceEntity.toSpokenLanguage().flagEmoji} → ${roomPreferenceEntity.toLanguage().flagEmoji} ${roomPreferenceEntity.displayName}",
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            color = DuoGreenDark
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .testTag("language_selection_lazy_column"),
            contentPadding = PaddingValues(top = 14.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Hero Summary Banner of Selected Spoken & Target Language Pair
            item {
                TargetLanguageHeroCard(
                    spokenLanguage = pendingSpokenLanguage,
                    selectedLanguage = pendingLanguage,
                    selectedFocus = pendingFocus,
                    modulesCount = personalizedPreviewModules.size,
                    exercisesCount = totalExercisesForSelected,
                    dialoguesCount = totalDialoguesForSelected,
                    onPlayGreeting = { onPlayLanguageSample(pendingLanguage) }
                )
            }

            // 1b. Question 1: "¿Qué idioma hablas?" (Native / Spoken Language Selector Card)
            item {
                SpokenLanguageSelectorCard(
                    selectedSpokenLanguage = pendingSpokenLanguage,
                    currentTargetLanguage = pendingLanguage,
                    onSelectSpokenLanguage = { newSpoken ->
                        pendingSpokenLanguage = newSpoken
                        val distinctTarget = Language.resolveDistinctTargetLanguage(newSpoken, pendingLanguage)
                        pendingLanguage = distinctTarget
                        onSelectLanguagePair(newSpoken, distinctTarget, pendingFocus, false)
                    }
                )
            }

            // 2. Learning Focus Personalization Section ("Enfoque de tus lecciones")
            item {
                LearningFocusSelectorCard(
                    selectedFocus = pendingFocus,
                    onSelectFocus = { focus ->
                        pendingFocus = focus
                        // Immediately persist updated focus + language pair in Room to personalize lessons
                        onSelectLanguagePair(pendingSpokenLanguage, pendingLanguage, focus, false)
                    }
                )
            }

            // 3. Question 2 Section Header: "¿Qué idioma deseas aprender?"
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.languageToLearnTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.testTag("choose_target_language_heading")
                        )
                        Text(
                            text = strings.languageToLearnSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = DuoBlue.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "${Language.entries.size} ${strings.navLanguages}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = DuoBlue,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // 4. Target Language Cards List
            items(
                items = Language.entries,
                key = { it.code }
            ) { language ->
                val isSelected = language == pendingLanguage
                val langModules = remember(language, pendingFocus) {
                    ExerciseRepository.getPersonalizedModulesForLanguage(language, pendingFocus)
                }
                val langExercisesCount = remember(language) {
                    ExerciseRepository.getExercisesForLanguage(language).size
                }

                TargetLanguageOptionCard(
                    language = language,
                    spokenLanguage = pendingSpokenLanguage,
                    isSelected = isSelected,
                    modulesCount = langModules.size,
                    exercisesCount = langExercisesCount,
                    onSelect = {
                        if (language.code.equals(pendingSpokenLanguage.code, ignoreCase = true)) {
                            pendingSpokenLanguage = if (language == Language.SPANISH) {
                                SpokenLanguage.ENGLISH
                            } else {
                                SpokenLanguage.SPANISH
                            }
                        }
                        pendingLanguage = language
                        onSelectLanguagePair(pendingSpokenLanguage, language, pendingFocus, false)
                    },
                    onPlayGreeting = {
                        onPlayLanguageSample(language)
                    }
                )
            }

            // 5. Personalized Lessons Preview Card for the Selected Language
            item {
                PersonalizedLessonsPreviewCard(
                    language = pendingLanguage,
                    learningFocus = pendingFocus,
                    personalizedModules = personalizedPreviewModules,
                    onStartFirstLesson = { module ->
                        onSelectLanguagePair(pendingSpokenLanguage, pendingLanguage, pendingFocus, false)
                        onStartPersonalizedLesson(module)
                    }
                )
            }
        }

        // Bottom Sticky Confirmation Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DuoButton(
                    text = "${strings.iSpeakPrefix} ${pendingSpokenLanguage.getDisplayNameFor(pendingSpokenLanguage)} ${pendingSpokenLanguage.flagEmoji} • ${strings.startLearningLanguageBtn(pendingLanguage.getDisplayNameFor(pendingSpokenLanguage))} ${pendingLanguage.flagEmoji}",
                    onClick = {
                        onSelectLanguagePair(pendingSpokenLanguage, pendingLanguage, pendingFocus, true)
                    },
                    colorStyle = DuoButtonColor.GREEN,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "confirm_language_selection_button"
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SpokenLanguageSelectorCard(
    selectedSpokenLanguage: SpokenLanguage,
    currentTargetLanguage: Language,
    onSelectSpokenLanguage: (SpokenLanguage) -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("spoken_language_selector_card"),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.5.dp, DuoPurple.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.RecordVoiceOver,
                    contentDescription = null,
                    tint = DuoPurple,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    val strings = AppStrings.forSpokenLanguage(selectedSpokenLanguage)
                    Text(
                        text = strings.languageYouSpeakTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.testTag("choose_spoken_language_heading")
                    )
                    Text(
                        text = strings.appChangesLanguageNotice,
                        style = MaterialTheme.typography.bodySmall,
                        color = DuoGreenDark,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 3
            ) {
                SpokenLanguage.entries.forEach { spoken ->
                    val isSelected = spoken == selectedSpokenLanguage
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) DuoPurple.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) DuoPurple else DuoCardBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onSelectSpokenLanguage(spoken) }
                            .testTag("spoken_language_card_${spoken.code}")
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = spoken.flagEmoji, fontSize = 22.sp)
                            Text(
                                text = spoken.nativeName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (isSelected) DuoPurple else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (isSelected) {
                                Text(
                                    text = AppStrings.forSpokenLanguage(selectedSpokenLanguage).yourLanguageBadge,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = DuoPurple
                                )
                            }
                        }
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = DuoPurple.copy(alpha = 0.08f),
                modifier = Modifier.fillMaxWidth()
            ) {
                val strings = AppStrings.forSpokenLanguage(selectedSpokenLanguage)
                Text(
                    text = "${strings.iSpeakPrefix} ${selectedSpokenLanguage.flagEmoji} ${selectedSpokenLanguage.nativeName} → ${strings.iLearnPrefix} ${currentTargetLanguage.flagEmoji} ${currentTargetLanguage.getDisplayNameFor(selectedSpokenLanguage)}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = DuoPurple,
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("active_language_pair_banner")
                )
            }
        }
    }
}

@Composable
private fun TargetLanguageHeroCard(
    spokenLanguage: SpokenLanguage,
    selectedLanguage: Language,
    selectedFocus: LearningFocusGoal,
    modulesCount: Int,
    exercisesCount: Int,
    dialoguesCount: Int,
    onPlayGreeting: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("target_language_hero_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            DuoBlue.copy(alpha = 0.10f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(18.dp),
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
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = DuoBlue.copy(alpha = 0.15f),
                        modifier = Modifier.size(58.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = selectedLanguage.flagEmoji,
                                fontSize = 32.sp
                            )
                        }
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "HABLAS ${spokenLanguage.flagEmoji} ${spokenLanguage.displayName.uppercase()} • APRENDES:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = DuoBlue,
                                letterSpacing = 0.7.sp
                            )
                        }
                        Text(
                            text = "${selectedLanguage.displayName} • ${selectedLanguage.nativeName}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${selectedFocus.badgeEmoji} Enfoque: ${selectedFocus.titleEs}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = DuoPurple
                        )
                    }
                }

                FilledTonalIconButton(
                    onClick = onPlayGreeting,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = DuoBlue.copy(alpha = 0.16f),
                        contentColor = DuoBlue
                    ),
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("hero_play_greeting_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Escuchar saludo en ${selectedLanguage.displayName}"
                    )
                }
            }

            // Phonetic Focus Banner
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = DuoPurple.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, DuoPurple.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = DuoPurple,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Fonética: ${selectedLanguage.phoneticFocus}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Course Stats Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CourseMetricPill(
                    label = "$modulesCount Lecciones",
                    color = DuoGreenDark,
                    modifier = Modifier.weight(1f)
                )
                CourseMetricPill(
                    label = "$exercisesCount Ejercicios",
                    color = DuoBlue,
                    modifier = Modifier.weight(1f)
                )
                CourseMetricPill(
                    label = "$dialoguesCount Diálogos IA",
                    color = DuoOrange,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun CourseMetricPill(
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.28f))
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 11.sp,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun LearningFocusSelectorCard(
    selectedFocus: LearningFocusGoal,
    onSelectFocus: (LearningFocusGoal) -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("learning_focus_selector_card"),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.5.dp, DuoCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = DuoOrange,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = "Personaliza tu enfoque de aprendizaje",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Ordena y adapta las lecciones según tu objetivo principal",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                LearningFocusGoal.entries.forEach { goal ->
                    val isSelected = goal == selectedFocus
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectFocus(goal) },
                        label = {
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text(
                                    text = "${goal.badgeEmoji} ${goal.titleEs}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = goal.descriptionEs,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        trailingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Enfoque activo",
                                    tint = DuoGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DuoGreen.copy(alpha = 0.12f),
                            selectedLabelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = DuoCardBorder,
                            selectedBorderColor = DuoGreen,
                            selectedBorderWidth = 2.dp
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("learning_focus_chip_${goal.id}")
                    )
                }
            }
        }
    }
}

@Composable
private fun TargetLanguageOptionCard(
    language: Language,
    spokenLanguage: SpokenLanguage,
    isSelected: Boolean,
    modulesCount: Int,
    exercisesCount: Int,
    onSelect: () -> Unit,
    onPlayGreeting: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) DuoGreen else DuoCardBorder,
        animationSpec = tween(durationMillis = 220),
        label = "langCardBorderColor"
    )
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) {
            DuoGreen.copy(alpha = 0.09f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(durationMillis = 220),
        label = "langCardContainerColor"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = if (isSelected) 2.5.dp else 1.5.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onSelect() }
            .testTag("target_language_card_${language.code}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                    Surface(
                        shape = CircleShape,
                        color = if (isSelected) DuoGreen.copy(alpha = 0.2f) else DuoCardBorder.copy(alpha = 0.35f),
                        modifier = Modifier.size(50.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = language.flagEmoji,
                                fontSize = 26.sp
                            )
                        }
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = language.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = DuoBlue.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = language.cefrRange,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DuoBlue,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = "${language.nativeName} • $modulesCount lecciones ($exercisesCount ejercicios)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = onPlayGreeting,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("play_greeting_${language.code}")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Escuchar ejemplo en ${language.displayName}",
                            tint = DuoBlue
                        )
                    }

                    if (isSelected) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = DuoGreen,
                            modifier = Modifier.testTag("selected_badge_${language.code}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Idioma seleccionado",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "ACTIVO",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 10.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = DuoCardBorder.copy(alpha = 0.5f))

            // Sample Phrase & Phonetic Focus Row
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "\"${language.greeting}\"",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${spokenLanguage.translationLabel} (${spokenLanguage.flagEmoji}): ${language.getGreetingTranslationFor(spokenLanguage)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "🎯 ${language.phoneticFocus}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) DuoGreenDark else DuoPurple
                )
            }
        }
    }
}

@Composable
private fun PersonalizedLessonsPreviewCard(
    language: Language,
    learningFocus: LearningFocusGoal,
    personalizedModules: List<LessonModule>,
    onStartFirstLesson: (LessonModule) -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("personalized_lessons_preview_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DuoGreen.copy(alpha = 0.15f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = DuoGreenDark,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Lecciones Personalizadas • ${language.displayName}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Adaptadas a: ${learningFocus.titleEs} (${personalizedModules.size} módulos)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            personalizedModules.take(4).forEachIndexed { index, module ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("preview_module_item_${module.id}")
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
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = DuoPurple.copy(alpha = 0.14f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "Nv.${module.requiredLevel}",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 10.sp,
                                        color = DuoPurple
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "${index + 1}. ${module.title}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = module.description,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        if (index == 0) {
                            FilledTonalIconButton(
                                onClick = { onStartFirstLesson(module) },
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = DuoGreen.copy(alpha = 0.18f),
                                    contentColor = DuoGreenDark
                                ),
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("preview_start_first_lesson_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Comenzar ${module.title}",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
