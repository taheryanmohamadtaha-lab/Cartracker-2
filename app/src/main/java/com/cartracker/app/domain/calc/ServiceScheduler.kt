package com.cartracker.app.domain.calc

import com.cartracker.app.domain.model.ServiceRecord
import com.cartracker.app.domain.model.ServiceType
import java.time.Instant
import java.time.ZoneId
import kotlin.math.max

enum class DueStatus { OVERDUE, SOON, OK }

data class ServiceDue(
    val type: ServiceType,
    val lastRecord: ServiceRecord,
    val nextDueKm: Int?,
    val nextDueMillis: Long?,
    val remainingKm: Int?,
    /** منفی یعنی از سررسید گذشته است. */
    val remainingDays: Long?,
    val status: DueStatus
)

/**
 * سررسید سرویس بعدی بر اساس آخرین ثبت هر نوع سرویس:
 * سررسید = کیلومتر آخرین سرویس + فاصله کیلومتری، و تاریخ آخرین سرویس + فاصله ماهانه؛ هرکدام زودتر برسد.
 */
object ServiceScheduler {

    private const val MILLIS_PER_DAY = 86_400_000L
    private const val SOON_DAYS = 30L
    private const val SOON_MIN_KM = 500

    fun nextDueKm(lastOdometerKm: Int, intervalKm: Int?): Int? =
        intervalKm?.let { lastOdometerKm + it }

    fun nextDueMillis(
        lastMillis: Long,
        intervalMonths: Int?,
        zone: ZoneId = ZoneId.systemDefault()
    ): Long? = intervalMonths?.let { months ->
        Instant.ofEpochMilli(lastMillis).atZone(zone).toLocalDate()
            .plusMonths(months.toLong())
            .atTime(12, 0).atZone(zone).toInstant().toEpochMilli()
    }

    /** کیلومتر فعلی تخمینی = بزرگ‌ترین کیلومتر ثبت‌شده در همه سوابق (یا کیلومتر اولیه). */
    fun currentOdometer(
        initialKm: Int,
        fuelOdometers: List<Int>,
        serviceOdometers: List<Int>,
        expenseOdometers: List<Int>
    ): Int = (listOf(initialKm) + fuelOdometers + serviceOdometers + expenseOdometers).max()

    fun computeDues(
        types: List<ServiceType>,
        records: List<ServiceRecord>,
        currentOdometerKm: Int,
        nowMillis: Long,
        zone: ZoneId = ZoneId.systemDefault()
    ): List<ServiceDue> {
        val byType = records.groupBy { it.serviceTypeId }
        val result = mutableListOf<ServiceDue>()
        for (type in types) {
            if (type.defaultIntervalKm == null && type.defaultIntervalMonths == null) continue
            val last = byType[type.id]
                ?.maxWithOrNull(compareBy<ServiceRecord>({ it.dateMillis }, { it.odometerKm }))
                ?: continue

            val nextKm = nextDueKm(last.odometerKm, type.defaultIntervalKm)
            val nextMillis = nextDueMillis(last.dateMillis, type.defaultIntervalMonths, zone)
            val remainingKm = nextKm?.let { it - currentOdometerKm }
            val remainingDays = nextMillis?.let { Math.floorDiv(it - nowMillis, MILLIS_PER_DAY) }

            val overdue = (remainingKm != null && remainingKm <= 0) ||
                (remainingDays != null && remainingDays < 0)
            val soonKm = max(SOON_MIN_KM, (type.defaultIntervalKm ?: 0) / 10)
            val soon = !overdue &&
                ((remainingKm != null && remainingKm <= soonKm) ||
                    (remainingDays != null && remainingDays <= SOON_DAYS))

            result += ServiceDue(
                type = type,
                lastRecord = last,
                nextDueKm = nextKm,
                nextDueMillis = nextMillis,
                remainingKm = remainingKm,
                remainingDays = remainingDays,
                status = when {
                    overdue -> DueStatus.OVERDUE
                    soon -> DueStatus.SOON
                    else -> DueStatus.OK
                }
            )
        }
        return result.sortedWith(
            compareBy<ServiceDue> { it.status.ordinal }
                .thenBy { it.remainingKm ?: Int.MAX_VALUE }
        )
    }
}
