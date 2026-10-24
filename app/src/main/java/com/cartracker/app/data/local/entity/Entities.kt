package com.cartracker.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// همه تاریخ‌ها به‌صورت میلی‌ثانیه (UTC) ذخیره می‌شوند و فقط هنگام نمایش به شمسی تبدیل می‌شوند.
// نوع‌های شمارشی (نوع سوخت، دسته هزینه، ...) به‌صورت رشته ذخیره می‌شوند تا مهاجرت دیتابیس ساده بماند.

@Entity(tableName = "vehicle")
data class VehicleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val brand: String,
    val model: String,
    val year: Int?,
    val plate: String?,
    val fuelType: String,            // PETROL | DIESEL | CNG | BI_FUEL
    val tankCapacityLiters: Double?,
    val initialOdometerKm: Int,
    val colorArgb: Long,
    val photoUri: String?,
    val createdAt: Long,
    val isArchived: Boolean = false
)

@Entity(
    tableName = "fuel_log",
    foreignKeys = [ForeignKey(
        entity = VehicleEntity::class,
        parentColumns = ["id"],
        childColumns = ["vehicleId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["vehicleId", "odometerKm"]), Index(value = ["vehicleId", "dateMillis"])]
)
data class FuelLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehicleId: Long,
    val dateMillis: Long,
    val odometerKm: Int,
    val liters: Double,
    val pricePerLiter: Double,
    val totalCost: Double,
    val fuelType: String,            // برای دوگانه‌سوز: PETROL یا CNG
    val station: String?,
    val isFullTank: Boolean,
    val note: String?
)

@Entity(
    tableName = "service_type",
    indices = [Index(value = ["name"], unique = true)]
)
data class ServiceTypeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val iconKey: String,
    val defaultIntervalKm: Int?,
    val defaultIntervalMonths: Int?,
    val isDefault: Boolean,
    val isArchived: Boolean = false
)

@Entity(
    tableName = "service_record",
    foreignKeys = [
        ForeignKey(
            entity = VehicleEntity::class,
            parentColumns = ["id"],
            childColumns = ["vehicleId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ServiceTypeEntity::class,
            parentColumns = ["id"],
            childColumns = ["serviceTypeId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["vehicleId", "dateMillis"]),
        Index(value = ["serviceTypeId"])
    ]
)
data class ServiceRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehicleId: Long,
    val serviceTypeId: Long,
    val dateMillis: Long,
    val odometerKm: Int,
    val cost: Double,
    val place: String?,
    val note: String?
)

@Entity(
    tableName = "expense_record",
    foreignKeys = [ForeignKey(
        entity = VehicleEntity::class,
        parentColumns = ["id"],
        childColumns = ["vehicleId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [
        Index(value = ["vehicleId", "dateMillis"]),
        Index(value = ["vehicleId", "category"]),
        Index(value = ["tripTag"])
    ]
)
data class ExpenseRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehicleId: Long,
    val category: String,            // REPAIR | PART | INSURANCE | INSPECTION | TOLL | PARKING | FINE | WASH | ACCESSORY | OTHER ...
    val title: String,
    val dateMillis: Long,
    val odometerKm: Int?,
    val amount: Double,
    val tripTag: String?,
    val expiryMillis: Long?,         // انقضای بیمه یا معاینه فنی
    val detailsJson: String?,        // جزئیات خاص هر دسته (برند قطعه، شماره بیمه‌نامه، ...)
    val note: String?
)

@Entity(
    tableName = "reminder",
    foreignKeys = [
        ForeignKey(
            entity = VehicleEntity::class,
            parentColumns = ["id"],
            childColumns = ["vehicleId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ServiceTypeEntity::class,
            parentColumns = ["id"],
            childColumns = ["serviceTypeId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["vehicleId", "enabled"]),
        Index(value = ["serviceTypeId"])
    ]
)
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehicleId: Long,
    val serviceTypeId: Long?,
    val title: String,
    val kind: String,                // SERVICE | INSURANCE | INSPECTION | LICENSE | CUSTOM
    val intervalKm: Int?,
    val intervalMonths: Int?,
    val lastDoneKm: Int?,
    val lastDoneMillis: Long?,
    val nextDueKm: Int?,
    val nextDueMillis: Long?,
    val snoozedUntilMillis: Long?,
    val enabled: Boolean = true
)

@Entity(
    tableName = "attachment",
    indices = [Index(value = ["ownerType", "ownerId"])]
)
data class AttachmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ownerType: String,           // SERVICE_RECORD | EXPENSE_RECORD | VEHICLE
    val ownerId: Long,
    val uri: String,
    val createdAt: Long
)
