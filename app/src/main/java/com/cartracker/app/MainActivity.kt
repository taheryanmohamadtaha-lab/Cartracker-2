package com.cartracker.app

import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cartracker.app.presentation.AppViewModel
import com.cartracker.app.presentation.navigation.AppRoot
import com.cartracker.app.presentation.settings.SettingsActions
import com.cartracker.app.presentation.theme.CarTrackerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // اگر کاربر انیمیشن‌های سیستم را خاموش کرده باشد، انیمیشن‌های برنامه هم ساده می‌شوند.
        val systemAnimationsOff = Settings.Global.getFloat(
            contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        ) == 0f

        setContent {
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            val current = settings
            val actions = remember(viewModel) {
                SettingsActions(
                    onThemeSelected = viewModel::setTheme,
                    onModeSelected = viewModel::setThemeMode,
                    onReduceAnimationsChanged = viewModel::setReduceAnimations,
                    onPersianDigitsChanged = viewModel::setPersianDigits,
                    onCurrencySelected = viewModel::setCurrency
                )
            }
            if (current == null) {
                // تا خواندن تنظیمات از دیتاستور (چند میلی‌ثانیه) فقط یک پس‌زمینه ساده
                Box(modifier = Modifier.fillMaxSize().background(Color(0xFF14101F)))
            } else {
                CarTrackerTheme(
                    themeId = current.themeId,
                    mode = current.themeMode,
                    reduceMotion = current.reduceAnimations || systemAnimationsOff
                ) {
                    AppRoot(settings = current, actions = actions)
                }
            }
        }
    }
}
