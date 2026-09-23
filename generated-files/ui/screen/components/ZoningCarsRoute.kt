package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.ZoningCarsUiEvent
import ifac.td.taxi.compose.viewmodel.ZoningCarsComposeViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 13-1: import androidx.compose.runtime.*
@Composable
fun ZoningCarsRoute(
    viewModel: ZoningCarsComposeViewModel,
    navController: NavController,
    idMacroZone: Int,
    idZone: Int,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val buttonsState by viewModel.buttonsState.collectAsStateWithLifecycle()
    val dialogState by viewModel.dialogState.collectAsStateWithLifecycle()
    LaunchedEffect(idMacroZone, idZone) {
        viewModel.onEvent(ZoningCarsUiEvent.Init(idMacroZone, idZone))
    }
    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                ZoningCarsEffect.NavigateBack -> navController.popBackStack()
                ZoningCarsEffect.NavigateToHome -> {
                    navController.navigate(
                        R.id.action_zoningCarsFragment_to_homeFragment
                    )
                }
                ZoningCarsEffect.NavigateToOnTrip -> {
                    navController.navigate(
                        HomeDirections.goToOnTripFragment()
                    )
                }
                ZoningCarsEffect.NavigateToPendingTrips -> {
                    navController.navigate(
                        R.id.action_zoningCarsFragment_to_pendingTripsFragment
                    )
                }
                is ZoningCarsEffect.ShowToast -> {
                    // Handle in your host as needed; or emit to a snackbar host.
                }
                ZoningCarsEffect.OpenLocateOnHiredDialog -> {
                    viewModel.onEvent(ZoningCarsUiEvent.ShowLocateOnHiredDialog)
                }
                ZoningCarsEffect.OpenDelocateOnHiredDialog -> {
                    viewModel.onEvent(ZoningCarsUiEvent.ShowDelocateOnHiredDialog)
                }
            }
        }
    }
    ZoningCarsScreen(
        uiState = uiState,
        buttonsState = buttonsState,
        dialogState = dialogState,
        onEvent = viewModel::onEvent,
        onDismissDialog = { viewModel.onEvent(ZoningCarsUiEvent.DismissDialog) },
        onDialogButton = { button ->
            viewModel.onEvent(ZoningCarsUiEvent.DialogButtonClicked(button))
        }
    )
}
