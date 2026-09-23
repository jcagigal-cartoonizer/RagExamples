package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.TripHistoryButtonsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 263-3: import androidx.compose.runtime.Immutable
@Immutable
data class TripHistoryButtonsState(
    val backVisible: Boolean = true,
    val allVisible: Boolean = true,
    val deleteVisible: Boolean = true,
    val backEnabled: Boolean = true,
    val allEnabled: Boolean = true,
    val deleteEnabled: Boolean = true,
    val backColors: ButtonColorTokens = ButtonColorTokens.primary(),
    val allColors: ButtonColorTokens = ButtonColorTokens.secondary(),
    val deleteColors: ButtonColorTokens = ButtonColorTokens.danger(),
) {
    companion object {
        fun from(
            hasTrips: Boolean,
            hasSelection: Boolean
        ): TripHistoryButtonsState {
            return TripHistoryButtonsState(
                backVisible = true,
                allVisible = hasTrips,
                deleteVisible = hasTrips,
                backEnabled = true,
                allEnabled = hasTrips,
                deleteEnabled = hasSelection,
                backColors = ButtonColorTokens.primary(),
                allColors = ButtonColorTokens.secondary(enabled = hasTrips),
                deleteColors = ButtonColorTokens.danger(enabled = hasSelection),
            )
        }
    }
}
@Immutable
data class ButtonColorTokens(
    val container: Color,
    val content: Color,
    val disabledContainer: Color,
    val disabledContent: Color,
) {
    companion object {
        fun primary(enabled: Boolean = true) = ButtonColorTokens(
            container = Color(0xFF1976D2),
            content = Color.White,
            disabledContainer = Color(0xFFB0BEC5),
            disabledContent = Color(0xFFFAFAFA),
        )
        fun secondary(enabled: Boolean = true) = ButtonColorTokens(
            container = Color(0xFF455A64),
            content = Color.White,
            disabledContainer = Color(0xFFB0BEC5),
            disabledContent = Color(0xFFFAFAFA),
        )
        fun danger(enabled: Boolean = true) = ButtonColorTokens(
            container = Color(0xFFD32F2F),
            content = Color.White,
            disabledContainer = Color(0xFFB0BEC5),
            disabledContent = Color(0xFFFAFAFA),
        )
    }
}
> If you have exact XML colors, replace the hardcoded colors above with your app’s color values. This structure is what you asked for: a full state holder mirroring visibility/color behavior.
