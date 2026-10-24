package com.cartracker.app.presentation.fuel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cartracker.app.core.AppFormatter
import com.cartracker.app.core.JalaliDate
import com.cartracker.app.domain.model.FuelKind
import com.cartracker.app.domain.model.FuelLog
import com.cartracker.app.domain.model.Vehicle
import com.cartracker.app.domain.repository.FuelLogRepository
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
import kotlin.math.roundToInt

enum class AmountField { LITERS, PRICE, TOTAL }

data class FuelWarning(val type: Type, val value: Int) {
    enum class Type { BELOW_PREVIOUS, ABOVE_NEXT, BELOW_INITIAL, OVER_TANK }
}

data class FuelFormState(
    val isLoading: Boolean = true,
    val notFound: Boolean = false,
    val isNew: Boolean = true,
    val vehicle: Vehicle? = null,
    val lastOdometerKm: Int? = null,
    val dateMillis: Long = 0L,
    val odometer: String = "",
    val liters: String = "",
    val price: String = "",
    val total: String = "",
    val fuelKind: FuelKind = FuelKind.PETROL,
    val station: String = "",
    val isFullTank: Boolean = true,
    val note: String = "",
    /** دو فیلد مبلغی که کاربر آخر وارد کرده؛ فیلد سوم خودکار محاسبه می‌شود. */
    val edited: List<AmountField> = emptyList(),
    val odometerError: Boolean = false,
    val amountError: Boolean = false,
    val saveFailed: Boolean = false,
    val isSaving: Boolean = false,
    val warnings: List<FuelWarning> = emptyList()
)

