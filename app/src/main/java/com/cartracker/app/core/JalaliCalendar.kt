package com.cartracker.app.core

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class JalaliDate(val year: Int, val month: Int, val day: Int) {

    fun toLocalDate(): LocalDate {
        val (gy, gm, gd) = JalaliCalendar.toGregorian(year, month, day)
        return LocalDate.of(gy, gm, gd)
    }

    /** تاریخ را ظهر همان روز ذخیره می‌کنیم تا جابه‌جایی ساعت یا منطقه زمانی روز را عوض نکند. */
    fun toEpochMillis(zone: ZoneId = ZoneId.systemDefault()): Long =
        toLocalDate().atTime(12, 0).atZone(zone).toInstant().toEpochMilli()

    companion object {
        fun fromLocalDate(date: LocalDate): JalaliDate {
            val (jy, jm, jd) = JalaliCalendar.toJalali(date.year, date.monthValue, date.dayOfMonth)
            return JalaliDate(jy, jm, jd)
        }

        fun fromEpochMillis(millis: Long, zone: ZoneId = ZoneId.systemDefault()): JalaliDate =
            fromLocalDate(Instant.ofEpochMilli(millis).atZone(zone).toLocalDate())

        fun today(): JalaliDate = fromLocalDate(LocalDate.now())
    }
}

/** تبدیل بین تقویم میلادی و شمسی (الگوریتم حسابی؛ برای سال‌های ۱۳۰۰ تا ۱۵۰۰ بررسی شده). */
object JalaliCalendar {

    fun toJalali(gy: Int, gm: Int, gd: Int): Triple<Int, Int, Int> {
        val offsets = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        val gy2 = if (gm > 2) gy + 1 else gy
        var days = 355666 + (365 * gy) + ((gy2 + 3) / 4) - ((gy2 + 99) / 100) +
            ((gy2 + 399) / 400) + gd + offsets[gm - 1]
        var jy = -1595 + (33 * (days / 12053))
        days %= 12053
        jy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            jy += (days - 1) / 365
            days = (days - 1) % 365
        }
        val jm: Int
        val jd: Int
        if (days < 186) {
            jm = 1 + days / 31
            jd = 1 + days % 31
        } else {
            jm = 7 + (days - 186) / 30
            jd = 1 + (days - 186) % 30
        }
        return Triple(jy, jm, jd)
    }

    fun toGregorian(jy: Int, jm: Int, jd: Int): Triple<Int, Int, Int> {
        val jy2 = jy + 1595
        var days = -355668 + (365 * jy2) + ((jy2 / 33) * 8) + (((jy2 % 33) + 3) / 4) + jd +
            (if (jm < 7) (jm - 1) * 31 else ((jm - 7) * 30) + 186)
        var gy = 400 * (days / 146097)
        days %= 146097
        if (days > 36524) {
            days--
            gy += 100 * (days / 36524)
            days %= 36524
            if (days >= 365) days++
        }
        gy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            gy += (days - 1) / 365
            days = (days - 1) % 365
        }
        var gd = days + 1
        val leap = (gy % 4 == 0 && gy % 100 != 0) || gy % 400 == 0
        val monthLengths = intArrayOf(0, 31, if (leap) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        var gm = 0
        while (gm < 13 && gd > monthLengths[gm]) {
            gd -= monthLengths[gm]
            gm++
        }
        return Triple(gy, gm, gd)
    }

    fun isLeap(year: Int): Boolean {
        val (gy, gm, gd) = toGregorian(year, 12, 30)
        return toJalali(gy, gm, gd) == Triple(year, 12, 30)
    }

    fun daysInMonth(year: Int, month: Int): Int = when {
        month <= 6 -> 31
        month <= 11 -> 30
        else -> if (isLeap(year)) 30 else 29
    }
}
