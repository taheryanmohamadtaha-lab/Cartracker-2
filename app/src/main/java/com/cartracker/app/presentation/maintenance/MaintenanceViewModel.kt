package com.cartracker.app.presentation.maintenance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cartracker.app.domain.repository.ExpenseRepository
import com.cartracker.app.domain.repository.ServiceRecordRepository
import com.cartracker.app.domain.repository.SettingsRepository
import com.cartracker.app.domain.usecase.MaintenanceOverview
import com.cartracker.app.domain.usecase.ObserveMaintenanceOverview
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface MaintenanceUiState {
    data object Loading : MaintenanceUiState
    data object Error : MaintenanceUiState
    data class Content(val overview: MaintenanceOverview) : MaintenanceUiState
}

@HiltViewModel
class MaintenanceViewModel @Inject constructor(
    observeOverview: ObserveMaintenanceOverview,
    private val settingsRepository: SettingsRepository,
    private val serviceRepository: ServiceRecordRepository,
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    val state: StateFlow<MaintenanceUiState> = observeOverview()
        .map<MaintenanceOverview, MaintenanceUiState> { MaintenanceUiState.Content(it) }
        .catch { emit(MaintenanceUiState.Error) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MaintenanceUiState.Loading)

    fun selectVehicle(id: Long) {
        viewModelScope.launch { settingsRepository.setActiveVehicle(id) }
    }

    fun deleteService(id: Long) {
        viewModelScope.launch { serviceRepository.delete(id) }
    }

    fun deleteExpense(id: Long) {
        viewModelScope.launch { expenseRepository.delete(id) }
    }
}
