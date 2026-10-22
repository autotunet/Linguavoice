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
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeUp
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Exercise
import com.example.data.model.UserProfile
import com.example.data.repository.ExerciseRepository
import com.example.ui.components.DuoButton
import com.example.ui.components.DuoButtonColor
import com.example.ads.AdMobBanner
import com.example.data.model.LevelSystem
import com.example.data.model.UserLevelInfo
import androidx.compose.material.icons.filled.Lock
import com.example.ui.components.MascotBubble
import com.example.ui.theme.DuoBlue
import com.example.ui.theme.DuoCardBorder
import com.example.ui.theme.DuoGreen
import com.example.ui.theme.DuoOrange
import com.example.ui.theme.DuoPurple

@Composable
fun FreePracticeScreen(
    userProfile: UserProfile,
    levelInfo: UserLevelInfo,
    onSelectExercise: (Exercise) -> Unit,
    onPlayAudio: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val allExercises = remember(userProfile.selectedLanguage) {
        ExerciseRepository.getExercisesForLanguage(userProfile.selectedLanguage)
    }

    var selectedFilter by remember { mutableStateOf("Todos") }
    var lockedMessage by remember { mutableStateOf<String?>(null) }

    val filteredList = remember(selectedFilter, allExercises) {
        when (selectedFilter) {
            "Trabalenguas" -> allExercises.filter { it.promptQuestion.contains("Trabalenguas", ignoreCase = true) }
            "Saludos" -> allExercises.filter { it.lessonId.endsWith("1") }
            "Café" -> allExercises.filter { it.lessonId.endsWith("2") }
            "Viajes" -> allExercises.filter { it.lessonId.endsWith("3") }
            else -> allExercises
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            MascotBubble(
                message = "¡Gimnasio de Pronunciación! Elige cualquier frase o categoría para ejercitar.",
                subMessage = "Tu Nivel ${levelInfo.level} (${levelInfo.levelTitle}) desbloquea categorías con mayor desafío fonético."
            )
        }

        // Category Pills with Level Locking
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Todos", "Saludos", "Café", "Viajes", "Trabalenguas").forEach { filter ->
                        val isSelected = selectedFilter == filter
                        val reqLevel = LevelSystem.getRequiredLevelForCategory(filter)
                        val isUnlocked = LevelSystem.isCategoryUnlocked(filter, levelInfo.level)

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .border(
                                    width = 1.5.dp,
                                    color = if (isSelected) DuoGreen else DuoCardBorder,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .background(if (isSelected) DuoGreen else if (isUnlocked) MaterialTheme.colorScheme.surface else Color(0xFFF3F3F3))
                                .clickable {
                                    if (isUnlocked) {
                                        selectedFilter = filter
                                        lockedMessage = null
                                    } else {
                                        lockedMessage = "🔒 '$filter' se desbloquea en Nivel $reqLevel (Te faltan ${levelInfo.xpNeededForNextLevel} EXP)"
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 7.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (!isUnlocked) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Bloqueado",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                                Text(
                                    text = filter,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isSelected) Color.White else if (isUnlocked) MaterialTheme.colorScheme.onSurface else Color.Gray
                                )
                            }
                        }
                    }
                }

                lockedMessage?.let { msg ->
                    Text(
                        text = msg,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = DuoOrange
                    )
                }
            }
        }

        items(filteredList) { exercise ->
            PracticeExerciseCard(
                exercise = exercise,
                onPlay = { onPlayAudio(exercise.targetPhrase) },
                onPractice = { onSelectExercise(exercise) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun PracticeExerciseCard(
    exercise: Exercise,
    onPlay: () -> Unit,
    onPractice: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("practice_card_${exercise.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exercise.promptQuestion,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = DuoPurple
                    )
                    Text(
                        text = exercise.targetPhrase,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        text = exercise.phoneticGuide,
                        fontSize = 13.sp,
                        fontStyle = FontStyle.Italic,
                        color = DuoBlue,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Text(
                        text = exercise.translation,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // XP badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFFFF3CD))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "+${exercise.xpReward} EXP",
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        color = DuoOrange
                    )
                }
            }

            // Tip note
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(8.dp)
            ) {
                Text(
                    text = "💡 ${exercise.pronunciationTip}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action buttons: Listen & Speak
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DuoButton(
                    text = "Escuchar",
                    onClick = onPlay,
                    modifier = Modifier.weight(1f),
                    colorStyle = DuoButtonColor.WHITE,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = DuoBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    testTag = "listen_card_${exercise.id}"
                )

                DuoButton(
                    text = "Hablar",
                    onClick = onPractice,
                    modifier = Modifier.weight(1.3f),
                    colorStyle = DuoButtonColor.GREEN,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    testTag = "speak_card_${exercise.id}"
                )
            }
        }
    }
}
