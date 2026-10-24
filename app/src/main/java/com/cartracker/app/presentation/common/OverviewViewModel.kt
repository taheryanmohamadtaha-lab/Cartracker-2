package com.cartracker.app.presentation.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cartracker.app.domain.repository.FuelLogRepository
import com.cartracker.app.domain.repository.SettingsRepository
import com.cartracker.app.domain.usecase.FuelOverview
import com.cartracker.app.domain.usecase.ObserveFuelOverview
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface OverviewUiState {
    data object Loading : OverviewUiState
    data object Error : OverviewUiState
    data class Content(val overview: FuelOverview) : OverviewUiState
}

/** وضعیت مشترک صفحه خانه و صفحه سوخت: خودروی فعال، سوخت‌گیری‌ها و آمار. */
@HiltViewModel
class OverviewViewModel @Inject constructor(
    observeOverview: ObserveFuelOverview,
    private val settingsRepository: SettingsRepository,
    private val fuelRepository: FuelLogRepository
) : ViewModel() {

    val state: StateFlow<OverviewUiState> = observeOverview()
        .map<FuelOverview, OverviewUiState> { OverviewUiState.Content(it) }
        .catch { emit(OverviewUiState.Error) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OverviewUiState.Loading)

    fun selectVehicle(id: Long) {
        viewModelScope.launch { settingsRepository.setActiveVehicle(id) }
    }

    fun deleteLog(id: Long) {
        viewModelScope.launch { fuelRepository.delete(id) }
    }
}
