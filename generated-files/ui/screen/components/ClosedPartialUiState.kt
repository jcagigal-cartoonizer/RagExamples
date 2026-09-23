package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 11-1: import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
@Immutable
data class ClosedPartialUiState(
    val isLoading: Boolean = false,
    val ticketContent: String = "",
    val buttonsState: ClosedPartialButtonsState = ClosedPartialButtonsState(),
    val dialog: ClosedPartialDialogState? = null
)
sealed interface ClosedPartialUiEffect {
    data object NavigateBack : ClosedPartialUiEffect
    data class NavigateToTotalizers(val destinationId: Int) : ClosedPartialUiEffect
    data object PrintPartial : ClosedPartialUiEffect
    data object CloseDialog : ClosedPartialUiEffect
}
@Immutable
data class ClosedPartialDialogState(
    val title: String,
    val message: String,
    val confirmText: String = "OK",
    val dismissText: String? = null
)
@Immutable
data class ClosedPartialButtonsState(
    val cancel: ClosedPartialButtonStyle = ClosedPartialButtonStyle.Enabled,
    val print: ClosedPartialButtonStyle = ClosedPartialButtonStyle.Enabled,
    val totalizers: ClosedPartialButtonStyle = ClosedPartialButtonStyle.Hidden
) {
    val showTotalizers: Boolean get() = totalizers !is ClosedPartialButtonStyle.Hidden
}
sealed interface ClosedPartialButtonStyle {
    data object Hidden : ClosedPartialButtonStyle
    data object Disabled : ClosedPartialButtonStyle
    data object Enabled : ClosedPartialButtonStyle
}
