package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.GPSTestButtonsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 62-2: import androidx.compose.runtime.Immutable
@Immutable
data class GPSTestButtonsState(
    val accept: ButtonState = ButtonState(
        text = "Accept",
        enabled = true,
        visible = true,
        containerColor = ComposeColor(0xFF2E7D32),
        contentColor = ComposeColor.White
    ),
    val gps: ButtonState = ButtonState(
        text = "GPS",
        enabled = true,
        visible = true,
        containerColor = ComposeColor(0xFF1565C0),
        contentColor = ComposeColor.White
    )
) {
    @Immutable
    data class ButtonState(
        val text: String,
        val enabled: Boolean,
        val visible: Boolean,
        val containerColor: ComposeColor,
        val contentColor: ComposeColor,
        val borderColor: ComposeColor? = null
    )
}
