package com.cartracker.app.domain.usecase

import com.cartracker.app.domain.calc.FuelCalculator
import com.cartracker.app.domain.calc.FuelEntryStats
import com.cartracker.app.domain.calc.FuelSummary
import com.cartracker.app.domain.model.FuelLog
import com.cartracker.app.domain.model.Vehicle
import com.cartracker.app.domain.repository.FuelLogRepository
import com.cartracker.app.domain.repository.SettingsRepository
import com.cartracker.app.domain.repository.VehicleRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class VehicleSelection(
    val vehicles: List<Vehicle>,
    val active: Vehicle?
)

/** فهرست خودروها و خودروی فعال (اگر انتخاب‌شده‌ای نباشد، اولین خودرو). */
class ObserveVehicleSelection @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(): Flow<VehicleSelection> = combine(
        vehicleRepository.observeAll(),
        settingsRepository.settings.map { it.activeVehicleId }.distinctUntilChanged()
    ) { vehicles, activeId ->
        VehicleSelection(
            vehicles = vehicles,
            active = vehicles.firstOrNull { it.id == activeId } ?: vehicles.firstOrNull()
        )
    }
}

data class FuelEntry(
    val log: FuelLog,
    val stats: FuelEntryStats
)

data class FuelOverview(
    val selection: VehicleSelection,
    val entries: List<FuelEntry>,   // جدیدترین اول
    val summary: FuelSummary
)

/** سوخت‌گیری‌های خودروی فعال همراه با محاسبات مصرف. */
class ObserveFuelOverview @Inject constructor(
    private val observeSelection: ObserveVehicleSelection,
    private val fuelRepository: FuelLogRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<FuelOverview> = observeSelection().flatMapLatest { selection ->
        val active = selection.active
        if (active == null) {
            flowOf(FuelOverview(selection, emptyList(), FuelSummary.EMPTY))
        } else {
            fuelRepository.observeByVehicle(active.id).map { logs ->
                val analysis = FuelCalculator.analyze(logs)
                val newestFirst = logs.sortedWith(
                    compareByDescending<FuelLog> { it.odometerKm }
                        .thenByDescending { it.dateMillis }
                        .thenByDescending { it.id }
                )
                FuelOverview(
                    selection = selection,
                    entries = newestFirst.map { log ->
                        FuelEntry(log, analysis.entries[log.id] ?: FuelEntryStats())
                    },
                    summary = analysis.summary
                )
            }
        }
    }
}
