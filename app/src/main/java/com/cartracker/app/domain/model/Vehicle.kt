package com.cartracker.app.domain.model

/** نوع سوخت یک سوخت‌گیری مشخص. */
enum class FuelKind {
    PETROL, DIESEL, CNG;

    companion object {
        fun fromName(name: String): FuelKind = entries.firstOrNull { it.name == name } ?: PETROL
    }
}

/** نوع سوخت خودرو؛ دوگانه‌سوز هم بنزین و هم CNG می‌گیرد. */
enum class FuelType(val kinds: List<FuelKind>) {
    PETROL(listOf(FuelKind.PETROL)),
    DIESEL(listOf(FuelKind.DIESEL)),
    CNG(listOf(FuelKind.CNG)),
    BI_FUEL(listOf(FuelKind.PETROL, FuelKind.CNG));

    companion object {
        fun fromName(name: String): FuelType = entries.firstOrNull { it.name == name } ?: PETROL
    }
}

data class Vehicle(
    val id: Long = 0,
    val name: String,
    val brand: String,
    val model: String,
    val year: Int?,
    val plate: String?,
    val fuelType: FuelType,
    val tankCapacityLiters: Double?,
    val initialOdometerKm: Int,
    val colorArgb: Long,
    val photoPath: String?,
    val createdAt: Long
)

object VehicleColors {
    val palette: List<Long> = listOf(
        0xFFE53935, 0xFFFB8C00, 0xFFFDD835, 0xFF43A047, 0xFF00ACC1,
        0xFF1E88E5, 0xFF5E35B1, 0xFFD81B60, 0xFF546E7A, 0xFFFFFFFF, 0xFF212121
    )
    val default: Long = palette[5]
}

data class FuelLog(
    val id: Long = 0,
    val vehicleId: Long,
    val dateMillis: Long,
    val odometerKm: Int,
    val liters: Double,
    val pricePerLiter: Double,   // تومان
    val totalCost: Double,       // تومان
    val fuelKind: FuelKind,
    val station: String?,
    val isFullTank: Boolean,
    val note: String?
)
