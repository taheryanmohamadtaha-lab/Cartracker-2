package com.cartracker.app.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.cartracker.app.domain.model.AppSettings
import com.cartracker.app.domain.model.AppThemeId
import com.cartracker.app.domain.model.CurrencyUnit
import com.cartracker.app.domain.model.ThemeMode
import com.cartracker.app.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class DataStoreSettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {

    private object Keys {
        val THEME = stringPreferencesKey("theme_id")
        val MODE = stringPreferencesKey("theme_mode")
        val REDUCE = booleanPreferencesKey("reduce_animations")
        val PERSIAN_DIGITS = booleanPreferencesKey("persian_digits")
        val CURRENCY = stringPreferencesKey("currency")
        val ACTIVE_VEHICLE = longPreferencesKey("active_vehicle_id")
    }

    override val settings: Flow<AppSettings> = context.settingsDataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { prefs ->
            AppSettings(
                themeId = prefs[Keys.THEME]
                    ?.let { name -> runCatching { AppThemeId.valueOf(name) }.getOrNull() }
                    ?: AppThemeId.NEON_CITY,
                themeMode = prefs[Keys.MODE]
                    ?.let { name -> runCatching { ThemeMode.valueOf(name) }.getOrNull() }
                    ?: ThemeMode.SYSTEM,
                reduceAnimations = prefs[Keys.REDUCE] ?: false,
                persianDigits = prefs[Keys.PERSIAN_DIGITS] ?: true,
                currency = prefs[Keys.CURRENCY]
                    ?.let { name -> runCatching { CurrencyUnit.valueOf(name) }.getOrNull() }
                    ?: CurrencyUnit.TOMAN,
                activeVehicleId = prefs[Keys.ACTIVE_VEHICLE]
            )
        }

    override suspend fun setTheme(themeId: AppThemeId) {
        context.settingsDataStore.edit { it[Keys.THEME] = themeId.name }
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        context.settingsDataStore.edit { it[Keys.MODE] = mode.name }
    }

    override suspend fun setReduceAnimations(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.REDUCE] = enabled }
    }

    override suspend fun setPersianDigits(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.PERSIAN_DIGITS] = enabled }
    }

    override suspend fun setCurrency(currency: CurrencyUnit) {
        context.settingsDataStore.edit { it[Keys.CURRENCY] = currency.name }
    }

    override suspend fun setActiveVehicle(id: Long) {
        context.settingsDataStore.edit { it[Keys.ACTIVE_VEHICLE] = id }
    }
}
