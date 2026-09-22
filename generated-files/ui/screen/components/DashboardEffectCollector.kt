package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.runtime.Composable
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
import androidx.compose.foundation.layout.*
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
