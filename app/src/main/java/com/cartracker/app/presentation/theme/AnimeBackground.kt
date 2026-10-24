package com.cartracker.app.presentation.theme

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import com.cartracker.app.domain.model.AppThemeId
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/**
 * پس‌زمینه کاملاً کدنویسی‌شده (Canvas) برای هر تم.
 * همه شکل‌ها اورجینال و ساده‌اند. زمان در بازه 0..1 تکرار می‌شود (هر ۳۰ ثانیه)
 * و همه حرکت‌ها مضرب صحیحی از آن هستند تا حلقه بدون پرش باشد.
 * مقدار زمان فقط داخل Canvas خوانده می‌شود، پس فقط مرحله رسم دوباره اجرا می‌شود و Recomposition ندارد.
 */
@Composable
fun AnimeBackground(
    themeId: AppThemeId,
    variant: ThemeVariant,
    animated: Boolean,
    modifier: Modifier = Modifier
) {
    val scene = remember(themeId) { createScene(themeId) }
    val time: State<Float> = if (animated) {
        val transition = rememberInfiniteTransition(label = "background")
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 30_000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "backgroundTime"
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val t = time.value
        drawRect(brush = Brush.verticalGradient(variant.gradient))
        when (themeId) {
            AppThemeId.NEON_CITY -> drawNeonCity(scene, variant, t)
            AppThemeId.SAKURA -> drawSakura(scene, variant, t)
            AppThemeId.COAST_SUNSET -> drawCoast(scene, variant, t)
            AppThemeId.STARRY_SKY -> drawStarry(scene, variant, t)
            AppThemeId.DREAM_FOREST -> drawForest(scene, variant, t)
        }
    }
}

// ---------------------------------------------------------------------------
// داده‌های صحنه (یک‌بار ساخته می‌شود و ثابت می‌ماند)
// ---------------------------------------------------------------------------

private class Item(val x: Float, val w: Float, val h: Float, val layer: Int)

private class Particle(
    val x: Float,
    val y: Float,
    val size: Float,
    val speed: Int,
    val phase: Float
)

private class Scene(
    val items: List<Item>,
    val particles: List<Particle>,
    val stars: List<Particle>
)

private val TWO_PI = (2.0 * PI).toFloat()

private fun Random.range(from: Float, to: Float): Float = from + nextFloat() * (to - from)

private fun frac(v: Float): Float = v - floor(v)

private fun createScene(id: AppThemeId): Scene {
    val rnd = Random(id.ordinal * 97 + 11)
    val items = mutableListOf<Item>()

    when (id) {
        AppThemeId.NEON_CITY -> {
            for (layer in 0..2) {
                var x = -0.02f
                while (x < 1f) {
                    val bw = rnd.range(0.06f, 0.12f)
                    val bh = when (layer) {
                        0 -> rnd.range(0.20f, 0.40f)
                        1 -> rnd.range(0.14f, 0.30f)
                        else -> rnd.range(0.07f, 0.17f)
                    }
                    items.add(Item(x, bw, bh, layer))
                    x += bw * 0.92f
                }
            }
        }
        AppThemeId.DREAM_FOREST -> {
            for (layer in 0..2) {
                var x = -0.05f
                while (x < 1.05f) {
                    val tw = rnd.range(0.10f, 0.17f)
                    val th = rnd.range(0.14f, 0.26f)
                    items.add(Item(x, tw, th, layer))
                    x += tw * rnd.range(0.55f, 0.8f)
                }
            }
        }
        else -> Unit
    }

    fun makeParticles(
        count: Int,
        speedMin: Int,
        speedMax: Int,
        yMax: Float,
        sizeMin: Float,
        sizeMax: Float
    ): List<Particle> = List(count) {
        Particle(
            x = rnd.nextFloat(),
            y = rnd.nextFloat() * yMax,
            size = rnd.range(sizeMin, sizeMax),
            speed = rnd.nextInt(speedMin, speedMax + 1),
            phase = rnd.nextFloat()
        )
    }

    val stars = when (id) {
        AppThemeId.NEON_CITY -> makeParticles(30, 6, 14, 0.55f, 0.5f, 1.1f)
        AppThemeId.STARRY_SKY -> makeParticles(70, 6, 14, 0.7f, 0.5f, 1.4f)
        else -> emptyList()
    }
    val particles = when (id) {
        AppThemeId.SAKURA -> makeParticles(18, 2, 3, 1f, 0.7f, 1.4f)
        AppThemeId.COAST_SUNSET -> makeParticles(10, 1, 3, 1f, 0.7f, 1.4f)
        AppThemeId.DREAM_FOREST -> makeParticles(16, 2, 4, 1f, 0.6f, 1.2f)
        else -> emptyList()
    }
    return Scene(items, particles, stars)
}

