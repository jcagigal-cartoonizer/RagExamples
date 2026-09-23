package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.DashboardUiEvent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 425-5: import androidx.compose.foundation.layout.*
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
