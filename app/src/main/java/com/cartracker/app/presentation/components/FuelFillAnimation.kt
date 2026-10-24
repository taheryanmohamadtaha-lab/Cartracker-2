package com.cartracker.app.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cartracker.app.R
import kotlinx.coroutines.delay

/**
 * پمپ بنزین کوچک و باکی که پر می‌شود. progress بین ۰ تا ۱ است و فقط داخل Canvas خوانده می‌شود
 * تا انیمیشن باعث ساخت دوباره صفحه نشود.
 */
@Composable
fun FuelFillAnimation(progress: () -> Float, modifier: Modifier = Modifier) {
    val bodyColor = MaterialTheme.colorScheme.primary
    val fuelColor = MaterialTheme.colorScheme.tertiary
    val lineColor = MaterialTheme.colorScheme.onSurface

    Canvas(modifier = modifier) {
        val p = progress().coerceIn(0f, 1f)
        val w = size.width
        val h = size.height
        val stroke = 4.dp.toPx()

        // بدنه پمپ
        val pumpX = w * 0.04f
        val pumpY = h * 0.08f
        val pumpW = w * 0.28f
        val pumpH = h * 0.86f
        drawRoundRect(
            color = bodyColor,
            topLeft = Offset(pumpX, pumpY),
            size = Size(pumpW, pumpH),
            cornerRadius = CornerRadius(14.dp.toPx())
        )
        drawRoundRect(
            color = Color.White.copy(alpha = 0.85f),
            topLeft = Offset(pumpX + pumpW * 0.15f, pumpY + pumpH * 0.12f),
            size = Size(pumpW * 0.7f, pumpH * 0.22f),
            cornerRadius = CornerRadius(6.dp.toPx())
        )

        // باک
        val tankX = w * 0.58f
        val tankY = h * 0.38f
        val tankW = w * 0.36f
        val tankH = h * 0.52f
        val tankCorner = CornerRadius(16.dp.toPx())

        // شیلنگ و نازل
        val nozzleX = tankX + tankW * 0.5f
        val nozzleY = tankY - h * 0.12f
        val hose = Path().apply {
            moveTo(pumpX + pumpW, pumpY + pumpH * 0.45f)
            cubicTo(
                w * 0.46f, pumpY + pumpH * 0.45f,
                w * 0.44f, nozzleY,
                nozzleX, nozzleY
            )
        }
        drawPath(path = hose, color = lineColor, style = Stroke(width = stroke, cap = StrokeCap.Round))

        // سطح سوخت
        drawRoundRect(
            color = lineColor.copy(alpha = 0.12f),
            topLeft = Offset(tankX, tankY),
            size = Size(tankW, tankH),
            cornerRadius = tankCorner
        )
        val level = tankH * p
        clipRect(
            left = tankX,
            top = tankY + tankH - level,
            right = tankX + tankW,
            bottom = tankY + tankH
        ) {
            drawRoundRect(
                color = fuelColor,
                topLeft = Offset(tankX, tankY),
                size = Size(tankW, tankH),
                cornerRadius = tankCorner
            )
        }
        drawRoundRect(
            color = lineColor,
            topLeft = Offset(tankX, tankY),
            size = Size(tankW, tankH),
            cornerRadius = tankCorner,
            style = Stroke(width = stroke)
        )

        // قطره در حال ریختن
        if (p < 1f) {
            val t = (p * 6f) % 1f
            val startY = nozzleY + 4.dp.toPx()
            val endY = tankY + tankH - level
            drawCircle(
                color = fuelColor,
                radius = 4.dp.toPx(),
                center = Offset(nozzleX, startY + (endY - startY) * t)
            )
        } else {
            // علامت تیک وقتی پر شد
            val check = Path().apply {
                moveTo(tankX + tankW * 0.28f, tankY + tankH * 0.52f)
                lineTo(tankX + tankW * 0.45f, tankY + tankH * 0.68f)
                lineTo(tankX + tankW * 0.74f, tankY + tankH * 0.36f)
            }
            drawPath(
                path = check,
                color = Color.White,
                style = Stroke(width = stroke * 1.4f, cap = StrokeCap.Round)
            )
        }
    }
}

/** پوشش کوتاه «ثبت شد» با انیمیشن پر شدن باک؛ بعد از پایان onFinished صدا زده می‌شود. */
@Composable
fun FuelSuccessOverlay(onFinished: () -> Unit) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(durationMillis = 1100, easing = FastOutSlowInEasing))
        delay(450)
        onFinished()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            ),
        contentAlignment = Alignment.Center
    ) {
        AnimeCard(modifier = Modifier.padding(32.dp)) {
            Box(
                modifier = Modifier
                    .width(220.dp)
                    .height(150.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                FuelFillAnimation(progress = { progress.value }, modifier = Modifier.fillMaxSize())
            }
            Text(
                text = stringResource(R.string.fuel_saved),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
