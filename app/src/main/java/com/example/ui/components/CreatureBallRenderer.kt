package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.example.game.CreatureType
import com.example.game.SpecialType
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CreatureBallRenderer(
    type: CreatureType,
    special: SpecialType,
    isMatched: Boolean,
    modifier: Modifier = Modifier,
    pulseTime: Float = 0f
) {
    val opacity = if (isMatched) 0.3f else 1f

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val r = w.coerceAtMost(h) / 2 * 0.9f
        val cx = w / 2
        val cy = h / 2

        if (isMatched) {
            // Draw popping ring
            drawCircle(
                color = Color(type.colorPrimary),
                radius = r * 1.1f,
                center = Offset(cx, cy),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f),
                alpha = 0.5f
            )
        }

        // Draw creature based on element
        when (type) {
            CreatureType.FIRE -> drawFireDragon(cx, cy, r, opacity, pulseTime)
            CreatureType.ICE -> drawIceCat(cx, cy, r, opacity, pulseTime)
            CreatureType.THUNDER -> drawThunderWolf(cx, cy, r, opacity, pulseTime)
            CreatureType.SHADOW -> drawShadowImp(cx, cy, r, opacity, pulseTime)
            CreatureType.GOLDEN_SNAKE -> drawGoldenSnake(cx, cy, r, opacity, pulseTime)
            CreatureType.RAINBOW -> drawRainbowPhoenix(cx, cy, r, opacity, pulseTime)
        }

        // Special aura modifiers
        when (special) {
            SpecialType.LINE_ROW -> {
                // Horizontal laser band
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(cx - r, cy - 4f),
                    size = Size(r * 2, 8f),
                    cornerRadius = CornerRadius(4f, 4f),
                    alpha = 0.7f
                )
                drawCircle(
                    color = Color.White,
                    radius = r * 0.3f,
                    center = Offset(cx, cy),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                )
            }
            SpecialType.LINE_COLUMN -> {
                // Vertical laser band
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(cx - 4f, cy - r),
                    size = Size(8f, r * 2),
                    cornerRadius = CornerRadius(4f, 4f),
                    alpha = 0.7f
                )
                drawCircle(
                    color = Color.White,
                    radius = r * 0.3f,
                    center = Offset(cx, cy),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                )
            }
            SpecialType.EXPLOSIVE -> {
                // Fire ring aura
                drawCircle(
                    color = Color(0xFFFF5722),
                    radius = r * 1.15f,
                    center = Offset(cx, cy),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 5f), 0f)),
                    alpha = 0.8f
                )
            }
            SpecialType.MYTHIC_RAINBOW -> {
                // Glittering rainbow aura star
                drawCircle(
                    color = Color(0xFFFFD700),
                    radius = r * 1.2f,
                    center = Offset(cx, cy),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f),
                    alpha = 0.9f
                )
            }
            else -> {}
        }
    }
}

