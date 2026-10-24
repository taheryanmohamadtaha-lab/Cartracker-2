package com.cartracker.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cartracker.app.data.local.dao.AttachmentDao
import com.cartracker.app.data.local.dao.ExpenseDao
import com.cartracker.app.data.local.dao.FuelLogDao
import com.cartracker.app.data.local.dao.ServiceRecordDao
import com.cartracker.app.data.local.dao.ServiceTypeDao
import com.cartracker.app.data.local.dao.VehicleDao
import com.cartracker.app.data.local.entity.AttachmentEntity
import com.cartracker.app.data.local.entity.ExpenseRecordEntity
import com.cartracker.app.data.local.entity.FuelLogEntity
import com.cartracker.app.data.local.entity.ReminderEntity
import com.cartracker.app.data.local.entity.ServiceRecordEntity
import com.cartracker.app.data.local.entity.ServiceTypeEntity
import com.cartracker.app.data.local.entity.VehicleEntity

@Database(
    entities = [
        VehicleEntity::class,
        FuelLogEntity::class,
        ServiceTypeEntity::class,
        ServiceRecordEntity::class,
        ExpenseRecordEntity::class,
        ReminderEntity::class,
        AttachmentEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao
    abstract fun fuelLogDao(): FuelLogDao
    abstract fun serviceTypeDao(): ServiceTypeDao
    abstract fun serviceRecordDao(): ServiceRecordDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun attachmentDao(): AttachmentDao
}
