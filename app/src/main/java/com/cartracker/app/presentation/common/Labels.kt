package com.cartracker.app.presentation.common

import androidx.annotation.StringRes
import com.cartracker.app.R
import com.cartracker.app.domain.model.ExpenseCategory
import com.cartracker.app.domain.model.FuelKind
import com.cartracker.app.domain.model.FuelType
import com.cartracker.app.domain.model.InsuranceKind
import com.cartracker.app.domain.model.RepairArea

@StringRes
fun FuelType.labelRes(): Int = when (this) {
    FuelType.PETROL -> R.string.fuel_type_petrol
    FuelType.DIESEL -> R.string.fuel_type_diesel
    FuelType.CNG -> R.string.fuel_type_cng
    FuelType.BI_FUEL -> R.string.fuel_type_bifuel
}

@StringRes
fun FuelKind.labelRes(): Int = when (this) {
    FuelKind.PETROL -> R.string.fuel_type_petrol
    FuelKind.DIESEL -> R.string.fuel_type_diesel
    FuelKind.CNG -> R.string.fuel_type_cng
}

@StringRes
fun ExpenseCategory.labelRes(): Int = when (this) {
    ExpenseCategory.REPAIR -> R.string.expense_repair
    ExpenseCategory.PART -> R.string.expense_part
    ExpenseCategory.INSURANCE -> R.string.expense_insurance
    ExpenseCategory.INSPECTION -> R.string.expense_inspection
    ExpenseCategory.ANNUAL_TAX -> R.string.expense_annual_tax
    ExpenseCategory.HIGHWAY_TOLL -> R.string.expense_highway_toll
    ExpenseCategory.PARKING -> R.string.expense_parking
    ExpenseCategory.FINE -> R.string.expense_fine
    ExpenseCategory.WASH -> R.string.expense_wash
    ExpenseCategory.ACCESSORY -> R.string.expense_accessory
    ExpenseCategory.OTHER -> R.string.expense_other
}

@StringRes
fun RepairArea.labelRes(): Int = when (this) {
    RepairArea.ENGINE -> R.string.repair_engine
    RepairArea.ELECTRICAL -> R.string.repair_electrical
    RepairArea.BODY -> R.string.repair_body
    RepairArea.SUSPENSION -> R.string.repair_suspension
    RepairArea.BRAKE -> R.string.repair_brake
    RepairArea.TRANSMISSION -> R.string.repair_transmission
    RepairArea.COOLING -> R.string.repair_cooling
    RepairArea.OTHER -> R.string.repair_other
}

@StringRes
fun InsuranceKind.labelRes(): Int = when (this) {
    InsuranceKind.THIRD_PARTY -> R.string.insurance_third_party
    InsuranceKind.BODY -> R.string.insurance_body
}