// 1. PYRO: RED FANTASY BABY DRAGON
private fun DrawScope.drawFireDragon(cx: Float, cy: Float, r: Float, alpha: Float, pulse: Float) {
    // Gradient shader body
    val bodyGrad = Brush.radialGradient(
        colors = listOf(Color(0xFFFFA235), Color(0xFFFF2A2A), Color(0xFF4A0002)),
        center = Offset(cx - r * 0.2f, cy - r * 0.2f),
        radius = r * 1.3f
    )
    drawCircle(brush = bodyGrad, radius = r, center = Offset(cx, cy), alpha = alpha)

    // Draw little gold horns (matching user image assets!)
    val leftHorn = Path().apply {
        moveTo(cx - r * 0.6f, cy - r * 0.6f)
        quadraticTo(cx - r * 0.9f, cy - r * 1.2f, cx - r * 0.5f, cy - r * 1.2f)
        quadraticTo(cx - r * 0.3f, cy - r * 0.8f, cx - r * 0.3f, cy - r * 0.6f)
        close()
    }
    val rightHorn = Path().apply {
        moveTo(cx + r * 0.6f, cy - r * 0.6f)
        quadraticTo(cx + r * 0.9f, cy - r * 1.2f, cx + r * 0.5f, cy - r * 1.2f)
        quadraticTo(cx + r * 0.3f, cy - r * 0.8f, cx + r * 0.3f, cy - r * 0.6f)
        close()
    }
    drawPath(leftHorn, brush = Brush.linearGradient(listOf(Color(0xFFFFF1AC), Color(0xFFFFC107))), alpha = alpha)
    drawPath(rightHorn, brush = Brush.linearGradient(listOf(Color(0xFFFFF1AC), Color(0xFFFFC107))), alpha = alpha)

    // Draw dragon forehead custom scales
    drawCircle(color = Color(0xFFFFF1AC), radius = r * 0.1f, center = Offset(cx, cy - r * 0.4f), alpha = alpha * 0.5f)
    drawCircle(color = Color(0xFFFFF1AC), radius = r * 0.07f, center = Offset(cx - r * 0.2f, cy - r * 0.35f), alpha = alpha * 0.5f)
    drawCircle(color = Color(0xFFFFF1AC), radius = r * 0.07f, center = Offset(cx + r * 0.2f, cy - r * 0.35f), alpha = alpha * 0.5f)

    // Huge Pixar shiny brown eyes
    drawPixarEyes(cx, cy, r, Color(0xFF5D3100), alpha)

    // Cute baby dragon smile & snout
    drawCircle(color = Color(0xFFFF7A7A), radius = r * 0.15f, center = Offset(cx, cy + r * 0.35f), alpha = alpha * 0.5f)
    drawCircle(color = Color(0xFF1B0002), radius = r * 0.03f, center = Offset(cx - r * 0.05f, cy + r * 0.32f), alpha = alpha)
    drawCircle(color = Color(0xFF1B0002), radius = r * 0.03f, center = Offset(cx + r * 0.05f, cy + r * 0.32f), alpha = alpha)

    val smile = Path().apply {
        moveTo(cx - r * 0.15f, cy + r * 0.45f)
        quadraticTo(cx, cy + r * 0.55f, cx + r * 0.15f, cy + r * 0.45f)
    }
    drawPath(smile, color = Color(0xFF330001), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f), alpha = alpha)
}

// 2. FROSTY: CRYSTAL ICE CAT
private fun DrawScope.drawIceCat(cx: Float, cy: Float, r: Float, alpha: Float, pulse: Float) {
    val bodyGrad = Brush.radialGradient(
        colors = listOf(Color(0xFFE0F7FA), Color(0xFF00E5FF), Color(0xFF0D47A1)),
        center = Offset(cx - r * 0.2f, cy - r * 0.2f),
        radius = r * 1.3f
    )
    drawCircle(brush = bodyGrad, radius = r, center = Offset(cx, cy), alpha = alpha)

    // Sharp cat ears
    val leftEar = Path().apply {
        moveTo(cx - r * 0.8f, cy - r * 0.4f)
        lineTo(cx - r * 0.9f, cy - r * 1.1f)
        lineTo(cx - r * 0.3f, cy - r * 0.7f)
    }
    val rightEar = Path().apply {
        moveTo(cx + r * 0.8f, cy - r * 0.4f)
        lineTo(cx + r * 0.9f, cy - r * 1.1f)
        lineTo(cx + r * 0.3f, cy - r * 0.7f)
    }
    drawPath(leftEar, color = Color(0xFF0D47A1), alpha = alpha)
    drawPath(rightEar, color = Color(0xFF0D47A1), alpha = alpha)

    // Inner pink ears
    val leftInner = Path().apply {
        moveTo(cx - r * 0.75f, cy - r * 0.45f)
        lineTo(cx - r * 0.82f, cy - r * 0.95f)
        lineTo(cx - r * 0.4f, cy - r * 0.65f)
    }
    drawPath(leftInner, color = Color(0xFFFF80AB), alpha = alpha)

    // Ice forehead shard gem
    val gem = Path().apply {
        moveTo(cx, cy - r * 0.6f)
        lineTo(cx + r * 0.12f, cy - r * 0.45f)
        lineTo(cx, cy - r * 0.3f)
        lineTo(cx - r * 0.12f, cy - r * 0.45f)
        close()
    }
    drawPath(gem, color = Color.White, alpha = alpha)

    // Huge shiny blue eyes
    drawPixarEyes(cx, cy, r, Color(0xFF003C60), alpha)

    // Cat whiskers and snout
    drawCircle(color = Color.White, radius = 4f, center = Offset(cx - r * 0.1f, cy + r * 0.35f), alpha = alpha)
    drawCircle(color = Color.White, radius = 4f, center = Offset(cx + r * 0.1f, cy + r * 0.35f), alpha = alpha)
    val nose = Path().apply {
        moveTo(cx - 5f, cy + r * 0.28f)
        lineTo(cx + 5f, cy + r * 0.28f)
        lineTo(cx, cy + r * 0.35f)
        close()
    }
    drawPath(nose, color = Color(0xFFFF4081), alpha = alpha)
}

