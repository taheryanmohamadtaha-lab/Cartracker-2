@file:OptIn(ExperimentalMaterial3Api::class)

package com.cartracker.app.presentation.maintenance

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.cartracker.app.domain.calc.ServiceScheduler
import com.cartracker.app.domain.model.ServiceType
import com.cartracker.app.presentation.components.AnimeCard
import com.cartracker.app.presentation.components.AttachmentsSection
import com.cartracker.app.presentation.components.DateField
import com.cartracker.app.presentation.components.LabeledTextField
import com.cartracker.app.presentation.components.LoadingView
import com.cartracker.app.presentation.components.MessageCard
import com.cartracker.app.presentation.components.MoneyField
import com.cartracker.app.presentation.components.ScreenHeader

@Composable
fun ServiceEditScreen(
    onDone: () -> Unit,
    onManageTypes: () -> Unit,
    viewModel: ServiceEditViewModel = hiltViewModel()
) {
    val form by viewModel.form.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.done.collect { onDone() } }

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(
            title = stringResource(if (form.isNew) R.string.service_add else R.string.service_edit),
            onBack = onDone
        )
        when {
            form.isLoading -> LoadingView()
            form.notFound -> Box(modifier = Modifier.padding(16.dp)) {
                MessageCard(
                    icon = Icons.Rounded.Warning,
                    title = stringResource(R.string.error_title),
                    message = stringResource(R.string.record_not_found)
                )
            }
            else -> ServiceForm(form = form, viewModel = viewModel, onManageTypes = onManageTypes)
        }
    }
}

@Composable
private fun ServiceForm(
    form: ServiceFormState,
    viewModel: ServiceEditViewModel,
    onManageTypes: () -> Unit
) {
    val formatter = LocalFormatter.current
    var showTypePicker by remember { mutableStateOf(false) }
    val selectedType = form.selectedType

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AnimeCard(modifier = Modifier.fillMaxWidth()) {
            form.vehicle?.let {
                Text(
                    text = it.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
            }
            OutlinedButton(
                onClick = { showTypePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(selectedType?.name ?: stringResource(R.string.service_choose_type))
            }
            if (form.typeError) {
                Text(
                    text = stringResource(R.string.error_choose_type),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            DateField(
                label = stringResource(R.string.fuel_date),
                millis = form.dateMillis,
                onChange = { millis -> viewModel.update { it.copy(dateMillis = millis) } }
            )

            LabeledTextField(
                value = form.odometer,
                onValueChange = { v ->
                    viewModel.update { it.copy(odometer = v.filter { c -> c.isDigit() }, odometerError = false) }
                },
                label = stringResource(R.string.fuel_odometer),
                keyboardType = KeyboardType.Number,
                suffix = stringResource(R.string.unit_km),
                isError = form.odometerError,
                supporting = when {
                    form.odometerError -> stringResource(R.string.error_invalid_number)
                    form.currentOdometerKm != null -> stringResource(
                        R.string.current_odometer_hint,
                        formatter.integer(form.currentOdometerKm)
                    )
                    else -> null
                }
            )

            MoneyField(
                value = form.cost,
                onChange = { v -> viewModel.update { it.copy(cost = v, costError = false) } },
                label = stringResource(R.string.cost_label),
                isError = form.costError,
                errorText = stringResource(R.string.error_invalid_number)
            )

            LabeledTextField(
                value = form.place,
                onValueChange = { v -> viewModel.update { it.copy(place = v) } },
                label = stringResource(R.string.service_place)
            )
            LabeledTextField(
                value = form.note,
                onValueChange = { v -> viewModel.update { it.copy(note = v) } },
                label = stringResource(R.string.fuel_note),
                imeAction = ImeAction.Done
            )
        }

        NextDueCard(form = form)

        AnimeCard(modifier = Modifier.fillMaxWidth()) {
            AttachmentsSection(
                paths = form.attachments,
                onAdd = viewModel::onPhotoPicked,
                onRemove = viewModel::removeAttachment
            )
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

    if (showTypePicker) {
        TypePickerDialog(
            types = form.types,
            onSelect = { type ->
                viewModel.onTypeSelected(type)
                showTypePicker = false
            },
            onManage = {
                showTypePicker = false
                onManageTypes()
            },
            onDismiss = { showTypePicker = false }
        )
    }
}

/** سررسید پیشنهادی سرویس بعدی بر اساس نوع سرویس و کیلومتر و تاریخ واردشده. */
@Composable
private fun NextDueCard(form: ServiceFormState) {
    val type = form.selectedType ?: return
    val odometer = AppFormatter.parseInt(form.odometer)
    val nextKm = if (odometer != null) ServiceScheduler.nextDueKm(odometer, type.defaultIntervalKm) else null
    val nextMillis = ServiceScheduler.nextDueMillis(form.dateMillis, type.defaultIntervalMonths)
    if (nextKm == null && nextMillis == null) return

    val formatter = LocalFormatter.current
    AnimeCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.service_next_due_title),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        if (nextKm != null) {
            Text(
                text = stringResource(
                    R.string.service_next_due_km,
                    formatter.integer(nextKm),
                    stringResource(R.string.unit_km)
                ),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        if (nextMillis != null) {
            Text(
                text = stringResource(R.string.service_next_due_date, formatter.date(nextMillis)),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Text(
            text = stringResource(R.string.service_next_due_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TypePickerDialog(
    types: List<ServiceType>,
    onSelect: (ServiceType) -> Unit,
    onManage: () -> Unit,
    onDismiss: () -> Unit
) {
    val formatter = LocalFormatter.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.service_choose_type)) },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = 380.dp)) {
                items(types, key = { it.id }) { type ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(type) }
                            .padding(vertical = 10.dp)
                    ) {
                        Text(text = type.name, style = MaterialTheme.typography.bodyLarge)
                        val interval = intervalText(type, formatter)
                        if (interval.isNotEmpty()) {
                            Text(
                                text = interval,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onManage) {
                Icon(Icons.Rounded.Build, contentDescription = null)
                Text(
                    text = stringResource(R.string.service_types_manage),
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

/** مثل «هر ۵٬۰۰۰ کیلومتر · هر ۶ ماه». */
@Composable
fun intervalText(type: ServiceType, formatter: AppFormatter): String {
    val parts = buildList {
        type.defaultIntervalKm?.let {
            add(stringResource(R.string.interval_km, formatter.integer(it)))
        }
        type.defaultIntervalMonths?.let {
            add(stringResource(R.string.interval_months, formatter.integer(it)))
        }
    }
    return parts.joinToString(" · ")
}