// ---------------------------------------------------------------------------
// ابزارهای رسم مشترک
// ---------------------------------------------------------------------------

private fun DrawScope.glow(color: Color, center: Offset, radius: Float) {
    val glowRadius = radius * 2.6f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = 0.55f), Color.Transparent),
            center = center,
            radius = glowRadius
        ),
        radius = glowRadius,
        center = center
    )
    drawCircle(color = color, radius = radius, center = center)
}

private fun DrawScope.hill(color: Color, baseY: Float, amp: Float) {
    val w = size.width
    val h = size.height
    val path = Path().apply {
        moveTo(0f, baseY)
        cubicTo(w * 0.3f, baseY - amp, w * 0.65f, baseY + amp, w, baseY - amp * 0.3f)
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    drawPath(path, color)
}

private fun DrawScope.drawStars(scene: Scene, color: Color, t: Float) {
    val w = size.width
    val h = size.height
    val u = w / 360f
    scene.stars.forEach { s ->
        val twinkle = 0.5f + 0.5f * sin(TWO_PI * (t * s.speed + s.phase))
        drawCircle(
            color = color.copy(alpha = 0.3f + 0.7f * twinkle),
            radius = s.size * 1.6f * u,
            center = Offset(s.x * w, s.y * h)
        )
    }
}

private fun cubic(a: Float, b: Float, c: Float, d: Float, s: Float): Float {
    val q = 1f - s
    return q * q * q * a + 3f * q * q * s * b + 3f * q * s * s * c + s * s * s * d
}

// ---------------------------------------------------------------------------
// ۱. شهر نئونی شب
// ---------------------------------------------------------------------------

private fun DrawScope.drawNeonCity(scene: Scene, v: ThemeVariant, t: Float) {
    val w = size.width
    val h = size.height
    val u = w / 360f
    glow(v.skyAccent, Offset(w * 0.78f, h * 0.13f), w * 0.07f)
    drawStars(scene, v.particle, t)

    val bases = floatArrayOf(0.70f, 0.82f, 0.96f)
    val colors = arrayOf(v.far, v.mid, v.near)
    val step = (t * 40f).toInt()

    scene.items.forEachIndexed { index, b ->
        val base = h * bases[b.layer]
        val bw = w * b.w
        val bh = h * b.h
        val left = w * b.x
        drawRect(colors[b.layer], Offset(left, base - bh), Size(bw, bh + (h - base)))

        if (b.layer > 0) {
            val cols = min(3, max(1, (bw / (14f * u)).toInt()))
            val rows = min(8, ((bh - 10f * u) / (14f * u)).toInt())
            val lightAlpha = if (b.layer == 1) 0.5f else 0.9f
            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    val key = index * 131 + r * 17 + c * 7
                    val lit = key % 5 != 0 && (key + step) % 23 != 0
                    if (lit) {
                        drawRect(
                            color = v.particle.copy(alpha = lightAlpha),
                            topLeft = Offset(
                                left + (c + 0.5f) * bw / cols - 2f * u,
                                base - bh + 8f * u + r * 14f * u
                            ),
                            size = Size(4f * u, 6f * u)
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// ۲. گل‌های شکوفه بهاری
// ---------------------------------------------------------------------------

private fun DrawScope.drawSakura(scene: Scene, v: ThemeVariant, t: Float) {
    val w = size.width
    val h = size.height
    val u = w / 360f

    glow(v.skyAccent, Offset(w * 0.2f, h * 0.17f), w * 0.07f)
    hill(v.far, h * 0.76f, h * 0.05f)
    hill(v.mid, h * 0.85f, -h * 0.04f)
    hill(v.near, h * 0.93f, h * 0.03f)

    // شاخه و شکوفه‌ها
    val branch = Path().apply {
        moveTo(-w * 0.05f, h * 0.10f)
        cubicTo(w * 0.18f, h * 0.04f, w * 0.34f, h * 0.20f, w * 0.62f, h * 0.11f)
    }
    drawPath(
        path = branch,
        color = Color(0xFF8A5A44).copy(alpha = if (v.isDark) 0.7f else 0.9f),
        style = Stroke(width = 7f * u, cap = StrokeCap.Round)
    )
    for (i in 0..13) {
        val s = i / 13f
        val bx = cubic(-0.05f, 0.18f, 0.34f, 0.62f, s) * w
        val by = cubic(0.10f, 0.04f, 0.20f, 0.11f, s) * h + sin(i * 12.9f) * 6f * u
        val r = (7f + 4f * ((i * 7) % 3)) * u
        drawCircle(v.particle.copy(alpha = 0.95f), r, Offset(bx, by))
        drawCircle(v.skyAccent.copy(alpha = 0.9f), r * 0.4f, Offset(bx, by))
    }

    // گلبرگ‌های در حال ریزش
    scene.particles.forEach { p ->
        val y = frac(p.y + t * p.speed) * h
        val x = (p.x + 0.04f * sin(TWO_PI * (t * p.speed * 3 + p.phase))) * w
        val angle = 360f * frac(t * p.speed * 2 + p.phase)
        rotate(degrees = angle, pivot = Offset(x, y)) {
            drawOval(
                color = v.particle.copy(alpha = 0.9f),
                topLeft = Offset(x - 5f * p.size * u, y - 3f * p.size * u),
                size = Size(10f * p.size * u, 6f * p.size * u)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// ۳. غروب جاده ساحلی
// ---------------------------------------------------------------------------

private fun DrawScope.drawCoast(scene: Scene, v: ThemeVariant, t: Float) {
    val w = size.width
    val h = size.height
    val u = w / 360f
    val horizon = h * 0.62f

    glow(v.skyAccent, Offset(w * 0.5f, horizon - w * 0.02f), w * 0.13f)

    // دریا
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(v.far, v.mid),
            startY = horizon,
            endY = h
        ),
        topLeft = Offset(0f, horizon),
        size = Size(w, h - horizon)
    )

    // بازتاب خورشید روی آب
    for (i in 0 until 6) {
        val y = horizon + (i * 0.028f + 0.015f) * h
        val half = w * (0.16f - i * 0.02f) * (1f + 0.15f * sin(TWO_PI * (t * 4 + i * 0.17f)))
        drawLine(
            color = v.skyAccent.copy(alpha = 0.55f - i * 0.07f),
            start = Offset(w * 0.5f - half, y),
            end = Offset(w * 0.5f + half, y),
            strokeWidth = 3f * u,
            cap = StrokeCap.Round
        )
    }

    // موج‌های کوچک
    scene.particles.forEach { p ->
        val y = horizon + 0.05f * h + p.y * (h - horizon) * 0.8f
        val x = frac(p.x + t * p.speed) * w
        drawLine(
            color = v.skyAccent.copy(alpha = 0.3f),
            start = Offset(x, y),
            end = Offset(x + 22f * u * p.size, y),
            strokeWidth = 2f * u,
            cap = StrokeCap.Round
        )
    }

    // صخره و جاده
    hill(v.near, h * 0.90f, -h * 0.04f)
    drawRect(Color.Black.copy(alpha = 0.25f), Offset(0f, h * 0.95f), Size(w, h * 0.05f))
    for (i in 0 until 7) {
        val x = frac(i / 7f - t * 6f) * w
        drawRect(
            color = v.skyAccent.copy(alpha = 0.8f),
            topLeft = Offset(x, h * 0.975f),
            size = Size(26f * u, 3f * u)
        )
    }
}

// ---------------------------------------------------------------------------
// ۴. آسمان ستاره‌ای
// ---------------------------------------------------------------------------

private fun DrawScope.drawStarry(scene: Scene, v: ThemeVariant, t: Float) {
    val w = size.width
    val h = size.height
    val u = w / 360f

    // هلال ماه
    val c = Offset(w * 0.24f, h * 0.15f)
    val r = w * 0.09f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(v.skyAccent.copy(alpha = 0.35f), Color.Transparent),
            center = c,
            radius = r * 3f
        ),
        radius = r * 3f,
        center = c
    )
    val cut = Path().apply {
        addOval(Rect(center = Offset(c.x + r * 0.5f, c.y - r * 0.2f), radius = r * 0.9f))
    }
    clipPath(cut, clipOp = ClipOp.Difference) {
        drawCircle(color = v.skyAccent, radius = r, center = c)
    }

    drawStars(scene, v.particle, t)

    // شهاب‌سنگ (دو بار در هر چرخه)
    val cycle = frac(t * 2f)
    if (cycle < 0.12f) {
        val p = cycle / 0.12f
        val sx = w * (0.95f - 0.5f * p)
        val sy = h * (0.05f + 0.25f * p)
        drawLine(
            color = v.particle.copy(alpha = 1f - p),
            start = Offset(sx, sy),
            end = Offset(sx + w * 0.1f, sy - h * 0.05f),
            strokeWidth = 3f * u,
            cap = StrokeCap.Round
        )
    }

    hill(v.far, h * 0.80f, h * 0.06f)
    hill(v.mid, h * 0.88f, -h * 0.05f)
    hill(v.near, h * 0.95f, h * 0.03f)
}

// ---------------------------------------------------------------------------
// ۵. جنگل رؤیایی
// ---------------------------------------------------------------------------

private fun DrawScope.drawPine(color: Color, cx: Float, baseY: Float, tw: Float, th: Float) {
    val path = Path().apply {
        moveTo(cx, baseY - th)
        lineTo(cx + tw * 0.36f, baseY - th * 0.40f)
        lineTo(cx - tw * 0.36f, baseY - th * 0.40f)
        close()
        moveTo(cx, baseY - th * 0.72f)
        lineTo(cx + tw * 0.5f, baseY)
        lineTo(cx - tw * 0.5f, baseY)
        close()
    }
    drawPath(path, color)
}

private fun DrawScope.drawForest(scene: Scene, v: ThemeVariant, t: Float) {
    val w = size.width
    val h = size.height
    val u = w / 360f

    glow(v.skyAccent, Offset(w * 0.75f, h * 0.08f), w * 0.05f)

    // پرتوهای نور
    for (i in 0 until 3) {
        val x0 = w * (0.15f + i * 0.3f)
        val ray = Path().apply {
            moveTo(x0, 0f)
            lineTo(x0 + w * 0.12f, 0f)
            lineTo(x0 + w * 0.32f, h * 0.8f)
            lineTo(x0 + w * 0.10f, h * 0.8f)
            close()
        }
        val alpha = 0.10f + 0.04f * sin(TWO_PI * (t * 2 + i * 0.3f))
        drawPath(ray, v.skyAccent.copy(alpha = alpha))
    }

    val bases = floatArrayOf(0.74f, 0.88f, 1.02f)
    val colors = arrayOf(v.far, v.mid, v.near)
    scene.items.forEach { tree ->
        drawPine(
            color = colors[tree.layer],
            cx = w * tree.x + w * tree.w / 2f,
            baseY = h * bases[tree.layer],
            tw = w * tree.w,
            th = h * tree.h
        )
    }
    drawRect(v.near, Offset(0f, h * 0.95f), Size(w, h * 0.05f))

    // کرم‌های شب‌تاب
    scene.particles.forEach { p ->
        val x = (p.x + 0.03f * sin(TWO_PI * (t * p.speed + p.phase))) * w
        val y = (0.35f + p.y * 0.6f + 0.02f * cos(TWO_PI * (t * p.speed + p.phase))) * h
        val pulse = 0.5f + 0.5f * sin(TWO_PI * (t * p.speed * 3 + p.phase))
        val a = 0.4f + 0.6f * pulse
        val glowR = 10f * u * p.size
        val center = Offset(x, y)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(v.particle.copy(alpha = a), Color.Transparent),
                center = center,
                radius = glowR
            ),
            radius = glowR,
            center = center
        )
        drawCircle(v.particle.copy(alpha = a), 2f * u * p.size, center)
    }
}