// 3. SPARKY: VOLT THUNDER WOLF
private fun DrawScope.drawThunderWolf(cx: Float, cy: Float, r: Float, alpha: Float, pulse: Float) {
    val bodyGrad = Brush.radialGradient(
        colors = listOf(Color(0xFFFFFDE7), Color(0xFFFFEB3B), Color(0xFFE65100)),
        center = Offset(cx - r * 0.2f, cy - r * 0.2f),
        radius = r * 1.3f
    )
    drawCircle(brush = bodyGrad, radius = r, center = Offset(cx, cy), alpha = alpha)

    // Wolf pointy ears
    val leftEar = Path().apply {
        moveTo(cx - r * 0.8f, cy - r * 0.2f)
        lineTo(cx - r * 0.7f, cy - r * 1.1f)
        lineTo(cx - r * 0.3f, cy - r * 0.7f)
    }
    val rightEar = Path().apply {
        moveTo(cx + r * 0.8f, cy - r * 0.2f)
        lineTo(cx + r * 0.7f, cy - r * 1.1f)
        lineTo(cx + r * 0.3f, cy - r * 0.7f)
    }
    drawPath(leftEar, color = Color(0xFFE65100), alpha = alpha)
    drawPath(rightEar, color = Color(0xFFE65100), alpha = alpha)

    // Zig zag electric cheeks
    val leftSpark = Path().apply {
        moveTo(cx - r, cy)
        lineTo(cx - r * 1.2f, cy + r * 0.2f)
        lineTo(cx - r * 0.9f, cy + r * 0.3f)
    }
    val rightSpark = Path().apply {
        moveTo(cx + r, cy)
        lineTo(cx + r * 1.2f, cy + r * 0.2f)
        lineTo(cx + r * 0.9f, cy + r * 0.3f)
    }
    drawPath(leftSpark, color = Color(0xFFFFEB3B), alpha = alpha)
    drawPath(rightSpark, color = Color(0xFFFFEB3B), alpha = alpha)

    // Eyes: Electric golden/green
    drawPixarEyes(cx, cy, r, Color(0xFF00796B), alpha)

    // Black nose & cheeky smile
    drawCircle(color = Color(0xFF1E1E1E), radius = 6f, center = Offset(cx, cy + r * 0.3f), alpha = alpha)
    val tongue = Path().apply {
        moveTo(cx - r * 0.1f, cy + r * 0.42f)
        quadraticTo(cx, cy + r * 0.6f, cx + r * 0.1f, cy + r * 0.42f)
    }
    drawPath(tongue, color = Color(0xFFFF5252), alpha = alpha)
}

// 4. UMBRIA: SHADOW IMP
private fun DrawScope.drawShadowImp(cx: Float, cy: Float, r: Float, alpha: Float, pulse: Float) {
    val bodyGrad = Brush.radialGradient(
        colors = listOf(Color(0xFF8E24AA), Color(0xFF4A148C), Color(0xFF1A002C)),
        center = Offset(cx - r * 0.2f, cy - r * 0.2f),
        radius = r * 1.3f
    )
    drawCircle(brush = bodyGrad, radius = r, center = Offset(cx, cy), alpha = alpha)

    // Cute curved shadow horns
    val leftHorn = Path().apply {
        moveTo(cx - r * 0.4f, cy - r * 0.7f)
        quadraticTo(cx - r * 0.8f, cy - r * 1.1f, cx - r * 1.0f, cy - r * 0.9f)
        quadraticTo(cx - r * 0.7f, cy - r * 0.7f, cx - r * 0.3f, cy - r * 0.5f)
    }
    val rightHorn = Path().apply {
        moveTo(cx + r * 0.4f, cy - r * 0.7f)
        quadraticTo(cx + r * 0.8f, cy - r * 1.1f, cx + r * 1.0f, cy - r * 0.9f)
        quadraticTo(cx + r * 0.7f, cy - r * 0.7f, cx + r * 0.3f, cy - r * 0.5f)
    }
    drawPath(leftHorn, color = Color(0xFF120024), alpha = alpha)
    drawPath(rightHorn, color = Color(0xFF120024), alpha = alpha)

    // Mysterious slit eyes (glowing neon purple!)
    val shadowMaskGrad = Brush.radialGradient(
        colors = listOf(Color(0xE01A002C), Color.Transparent),
        center = Offset(cx, cy),
        radius = r * 1.1f
    )
    drawCircle(brush = shadowMaskGrad, radius = r * 0.85f, center = Offset(cx, cy), alpha = alpha)

    // Draw glowing cat slit eyes
    drawSlitGlowingEye(cx - r * 0.35f, cy, r * 0.28f, Color(0xFF00FFCC), alpha)
    drawSlitGlowingEye(cx + r * 0.35f, cy, r * 0.28f, Color(0xFF00FFCC), alpha)

    // Tiny vampire fangs
    val leftFang = Path().apply {
        moveTo(cx - r * 0.12f, cy + r * 0.32f)
        lineTo(cx - r * 0.16f, cy + r * 0.45f)
        lineTo(cx - r * 0.2f, cy + r * 0.32f)
    }
    val rightFang = Path().apply {
        moveTo(cx + r * 0.12f, cy + r * 0.32f)
        lineTo(cx + r * 0.16f, cy + r * 0.45f)
        lineTo(cx + r * 0.2f, cy + r * 0.32f)
    }
    drawPath(leftFang, color = Color.White, alpha = alpha)
    drawPath(rightFang, color = Color.White, alpha = alpha)
}

