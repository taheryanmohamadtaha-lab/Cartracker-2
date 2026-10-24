@file:OptIn(ExperimentalMaterial3Api::class)

package com.cartracker.app.presentation.maintenance

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
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cartracker.app.R
import com.cartracker.app.core.LocalFormatter
import com.cartracker.app.domain.model.ExpenseCategory
import com.cartracker.app.domain.model.InsuranceKind
import com.cartracker.app.domain.model.RepairArea
import com.cartracker.app.presentation.common.labelRes
import com.cartracker.app.presentation.components.AnimeCard
import com.cartracker.app.presentation.components.AttachmentsSection
import com.cartracker.app.presentation.components.ChoiceChips
import com.cartracker.app.presentation.components.DateField
import com.cartracker.app.presentation.components.LabeledTextField
import com.cartracker.app.presentation.components.LoadingView
import com.cartracker.app.presentation.components.MessageCard
import com.cartracker.app.presentation.components.MoneyField
import com.cartracker.app.presentation.components.ScreenHeader

@Composable
fun ExpenseEditScreen(
    onDone: () -> Unit,
    viewModel: ExpenseEditViewModel = hiltViewModel()
) {
    val form by viewModel.form.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.done.collect { onDone() } }

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(
            title = stringResource(if (form.isNew) R.string.expense_add else R.string.expense_edit),
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
            else -> ExpenseForm(form = form, viewModel = viewModel)
        }
    }
}

@Composable
private fun ExpenseForm(form: ExpenseFormState, viewModel: ExpenseEditViewModel) {
    val formatter = LocalFormatter.current
    val category = form.category

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
            Text(
                text = stringResource(R.string.expense_category),
                style = MaterialTheme.typography.labelLarge
            )
            ChoiceChips(
                items = ExpenseCategory.values().toList(),
                selected = category,
                onSelect = viewModel::onCategoryChange,
                label = { stringResource(it.labelRes()) }
            )

            LabeledTextField(
                value = form.title,
                onValueChange = { v -> viewModel.update { it.copy(title = v, titleError = false) } },
                label = stringResource(
                    if (category.requiresTitle()) R.string.expense_title_required else R.string.expense_title_optional
                ),
                isError = form.titleError,
                supporting = if (form.titleError) stringResource(R.string.error_required) else null
            )

            DateField(
                label = stringResource(R.string.fuel_date),
                millis = form.dateMillis,
                onChange = { millis -> viewModel.update { it.copy(dateMillis = millis) } }
            )

            MoneyField(
                value = form.amount,
                onChange = { v -> viewModel.update { it.copy(amount = v, amountError = false) } },
                label = stringResource(R.string.amount_label),
                isError = form.amountError,
                errorText = stringResource(R.string.error_invalid_number)
            )

            LabeledTextField(
                value = form.odometer,
                onValueChange = { v ->
                    viewModel.update { it.copy(odometer = v.filter { c -> c.isDigit() }, odometerError = false) }
                },
                label = stringResource(R.string.odometer_optional),
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
        }

        CategoryDetails(form = form, viewModel = viewModel)

        AnimeCard(modifier = Modifier.fillMaxWidth()) {
            LabeledTextField(
                value = form.tripTag,
                onValueChange = { v -> viewModel.update { it.copy(tripTag = v) } },
                label = stringResource(R.string.trip_tag_label)
            )
            if (form.tripTags.isNotEmpty()) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    form.tripTags.take(8).forEach { tag ->
                        AssistChip(
                            onClick = { viewModel.update { it.copy(tripTag = tag) } },
                            label = { Text(tag) }
                        )
                    }
                }
            }
            LabeledTextField(
                value = form.note,
                onValueChange = { v -> viewModel.update { it.copy(note = v) } },
                label = stringResource(R.string.fuel_note),
                imeAction = ImeAction.Done
            )
        }

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
}

/** فیلدهای ویژهٔ هر دسته (تعمیر، قطعه، بیمه، معاینه فنی). */
@Composable
private fun CategoryDetails(form: ExpenseFormState, viewModel: ExpenseEditViewModel) {
    val category = form.category
    if (category != ExpenseCategory.REPAIR && category != ExpenseCategory.PART && !category.hasExpiry()) return

    AnimeCard(modifier = Modifier.fillMaxWidth()) {
        when (category) {
            ExpenseCategory.REPAIR -> {
                Text(
                    text = stringResource(R.string.repair_area_label),
                    style = MaterialTheme.typography.labelLarge
                )
                ChoiceChips(
                    items = RepairArea.values().toList(),
                    selected = form.repairArea,
                    onSelect = { area -> viewModel.update { it.copy(repairArea = area) } },
                    label = { stringResource(it.labelRes()) }
                )
            }
            ExpenseCategory.PART -> {
                LabeledTextField(
                    value = form.brand,
                    onValueChange = { v -> viewModel.update { it.copy(brand = v) } },
                    label = stringResource(R.string.part_brand)
                )
                LabeledTextField(
                    value = form.partNumber,
                    onValueChange = { v -> viewModel.update { it.copy(partNumber = v) } },
                    label = stringResource(R.string.part_number)
                )
                LabeledTextField(
                    value = form.warrantyMonths,
                    onValueChange = { v ->
                        viewModel.update { it.copy(warrantyMonths = v.filter { c -> c.isDigit() }, numberError = false) }
                    },
                    label = stringResource(R.string.part_warranty_months),
                    keyboardType = KeyboardType.Number,
                    isError = form.numberError,
                    supporting = if (form.numberError) stringResource(R.string.error_invalid_number) else null
                )
            }
            ExpenseCategory.INSURANCE -> {
                ChoiceChips(
                    items = InsuranceKind.values().toList(),
                    selected = form.insuranceKind,
                    onSelect = { kind -> viewModel.update { it.copy(insuranceKind = kind) } },
                    label = { stringResource(it.labelRes()) }
                )
                LabeledTextField(
                    value = form.company,
                    onValueChange = { v -> viewModel.update { it.copy(company = v) } },
                    label = stringResource(R.string.insurance_company)
                )
                LabeledTextField(
                    value = form.policyNumber,
                    onValueChange = { v -> viewModel.update { it.copy(policyNumber = v) } },
                    label = stringResource(R.string.insurance_policy_number)
                )
                DateField(
                    label = stringResource(R.string.insurance_start),
                    millis = form.startMillis,
                    onChange = { millis -> viewModel.update { it.copy(startMillis = millis) } }
                )
                ExpiryField(form = form, viewModel = viewModel, labelRes = R.string.insurance_end)
                LabeledTextField(
                    value = form.discount,
                    onValueChange = { v ->
                        viewModel.update { it.copy(discount = v.filter { c -> c.isDigit() }, numberError = false) }
                    },
                    label = stringResource(R.string.insurance_discount),
                    keyboardType = KeyboardType.Number,
                    suffix = "٪",
                    isError = form.numberError,
                    supporting = if (form.numberError) stringResource(R.string.error_percent_range) else null
                )
            }
            ExpenseCategory.INSPECTION -> {
                ExpiryField(form = form, viewModel = viewModel, labelRes = R.string.inspection_expiry)
            }
            else -> Unit
        }
    }
}

@Composable
private fun ExpiryField(form: ExpenseFormState, viewModel: ExpenseEditViewModel, labelRes: Int) {
    val expiry = form.expiryMillis ?: form.dateMillis
    DateField(
        label = stringResource(labelRes),
        millis = expiry,
        onChange = { millis -> viewModel.update { it.copy(expiryMillis = millis, expiryError = false) } }
    )
    if (form.expiryError) {
        Text(
            text = stringResource(R.string.error_required),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
