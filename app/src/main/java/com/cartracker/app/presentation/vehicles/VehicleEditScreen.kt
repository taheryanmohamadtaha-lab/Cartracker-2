@file:OptIn(ExperimentalMaterial3Api::class)

package com.cartracker.app.presentation.vehicles

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cartracker.app.R
import com.cartracker.app.domain.model.FuelType
import com.cartracker.app.domain.model.VehicleColors
import com.cartracker.app.presentation.common.labelRes
import com.cartracker.app.presentation.components.AnimeCard
import com.cartracker.app.presentation.components.LabeledTextField
import com.cartracker.app.presentation.components.LoadingView
import com.cartracker.app.presentation.components.ScreenHeader
import com.cartracker.app.presentation.components.rememberFileBitmap

@Composable
fun VehicleEditScreen(
    onDone: () -> Unit,
    viewModel: VehicleEditViewModel = hiltViewModel()
) {
    val form by viewModel.form.collectAsStateWithLifecycle()
    var confirmDelete by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.done.collect { onDone() } }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) viewModel.onPhotoPicked(uri)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(
            title = stringResource(if (form.isNew) R.string.vehicle_add_title else R.string.vehicle_edit_title),
            onBack = onDone,
            actions = {
                if (!form.isNew) {
                    IconButton(onClick = { confirmDelete = true }) {
                        Icon(Icons.Rounded.Delete, contentDescription = stringResource(R.string.delete))
                    }
                }
            }
        )

        if (form.isLoading) {
            LoadingView()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AnimeCard(modifier = Modifier.fillMaxWidth()) {
                    PhotoPicker(
                        photoPath = form.photoPath,
                        color = Color(form.colorArgb.toInt()),
                        onPick = {
                            photoPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onRemove = viewModel::removePhoto
                    )
                    LabeledTextField(
                        value = form.name,
                        onValueChange = { v -> viewModel.update { it.copy(name = v, nameError = false) } },
                        label = stringResource(R.string.vehicle_name),
                        isError = form.nameError,
                        supporting = if (form.nameError) stringResource(R.string.error_required) else null
                    )
                    LabeledTextField(
                        value = form.brand,
                        onValueChange = { v -> viewModel.update { it.copy(brand = v) } },
                        label = stringResource(R.string.vehicle_brand)
                    )
                    LabeledTextField(
                        value = form.model,
                        onValueChange = { v -> viewModel.update { it.copy(model = v) } },
                        label = stringResource(R.string.vehicle_model)
                    )
                    LabeledTextField(
                        value = form.year,
                        onValueChange = { v ->
                            viewModel.update { it.copy(year = v.filter { c -> c.isDigit() }, yearError = false) }
                        },
                        label = stringResource(R.string.vehicle_year),
                        keyboardType = KeyboardType.Number,
                        isError = form.yearError,
                        supporting = if (form.yearError) stringResource(R.string.error_invalid_year) else null
                    )
                    LabeledTextField(
                        value = form.plate,
                        onValueChange = { v -> viewModel.update { it.copy(plate = v) } },
                        label = stringResource(R.string.vehicle_plate)
                    )
                }

                AnimeCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.vehicle_fuel_type),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FuelType.entries.forEach { type ->
                            FilterChip(
                                selected = form.fuelType == type,
                                onClick = { viewModel.update { it.copy(fuelType = type) } },
                                label = { Text(stringResource(type.labelRes())) }
                            )
                        }
                    }
                    LabeledTextField(
                        value = form.tank,
                        onValueChange = { v ->
                            viewModel.update {
                                it.copy(tank = v.filter { c -> c.isDigit() || c in ".٫/" }, tankError = false)
                            }
                        },
                        label = stringResource(R.string.vehicle_tank),
                        keyboardType = KeyboardType.Decimal,
                        suffix = stringResource(R.string.unit_liter),
                        isError = form.tankError,
                        supporting = if (form.tankError) stringResource(R.string.error_invalid_number) else null
                    )
                    LabeledTextField(
                        value = form.odometer,
                        onValueChange = { v ->
                            viewModel.update { it.copy(odometer = v.filter { c -> c.isDigit() }, odometerError = false) }
                        },
                        label = stringResource(R.string.vehicle_initial_odometer),
                        keyboardType = KeyboardType.Number,
                        suffix = stringResource(R.string.unit_km),
                        isError = form.odometerError,
                        supporting = if (form.odometerError) stringResource(R.string.error_invalid_number) else null,
                        imeAction = ImeAction.Done
                    )
                }

                AnimeCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.vehicle_color),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        VehicleColors.palette.forEach { argb ->
                            ColorDot(
                                color = Color(argb.toInt()),
                                selected = form.colorArgb == argb,
                                onClick = { viewModel.update { it.copy(colorArgb = argb) } }
                            )
                        }
                    }
                }

                Button(
                    onClick = viewModel::save,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text(stringResource(R.string.save))
                }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.vehicle_delete_title)) },
            text = { Text(stringResource(R.string.vehicle_delete_message)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    viewModel.delete()
                }) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun PhotoPicker(
    photoPath: String?,
    color: Color,
    onPick: () -> Unit,
    onRemove: () -> Unit
) {
    val bitmap = rememberFileBitmap(photoPath, 512)
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(112.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.2f))
                .border(3.dp, color, CircleShape)
                .clickable(onClick = onPick),
            contentAlignment = Alignment.Center
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = stringResource(R.string.vehicle_photo),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.AddPhotoAlternate,
                    contentDescription = stringResource(R.string.vehicle_photo_add),
                    tint = color,
                    modifier = Modifier.size(44.dp)
                )
            }
        }
        if (photoPath != null) {
            TextButton(onClick = onRemove) { Text(stringResource(R.string.vehicle_photo_remove)) }
        } else {
            Text(
                text = stringResource(R.string.vehicle_photo_add),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun ColorDot(color: Color, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (selected) 3.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = stringResource(R.string.selected_desc),
                tint = if (color.luminance() > 0.6f) Color.Black else Color.White
            )
        }
    }
}

private fun Color.luminance(): Float = 0.2126f * red + 0.7152f * green + 0.0722f * blue
