package ifac.td.taxi.ui.screen.components
import  ifac.td.taxi.R
import ifac.td.taxi.repository.connections.service.model.*
import androidx.annotation.StringRes
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import ifac.td.taxi.compose.viewmodel.*
import androidx.navigation.NavController
import ifac.td.taxi.viewmodel.MainActivityViewModel
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.platform.*
import androidx.core.net.toUri
import androidx.navigation.*
import ifac.td.taxi.ui.screen.components.*
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.*
import com.interfacom.sdk.taximeter.bravocomm.*
import ifac.td.taxi.domain.model.*
import ifac.td.taxi.domain.usecase.*
import ifac.td.taxi.framework.sdk.bravocentral.usecase.*
import ifac.td.taxi.framework.sdk.usecase.*
import ifac.td.taxi.repository.room.entities.*
import ifac.td.taxi.repository.room.entities.countdown.*
import ifac.td.taxi.repository.room.entities.message.*
import ifac.td.taxi.viewmodel.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
