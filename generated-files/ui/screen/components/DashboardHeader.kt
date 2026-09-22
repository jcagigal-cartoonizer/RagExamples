package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.LifecycleEventEffect
import ifac.td.taxi.viewmodel.DashboardEffectCollector
import ifac.td.taxi.viewmodel.DashboardUiEffect
import ifac.td.taxi.viewmodel.DashboardUiEvent
import ifac.td.taxi.viewmodel.DashboardUiState
import ifac.td.taxi.viewmodel.DashboardViewModel
@Composable
fun DashboardRoute(
    viewModel: DashboardComposeViewModel,
    showHeader: (Boolean) -> Unit,
    navigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
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
