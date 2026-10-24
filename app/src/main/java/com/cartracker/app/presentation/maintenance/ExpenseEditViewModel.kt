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
import com.cartracker.app.domain.model.ExpenseCategory
import com.cartracker.app.domain.model.ExpenseDetails
import com.cartracker.app.domain.model.ExpenseRecord
import com.cartracker.app.domain.model.InsuranceKind
import com.cartracker.app.domain.model.RepairArea
import com.cartracker.app.domain.model.Vehicle
import com.cartracker.app.domain.repository.AttachmentRepository
import com.cartracker.app.domain.repository.ExpenseRepository
import com.cartracker.app.domain.repository.FuelLogRepository
import com.cartracker.app.domain.repository.ServiceRecordRepository
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
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

data class ExpenseFormState(
    val isLoading: Boolean = true,
    val notFound: Boolean = false,
    val isNew: Boolean = true,
    val vehicle: Vehicle? = null,
    val category: ExpenseCategory = ExpenseCategory.REPAIR,
    val title: String = "",
    val dateMillis: Long = 0L,
    val odometer: String = "",
    val amount: String = "",
    val tripTag: String = "",
    val note: String = "",
    // تعمیرات
    val repairArea: RepairArea? = null,
    // قطعات
    val brand: String = "",
    val partNumber: String = "",
    val warrantyMonths: String = "",
    // بیمه
    val insuranceKind: InsuranceKind = InsuranceKind.THIRD_PARTY,
    val company: String = "",
    val policyNumber: String = "",
    val startMillis: Long = 0L,
    val discount: String = "",
    /** پایان بیمه یا انقضای معاینه فنی. */
    val expiryMillis: Long? = null,
    val tripTags: List<String> = emptyList(),
    val attachments: List<String> = emptyList(),
    val currentOdometerKm: Int? = null,
    val titleError: Boolean = false,
    val amountError: Boolean = false,
    val odometerError: Boolean = false,
    val expiryError: Boolean = false,
    val numberError: Boolean = false,
    val saveFailed: Boolean = false,
    val isSaving: Boolean = false
)

/** دسته‌هایی که نام یا عنوان برایشان الزامی است. */
fun ExpenseCategory.requiresTitle(): Boolean = when (this) {
    ExpenseCategory.REPAIR, ExpenseCategory.PART, ExpenseCategory.ACCESSORY, ExpenseCategory.OTHER -> true
    else -> false
}

/** دسته‌هایی که تاریخ پایان/انقضا دارند. */
fun ExpenseCategory.hasExpiry(): Boolean =
    this == ExpenseCategory.INSURANCE || this == ExpenseCategory.INSPECTION

