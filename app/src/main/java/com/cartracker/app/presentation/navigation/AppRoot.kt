package com.cartracker.app.presentation.navigation

import androidx.annotation.StringRes
import androidx.compose.animation.Crossfade
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocalGasStation
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cartracker.app.R
import com.cartracker.app.core.LocalFormatter
import com.cartracker.app.core.rememberAppFormatter
import com.cartracker.app.domain.model.AppSettings
import com.cartracker.app.presentation.fuel.FuelEditScreen
import com.cartracker.app.presentation.fuel.FuelScreen
import com.cartracker.app.presentation.home.HomeScreen
import com.cartracker.app.presentation.maintenance.ExpenseEditScreen
import com.cartracker.app.presentation.maintenance.MaintenanceScreen
import com.cartracker.app.presentation.maintenance.ServiceEditScreen
import com.cartracker.app.presentation.maintenance.ServiceTypesScreen
import com.cartracker.app.presentation.settings.SettingsActions
import com.cartracker.app.presentation.settings.SettingsScreen
import com.cartracker.app.presentation.theme.AnimeBackground
import com.cartracker.app.presentation.theme.AppThemes
import com.cartracker.app.presentation.theme.LocalActiveTheme
import com.cartracker.app.presentation.theme.LocalReduceMotion
import com.cartracker.app.presentation.vehicles.VehicleEditScreen
import com.cartracker.app.presentation.vehicles.VehiclesScreen

private object Routes {
    const val HOME = "home"
    const val FUEL = "fuel"
    const val MAINTENANCE = "maintenance"
    const val SETTINGS = "settings"
    const val VEHICLES = "vehicles"
    const val VEHICLE_EDIT = "vehicle_edit/{vehicleId}"
    const val FUEL_EDIT = "fuel_edit/{vehicleId}/{logId}"

    const val SERVICE_EDIT = "service_edit/{vehicleId}/{recordId}"
    const val EXPENSE_EDIT = "expense_edit/{vehicleId}/{recordId}"
    const val SERVICE_TYPES = "service_types"

    fun serviceEdit(vehicleId: Long, recordId: Long) = "service_edit/$vehicleId/$recordId"
    fun expenseEdit(vehicleId: Long, recordId: Long) = "expense_edit/$vehicleId/$recordId"
    fun vehicleEdit(vehicleId: Long) = "vehicle_edit/$vehicleId"
    fun fuelEdit(vehicleId: Long, logId: Long) = "fuel_edit/$vehicleId/$logId"
}

private data class BottomItem(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector
)

private val bottomItems = listOf(
    BottomItem(Routes.HOME, R.string.nav_home, Icons.Rounded.Home),
    BottomItem(Routes.FUEL, R.string.nav_fuel, Icons.Rounded.LocalGasStation),
    BottomItem(Routes.MAINTENANCE, R.string.nav_maintenance, Icons.Rounded.Build),
    BottomItem(Routes.SETTINGS, R.string.nav_settings, Icons.Rounded.Settings)
)

private val topLevelRoutes = bottomItems.map { it.route }.toSet()

private const val NEW_ITEM_ID = -1L

