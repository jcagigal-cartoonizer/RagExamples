package ifac.td.taxi.ui.screen
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
// # Block 11-1: import androidx.compose.foundation.layout.*
@Composable
fun ZoningServicesScreen(
    navController: NavController,
    viewModel: ZoningServicesComposeViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToOnTrip: () -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // Collect one-shot effects lifecycle-aware
    LaunchedEffect(viewModel, lifecycleOwner) {
        viewModel.effects
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .collectLatest { effect ->
                when (effect) {
                    ZoningServicesUiEffect.NavigateBack -> onNavigateBack()
                    ZoningServicesUiEffect.NavigateToHome -> onNavigateToHome()
                    ZoningServicesUiEffect.NavigateToOnTrip -> onNavigateToOnTrip()
                    ZoningServicesUiEffect.ShowCloseDialog -> {
                        viewModel.onEvent(ZoningServicesUiEvent.ShowCloseDialog)
                    }
                    ZoningServicesUiEffect.HideCloseDialog -> {
                        viewModel.onEvent(ZoningServicesUiEvent.HideCloseDialog)
                    }
                }
            }
    }
    val buttonsState = uiState.buttonsState
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = uiState.zoneName,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                textAlign = TextAlign.Center
            )
            ZoningServicesButtonsRow(
                state = buttonsState,
                onShowAllClick = { viewModel.onEvent(ZoningServicesUiEvent.ShowAllClicked) },
                onShowRecentClick = { viewModel.onEvent(ZoningServicesUiEvent.ShowRecentClicked) },
                onCancelClick = { viewModel.onEvent(ZoningServicesUiEvent.CancelClicked) },
                onCloseClick = { viewModel.onEvent(ZoningServicesUiEvent.CloseClicked) },
            )
            LinearProgressIndicator(
                progress = { uiState.refreshProgress / 1000f },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .height(6.dp)
            )
            if (uiState.showLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            if (uiState.zoneTrips.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No cars available")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.zoneTrips, key = { it.id ?: it.hashCode() }) { trip ->
                        ZoneTripRow(trip)
                    }
                }
            }
        }
        if (uiState.dialogState.visible) {
            ZoningServicesCustomDialogCustomDialog(
                state = uiState.dialogState,
                onConfirm = { viewModel.onEvent(ZoningServicesUiEvent.ConfirmDialog) },
                onDismiss = { viewModel.onEvent(ZoningServicesUiEvent.DismissDialog) }
            )
        }
    }
}
@Composable
fun ZoneTripRow(trip: ZoneTripModel) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Trip ID: ${trip.id ?: "-"}")
            Text(text = "Pickup: ${trip.pickupTime ?: "-"}")
            Text(text = "Company: ${trip.company ?: "-"}")
        }
    }
}
