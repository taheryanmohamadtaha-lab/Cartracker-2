@file:OptIn(ExperimentalMaterial3Api::class)

package com.cartracker.app.presentation.maintenance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cartracker.app.R
import com.cartracker.app.core.LocalFormatter
import com.cartracker.app.domain.calc.DueStatus
import com.cartracker.app.domain.calc.ServiceDue
import com.cartracker.app.domain.model.ExpenseCategory
import com.cartracker.app.domain.model.ExpenseRecord
import com.cartracker.app.domain.usecase.ExpenseSummary
import com.cartracker.app.domain.usecase.MaintenanceOverview
import com.cartracker.app.domain.usecase.ServiceHistoryItem
import com.cartracker.app.presentation.common.labelRes
import com.cartracker.app.presentation.components.AnimeCard
import com.cartracker.app.presentation.components.ConfirmDialog
import com.cartracker.app.presentation.components.LoadingView
import com.cartracker.app.presentation.components.MessageCard
import com.cartracker.app.presentation.components.StatTile
import com.cartracker.app.presentation.components.VehicleSwitcher

private const val SECTION_SERVICES = 0
private const val SECTION_EXPENSES = 1

@Composable
fun MaintenanceScreen(
    onAddService: (vehicleId: Long) -> Unit,
    onEditService: (vehicleId: Long, recordId: Long) -> Unit,
    onAddExpense: (vehicleId: Long) -> Unit,
    onEditExpense: (vehicleId: Long, recordId: Long) -> Unit,
    onManageTypes: () -> Unit,
    onManageVehicles: () -> Unit,
    onAddVehicle: () -> Unit,
    viewModel: MaintenanceViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (val current = state) {
        MaintenanceUiState.Loading -> LoadingView()
        MaintenanceUiState.Error -> Box(modifier = Modifier.padding(16.dp)) {
            MessageCard(
                icon = Icons.Rounded.Warning,
                title = stringResource(R.string.error_title),
                message = stringResource(R.string.error_generic)
            )
        }
        is MaintenanceUiState.Content -> MaintenanceContent(
            overview = current.overview,
            onSelectVehicle = viewModel::selectVehicle,
            onDeleteService = viewModel::deleteService,
            onDeleteExpense = viewModel::deleteExpense,
            onAddService = onAddService,
            onEditService = onEditService,
            onAddExpense = onAddExpense,
            onEditExpense = onEditExpense,
            onManageTypes = onManageTypes,
            onManageVehicles = onManageVehicles,
            onAddVehicle = onAddVehicle
        )
    }
}

