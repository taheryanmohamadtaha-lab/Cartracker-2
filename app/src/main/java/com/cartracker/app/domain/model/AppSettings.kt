package com.cartracker.app.domain.model

enum class ThemeMode { SYSTEM, LIGHT, DARK }

enum class AppThemeId { NEON_CITY, SAKURA, COAST_SUNSET, STARRY_SKY, DREAM_FOREST }

/** واحد نمایش پول. مبالغ همیشه به تومان ذخیره می‌شوند و فقط هنگام نمایش به ریال تبدیل می‌شوند. */
enum class CurrencyUnit { TOMAN, RIAL }

data class AppSettings(
    val themeId: AppThemeId = AppThemeId.NEON_CITY,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val reduceAnimations: Boolean = false,
    val persianDigits: Boolean = true,
    val currency: CurrencyUnit = CurrencyUnit.TOMAN,
    val activeVehicleId: Long? = null
)
