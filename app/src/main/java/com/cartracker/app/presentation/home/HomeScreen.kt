@file:OptIn(ExperimentalMaterial3Api::class)

package com.cartracker.app.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
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
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.LocalGasStation
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cartracker.app.R
import com.cartracker.app.core.LocalFormatter
import com.cartracker.app.domain.model.FuelKind
import com.cartracker.app.domain.usecase.FuelOverview
import com.cartracker.app.presentation.common.OverviewUiState
import com.cartracker.app.presentation.common.OverviewViewModel
import com.cartracker.app.presentation.common.labelRes
import com.cartracker.app.presentation.components.AnimeCard
import com.cartracker.app.presentation.components.LoadingView
import com.cartracker.app.presentation.components.MessageCard
import com.cartracker.app.presentation.components.StatTile
import com.cartracker.app.presentation.components.VehicleAvatar
import com.cartracker.app.presentation.components.VehicleSwitcher
import com.cartracker.app.presentation.theme.LocalReduceMotion

@Composable
fun HomeScreen(
    onAddFuel: (vehicleId: Long) -> Unit,
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
        is OverviewUiState.Content -> HomeContent(
            overview = current.overview,
            onSelectVehicle = viewModel::selectVehicle,
            onAddFuel = onAddFuel,
            onManageVehicles = onManageVehicles,
            onAddVehicle = onAddVehicle
        )
    }
}

@Composable
private fun HomeContent(
    overview: FuelOverview,
    onSelectVehicle: (Long) -> Unit,
    onAddFuel: (Long) -> Unit,
    onManageVehicles: () -> Unit,
    onAddVehicle: () -> Unit
) {
    val reduceMotion = LocalReduceMotion.current
    var visible by remember { mutableStateOf(reduceMotion) }
    LaunchedEffect(Unit) { visible = true }

    val active = overview.selection.active

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = if (reduceMotion) {
                EnterTransition.None
            } else {
                fadeIn(tween(500)) + scaleIn(tween(500), initialScale = 0.94f)
            }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (active == null) {
                    WelcomeCard(onAddVehicle = onAddVehicle)
                } else {
                    VehicleSwitcher(
                        vehicles = overview.selection.vehicles,
                        activeId = active.id,
                        onSelect = onSelectVehicle,
                        onManage = onManageVehicles
                    )
                    ActiveVehicleCard(overview = overview, onAddFuel = onAddFuel)
                }
                CreatorCard()
            }
        }
    }
}

@Composable
private fun WelcomeCard(onAddVehicle: () -> Unit) {
    AnimeCard(modifier = Modifier.fillMaxWidth()) {
        Icon(
            imageVector = Icons.Rounded.DirectionsCar,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(top = 4.dp)
                .align(Alignment.CenterHorizontally)
        )
        Text(
            text = stringResource(R.string.home_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = stringResource(R.string.home_subtitle),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = stringResource(R.string.home_welcome),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = onAddVehicle,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        ) {
            Text(stringResource(R.string.home_add_first_vehicle))
        }
    }
}

@Composable
private fun ActiveVehicleCard(
    overview: FuelOverview,
    onAddFuel: (Long) -> Unit
) {
    val vehicle = overview.selection.active ?: return
    val formatter = LocalFormatter.current
    val summary = overview.summary
    val unit = stringResource(R.string.unit_l_per_100km)
    val dash = stringResource(R.string.value_none)
    val currentOdometer = overview.entries.maxOfOrNull { it.log.odometerKm } ?: vehicle.initialOdometerKm

    val details = listOf(
        listOf(vehicle.brand, vehicle.model).filter { it.isNotBlank() }.joinToString(" "),
        vehicle.year?.let { formatter.digits(it.toString()) }.orEmpty(),
        vehicle.plate.orEmpty()
    ).filter { it.isNotBlank() }.joinToString(" · ")

    AnimeCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            VehicleAvatar(vehicle = vehicle, size = 64.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = vehicle.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (details.isNotEmpty()) {
                    Text(text = details, style = MaterialTheme.typography.bodySmall)
                }
                Text(
                    text = stringResource(vehicle.fuelType.labelRes()),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatTile(
                label = stringResource(R.string.home_current_odometer),
                value = formatter.integer(currentOdometer) + " " + stringResource(R.string.unit_km),
                modifier = Modifier.weight(1f)
            )
            StatTile(
                label = stringResource(R.string.stat_total_cost),
                value = formatter.money(summary.totalCost),
                modifier = Modifier.weight(1f)
            )
        }
        FuelKind.entries.forEach { kind ->
            val average = summary.averageByKind[kind] ?: return@forEach
            val label = if (summary.averageByKind.size > 1) {
                stringResource(R.string.stat_average_consumption) + " (" + stringResource(kind.labelRes()) + ")"
            } else {
                stringResource(R.string.stat_average_consumption)
            }
            StatTile(label = label, value = formatter.number(average, 1) + " " + unit)
        }
        if (summary.averageByKind.isEmpty()) {
            StatTile(label = stringResource(R.string.stat_average_consumption), value = dash)
            Text(
                text = stringResource(R.string.fuel_summary_need_two_full),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
        Button(
            onClick = { onAddFuel(vehicle.id) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        ) {
            Icon(Icons.Rounded.LocalGasStation, contentDescription = null)
            Text(
                text = stringResource(R.string.fuel_add),
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
private fun CreatorCard() {
    AnimeCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.creator_label) + ": " + stringResource(R.string.creator_name),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
