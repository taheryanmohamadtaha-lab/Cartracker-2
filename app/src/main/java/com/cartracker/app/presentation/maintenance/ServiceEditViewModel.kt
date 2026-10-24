package com.cartracker.app.presentation.maintenance

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cartracker.app.core.AppFormatter
import com.cartracker.app.core.JalaliDate
import com.cartracker.app.data.files.VehiclePhotoStore
import com.cartracker.app.domain.calc.ServiceScheduler
import com.cartracker.app.domain.model.AttachmentOwner
import com.cartracker.app.domain.model.ServiceRecord
import com.cartracker.app.domain.model.ServiceType
import com.cartracker.app.domain.model.Vehicle
import com.cartracker.app.domain.repository.AttachmentRepository
import com.cartracker.app.domain.repository.ExpenseRepository
import com.cartracker.app.domain.repository.FuelLogRepository
import com.cartracker.app.domain.repository.ServiceRecordRepository
import com.cartracker.app.domain.repository.ServiceTypeRepository
import com.cartracker.app.domain.repository.SettingsRepository
import com.cartracker.app.domain.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ServiceFormState(
    val isLoading: Boolean = true,
    val notFound: Boolean = false,
    val isNew: Boolean = true,
    val vehicle: Vehicle? = null,
    val types: List<ServiceType> = emptyList(),
    val typeId: Long? = null,
    val dateMillis: Long = 0L,
    val odometer: String = "",
    val cost: String = "",
    val place: String = "",
    val note: String = "",
    val attachments: List<String> = emptyList(),
    val currentOdometerKm: Int? = null,
    val typeError: Boolean = false,
    val odometerError: Boolean = false,
    val costError: Boolean = false,
    val saveFailed: Boolean = false,
    val isSaving: Boolean = false
) {
    val selectedType: ServiceType? get() = types.firstOrNull { it.id == typeId }
}

@HiltViewModel
class ServiceEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val vehicleRepository: VehicleRepository,
    private val typeRepository: ServiceTypeRepository,
    private val serviceRepository: ServiceRecordRepository,
    private val expenseRepository: ExpenseRepository,
    private val fuelRepository: FuelLogRepository,
    private val attachmentRepository: AttachmentRepository,
    private val settingsRepository: SettingsRepository,
    private val photoStore: VehiclePhotoStore
) : ViewModel() {

    private val vehicleId: Long = savedStateHandle.get<Long>("vehicleId") ?: -1L
    private val recordId: Long = savedStateHandle.get<Long>("recordId") ?: -1L

    private var formatter = AppFormatter()
    private var originalAttachments: List<String> = emptyList()
    private var saved = false

    private val _form = MutableStateFlow(ServiceFormState(isNew = recordId <= 0))
    val form: StateFlow<ServiceFormState> = _form.asStateFlow()

    private val _done = Channel<Unit>(Channel.BUFFERED)
    val done = _done.receiveAsFlow()

    init {
        // فهرست انواع سرویس زنده است (مثلاً بعد از افزودن نوع تازه در صفحه مدیریت انواع به‌روز می‌شود).
        viewModelScope.launch {
            typeRepository.observeAll().collect { all ->
                _form.update { state ->
                    state.copy(types = all.filter { !it.isArchived || it.id == state.typeId })
                }
            }
        }
        viewModelScope.launch { load() }
    }

    private suspend fun load() {
        val settings = settingsRepository.settings.first()
        formatter = AppFormatter(settings.persianDigits, settings.currency)

        val vehicle = vehicleRepository.getById(vehicleId)
        if (vehicle == null) {
            _form.update { it.copy(isLoading = false, notFound = true) }
            return
        }
        val existing = if (recordId > 0) serviceRepository.getById(recordId) else null
        if (recordId > 0 && existing == null) {
            _form.update { it.copy(isLoading = false, notFound = true) }
            return
        }

        val fuelOdometers = fuelRepository.observeByVehicle(vehicleId).first().map { it.odometerKm }
        val serviceOdometers = serviceRepository.observeByVehicle(vehicleId).first().map { it.odometerKm }
        val expenseOdometers = expenseRepository.observeByVehicle(vehicleId).first().mapNotNull { it.odometerKm }
        val currentOdometer = ServiceScheduler.currentOdometer(
            vehicle.initialOdometerKm, fuelOdometers, serviceOdometers, expenseOdometers
        )

        if (existing != null) {
            originalAttachments = attachmentRepository.get(AttachmentOwner.SERVICE_RECORD, existing.id)
        }

        _form.update { state ->
            state.copy(
                isLoading = false,
                isNew = existing == null,
                vehicle = vehicle,
                currentOdometerKm = currentOdometer,
                typeId = existing?.serviceTypeId,
                dateMillis = existing?.dateMillis ?: JalaliDate.today().toEpochMillis(),
                odometer = existing?.let { formatter.plain(it.odometerKm.toDouble(), 0) }.orEmpty(),
                cost = existing?.let { formatter.plain(formatter.toDisplayAmount(it.cost), 0) }.orEmpty(),
                place = existing?.place.orEmpty(),
                note = existing?.note.orEmpty(),
                attachments = originalAttachments
            )
        }
    }

    fun update(transform: (ServiceFormState) -> ServiceFormState) {
        _form.update(transform)
    }

    fun onTypeSelected(type: ServiceType) {
        _form.update { it.copy(typeId = type.id, typeError = false) }
    }

    fun onPhotoPicked(uri: Uri) {
        viewModelScope.launch {
            val path = photoStore.import(uri) ?: return@launch
            _form.update { it.copy(attachments = it.attachments + path) }
        }
    }

    fun removeAttachment(path: String) {
        _form.update { it.copy(attachments = it.attachments - path) }
        if (path !in originalAttachments) photoStore.delete(path)
    }

    fun save() {
        val state = _form.value
        val vehicle = state.vehicle ?: return
        val typeId = state.typeId
        val odometer = AppFormatter.parseInt(state.odometer)?.takeIf { it >= 0 }
        val cost = if (state.cost.isBlank()) 0.0 else AppFormatter.parseDouble(state.cost)?.takeIf { it >= 0.0 }

        if (typeId == null || odometer == null || cost == null) {
            _form.update {
                it.copy(
                    typeError = typeId == null,
                    odometerError = odometer == null,
                    costError = cost == null
                )
            }
            return
        }

        val record = ServiceRecord(
            id = if (recordId > 0) recordId else 0L,
            vehicleId = vehicle.id,
            serviceTypeId = typeId,
            dateMillis = state.dateMillis,
            odometerKm = odometer,
            cost = formatter.fromDisplayAmount(cost),
            place = state.place.trim().ifEmpty { null },
            note = state.note.trim().ifEmpty { null }
        )
        viewModelScope.launch {
            _form.update { it.copy(isSaving = true, saveFailed = false) }
            runCatching {
                val id = serviceRepository.save(record)
                attachmentRepository.replace(AttachmentOwner.SERVICE_RECORD, id, state.attachments)
            }.onSuccess {
                saved = true
                _done.send(Unit)
            }.onFailure {
                _form.update { it.copy(isSaving = false, saveFailed = true) }
            }
        }
    }

    override fun onCleared() {
        // عکس‌هایی که اضافه شده ولی ذخیره نشده‌اند را پاک می‌کنیم.
        if (!saved) {
            _form.value.attachments.filter { it !in originalAttachments }.forEach { photoStore.delete(it) }
        }
    }
}
