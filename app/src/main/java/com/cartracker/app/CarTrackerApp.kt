package com.cartracker.app

import android.app.Application
import com.cartracker.app.data.seed.DefaultDataSeeder
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class CarTrackerApp : Application() {

    @Inject
    lateinit var seeder: DefaultDataSeeder

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        appScope.launch { runCatching { seeder.seedIfNeeded() } }
    }
}
