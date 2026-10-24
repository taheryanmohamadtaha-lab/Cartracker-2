package com.cartracker.app.presentation.vehicles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cartracker.app.domain.repository.SettingsRepository
import com.cartracker.app.domain.usecase.ObserveVehicleSelection
import com.cartracker.app.domain.usecase.VehicleSelection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VehiclesViewModel @Inject constructor(
    observeSelection: ObserveVehicleSelection,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    /** null یعنی هنوز بارگذاری نشده است. */
    val selection: StateFlow<VehicleSelection?> = observeSelection()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun selectVehicle(id: Long) {
        viewModelScope.launch { settingsRepository.setActiveVehicle(id) }
    }
}
