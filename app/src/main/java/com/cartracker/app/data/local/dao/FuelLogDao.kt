package com.cartracker.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.cartracker.app.data.local.entity.FuelLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FuelLogDao {

    @Query("SELECT * FROM fuel_log WHERE vehicleId = :vehicleId ORDER BY odometerKm DESC, dateMillis DESC")
    fun observeByVehicle(vehicleId: Long): Flow<List<FuelLogEntity>>

    @Query("SELECT * FROM fuel_log WHERE id = :id")
    suspend fun getById(id: Long): FuelLogEntity?

    @Insert
    suspend fun insert(log: FuelLogEntity): Long

    @Update
    suspend fun update(log: FuelLogEntity)

    @Query("DELETE FROM fuel_log WHERE id = :id")
    suspend fun deleteById(id: Long)
}
