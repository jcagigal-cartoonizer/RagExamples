package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.DashboardScreen
import ifac.td.taxi.compose.viewmodel.DashboardComposeViewModel
import ifac.td.taxi.ui.screen.components.DashboardUiEffect
import ifac.td.taxi.ui.screen.components.DashboardUiEvent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 6-1: import androidx.compose.foundation.background
@Composable
fun DashboardRoute(
    viewModel: DashboardComposeViewModel,
    showHeader: (Boolean) -> Unit,
    navigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // Start/stop lifecycle-style work similar to onResume/onPause
    LifecycleStartEffect(viewModel) {
        showHeader(true)
        viewModel.onEvent(DashboardUiEvent.OnResume)
        onStopOrDispose {
            viewModel.onEvent(DashboardUiEvent.OnPause)
        }
    }
    DashboardEffectCollector(
        viewModel = viewModel,
        onNavigateBack = navigateBack
    )
    DashboardScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent
    )
}
@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    onEvent: (DashboardUiEvent) -> Unit,
) {
    var dialogState by remember { mutableStateOf<DashboardDialogState?>(null) }
    LaunchedEffect(Unit) {
        onEvent(DashboardUiEvent.ScreenShown)
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            DashboardHeader(
                state = uiState.buttonsState,
                onEvent = onEvent
            )
            Divider()
            Row(modifier = Modifier.fillMaxSize()) {
                // Nearby zones
                Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    Text(
                        text = "Nearby zones",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                    LazyColumn {
                        items(uiState.nearbyZones) { zone ->
                            Text(
                                text = zone.displayName,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
                // Far zones
                Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    Text(
                        text = "Far zones",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                    LazyColumn {
                        items(uiState.farZones) { zone ->
                            Text(
                                text = zone.displayName,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
        if (uiState.pendingTrips.isNotEmpty()) {
            PendingTripsPanel(
                pendingTrips = uiState.pendingTrips,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            )
        }
    }
    if (dialogState != null) {
        DashboardCustomDialog(
            dialogState = dialogState!!,
            onDismiss = { dialogState = null },
            onConfirm = {
                dialogState?.onConfirm?.invoke()
                dialogState = null
            }
        )
    }
}
