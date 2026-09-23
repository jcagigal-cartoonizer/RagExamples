package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.ui.window.Dialog
// # Block 419-7: import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Immutable
@Immutable
data class AboutCustomDialogState(
    val title: String,
    val message: String,
    val confirmText: String,
    val dismissText: String? = null,
    val showDismiss: Boolean = false
)
