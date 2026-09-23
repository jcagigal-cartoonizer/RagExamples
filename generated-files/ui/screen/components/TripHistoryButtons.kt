package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.TripHistoryButtons
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 440-5: import androidx.compose.foundation.layout.Arrangement
@Composable
fun TripHistoryButtons(
    state: TripHistoryButtonsState,
    onBackClick: () -> Unit,
    onAllClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (state.backVisible) {
            CustomStyledButton(
                text = "Back",
                enabled = state.backEnabled,
                colors = state.backColors,
                modifier = Modifier.weight(1f),
                onClick = onBackClick
            )
        }
        if (state.allVisible) {
            CustomStyledButton(
                text = "All",
                enabled = state.allEnabled,
                colors = state.allColors,
                modifier = Modifier.weight(1f),
                onClick = onAllClick
            )
        }
        if (state.deleteVisible) {
            CustomStyledButton(
                text = "Delete",
                enabled = state.deleteEnabled,
                colors = state.deleteColors,
                modifier = Modifier.weight(1f),
                onClick = onDeleteClick
            )
        }
    }
}
@Composable
fun CustomStyledButton(
    text: String,
    enabled: Boolean,
    colors: ButtonColorTokens,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        modifier = modifier,
        enabled = enabled,
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (enabled) colors.container else colors.disabledContainer,
            contentColor = if (enabled) colors.content else colors.disabledContent,
            disabledContainerColor = colors.disabledContainer,
            disabledContentColor = colors.disabledContent
        )
    ) {
        Text(text = text)
    }
}
