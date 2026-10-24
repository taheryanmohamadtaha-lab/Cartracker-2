@file:OptIn(ExperimentalMaterial3Api::class)

package com.cartracker.app.presentation.components

import android.net.Uri
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.cartracker.app.R
import com.cartracker.app.core.AppFormatter
import com.cartracker.app.core.LocalFormatter

/** دکمه انتخاب تاریخ شمسی (با پنجره انتخاب‌گر). */
@Composable
fun DateField(
    label: String,
    millis: Long,
    onChange: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val formatter = LocalFormatter.current
    var showPicker by remember { mutableStateOf(false) }

    OutlinedButton(onClick = { showPicker = true }, modifier = modifier.fillMaxWidth()) {
        Icon(Icons.Rounded.CalendarMonth, contentDescription = null)
        Text(
            text = label + ": " + formatter.date(millis),
            modifier = Modifier.padding(start = 8.dp)
        )
    }
    if (showPicker) {
        JalaliDatePickerDialog(
            initialMillis = millis,
            onConfirm = {
                onChange(it)
                showPicker = false
            },
            onDismiss = { showPicker = false }
        )
    }
}

/** فیلد مبلغ با واحد پول انتخاب‌شده و نمایش عدد با جداکننده هزارگان. */
@Composable
fun MoneyField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorText: String? = null
) {
    val formatter = LocalFormatter.current
    val parsed = AppFormatter.parseDouble(value)
    LabeledTextField(
        value = value,
        onValueChange = { text -> onChange(text.filter { c -> c.isDigit() || c in ".٫/,٬،" }) },
        label = label,
        modifier = modifier,
        keyboardType = KeyboardType.Decimal,
        suffix = formatter.currencyLabel,
        isError = isError,
        supporting = when {
            isError -> errorText
            parsed != null && parsed > 0.0 -> formatter.number(parsed, 0) + " " + formatter.currencyLabel
            else -> null
        }
    )
}

/** ردیف افقی چیپ‌ها برای انتخاب یک مورد. */
@Composable
fun <T> ChoiceChips(
    items: List<T>,
    selected: T?,
    onSelect: (T) -> Unit,
    label: @Composable (T) -> String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { item ->
            FilterChip(
                selected = item == selected,
                onClick = { onSelect(item) },
                label = { Text(label(item)) }
            )
        }
    }
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(R.string.delete)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

/** عکس فاکتور: افزودن از گالری، دیدن بزرگ‌شده با لمس و حذف. */
@Composable
fun AttachmentsSection(
    paths: List<String>,
    onAdd: (Uri) -> Unit,
    onRemove: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) onAdd(uri)
    }
    var preview by remember { mutableStateOf<String?>(null) }

    androidx.compose.foundation.layout.Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.attachments_title),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            paths.forEach { path ->
                AttachmentThumb(
                    path = path,
                    onOpen = { preview = path },
                    onRemove = { onRemove(path) }
                )
            }
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                    .clickable {
                        picker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.AddPhotoAlternate,
                    contentDescription = stringResource(R.string.attachments_add),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    val current = preview
    if (current != null) {
        Dialog(onDismissRequest = { preview = null }) {
            Surface(shape = MaterialTheme.shapes.large) {
                val bitmap = rememberFileBitmap(current, 1280)
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = stringResource(R.string.attachments_title),
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    LoadingView(modifier = Modifier.size(120.dp))
                }
            }
        }
    }
}

@Composable
private fun AttachmentThumb(path: String, onOpen: () -> Unit, onRemove: () -> Unit) {
    val bitmap = rememberFileBitmap(path, 256)
    Box(modifier = Modifier.size(72.dp)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(onClick = onOpen)
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(26.dp)
                .background(MaterialTheme.colorScheme.surface, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = stringResource(R.string.delete),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
