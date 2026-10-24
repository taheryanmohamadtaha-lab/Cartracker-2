package com.cartracker.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.cartracker.app.data.local.entity.VehicleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {

    @Query("SELECT * FROM vehicle WHERE isArchived = 0 ORDER BY createdAt ASC, id ASC")
    fun observeAll(): Flow<List<VehicleEntity>>

    @Query("SELECT * FROM vehicle WHERE id = :id")
    suspend fun getById(id: Long): VehicleEntity?

    @Insert
    suspend fun insert(vehicle: VehicleEntity): Long

    @Update
    suspend fun update(vehicle: VehicleEntity)

    @Query("DELETE FROM vehicle WHERE id = :id")
    suspend fun deleteById(id: Long)
}
