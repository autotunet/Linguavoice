package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.data.model.Language
import com.example.data.model.PronunciationResult
import com.example.ui.theme.DuoCardBorder
import com.example.ui.theme.DuoGreen
import com.example.ui.theme.DuoOrange
import com.example.ui.theme.DuoRed
import com.example.ui.theme.DuoRedLight

@Composable
fun LanguageSelectionDialog(
    selectedLanguage: Language,
    onSelectLanguage: (Language) -> Unit,
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
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Elige un idioma para practicar",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "El reconocimiento de voz y las lecciones se adaptarán a la fonética nativa del idioma.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Language.values().forEach { lang ->
                        val isSelected = lang == selectedLanguage
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .border(
                                    width = 2.dp,
                                    color = if (isSelected) DuoGreen else DuoCardBorder,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .background(if (isSelected) DuoGreen.copy(alpha = 0.1f) else Color.Transparent)
                                .clickable { onSelectLanguage(lang) }
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                .testTag("select_lang_${lang.code}"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(text = lang.flagEmoji, fontSize = 24.sp)
                                Column {
                                    Text(
                                        text = lang.displayName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = lang.greeting,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Seleccionado",
                                    tint = DuoGreen
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
