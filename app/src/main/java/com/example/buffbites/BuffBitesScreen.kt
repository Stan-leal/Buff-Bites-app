package com.example.buffbites

import android.content.ClipData.Item
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.buffbites.ui.OrderViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.example.buffbites.data.Datasource
import com.example.buffbites.ui.ChooseDeliveryTimeScreen
import com.example.buffbites.ui.ChooseMenuScreen
import com.example.buffbites.ui.OrderSummaryScreen
import com.example.buffbites.ui.OrderUiState
import com.example.buffbites.ui.StartOrderScreen

enum class BuffBitesScreen(@StringRes val title: Int) {
    Start(title = R.string.app_name),
    Meal(title = R.string.choose_meal),
    Pickup(title = R.string.choose_delivery_time),
    Summary(title = R.string.order_summary)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuffBiteBar(
    currentScreen: BuffBitesScreen,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier
){
    TopAppBar(
        title = { Text(stringResource(currentScreen.title)) },
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back_button)
                )
            }
        }

    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuffBitesApp(
    viewModel: OrderViewModel = viewModel(),
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = BuffBitesScreen.valueOf(
        backStackEntry?.destination?.route ?: BuffBitesScreen.Start.name
    )


    Scaffold(
        topBar = {
            BuffBiteBar(
                currentScreen = currentScreen,
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = { navController.navigateUp()}
            )


        }
    ) { innerPadding ->
        val uiState by viewModel.uiState.collectAsState()


        NavHost(
            navController = navController,
            startDestination = BuffBitesScreen.Start.name,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = BuffBitesScreen.Start.name) {
                val context = uiState.selectedVendor
                StartOrderScreen(
                    restaurantOptions = Datasource.restaurants,
                    onNextButtonClicked = {
                        navController.navigate(BuffBitesScreen.Meal.name)
                    }
                )
            }
            composable(route = BuffBitesScreen.Meal.name) {
                ChooseMenuScreen(
                    onNextButtonClicked = {navController.navigate(BuffBitesScreen.Pickup.name)},
                    onCancelButtonClicked = {
                        cancelOrderAndNavigateToStart(viewModel, navController) },
                    options = uiState.selectedVendor?.menuItems ?: listOf(),
                    onSelectionChanged = { viewModel.updateMeal(it)}

                )
            }
            composable(route = BuffBitesScreen.Pickup.name) {
                ChooseDeliveryTimeScreen(
                    subtotal = uiState.orderTotalPrice,
                    onNextButtonClicked = {navController.navigate(BuffBitesScreen.Summary.name)},
                    onCancelButtonClicked = {
                        cancelOrderAndNavigateToStart(viewModel, navController)
                    },
                    options = uiState.availableDeliveryTimes,


                    onSelectionChanged = { viewModel.updateDeliveryTime(it) },
                )
            }
            composable(route = BuffBitesScreen.Summary.name) {
                OrderSummaryScreen(
                    orderUiState = uiState,
                    onCancelButtonClicked = {
                        cancelOrderAndNavigateToStart(viewModel, navController) },
                    onSendButtonClicked = { subject: String, summary: String -> }
                )

            }

        }
    }
}

private fun cancelOrderAndNavigateToStart(
    viewModel: OrderViewModel,
    navController: NavHostController
){
    viewModel.resetOrder()
    navController.popBackStack(BuffBitesScreen.Start.name, inclusive = false)
}


