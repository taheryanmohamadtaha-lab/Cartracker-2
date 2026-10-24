package com.cartracker.app.data.seed

import android.content.Context
import androidx.annotation.StringRes
import com.cartracker.app.R
import com.cartracker.app.data.local.dao.ServiceTypeDao
import com.cartracker.app.data.local.entity.ServiceTypeEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** نوع‌های سرویس پیش‌فرض (نام‌ها در strings.xml) با فاصله پیشنهادی بر اساس کیلومتر و ماه. */
@Singleton
class DefaultDataSeeder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val serviceTypeDao: ServiceTypeDao
) {

    private class Seed(@StringRes val nameRes: Int, val km: Int?, val months: Int?)

    private val seeds = listOf(
        Seed(R.string.svc_engine_oil, 5_000, 6),
        Seed(R.string.svc_oil_filter, 5_000, 6),
        Seed(R.string.svc_air_filter, 15_000, 12),
        Seed(R.string.svc_cabin_filter, 15_000, 12),
        Seed(R.string.svc_fuel_filter, 30_000, 24),
        Seed(R.string.svc_gearbox_oil, 60_000, 36),
        Seed(R.string.svc_diff_oil, 60_000, 36),
        Seed(R.string.svc_power_steering_oil, 50_000, 36),
        Seed(R.string.svc_coolant, 40_000, 24),
        Seed(R.string.svc_brake_fluid, 40_000, 24),
        Seed(R.string.svc_washer_fluid, null, null),
        Seed(R.string.svc_brake_pads_front, 30_000, null),
        Seed(R.string.svc_brake_pads_rear, 40_000, null),
        Seed(R.string.svc_brake_discs, 60_000, null),
        Seed(R.string.svc_spark_plugs, 30_000, 24),
        Seed(R.string.svc_timing_belt, 90_000, 60),
        Seed(R.string.svc_alternator_belt, 60_000, 48),
        Seed(R.string.svc_tensioner_bearings, 90_000, 60),
        Seed(R.string.svc_battery, null, 36),
        Seed(R.string.svc_tires_replace, 50_000, 48),
        Seed(R.string.svc_tires_rotate, 10_000, 6),
        Seed(R.string.svc_tires_balance, 10_000, null),
        Seed(R.string.svc_tire_pressure, null, 1),
        Seed(R.string.svc_wipers, null, 12),
        Seed(R.string.svc_shock_absorbers, 80_000, 48),
        Seed(R.string.svc_control_arms, 80_000, 48),
        Seed(R.string.svc_lights_fuses, null, null),
        Seed(R.string.svc_ac_gas, null, 24),
        Seed(R.string.svc_ac_service, null, 12),
        Seed(R.string.svc_wash_wax, null, null)
    )

    /** فقط وقتی جدول خالی است (اولین اجرا) داده پیش‌فرض را می‌ریزد. */
    suspend fun seedIfNeeded() {
        if (serviceTypeDao.count() > 0) return
        serviceTypeDao.insertAll(
            seeds.map { seed ->
                ServiceTypeEntity(
                    name = context.getString(seed.nameRes),
                    iconKey = "build",
                    defaultIntervalKm = seed.km,
                    defaultIntervalMonths = seed.months,
                    isDefault = true
                )
            }
        )
    }
}
