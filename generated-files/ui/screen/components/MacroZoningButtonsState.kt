package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.MacroZoningButtonsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 477-4: import androidx.compose.ui.graphics.Color
data class MacroZoningButtonsState(
    val onStopVisible: Boolean = false,
    val onZoneVisible: Boolean = false,
    val hiredVisible: Boolean = false,
    val tripsVisible: Boolean = false,
    val hiredLabel: String = "Hired",
    val tripsLabel: String = "Trips",
    val placeholderText: String = "Sort by",
    val activeOrder: OrderOptions = OrderOptions.NONE,
    val pendingButton: MacroZoningActionButtonState = MacroZoningActionButtonState.Pending(),
    val preReservationButton: MacroZoningActionButtonState = MacroZoningActionButtonState.PreReservation()
)
sealed class MacroZoningActionButtonState(
    open val enabled: Boolean,
    open val visible: Boolean,
    open val backgroundColor: Color,
    open val contentColor: Color,
    open val disabledBackgroundColor: Color,
    open val disabledContentColor: Color
) {
    data class Pending(
        override val enabled: Boolean = false,
        override val visible: Boolean = true,
        override val backgroundColor: Color = Color(0xFF1976D2), // blue
        override val contentColor: Color = Color.White,
        override val disabledBackgroundColor: Color = Color(0xFFB0BEC5),
        override val disabledContentColor: Color = Color.White
    ) : MacroZoningActionButtonState(
        enabled, visible, backgroundColor, contentColor, disabledBackgroundColor, disabledContentColor
    )
    data class PreReservation(
        override val enabled: Boolean = false,
        override val visible: Boolean = false,
        override val backgroundColor: Color = Color(0xFF1976D2),
        override val contentColor: Color = Color.White,
        override val disabledBackgroundColor: Color = Color(0xFFB0BEC5),
        override val disabledContentColor: Color = Color.White
    ) : MacroZoningActionButtonState(
        enabled, visible, backgroundColor, contentColor, disabledBackgroundColor, disabledContentColor
    )
}