@Composable
fun AppRoot(
    settings: AppSettings,
    actions: SettingsActions
) {
    val reduceMotion = LocalReduceMotion.current
    val active = LocalActiveTheme.current
    val formatter = rememberAppFormatter(settings.persianDigits, settings.currency)
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    CompositionLocalProvider(LocalFormatter provides formatter) {
        Box(modifier = Modifier.fillMaxSize()) {
            // پس‌زمینه تم؛ هنگام عوض‌شدن تم یا حالت روز/شب، نرم محو می‌شود.
            Crossfade(
                targetState = active.id to active.variant.isDark,
                animationSpec = if (reduceMotion) snap() else tween(700),
                label = "themeBackground"
            ) { (themeId, dark) ->
                AnimeBackground(
                    themeId = themeId,
                    variant = AppThemes.variant(themeId, dark),
                    animated = !reduceMotion
                )
            }

            Scaffold(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onBackground,
                bottomBar = {
                    if (currentRoute == null || currentRoute in topLevelRoutes) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                        ) {
                            bottomItems.forEach { item ->
                                NavigationBarItem(
                                    selected = currentRoute == item.route,
                                    onClick = {
                                        navController.navigate(item.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(item.icon, contentDescription = null) },
                                    label = { Text(stringResource(item.labelRes)) }
                                )
                            }
                        }
                    }
                }
            ) { padding ->
                NavHost(
                    navController = navController,
                    startDestination = Routes.HOME,
                    modifier = Modifier.padding(padding),
                    enterTransition = {
                        if (reduceMotion) EnterTransition.None
                        else fadeIn(tween(250)) + slideInVertically(tween(250)) { it / 20 }
                    },
                    exitTransition = {
                        if (reduceMotion) ExitTransition.None else fadeOut(tween(150))
                    },
                    popEnterTransition = {
                        if (reduceMotion) EnterTransition.None else fadeIn(tween(250))
                    },
                    popExitTransition = {
                        if (reduceMotion) ExitTransition.None else fadeOut(tween(150))
                    }
                ) {
                    composable(Routes.HOME) {
                        HomeScreen(
                            onAddFuel = { vehicleId ->
                                navController.navigate(Routes.fuelEdit(vehicleId, NEW_ITEM_ID))
                            },
                            onManageVehicles = { navController.navigate(Routes.VEHICLES) },
                            onAddVehicle = {
                                navController.navigate(Routes.vehicleEdit(NEW_ITEM_ID))
                            }
                        )
                    }
                    composable(Routes.FUEL) {
                        FuelScreen(
                            onAdd = { vehicleId ->
                                navController.navigate(Routes.fuelEdit(vehicleId, NEW_ITEM_ID))
                            },
                            onEdit = { vehicleId, logId ->
                                navController.navigate(Routes.fuelEdit(vehicleId, logId))
                            },
                            onManageVehicles = { navController.navigate(Routes.VEHICLES) },
                            onAddVehicle = {
                                navController.navigate(Routes.vehicleEdit(NEW_ITEM_ID))
                            }
                        )
                    }
                    composable(Routes.MAINTENANCE) {
                        MaintenanceScreen(
                            onAddService = { vehicleId ->
                                navController.navigate(Routes.serviceEdit(vehicleId, NEW_ITEM_ID))
                            },
                            onEditService = { vehicleId, recordId ->
                                navController.navigate(Routes.serviceEdit(vehicleId, recordId))
                            },
                            onAddExpense = { vehicleId ->
                                navController.navigate(Routes.expenseEdit(vehicleId, NEW_ITEM_ID))
                            },
                            onEditExpense = { vehicleId, recordId ->
                                navController.navigate(Routes.expenseEdit(vehicleId, recordId))
                            },
                            onManageTypes = { navController.navigate(Routes.SERVICE_TYPES) },
                            onManageVehicles = { navController.navigate(Routes.VEHICLES) },
                            onAddVehicle = {
                                navController.navigate(Routes.vehicleEdit(NEW_ITEM_ID))
                            }
                        )
                    }
                    composable(Routes.SERVICE_TYPES) {
                        ServiceTypesScreen(onBack = { navController.popBackStack() })
                    }
                    composable(
                        route = Routes.SERVICE_EDIT,
                        arguments = listOf(
                            navArgument("vehicleId") { type = NavType.LongType },
                            navArgument("recordId") { type = NavType.LongType }
                        )
                    ) {
                        ServiceEditScreen(
                            onDone = { navController.popBackStack() },
                            onManageTypes = { navController.navigate(Routes.SERVICE_TYPES) }
                        )
                    }
                    composable(
                        route = Routes.EXPENSE_EDIT,
                        arguments = listOf(
                            navArgument("vehicleId") { type = NavType.LongType },
                            navArgument("recordId") { type = NavType.LongType }
                        )
                    ) {
                        ExpenseEditScreen(onDone = { navController.popBackStack() })
                    }
                    composable(Routes.SETTINGS) {
                        SettingsScreen(settings = settings, actions = actions)
                    }
                    composable(Routes.VEHICLES) {
                        VehiclesScreen(
                            onBack = { navController.popBackStack() },
                            onAdd = { navController.navigate(Routes.vehicleEdit(NEW_ITEM_ID)) },
                            onEdit = { id -> navController.navigate(Routes.vehicleEdit(id)) }
                        )
                    }
                    composable(
                        route = Routes.VEHICLE_EDIT,
                        arguments = listOf(navArgument("vehicleId") { type = NavType.LongType })
                    ) {
                        VehicleEditScreen(onDone = { navController.popBackStack() })
                    }
                    composable(
                        route = Routes.FUEL_EDIT,
                        arguments = listOf(
                            navArgument("vehicleId") { type = NavType.LongType },
                            navArgument("logId") { type = NavType.LongType }
                        )
                    ) {
                        FuelEditScreen(onDone = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}
