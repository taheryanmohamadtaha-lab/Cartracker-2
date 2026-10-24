package com.cartracker.app.domain.usecase

import com.cartracker.app.core.JalaliDate
import com.cartracker.app.domain.calc.ServiceDue
import com.cartracker.app.domain.calc.ServiceScheduler
import com.cartracker.app.domain.model.ExpenseCategory
import com.cartracker.app.domain.model.ExpenseRecord
import com.cartracker.app.domain.model.ServiceRecord
import com.cartracker.app.domain.model.ServiceType
import com.cartracker.app.domain.repository.ExpenseRepository
import com.cartracker.app.domain.repository.FuelLogRepository
import com.cartracker.app.domain.repository.ServiceRecordRepository
import com.cartracker.app.domain.repository.ServiceTypeRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

data class ServiceHistoryItem(
    val record: ServiceRecord,
    val typeName: String
)

data class ExpenseSummary(
    val total: Double,
    val thisMonth: Double,
    val byCategory: Map<ExpenseCategory, Double>
) {
    companion object {
        val EMPTY = ExpenseSummary(0.0, 0.0, emptyMap())
    }
}

data class MaintenanceOverview(
    val selection: VehicleSelection,
    /** انواع سرویس فعال (بایگانی‌نشده). */
    val serviceTypes: List<ServiceType>,
    val history: List<ServiceHistoryItem>,
    val dues: List<ServiceDue>,
    val serviceTotal: Double,
    val expenses: List<ExpenseRecord>,
    val expenseSummary: ExpenseSummary,
    val currentOdometerKm: Int,
    val tripTags: List<String>
) {
    companion object {
        fun empty(selection: VehicleSelection) = MaintenanceOverview(
            selection = selection,
            serviceTypes = emptyList(),
            history = emptyList(),
            dues = emptyList(),
            serviceTotal = 0.0,
            expenses = emptyList(),
            expenseSummary = ExpenseSummary.EMPTY,
            currentOdometerKm = 0,
            tripTags = emptyList()
        )
    }
}

/** سرویس‌ها، سررسیدها و هزینه‌های خودروی فعال. */
class ObserveMaintenanceOverview @Inject constructor(
    private val observeSelection: ObserveVehicleSelection,
    private val serviceTypeRepository: ServiceTypeRepository,
    private val serviceRepository: ServiceRecordRepository,
    private val expenseRepository: ExpenseRepository,
    private val fuelRepository: FuelLogRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<MaintenanceOverview> = observeSelection().flatMapLatest { selection ->
        val active = selection.active
        if (active == null) {
            flowOf(MaintenanceOverview.empty(selection))
        } else {
            combine(
                serviceTypeRepository.observeAll(),
                serviceRepository.observeByVehicle(active.id),
                expenseRepository.observeByVehicle(active.id),
                fuelRepository.observeByVehicle(active.id)
            ) { types, services, expenses, fuelLogs ->
                val now = System.currentTimeMillis()
                val typeNames = types.associate { it.id to it.name }
                val activeTypes = types.filter { !it.isArchived }

                val history = services
                    .sortedWith(
                        compareByDescending<ServiceRecord> { it.dateMillis }
                            .thenByDescending { it.odometerKm }
                            .thenByDescending { it.id }
                    )
                    .map { ServiceHistoryItem(it, typeNames[it.serviceTypeId].orEmpty()) }

                val currentOdometer = ServiceScheduler.currentOdometer(
                    initialKm = active.initialOdometerKm,
                    fuelOdometers = fuelLogs.map { it.odometerKm },
                    serviceOdometers = services.map { it.odometerKm },
                    expenseOdometers = expenses.mapNotNull { it.odometerKm }
                )

                val sortedExpenses = expenses.sortedWith(
                    compareByDescending<ExpenseRecord> { it.dateMillis }.thenByDescending { it.id }
                )

                MaintenanceOverview(
                    selection = selection,
                    serviceTypes = activeTypes,
                    history = history,
                    dues = ServiceScheduler.computeDues(activeTypes, services, currentOdometer, now),
                    serviceTotal = services.sumOf { it.cost },
                    expenses = sortedExpenses,
                    expenseSummary = summarize(expenses, now),
                    currentOdometerKm = currentOdometer,
                    tripTags = expenses
                        .mapNotNull { it.tripTag?.takeIf { tag -> tag.isNotBlank() } }
                        .groupingBy { it }.eachCount()
                        .entries.sortedByDescending { it.value }
                        .map { it.key }
                )
            }
        }
    }

    private fun summarize(expenses: List<ExpenseRecord>, nowMillis: Long): ExpenseSummary {
        if (expenses.isEmpty()) return ExpenseSummary.EMPTY
        val today = JalaliDate.fromEpochMillis(nowMillis)
        val start = JalaliDate(today.year, today.month, 1).toEpochMillis()
        val next = if (today.month == 12) {
            JalaliDate(today.year + 1, 1, 1)
        } else {
            JalaliDate(today.year, today.month + 1, 1)
        }.toEpochMillis()
        return ExpenseSummary(
            total = expenses.sumOf { it.amount },
            thisMonth = expenses.filter { it.dateMillis in start until next }.sumOf { it.amount },
            byCategory = expenses.groupBy { it.category }.mapValues { (_, list) -> list.sumOf { it.amount } }
        )
    }
}
