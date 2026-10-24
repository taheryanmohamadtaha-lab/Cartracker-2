@file:OptIn(ExperimentalMaterial3Api::class)

package com.cartracker.app.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** کارت گرد و نیمه‌شفاف با سایه نرم؛ روی پس‌زمینه تم خوانا می‌ماند. اگر onClick بدهید کارت قابل‌لمس می‌شود. */
@Composable
fun AnimeCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = MaterialTheme.shapes.large
    val color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
    val border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
    val inner: @Composable () -> Unit = {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content
        )
    }
    if (onClick == null) {
        Surface(
            modifier = modifier,
            shape = shape,
            color = color,
            shadowElevation = 8.dp,
            border = border,
            content = inner
        )
    } else {
        Surface(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            color = color,
            shadowElevation = 8.dp,
            border = border,
            content = inner
        )
    }
}
