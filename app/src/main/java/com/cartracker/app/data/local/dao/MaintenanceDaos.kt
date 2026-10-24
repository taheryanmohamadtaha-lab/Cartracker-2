package com.cartracker.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.cartracker.app.data.local.entity.AttachmentEntity
import com.cartracker.app.data.local.entity.ExpenseRecordEntity
import com.cartracker.app.data.local.entity.ServiceRecordEntity
import com.cartracker.app.data.local.entity.ServiceTypeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceTypeDao {

    @Query("SELECT * FROM service_type ORDER BY isDefault DESC, id ASC")
    fun observeAll(): Flow<List<ServiceTypeEntity>>

    @Query("SELECT COUNT(*) FROM service_type")
    suspend fun count(): Int

    @Query("SELECT * FROM service_type WHERE id = :id")
    suspend fun getById(id: Long): ServiceTypeEntity?

    @Insert
    suspend fun insert(type: ServiceTypeEntity): Long

    @Insert
    suspend fun insertAll(types: List<ServiceTypeEntity>)

    @Update
    suspend fun update(type: ServiceTypeEntity)

    @Query("SELECT COUNT(*) FROM service_record WHERE serviceTypeId = :id")
    suspend fun recordCount(id: Long): Int

    @Query("DELETE FROM service_type WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface ServiceRecordDao {

    @Query("SELECT * FROM service_record WHERE vehicleId = :vehicleId ORDER BY dateMillis DESC, odometerKm DESC")
    fun observeByVehicle(vehicleId: Long): Flow<List<ServiceRecordEntity>>

    @Query("SELECT * FROM service_record WHERE id = :id")
    suspend fun getById(id: Long): ServiceRecordEntity?

    @Insert
    suspend fun insert(record: ServiceRecordEntity): Long

    @Update
    suspend fun update(record: ServiceRecordEntity)

    @Query("DELETE FROM service_record WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface ExpenseDao {

    @Query("SELECT * FROM expense_record WHERE vehicleId = :vehicleId ORDER BY dateMillis DESC, id DESC")
    fun observeByVehicle(vehicleId: Long): Flow<List<ExpenseRecordEntity>>

    @Query("SELECT * FROM expense_record WHERE id = :id")
    suspend fun getById(id: Long): ExpenseRecordEntity?

    @Insert
    suspend fun insert(record: ExpenseRecordEntity): Long

    @Update
    suspend fun update(record: ExpenseRecordEntity)

    @Query("DELETE FROM expense_record WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface AttachmentDao {

    @Query("SELECT * FROM attachment WHERE ownerType = :ownerType AND ownerId = :ownerId ORDER BY id ASC")
    suspend fun getFor(ownerType: String, ownerId: Long): List<AttachmentEntity>

    @Insert
    suspend fun insert(attachment: AttachmentEntity): Long

    @Query("DELETE FROM attachment WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query(
        "SELECT uri FROM attachment WHERE " +
            "(ownerType = 'SERVICE_RECORD' AND ownerId NOT IN (SELECT id FROM service_record)) OR " +
            "(ownerType = 'EXPENSE_RECORD' AND ownerId NOT IN (SELECT id FROM expense_record))"
    )
    suspend fun orphanUris(): List<String>

    @Query(
        "DELETE FROM attachment WHERE " +
            "(ownerType = 'SERVICE_RECORD' AND ownerId NOT IN (SELECT id FROM service_record)) OR " +
            "(ownerType = 'EXPENSE_RECORD' AND ownerId NOT IN (SELECT id FROM expense_record))"
    )
    suspend fun deleteOrphans()
}
