package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.PortugalSettingsUiState
import ifac.td.taxi.ui.screen.components.PortugalSettingsButtonsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 316-4: import androidx.compose.runtime.Immutable
@Immutable
data class PortugalSettingsButtonsState(
    val acceptEnabled: Boolean,
    val acceptVisible: Boolean = true,
    val cancelVisible: Boolean = true,
    val changePinVisible: Boolean = true,
    val acceptContainerColor: Color,
    val acceptContentColor: Color,
    val cancelContainerColor: Color,
    val cancelContentColor: Color,
    val changePinContainerColor: Color,
    val changePinContentColor: Color,
) {
    companion object {
        fun fromUiState(state: PortugalSettingsUiState): PortugalSettingsButtonsState {
            // This mirrors typical XML behavior:
            // - Accept always visible
            // - Cancel always visible
            // - Change PIN visible
            // - When reset hash is active, Accept remains enabled but may be blocked by validation
            return PortugalSettingsButtonsState(
                acceptEnabled = true,
                acceptVisible = true,
                cancelVisible = true,
                changePinVisible = true,
                acceptContainerColor = PortugalButtonColors.AcceptContainer,
                acceptContentColor = PortugalButtonColors.AcceptContent,
                cancelContainerColor = PortugalButtonColors.CancelContainer,
                cancelContentColor = PortugalButtonColors.CancelContent,
                changePinContainerColor = PortugalButtonColors.ChangePinContainer,
                changePinContentColor = PortugalButtonColors.ChangePinContent,
            )
        }
    }
}
object PortugalButtonColors {
    val AcceptContainer = Color(0xFF2E7D32)
    val AcceptContent = Color.White
    val CancelContainer = Color(0xFF757575)
    val CancelContent = Color.White
    val ChangePinContainer = Color(0xFF1565C0)
    val ChangePinContent = Color.White
}
