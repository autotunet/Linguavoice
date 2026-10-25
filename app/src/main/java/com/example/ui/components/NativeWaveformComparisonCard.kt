package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.NativeAudioComparisonState
import com.example.audio.WaveformComparisonAnalysis
import com.example.data.model.PronunciationFeedbackStatus
import com.example.ui.theme.DuoBlue
import com.example.ui.theme.DuoCardBorder
import com.example.ui.theme.DuoGreen
import com.example.ui.theme.DuoGreenDark
import com.example.ui.theme.DuoOrange
import com.example.ui.theme.DuoPurple
import com.example.ui.theme.DuoRed
import com.example.ui.theme.LocalAppStrings
import java.util.Locale
import kotlin.math.abs

private enum class WaveformComparisonMode {
    OVERLAY,
    SPLIT_TRACKS
}

/**
 * Interactive component that plays the native speaker's `.wav` reference audio file
 * before the user records their own version, and renders a visual waveform comparison
 * (Overlay or Split tracks) between the native speaker and the user's recorded voice.
 */
@Composable
fun NativeWaveformComparisonCard(
    comparisonState: NativeAudioComparisonState,
    isUserListening: Boolean,
    liveRmsDb: Float,
    onPlayNativeAudio: (Float) -> Unit,
    onStopNativeAudio: () -> Unit,
    onStartUserRecording: () -> Unit,
    onPlayUserAudio: () -> Unit = {},
    onStopUserAudio: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    var displayMode by remember { mutableStateOf(WaveformComparisonMode.OVERLAY) }

    val durationSecondsText = remember(comparisonState.nativeDurationMs) {
        String.format(Locale.US, "%.1fs", comparisonState.nativeDurationMs / 1000f)
    }

    val animatedPlayhead by animateFloatAsState(
        targetValue = if (comparisonState.isPlayingNativeAudio) {
            comparisonState.nativePlaybackProgress.coerceIn(0f, 1f)
        } else {
            0f
        },
        animationSpec = tween(durationMillis = 120, easing = FastOutSlowInEasing),
        label = "nativeWavePlayhead"
    )

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("native_waveform_comparison_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Header: Native Reference Audio File Info + Listen Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = DuoBlue.copy(alpha = 0.14f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AudioFile,
                                contentDescription = "Archivo de audio nativo",
                                tint = DuoBlue,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "Hablante Nativo vs. Tu Voz",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${comparisonState.language.flagEmoji} ${comparisonState.nativeAudioFileName.ifBlank { "native_ref.wav" }} • $durationSecondsText",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.testTag("native_audio_filename_text")
                        )
                    }
                }

                // Step status badge ("Escucha antes de grabar" vs "Escuchado ✓")
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (comparisonState.hasListenedToNativeAudio) {
                        DuoGreen.copy(alpha = 0.14f)
                    } else {
                        DuoOrange.copy(alpha = 0.14f)
                    },
                    border = BorderStroke(
                        1.dp,
                        if (comparisonState.hasListenedToNativeAudio) DuoGreen.copy(alpha = 0.4f) else DuoOrange.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.testTag("native_audio_listened_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (comparisonState.hasListenedToNativeAudio) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = DuoGreenDark,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        Text(
                            text = if (comparisonState.hasListenedToNativeAudio) {
                                "Nativo escuchado"
                            } else {
                                "Paso 1: Escucha"
                            },
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp,
                            color = if (comparisonState.hasListenedToNativeAudio) DuoGreenDark else DuoOrange
                        )
                    }
                }
            }

            // 2. Native Audio File Playback Controls ("Reproducir archivo nativo antes de grabar")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Primary Play / Stop Native Audio File Button
                Row(
                    modifier = Modifier
                        .weight(1.35f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (comparisonState.isPlayingNativeAudio) DuoBlue else DuoBlue.copy(alpha = 0.12f)
                        )
                        .border(
                            1.5.dp,
                            DuoBlue,
                            RoundedCornerShape(14.dp)
                        )
                        .clickable {
                            if (comparisonState.isPlayingNativeAudio) {
                                onStopNativeAudio()
                            } else {
                                onPlayNativeAudio(1.0f)
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .testTag("play_native_audio_file_button"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (comparisonState.isPlayingNativeAudio) {
                            Icons.Default.Stop
                        } else {
                            Icons.Default.PlayArrow
                        },
                        contentDescription = "Reproducir archivo de hablante nativo",
                        tint = if (comparisonState.isPlayingNativeAudio) Color.White else DuoBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (comparisonState.isPlayingNativeAudio) {
                            "Reproduciendo Nativo..."
                        } else {
                            "Oír Hablante Nativo"
                        },
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        color = if (comparisonState.isPlayingNativeAudio) Color.White else DuoBlue
                    )
                }

                // Slow Native Reference Audio (0.75x)
                Row(
                    modifier = Modifier
                        .weight(0.85f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(DuoPurple.copy(alpha = 0.10f))
                        .border(1.5.dp, DuoPurple.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                        .clickable { onPlayNativeAudio(0.75f) }
                        .padding(horizontal = 10.dp, vertical = 10.dp)
                        .testTag("play_native_audio_slow_button"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Audio nativo lento",
                        tint = DuoPurple,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "0.75x",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = DuoPurple
                    )
                }
            }

            // 2b. Play back user's own recorded microphone voice ("Escuchar lo que el usuario habló en el micrófono")
            if (comparisonState.hasUserRecordedAudio) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (comparisonState.isPlayingUserAudio) DuoGreen else DuoGreen.copy(alpha = 0.12f)
                        )
                        .border(1.5.dp, DuoGreen, RoundedCornerShape(12.dp))
                        .clickable {
                            if (comparisonState.isPlayingUserAudio) {
                                onStopUserAudio()
                            } else {
                                onPlayUserAudio()
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 9.dp)
                        .testTag("play_user_recorded_audio_button"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (comparisonState.isPlayingUserAudio) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = strings.playMyRecordingBtn,
                            tint = if (comparisonState.isPlayingUserAudio) Color.White else DuoGreenDark,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = if (comparisonState.isPlayingUserAudio) {
                                    strings.playingMyRecordingBadge
                                } else {
                                    strings.playMyRecordingBtn
                                },
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                color = if (comparisonState.isPlayingUserAudio) Color.White else DuoGreenDark
                            )
                            if (comparisonState.lastRecognizedSpokenText.isNotBlank()) {
                                Text(
                                    text = "«${comparisonState.lastRecognizedSpokenText}»",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (comparisonState.isPlayingUserAudio) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (comparisonState.isPlayingUserAudio) Color.White.copy(alpha = 0.2f) else DuoGreen.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = comparisonState.userAudioFileName.ifBlank { "user_rec.wav" },
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (comparisonState.isPlayingUserAudio) Color.White else DuoGreenDark,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // 3. Legend & Display Mode Toggle (Superpuesta vs. Lado a Lado)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LegendDot(color = DuoBlue, label = "Hablante Nativo")
                    LegendDot(
                        color = if (comparisonState.hasUserWaveform || isUserListening) DuoGreen else Color.Gray,
                        label = if (isUserListening) "Tu Voz (Grabando...)" else "Tu Grabación"
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = displayMode == WaveformComparisonMode.OVERLAY,
                        onClick = { displayMode = WaveformComparisonMode.OVERLAY },
                        label = {
                            Text(
                                text = "Superpuesta",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DuoBlue.copy(alpha = 0.15f),
                            selectedLabelColor = DuoBlue
                        ),
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("waveform_mode_overlay_chip")
                    )
                    FilterChip(
                        selected = displayMode == WaveformComparisonMode.SPLIT_TRACKS,
                        onClick = { displayMode = WaveformComparisonMode.SPLIT_TRACKS },
                        label = {
                            Text(
                                text = "Paralela",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DuoPurple.copy(alpha = 0.15f),
                            selectedLabelColor = DuoPurple
                        ),
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("waveform_mode_split_chip")
                    )
                }
            }

            // 4. Dual Waveform Canvas Visualizer
            val nativeAmps = comparisonState.nativeWaveformAmplitudes
            val activeUserAmps = when {
                isUserListening && comparisonState.liveUserRecordingAmplitudes.isNotEmpty() ->
                    comparisonState.liveUserRecordingAmplitudes
                comparisonState.userWaveformAmplitudes.isNotEmpty() ->
                    comparisonState.userWaveformAmplitudes
                else -> emptyList()
            }

            if (displayMode == WaveformComparisonMode.OVERLAY) {
                OverlayWaveformCanvas(
                    nativeAmplitudes = nativeAmps,
                    userAmplitudes = activeUserAmps,
                    isPlayingNative = comparisonState.isPlayingNativeAudio,
                    playheadProgress = animatedPlayhead,
                    isUserRecording = isUserListening,
                    liveRmsDb = liveRmsDb
                )
            } else {
                SplitTracksWaveformCanvas(
                    nativeAmplitudes = nativeAmps,
                    userAmplitudes = activeUserAmps,
                    isPlayingNative = comparisonState.isPlayingNativeAudio,
                    playheadProgress = animatedPlayhead,
                    isUserRecording = isUserListening
                )
            }

            // 5. Quantitative Waveform Comparison Metrics (shown once user has recorded their version)
            AnimatedVisibility(
                visible = comparisonState.comparisonAnalysis != null,
                enter = fadeIn() + expandVertically()
            ) {
                comparisonState.comparisonAnalysis?.let { analysis ->
                    WaveformComparisonMetricsSection(
                        analysis = analysis,
                        onReplayNative = { onPlayNativeAudio(1.0f) },
                        onReRecordUser = onStartUserRecording
                    )
                }
            }

            // Prompt if user hasn't recorded yet
            if (!comparisonState.hasUserWaveform && !isUserListening) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CompareArrows,
                            contentDescription = null,
                            tint = DuoBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (comparisonState.hasListenedToNativeAudio) {
                                "¡Listo! Ahora graba tu voz para superponer y comparar tu forma de onda con la del nativo."
                            } else {
                                "Escucha primero el audio del hablante nativo y luego graba tu versión para comparar ambas ondas."
                            },
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendDot(
    color: Color,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun OverlayWaveformCanvas(
    nativeAmplitudes: List<Float>,
    userAmplitudes: List<Float>,
    isPlayingNative: Boolean,
    playheadProgress: Float,
    isUserRecording: Boolean,
    liveRmsDb: Float
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF5F9FF),
        border = BorderStroke(1.dp, DuoBlue.copy(alpha = 0.22f)),
        modifier = Modifier
            .fillMaxWidth()
            .height(104.dp)
            .testTag("overlay_waveform_canvas")
    ) {
        Canvas(modifier = Modifier.fillMaxWidth()) {
            val barCount = nativeAmplitudes.size.coerceAtLeast(24)
            val width = size.width
            val height = size.height
            val centerY = height / 2f
            val horizontalPadding = 12.dp.toPx()
            val usableWidth = (width - horizontalPadding * 2f).coerceAtLeast(1f)
            val slotWidth = usableWidth / barCount.toFloat()
            val barWidth = (slotWidth * 0.56f).coerceAtLeast(3.dp.toPx())

            // Center baseline axis
            drawLine(
                color = DuoBlue.copy(alpha = 0.18f),
                start = Offset(horizontalPadding, centerY),
                end = Offset(width - horizontalPadding, centerY),
                strokeWidth = 1.dp.toPx()
            )

            // Draw Native Speaker Waveform bars + smooth upper envelope path
            val nativeEnvelopePath = Path()
            for (i in 0 until barCount) {
                val amp = nativeAmplitudes.getOrElse(i) { 0.25f }.coerceIn(0.08f, 1.0f)
                val barHeight = amp * (height * 0.78f)
                val xCenter = horizontalPadding + i * slotWidth + slotWidth / 2f
                val topY = centerY - barHeight / 2f

                val isPlayedSlice = isPlayingNative && (i.toFloat() / barCount.toFloat()) <= playheadProgress
                val nativeBarColor = if (isPlayedSlice) {
                    DuoBlue
                } else {
                    DuoBlue.copy(alpha = if (userAmplitudes.isNotEmpty()) 0.38f else 0.78f)
                }

                drawRoundRect(
                    color = nativeBarColor,
                    topLeft = Offset(xCenter - barWidth / 2f, topY),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                )

                if (i == 0) {
                    nativeEnvelopePath.moveTo(xCenter, topY)
                } else {
                    nativeEnvelopePath.lineTo(xCenter, topY)
                }
            }

            drawPath(
                path = nativeEnvelopePath,
                color = DuoBlue.copy(alpha = 0.55f),
                style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw User Recorded Waveform bars overlaid for direct peak-to-peak comparison
            if (userAmplitudes.isNotEmpty()) {
                val userEnvelopePath = Path()
                val userBarWidth = (barWidth * 0.68f).coerceAtLeast(2.5.dp.toPx())
                for (i in 0 until barCount) {
                    val uAmp = userAmplitudes.getOrElse(i) { 0.15f }.coerceIn(0.08f, 1.0f)
                    val nAmp = nativeAmplitudes.getOrElse(i) { 0.25f }.coerceIn(0.08f, 1.0f)
                    val diff = abs(nAmp - uAmp)
                    val userHeight = uAmp * (height * 0.78f)
                    val xCenter = horizontalPadding + i * slotWidth + slotWidth / 2f
                    val topY = centerY - userHeight / 2f

                    val barColor = when {
                        isUserRecording -> DuoGreen
                        diff <= 0.18f -> DuoGreen
                        diff <= 0.32f -> DuoOrange
                        else -> DuoRed
                    }

                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(xCenter - userBarWidth / 2f, topY),
                        size = Size(userBarWidth, userHeight),
                        cornerRadius = CornerRadius(userBarWidth / 2f, userBarWidth / 2f)
                    )

                    if (i == 0) {
                        userEnvelopePath.moveTo(xCenter, topY)
                    } else {
                        userEnvelopePath.lineTo(xCenter, topY)
                    }
                }

                drawPath(
                    path = userEnvelopePath,
                    color = DuoGreenDark.copy(alpha = 0.75f),
                    style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
                )
            } else if (isUserRecording) {
                // Live pulse indicator when recording starts
                val pulseAmp = ((liveRmsDb + 2f) / 12f).coerceIn(0.15f, 0.95f)
                val pulseRadius = (pulseAmp * 18.dp.toPx()).coerceAtLeast(6.dp.toPx())
                drawCircle(
                    color = DuoGreen.copy(alpha = 0.35f),
                    radius = pulseRadius,
                    center = Offset(width / 2f, centerY)
                )
            }

            // Playhead vertical line during native audio playback
            if (isPlayingNative) {
                val playheadX = horizontalPadding + playheadProgress * usableWidth
                drawLine(
                    color = DuoOrange,
                    start = Offset(playheadX, 6.dp.toPx()),
                    end = Offset(playheadX, height - 6.dp.toPx()),
                    strokeWidth = 2.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
private fun SplitTracksWaveformCanvas(
    nativeAmplitudes: List<Float>,
    userAmplitudes: List<Float>,
    isPlayingNative: Boolean,
    playheadProgress: Float,
    isUserRecording: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("split_tracks_waveform_container"),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Track 1: Native Speaker Reference
        SingleTrackWaveformRow(
            title = "Onda del Hablante Nativo (Referencia)",
            amplitudes = nativeAmplitudes,
            primaryColor = DuoBlue,
            backgroundColor = Color(0xFFF3F8FF),
            playheadProgress = if (isPlayingNative) playheadProgress else null
        )

        // Track 2: User's Recorded Version
        SingleTrackWaveformRow(
            title = if (userAmplitudes.isNotEmpty()) {
                "Tu Forma de Onda Grabada"
            } else if (isUserRecording) {
                "Capturando tu forma de onda en vivo..."
            } else {
                "Tu Forma de Onda (Graba tu voz para comparar)"
            },
            amplitudes = if (userAmplitudes.isNotEmpty()) {
                userAmplitudes
            } else {
                List(nativeAmplitudes.size.coerceAtLeast(24)) { 0.10f }
            },
            primaryColor = if (userAmplitudes.isNotEmpty() || isUserRecording) DuoGreen else Color.LightGray,
            backgroundColor = Color(0xFFF4FBF0),
            playheadProgress = null
        )
    }
}

@Composable
private fun SingleTrackWaveformRow(
    title: String,
    amplitudes: List<Float>,
    primaryColor: Color,
    backgroundColor: Color,
    playheadProgress: Float?
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = primaryColor
            )
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
            ) {
                val barCount = amplitudes.size.coerceAtLeast(24)
                val slotWidth = size.width / barCount.toFloat()
                val barWidth = (slotWidth * 0.55f).coerceAtLeast(2.5.dp.toPx())
                val centerY = size.height / 2f

                for (i in 0 until barCount) {
                    val amp = amplitudes.getOrElse(i) { 0.15f }.coerceIn(0.08f, 1.0f)
                    val barH = amp * (size.height * 0.85f)
                    val x = i * slotWidth + (slotWidth - barWidth) / 2f
                    drawRoundRect(
                        color = primaryColor,
                        topLeft = Offset(x, centerY - barH / 2f),
                        size = Size(barWidth, barH),
                        cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                    )
                }

                if (playheadProgress != null) {
                    val px = playheadProgress * size.width
                    drawLine(
                        color = DuoOrange,
                        start = Offset(px, 0f),
                        end = Offset(px, size.height),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }
        }
    }
}

@Composable
private fun WaveformComparisonMetricsSection(
    analysis: WaveformComparisonAnalysis,
    onReplayNative: () -> Unit,
    onReRecordUser: () -> Unit
) {
    val statusColor = when (analysis.status) {
        PronunciationFeedbackStatus.CORRECT -> DuoGreenDark
        PronunciationFeedbackStatus.ALMOST_CORRECT -> DuoOrange
        PronunciationFeedbackStatus.INCORRECT -> DuoRed
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(statusColor.copy(alpha = 0.08f))
            .border(1.dp, statusColor.copy(alpha = 0.32f), RoundedCornerShape(16.dp))
            .padding(12.dp)
            .testTag("waveform_comparison_metrics_section"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
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
                    imageVector = Icons.Default.Equalizer,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Resultado de Comparación de Onda",
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    color = statusColor
                )
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = statusColor,
                modifier = Modifier.testTag("waveform_similarity_badge")
            ) {
                Text(
                    text = "${analysis.waveformSimilarityPercentage}% similitud",
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        // 3 Acoustic Comparison Bars: Similitud de Onda, Ritmo, Entonación
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WaveformMetricChip(
                label = "Similitud Onda",
                percentage = analysis.waveformSimilarityPercentage,
                color = statusColor,
                modifier = Modifier.weight(1f)
            )
            WaveformMetricChip(
                label = "Sincronía Ritmo",
                percentage = analysis.rhythmMatchPercentage,
                color = DuoBlue,
                modifier = Modifier.weight(1f)
            )
            WaveformMetricChip(
                label = "Entonación",
                percentage = analysis.intonationMatchPercentage,
                color = DuoPurple,
                modifier = Modifier.weight(1f)
            )
        }

        Text(
            text = analysis.coachingSummaryEs,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        HorizontalDivider(color = statusColor.copy(alpha = 0.2f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White)
                    .border(1.dp, DuoBlue.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .clickable { onReplayNative() }
                    .padding(vertical = 7.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = null,
                    tint = DuoBlue,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Oír Nativo",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = DuoBlue
                )
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White)
                    .border(1.dp, DuoGreen.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .clickable { onReRecordUser() }
                    .padding(vertical = 7.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = DuoGreenDark,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Grabar de Nuevo",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = DuoGreenDark
                )
            }
        }
    }
}

@Composable
private fun WaveformMetricChip(
    label: String,
    percentage: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$percentage%",
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                color = color
            )
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Displays real-time and post-recording microphone speech recognition verification:
 * - What question/words the app just asked or said
 * - What the microphone heard and recognized from the user's voice
 * - Button to listen to the user's recorded voice
 * - Whether what the user said is Correct, Almost Correct, or Incorrect
 */
@Composable
fun MicrophoneRecognitionVerificationCard(
    appPromptOrQuestion: String,
    expectedTargetWords: String,
    recognizedSpokenText: String,
    isListening: Boolean,
    evaluationStatus: PronunciationFeedbackStatus?,
    scorePercentage: Int?,
    isPlayingUserAudio: Boolean,
    onPlayUserAudio: () -> Unit,
    onStopUserAudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val statusColor = when (evaluationStatus) {
        PronunciationFeedbackStatus.CORRECT -> DuoGreenDark
        PronunciationFeedbackStatus.ALMOST_CORRECT -> DuoOrange
        PronunciationFeedbackStatus.INCORRECT -> DuoRed
        null -> if (isListening) DuoBlue else MaterialTheme.colorScheme.primary
    }

    val statusBadgeText = when (evaluationStatus) {
        PronunciationFeedbackStatus.CORRECT -> strings.statusCorrect
        PronunciationFeedbackStatus.ALMOST_CORRECT -> strings.statusAlmostCorrect
        PronunciationFeedbackStatus.INCORRECT -> strings.statusIncorrect
        null -> if (isListening) strings.micListeningAndRecognizing else strings.voiceRecordedReadyBadge
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("mic_question_recognition_verification_card"),
        shape = RoundedCornerShape(16.dp),
        color = statusColor.copy(alpha = 0.08f),
        border = BorderStroke(1.5.dp, statusColor.copy(alpha = 0.38f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = strings.evaluationStatusLabel,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        color = statusColor
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor,
                    modifier = Modifier.testTag("mic_recognition_verdict_badge")
                ) {
                    Text(
                        text = if (scorePercentage != null) "$statusBadgeText ($scorePercentage%)" else statusBadgeText,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            if (appPromptOrQuestion.isNotBlank()) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = strings.appAskedOrSaidLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = appPromptOrQuestion,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = strings.targetPhraseToSayLabel,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "«$expectedTargetWords»",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = DuoBlue
                )
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, statusColor.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.whatYouSpokeLabel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (recognizedSpokenText.isNotBlank()) {
                                "🎙️ «$recognizedSpokenText»"
                            } else {
                                "🎙️ ..."
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = statusColor,
                            modifier = Modifier.testTag("mic_recognized_spoken_text")
                        )
                    }

                    if (!isListening && recognizedSpokenText.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isPlayingUserAudio) DuoGreen else DuoGreen.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, DuoGreen),
                            modifier = Modifier
                                .clickable {
                                    if (isPlayingUserAudio) onStopUserAudio() else onPlayUserAudio()
                                }
                                .testTag("listen_user_mic_recording_chip")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPlayingUserAudio) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = strings.playMyRecordingBtn,
                                    tint = if (isPlayingUserAudio) Color.White else DuoGreenDark,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = if (isPlayingUserAudio) strings.stopMyRecordingBtn else strings.playMyRecordingBtn,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isPlayingUserAudio) Color.White else DuoGreenDark
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

