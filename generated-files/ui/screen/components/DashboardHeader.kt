package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.state.DashboardButtonsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// // ### Header

@Composable
private fun DashboardHeader(buttonsState: DashboardButtonsState) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        if (buttonsState.showOnStop) Text("On Stop")
        if (buttonsState.showOnZone) Text("On Zone")
        if (buttonsState.showHired) Text("Hired")
        if (buttonsState.showTrips) Text("Trips")
    }
}

