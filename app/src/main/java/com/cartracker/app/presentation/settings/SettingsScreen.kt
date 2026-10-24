@file:OptIn(ExperimentalMaterial3Api::class)

package com.cartracker.app.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cartracker.app.R
import com.cartracker.app.domain.model.AppSettings
import com.cartracker.app.domain.model.AppThemeId
import com.cartracker.app.domain.model.CurrencyUnit
import com.cartracker.app.domain.model.ThemeMode
import com.cartracker.app.presentation.components.AnimeCard
import com.cartracker.app.presentation.theme.AnimeBackground
import com.cartracker.app.presentation.theme.AppThemes
import com.cartracker.app.presentation.theme.LocalActiveTheme
import com.cartracker.app.presentation.theme.labelRes

@Composable
fun SettingsScreen(
    settings: AppSettings,
    actions: SettingsActions
) {
    val isDark = LocalActiveTheme.current.variant.isDark

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // حالت روز / شب
        AnimeCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.settings_mode_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeMode.entries.forEach { mode ->
                    FilterChip(
                        selected = settings.themeMode == mode,
                        onClick = { actions.onModeSelected(mode) },
                        label = { Text(stringResource(mode.labelRes())) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // انتخاب تم با پیش‌نمایش زنده
        AnimeCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.settings_theme_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            AppThemeId.entries.forEach { id ->
                ThemePreviewCard(
                    id = id,
                    selected = settings.themeId == id,
                    dark = isDark,
                    onClick = { actions.onThemeSelected(id) }
                )
            }
        }

        // اعداد و واحد پول
        AnimeCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.settings_digits_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = settings.persianDigits,
                    onClick = { actions.onPersianDigitsChanged(true) },
                    label = { Text(stringResource(R.string.digits_persian)) },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = !settings.persianDigits,
                    onClick = { actions.onPersianDigitsChanged(false) },
                    label = { Text(stringResource(R.string.digits_english)) },
                    modifier = Modifier.weight(1f)
                )
            }
            Text(
                text = stringResource(R.string.settings_currency_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = settings.currency == CurrencyUnit.TOMAN,
                    onClick = { actions.onCurrencySelected(CurrencyUnit.TOMAN) },
                    label = { Text(stringResource(R.string.currency_toman)) },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = settings.currency == CurrencyUnit.RIAL,
                    onClick = { actions.onCurrencySelected(CurrencyUnit.RIAL) },
                    label = { Text(stringResource(R.string.currency_rial)) },
                    modifier = Modifier.weight(1f)
                )
            }
            Text(
                text = stringResource(R.string.settings_units_note),
                style = MaterialTheme.typography.bodySmall
            )
        }

        // کاهش انیمیشن
        AnimeCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.settings_reduce_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.settings_reduce_desc),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Switch(
                    checked = settings.reduceAnimations,
                    onCheckedChange = actions.onReduceAnimationsChanged
                )
            }
        }

        // درباره
        AnimeCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.settings_about_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.app_name) + " - " +
                    stringResource(R.string.about_version),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = stringResource(R.string.creator_label) + ": " +
                    stringResource(R.string.creator_name),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ThemePreviewCard(
    id: AppThemeId,
    selected: Boolean,
    dark: Boolean,
    onClick: () -> Unit
) {
    val variant = remember(id, dark) { AppThemes.variant(id, dark) }
    val scheme = variant.colorScheme
    val shape = RoundedCornerShape(20.dp)
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(shape)
            .border(if (selected) 3.dp else 1.dp, borderColor, shape)
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
    ) {
        // پیش‌نمایش ثابت (بدون انیمیشن) تا باتری مصرف نشود
        AnimeBackground(themeId = id, variant = variant, animated = false)

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(scheme.surface.copy(alpha = 0.9f))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(id.labelRes()),
                color = scheme.onSurface,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            listOf(scheme.primary, scheme.secondary, scheme.tertiary).forEach { color ->
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .background(color, CircleShape)
                )
            }
            if (selected) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = stringResource(R.string.selected_desc),
                    tint = scheme.primary
                )
            }
        }
    }
}

/** همه رویدادهای صفحه تنظیمات در یک جا. */
@Immutable
class SettingsActions(
    val onThemeSelected: (AppThemeId) -> Unit,
    val onModeSelected: (ThemeMode) -> Unit,
    val onReduceAnimationsChanged: (Boolean) -> Unit,
    val onPersianDigitsChanged: (Boolean) -> Unit,
    val onCurrencySelected: (CurrencyUnit) -> Unit
)
