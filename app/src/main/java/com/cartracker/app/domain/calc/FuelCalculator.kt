package com.cartracker.app.domain.calc

import com.cartracker.app.domain.model.FuelKind
import com.cartracker.app.domain.model.FuelLog

data class FuelEntryStats(
    /** فاصله از سوخت‌گیری قبلی (بر اساس کیلومتر). */
    val distanceSincePreviousKm: Int? = null,
    /** مصرف (لیتر در ۱۰۰ کیلومتر)؛ فقط برای سوخت‌گیری «باک پر» که مبنای قبلی دارد. */
    val consumptionPer100Km: Double? = null,
    /** هزینه سوخت به ازای هر کیلومتر (تومان). */
    val costPerKm: Double? = null
)

data class FuelSummary(
    val averageByKind: Map<FuelKind, Double>,
    val lastConsumption: Double?,
    val averageCostPerKm: Double?,
    val totalCost: Double,
    val totalLiters: Double,
    val averagePricePerLiter: Double?,
    val count: Int
) {
    companion object {
        val EMPTY = FuelSummary(emptyMap(), null, null, 0.0, 0.0, null, 0)
    }
}

data class FuelAnalysis(
    val entries: Map<Long, FuelEntryStats>,
    val summary: FuelSummary
)

/**
 * روش «باک پر به باک پر»:
 * مصرف هر بازه = مجموع لیتر سوخت‌گیری‌های بعد از مبنا تا همین باک پر ÷ مسافت بین دو باک پر × ۱۰۰.
 * سوخت‌گیری ناقص بین دو باک پر فقط به لیتر بازه اضافه می‌شود.
 * برای دوگانه‌سوز، هر نوع سوخت جداگانه حساب می‌شود (تقریبی است، چون مسافت با هر دو سوخت طی می‌شود).
 */
object FuelCalculator {

    private class Interval(
        val kind: FuelKind,
        val distanceKm: Int,
        val liters: Double,
        val cost: Double
    )

    fun analyze(logs: List<FuelLog>): FuelAnalysis {
        if (logs.isEmpty()) return FuelAnalysis(emptyMap(), FuelSummary.EMPTY)

        val sorted = logs.sortedWith(
            compareBy<FuelLog> { it.odometerKm }.thenBy { it.dateMillis }.thenBy { it.id }
        )

        val intervals = HashMap<Long, Interval>()
        for (kind in FuelKind.entries) {
            var baseline: FuelLog? = null
            var liters = 0.0
            var cost = 0.0
            for (log in sorted) {
                if (log.fuelKind != kind) continue
                val base = baseline
                if (base != null) {
                    liters += log.liters
                    cost += log.totalCost
                }
                if (log.isFullTank) {
                    if (base != null) {
                        val distance = log.odometerKm - base.odometerKm
                        if (distance > 0 && liters > 0.0) {
                            intervals[log.id] = Interval(kind, distance, liters, cost)
                        }
                    }
                    baseline = log
                    liters = 0.0
                    cost = 0.0
                }
            }
        }

        val entries = HashMap<Long, FuelEntryStats>()
        for (index in sorted.indices) {
            val log = sorted[index]
            val previous = if (index > 0) sorted[index - 1] else null
            val distance = previous?.let { log.odometerKm - it.odometerKm }?.takeIf { it > 0 }
            val interval = intervals[log.id]
            entries[log.id] = FuelEntryStats(
                distanceSincePreviousKm = distance,
                consumptionPer100Km = interval?.let { it.liters / it.distanceKm * 100.0 },
                costPerKm = interval?.let { it.cost / it.distanceKm }
            )
        }

        val averageByKind = intervals.values
            .groupBy { it.kind }
            .mapValues { (_, list) ->
                list.sumOf { it.liters } / list.sumOf { it.distanceKm.toDouble() } * 100.0
            }

        val lastWithInterval = sorted.lastOrNull { intervals.containsKey(it.id) }
        val lastConsumption = lastWithInterval?.let { entries[it.id]?.consumptionPer100Km }

        val span = sorted.last().odometerKm - sorted.first().odometerKm
        val averageCostPerKm = if (sorted.size >= 2 && span > 0) {
            sorted.drop(1).sumOf { it.totalCost } / span
        } else {
            null
        }

        val totalCost = logs.sumOf { it.totalCost }
        val totalLiters = logs.sumOf { it.liters }

        return FuelAnalysis(
            entries = entries,
            summary = FuelSummary(
                averageByKind = averageByKind,
                lastConsumption = lastConsumption,
                averageCostPerKm = averageCostPerKm,
                totalCost = totalCost,
                totalLiters = totalLiters,
                averagePricePerLiter = if (totalLiters > 0.0) totalCost / totalLiters else null,
                count = logs.size
            )
        )
    }
}
