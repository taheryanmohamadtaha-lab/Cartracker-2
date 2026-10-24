package com.cartracker.app.data.mapper

import com.cartracker.app.data.local.entity.ExpenseRecordEntity
import com.cartracker.app.data.local.entity.ServiceRecordEntity
import com.cartracker.app.data.local.entity.ServiceTypeEntity
import com.cartracker.app.domain.model.ExpenseCategory
import com.cartracker.app.domain.model.ExpenseDetails
import com.cartracker.app.domain.model.ExpenseRecord
import com.cartracker.app.domain.model.InsuranceKind
import com.cartracker.app.domain.model.RepairArea
import com.cartracker.app.domain.model.ServiceRecord
import com.cartracker.app.domain.model.ServiceType
import org.json.JSONObject

fun ServiceTypeEntity.toDomain() = ServiceType(
    id = id,
    name = name,
    iconKey = iconKey,
    defaultIntervalKm = defaultIntervalKm,
    defaultIntervalMonths = defaultIntervalMonths,
    isDefault = isDefault,
    isArchived = isArchived
)

fun ServiceType.toEntity() = ServiceTypeEntity(
    id = id,
    name = name,
    iconKey = iconKey,
    defaultIntervalKm = defaultIntervalKm,
    defaultIntervalMonths = defaultIntervalMonths,
    isDefault = isDefault,
    isArchived = isArchived
)

fun ServiceRecordEntity.toDomain() = ServiceRecord(
    id = id,
    vehicleId = vehicleId,
    serviceTypeId = serviceTypeId,
    dateMillis = dateMillis,
    odometerKm = odometerKm,
    cost = cost,
    place = place,
    note = note
)

fun ServiceRecord.toEntity() = ServiceRecordEntity(
    id = id,
    vehicleId = vehicleId,
    serviceTypeId = serviceTypeId,
    dateMillis = dateMillis,
    odometerKm = odometerKm,
    cost = cost,
    place = place,
    note = note
)

fun ExpenseRecordEntity.toDomain() = ExpenseRecord(
    id = id,
    vehicleId = vehicleId,
    category = ExpenseCategory.fromName(category),
    title = title,
    dateMillis = dateMillis,
    odometerKm = odometerKm,
    amount = amount,
    tripTag = tripTag,
    expiryMillis = expiryMillis,
    details = ExpenseDetailsJson.decode(detailsJson),
    note = note
)

fun ExpenseRecord.toEntity() = ExpenseRecordEntity(
    id = id,
    vehicleId = vehicleId,
    category = category.name,
    title = title,
    dateMillis = dateMillis,
    odometerKm = odometerKm,
    amount = amount,
    tripTag = tripTag,
    expiryMillis = expiryMillis,
    detailsJson = ExpenseDetailsJson.encode(details),
    note = note
)

/** جزئیات خاص هر دسته هزینه را به JSON ساده تبدیل می‌کند و برعکس. */
object ExpenseDetailsJson {

    fun encode(details: ExpenseDetails): String? {
        if (details.isEmpty) return null
        val json = JSONObject()
        details.repairArea?.let { json.put("repairArea", it.name) }
        details.brand?.let { json.put("brand", it) }
        details.partNumber?.let { json.put("partNumber", it) }
        details.warrantyMonths?.let { json.put("warrantyMonths", it) }
        details.insuranceKind?.let { json.put("insuranceKind", it.name) }
        details.company?.let { json.put("company", it) }
        details.policyNumber?.let { json.put("policyNumber", it) }
        details.startMillis?.let { json.put("startMillis", it) }
        details.discountPercent?.let { json.put("discountPercent", it) }
        return json.toString()
    }

    fun decode(text: String?): ExpenseDetails {
        if (text.isNullOrBlank()) return ExpenseDetails()
        val json = runCatching { JSONObject(text) }.getOrNull() ?: return ExpenseDetails()

        fun string(key: String): String? =
            if (json.has(key)) json.optString(key, "").ifBlank { null } else null

        fun int(key: String): Int? = if (json.has(key)) json.optInt(key) else null
        fun long(key: String): Long? = if (json.has(key)) json.optLong(key) else null

        return ExpenseDetails(
            repairArea = string("repairArea")?.let { RepairArea.fromName(it) },
            brand = string("brand"),
            partNumber = string("partNumber"),
            warrantyMonths = int("warrantyMonths"),
            insuranceKind = string("insuranceKind")?.let { InsuranceKind.fromName(it) },
            company = string("company"),
            policyNumber = string("policyNumber"),
            startMillis = long("startMillis"),
            discountPercent = int("discountPercent")
        )
    }
}
