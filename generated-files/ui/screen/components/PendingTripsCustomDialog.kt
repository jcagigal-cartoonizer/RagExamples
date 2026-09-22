package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ifac.td.taxi.viewmodel.PendingTripsUiEffect
import ifac.td.taxi.viewmodel.PendingTripsUiEvent
import ifac.td.taxi.viewmodel.PendingTripsUiState
import ifac.td.taxi.viewmodel.PendingTripsViewModel
import kotlinx.coroutines.flow.collectLatest
@Composable
fun PendingTripsScreen(
    viewModel: PendingTripsComposeViewModel,
    onNavigateBack: () -> Unit,
    onShowHeader: (Boolean) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var dialogState by remember { mutableStateOf<PendingTripsDialogState?>(null) }
    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                PendingTripsUiEffect.NavigateBack -> onNavigateBack()
                is PendingTripsUiEffect.ShowToast -> {
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
