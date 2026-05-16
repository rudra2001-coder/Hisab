package com.rudra.hisab.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rudra.hisab.data.preferences.AppSettings
import kotlinx.coroutines.delay
import kotlin.math.*

// ─────────────────────────────────────────────────────────────────────────────
//  SplashScreen
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun SplashScreen(
    settings: AppSettings,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToLock: () -> Unit,
    onNavigateToDashboard: () -> Unit
) {
    // ── Navigation timing ─────────────────────────────────────────────────────
    LaunchedEffect(settings) {
        val navDelay = when {
            !settings.hasCompletedOnboarding -> 2800L
            settings.isPinEnabled            -> 2200L
            else                             -> 2500L
        }
        delay(navDelay)
        when {
            !settings.hasCompletedOnboarding -> onNavigateToOnboarding()
            settings.isPinEnabled            -> onNavigateToLock()
            else                             -> onNavigateToDashboard()
        }
    }

    // ── Colour palette ────────────────────────────────────────────────────────
    val deepNavy   = Color(0xFF0A0E1A)
    val royalBlue  = Color(0xFF1A3A6B)
    val goldAccent = Color(0xFFD4A843)
    val goldLight  = Color(0xFFF5CC6A)
    val goldFade   = Color(0x40D4A843)

    // ── Master entrance animation (0→1 over 900 ms) ───────────────────────────
    val entrance = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        entrance.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 900, easing = EaseOutCubic)
        )
    }

    // ── Infinite slow rotation for outer ring ─────────────────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "splash_infinite")

    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing)
        ),
        label = "ring_rotation"
    )

    // ── Pulsing glow scale ────────────────────────────────────────────────────
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.92f, targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    // ── Shimmer sweep across the title text ───────────────────────────────────
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -1f, targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    // ── Particle alpha (subtle twinkle) ───────────────────────────────────────
    val particleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "particle_alpha"
    )

    // ── Tagline delayed fade-in ───────────────────────────────────────────────
    val taglineAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(700)
        taglineAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600, easing = EaseOutCubic)
        )
    }

    // ── Divider line width ────────────────────────────────────────────────────
    val dividerWidth = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(500)
        dividerWidth.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 700, easing = EaseOutCubic)
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  UI
    // ─────────────────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(deepNavy, Color(0xFF060912))
                )
            ),
        contentAlignment = Alignment.Center
    ) {

        // ── Background particle canvas ────────────────────────────────────────
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawParticles(
                drawScope     = this,
                goldAccent    = goldAccent,
                particleAlpha = particleAlpha * entrance.value
            )
        }

        // ── Central glow aura ─────────────────────────────────────────────────
        Canvas(
            modifier = Modifier
                .size(320.dp)
                .scale(glowPulse)
                .alpha(entrance.value)
        ) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(goldFade, Color.Transparent),
                    center = center,
                    radius = size.minDimension / 2f
                )
            )
        }

        // ── Rotating decorative ring ──────────────────────────────────────────
        Canvas(
            modifier = Modifier
                .size(220.dp)
                .alpha(entrance.value * 0.7f)
        ) {
            rotate(ringRotation) {
                val r = size.minDimension / 2f - 4.dp.toPx()
                // dashed arc segments
                repeat(12) { i ->
                    val startAngle = i * 30f
                    drawArc(
                        color       = goldAccent,
                        startAngle  = startAngle,
                        sweepAngle  = 18f,
                        useCenter   = false,
                        style       = Stroke(
                            width       = 1.5.dp.toPx(),
                            pathEffect  = PathEffect.cornerPathEffect(4.dp.toPx())
                        ),
                        topLeft = Offset(center.x - r, center.y - r),
                        size    = androidx.compose.ui.geometry.Size(r * 2, r * 2)
                    )
                }
            }
        }

        // ── Inner solid ring ──────────────────────────────────────────────────
        Canvas(
            modifier = Modifier
                .size(160.dp)
                .alpha(entrance.value)
        ) {
            drawCircle(
                color = goldAccent,
                style = Stroke(width = 1.dp.toPx()),
                alpha = 0.4f
            )
        }

        // ── Text content ──────────────────────────────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .scale(0.7f + 0.3f * entrance.value)
                .alpha(entrance.value)
        ) {

            // Bengali title with shimmer overlay
            Box(contentAlignment = Alignment.Center) {
                // Base text
                Text(
                    text       = "হিসাব",
                    fontSize   = 68.sp,
                    fontWeight = FontWeight.Bold,
                    color      = goldAccent,
                    letterSpacing = 4.sp
                )
                // Shimmer overlay text (white highlight sweeping across)
                Text(
                    text       = "হিসাব",
                    fontSize   = 68.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp,
                    style      = MaterialTheme.typography.headlineLarge.copy(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                goldLight.copy(alpha = 0.85f),
                                Color.Transparent
                            ),
                            startX = shimmerOffset * 600f,
                            endX   = shimmerOffset * 600f + 200f
                        )
                    )
                )
            }

            // Animated divider
            Canvas(
                modifier = Modifier
                    .width(180.dp)
                    .height(2.dp)
            ) {
                val lineW = size.width * dividerWidth.value
                drawLine(
                    brush       = Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, goldAccent, Color.Transparent)
                    ),
                    start       = Offset((size.width - lineW) / 2f, 0f),
                    end         = Offset((size.width + lineW) / 2f, 0f),
                    strokeWidth = size.height,
                    cap         = StrokeCap.Round
                )
            }

            // Tagline
            Text(
                text       = "আপনার আর্থিক হিসাব রাখুন",
                fontSize   = 14.sp,
                fontWeight = FontWeight.Light,
                color      = goldAccent.copy(alpha = 0.75f),
                letterSpacing = 1.sp,
                modifier   = Modifier.alpha(taglineAlpha.value)
            )
        }

        // ── Version label at bottom ───────────────────────────────────────────
        Text(
            text     = "v1.0",
            fontSize = 11.sp,
            color    = Color.White.copy(alpha = 0.2f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .alpha(taglineAlpha.value)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Helper – draw scattered background gold dots
// ─────────────────────────────────────────────────────────────────────────────
private fun drawParticles(
    drawScope:     DrawScope,
    goldAccent:    Color,
    particleAlpha: Float
) {
    // Deterministic pseudo-random particle positions
    val positions = listOf(
        Offset(0.1f, 0.15f), Offset(0.85f, 0.08f), Offset(0.6f,  0.22f),
        Offset(0.25f, 0.72f), Offset(0.9f, 0.6f),  Offset(0.05f, 0.5f),
        Offset(0.75f, 0.85f), Offset(0.4f, 0.92f), Offset(0.15f, 0.35f),
        Offset(0.92f, 0.4f), Offset(0.5f, 0.05f),  Offset(0.35f, 0.55f)
    )
    val sizes   = listOf(2f, 1.5f, 3f, 2f, 1.5f, 2.5f, 1.5f, 2f, 1f, 2.5f, 2f, 1.5f)
    val alphas  = listOf(0.6f, 0.4f, 0.8f, 0.5f, 0.3f, 0.7f, 0.4f, 0.6f, 0.3f, 0.5f, 0.7f, 0.4f)

    with(drawScope) {
        positions.forEachIndexed { i, pos ->
            drawCircle(
                color  = goldAccent,
                radius = sizes[i] * density,
                center = Offset(pos.x * size.width, pos.y * size.height),
                alpha  = alphas[i] * particleAlpha
            )
        }
    }
}