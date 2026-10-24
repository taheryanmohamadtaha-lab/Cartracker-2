package com.cartracker.app.domain.repository

import com.cartracker.app.domain.model.FuelLog
import com.cartracker.app.domain.model.Vehicle
import kotlinx.coroutines.flow.Flow

interface VehicleRepository {
    fun observeAll(): Flow<List<Vehicle>>
    suspend fun getById(id: Long): Vehicle?

    /** اگر id صفر باشد ثبت جدید و در غیر این صورت ویرایش است؛ id نهایی را برمی‌گرداند. */
    suspend fun save(vehicle: Vehicle): Long
    suspend fun delete(id: Long)
}

interface FuelLogRepository {
    fun observeByVehicle(vehicleId: Long): Flow<List<FuelLog>>
    suspend fun getById(id: Long): FuelLog?
    suspend fun save(log: FuelLog): Long
    suspend fun delete(id: Long)
}
