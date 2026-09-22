package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ifac.td.taxi.viewmodel.HeaderButtonState
@Composable
fun DashboardHeaderButton(
    state: HeaderButtonState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!state.visible) return
    Button(
        onClick = onClick,
        enabled = state.enabled,
        modifier = modifier.height(44.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = state.backgroundColor,
            contentColor = state.textColor
        ),
        border = BorderStroke(1.dp, Color.Transparent)
    ) {
        Text(text = state.text, color = state.textColor)
    }
}
// // # Block 425-5: import androidx.compose.foundation.layout.*
// import androidx.compose.foundation.layout.*
// import androidx.compose.runtime.Composable
import ifac.td.taxi.viewmodel.DashboardUiEvent
import ifac.td.taxi.viewmodel.DashboardButtonsState
@Composable
fun DashboardHeader(
    state: DashboardButtonsState,
    onEvent: (DashboardUiEvent) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DashboardHeaderButton(state.onStop, onClick = { /* sort behavior placeholder */ })
            DashboardHeaderButton(state.onZone, onClick = { /* sort behavior placeholder */ })
            DashboardHeaderButton(state.hired, onClick = { onEvent(DashboardUiEvent.LocateOnHiredClicked) })
            DashboardHeaderButton(state.trips, onClick = { onEvent(DashboardUiEvent.PendingTripsClicked) })
        }
    }
}
// // # Block 452-6: import androidx.compose.foundation.background
// // import androidx.compose.foundation.background
// import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
// import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
@Composable
fun DashboardDashboardCustomDialog(
    dialogState: DashboardDialogState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = dialogState.title)
        },
        text = {
            Text(text = dialogState.message)
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
            ) {
                Text(dialogState.confirmText)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB00020))
            ) {
                Text(dialogState.dismissText)
            }
        }
    )
}
// // # Block 503-7: import androidx.compose.runtime.Composable
// import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest
@Composable
fun DashboardUiEffectCollector(
    viewModel: DashboardComposeViewModel,
    onNavigateBack: () -> Unit,
) {
    LaunchedEffect(viewModel) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                DashboardUiEffect.NavigateBack -> onNavigateBack()
                is DashboardUiEffect.ShowDialog -> {
                    // handled by DashboardScreen local dialog state if you wire it through state/effects
                }
                is DashboardUiEffect.ShowToast -> {
                    // hook toast/snackbar here if needed
                }
            }
        }
    }
}
Old fragment behavior -> Compose equivalent:
A few things depend on your project specifics:
1. **Exact `AvailableColumnsEnum` parsing**
2. **Exact custom button visuals**
3. **Exact dialog XML layout**
4. **Pending trips list**
