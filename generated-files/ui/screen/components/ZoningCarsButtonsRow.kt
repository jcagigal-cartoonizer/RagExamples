package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.ZoningCarsButtonsRow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 555-6: import androidx.compose.foundation.layout.*
@Composable
fun ZoningCarsButtonsRow(
    state: ZoningCarsButtonsState,
    onBackClick: () -> Unit,
    onCloseClick: () -> Unit,
    onPendingClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (state.showBackButton) {
            CustomButtonLike(
                text = "Back",
                style = state.backButtonStyle,
                color = state.backButtonColor,
                onClick = onBackClick,
                modifier = Modifier.weight(1f)
            )
        }
        if (state.showCloseButton) {
            CustomButtonLike(
                text = "Close",
                style = state.closeButtonStyle,
                color = state.closeButtonColor,
                onClick = onCloseClick,
                modifier = Modifier.weight(1f)
            )
        }
        if (state.showPendingButton) {
            CustomButtonLike(
                text = "Pending",
                style = state.pendingButtonStyle,
                color = state.pendingButtonColor,
                onClick = onPendingClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
