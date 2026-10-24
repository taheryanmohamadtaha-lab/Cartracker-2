package com.cartracker.app

import com.cartracker.app.domain.calc.FuelCalculator
import com.cartracker.app.domain.model.FuelKind
import com.cartracker.app.domain.model.FuelLog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class FuelCalculatorTest {

    private fun log(
        id: Long,
        odometer: Int,
        liters: Double,
        full: Boolean = true,
        kind: FuelKind = FuelKind.PETROL,
        price: Double = 1000.0
    ) = FuelLog(
        id = id,
        vehicleId = 1,
        dateMillis = id * 1_000L,
        odometerKm = odometer,
        liters = liters,
        pricePerLiter = price,
        totalCost = liters * price,
        fuelKind = kind,
        station = null,
        isFullTank = full,
        note = null
    )

    @Test
    fun firstFullTankHasNoConsumption() {
        val analysis = FuelCalculator.analyze(listOf(log(1, 1000, 40.0)))
        assertNull(analysis.entries.getValue(1).consumptionPer100Km)
        assertEquals(0, analysis.summary.averageByKind.size)
    }

    @Test
    fun twoFullTanksGiveConsumption() {
        val analysis = FuelCalculator.analyze(listOf(log(1, 1000, 40.0), log(2, 1500, 30.0)))
        assertEquals(6.0, analysis.entries.getValue(2).consumptionPer100Km!!, 1e-9)
        assertEquals(6.0, analysis.summary.averageByKind.getValue(FuelKind.PETROL), 1e-9)
        assertEquals(500, analysis.entries.getValue(2).distanceSincePreviousKm)
    }

    @Test
    fun partialFillBetweenFullTanksIsIncluded() {
        val analysis = FuelCalculator.analyze(
            listOf(
                log(1, 1000, 40.0),
                log(2, 1200, 10.0, full = false),
                log(3, 1500, 20.0)
            )
        )
        // (10 + 20) لیتر در ۵۰۰ کیلومتر
        assertEquals(6.0, analysis.entries.getValue(3).consumptionPer100Km!!, 1e-9)
        assertNull(analysis.entries.getValue(2).consumptionPer100Km)
    }

    @Test
    fun orderDoesNotDependOnInputOrder() {
        val analysis = FuelCalculator.analyze(listOf(log(2, 1500, 30.0), log(1, 1000, 40.0)))
        assertNotNull(analysis.entries.getValue(2).consumptionPer100Km)
        assertEquals(6.0, analysis.summary.lastConsumption!!, 1e-9)
    }

    @Test
    fun biFuelKindsAreSeparate() {
        val analysis = FuelCalculator.analyze(
            listOf(
                log(1, 1000, 40.0, kind = FuelKind.PETROL),
                log(2, 1100, 10.0, kind = FuelKind.CNG),
                log(3, 1500, 30.0, kind = FuelKind.PETROL),
                log(4, 1700, 20.0, kind = FuelKind.CNG)
            )
        )
        assertEquals(6.0, analysis.summary.averageByKind.getValue(FuelKind.PETROL), 1e-9)
        assertEquals(20.0 / 600.0 * 100.0, analysis.summary.averageByKind.getValue(FuelKind.CNG), 1e-9)
    }

    @Test
    fun averageCostPerKmUsesWholeSpan() {
        val analysis = FuelCalculator.analyze(listOf(log(1, 1000, 40.0), log(2, 1500, 30.0)))
        // هزینه بعد از اولین ثبت: ۳۰ لیتر × ۱۰۰۰ در ۵۰۰ کیلومتر
        assertEquals(60.0, analysis.summary.averageCostPerKm!!, 1e-9)
    }
}
