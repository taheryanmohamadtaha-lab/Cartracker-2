package com.cartracker.app.data.repository

import com.cartracker.app.data.local.dao.FuelLogDao
import com.cartracker.app.data.local.dao.VehicleDao
import com.cartracker.app.data.mapper.toDomain
import com.cartracker.app.data.mapper.toEntity
import com.cartracker.app.domain.model.FuelLog
import com.cartracker.app.domain.model.Vehicle
import com.cartracker.app.domain.repository.FuelLogRepository
import com.cartracker.app.domain.repository.VehicleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VehicleRepositoryImpl @Inject constructor(
    private val dao: VehicleDao
) : VehicleRepository {

    override fun observeAll(): Flow<List<Vehicle>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: Long): Vehicle? = dao.getById(id)?.toDomain()

    override suspend fun save(vehicle: Vehicle): Long =
        if (vehicle.id == 0L) {
            dao.insert(vehicle.toEntity())
        } else {
            dao.update(vehicle.toEntity())
            vehicle.id
        }

    override suspend fun delete(id: Long) {
        dao.deleteById(id)
    }
}

@Singleton
class FuelLogRepositoryImpl @Inject constructor(
    private val dao: FuelLogDao
) : FuelLogRepository {

    override fun observeByVehicle(vehicleId: Long): Flow<List<FuelLog>> =
        dao.observeByVehicle(vehicleId).map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: Long): FuelLog? = dao.getById(id)?.toDomain()

    override suspend fun save(log: FuelLog): Long =
        if (log.id == 0L) {
            dao.insert(log.toEntity())
        } else {
            dao.update(log.toEntity())
            log.id
        }

    override suspend fun delete(id: Long) {
        dao.deleteById(id)
    }
}