// 5. SERPY: THE GOLDEN COBRA
private fun DrawScope.drawGoldenSnake(cx: Float, cy: Float, r: Float, alpha: Float, pulse: Float) {
    val bodyGrad = Brush.radialGradient(
        colors = listOf(Color(0xFFFFF9C4), Color(0xFFFFD54F), Color(0xFFE65100)),
        center = Offset(cx - r * 0.2f, cy - r * 0.2f),
        radius = r * 1.3f
    )
    drawCircle(brush = bodyGrad, radius = r, center = Offset(cx, cy), alpha = alpha)

    // Outer royal cobra hood arcs
    val leftHood = Path().apply {
        moveTo(cx - r * 0.6f, cy - r * 0.6f)
        quadraticTo(cx - r * 1.2f, cy, cx - r * 0.7f, cy + r * 0.7f)
        close()
    }
    val rightHood = Path().apply {
        moveTo(cx + r * 0.6f, cy - r * 0.6f)
        quadraticTo(cx + r * 1.2f, cy, cx + r * 0.7f, cy + r * 0.7f)
        close()
    }
    drawPath(leftHood, brush = Brush.linearGradient(listOf(Color(0xFFE65100), Color(0xFFFF8F00))), alpha = alpha)
    drawPath(rightHood, brush = Brush.linearGradient(listOf(Color(0xFFE65100), Color(0xFFFF8F00))), alpha = alpha)

    // Cute snake diamond on forehead
    val diamondMark = Path().apply {
        moveTo(cx, cy - r * 0.65f)
        lineTo(cx + r * 0.08f, cy - r * 0.55f)
        lineTo(cx, cy - r * 0.45f)
        lineTo(cx - r * 0.08f, cy - r * 0.55f)
        close()
    }
    drawPath(diamondMark, color = Color(0xFFD50000), alpha = alpha)

    // Pixar green golden eyes
    drawPixarEyes(cx, cy, r, Color(0xFF00300A), alpha)

    // Cute snake tongue popping out!
    val tongue = Path().apply {
        moveTo(cx, cy + r * 0.35f)
        lineTo(cx, cy + r * 0.55f)
        lineTo(cx - r * 0.05f, cy + r * 0.63f)
        moveTo(cx, cy + r * 0.55f)
        lineTo(cx + r * 0.05f, cy + r * 0.63f)
    }
    drawPath(tongue, color = Color(0xFFFF1744), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f), alpha = alpha)
}

