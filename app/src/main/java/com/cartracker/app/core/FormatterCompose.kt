package com.cartracker.app.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import com.cartracker.app.R
import com.cartracker.app.domain.model.CurrencyUnit

val LocalFormatter = staticCompositionLocalOf { AppFormatter() }

@Composable
fun rememberAppFormatter(persianDigits: Boolean, currency: CurrencyUnit): AppFormatter {
    val toman = stringResource(R.string.currency_toman)
    val rial = stringResource(R.string.currency_rial)
    val months = stringArrayResource(R.array.jalali_months).toList()
    return remember(persianDigits, currency, toman, rial, months) {
        AppFormatter(
            persianDigits = persianDigits,
            currency = currency,
            tomanLabel = toman,
            rialLabel = rial,
            monthNames = months
        )
    }
}
