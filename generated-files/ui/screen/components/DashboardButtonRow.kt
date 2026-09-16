package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.state.DashboardButtons
import ifac.td.taxi.ui.screen.state.DashboardButtonsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// // ### Buttons row

@Composable
private fun DashboardButtonsRow(
    buttonsState: DashboardButtonsState,
    pendingTripsCount: Int,
    onLocateClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (buttonsState.locateButtonVisible) {
            DashboardCustomButton(
                text = androidx.compose.ui.res.stringResource(buttonsState.locateButtonTextRes),
                type = buttonsState.locateButtonType,
                enabled = buttonsState.locateButtonEnabled,
                onClick = onLocateClick,
                modifier = Modifier.weight(1f)
            )
        }

        Text(
            text = "Pending: $pendingTripsCount",
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