// 6. IRIS: RAINBOW MYTHIC PHOENIX CHICK
private fun DrawScope.drawRainbowPhoenix(cx: Float, cy: Float, r: Float, alpha: Float, pulse: Float) {
    // Stunning Multicolor radial sweep gradient for cosmic effect!
    val bodyGrad = Brush.sweepGradient(
        colors = listOf(Color(0xFFFF1744), Color(0xFFFFEB3B), Color(0xFF00E676), Color(0xFF2979FF), Color(0xFFD500F9), Color(0xFFFF1744)),
        center = Offset(cx, cy)
    )
    drawCircle(brush = bodyGrad, radius = r, center = Offset(cx, cy), alpha = alpha)

    // Outer cosmic feathered plumes
    drawCircle(color = Color(0xE0FFFFFF), radius = r * 1.05f, center = Offset(cx, cy), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f))

    // Plumes on top
    val crestLeft = Path().apply {
        moveTo(cx, cy - r)
        quadraticTo(cx - r * 0.3f, cy - r * 1.3f, cx - r * 0.2f, cy - r * 1.4f)
        quadraticTo(cx, cy - r * 1.2f, cx, cy - r)
    }
    val crestRight = Path().apply {
        moveTo(cx, cy - r)
        quadraticTo(cx + r * 0.3f, cy - r * 1.3f, cx + r * 0.2f, cy - r * 1.4f)
        quadraticTo(cx, cy - r * 1.2f, cx, cy - r)
    }
    drawPath(crestLeft, color = Color(0xFFFFD700), alpha = alpha)
    drawPath(crestRight, color = Color(0xFFFF0055), alpha = alpha)

    // Galactic eyes
    drawPixarEyes(cx, cy, r, Color(0xFF311B92), alpha)

    // Golden cute bird beak
    val beak = Path().apply {
        moveTo(cx - r * 0.12f, cy + r * 0.28f)
        quadraticTo(cx, cy + r * 0.5f, cx + r * 0.12f, cy + r * 0.28f)
        close()
    }
    drawPath(beak, color = Color(0xFFFFAB00), alpha = alpha)
}

// SHARED UTILITY: Pixar huge glossy cartoon eyes
private fun DrawScope.drawPixarEyes(cx: Float, cy: Float, r: Float, baseIris: Color, alpha: Float) {
    val eyeRadius = r * 0.3f
    val pupilRadius = r * 0.16f
    
    // Position of left eye center and right eye center
    val lx = cx - r * 0.35f
    val rx = cx + r * 0.35f
    val ey = cy - r * 0.05f

    // 1. Sclera (White eye background)
    drawCircle(color = Color.White, radius = eyeRadius, center = Offset(lx, ey), alpha = alpha)
    drawCircle(color = Color.White, radius = eyeRadius, center = Offset(rx, ey), alpha = alpha)

    // 2. Iris (Colored section)
    drawCircle(color = baseIris, radius = pupilRadius * 1.25f, center = Offset(lx, ey), alpha = alpha)
    drawCircle(color = baseIris, radius = pupilRadius * 1.25f, center = Offset(rx, ey), alpha = alpha)

    // 3. Pupil (Black center)
    drawCircle(color = Color(0xFF0C0705), radius = pupilRadius, center = Offset(lx + r*0.02f, ey), alpha = alpha)
    drawCircle(color = Color(0xFF0C0705), radius = pupilRadius, center = Offset(rx - r*0.02f, ey), alpha = alpha)

    // 4. Gloss shine (Pixar reflection dual white spots)
    drawCircle(color = Color.White, radius = r * 0.08f, center = Offset(lx - r*0.06f, ey - r*0.08f), alpha = alpha)
    drawCircle(color = Color.White, radius = r * 0.08f, center = Offset(rx - r*0.06f, ey - r*0.08f), alpha = alpha)

    drawCircle(color = Color.White, radius = r * 0.035f, center = Offset(lx + r*0.06f, ey + r*0.06f), alpha = alpha)
    drawCircle(color = Color.White, radius = r * 0.035f, center = Offset(rx + r*0.06f, ey + r*0.06f), alpha = alpha)
}

// INDEPENDENT SLIT SHADOW EYE
private fun DrawScope.drawSlitGlowingEye(cx: Float, cy: Float, radius: Float, color: Color, alpha: Float) {
    // Elliptical glowing eye path
    val eyePath = Path().apply {
        moveTo(cx - radius, cy)
        quadraticTo(cx, cy - radius * 0.5f, cx + radius, cy)
        quadraticTo(cx, cy + radius * 0.5f, cx - radius, cy)
        close()
    }
    drawPath(eyePath, color = color, alpha = alpha)

    // Internal vertical black pupil slit
    drawRoundRect(
        color = Color(0xFF120025),
        topLeft = Offset(cx - 3f, cy - radius * 0.3f),
        size = Size(6f, radius * 0.6f),
        cornerRadius = CornerRadius(3f, 3f),
        alpha = alpha
    )
}
