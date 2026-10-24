package com.cartracker.app.domain.model

data class ServiceType(
    val id: Long = 0,
    val name: String,
    val iconKey: String = "build",
    val defaultIntervalKm: Int?,
    val defaultIntervalMonths: Int?,
    val isDefault: Boolean = false,
    val isArchived: Boolean = false
)

data class ServiceRecord(
    val id: Long = 0,
    val vehicleId: Long,
    val serviceTypeId: Long,
    val dateMillis: Long,
    val odometerKm: Int,
    val cost: Double,            // تومان
    val place: String?,
    val note: String?
)

enum class ExpenseCategory {
    REPAIR, PART, INSURANCE, INSPECTION, ANNUAL_TAX, HIGHWAY_TOLL,
    PARKING, FINE, WASH, ACCESSORY, OTHER;

    companion object {
        fun fromName(name: String): ExpenseCategory = entries.firstOrNull { it.name == name } ?: OTHER
    }
}

enum class RepairArea {
    ENGINE, ELECTRICAL, BODY, SUSPENSION, BRAKE, TRANSMISSION, COOLING, OTHER;

    companion object {
        fun fromName(name: String): RepairArea? = entries.firstOrNull { it.name == name }
    }
}

enum class InsuranceKind {
    THIRD_PARTY, BODY;

    companion object {
        fun fromName(name: String): InsuranceKind? = entries.firstOrNull { it.name == name }
    }
}

/** جزئیات خاص هر دسته هزینه (در دیتابیس به‌صورت JSON ذخیره می‌شود). */
data class ExpenseDetails(
    val repairArea: RepairArea? = null,
    val brand: String? = null,
    val partNumber: String? = null,
    val warrantyMonths: Int? = null,
    val insuranceKind: InsuranceKind? = null,
    val company: String? = null,
    val policyNumber: String? = null,
    val startMillis: Long? = null,
    val discountPercent: Int? = null
) {
    val isEmpty: Boolean
        get() = repairArea == null && brand == null && partNumber == null && warrantyMonths == null &&
            insuranceKind == null && company == null && policyNumber == null &&
            startMillis == null && discountPercent == null
}

data class ExpenseRecord(
    val id: Long = 0,
    val vehicleId: Long,
    val category: ExpenseCategory,
    val title: String,
    val dateMillis: Long,
    val odometerKm: Int?,
    val amount: Double,          // تومان
    val tripTag: String?,
    val expiryMillis: Long?,     // پایان بیمه یا انقضای معاینه فنی
    val details: ExpenseDetails,
    val note: String?
)

enum class AttachmentOwner { SERVICE_RECORD, EXPENSE_RECORD, VEHICLE }
