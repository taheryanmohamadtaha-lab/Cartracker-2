package com.cartracker.app

import com.cartracker.app.domain.calc.DueStatus
import com.cartracker.app.domain.calc.ServiceScheduler
import com.cartracker.app.domain.model.ServiceRecord
import com.cartracker.app.domain.model.ServiceType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class ServiceSchedulerTest {

    private val zone = ZoneId.of("UTC")
    private fun millis(d: LocalDate) = d.atTime(12, 0).atZone(zone).toInstant().toEpochMilli()

    private val oil = ServiceType(
        id = 1, name = "oil", defaultIntervalKm = 5000, defaultIntervalMonths = 6
    )

    private fun record(km: Int, date: LocalDate) = ServiceRecord(
        id = 1, vehicleId = 1, serviceTypeId = 1, dateMillis = millis(date),
        odometerKm = km, cost = 0.0, place = null, note = null
    )

    @Test
    fun nextDueKm_addsInterval() {
        assertEquals(15000, ServiceScheduler.nextDueKm(10000, 5000))
        assertNull(ServiceScheduler.nextDueKm(10000, null))
    }

    @Test
    fun nextDueMillis_addsMonths() {
        val next = ServiceScheduler.nextDueMillis(millis(LocalDate.of(2026, 1, 31)), 1, zone)
        assertEquals(millis(LocalDate.of(2026, 2, 28)), next)
    }

    @Test
    fun status_okSoonOverdue() {
        val now = millis(LocalDate.of(2026, 2, 1))
        val rec = record(10000, LocalDate.of(2026, 1, 1))
        fun status(odo: Int) =
            ServiceScheduler.computeDues(listOf(oil), listOf(rec), odo, now, zone).single().status
        assertEquals(DueStatus.OK, status(11000))
        assertEquals(DueStatus.SOON, status(14600))
        assertEquals(DueStatus.OVERDUE, status(15000))
    }

    @Test
    fun overdueByDate() {
        val now = millis(LocalDate.of(2027, 1, 1))
        val rec = record(10000, LocalDate.of(2026, 1, 1))
        val due = ServiceScheduler.computeDues(listOf(oil), listOf(rec), 10500, now, zone).single()
        assertEquals(DueStatus.OVERDUE, due.status)
    }

    @Test
    fun currentOdometer_isMax() {
        assertEquals(900, ServiceScheduler.currentOdometer(100, listOf(300), listOf(900), listOf(500)))
    }
}
