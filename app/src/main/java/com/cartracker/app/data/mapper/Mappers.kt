package com.cartracker.app.data.mapper

import com.cartracker.app.data.local.entity.FuelLogEntity
import com.cartracker.app.data.local.entity.VehicleEntity
import com.cartracker.app.domain.model.FuelKind
import com.cartracker.app.domain.model.FuelLog
import com.cartracker.app.domain.model.FuelType
import com.cartracker.app.domain.model.Vehicle

fun VehicleEntity.toDomain() = Vehicle(
    id = id,
    name = name,
    brand = brand,
    model = model,
    year = year,
    plate = plate,
    fuelType = FuelType.fromName(fuelType),
    tankCapacityLiters = tankCapacityLiters,
    initialOdometerKm = initialOdometerKm,
    colorArgb = colorArgb,
    photoPath = photoUri,
    createdAt = createdAt
)

fun Vehicle.toEntity() = VehicleEntity(
    id = id,
    name = name,
    brand = brand,
    model = model,
    year = year,
    plate = plate,
    fuelType = fuelType.name,
    tankCapacityLiters = tankCapacityLiters,
    initialOdometerKm = initialOdometerKm,
    colorArgb = colorArgb,
    photoUri = photoPath,
    createdAt = createdAt
)

fun FuelLogEntity.toDomain() = FuelLog(
    id = id,
    vehicleId = vehicleId,
    dateMillis = dateMillis,
    odometerKm = odometerKm,
    liters = liters,
    pricePerLiter = pricePerLiter,
    totalCost = totalCost,
    fuelKind = FuelKind.fromName(fuelType),
    station = station,
    isFullTank = isFullTank,
    note = note
)

fun FuelLog.toEntity() = FuelLogEntity(
    id = id,
    vehicleId = vehicleId,
    dateMillis = dateMillis,
    odometerKm = odometerKm,
    liters = liters,
    pricePerLiter = pricePerLiter,
    totalCost = totalCost,
    fuelType = fuelKind.name,
    station = station,
    isFullTank = isFullTank,
    note = note
)
