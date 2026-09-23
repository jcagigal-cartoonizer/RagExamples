package ifac.td.taxi.ui.screen
import ifac.td.taxi.compose.viewmodel.ZoningServicesComposeViewModel
import ifac.td.taxi.ui.screen.components.ZoningServicesScreen
import ifac.td.taxi.ui.screen.components.ZoningServicesUiEvent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
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
            ZoningServicesCustomDialog(
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
