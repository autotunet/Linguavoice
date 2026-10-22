package com.example.ui.screens

import android.app.Activity
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.KeyboardVoice
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.audio.SpeechState
import com.example.data.model.DialogueScenario
import com.example.data.model.UserProfile
import com.example.data.repository.ExerciseRepository
import com.example.ui.components.CurrentLevelTopProgressBar
import com.example.ui.components.DuoButton
import com.example.ui.components.DuoButtonColor
import com.example.ui.components.LevelRestartedByZeroLivesDialog
import com.example.ui.components.LivesBarIndicator
import com.example.ui.components.MascotBubble
import com.example.ui.components.PronunciationResultSheet
import com.example.ui.components.PronunciationSuccessCelebrationOverlay
import com.example.ui.components.PulsingMicHalo
import com.example.ui.components.WaveformVisualizer
import com.example.ui.theme.DuoBlue
import com.example.ui.theme.DuoCardBorder
import com.example.ui.theme.DuoGreen
import com.example.ui.theme.DuoGreenLight
import com.example.ui.theme.DuoOrange
import com.example.ui.theme.DuoRed
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.LanguageGameViewModel

@Composable
fun DialogueScreen(
    viewModel: LanguageGameViewModel,
    userProfile: UserProfile,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val activeScenario by viewModel.activeDialogue.collectAsState()
    val turnIndex by viewModel.dialogueTurnIndex.collectAsState()
    val speechState by viewModel.speechState.collectAsState()
    val pronunciationState by viewModel.pronunciationUiState.collectAsState()
    val result by viewModel.evaluationResult.collectAsState()
    val livesState by viewModel.livesState.collectAsState()
    val levelProgress by viewModel.levelProgressState.collectAsState()
    val celebrationState by viewModel.celebrationState.collectAsState()

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startListeningForDialogue()
        }
    }

    val startDialogueMicWithPermission = {
        val granted = androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (granted) {
            viewModel.startListeningForDialogue()
        } else {
            micPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
        }
    }

    val systemSpeechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { activityResult ->
        if (activityResult.resultCode == Activity.RESULT_OK) {
            val matches = activityResult.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spoken = matches?.firstOrNull().orEmpty()
            if (spoken.isNotBlank()) {
                viewModel.onSpeechRecognized(spoken)
            }
        }
    }

    val scenarios = remember(userProfile.selectedLanguage) {
        ExerciseRepository.getDialogueScenarios(userProfile.selectedLanguage)
    }

    if (activeScenario == null) {
        // Scenarios list selection
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                MascotBubble(
                    message = "¡Roleplay conversacional! Habla en situaciones del mundo real como si estuvieras allí.",
                    subMessage = "El interlocutor hablará primero, y luego será tu turno de responder en voz alta."
                )
            }

            items(scenarios) { scenario ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialogue_scenario_${scenario.id}"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Text(
                            text = scenario.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = scenario.situation,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        DuoButton(
                            text = "Iniciar Diálogo",
                            onClick = { viewModel.startDialogueScenario(scenario) },
                            colorStyle = DuoButtonColor.BLUE,
                            modifier = Modifier.fillMaxWidth(),
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.ChatBubbleOutline,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            testTag = "start_dialogue_${scenario.id}"
                        )
                    }
                }
            }
        }
    } else {
        // Active Scenario in progress
        val scenario = activeScenario!!
        val currentTurn = scenario.turns.getOrNull(turnIndex)
        val isListening = pronunciationState.isListening || speechState is SpeechState.Listening
        val rmsDb = if (pronunciationState.isListening) {
            pronunciationState.rmsDb
        } else if (speechState is SpeechState.Listening) {
            (speechState as SpeechState.Listening).rmsDb
        } else {
            0f
        }
        val partialText = pronunciationState.partialSpokenText.ifBlank {
            if (speechState is SpeechState.Listening) (speechState as SpeechState.Listening).partialText else ""
        }

        Box(modifier = modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(6.dp))

                // Top Visual Progress Bar indicating percentage of the current dialogue level completed
                CurrentLevelTopProgressBar(
                    levelProgress = levelProgress,
                    livesState = livesState,
                    onCloseClick = { viewModel.navigateTo(AppScreen.PATH_LEARN) },
                    onLivesClick = { viewModel.setHeartsDialogVisible(true) },
                    closeButtonTestTag = "dialogue_close_button"
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Chat Messages List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 140.dp)
                ) {
                    itemsIndexed(scenario.turns.take(turnIndex + 1)) { idx, turn ->
                        // AI Prompt Bubble
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(DuoBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "🤖", fontSize = 22.sp)
                            }

                            Column(
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp))
                                    .background(Color.White)
                                    .border(1.5.dp, DuoCardBorder, RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = turn.avatarName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = DuoBlue
                                    )
                                    Icon(
                                        imageVector = Icons.Default.VolumeUp,
                                        contentDescription = "Escuchar",
                                        tint = DuoBlue,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable { viewModel.playCurrentDialogueTurnAiVoice() }
                                    )
                                }
                                Text(
                                    text = turn.targetText,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                                Text(
                                    text = turn.translation,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }

                        // User Expected reply / guidance
                        if (turn.expectedUserReply != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Column(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomEnd = 16.dp, bottomStart = 16.dp))
                                        .background(DuoGreenLight)
                                        .border(1.5.dp, DuoGreen, RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomEnd = 16.dp, bottomStart = 16.dp))
                                        .padding(12.dp),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = "Tu respuesta esperada:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DuoGreen
                                    )
                                    Text(
                                        text = turn.expectedUserReply,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp,
                                        color = Color(0xFF2E7D32),
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Waveform & Mic Bar in dialogue
                WaveformVisualizer(
                    isListening = isListening,
                    rmsDb = rmsDb,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (partialText.isNotBlank()) {
                    Text(
                        text = "🎙️ \"$partialText\"",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = DuoGreen,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                // Mic controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PulsingMicHalo(isListening = isListening, rmsDb = rmsDb) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(if (isListening) DuoRed else DuoGreen)
                                .clickable {
                                    if (isListening) viewModel.stopListening()
                                    else startDialogueMicWithPermission()
                                }
                                .testTag("dialogue_mic_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "Responder",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }

                if (isListening) {
                    DuoButton(
                        text = "Listo / Terminar de Hablar",
                        onClick = { viewModel.stopListening() },
                        colorStyle = DuoButtonColor.ORANGE,
                        modifier = Modifier
                            .fillMaxWidth(0.75f)
                            .padding(bottom = 12.dp),
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        testTag = "dialogue_stop_speaking_button"
                    )
                }

                // Alternative Voice options
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // System STT Dialog
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.5.dp, DuoBlue, RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .clickable {
                                try {
                                    systemSpeechLauncher.launch(viewModel.createSpeechIntent())
                                } catch (e: Exception) {
                                    viewModel.startListeningForDialogue()
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 8.dp)
                            .testTag("dialogue_system_stt_button"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardVoice,
                            contentDescription = "Voz del sistema",
                            tint = DuoBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Voz Sistema",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = DuoBlue,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    // Fallback demo for dialogue
                    currentTurn?.expectedUserReply?.let { expected ->
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.5.dp, DuoCardBorder, RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .clickable { viewModel.simulateSpeakingDemo(expected) }
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                                .testTag("dialogue_demo_speech_button"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "⚡ Demo OK",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                        }

                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.5.dp, DuoRed.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .background(Color(0xFFFFF5F5))
                                .clickable { viewModel.simulatePronunciationError() }
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                                .testTag("dialogue_error_speech_button"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "💔 Error (-1)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = DuoRed
                            )
                        }
                    }
                }
            }

            // Lottie-style Vector & Particle Celebration Overlay when dialogue turn is passed
            PronunciationSuccessCelebrationOverlay(
                celebrationState = celebrationState,
                onReplayCelebration = { viewModel.replayCelebrationEffect() },
                modifier = Modifier.align(Alignment.TopCenter)
            )

            // Evaluation sheet
            PronunciationResultSheet(
                result = result,
                onContinue = { viewModel.advanceDialogueTurn() },
                onRetry = { viewModel.retryCurrentExercise() },
                onWordClick = { word -> viewModel.speakWord(word) },
                remainingLives = livesState.currentLives,
                maxLives = livesState.maxLives,
                onReplayFeedbackSound = {
                    if (result?.isPassed == true) {
                        viewModel.replayCelebrationEffect()
                    } else {
                        viewModel.replayLastFeedbackSound()
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            if (livesState.isLevelRestartedByZeroLives) {
                LevelRestartedByZeroLivesDialog(
                    lastFailedResult = livesState.lastFailedResultBeforeRestart,
                    levelRestartCount = livesState.levelRestartCount,
                    onConfirmRestart = { viewModel.acknowledgeLevelRestart() },
                    onExitToMap = {
                        viewModel.acknowledgeLevelRestart()
                        viewModel.navigateTo(AppScreen.PATH_LEARN)
                    }
                )
            }
        }
    }
}
