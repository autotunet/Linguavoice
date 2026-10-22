package com.example.ui.screens

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardVoice
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.audio.SpeechState
import com.example.data.model.LessonModule
import com.example.data.model.WordAccuracy
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
import com.example.ui.viewmodel.RecognitionStatus

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SpeakingExerciseScreen(
    viewModel: LanguageGameViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val exercise by viewModel.currentExercise.collectAsState()
    val activeModule by viewModel.activeModule.collectAsState()
    val isCompleted by viewModel.isLessonCompleted.collectAsState()
    val speechState by viewModel.speechState.collectAsState()
    val pronunciationState by viewModel.pronunciationUiState.collectAsState()
    val result by viewModel.evaluationResult.collectAsState()
    val isTtsSpeaking by viewModel.isTtsSpeaking.collectAsState()
    val livesState by viewModel.livesState.collectAsState()
    val levelProgress by viewModel.levelProgressState.collectAsState()
    val celebrationState by viewModel.celebrationState.collectAsState()

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startListeningForExercise()
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

    if (isCompleted) {
        LessonCompleteView(
            module = activeModule,
            onFinish = {
                val act = context as? Activity
                if (act != null) {
                    com.example.ads.AdMobManager.showAutomaticInterstitial(act) {
                        viewModel.navigateTo(AppScreen.PATH_LEARN)
                    }
                } else {
                    viewModel.navigateTo(AppScreen.PATH_LEARN)
                }
            }
        )
        return
    }

    val currentEx = exercise ?: return

    val isListening = pronunciationState.isListening || speechState is SpeechState.Listening
    val rmsDb = if (pronunciationState.isListening) {
        pronunciationState.rmsDb
    } else if (speechState is SpeechState.Listening) {
        (speechState as SpeechState.Listening).rmsDb
    } else {
        0f
    }
    val liveTranscript = pronunciationState.partialSpokenText.ifBlank {
        if (speechState is SpeechState.Listening) (speechState as SpeechState.Listening).partialText else ""
    }
    val liveWords = pronunciationState.liveWordAccuracies
    val matchedWordsCount = liveWords.count { it.isCorrect || it.isPartiallyCorrect }

    val startMicWithPermissionCheck = {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            viewModel.startListeningForExercise()
        } else {
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Top Visual Progress Bar indicating the percentage of the current level completed
            CurrentLevelTopProgressBar(
                levelProgress = levelProgress,
                livesState = livesState,
                onCloseClick = { viewModel.navigateTo(AppScreen.PATH_LEARN) },
                onLivesClick = { viewModel.setHeartsDialogVisible(true) },
                closeButtonTestTag = "exercise_close_button"
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Mascot Prompt
            MascotBubble(
                message = currentEx.promptQuestion,
                subMessage = "Escucha atentamente el acento y repítelo con el micrófono."
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Main Phrase Card with Real-Time Word Highlighting
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Audio Controls: Normal and Turtle/Slow mode
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Normal Audio Button
                        Row(
                            modifier = Modifier
                                .testTag("audio_normal_btn")
                                .clip(RoundedCornerShape(16.dp))
                                .border(2.dp, DuoBlue, RoundedCornerShape(16.dp))
                                .background(if (isTtsSpeaking) DuoBlue.copy(alpha = 0.15f) else Color.White)
                                .clickable { viewModel.speakTargetPhrase(1.0f) }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Escuchar normal",
                                tint = DuoBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Escuchar",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = DuoBlue
                            )
                        }

                        // Turtle / Slow audio mode
                        Row(
                            modifier = Modifier
                                .testTag("audio_slow_btn")
                                .clip(RoundedCornerShape(16.dp))
                                .border(2.dp, DuoOrange, RoundedCornerShape(16.dp))
                                .background(Color.White)
                                .clickable { viewModel.speakTargetPhrase(0.65f) }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = "🐢", fontSize = 16.sp)
                            Text(
                                text = "Lento",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = DuoOrange
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Target Phrase Text
                    Text(
                        text = currentEx.targetPhrase,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        lineHeight = 28.sp
                    )

                    // Interactive Real-Time Word Chips (lights up word-by-word as SpeechRecognizer streams partial text)
                    if (liveWords.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("live_word_highlighting_row"),
                            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            liveWords.forEachIndexed { idx, wordItem ->
                                LiveWordPill(
                                    wordAccuracy = wordItem,
                                    isListening = isListening,
                                    onClick = { viewModel.speakWord(wordItem.targetWord) },
                                    modifier = Modifier.testTag("live_word_chip_$idx")
                                )
                            }
                        }

                        if (isListening || liveTranscript.isNotBlank()) {
                            Text(
                                text = "Palabras captadas en vivo: $matchedWordsCount/${liveWords.size} (toca una palabra para oírla)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (matchedWordsCount > 0) DuoGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }

                    // Phonetic transcription
                    Text(
                        text = currentEx.phoneticGuide,
                        fontSize = 14.sp,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    // Translation
                    Text(
                        text = currentEx.translation,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Speech Recognition Visualizer & Real-Time Voice-to-Text Stream
            WaveformVisualizer(
                isListening = isListening,
                rmsDb = rmsDb,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            // Real-time Speech-to-Text conversion status & live transcript box
            RealTimeSpeechTranscriptCard(
                status = pronunciationState.status,
                isListening = isListening,
                liveTranscript = liveTranscript,
                recognizedText = pronunciationState.recognizedText,
                matchedWordsCount = matchedWordsCount,
                totalWordsCount = liveWords.size
            )

            // Error display if speech had error
            val errorMsg = pronunciationState.errorMessage ?: (speechState as? SpeechState.Error)?.errorMessage
            if (!errorMsg.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFEAEA))
                        .padding(10.dp)
                        .testTag("speech_error_banner")
                ) {
                    Text(
                        text = errorMsg,
                        color = DuoRed,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Microphone Big Action Button with Pulsing Halo
            PulsingMicHalo(isListening = isListening, rmsDb = rmsDb) {
                Box(
                    modifier = Modifier
                        .size(86.dp)
                        .clip(CircleShape)
                        .background(if (isListening) DuoRed else DuoGreen)
                        .clickable {
                            if (isListening) {
                                viewModel.stopListening()
                            } else {
                                startMicWithPermissionCheck()
                            }
                        }
                        .testTag("microphone_main_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Hablar",
                        tint = Color.White,
                        modifier = Modifier.size(42.dp)
                    )
                }
            }

            Text(
                text = when {
                    isListening -> "¡Te escuchamos en tiempo real! Habla ahora..."
                    pronunciationState.status == RecognitionStatus.ANALYZING -> "Convirtiendo voz a texto y evaluando..."
                    else -> "Toca el micrófono y pronuncia la frase"
                },
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = if (isListening) DuoRed else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 12.dp)
            )

            if (isListening) {
                DuoButton(
                    text = "Listo / Terminar de Hablar",
                    onClick = { viewModel.stopListening() },
                    colorStyle = DuoButtonColor.ORANGE,
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .padding(top = 12.dp),
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    testTag = "stop_speaking_button"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Alternative STT & Testing options
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 120.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // System STT Dialog
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.5.dp, DuoBlue, RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .clickable {
                            try {
                                systemSpeechLauncher.launch(viewModel.createSpeechIntent())
                            } catch (e: Exception) {
                                startMicWithPermissionCheck()
                            }
                        }
                        .padding(horizontal = 8.dp, vertical = 10.dp)
                        .testTag("system_stt_button"),
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

                // Demo audio simulation (Pass)
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.5.dp, DuoCardBorder, RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .clickable {
                            viewModel.simulateSpeakingDemo(currentEx.targetPhrase)
                        }
                        .padding(horizontal = 8.dp, vertical = 10.dp)
                        .testTag("simulate_speech_demo"),
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

                // Demo pronunciation error simulation (Loses 1 life, restarts level at 0)
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.5.dp, DuoRed.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        .background(Color(0xFFFFF5F5))
                        .clickable {
                            viewModel.simulatePronunciationError()
                        }
                        .padding(horizontal = 8.dp, vertical = 10.dp)
                        .testTag("simulate_error_demo"),
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

        // Lottie-style Vector & Particle Celebration Overlay when exercise is passed
        PronunciationSuccessCelebrationOverlay(
            celebrationState = celebrationState,
            onReplayCelebration = { viewModel.replayCelebrationEffect() },
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // Bottom Result Evaluation Sheet
        PronunciationResultSheet(
            result = result,
            onContinue = { viewModel.onContinueNextExercise() },
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

        // Modal Dialog when lives reach 0 and the level restarts
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

@Composable
private fun LiveWordPill(
    wordAccuracy: WordAccuracy,
    isListening: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = when {
            wordAccuracy.isCorrect -> Color(0xFFD7FFB8)
            wordAccuracy.isPartiallyCorrect -> Color(0xFFFFF3CD)
            isListening -> Color(0xFFF3F6F9)
            else -> Color(0xFFF7F7F7)
        },
        animationSpec = tween(250),
        label = "liveWordBg"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            wordAccuracy.isCorrect -> DuoGreen
            wordAccuracy.isPartiallyCorrect -> DuoOrange
            else -> DuoCardBorder
        },
        animationSpec = tween(250),
        label = "liveWordBorder"
    )

    val textColor = when {
        wordAccuracy.isCorrect -> Color(0xFF2E7D32)
        wordAccuracy.isPartiallyCorrect -> Color(0xFFB78103)
        else -> Color(0xFF4B4B4B)
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (wordAccuracy.isCorrect) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = DuoGreen,
                modifier = Modifier.size(13.dp)
            )
        }
        Text(
            text = wordAccuracy.targetWord,
            fontSize = 13.sp,
            fontWeight = if (wordAccuracy.isCorrect || wordAccuracy.isPartiallyCorrect) FontWeight.Black else FontWeight.SemiBold,
            color = textColor
        )
    }
}

@Composable
private fun RealTimeSpeechTranscriptCard(
    status: RecognitionStatus,
    isListening: Boolean,
    liveTranscript: String,
    recognizedText: String,
    matchedWordsCount: Int,
    totalWordsCount: Int
) {
    val displayTranscript = liveTranscript.ifBlank { recognizedText }
    if (!isListening && displayTranscript.isBlank() && status != RecognitionStatus.ANALYZING) {
        return
    }

    val liveProgress = if (totalWordsCount > 0) {
        (matchedWordsCount.toFloat() / totalWordsCount.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .testTag("realtime_stt_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isListening) DuoGreenLight.copy(alpha = 0.45f) else Color(0xFFF4F9FF)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.5.dp,
            color = if (isListening) DuoGreen else DuoBlue.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = if (isListening) DuoGreen else DuoBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = when {
                            isListening -> "VOZ A TEXTO EN TIEMPO REAL"
                            status == RecognitionStatus.ANALYZING -> "PROCESANDO PRONUNCIACIÓN..."
                            else -> "TRANSCRIPCIÓN DETECTADA"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isListening) DuoGreen else DuoBlue
                    )
                }

                if (totalWordsCount > 0) {
                    Text(
                        text = "${(liveProgress * 100).toInt()}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isListening) DuoGreen else DuoBlue
                    )
                }
            }

            if (totalWordsCount > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { liveProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = DuoGreen,
                    trackColor = Color.White.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (displayTranscript.isNotBlank()) {
                    "🎙️ \"$displayTranscript${if (isListening) "..." else ""}\""
                } else {
                    "🎙️ Escuchando tu voz... pronuncia la frase"
                },
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFF1F2937),
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("live_partial_transcript_text")
            )
        }
    }
}

@Composable
fun LessonCompleteView(
    module: LessonModule?,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Mascot Celebration Image
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .border(4.dp, DuoGreen, CircleShape)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_mascot_celebrating),
                contentDescription = "Mascota celebrando",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "¡LECCIÓN COMPLETADA!",
            fontWeight = FontWeight.Black,
            fontSize = 24.sp,
            color = DuoGreen,
            letterSpacing = 1.sp
        )

        Text(
            text = module?.title ?: "Módulo finalizado",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Rewards summary card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(2.dp, DuoCardBorder, RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "TOTAL EXP", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = DuoOrange,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "+40",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = DuoOrange,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "PRECISIÓN", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Text(
                    text = "94%",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = DuoGreen
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        DuoButton(
            text = "CONTINUAR",
            onClick = onFinish,
            colorStyle = DuoButtonColor.GREEN,
            modifier = Modifier.fillMaxWidth(),
            testTag = "finish_lesson_button"
        )
    }
}
