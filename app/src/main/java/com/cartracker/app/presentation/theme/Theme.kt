package com.cartracker.app.presentation.theme

import android.app.Activity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.cartracker.app.domain.model.AppThemeId
import com.cartracker.app.domain.model.ThemeMode

@Immutable
data class ActiveTheme(val id: AppThemeId, val variant: ThemeVariant)

val LocalActiveTheme = staticCompositionLocalOf {
    ActiveTheme(AppThemeId.NEON_CITY, AppThemes.variant(AppThemeId.NEON_CITY, true))
}

/** اگر true باشد، انیمیشن‌های تزئینی خاموش یا ساده می‌شوند. */
val LocalReduceMotion = staticCompositionLocalOf { false }

private val AnimeShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp)
)

@Composable
fun CarTrackerTheme(
    themeId: AppThemeId,
    mode: ThemeMode,
    reduceMotion: Boolean,
    content: @Composable () -> Unit
) {
    val dark = when (mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val variant = AppThemes.variant(themeId, dark)
    val colors = animateScheme(variant.colorScheme, reduceMotion)
    val active = remember(themeId, dark) { ActiveTheme(themeId, variant) }

    // رنگ آیکون‌های نوار وضعیت و ناوبری باید با حالت روز/شب خود برنامه هماهنگ باشد.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !dark
                controller.isAppearanceLightNavigationBars = !dark
            }
        }
    }

    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Rtl,
        LocalActiveTheme provides active,
        LocalReduceMotion provides reduceMotion
    ) {
        MaterialTheme(
            colorScheme = colors,
            typography = Typography(),
            shapes = AnimeShapes,
            content = content
        )
    }
}

/** تغییر نرم رنگ‌ها هنگام جابه‌جایی روز/شب یا تم. */
@Composable
private fun animateScheme(target: ColorScheme, reduceMotion: Boolean): ColorScheme {
    val spec: AnimationSpec<Color> = if (reduceMotion) snap() else tween(durationMillis = 500)

    @Composable
    fun animated(color: Color): Color =
        animateColorAsState(targetValue = color, animationSpec = spec, label = "scheme").value

    return target.copy(
        primary = animated(target.primary),
        onPrimary = animated(target.onPrimary),
        primaryContainer = animated(target.primaryContainer),
        onPrimaryContainer = animated(target.onPrimaryContainer),
        secondary = animated(target.secondary),
        onSecondary = animated(target.onSecondary),
        secondaryContainer = animated(target.secondaryContainer),
        onSecondaryContainer = animated(target.onSecondaryContainer),
        tertiary = animated(target.tertiary),
        onTertiary = animated(target.onTertiary),
        background = animated(target.background),
        onBackground = animated(target.onBackground),
        surface = animated(target.surface),
        onSurface = animated(target.onSurface),
        surfaceVariant = animated(target.surfaceVariant),
        onSurfaceVariant = animated(target.onSurfaceVariant),
        outline = animated(target.outline),
        surfaceContainer = animated(target.surfaceContainer),
        surfaceContainerHigh = animated(target.surfaceContainerHigh),
        surfaceContainerHighest = animated(target.surfaceContainerHighest)
    )
}
