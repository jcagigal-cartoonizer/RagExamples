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
// # Block 5-1: import androidx.compose.foundation.layout.*
@Composable
fun PendingTripsScreen(
    viewModel: PendingTripsComposeViewModel,
    onNavigateBack: () -> Unit,
    onShowHeader: (Boolean) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // Dialog state
    var dialogState by remember { mutableStateOf<PendingTripsDialogState?>(null) }
    // Effects collector
    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                PendingTripsUiEffect.NavigateBack -> onNavigateBack()
                is PendingTripsUiEffect.ShowToast -> {
                    // Let parent host show toast if needed
                    // Or handle here with Snackbar
                }
                is PendingTripsUiEffect.OpenConfirmDialog -> {
                    dialogState = PendingTripsDialogState(
                        title = effect.title,
                        description = effect.description,
                        pendingTrip = effect.pendingTrip
                    )
                }
            }
        }
    }
    // Screen enter behavior: similar to onResume/setupComponents
    LaunchedEffect(Unit) {
        onShowHeader(true)
        viewModel.onScreenStarted()
    }
    DisposableEffect(Unit) {
        onDispose {
            viewModel.onScreenStopped()
        }
    }
    PendingTripsContent(
        uiState = uiState,
        buttonsState = uiState.buttonsState,
        onTripClick = { tripId ->
            viewModel.onTripClicked(tripId)
        },
        onButtonClick = { buttonAction ->
            viewModel.onButtonAction(buttonAction)
        }
    )
    dialogState?.let { state ->
        PendingTripsCustomDialog(
            title = state.title,
            description = state.description,
            buttonsState = uiState.buttonsState.dialogButtonsState,
            onDismissRequest = { dialogState = null },
            onButtonClick = { button ->
                dialogState?.pendingTrip?.let { trip ->
                    when (button) {
                        PendingTripsDialogButton.CANCEL -> {
                            dialogState = null
                        }
                        PendingTripsDialogButton.ACCEPT -> {
                            viewModel.onConfirmRequestTrip(trip)
                            dialogState = null
                        }
                    }
                }
            }
        )
    }
}
@Composable
fun PendingTripsContent(
    uiState: PendingTripsUiState,
    buttonsState: PendingTripsButtonsState,
    onTripClick: (String) -> Unit,
    onButtonClick: (PendingTripsScreenButtonAction) -> Unit,
) {
    Scaffold(
        topBar = {
            // Put your custom top bar here if needed
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            PendingTripsButtonsRow(
                state = buttonsState,
                onButtonClick = onButtonClick
            )
            if (uiState.pendingTrips.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(text = "No trips")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.pendingTrips, key = { it.tripID ?: it.hashCode().toString() }) { trip ->
                        PendingTripRow(
                            tripId = trip.tripID.orEmpty(),
                            pickupAddress = trip.pickupAddress.orEmpty(),
                            onClick = { onTripClick(trip.tripID.orEmpty()) }
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun PendingTripRow(
    tripId: String,
    pickupAddress: String,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(text = "Trip ID: $tripId", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(text = pickupAddress.ifBlank { "No address" })
        }
    }
}