@HiltViewModel
class ExpenseEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val vehicleRepository: VehicleRepository,
    private val expenseRepository: ExpenseRepository,
    private val serviceRepository: ServiceRecordRepository,
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

    private val _form = MutableStateFlow(ExpenseFormState(isNew = recordId <= 0))
    val form: StateFlow<ExpenseFormState> = _form.asStateFlow()

    private val _done = Channel<Unit>(Channel.BUFFERED)
    val done = _done.receiveAsFlow()

    init {
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
        val existing = if (recordId > 0) expenseRepository.getById(recordId) else null
        if (recordId > 0 && existing == null) {
            _form.update { it.copy(isLoading = false, notFound = true) }
            return
        }

        val expenses = expenseRepository.observeByVehicle(vehicleId).first()
        val currentOdometer = ServiceScheduler.currentOdometer(
            initialKm = vehicle.initialOdometerKm,
            fuelOdometers = fuelRepository.observeByVehicle(vehicleId).first().map { it.odometerKm },
            serviceOdometers = serviceRepository.observeByVehicle(vehicleId).first().map { it.odometerKm },
            expenseOdometers = expenses.mapNotNull { it.odometerKm }
        )
        val tags = expenses
            .mapNotNull { it.tripTag?.takeIf { tag -> tag.isNotBlank() } }
            .groupingBy { it }.eachCount()
            .entries.sortedByDescending { it.value }
            .map { it.key }

        if (existing != null) {
            originalAttachments = attachmentRepository.get(AttachmentOwner.EXPENSE_RECORD, existing.id)
        }

        val today = JalaliDate.today().toEpochMillis()
        val details = existing?.details ?: ExpenseDetails()
        _form.update { state ->
            state.copy(
                isLoading = false,
                isNew = existing == null,
                vehicle = vehicle,
                currentOdometerKm = currentOdometer,
                tripTags = tags,
                category = existing?.category ?: ExpenseCategory.REPAIR,
                title = existing?.title.orEmpty(),
                dateMillis = existing?.dateMillis ?: today,
                odometer = existing?.odometerKm?.let { formatter.plain(it.toDouble(), 0) }.orEmpty(),
                amount = existing?.let { formatter.plain(formatter.toDisplayAmount(it.amount), 0) }.orEmpty(),
                tripTag = existing?.tripTag.orEmpty(),
                note = existing?.note.orEmpty(),
                repairArea = details.repairArea,
                brand = details.brand.orEmpty(),
                partNumber = details.partNumber.orEmpty(),
                warrantyMonths = details.warrantyMonths?.let { formatter.plain(it.toDouble(), 0) }.orEmpty(),
                insuranceKind = details.insuranceKind ?: InsuranceKind.THIRD_PARTY,
                company = details.company.orEmpty(),
                policyNumber = details.policyNumber.orEmpty(),
                startMillis = details.startMillis ?: existing?.dateMillis ?: today,
                discount = details.discountPercent?.let { formatter.plain(it.toDouble(), 0) }.orEmpty(),
                expiryMillis = existing?.expiryMillis,
                attachments = originalAttachments
            )
        }
    }

    fun update(transform: (ExpenseFormState) -> ExpenseFormState) {
        _form.update(transform)
    }

    /** با تغییر دسته به بیمه یا معاینه فنی، تاریخ انقضا به‌صورت پیش‌فرض یک سال بعد گذاشته می‌شود. */
    fun onCategoryChange(category: ExpenseCategory) {
        _form.update { state ->
            val expiry = when {
                !category.hasExpiry() -> state.expiryMillis
                state.expiryMillis != null -> state.expiryMillis
                else -> oneYearAfter(state.dateMillis)
            }
            state.copy(category = category, expiryMillis = expiry, titleError = false, expiryError = false)
        }
    }

    private fun oneYearAfter(millis: Long): Long {
        val zone = ZoneId.systemDefault()
        return Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()
            .plusYears(1).atTime(12, 0).atZone(zone).toInstant().toEpochMilli()
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
        val category = state.category

        val title = state.title.trim()
        val amount = AppFormatter.parseDouble(state.amount)?.takeIf { it >= 0.0 }
        val odometer = if (state.odometer.isBlank()) null else AppFormatter.parseInt(state.odometer)
        val warranty = if (state.warrantyMonths.isBlank()) null else AppFormatter.parseInt(state.warrantyMonths)
        val discount = if (state.discount.isBlank()) null else AppFormatter.parseInt(state.discount)

        val titleError = category.requiresTitle() && title.isEmpty()
        val amountError = amount == null
        val odometerError = state.odometer.isNotBlank() && (odometer == null || odometer < 0)
        val expiryError = category.hasExpiry() && state.expiryMillis == null
        val numberError = (category == ExpenseCategory.PART && state.warrantyMonths.isNotBlank() && warranty == null) ||
            (category == ExpenseCategory.INSURANCE && state.discount.isNotBlank() &&
                (discount == null || discount !in 0..100))

        if (titleError || amount == null || odometerError || expiryError || numberError) {
            _form.update {
                it.copy(
                    titleError = titleError,
                    amountError = amountError,
                    odometerError = odometerError,
                    expiryError = expiryError,
                    numberError = numberError
                )
            }
            return
        }

        val details = when (category) {
            ExpenseCategory.REPAIR -> ExpenseDetails(repairArea = state.repairArea)
            ExpenseCategory.PART -> ExpenseDetails(
                brand = state.brand.trim().ifEmpty { null },
                partNumber = state.partNumber.trim().ifEmpty { null },
                warrantyMonths = warranty
            )
            ExpenseCategory.INSURANCE -> ExpenseDetails(
                insuranceKind = state.insuranceKind,
                company = state.company.trim().ifEmpty { null },
                policyNumber = state.policyNumber.trim().ifEmpty { null },
                startMillis = state.startMillis,
                discountPercent = discount
            )
            else -> ExpenseDetails()
        }

        val record = ExpenseRecord(
            id = if (recordId > 0) recordId else 0L,
            vehicleId = vehicle.id,
            category = category,
            title = title,
            dateMillis = state.dateMillis,
            odometerKm = odometer,
            amount = formatter.fromDisplayAmount(amount),
            tripTag = state.tripTag.trim().ifEmpty { null },
            expiryMillis = if (category.hasExpiry()) state.expiryMillis else null,
            details = details,
            note = state.note.trim().ifEmpty { null }
        )
        viewModelScope.launch {
            _form.update { it.copy(isSaving = true, saveFailed = false) }
            runCatching {
                val id = expenseRepository.save(record)
                attachmentRepository.replace(AttachmentOwner.EXPENSE_RECORD, id, state.attachments)
            }.onSuccess {
                saved = true
                _done.send(Unit)
            }.onFailure {
                _form.update { it.copy(isSaving = false, saveFailed = true) }
            }
        }
    }

    override fun onCleared() {
        if (!saved) {
            _form.value.attachments.filter { it !in originalAttachments }.forEach { photoStore.delete(it) }
        }
    }
}