@HiltViewModel
class FuelEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val vehicleRepository: VehicleRepository,
    private val fuelRepository: FuelLogRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val vehicleId: Long = savedStateHandle.get<Long>("vehicleId") ?: -1L
    private val logId: Long = savedStateHandle.get<Long>("logId") ?: -1L

    private var formatter = AppFormatter()
    private var allLogs: List<FuelLog> = emptyList()

    private val _form = MutableStateFlow(FuelFormState(isNew = logId <= 0))
    val form: StateFlow<FuelFormState> = _form.asStateFlow()

    private val _done = Channel<Unit>(Channel.BUFFERED)
    val done = _done.receiveAsFlow()

    init {
        viewModelScope.launch {
            val settings = settingsRepository.settings.first()
            formatter = AppFormatter(settings.persianDigits, settings.currency)
            val vehicle = vehicleRepository.getById(vehicleId)
            if (vehicle == null) {
                _form.update { it.copy(isLoading = false, notFound = true) }
                return@launch
            }
            allLogs = fuelRepository.observeByVehicle(vehicleId).first()
            val last = allLogs.maxOfOrNull { it.odometerKm }
            val existing = if (logId > 0) allLogs.firstOrNull { it.id == logId } else null
            if (logId > 0 && existing == null) {
                _form.update { it.copy(isLoading = false, notFound = true) }
                return@launch
            }

            val base = FuelFormState(
                isLoading = false,
                isNew = existing == null,
                vehicle = vehicle,
                lastOdometerKm = last,
                dateMillis = JalaliDate.today().toEpochMillis(),
                fuelKind = vehicle.fuelType.kinds.first()
            )
            val initial = if (existing == null) {
                base
            } else {
                base.copy(
                    dateMillis = existing.dateMillis,
                    odometer = formatter.plain(existing.odometerKm.toDouble(), 0),
                    liters = formatter.plain(existing.liters, 2),
                    price = formatter.plain(formatter.toDisplayAmount(existing.pricePerLiter), 0),
                    total = formatter.plain(formatter.toDisplayAmount(existing.totalCost), 0),
                    fuelKind = existing.fuelKind,
                    station = existing.station.orEmpty(),
                    isFullTank = existing.isFullTank,
                    note = existing.note.orEmpty(),
                    edited = listOf(AmountField.LITERS, AmountField.PRICE)
                )
            }
            _form.value = initial.copy(warnings = warningsFor(initial))
        }
    }

    // ----- تغییر فیلدها -----

    fun onDateChange(millis: Long) = mutate { it.copy(dateMillis = millis) }

    fun onOdometerChange(text: String) =
        mutate { it.copy(odometer = text.filter { c -> c.isDigit() }, odometerError = false) }

    fun onKindChange(kind: FuelKind) = mutate { it.copy(fuelKind = kind) }

    fun onStationChange(text: String) = mutate { it.copy(station = text) }

    fun onFullTankChange(value: Boolean) = mutate { it.copy(isFullTank = value) }

    fun onNoteChange(text: String) = mutate { it.copy(note = text) }

    fun onAmountChange(field: AmountField, text: String) {
        val cleaned = text.filter { c -> c.isDigit() || c in NUMBER_SYMBOLS }
        mutate { state ->
            val withText = when (field) {
                AmountField.LITERS -> state.copy(liters = cleaned)
                AmountField.PRICE -> state.copy(price = cleaned)
                AmountField.TOTAL -> state.copy(total = cleaned)
            }
            recompute(
                withText.copy(
                    edited = (state.edited - field + field).takeLast(2),
                    amountError = false
                )
            )
        }
    }

    private fun mutate(transform: (FuelFormState) -> FuelFormState) {
        _form.update { current ->
            val next = transform(current)
            next.copy(warnings = warningsFor(next))
        }
    }

    /** از بین سه فیلد لیتر، قیمت هر لیتر و مبلغ کل، آن که کاربر ننوشته از دو فیلد دیگر محاسبه می‌شود. */
    private fun recompute(state: FuelFormState): FuelFormState {
        if (state.edited.size < 2) return state
        val derived = AmountField.entries.first { it !in state.edited }
        val liters = AppFormatter.parseDouble(state.liters)
        val price = AppFormatter.parseDouble(state.price)
        val total = AppFormatter.parseDouble(state.total)
        return when (derived) {
            AmountField.TOTAL -> state.copy(
                total = if (liters != null && price != null) formatter.plain(liters * price, 0) else ""
            )
            AmountField.PRICE -> state.copy(
                price = if (total != null && liters != null && liters > 0.0) {
                    formatter.plain(total / liters, 0)
                } else {
                    ""
                }
            )
            AmountField.LITERS -> state.copy(
                liters = if (total != null && price != null && price > 0.0) {
                    formatter.plain(total / price, 2)
                } else {
                    ""
                }
            )
        }
    }

    /** هشدارهای غیرمسدودکننده: کیلومتر نامعقول یا لیتر بیشتر از ظرفیت باک. */
    private fun warningsFor(state: FuelFormState): List<FuelWarning> {
        val vehicle = state.vehicle ?: return emptyList()
        val result = mutableListOf<FuelWarning>()

        val odometer = AppFormatter.parseInt(state.odometer)
        if (odometer != null) {
            val others = allLogs.filter { it.id != logId }
            val byDate = compareBy<FuelLog>({ it.dateMillis }, { it.odometerKm })
            val previous = others.filter { it.dateMillis <= state.dateMillis }.maxWithOrNull(byDate)
            val next = others.filter { it.dateMillis > state.dateMillis }.minWithOrNull(byDate)
            if (previous != null && odometer < previous.odometerKm) {
                result += FuelWarning(FuelWarning.Type.BELOW_PREVIOUS, previous.odometerKm)
            }
            if (next != null && odometer > next.odometerKm) {
                result += FuelWarning(FuelWarning.Type.ABOVE_NEXT, next.odometerKm)
            }
            if (previous == null && odometer < vehicle.initialOdometerKm) {
                result += FuelWarning(FuelWarning.Type.BELOW_INITIAL, vehicle.initialOdometerKm)
            }
        }

        val liters = AppFormatter.parseDouble(state.liters)
        val capacity = vehicle.tankCapacityLiters
        if (liters != null && capacity != null && capacity > 0.0 &&
            state.fuelKind != FuelKind.CNG && liters > capacity * 1.05
        ) {
            result += FuelWarning(FuelWarning.Type.OVER_TANK, capacity.roundToInt())
        }
        return result
    }

    fun save() {
        val state = _form.value
        val vehicle = state.vehicle ?: return
        val odometer = AppFormatter.parseInt(state.odometer)?.takeIf { it >= 0 }
        val liters = AppFormatter.parseDouble(state.liters)?.takeIf { it > 0.0 }
        val price = AppFormatter.parseDouble(state.price)?.takeIf { it > 0.0 }
        val total = AppFormatter.parseDouble(state.total)?.takeIf { it > 0.0 }

        if (odometer == null || liters == null || price == null || total == null) {
            _form.update {
                it.copy(
                    odometerError = odometer == null,
                    amountError = liters == null || price == null || total == null
                )
            }
            return
        }

        val log = FuelLog(
            id = if (logId > 0) logId else 0L,
            vehicleId = vehicle.id,
            dateMillis = state.dateMillis,
            odometerKm = odometer,
            liters = liters,
            pricePerLiter = formatter.fromDisplayAmount(price),
            totalCost = formatter.fromDisplayAmount(total),
            fuelKind = state.fuelKind,
            station = state.station.trim().ifEmpty { null },
            isFullTank = state.isFullTank,
            note = state.note.trim().ifEmpty { null }
        )
        viewModelScope.launch {
            _form.update { it.copy(isSaving = true, saveFailed = false) }
            runCatching { fuelRepository.save(log) }
                .onSuccess { _done.send(Unit) }
                .onFailure { _form.update { it.copy(isSaving = false, saveFailed = true) } }
        }
    }

    private companion object {
        const val NUMBER_SYMBOLS = ".٫/,٬،"
    }
}
