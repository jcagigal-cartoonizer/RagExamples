package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.PendingTripsButtonsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 329-3: import androidx.compose.ui.graphics.Color
data class PendingTripsButtonsState(
    val confirmVisible: Boolean = true,
    val cancelVisible: Boolean = true,
    val confirmEnabled: Boolean = true,
    val cancelEnabled: Boolean = true,
    val confirmText: String = "Accept",
    val cancelText: String = "Cancel",
    val confirmContainerColor: Color = Color(0xFF2E7D32),
    val confirmContentColor: Color = Color.White,
    val cancelContainerColor: Color = Color(0xFFE0E0E0),
    val cancelContentColor: Color = Color(0xFF1F1F1F),
    val confirmDisabledContainerColor: Color = Color(0xFFBDBDBD),
    val confirmDisabledContentColor: Color = Color(0xFF757575),
    val cancelDisabledContainerColor: Color = Color(0xFFF5F5F5),
    val cancelDisabledContentColor: Color = Color(0xFF9E9E9E),
    val dialogButtonsState: PendingTripsDialogButtonsState = PendingTripsDialogButtonsState.default()
) {
    companion object {
        fun default() = PendingTripsButtonsState()
    }
}
data class PendingTripsDialogButtonsState(
    val cancelVisible: Boolean = true,
    val acceptVisible: Boolean = true,
    val cancelEnabled: Boolean = true,
    val acceptEnabled: Boolean = true,
    val cancelContainerColor: Color = Color(0xFFE0E0E0),
    val cancelContentColor: Color = Color(0xFF1F1F1F),
    val acceptContainerColor: Color = Color(0xFF2E7D32),
    val acceptContentColor: Color = Color.White
) {
    companion object {
        fun default() = PendingTripsDialogButtonsState()
    }
}
enum class PendingTripsScreenButtonAction {
    CANCEL,
    ACCEPT
}
enum class PendingTripsDialogButton {
    CANCEL,
    ACCEPT
}
These helpers approximate your custom button component styling and visibility behavior.
