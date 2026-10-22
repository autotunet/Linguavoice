package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DuoBlue
import com.example.ui.theme.DuoGreen
import com.example.ui.theme.DuoGreenDark
import com.example.ui.theme.DuoOrange
import com.example.ui.theme.DuoPurple
import com.example.ui.theme.DuoYellow
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

enum class CelebrationTier {
    PERFECT,
    EXCELLENT,
    PASSED;

    companion object {
        fun fromScore(scorePercentage: Int): CelebrationTier {
            return when {
                scorePercentage >= 90 -> PERFECT
                scorePercentage >= 80 -> EXCELLENT
                else -> PASSED
            }
        }
    }
}

data class CelebrationEffectState(
    val isActive: Boolean = false,
    val triggerId: Int = 0,
    val totalCelebrationsTriggered: Int = 0,
    val scorePercentage: Int = 0,
    val xpEarned: Int = 0,
    val tier: CelebrationTier = CelebrationTier.PASSED
)

private enum class ParticleShape {
    CONFETTI_RIBBON,
    FIVE_POINT_STAR,
    FOUR_POINT_SPARKLE,
    GLOW_CIRCLE,
    DIAMOND
}

private data class CelebrationParticle(
    val id: Int,
    val originXFraction: Float,
    val originYFraction: Float,
    val velocityX: Float,
    val velocityY: Float,
    val gravity: Float,
    val baseSize: Float,
    val rotationSpeed: Float,
    val initialRotation: Float,
    val tumblePhase: Float,
    val color: Color,
    val shape: ParticleShape,
    val delayFraction: Float
)

/**
 * Full-screen Lottie-style vector animation and multi-emitter particle burst overlay
 * triggered when a user successfully completes a pronunciation exercise.
 */
