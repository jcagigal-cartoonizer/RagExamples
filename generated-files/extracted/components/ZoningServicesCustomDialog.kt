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
import  androidx.compose.ui.window.Dialog
// # Block 504-6: import androidx.compose.foundation.layout.*
@Composable
fun ZoningServicesCustomDialogCustomDialog(
    state: ZoningServicesCustomDialogCustomDialogState,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = state.title)
        },
        text = {
            Text(text = state.message)
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(text = state.confirmText)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(text = state.dismissText)
            }
        }
    )
}
Your old fragment had:
That is now represented in `ZoningServicesButtonsState`.
is now consolidated into `ZoningServicesUiState`.
@Composable
fun ZoningServicesRoute(
    navController: NavController,
    viewModel: ZoningServicesComposeViewModel,
    idMacroZone: Int,
    idZone: Int,
) {
    LaunchedEffect(idMacroZone, idZone) {
        viewModel.onEvent(ZoningServicesUiEvent.Init(idMacroZone, idZone))
    }
    ZoningServicesScreen(
        navController = navController,
        viewModel = viewModel,
        onNavigateBack = { navController.popBackStack() },
        onNavigateToHome = {
            navController.navigate(
                ifac.td.taxi.ui.screen.ZoningServicesFragmentDirections
                    .actionZoningServicesFragmentToHomeFragment()
            )
        },
        onNavigateToOnTrip = {
            navController.navigate(ifac.td.taxi.HomeDirections.goToOnTripFragment())
        }
    )
}
To match your XML even more precisely, the next step would be:
