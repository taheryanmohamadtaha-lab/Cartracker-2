@file:OptIn(ExperimentalMaterial3Api::class)

package com.cartracker.app.presentation.maintenance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cartracker.app.R
import com.cartracker.app.core.LocalFormatter
import com.cartracker.app.domain.model.ServiceType
import com.cartracker.app.presentation.components.AnimeCard
import com.cartracker.app.presentation.components.ConfirmDialog
import com.cartracker.app.presentation.components.LabeledTextField
import com.cartracker.app.presentation.components.LoadingView
import com.cartracker.app.presentation.components.ScreenHeader

@Composable
fun ServiceTypesScreen(
    onBack: () -> Unit,
    viewModel: ServiceTypesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val formatter = LocalFormatter.current
    var toDelete by remember { mutableStateOf<ServiceType?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(title = stringResource(R.string.service_types_title), onBack = onBack)
        androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
            if (state.isLoading) {
                LoadingView()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            text = stringResource(R.string.service_types_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    items(state.types, key = { it.id }) { type ->
                        AnimeCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = type.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    val interval = intervalText(type, formatter)
                                    Text(
                                        text = interval.ifEmpty { stringResource(R.string.interval_none) },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { viewModel.openEdit(type) }) {
                                    Icon(Icons.Rounded.Edit, contentDescription = stringResource(R.string.edit))
                                }
                                IconButton(onClick = { toDelete = type }) {
                                    Icon(Icons.Rounded.Delete, contentDescription = stringResource(R.string.delete))
                                }
                            }
                        }
                    }
                }
            }
            FloatingActionButton(
                onClick = viewModel::openNew,
                modifier = Modifier
                    .align(androidx.compose.ui.Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.service_type_add))
            }
        }
    }

    state.dialog?.let { dialog ->
        TypeDialog(
            dialog = dialog,
            onChange = viewModel::updateDialog,
            onSave = viewModel::saveDialog,
            onDismiss = viewModel::dismissDialog
        )
    }

    toDelete?.let { type ->
        ConfirmDialog(
            title = stringResource(R.string.service_type_delete_title),
            message = stringResource(R.string.service_type_delete_message, type.name),
            onConfirm = {
                viewModel.delete(type.id)
                toDelete = null
            },
            onDismiss = { toDelete = null }
        )
    }
}

@Composable
private fun TypeDialog(
    dialog: TypeDialogState,
    onChange: ((TypeDialogState) -> TypeDialogState) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(if (dialog.id == 0L) R.string.service_type_add else R.string.service_type_edit))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                LabeledTextField(
                    value = dialog.name,
                    onValueChange = { v -> onChange { it.copy(name = v, nameError = false, duplicateError = false) } },
                    label = stringResource(R.string.service_type_name),
                    isError = dialog.nameError || dialog.duplicateError,
                    supporting = when {
                        dialog.nameError -> stringResource(R.string.error_required)
                        dialog.duplicateError -> stringResource(R.string.error_duplicate_name)
                        else -> null
                    }
                )
                LabeledTextField(
                    value = dialog.km,
                    onValueChange = { v -> onChange { it.copy(km = v.filter { c -> c.isDigit() }, numberError = false) } },
                    label = stringResource(R.string.interval_km_label),
                    keyboardType = KeyboardType.Number,
                    suffix = stringResource(R.string.unit_km),
                    isError = dialog.numberError,
                    supporting = if (dialog.numberError) stringResource(R.string.error_invalid_number) else null
                )
                LabeledTextField(
                    value = dialog.months,
                    onValueChange = { v -> onChange { it.copy(months = v.filter { c -> c.isDigit() }, numberError = false) } },
                    label = stringResource(R.string.interval_months_label),
                    keyboardType = KeyboardType.Number,
                    isError = dialog.numberError
                )
            }
        },
        confirmButton = { TextButton(onClick = onSave) { Text(stringResource(R.string.save)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}
