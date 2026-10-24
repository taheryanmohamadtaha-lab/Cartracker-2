package com.cartracker.app.core

import androidx.compose.runtime.Immutable
import com.cartracker.app.domain.model.CurrencyUnit
import java.util.Locale
import kotlin.math.abs

/**
 * قالب‌بندی اعداد، پول و تاریخ طبق تنظیمات کاربر (اعداد فارسی/انگلیسی، تومان/ریال، تاریخ شمسی).
 * مبلغ‌ها همیشه به تومان ذخیره می‌شوند؛ تبدیل به ریال فقط هنگام نمایش و ورودی انجام می‌شود.
 */
@Immutable
class AppFormatter(
    val persianDigits: Boolean = true,
    val currency: CurrencyUnit = CurrencyUnit.TOMAN,
    private val tomanLabel: String = "",
    private val rialLabel: String = "",
    private val monthNames: List<String> = emptyList()
) {
    private val thousandsSeparator: Char = if (persianDigits) '٬' else ','
    private val decimalSeparator: Char = if (persianDigits) '٫' else '.'

    val currencyLabel: String
        get() = if (currency == CurrencyUnit.TOMAN) tomanLabel else rialLabel

    fun toDisplayAmount(toman: Double): Double =
        if (currency == CurrencyUnit.RIAL) toman * 10.0 else toman

    fun fromDisplayAmount(display: Double): Double =
        if (currency == CurrencyUnit.RIAL) display / 10.0 else display

    /** ارقام را طبق تنظیمات فارسی یا انگلیسی می‌کند (فقط ارقام لاتین ورودی را تبدیل می‌کند). */
    fun digits(text: String): String {
        if (!persianDigits) return text
        return buildString(text.length) {
            for (c in text) append(if (c in '0'..'9') '۰' + (c - '0') else c)
        }
    }

    /** عدد ساده بدون جداکننده هزارگان، مناسب نمایش داخل فیلد ورودی. */
    fun plain(value: Double, maxDecimals: Int = 2): String = digits(trimmed(value, maxDecimals))

    /** عدد با جداکننده هزارگان. */
    fun number(value: Double, maxDecimals: Int = 1): String {
        val text = trimmed(abs(value), maxDecimals)
        val integerPart = text.substringBefore('.')
        val fraction = if (text.contains('.')) text.substringAfter('.') else ""
        val grouped = integerPart.reversed().chunked(3)
            .joinToString(thousandsSeparator.toString())
            .reversed()
        val body = if (fraction.isEmpty()) grouped else grouped + decimalSeparator + fraction
        val signed = if (value < 0 && body != "0") "-$body" else body
        return digits(signed)
    }

    fun integer(value: Int): String = number(value.toDouble(), 0)

    /** مبلغ (که به تومان ذخیره شده) همراه با واحد انتخاب‌شده. */
    fun money(toman: Double): String =
        (number(toDisplayAmount(toman), 0) + " " + currencyLabel).trim()

    /** تاریخ شمسی مثل «۲۹ مهر ۱۴۰۵». */
    fun date(millis: Long): String {
        val d = JalaliDate.fromEpochMillis(millis)
        val monthName = monthNames.getOrNull(d.month - 1)
        return if (monthName == null) {
            digits("${d.year}/${d.month}/${d.day}")
        } else {
            digits(d.day.toString()) + " " + monthName + " " + digits(d.year.toString())
        }
    }

    private fun trimmed(value: Double, maxDecimals: Int): String {
        var text = String.format(Locale.US, "%.${maxDecimals}f", value)
        if (text.contains('.')) text = text.trimEnd('0').trimEnd('.')
        if (text == "-0") text = "0"
        return text
    }

    companion object {
        /** ارقام فارسی و عربی را به لاتین تبدیل می‌کند. */
        fun normalizeDigits(text: String): String = buildString(text.length) {
            for (c in text) {
                append(
                    when (c) {
                        in '۰'..'۹' -> '0' + (c - '۰')
                        in '٠'..'٩' -> '0' + (c - '٠')
                        else -> c
                    }
                )
            }
        }

        /** عدد اعشاری را از متن کاربر می‌خواند (ارقام فارسی، «٫» و «/» به‌عنوان ممیز، جداکننده هزارگان نادیده گرفته می‌شود). */
        fun parseDouble(text: String): Double? {
            val cleaned = buildString {
                for (c in normalizeDigits(text.trim())) {
                    when (c) {
                        in '0'..'9', '.' -> append(c)
                        '٫', '/' -> append('.')
                        else -> Unit
                    }
                }
            }
            if (cleaned.isEmpty() || cleaned.count { it == '.' } > 1) return null
            return cleaned.toDoubleOrNull()
        }

        fun parseInt(text: String): Int? {
            val value = parseDouble(text) ?: return null
            if (value % 1.0 != 0.0 || value > Int.MAX_VALUE) return null
            return value.toInt()
        }
    }
}
