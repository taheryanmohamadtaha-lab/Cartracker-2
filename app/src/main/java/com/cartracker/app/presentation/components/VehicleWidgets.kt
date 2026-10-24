@file:OptIn(ExperimentalMaterial3Api::class)

package com.cartracker.app.presentation.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cartracker.app.R
import com.cartracker.app.domain.model.Vehicle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private fun decodeSampledBitmap(path: String, maxSize: Int): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(path, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
    var sample = 1
    while (bounds.outWidth / (sample * 2) >= maxSize && bounds.outHeight / (sample * 2) >= maxSize) {
        sample *= 2
    }
    val options = BitmapFactory.Options().apply { inSampleSize = sample }
    return BitmapFactory.decodeFile(path, options)
}

/** عکس را در پس‌زمینه می‌خواند تا رابط کاربری کند نشود. */
@Composable
fun rememberFileBitmap(path: String?, maxSize: Int = 512): ImageBitmap? {
    val state = produceState<ImageBitmap?>(initialValue = null, path, maxSize) {
        value = if (path == null) {
            null
        } else {
            withContext(Dispatchers.IO) {
                runCatching { decodeSampledBitmap(path, maxSize)?.asImageBitmap() }.getOrNull()
            }
        }
    }
    return state.value
}

fun vehicleColor(vehicle: Vehicle): Color = Color(vehicle.colorArgb.toInt())

/** دایره عکس خودرو؛ اگر عکسی نباشد، آیکون ماشین به رنگ خودرو. */
@Composable
fun VehicleAvatar(vehicle: Vehicle, size: Dp, modifier: Modifier = Modifier) {
    val bitmap = rememberFileBitmap(vehicle.photoPath, 256)
    val color = vehicleColor(vehicle)
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.25f))
            .border(2.dp, color, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Rounded.DirectionsCar,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(size * 0.55f)
            )
        }
    }
}

/** سوئیچ سریع بین خودروها (چیپ‌های افقی) + دکمه مدیریت خودروها. */
@Composable
fun VehicleSwitcher(
    vehicles: List<Vehicle>,
    activeId: Long?,
    onSelect: (Long) -> Unit,
    onManage: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            vehicles.forEach { vehicle ->
                FilterChip(
                    selected = vehicle.id == activeId,
                    onClick = { onSelect(vehicle.id) },
                    label = { Text(vehicle.name) },
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(vehicleColor(vehicle), CircleShape)
                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
            AssistChip(
                onClick = onManage,
                label = { Text(stringResource(R.string.vehicles_manage)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.DirectionsCar,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }
    }
}
