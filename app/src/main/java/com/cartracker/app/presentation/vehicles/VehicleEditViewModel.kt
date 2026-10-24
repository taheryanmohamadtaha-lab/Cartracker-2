package com.cartracker.app.presentation.vehicles

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cartracker.app.core.AppFormatter
import com.cartracker.app.data.files.VehiclePhotoStore
import com.cartracker.app.domain.model.FuelType
import com.cartracker.app.domain.model.Vehicle
import com.cartracker.app.domain.model.VehicleColors
import com.cartracker.app.domain.repository.AttachmentRepository
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

data class VehicleFormState(
    val isLoading: Boolean = false,
    val isNew: Boolean = true,
    val name: String = "",
    val brand: String = "",
    val model: String = "",
    val year: String = "",
    val plate: String = "",
    val fuelType: FuelType = FuelType.PETROL,
    val tank: String = "",
    val odometer: String = "0",
    val colorArgb: Long = VehicleColors.default,
    val photoPath: String? = null,
    val nameError: Boolean = false,
    val yearError: Boolean = false,
    val tankError: Boolean = false,
    val odometerError: Boolean = false
)

@HiltViewModel
class VehicleEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val vehicleRepository: VehicleRepository,
    private val settingsRepository: SettingsRepository,
    private val attachmentRepository: AttachmentRepository,
    private val photoStore: VehiclePhotoStore
) : ViewModel() {

    private val vehicleId: Long = savedStateHandle.get<Long>("vehicleId") ?: -1L
    private var original: Vehicle? = null

    private val _form = MutableStateFlow(
        VehicleFormState(isLoading = vehicleId > 0, isNew = vehicleId <= 0)
    )
    val form: StateFlow<VehicleFormState> = _form.asStateFlow()

    private val _done = Channel<Unit>(Channel.BUFFERED)
    val done = _done.receiveAsFlow()

    init {
        viewModelScope.launch {
            val formatter = AppFormatter(settingsRepository.settings.first().persianDigits)
            if (vehicleId > 0) {
                val vehicle = vehicleRepository.getById(vehicleId)
                if (vehicle == null) {
                    _done.send(Unit)
                } else {
                    original = vehicle
                    _form.value = VehicleFormState(
                        isLoading = false,
                        isNew = false,
                        name = vehicle.name,
                        brand = vehicle.brand,
                        model = vehicle.model,
                        year = vehicle.year?.let { formatter.plain(it.toDouble(), 0) }.orEmpty(),
                        plate = vehicle.plate.orEmpty(),
                        fuelType = vehicle.fuelType,
                        tank = vehicle.tankCapacityLiters?.let { formatter.plain(it, 1) }.orEmpty(),
                        odometer = formatter.plain(vehicle.initialOdometerKm.toDouble(), 0),
                        colorArgb = vehicle.colorArgb,
                        photoPath = vehicle.photoPath
                    )
                }
            } else {
                _form.update { it.copy(odometer = formatter.digits("0")) }
            }
        }
    }

    fun update(transform: (VehicleFormState) -> VehicleFormState) {
        _form.update(transform)
    }

    fun onPhotoPicked(uri: Uri) {
        viewModelScope.launch {
            val path = photoStore.import(uri) ?: return@launch
            val previous = _form.value.photoPath
            // عکس انتخاب‌شده‌ای که هنوز ذخیره نشده بود، دیگر لازم نیست.
            if (previous != null && previous != original?.photoPath) photoStore.delete(previous)
            _form.update { it.copy(photoPath = path) }
        }
    }

    fun removePhoto() {
        val previous = _form.value.photoPath
        if (previous != null && previous != original?.photoPath) photoStore.delete(previous)
        _form.update { it.copy(photoPath = null) }
    }

    fun save() {
        val f = _form.value
        val name = f.name.trim()
        val odometer = AppFormatter.parseInt(f.odometer)
        val year = if (f.year.isBlank()) null else AppFormatter.parseInt(f.year)
        val tank = if (f.tank.isBlank()) null else AppFormatter.parseDouble(f.tank)

        val nameError = name.isEmpty()
        val odometerError = odometer == null || odometer < 0
        val yearError = f.year.isNotBlank() && (year == null || year !in 1200..2100)
        val tankError = f.tank.isNotBlank() && (tank == null || tank <= 0.0)
        if (nameError || odometerError || yearError || tankError || odometer == null) {
            _form.update {
                it.copy(
                    nameError = nameError,
                    odometerError = odometerError,
                    yearError = yearError,
                    tankError = tankError
                )
            }
            return
        }

        val old = original
        val vehicle = Vehicle(
            id = old?.id ?: 0L,
            name = name,
            brand = f.brand.trim(),
            model = f.model.trim(),
            year = year,
            plate = f.plate.trim().ifEmpty { null },
            fuelType = f.fuelType,
            tankCapacityLiters = tank,
            initialOdometerKm = odometer,
            colorArgb = f.colorArgb,
            photoPath = f.photoPath,
            createdAt = old?.createdAt ?: System.currentTimeMillis()
        )
        viewModelScope.launch {
            val id = vehicleRepository.save(vehicle)
            if (old == null) settingsRepository.setActiveVehicle(id)
            if (old?.photoPath != null && old.photoPath != f.photoPath) photoStore.delete(old.photoPath)
            _done.send(Unit)
        }
    }

    fun delete() {
        val old = original ?: return
        viewModelScope.launch {
            vehicleRepository.delete(old.id)
            photoStore.delete(old.photoPath)
            // عکس فاکتورهای سرویس‌ها و هزینه‌های این خودرو دیگر لازم نیست.
            attachmentRepository.cleanupOrphans()
            _done.send(Unit)
        }
    }
}
