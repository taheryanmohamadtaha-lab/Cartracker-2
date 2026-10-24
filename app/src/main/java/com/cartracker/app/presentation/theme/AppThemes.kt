package com.cartracker.app.presentation.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.cartracker.app.domain.model.AppThemeId

/** یک نسخه (روز یا شب) از یک تم: پالت Material 3 + رنگ‌های صحنه پس‌زمینه. */
@Immutable
data class ThemeVariant(
    val isDark: Boolean,
    val colorScheme: ColorScheme,
    val gradient: List<Color>,   // از بالا به پایین
    val skyAccent: Color,        // خورشید / ماه
    val far: Color,              // لایه دور
    val mid: Color,              // لایه میانی
    val near: Color,             // لایه نزدیک
    val particle: Color          // گلبرگ، ستاره، کرم شب‌تاب، پنجره‌های روشن
)

private fun scheme(
    dark: Boolean,
    primary: Color,
    onPrimary: Color,
    secondary: Color,
    tertiary: Color,
    background: Color,
    surface: Color,
    onSurface: Color,
    container: Color,
    outline: Color
): ColorScheme {
    val base = if (dark) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = container,
        onPrimaryContainer = onSurface,
        secondary = secondary,
        onSecondary = onPrimary,
        secondaryContainer = container,
        onSecondaryContainer = onSurface,
        tertiary = tertiary,
        onTertiary = onPrimary,
        tertiaryContainer = container,
        onTertiaryContainer = onSurface,
        background = background,
        onBackground = onSurface,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = container,
        onSurfaceVariant = onSurface.copy(alpha = 0.78f),
        outline = outline,
        outlineVariant = outline.copy(alpha = 0.5f),
        surfaceContainerLowest = surface,
        surfaceContainerLow = surface,
        surfaceContainer = surface,
        surfaceContainerHigh = container,
        surfaceContainerHighest = container
    )
}

object AppThemes {

