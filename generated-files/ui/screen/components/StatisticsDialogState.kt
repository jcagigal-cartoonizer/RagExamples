package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.StatisticsUiEvent
import ifac.td.taxi.ui.screen.components.StatisticsButtons
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 405-7: import androidx.compose.foundation.layout.Row
@Composable
fun StatisticsButtons(
    state: StatisticsButtonsState,
    onEvent: (StatisticsUiEvent) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatisticsStyledButton(
            text = "Billing",
            state = state.billing,
            onClick = { onEvent(StatisticsUiEvent.BillingClicked) }
        )
        StatisticsStyledButton(
            text = "Time",
            state = state.time,
            onClick = { onEvent(StatisticsUiEvent.TimeClicked) }
        )
    }
}
Because the XML and Kotlin source weren’t included verbatim, below is a clean Compose equivalent that follows the usual custom dialog structure:
data class StatisticsDialogState(
    val title: String,
    val message: String,
    val confirmText: String = "OK",
    val dismissText: String = "Cancel",
)
