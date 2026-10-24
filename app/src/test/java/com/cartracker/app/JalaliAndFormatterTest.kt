package com.cartracker.app

import com.cartracker.app.core.AppFormatter
import com.cartracker.app.core.JalaliCalendar
import com.cartracker.app.domain.model.CurrencyUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class JalaliAndFormatterTest {

    @Test
    fun gregorianToJalaliKnownDates() {
        assertEquals(Triple(1405, 7, 29), JalaliCalendar.toJalali(2026, 10, 21))
        assertEquals(Triple(1405, 1, 1), JalaliCalendar.toJalali(2026, 3, 21))
        assertEquals(Triple(1404, 1, 1), JalaliCalendar.toJalali(2025, 3, 21))
        assertEquals(Triple(1403, 1, 1), JalaliCalendar.toJalali(2024, 3, 20))
    }

    @Test
    fun jalaliToGregorianRoundTrip() {
        assertEquals(Triple(2026, 10, 21), JalaliCalendar.toGregorian(1405, 7, 29))
        assertEquals(Triple(2025, 3, 21), JalaliCalendar.toGregorian(1404, 1, 1))
    }

    @Test
    fun leapYears() {
        assertTrue(JalaliCalendar.isLeap(1403))
        assertFalse(JalaliCalendar.isLeap(1404))
        assertEquals(30, JalaliCalendar.daysInMonth(1403, 12))
        assertEquals(29, JalaliCalendar.daysInMonth(1404, 12))
        assertEquals(31, JalaliCalendar.daysInMonth(1404, 6))
        assertEquals(30, JalaliCalendar.daysInMonth(1404, 7))
    }

    @Test
    fun parsesPersianInput() {
        assertEquals(1234.5, AppFormatter.parseDouble("۱٬۲۳۴٫۵")!!, 1e-9)
        assertEquals(12.5, AppFormatter.parseDouble("12/5")!!, 1e-9)
        assertEquals(45000, AppFormatter.parseInt("۴۵۰۰۰"))
        assertNull(AppFormatter.parseDouble(""))
        assertNull(AppFormatter.parseDouble("1.2.3"))
    }

    @Test
    fun formatsNumbersAndMoney() {
        val persian = AppFormatter(persianDigits = true, tomanLabel = "تومان")
        assertEquals("۱٬۲۳۴٬۵۶۷", persian.number(1234567.0, 0))
        assertEquals("۷٫۵", persian.number(7.5, 1))

        val english = AppFormatter(
            persianDigits = false,
            currency = CurrencyUnit.RIAL,
            rialLabel = "ریال"
        )
        assertEquals("1,234,567", english.number(1234567.0, 0))
        assertEquals("10,000 ریال", english.money(1000.0))
        assertEquals(100.0, english.fromDisplayAmount(1000.0), 1e-9)
    }
}
