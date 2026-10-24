@file:OptIn(ExperimentalMaterial3Api::class)

package com.cartracker.app.presentation.fuel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.LocalGasStation
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.cartracker.app.domain.calc.FuelSummary
import com.cartracker.app.domain.model.FuelKind
import com.cartracker.app.domain.usecase.FuelEntry
import com.cartracker.app.domain.usecase.FuelOverview
import com.cartracker.app.presentation.common.OverviewUiState
import com.cartracker.app.presentation.common.OverviewViewModel
import com.cartracker.app.presentation.common.labelRes
import com.cartracker.app.presentation.components.AnimeCard
import com.cartracker.app.presentation.components.LoadingView
import com.cartracker.app.presentation.components.MessageCard
import com.cartracker.app.presentation.components.StatTile
import com.cartracker.app.presentation.components.VehicleSwitcher

@Composable
fun FuelScreen(
    onAdd: (vehicleId: Long) -> Unit,
    onEdit: (vehicleId: Long, logId: Long) -> Unit,
    onManageVehicles: () -> Unit,
    onAddVehicle: () -> Unit,
    viewModel: OverviewViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (val current = state) {
        OverviewUiState.Loading -> LoadingView()
        OverviewUiState.Error -> Box(modifier = Modifier.padding(16.dp)) {
            MessageCard(
                icon = Icons.Rounded.Warning,
                title = stringResource(R.string.error_title),
                message = stringResource(R.string.error_generic)
            )
        }
        is OverviewUiState.Content -> FuelContent(
            overview = current.overview,
            onSelectVehicle = viewModel::selectVehicle,
            onDelete = viewModel::deleteLog,
            onAdd = onAdd,
            onEdit = onEdit,
            onManageVehicles = onManageVehicles,
            onAddVehicle = onAddVehicle
        )
    }
}

@Composable
private fun FuelContent(
    overview: FuelOverview,
    onSelectVehicle: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onAdd: (Long) -> Unit,
    onEdit: (Long, Long) -> Unit,
    onManageVehicles: () -> Unit,
    onAddVehicle: () -> Unit
) {
    val active = overview.selection.active
    var pendingDelete by remember { mutableStateOf<FuelEntry?>(null) }

    if (active == null) {
        Box(modifier = Modifier.padding(16.dp)) {
            MessageCard(
                icon = Icons.Rounded.DirectionsCar,
                title = stringResource(R.string.no_vehicle_title),
                message = stringResource(R.string.no_vehicle_message),
                actionLabel = stringResource(R.string.vehicle_add_title),
                onAction = onAddVehicle
            )
        }
        return
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
            item(key = "summary") {
                FuelSummaryCard(summary = overview.summary)
            }
            if (overview.entries.isEmpty()) {
                item(key = "empty") {
                    MessageCard(
                        icon = Icons.Rounded.LocalGasStation,
                        title = stringResource(R.string.fuel_empty_title),
                        message = stringResource(R.string.fuel_empty_message),
                        actionLabel = stringResource(R.string.fuel_add),
                        onAction = { onAdd(active.id) }
                    )
                }
            } else {
                items(overview.entries, key = { it.log.id }) { entry ->
                    FuelEntryCard(
                        entry = entry,
                        showKind = active.fuelType.kinds.size > 1,
                        onClick = { onEdit(active.id, entry.log.id) },
                        onDelete = { pendingDelete = entry },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { onAdd(active.id) },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.fuel_add))
        }
    }

    val toDelete = pendingDelete
    if (toDelete != null) {
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(stringResource(R.string.fuel_delete_title)) },
            text = { Text(stringResource(R.string.fuel_delete_message)) },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(toDelete.log.id)
                    pendingDelete = null
                }) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun FuelSummaryCard(summary: FuelSummary) {
    val formatter = LocalFormatter.current
    val unit = stringResource(R.string.unit_l_per_100km)
    val dash = stringResource(R.string.value_none)

    AnimeCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.fuel_summary_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        if (summary.averageByKind.isEmpty()) {
            StatTile(
                label = stringResource(R.string.stat_average_consumption),
                value = dash
            )
            Text(
                text = stringResource(R.string.fuel_summary_need_two_full),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            FuelKind.entries.forEach { kind ->
                val average = summary.averageByKind[kind] ?: return@forEach
                val label = if (summary.averageByKind.size > 1) {
                    stringResource(R.string.stat_average_consumption) + " (" +
                        stringResource(kind.labelRes()) + ")"
                } else {
                    stringResource(R.string.stat_average_consumption)
                }
                StatTile(label = label, value = formatter.number(average, 1) + " " + unit)
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatTile(
                label = stringResource(R.string.stat_last_consumption),
                value = summary.lastConsumption?.let { formatter.number(it, 1) + " " + unit } ?: dash,
                modifier = Modifier.weight(1f)
            )
            StatTile(
                label = stringResource(R.string.stat_cost_per_km),
                value = summary.averageCostPerKm?.let { formatter.money(it) } ?: dash,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatTile(
                label = stringResource(R.string.stat_total_cost),
                value = formatter.money(summary.totalCost),
                modifier = Modifier.weight(1f)
            )
            StatTile(
                label = stringResource(R.string.stat_total_liters),
                value = formatter.number(summary.totalLiters, 1) + " " + stringResource(R.string.unit_liter),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun FuelEntryCard(
    entry: FuelEntry,
    showKind: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formatter = LocalFormatter.current
    val log = entry.log
    val stats = entry.stats
    val literUnit = stringResource(R.string.unit_liter)
    val kmUnit = stringResource(R.string.unit_km)

    AnimeCard(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formatter.date(log.dateMillis),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                val badges = buildList {
                    add(stringResource(if (log.isFullTank) R.string.fuel_full_tank else R.string.fuel_partial))
                    if (showKind) add(stringResource(log.fuelKind.labelRes()))
                    log.station?.let { add(it) }
                }
                Text(
                    text = badges.joinToString(" · "),
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
                value = formatter.integer(log.odometerKm) + " " + kmUnit,
                modifier = Modifier.weight(1f)
            )
            StatTile(
                label = stringResource(R.string.fuel_liters),
                value = formatter.number(log.liters, 2) + " " + literUnit,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatTile(
                label = stringResource(R.string.fuel_price_per_liter),
                value = formatter.money(log.pricePerLiter),
                modifier = Modifier.weight(1f)
            )
            StatTile(
                label = stringResource(R.string.fuel_total),
                value = formatter.money(log.totalCost),
                modifier = Modifier.weight(1f)
            )
        }
        if (stats.distanceSincePreviousKm != null || stats.consumptionPer100Km != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatTile(
                    label = stringResource(R.string.fuel_distance_since_previous),
                    value = stats.distanceSincePreviousKm?.let { formatter.integer(it) + " " + kmUnit }
                        ?: stringResource(R.string.value_none),
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    label = stringResource(R.string.stat_consumption),
                    value = stats.consumptionPer100Km?.let {
                        formatter.number(it, 1) + " " + stringResource(R.string.unit_l_per_100km)
                    } ?: stringResource(R.string.value_none),
                    modifier = Modifier.weight(1f)
                )
            }
        }
        stats.costPerKm?.let { cost ->
            StatTile(
                label = stringResource(R.string.stat_cost_per_km),
                value = formatter.money(cost)
            )
        }
    }
}
