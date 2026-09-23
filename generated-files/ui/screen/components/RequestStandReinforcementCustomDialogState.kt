package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.RequestStandReinforcementCustomDialogState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.ui.window.Dialog
// # Block 410-6: import androidx.compose.runtime.Immutable
@Immutable
data class RequestStandReinforcementCustomDialogState(
    val isVisible: Boolean = false,
    val title: String = "",
    val message: String = "",
    val confirmText: String = "OK",
    val dismissText: String = "Cancel"
)