    // ---------- ۱. شهر نئونی شب ----------
    private val neonDark = ThemeVariant(
        isDark = true,
        colorScheme = scheme(
            dark = true,
            primary = Color(0xFFFF6BD6), onPrimary = Color(0xFF2B0A33),
            secondary = Color(0xFFB48CFF), tertiary = Color(0xFF4DE1FF),
            background = Color(0xFF120A24), surface = Color(0xFF1D1138),
            onSurface = Color(0xFFF3E9FF), container = Color(0xFF2C1A52),
            outline = Color(0xFF8A6CC9)
        ),
        gradient = listOf(Color(0xFF120A24), Color(0xFF2A1250), Color(0xFF5B1F7A)),
        skyAccent = Color(0xFFFFD6F5),
        far = Color(0xFF2B1650), mid = Color(0xFF1E0F3D), near = Color(0xFF150A2B),
        particle = Color(0xFFFF9BEA)
    )
    private val neonLight = ThemeVariant(
        isDark = false,
        colorScheme = scheme(
            dark = false,
            primary = Color(0xFF7B2CBF), onPrimary = Color(0xFFFFFFFF),
            secondary = Color(0xFFC2287A), tertiary = Color(0xFF0B7A94),
            background = Color(0xFFFBF3FF), surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF2A1346), container = Color(0xFFEBD9FF),
            outline = Color(0xFF9C7BC9)
        ),
        gradient = listOf(Color(0xFFFFE1F5), Color(0xFFE3C7FF), Color(0xFFB79CFF)),
        skyAccent = Color(0xFFFFFFFF),
        far = Color(0xFFC9A8F2), mid = Color(0xFFAA84E3), near = Color(0xFF8C61CF),
        particle = Color(0xFFFF7ACB)
    )

    // ---------- ۲. گل‌های شکوفه بهاری ----------
    private val sakuraLight = ThemeVariant(
        isDark = false,
        colorScheme = scheme(
            dark = false,
            primary = Color(0xFFC93A70), onPrimary = Color(0xFFFFFFFF),
            secondary = Color(0xFF2F7D52), tertiary = Color(0xFFB45A2C),
            background = Color(0xFFFFF5F8), surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF3B2430), container = Color(0xFFFFE0EA),
            outline = Color(0xFFD09AB0)
        ),
        gradient = listOf(Color(0xFFFFE4EE), Color(0xFFFFF2F6), Color(0xFFE6F6E6)),
        skyAccent = Color(0xFFFFF0B8),
        far = Color(0xFFF6C5D6), mid = Color(0xFFBEE3C3), near = Color(0xFF8FCB9F),
        particle = Color(0xFFFFA9C6)
    )
    private val sakuraDark = ThemeVariant(
        isDark = true,
        colorScheme = scheme(
            dark = true,
            primary = Color(0xFFFF9FC1), onPrimary = Color(0xFF4A1128),
            secondary = Color(0xFF8FD6A4), tertiary = Color(0xFFFFB88A),
            background = Color(0xFF1F1419), surface = Color(0xFF2B1D24),
            onSurface = Color(0xFFFBE9F0), container = Color(0xFF3D2832),
            outline = Color(0xFFB07A90)
        ),
        gradient = listOf(Color(0xFF1F1419), Color(0xFF3A2230), Color(0xFF4D2F3F)),
        skyAccent = Color(0xFFFFE3EE),
        far = Color(0xFF4A2E3E), mid = Color(0xFF2F4A3A), near = Color(0xFF223A2C),
        particle = Color(0xFFFFB3CC)
    )

    // ---------- ۳. غروب جاده ساحلی ----------
    private val coastLight = ThemeVariant(
        isDark = false,
        colorScheme = scheme(
            dark = false,
            primary = Color(0xFFD9480F), onPrimary = Color(0xFFFFFFFF),
            secondary = Color(0xFF1864AB), tertiary = Color(0xFF8A5A00),
            background = Color(0xFFFFF6EC), surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF2B2230), container = Color(0xFFFFE3CC),
            outline = Color(0xFFD9A67A)
        ),
        gradient = listOf(Color(0xFFA7D3FF), Color(0xFFFFD2A1), Color(0xFFFFA27D)),
        skyAccent = Color(0xFFFFF1C9),
        far = Color(0xFF5AA9E6), mid = Color(0xFF3C8DD9), near = Color(0xFF6B5B7B),
        particle = Color(0xFFFFFFFF)
    )
    private val coastDark = ThemeVariant(
        isDark = true,
        colorScheme = scheme(
            dark = true,
            primary = Color(0xFFFFA45C), onPrimary = Color(0xFF3A1A00),
            secondary = Color(0xFF74B9FF), tertiary = Color(0xFFFFD166),
            background = Color(0xFF14192B), surface = Color(0xFF1E2540),
            onSurface = Color(0xFFFFEFE0), container = Color(0xFF2C3558),
            outline = Color(0xFF8A93C0)
        ),
        gradient = listOf(Color(0xFF14192B), Color(0xFF3B3A72), Color(0xFFE0805F)),
        skyAccent = Color(0xFFFFC27A),
        far = Color(0xFF2B3F73), mid = Color(0xFF1E2E57), near = Color(0xFF151F3D),
        particle = Color(0xFFFFD9A0)
    )

    // ---------- ۴. آسمان ستاره‌ای ----------
    private val starryDark = ThemeVariant(
        isDark = true,
        colorScheme = scheme(
            dark = true,
            primary = Color(0xFFFFD166), onPrimary = Color(0xFF3A2A00),
            secondary = Color(0xFF8FB4FF), tertiary = Color(0xFFB9A7FF),
            background = Color(0xFF0B1230), surface = Color(0xFF141C45),
            onSurface = Color(0xFFEAF0FF), container = Color(0xFF1F2A60),
            outline = Color(0xFF6F7DB8)
        ),
        gradient = listOf(Color(0xFF050A1F), Color(0xFF0F1B4D), Color(0xFF263A82)),
        skyAccent = Color(0xFFFFE9A8),
        far = Color(0xFF1A2760), mid = Color(0xFF121B48), near = Color(0xFF0B1230),
        particle = Color(0xFFFFE9A8)
    )
    private val starryLight = ThemeVariant(
        isDark = false,
        colorScheme = scheme(
            dark = false,
            primary = Color(0xFF2F3E9E), onPrimary = Color(0xFFFFFFFF),
            secondary = Color(0xFF8A5A00), tertiary = Color(0xFF6B46C1),
            background = Color(0xFFF1F4FF), surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF1A2255), container = Color(0xFFDDE4FF),
            outline = Color(0xFF8D99D6)
        ),
        gradient = listOf(Color(0xFFC9D6FF), Color(0xFFA7B8F5), Color(0xFF7C8FE0)),
        skyAccent = Color(0xFFFFF4C4),
        far = Color(0xFF8D9DE3), mid = Color(0xFF6F80D0), near = Color(0xFF5463B8),
        particle = Color(0xFFFFD166)
    )

    // ---------- ۵. جنگل رؤیایی ----------
    private val forestLight = ThemeVariant(
        isDark = false,
        colorScheme = scheme(
            dark = false,
            primary = Color(0xFF0B7D6E), onPrimary = Color(0xFFFFFFFF),
            secondary = Color(0xFF2E7D3E), tertiary = Color(0xFF9C4FB8),
            background = Color(0xFFF0FBF6), surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF123027), container = Color(0xFFD6F3E6),
            outline = Color(0xFF7DB8A3)
        ),
        gradient = listOf(Color(0xFFD6F5F0), Color(0xFFBDEBD6), Color(0xFF8FD3B0)),
        skyAccent = Color(0xFFFFFFE0),
        far = Color(0xFF9ED8BC), mid = Color(0xFF63BE95), near = Color(0xFF2F9A72),
        particle = Color(0xFFFFF59A)
    )
    private val forestDark = ThemeVariant(
        isDark = true,
        colorScheme = scheme(
            dark = true,
            primary = Color(0xFF4DE1C1), onPrimary = Color(0xFF00382E),
            secondary = Color(0xFF7BDC8C), tertiary = Color(0xFFD9A3F5),
            background = Color(0xFF07201C), surface = Color(0xFF0F2E28),
            onSurface = Color(0xFFE0FFF5), container = Color(0xFF1A433A),
            outline = Color(0xFF58A892)
        ),
        gradient = listOf(Color(0xFF04140F), Color(0xFF0B3A33), Color(0xFF146456)),
        skyAccent = Color(0xFFC9FFE9),
        far = Color(0xFF0F4A3F), mid = Color(0xFF0A3A31), near = Color(0xFF062A23),
        particle = Color(0xFFE8FF7A)
    )

    fun variant(id: AppThemeId, dark: Boolean): ThemeVariant = when (id) {
        AppThemeId.NEON_CITY -> if (dark) neonDark else neonLight
        AppThemeId.SAKURA -> if (dark) sakuraDark else sakuraLight
        AppThemeId.COAST_SUNSET -> if (dark) coastDark else coastLight
        AppThemeId.STARRY_SKY -> if (dark) starryDark else starryLight
        AppThemeId.DREAM_FOREST -> if (dark) forestDark else forestLight
    }
}
