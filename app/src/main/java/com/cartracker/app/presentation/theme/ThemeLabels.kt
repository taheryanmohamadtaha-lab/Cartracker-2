package com.cartracker.app.presentation.theme

import androidx.annotation.StringRes
import com.cartracker.app.R
import com.cartracker.app.domain.model.AppThemeId
import com.cartracker.app.domain.model.ThemeMode

@StringRes
fun AppThemeId.labelRes(): Int = when (this) {
    AppThemeId.NEON_CITY -> R.string.theme_neon_city
    AppThemeId.SAKURA -> R.string.theme_sakura
    AppThemeId.COAST_SUNSET -> R.string.theme_coast_sunset
    AppThemeId.STARRY_SKY -> R.string.theme_starry_sky
    AppThemeId.DREAM_FOREST -> R.string.theme_dream_forest
}

@StringRes
fun ThemeMode.labelRes(): Int = when (this) {
    ThemeMode.SYSTEM -> R.string.mode_system
    ThemeMode.LIGHT -> R.string.mode_light
    ThemeMode.DARK -> R.string.mode_dark
}