@Composable
fun PronunciationSuccessCelebrationOverlay(
    celebrationState: CelebrationEffectState,
    onReplayCelebration: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = celebrationState.isActive,
        enter = fadeIn(animationSpec = tween(180)) + scaleIn(
            initialScale = 0.92f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        ),
        exit = fadeOut(animationSpec = tween(250)) + scaleOut(targetScale = 0.95f),
        modifier = modifier
            .fillMaxSize()
            .testTag("pronunciation_celebration_overlay")
    ) {
        val particleProgress = remember(celebrationState.triggerId) { Animatable(0f) }
        val badgePopScale = remember(celebrationState.triggerId) { Animatable(0.3f) }
        val checkStrokeProgress = remember(celebrationState.triggerId) { Animatable(0f) }
        val ringExpandProgress = remember(celebrationState.triggerId) { Animatable(0f) }

        LaunchedEffect(celebrationState.triggerId) {
            coroutineScope {
                launch {
                    particleProgress.snapTo(0f)
                    particleProgress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(
                            durationMillis = 2600,
                            easing = FastOutSlowInEasing
                        )
                    )
                }
                launch {
                    badgePopScale.snapTo(0.3f)
                    badgePopScale.animateTo(
                        targetValue = 1f,
                        animationSpec = spring(
                            dampingRatio = 0.52f,
                            stiffness = Spring.StiffnessMedium
                        )
                    )
                }
                launch {
                    ringExpandProgress.snapTo(0f)
                    ringExpandProgress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(
                            durationMillis = 1200,
                            easing = FastOutSlowInEasing
                        )
                    )
                }
                launch {
                    checkStrokeProgress.snapTo(0f)
                    checkStrokeProgress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(
                            durationMillis = 650,
                            delayMillis = 140,
                            easing = FastOutSlowInEasing
                        )
                    )
                }
            }
        }

        val infiniteTransition = rememberInfiniteTransition(label = "lottie_celebration_loop")
        val sunburstRotation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 9000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "sunburst_rotation"
        )
        val idlePulse by infiniteTransition.animateFloat(
            initialValue = 0.97f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 950, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "idle_pulse"
        )

        val particles = remember(celebrationState.triggerId, celebrationState.tier) {
            val count = when (celebrationState.tier) {
                CelebrationTier.PERFECT -> 76
                CelebrationTier.EXCELLENT -> 60
                CelebrationTier.PASSED -> 48
            }
            generateCelebrationParticles(
                seed = celebrationState.triggerId * 97 + celebrationState.scorePercentage,
                count = count
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            // 1. Physics-driven multi-emitter particle burst (confetti, stars, sparkles, diamonds)
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("celebration_particles_canvas")
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val t = particleProgress.value

                if (t < 0.99f && canvasWidth > 0f && canvasHeight > 0f) {
                    particles.forEach { p ->
                        val localT = ((t - p.delayFraction) / (1f - p.delayFraction)).coerceIn(0f, 1f)
                        if (localT > 0f) {
                            val startX = p.originXFraction * canvasWidth
                            val startY = p.originYFraction * canvasHeight

                            // Parabolic trajectory with air drag
                            val dragFactor = 1f - 0.28f * localT
                            val currentX = startX + (p.velocityX * canvasWidth * localT * dragFactor) +
                                sin(localT * 8f + p.tumblePhase) * 18f
                            val currentY = startY +
                                (p.velocityY * canvasHeight * localT) +
                                (0.5f * p.gravity * canvasHeight * localT * localT)

                            // Fade in quickly, stay bright, fade out smoothly near the end
                            val alpha = when {
                                localT < 0.08f -> (localT / 0.08f).coerceIn(0f, 1f)
                                localT > 0.72f -> (1f - (localT - 0.72f) / 0.28f).coerceIn(0f, 1f)
                                else -> 1f
                            }

                            if (alpha > 0.02f && currentY in -80f..(canvasHeight + 80f)) {
                                val currentRotation = p.initialRotation + p.rotationSpeed * localT * 360f
                                val tumbleScaleX = (0.35f + 0.65f * abs(cos(localT * PI.toFloat() * 3f + p.tumblePhase)))

                                withTransform({
                                    translate(left = currentX, top = currentY)
                                    rotate(degrees = currentRotation, pivot = Offset.Zero)
                                    scale(scaleX = tumbleScaleX, scaleY = 1f, pivot = Offset.Zero)
                                }) {
                                    drawCelebrationParticleShape(
                                        shape = p.shape,
                                        color = p.color.copy(alpha = alpha),
                                        radius = p.baseSize
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Lottie-style animated vector medallion & XP banner floating at top-center
            CelebrationLottieVectorBadge(
                celebrationState = celebrationState,
                badgeScale = badgePopScale.value * idlePulse,
                ringProgress = ringExpandProgress.value,
                checkProgress = checkStrokeProgress.value,
                sunburstRotation = sunburstRotation,
                onReplayClick = onReplayCelebration,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 74.dp, start = 16.dp, end = 16.dp)
            )
        }
    }
}

@Composable
private fun CelebrationLottieVectorBadge(
    celebrationState: CelebrationEffectState,
    badgeScale: Float,
    ringProgress: Float,
    checkProgress: Float,
    sunburstRotation: Float,
    onReplayClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val primaryColor = when (celebrationState.tier) {
        CelebrationTier.PERFECT -> DuoYellow
        CelebrationTier.EXCELLENT -> DuoGreen
        CelebrationTier.PASSED -> DuoBlue
    }

    val accentColor = when (celebrationState.tier) {
        CelebrationTier.PERFECT -> DuoOrange
        CelebrationTier.EXCELLENT -> DuoGreenDark
        CelebrationTier.PASSED -> DuoPurple
    }

    val badgeTitle = when (celebrationState.tier) {
        CelebrationTier.PERFECT -> "¡PRONUNCIACIÓN PERFECTA!"
        CelebrationTier.EXCELLENT -> "¡EXCELENTE PRONUNCIACIÓN!"
        CelebrationTier.PASSED -> "¡PRONUNCIACIÓN SUPERADA!"
    }

    Surface(
        modifier = modifier
            .scale(badgeScale)
            .testTag("celebration_lottie_badge"),
        shape = RoundedCornerShape(24.dp),
        color = Color.White.copy(alpha = 0.96f),
        shadowElevation = 10.dp
    ) {
        Row(
            modifier = Modifier
                .border(
                    width = 2.dp,
                    brush = Brush.horizontalGradient(
                        listOf(primaryColor, DuoGreen, DuoBlue)
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .clickable(enabled = onReplayClick != null) {
                    onReplayClick?.invoke()
                }
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Animated Lottie-style Vector Emblem Canvas
            Box(
                modifier = Modifier.size(56.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val maxRadius = size.minDimension / 2f

                    // Expanding shockwave rings
                    if (ringProgress in 0.01f..0.99f) {
                        val ringAlpha = (1f - ringProgress).coerceIn(0f, 1f)
                        drawCircle(
                            color = primaryColor.copy(alpha = ringAlpha * 0.55f),
                            radius = maxRadius * (0.65f + 0.55f * ringProgress),
                            center = center,
                            style = Stroke(width = 3.dp.toPx())
                        )
                        drawCircle(
                            color = DuoGreen.copy(alpha = ringAlpha * 0.4f),
                            radius = maxRadius * (0.5f + 0.7f * ringProgress),
                            center = center,
                            style = Stroke(width = 1.8.dp.toPx())
                        )
                    }

                    // Rotating Lottie-style sunburst rays
                    withTransform({
                        rotate(degrees = sunburstRotation, pivot = center)
                    }) {
                        val rayCount = 12
                        for (i in 0 until rayCount) {
                            val angleRad = (i * (360f / rayCount)) * (PI.toFloat() / 180f)
                            val innerR = maxRadius * 0.62f
                            val outerR = if (i % 2 == 0) maxRadius * 0.96f else maxRadius * 0.82f
                            val start = Offset(
                                x = center.x + cos(angleRad) * innerR,
                                y = center.y + sin(angleRad) * innerR
                            )
                            val end = Offset(
                                x = center.x + cos(angleRad) * outerR,
                                y = center.y + sin(angleRad) * outerR
                            )
                            drawLine(
                                color = if (i % 2 == 0) DuoYellow else DuoGreen.copy(alpha = 0.75f),
                                start = start,
                                end = end,
                                strokeWidth = 2.5.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }
                    }

                    // Medallion core circle with 3D rim
                    drawCircle(
                        color = accentColor,
                        radius = maxRadius * 0.60f,
                        center = Offset(center.x, center.y + 2.dp.toPx())
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(primaryColor, DuoGreen),
                            center = Offset(center.x - 4f, center.y - 4f),
                            radius = maxRadius * 0.65f
                        ),
                        radius = maxRadius * 0.58f,
                        center = center
                    )

                    // Orbiting vector mini-sparkles
                    val orbitAngles = listOf(35f, 145f, 225f, 315f)
                    orbitAngles.forEachIndexed { idx, deg ->
                        val rad = (deg + sunburstRotation * 0.5f) * (PI.toFloat() / 180f)
                        val sx = center.x + cos(rad) * (maxRadius * 0.78f)
                        val sy = center.y + sin(rad) * (maxRadius * 0.78f)
                        withTransform({
                            translate(left = sx, top = sy)
                        }) {
                            drawCelebrationParticleShape(
                                shape = if (idx % 2 == 0) ParticleShape.FOUR_POINT_SPARKLE else ParticleShape.FIVE_POINT_STAR,
                                color = if (idx % 2 == 0) DuoYellow else DuoBlue,
                                radius = 4.2.dp.toPx()
                            )
                        }
                    }

                    // Animated progressive vector checkmark inside the medallion
                    val p = checkProgress.coerceIn(0f, 1f)
                    if (p > 0f) {
                        val checkStart = Offset(center.x - maxRadius * 0.24f, center.y + maxRadius * 0.02f)
                        val checkMid = Offset(center.x - maxRadius * 0.05f, center.y + maxRadius * 0.20f)
                        val checkEnd = Offset(center.x + maxRadius * 0.27f, center.y - maxRadius * 0.16f)

                        val path = Path().apply {
                            moveTo(checkStart.x, checkStart.y)
                            if (p <= 0.4f) {
                                val segT = p / 0.4f
                                lineTo(
                                    x = checkStart.x + (checkMid.x - checkStart.x) * segT,
                                    y = checkStart.y + (checkMid.y - checkStart.y) * segT
                                )
                            } else {
                                lineTo(checkMid.x, checkMid.y)
                                val segT = ((p - 0.4f) / 0.6f).coerceIn(0f, 1f)
                                lineTo(
                                    x = checkMid.x + (checkEnd.x - checkMid.x) * segT,
                                    y = checkMid.y + (checkEnd.y - checkMid.y) * segT
                                )
                            }
                        }

                        drawPath(
                            path = path,
                            color = Color.White,
                            style = Stroke(
                                width = 3.8.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = DuoOrange,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = badgeTitle,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = DuoGreenDark,
                        modifier = Modifier.testTag("celebration_tier_title")
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = DuoGreen.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "Precisión ${celebrationState.scorePercentage}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = DuoGreenDark,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    if (celebrationState.xpEarned > 0) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = DuoYellow.copy(alpha = 0.28f),
                            modifier = Modifier.testTag("celebration_xp_badge")
                        ) {
                            Text(
                                text = "+${celebrationState.xpEarned} XP ✨",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFB78103),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            if (onReplayClick != null) {
                Spacer(modifier = Modifier.width(2.dp))
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(DuoBlue.copy(alpha = 0.12f))
                        .testTag("replay_celebration_badge_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Repetir animación de celebración",
                        tint = DuoBlue,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawCelebrationParticleShape(
    shape: ParticleShape,
    color: Color,
    radius: Float
) {
    when (shape) {
        ParticleShape.CONFETTI_RIBBON -> {
            drawRoundRect(
                color = color,
                topLeft = Offset(-radius, -radius * 0.48f),
                size = Size(radius * 2f, radius * 0.96f),
                cornerRadius = CornerRadius(radius * 0.25f, radius * 0.25f)
            )
        }
        ParticleShape.GLOW_CIRCLE -> {
            drawCircle(
                color = color,
                radius = radius * 0.75f,
                center = Offset.Zero
            )
        }
        ParticleShape.DIAMOND -> {
            val path = Path().apply {
                moveTo(0f, -radius)
                lineTo(radius * 0.75f, 0f)
                lineTo(0f, radius)
                lineTo(-radius * 0.75f, 0f)
                close()
            }
            drawPath(path = path, color = color)
        }
        ParticleShape.FOUR_POINT_SPARKLE -> {
            val path = Path().apply {
                val inner = radius * 0.28f
                moveTo(0f, -radius)
                lineTo(inner, -inner)
                lineTo(radius, 0f)
                lineTo(inner, inner)
                lineTo(0f, radius)
                lineTo(-inner, inner)
                lineTo(-radius, 0f)
                lineTo(-inner, -inner)
                close()
            }
            drawPath(path = path, color = color)
        }
        ParticleShape.FIVE_POINT_STAR -> {
            val path = Path().apply {
                val points = 5
                val innerRadius = radius * 0.45f
                var rot = -PI.toFloat() / 2f
                val step = PI.toFloat() / points
                moveTo(cos(rot) * radius, sin(rot) * radius)
                for (i in 0 until points) {
                    lineTo(cos(rot) * radius, sin(rot) * radius)
                    rot += step
                    lineTo(cos(rot) * innerRadius, sin(rot) * innerRadius)
                    rot += step
                }
                close()
            }
            drawPath(path = path, color = color)
        }
    }
}

private fun generateCelebrationParticles(seed: Int, count: Int): List<CelebrationParticle> {
    val random = Random(seed)
    val palette = listOf(
        DuoGreen,
        DuoYellow,
        DuoBlue,
        DuoOrange,
        DuoPurple,
        Color(0xFF00E676),
        Color(0xFFFF4081)
    )
    val shapes = ParticleShape.values()

    return List(count) { index ->
        // Three emitter zones: left cannon (0), right cannon (1), center burst (2)
        val emitterZone = index % 3
        val originX = when (emitterZone) {
            0 -> 0.08f + random.nextFloat() * 0.14f
            1 -> 0.78f + random.nextFloat() * 0.14f
            else -> 0.38f + random.nextFloat() * 0.24f
        }
        val originY = when (emitterZone) {
            0, 1 -> 0.55f + random.nextFloat() * 0.12f
            else -> 0.26f + random.nextFloat() * 0.10f
        }

        val vx = when (emitterZone) {
            0 -> 0.18f + random.nextFloat() * 0.58f
            1 -> -(0.18f + random.nextFloat() * 0.58f)
            else -> (random.nextFloat() - 0.5f) * 0.95f
        }
        val vy = -(0.42f + random.nextFloat() * 0.62f)
        val gravity = 0.95f + random.nextFloat() * 0.55f
        val sizePx = 10f + random.nextFloat() * 14f

        CelebrationParticle(
            id = index,
            originXFraction = originX,
            originYFraction = originY,
            velocityX = vx,
            velocityY = vy,
            gravity = gravity,
            baseSize = sizePx,
            rotationSpeed = (random.nextFloat() - 0.5f) * 4.2f,
            initialRotation = random.nextFloat() * 360f,
            tumblePhase = random.nextFloat() * 6.28f,
            color = palette[index % palette.size],
            shape = shapes[index % shapes.size],
            delayFraction = (random.nextFloat() * 0.16f)
        )
    }
}
