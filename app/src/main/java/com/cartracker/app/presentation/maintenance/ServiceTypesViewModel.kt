package com.cartracker.app.presentation.maintenance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cartracker.app.core.AppFormatter
import com.cartracker.app.domain.model.ServiceType
import com.cartracker.app.domain.repository.ServiceTypeRepository
import com.cartracker.app.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TypeDialogState(
    val id: Long = 0,
    val name: String = "",
    val km: String = "",
    val months: String = "",
    val isDefault: Boolean = false,
    val nameError: Boolean = false,
    val duplicateError: Boolean = false,
    val numberError: Boolean = false
)

data class ServiceTypesState(
    val isLoading: Boolean = true,
    val types: List<ServiceType> = emptyList(),
    val dialog: TypeDialogState? = null
)

@HiltViewModel
class ServiceTypesViewModel @Inject constructor(
    private val typeRepository: ServiceTypeRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ServiceTypesState())
    val state: StateFlow<ServiceTypesState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            typeRepository.observeAll().collect { all ->
                _state.update { it.copy(isLoading = false, types = all.filter { type -> !type.isArchived }) }
            }
        }
    }

    fun openNew() {
        _state.update { it.copy(dialog = TypeDialogState()) }
    }

    fun openEdit(type: ServiceType) {
        viewModelScope.launch {
            val formatter = AppFormatter(settingsRepository.settings.first().persianDigits)
            _state.update {
                it.copy(
                    dialog = TypeDialogState(
                        id = type.id,
                        name = type.name,
                        km = type.defaultIntervalKm?.let { km -> formatter.plain(km.toDouble(), 0) }.orEmpty(),
                        months = type.defaultIntervalMonths?.let { m -> formatter.plain(m.toDouble(), 0) }.orEmpty(),
                        isDefault = type.isDefault
                    )
                )
            }
        }
    }

    fun updateDialog(transform: (TypeDialogState) -> TypeDialogState) {
        _state.update { state -> state.dialog?.let { state.copy(dialog = transform(it)) } ?: state }
    }

    fun dismissDialog() {
        _state.update { it.copy(dialog = null) }
    }

    fun saveDialog() {
        val dialog = _state.value.dialog ?: return
        val name = dialog.name.trim()
        val km = if (dialog.km.isBlank()) null else AppFormatter.parseInt(dialog.km)?.takeIf { it > 0 }
        val months = if (dialog.months.isBlank()) null else AppFormatter.parseInt(dialog.months)?.takeIf { it > 0 }

        val nameError = name.isEmpty()
        val numberError = (dialog.km.isNotBlank() && km == null) || (dialog.months.isNotBlank() && months == null)
        if (nameError || numberError) {
            updateDialog { it.copy(nameError = nameError, numberError = numberError, duplicateError = false) }
            return
        }

        viewModelScope.launch {
            val result = runCatching {
                typeRepository.save(
                    ServiceType(
                        id = dialog.id,
                        name = name,
                        defaultIntervalKm = km,
                        defaultIntervalMonths = months,
                        isDefault = dialog.isDefault
                    )
                )
            }
            if (result.isSuccess) {
                _state.update { it.copy(dialog = null) }
            } else {
                // معمولاً یعنی نام تکراری است
                updateDialog { it.copy(duplicateError = true, nameError = false, numberError = false) }
            }
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch { typeRepository.deleteOrArchive(id) }
    }
}
