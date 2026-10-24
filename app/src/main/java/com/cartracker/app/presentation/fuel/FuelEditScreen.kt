@file:OptIn(ExperimentalMaterial3Api::class)

package com.cartracker.app.presentation.fuel

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cartracker.app.R
import com.cartracker.app.core.AppFormatter
import com.cartracker.app.core.LocalFormatter
import com.cartracker.app.presentation.common.labelRes
import com.cartracker.app.presentation.components.AnimeCard
import com.cartracker.app.presentation.components.FuelSuccessOverlay
import com.cartracker.app.presentation.components.JalaliDatePickerDialog
import com.cartracker.app.presentation.components.LabeledTextField
import com.cartracker.app.presentation.components.LoadingView
import com.cartracker.app.presentation.components.MessageCard
import com.cartracker.app.presentation.components.ScreenHeader
import com.cartracker.app.presentation.theme.LocalReduceMotion

@Composable
fun FuelEditScreen(
    onDone: () -> Unit,
    viewModel: FuelEditViewModel = hiltViewModel()
) {
    val form by viewModel.form.collectAsStateWithLifecycle()
    val reduceMotion = LocalReduceMotion.current
    var showSuccess by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    // بعد از ذخیره: انیمیشن پر شدن باک (یا خروج مستقیم اگر «کاهش انیمیشن» روشن است)
    LaunchedEffect(Unit) {
        viewModel.done.collect {
            if (reduceMotion) onDone() else showSuccess = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            ScreenHeader(
                title = stringResource(if (form.isNew) R.string.fuel_add else R.string.fuel_edit),
                onBack = onDone
            )
            when {
                form.isLoading -> LoadingView()
                form.notFound -> Box(modifier = Modifier.padding(16.dp)) {
                    MessageCard(
                        icon = Icons.Rounded.Warning,
                        title = stringResource(R.string.error_title),
                        message = stringResource(R.string.fuel_not_found)
                    )
                }
                else -> FuelForm(
                    form = form,
                    viewModel = viewModel,
                    onPickDate = { showDatePicker = true }
                )
            }
        }

        if (showSuccess) {
            FuelSuccessOverlay(onFinished = onDone)
        }
    }

    if (showDatePicker) {
        JalaliDatePickerDialog(
            initialMillis = form.dateMillis,
            onConfirm = { millis ->
                viewModel.onDateChange(millis)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@Composable
private fun FuelForm(
    form: FuelFormState,
    viewModel: FuelEditViewModel,
    onPickDate: () -> Unit
) {
    val formatter = LocalFormatter.current
    val vehicle = form.vehicle ?: return
    val kinds = vehicle.fuelType.kinds
    val currencyLabel = formatter.currencyLabel
    val totalValue = AppFormatter.parseDouble(form.total)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AnimeCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = vehicle.name,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold
            )

            OutlinedButton(onClick = onPickDate, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Rounded.CalendarMonth, contentDescription = null)
                Text(
                    text = stringResource(R.string.fuel_date) + ": " + formatter.date(form.dateMillis),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            LabeledTextField(
                value = form.odometer,
                onValueChange = viewModel::onOdometerChange,
                label = stringResource(R.string.fuel_odometer),
                keyboardType = KeyboardType.Number,
                suffix = stringResource(R.string.unit_km),
                isError = form.odometerError,
                supporting = when {
                    form.odometerError -> stringResource(R.string.error_invalid_number)
                    form.lastOdometerKm != null -> stringResource(
                        R.string.fuel_last_odometer,
                        formatter.integer(form.lastOdometerKm)
                    )
                    else -> null
                }
            )

            if (kinds.size > 1) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    kinds.forEach { kind ->
                        FilterChip(
                            selected = form.fuelKind == kind,
                            onClick = { viewModel.onKindChange(kind) },
                            label = { Text(stringResource(kind.labelRes())) }
                        )
                    }
                }
            }
        }

        AnimeCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.fuel_amounts_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LabeledTextField(
                value = form.liters,
                onValueChange = { viewModel.onAmountChange(AmountField.LITERS, it) },
                label = stringResource(R.string.fuel_liters),
                keyboardType = KeyboardType.Decimal,
                suffix = stringResource(R.string.unit_liter),
                isError = form.amountError
            )
            LabeledTextField(
                value = form.price,
                onValueChange = { viewModel.onAmountChange(AmountField.PRICE, it) },
                label = stringResource(R.string.fuel_price_per_liter),
                keyboardType = KeyboardType.Decimal,
                suffix = currencyLabel,
                isError = form.amountError
            )
            LabeledTextField(
                value = form.total,
                onValueChange = { viewModel.onAmountChange(AmountField.TOTAL, it) },
                label = stringResource(R.string.fuel_total),
                keyboardType = KeyboardType.Decimal,
                suffix = currencyLabel,
                isError = form.amountError,
                supporting = when {
                    form.amountError -> stringResource(R.string.error_amounts)
                    totalValue != null && totalValue > 0.0 ->
                        formatter.number(totalValue, 0) + " " + currencyLabel
                    else -> null
                }
            )
        }

        AnimeCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.fuel_full_tank),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.fuel_full_tank_hint),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Switch(checked = form.isFullTank, onCheckedChange = viewModel::onFullTankChange)
            }
            LabeledTextField(
                value = form.station,
                onValueChange = viewModel::onStationChange,
                label = stringResource(R.string.fuel_station)
            )
            LabeledTextField(
                value = form.note,
                onValueChange = viewModel::onNoteChange,
                label = stringResource(R.string.fuel_note),
                imeAction = ImeAction.Done
            )
        }

        if (form.warnings.isNotEmpty()) {
            AnimeCard(modifier = Modifier.fillMaxWidth()) {
                form.warnings.forEach { warning ->
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Text(
                            text = warningText(warning, formatter),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        if (form.saveFailed) {
            Text(
                text = stringResource(R.string.error_save_failed),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Button(
            onClick = viewModel::save,
            enabled = !form.isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text(stringResource(R.string.save))
        }
    }
}

@Composable
private fun warningText(warning: FuelWarning, formatter: AppFormatter): String {
    val value = formatter.integer(warning.value)
    return when (warning.type) {
        FuelWarning.Type.BELOW_PREVIOUS -> stringResource(R.string.fuel_warn_below_previous, value)
        FuelWarning.Type.ABOVE_NEXT -> stringResource(R.string.fuel_warn_above_next, value)
        FuelWarning.Type.BELOW_INITIAL -> stringResource(R.string.fuel_warn_below_initial, value)
        FuelWarning.Type.OVER_TANK -> stringResource(R.string.fuel_warn_over_tank, value)
    }
}
