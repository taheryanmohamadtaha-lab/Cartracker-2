package com.cartracker.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cartracker.app.domain.model.AppSettings
import com.cartracker.app.domain.model.AppThemeId
import com.cartracker.app.domain.model.CurrencyUnit
import com.cartracker.app.domain.model.ThemeMode
import com.cartracker.app.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val repository: SettingsRepository
) : ViewModel() {

    /** null یعنی هنوز از دیتا‌استور خوانده نشده است. */
    val settings: StateFlow<AppSettings?> = repository.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun setTheme(themeId: AppThemeId) {
        viewModelScope.launch { repository.setTheme(themeId) }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { repository.setThemeMode(mode) }
    }

    fun setReduceAnimations(enabled: Boolean) {
        viewModelScope.launch { repository.setReduceAnimations(enabled) }
    }

    fun setPersianDigits(enabled: Boolean) {
        viewModelScope.launch { repository.setPersianDigits(enabled) }
    }

    fun setCurrency(currency: CurrencyUnit) {
        viewModelScope.launch { repository.setCurrency(currency) }
    }
}
