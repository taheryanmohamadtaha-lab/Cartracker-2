@file:OptIn(ExperimentalMaterial3Api::class)

package com.cartracker.app.presentation.vehicles

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
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cartracker.app.R
import com.cartracker.app.core.LocalFormatter
import com.cartracker.app.domain.model.Vehicle
import com.cartracker.app.presentation.common.labelRes
import com.cartracker.app.presentation.components.AnimeCard
import com.cartracker.app.presentation.components.LoadingView
import com.cartracker.app.presentation.components.MessageCard
import com.cartracker.app.presentation.components.ScreenHeader
import com.cartracker.app.presentation.components.VehicleAvatar

@Composable
fun VehiclesScreen(
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (Long) -> Unit,
    viewModel: VehiclesViewModel = hiltViewModel()
) {
    val selection by viewModel.selection.collectAsStateWithLifecycle()
    val current = selection

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            ScreenHeader(title = stringResource(R.string.vehicles_title), onBack = onBack)
            when {
                current == null -> LoadingView()
                current.vehicles.isEmpty() -> Box(modifier = Modifier.padding(16.dp)) {
                    MessageCard(
                        icon = Icons.Rounded.DirectionsCar,
                        title = stringResource(R.string.vehicles_empty_title),
                        message = stringResource(R.string.vehicles_empty_message),
                        actionLabel = stringResource(R.string.vehicle_add_title),
                        onAction = onAdd
                    )
                }
                else -> LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(current.vehicles, key = { it.id }) { vehicle ->
                        VehicleRow(
                            vehicle = vehicle,
                            isActive = vehicle.id == current.active?.id,
                            onSelect = { viewModel.selectVehicle(vehicle.id) },
                            onEdit = { onEdit(vehicle.id) }
                        )
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = onAdd,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.vehicle_add_title))
        }
    }
}

@Composable
private fun VehicleRow(
    vehicle: Vehicle,
    isActive: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit
) {
    val formatter = LocalFormatter.current
    val details = listOf(
        listOf(vehicle.brand, vehicle.model).filter { it.isNotBlank() }.joinToString(" "),
        vehicle.year?.let { formatter.digits(it.toString()) }.orEmpty(),
        vehicle.plate.orEmpty()
    ).filter { it.isNotBlank() }.joinToString(" · ")

    AnimeCard(modifier = Modifier.fillMaxWidth(), onClick = onSelect) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            VehicleAvatar(vehicle = vehicle, size = 56.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = vehicle.name,
                    style = MaterialTheme.typography.titleMedium,
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
            if (isActive) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = stringResource(R.string.vehicle_active),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Rounded.Edit, contentDescription = stringResource(R.string.edit))
            }
        }
    }
}
