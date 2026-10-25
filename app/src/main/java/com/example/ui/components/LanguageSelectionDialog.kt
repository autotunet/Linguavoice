package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Language
import com.example.data.model.PronunciationResult
import com.example.data.model.SpokenLanguage
import com.example.ui.theme.AppStrings
import com.example.ui.theme.DuoBlue
import com.example.ui.theme.DuoCardBorder
import com.example.ui.theme.DuoGreen
import com.example.ui.theme.DuoGreenDark
import com.example.ui.theme.DuoOrange
import com.example.ui.theme.DuoPurple
import com.example.ui.theme.DuoRed
import com.example.ui.theme.DuoRedLight

/**
 * Interactive dialog that explicitly asks the user:
 * 1. "¿Qué idioma hablas?" (Native / spoken language)
 * 2. "¿Qué idioma deseas aprender?" (Target language)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LanguagePairOnboardingDialog(
    initialSpokenLanguage: SpokenLanguage,
    initialTargetLanguage: Language,
    onConfirmLanguagePair: (SpokenLanguage, Language) -> Unit,
    onOpenFullLanguageScreen: () -> Unit = {},
    onDismiss: () -> Unit
) {
    var selectedSpoken by remember(initialSpokenLanguage) { mutableStateOf(initialSpokenLanguage) }
    var selectedTarget by remember(initialTargetLanguage, initialSpokenLanguage) {
        mutableStateOf(Language.resolveDistinctTargetLanguage(initialSpokenLanguage, initialTargetLanguage))
    }
    val strings = remember(selectedSpoken) {
        AppStrings.forSpokenLanguage(selectedSpoken)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .testTag("language_pair_onboarding_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = DuoGreen.copy(alpha = 0.15f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = "🌍", fontSize = 26.sp)
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.whatLanguageDoYouSpeak,
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = strings.appChangesLanguageNotice,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DuoGreenDark
                        )
                    }
                }

                // Live Pair Preview Pill
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = DuoBlue.copy(alpha = 0.10f),
                    border = BorderStroke(1.5.dp, DuoBlue.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_language_pair_summary")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "IDIOMA QUE HABLAS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = DuoPurple
                            )
                            Text(
                                text = "${selectedSpoken.flagEmoji} ${selectedSpoken.displayName}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Aprender",
                            tint = DuoBlue,
                            modifier = Modifier.size(20.dp)
                        )

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "IDIOMA A APRENDER",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = DuoGreenDark
                            )
                            Text(
                                text = "${selectedTarget.flagEmoji} ${selectedTarget.displayName}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // STEP 1: ¿Qué idioma hablas?
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = null,
                            tint = DuoPurple,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = strings.languageYouSpeakTitle,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.testTag("onboarding_question_spoken_title")
                        )
                    }
                    Text(
                        text = strings.languageYouSpeakSubtitle,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        maxItemsInEachRow = 3
                    ) {
                        SpokenLanguage.entries.forEach { spoken ->
                            val isSelected = spoken == selectedSpoken
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
                                    .clickable {
                                        selectedSpoken = spoken
                                        selectedTarget = Language.resolveDistinctTargetLanguage(spoken, selectedTarget)
                                    }
                                    .testTag("onboarding_spoken_lang_${spoken.code}")
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(text = spoken.flagEmoji, fontSize = 22.sp)
                                    Text(
                                        text = spoken.displayName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isSelected) DuoPurple else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = DuoCardBorder.copy(alpha = 0.6f))

                // STEP 2: ¿Qué idioma deseas aprender?
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = DuoGreenDark,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = strings.languageToLearnTitle,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.testTag("onboarding_question_target_title")
                        )
                    }
                    Text(
                        text = strings.languageToLearnSubtitle,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(7.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Language.entries.forEach { lang ->
                            val isSameAsSpoken = lang.code.equals(selectedSpoken.code, ignoreCase = true)
                            val isSelected = lang == selectedTarget
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = when {
                                            isSelected -> DuoGreen
                                            isSameAsSpoken -> DuoCardBorder.copy(alpha = 0.5f)
                                            else -> DuoCardBorder
                                        },
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .background(
                                        when {
                                            isSelected -> DuoGreen.copy(alpha = 0.10f)
                                            isSameAsSpoken -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                                            else -> Color.Transparent
                                        }
                                    )
                                    .clickable {
                                        if (isSameAsSpoken) {
                                            selectedSpoken = if (lang == Language.SPANISH) {
                                                SpokenLanguage.ENGLISH
                                            } else {
                                                SpokenLanguage.SPANISH
                                            }
                                        }
                                        selectedTarget = lang
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                                    .testTag("onboarding_target_lang_${lang.code}"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(text = lang.flagEmoji, fontSize = 22.sp)
                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "${lang.getDisplayNameFor(selectedSpoken)} (${lang.nativeName})",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            if (isSameAsSpoken) {
                                                Text(
                                                    text = "(${strings.yourLanguageBadge})",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = DuoOrange
                                                )
                                            }
                                        }
                                        Text(
                                            text = "${lang.greeting} → ${lang.getGreetingTranslationFor(selectedSpoken)}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )
                                    }
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Idioma objetivo seleccionado",
                                        tint = DuoGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                DuoButton(
                    text = "${strings.startLearningLanguageBtn(selectedTarget.getDisplayNameFor(selectedSpoken))} ${selectedTarget.flagEmoji}",
                    onClick = {
                        onConfirmLanguagePair(selectedSpoken, selectedTarget)
                    },
                    colorStyle = DuoButtonColor.GREEN,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "confirm_language_pair_onboarding_btn"
                )
            }
        }
    }
}

@Composable
fun LanguageSelectionDialog(
    selectedLanguage: Language,
    selectedSpokenLanguage: SpokenLanguage = SpokenLanguage.SPANISH,
    onSelectLanguage: (Language) -> Unit,
    onSelectSpokenLanguage: (SpokenLanguage) -> Unit = {},
    onDismiss: () -> Unit
) {
    LanguagePairOnboardingDialog(
        initialSpokenLanguage = selectedSpokenLanguage,
        initialTargetLanguage = selectedLanguage,
        onConfirmLanguagePair = { spoken, target ->
            onSelectSpokenLanguage(spoken)
            onSelectLanguage(target)
        },
        onDismiss = onDismiss
    )
}

@Composable
fun HeartsInfoDialog(
    currentHearts: Int,
    onRefill: () -> Unit,
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
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = DuoRed,
                    modifier = Modifier.size(54.dp)
                )

                Text(
                    text = "Tienes $currentHearts de 5 vidas",
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp
                )

                Text(
                    text = "Pierdes 1 vida por cada error en la pronunciación. Si tus vidas llegan a 0, el nivel actual se reiniciará desde el principio.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                DuoButton(
                    text = "Recargar Vidas al Máximo",
                    onClick = onRefill,
                    colorStyle = DuoButtonColor.RED,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "refill_dialog_button"
                )

                DuoButton(
                    text = "Cerrar",
                    onClick = onDismiss,
                    colorStyle = DuoButtonColor.WHITE,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun LivesBarIndicator(
    currentLives: Int,
    maxLives: Int = 5,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (currentLives <= 1) DuoRedLight else DuoRed.copy(alpha = 0.08f))
            .border(1.dp, DuoRed.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .let { mod -> if (onClick != null) mod.clickable { onClick() } else mod }
            .padding(horizontal = 10.dp, vertical = 5.dp)
            .testTag("exercise_lives_indicator"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        for (i in 1..maxLives) {
            val isAlive = i <= currentLives
            Icon(
                imageVector = if (isAlive) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = if (isAlive) "Vida activa $i" else "Vida perdida $i",
                tint = if (isAlive) DuoRed else Color.Gray.copy(alpha = 0.45f),
                modifier = Modifier.size(16.dp)
            )
        }
        Text(
            text = "$currentLives/$maxLives",
            fontWeight = FontWeight.Black,
            fontSize = 12.sp,
            color = DuoRed,
            modifier = Modifier
                .padding(start = 4.dp)
                .testTag("exercise_lives_count_text")
        )
    }
}

@Composable
fun LevelRestartedByZeroLivesDialog(
    lastFailedResult: PronunciationResult?,
    levelRestartCount: Int,
    onConfirmRestart: () -> Unit,
    onExitToMap: () -> Unit
) {
    Dialog(onDismissRequest = onConfirmRestart) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("level_restarted_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = DuoRedLight,
                    modifier = Modifier.size(68.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Nivel reiniciado",
                            tint = DuoRed,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Text(
                    text = "¡Te quedaste sin vidas!",
                    fontWeight = FontWeight.Black,
                    fontSize = 21.sp,
                    color = DuoRed,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Tus vidas llegaron a 0 tras varios errores de pronunciación. El nivel se ha reiniciado automáticamente al primer ejercicio con tus 5 vidas restauradas.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                if (lastFailedResult != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(DuoRedLight.copy(alpha = 0.55f))
                            .border(1.dp, DuoRed.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Último intento: ${lastFailedResult.scorePercentage}% de precisión",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = DuoRed
                        )
                        if (lastFailedResult.recognizedText.isNotBlank()) {
                            Text(
                                text = "Dijiste: \"${lastFailedResult.recognizedText}\"",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                if (levelRestartCount > 0) {
                    Text(
                        text = "Reinicios en esta sesión: $levelRestartCount • Vidas restauradas: 5/5 ❤️",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = DuoOrange
                    )
                }

                DuoButton(
                    text = "Reintentar Nivel desde el Inicio",
                    onClick = onConfirmRestart,
                    colorStyle = DuoButtonColor.GREEN,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "confirm_level_restart_button"
                )

                DuoButton(
                    text = "Salir al Mapa",
                    onClick = onExitToMap,
                    colorStyle = DuoButtonColor.WHITE,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "exit_after_restart_button"
                )
            }
        }
    }
}
