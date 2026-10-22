package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DuoGreen
import com.example.ui.theme.DuoGreenLight
import kotlin.random.Random

@Composable
fun WaveformVisualizer(
    isListening: Boolean,
    rmsDb: Float,
    modifier: Modifier = Modifier
) {
    val barCount = 18
    val infiniteTransition = rememberInfiniteTransition(label = "wave_anim")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(DuoGreenLight.copy(alpha = 0.35f)),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val normalizedVolume = (rmsDb / 10f).coerceIn(0.1f, 1.2f)

        for (i in 0 until barCount) {
            val animBarHeight = if (isListening) {
                val waveOffset = kotlin.math.sin(phase + (i * 0.4f)).toFloat()
                (14.dp + (32.dp * (0.4f + 0.6f * normalizedVolume) * ((waveOffset + 1.2f) / 2.2f))).coerceIn(8.dp, 48.dp)
            } else {
                8.dp
            }

            Box(
                modifier = Modifier
                    .size(width = 6.dp, height = animBarHeight)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (isListening) DuoGreen else Color.LightGray.copy(alpha = 0.6f))
            )
        }
    }
}

@Composable
fun PulsingMicHalo(
    isListening: Boolean,
    rmsDb: Float,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "halo_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "halo_scale"
    )

    val haloAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "halo_alpha"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        if (isListening) {
            val dynamicScale = (pulseScale + (rmsDb / 20f)).coerceIn(1.1f, 1.6f)
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(dynamicScale)
                    .clip(CircleShape)
                    .background(DuoGreen.copy(alpha = haloAlpha))
            )
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .scale(dynamicScale * 0.85f)
                    .clip(CircleShape)
                    .background(DuoGreenLight.copy(alpha = 0.5f))
            )
        }
        content()
    }
}
