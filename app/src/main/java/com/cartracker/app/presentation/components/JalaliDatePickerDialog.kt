package com.cartracker.app.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cartracker.app.R
import com.cartracker.app.core.JalaliCalendar
import com.cartracker.app.core.JalaliDate
import com.cartracker.app.core.LocalFormatter

private const val MIN_YEAR = 1300
private const val MAX_YEAR = 1500

/** انتخاب‌گر تاریخ شمسی با سه چرخ بالا/پایین برای روز، ماه و سال. */
@Composable
fun JalaliDatePickerDialog(
    initialMillis: Long,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val formatter = LocalFormatter.current
    val monthNames = stringArrayResource(R.array.jalali_months)
    val initial = remember(initialMillis) { JalaliDate.fromEpochMillis(initialMillis) }
    var year by remember { mutableIntStateOf(initial.year.coerceIn(MIN_YEAR, MAX_YEAR)) }
    var month by remember { mutableIntStateOf(initial.month) }
    var day by remember { mutableIntStateOf(initial.day) }

    val maxDay = JalaliCalendar.daysInMonth(year, month)
    val shownDay = day.coerceAtMost(maxDay)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.date_picker_title)) },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // در چیدمان راست‌به‌چپ، اولین ستون سمت راست است: روز، ماه، سال
                Stepper(
                    label = stringResource(R.string.date_day),
                    value = formatter.digits(shownDay.toString()),
                    onUp = { day = if (shownDay >= maxDay) 1 else shownDay + 1 },
                    onDown = { day = if (shownDay <= 1) maxDay else shownDay - 1 }
                )
                Stepper(
                    label = stringResource(R.string.date_month),
                    value = monthNames[month - 1],
                    onUp = { month = if (month >= 12) 1 else month + 1 },
                    onDown = { month = if (month <= 1) 12 else month - 1 }
                )
                Stepper(
                    label = stringResource(R.string.date_year),
                    value = formatter.digits(year.toString()),
                    onUp = { if (year < MAX_YEAR) year += 1 },
                    onDown = { if (year > MIN_YEAR) year -= 1 }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(JalaliDate(year, month, shownDay).toEpochMillis()) }) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = {
                    val today = JalaliDate.today()
                    year = today.year
                    month = today.month
                    day = today.day
                }) {
                    Text(stringResource(R.string.date_today))
                }
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }
            }
        }
    )
}

@Composable
private fun Stepper(
    label: String,
    value: String,
    onUp: () -> Unit,
    onDown: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelMedium)
        IconButton(onClick = onUp) {
            Icon(Icons.Rounded.KeyboardArrowUp, contentDescription = stringResource(R.string.date_increase))
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        IconButton(onClick = onDown) {
            Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = stringResource(R.string.date_decrease))
        }
    }
}
