package com.cartracker.app.di

import com.cartracker.app.data.repository.FuelLogRepositoryImpl
import com.cartracker.app.data.repository.VehicleRepositoryImpl
import com.cartracker.app.data.settings.DataStoreSettingsRepository
import com.cartracker.app.domain.repository.FuelLogRepository
import com.cartracker.app.domain.repository.SettingsRepository
import com.cartracker.app.domain.repository.VehicleRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsModule {

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: DataStoreSettingsRepository): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindVehicleRepository(impl: VehicleRepositoryImpl): VehicleRepository

    @Binds
    @Singleton
    abstract fun bindFuelLogRepository(impl: FuelLogRepositoryImpl): FuelLogRepository
}