@Composable
private fun MaintenanceContent(
    overview: MaintenanceOverview,
    onSelectVehicle: (Long) -> Unit,
    onDeleteService: (Long) -> Unit,
    onDeleteExpense: (Long) -> Unit,
    onAddService: (Long) -> Unit,
    onEditService: (Long, Long) -> Unit,
    onAddExpense: (Long) -> Unit,
    onEditExpense: (Long, Long) -> Unit,
    onManageTypes: () -> Unit,
    onManageVehicles: () -> Unit,
    onAddVehicle: () -> Unit
) {
    var section by rememberSaveable { mutableStateOf(SECTION_SERVICES) }
    var categoryFilter by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingServiceDelete by remember { mutableStateOf<Long?>(null) }
    var pendingExpenseDelete by remember { mutableStateOf<Long?>(null) }

    val active = overview.selection.active
    if (active == null) {
        Box(modifier = Modifier.padding(16.dp)) {
            MessageCard(
                icon = Icons.Rounded.DirectionsCar,
                title = stringResource(R.string.no_vehicle_title),
                message = stringResource(R.string.no_vehicle_message_maintenance),
                actionLabel = stringResource(R.string.vehicle_add_title),
                onAction = onAddVehicle
            )
        }
        return
    }

    val filteredExpenses = overview.expenses.filter {
        categoryFilter == null || it.category.name == categoryFilter
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "switcher") {
                VehicleSwitcher(
                    vehicles = overview.selection.vehicles,
                    activeId = active.id,
                    onSelect = onSelectVehicle,
                    onManage = onManageVehicles
                )
            }
            item(key = "sections") {
                SectionSwitch(section = section, onSelect = { section = it })
            }

            if (section == SECTION_SERVICES) {
                if (overview.dues.isNotEmpty()) {
                    item(key = "dues") { DuesCard(dues = overview.dues) }
                }
                item(key = "types") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.service_history_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = onManageTypes) {
                            Icon(Icons.Rounded.Build, contentDescription = null)
                            Text(
                                text = stringResource(R.string.service_types_manage),
                                modifier = Modifier.padding(start = 6.dp)
                            )
                        }
                    }
                }
                if (overview.history.isEmpty()) {
                    item(key = "service-empty") {
                        MessageCard(
                            icon = Icons.Rounded.Build,
                            title = stringResource(R.string.service_empty_title),
                            message = stringResource(R.string.service_empty_message),
                            actionLabel = stringResource(R.string.service_add),
                            onAction = { onAddService(active.id) }
                        )
                    }
                } else {
                    items(overview.history, key = { "s" + it.record.id }) { item ->
                        ServiceCard(
                            item = item,
                            onClick = { onEditService(active.id, item.record.id) },
                            onDelete = { pendingServiceDelete = item.record.id },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            } else {
                item(key = "expense-summary") {
                    ExpenseSummaryCard(summary = overview.expenseSummary)
                }
                item(key = "expense-filter") {
                    CategoryFilter(
                        selected = categoryFilter,
                        onSelect = { categoryFilter = it },
                        available = overview.expenses.map { it.category }.toSet()
                    )
                }
                if (filteredExpenses.isEmpty()) {
                    item(key = "expense-empty") {
                        MessageCard(
                            icon = Icons.Rounded.Build,
                            title = stringResource(R.string.expense_empty_title),
                            message = stringResource(R.string.expense_empty_message),
                            actionLabel = stringResource(R.string.expense_add),
                            onAction = { onAddExpense(active.id) }
                        )
                    }
                } else {
                    items(filteredExpenses, key = { "e" + it.id }) { expense ->
                        ExpenseCard(
                            expense = expense,
                            onClick = { onEditExpense(active.id, expense.id) },
                            onDelete = { pendingExpenseDelete = expense.id },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                if (section == SECTION_SERVICES) onAddService(active.id) else onAddExpense(active.id)
            },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = stringResource(
                    if (section == SECTION_SERVICES) R.string.service_add else R.string.expense_add
                )
            )
        }
    }

    val serviceToDelete = pendingServiceDelete
    if (serviceToDelete != null) {
        ConfirmDialog(
            title = stringResource(R.string.service_delete_title),
            message = stringResource(R.string.record_delete_message),
            onConfirm = {
                onDeleteService(serviceToDelete)
                pendingServiceDelete = null
            },
            onDismiss = { pendingServiceDelete = null }
        )
    }
    val expenseToDelete = pendingExpenseDelete
    if (expenseToDelete != null) {
        ConfirmDialog(
            title = stringResource(R.string.expense_delete_title),
            message = stringResource(R.string.record_delete_message),
            onConfirm = {
                onDeleteExpense(expenseToDelete)
                pendingExpenseDelete = null
            },
            onDismiss = { pendingExpenseDelete = null }
        )
    }
}

@Composable
private fun SectionSwitch(section: Int, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = section == SECTION_SERVICES,
            onClick = { onSelect(SECTION_SERVICES) },
            label = { Text(stringResource(R.string.section_services)) },
            modifier = Modifier.weight(1f)
        )
        FilterChip(
            selected = section == SECTION_EXPENSES,
            onClick = { onSelect(SECTION_EXPENSES) },
            label = { Text(stringResource(R.string.section_expenses)) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun DuesCard(dues: List<ServiceDue>) {
    val formatter = LocalFormatter.current
    AnimeCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.dues_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        dues.forEach { due ->
            val color = when (due.status) {
                DueStatus.OVERDUE -> MaterialTheme.colorScheme.error
                DueStatus.SOON -> MaterialTheme.colorScheme.tertiary
                DueStatus.OK -> MaterialTheme.colorScheme.primary
            }
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = due.type.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = stringResource(
                            when (due.status) {
                                DueStatus.OVERDUE -> R.string.due_status_overdue
                                DueStatus.SOON -> R.string.due_status_soon
                                DueStatus.OK -> R.string.due_status_ok
                            }
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
                val parts = buildList {
                    due.remainingKm?.let { km ->
                        add(
                            if (km >= 0) {
                                stringResource(R.string.due_remaining_km, formatter.integer(km))
                            } else {
                                stringResource(R.string.due_overdue_km, formatter.integer(-km))
                            }
                        )
                    }
                    due.remainingDays?.let { days ->
                        val abs = Math.abs(days).toInt()
                        add(
                            if (days >= 0) {
                                stringResource(R.string.due_remaining_days, formatter.integer(abs))
                            } else {
                                stringResource(R.string.due_overdue_days, formatter.integer(abs))
                            }
                        )
                    }
                }
                if (parts.isNotEmpty()) {
                    Text(
                        text = parts.joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = color
                    )
                }
            }
        }
    }
}

@Composable
private fun ServiceCard(
    item: ServiceHistoryItem,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formatter = LocalFormatter.current
    val record = item.record
    val unknown = stringResource(R.string.service_type_unknown)

    AnimeCard(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.typeName.ifBlank { unknown },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatter.date(record.dateMillis),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Rounded.Delete, contentDescription = stringResource(R.string.delete))
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatTile(
                label = stringResource(R.string.fuel_odometer),
                value = formatter.integer(record.odometerKm) + " " + stringResource(R.string.unit_km),
                modifier = Modifier.weight(1f)
            )
            StatTile(
                label = stringResource(R.string.cost_label),
                value = formatter.money(record.cost),
                modifier = Modifier.weight(1f)
            )
        }
        record.place?.let { place ->
            Text(text = place, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun ExpenseSummaryCard(summary: ExpenseSummary) {
    val formatter = LocalFormatter.current
    AnimeCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.expense_summary_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatTile(
                label = stringResource(R.string.expense_total),
                value = formatter.money(summary.total),
                modifier = Modifier.weight(1f)
            )
            StatTile(
                label = stringResource(R.string.expense_this_month),
                value = formatter.money(summary.thisMonth),
                modifier = Modifier.weight(1f)
            )
        }
        val ranked = summary.byCategory.entries
            .filter { it.value > 0.0 }
            .sortedByDescending { it.value }
        if (ranked.isNotEmpty() && summary.total > 0.0) {
            ranked.forEach { (category, amount) ->
                Column {
                    Row {
                        Text(
                            text = stringResource(category.labelRes()),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = formatter.money(amount),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    LinearProgressIndicator(
                        progress = { (amount / summary.total).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryFilter(
    selected: String?,
    onSelect: (String?) -> Unit,
    available: Set<ExpenseCategory>
) {
    com.cartracker.app.presentation.components.ChoiceChips(
        items = listOf<ExpenseCategory?>(null) + ExpenseCategory.entries.filter { it in available },
        selected = selected?.let { ExpenseCategory.fromName(it) },
        onSelect = { onSelect(it?.name) },
        label = { category ->
            if (category == null) {
                stringResource(R.string.filter_all)
            } else {
                stringResource(category.labelRes())
            }
        }
    )
}

@Composable
private fun ExpenseCard(
    expense: ExpenseRecord,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formatter = LocalFormatter.current
    val categoryLabel = stringResource(expense.category.labelRes())

    AnimeCard(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (expense.title.isBlank()) categoryLabel else expense.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                val subtitle = buildList {
                    if (expense.title.isNotBlank()) add(categoryLabel)
                    add(formatter.date(expense.dateMillis))
                    expense.tripTag?.let { add("#" + it) }
                }
                Text(
                    text = subtitle.joinToString(" · "),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Rounded.Delete, contentDescription = stringResource(R.string.delete))
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatTile(
                label = stringResource(R.string.amount_label),
                value = formatter.money(expense.amount),
                modifier = Modifier.weight(1f)
            )
            val expiry = expense.expiryMillis
            if (expiry != null) {
                StatTile(
                    label = stringResource(R.string.expiry_label),
                    value = formatter.date(expiry),
                    modifier = Modifier.weight(1f)
                )
            } else if (expense.odometerKm != null) {
                StatTile(
                    label = stringResource(R.string.fuel_odometer),
                    value = formatter.integer(expense.odometerKm) + " " + stringResource(R.string.unit_km),
                    modifier = Modifier.weight(1f)
                )
            }
        }
        val details = expense.details
        val extra = buildList {
            details.repairArea?.let { add(stringResource(it.labelRes())) }
            details.brand?.let { add(it) }
            details.partNumber?.let { add(stringResource(R.string.part_number_short, it)) }
            details.insuranceKind?.let { add(stringResource(it.labelRes())) }
            details.company?.let { add(it) }
        }
        if (extra.isNotEmpty()) {
            Text(text = extra.joinToString(" · "), style = MaterialTheme.typography.bodySmall)
        }
    }
}
