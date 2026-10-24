package com.cartracker.app.domain.repository

import com.cartracker.app.domain.model.AppSettings
import com.cartracker.app.domain.model.AppThemeId
import com.cartracker.app.domain.model.CurrencyUnit
import com.cartracker.app.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<AppSettings>
    suspend fun setTheme(themeId: AppThemeId)
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setReduceAnimations(enabled: Boolean)
    suspend fun setPersianDigits(enabled: Boolean)
    suspend fun setCurrency(currency: CurrencyUnit)
    suspend fun setActiveVehicle(id: Long)
}
