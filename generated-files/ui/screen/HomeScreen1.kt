package ifac.td.taxi.ui.screen
import ifac.td.taxi.domain.usecase.*
import ifac.td.taxi.repository.room.entities.message.MessageType
import androidx.lifecycle.viewModelScope
import android.media.ToneGenerator
import ifac.td.taxi.viewmodel.model.MessageUIEnum
import ifac.td.taxi.framework.util.Logs
import kotlinx.coroutines.launch
import ifac.td.taxi.repository.connections.service.model.ShortBreakStatus
import com.interfacom.sdk.taximeter.bravocomm.W2CLocation
import ifac.td.taxi.domain.model.Trip
import ifac.td.taxi.framework.sdk.bravocentral.usecase.BravoCentralUseCase
import ifac.td.taxi.framework.sdk.usecase.LicensingUseCase
import android.app.Application
import com.interfacom.sdk.taximeter.bravocomm.ifConstants
import kotlinx.coroutines.Dispatchers
import ifac.td.taxi.framework.sdk.bravocentral.usecase.BravoCentralConfigurationUseCase
import ifac.td.taxi.domain.model.PaymentMethod
import androidx.compose.runtime.Composable
import ifac.td.taxi.ui.screen.home.*
import kotlinx.coroutines.flow.*
// // ## 6) Compose screen with lifecycle collection and dialog handling

// This is the `onResume` replacement using `repeatOnLifecycle`.


import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ifac.td.taxi.viewmodel.HomeViewModel
import ifac.td.taxi.viewmodel.MainActivityViewModel
import ifac.td.taxi.viewmodel.model.MessageUIEnum

@Composable
fun HomeRoute(
    navController: NavController,
    viewModel: HomeViewModel,
    sharedViewModel: MainActivityViewModel,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            launch {
                sharedViewModel.bravoStateFlow.collectLatest { state ->
                    uiState.buttons.central.backgroundColor
                    viewModel.checkRoofLight(sharedViewModel.roofLightFlow.value)
                }
            }
            launch {
                sharedViewModel.roofLightFlow.collectLatest { value ->
                    viewModel.checkRoofLight(value)
                }
            }
            launch {
                sharedViewModel.locationEnabledFlow.collectLatest { value ->
                    viewModel.updateLocationEnabled(value.first, value.second)
                    viewModel.checkZoningButton(value.first)
                }
            }
            launch {
                sharedViewModel.locatedOnStop.collectLatest {
                    viewModel.updateLocatedOnStop(it)
                }
            }
            launch {
                sharedViewModel.locationType.collectLatest {
                    viewModel.updateLocationType(it)
                }
            }
            launch {
                sharedViewModel.orangeBtnPendingFlow.collectLatest {
                    viewModel.updatePendingOrange(it)
                }
            }
            launch {
                sharedViewModel.shortBreakStatus.collectLatest {
                    viewModel.onShortBreakStatusChanged(it)
                }
            }
            launch {
                sharedViewModel.updateMessageUI.collectLatest {
                    viewModel.getMessages()
                }
            }
            launch {
                viewModel.uiEffect.collectLatest { effect ->
                    when (effect) {
                        is HomeUiEffect.Navigate -> {
                            navController.navigate(effect.route.route)
                        }
                        is HomeUiEffect.ShowToast -> Toast.makeText(context, context.getString(effect.messageRes), Toast.LENGTH_SHORT).show()
                        is HomeUiEffect.PlayBeep -> sharedViewModel.beep(effect.tone)
                        is HomeUiEffect.KeepScreenOn -> { /* activity callback if needed */ }
                        is HomeUiEffect.OpenDialog -> viewModel.setDialog(effect.dialog)
                        HomeUiEffect.CloseDialog -> viewModel.clearDialog()
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.onResume()
        sharedViewModel.resetDispatchFlow()
        sharedViewModel.resetCurrentAmountFlow()
    }

    HomeScreen(
        uiState = uiState,
        onZoningClick = viewModel::navigateToZoning,
        onPendingClick = { viewModel.handlePendingTripsPress(sharedViewModel.pendingTripsListFlow.value?.isNotEmpty() == true) },
        onLocateStandClick = { /* implement your locate logic */ },
        onLocationClick = viewModel::onLocationButtonClicked,
        onReceiptsClick = viewModel::navigateToReceipts,
        onMessagesClick = viewModel::navigateToMessages,
        onCentralClick = viewModel::navigateToCentral,
        onDashboardClick = viewModel::navigateToDashboard,
        onFixedPriceClick = viewModel::navigateToFixedPrice,
        onRoofLightClick = viewModel::onRoofLightClicked,
    )

    when (uiState.dialog) {
        HomeDialogSpec.LocationConfirmDeactivate -> LocationDialog(
            title = context.getString(R.string.confirm_deactivate_location),
            confirmText = context.getString(android.R.string.ok),
            cancelText = context.getString(android.R.string.cancel),
            onConfirm = {
                viewModel.confirmDeactivateLocation()
                viewModel.clearDialog()
            },
            onDismiss = viewModel::clearDialog
        )

        HomeDialogSpec.LocationConfirmActivate -> LocationDialog(
            title = context.getString(R.string.confirm_activate_location),
            confirmText = context.getString(android.R.string.ok),
            cancelText = context.getString(android.R.string.cancel),
            onConfirm = {
                viewModel.confirmActivateLocation()
                viewModel.clearDialog()
            },
            onDismiss = viewModel::clearDialog
        )

        HomeDialogSpec.RoofLightConfirm -> RoofLightDialog(
            onConfirmOn = {
                viewModel.setRoofLightOn()
                viewModel.clearDialog()
            },
            onConfirmOff = {
                viewModel.setRoofLightOff()
                viewModel.clearDialog()
            },
            onDismiss = viewModel::clearDialog
        )

        HomeDialogSpec.ManualTripConfirm -> ManualTripDialog(
            onConfirm = {
                viewModel.confirmManualTrip()
                viewModel.clearDialog()
            },
            onDismiss = viewModel::clearDialog
        )

        HomeDialogSpec.PendingTripsInfo -> PendingTripsDialog(
            onOpen = {
                navController.navigate(HomeRoute.PendingTrips.route)
                viewModel.clearDialog()
            },
            onDismiss = viewModel::clearDialog
        )

        null -> Unit
        else -> Unit
    }
}


