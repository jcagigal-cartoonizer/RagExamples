package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 312-4: import ifac.td.taxi.ui.custom.button.ButtonType
data class ReceiptHistoryDialogState(
    val visible: Boolean = false,
    val title: String = "",
    val description: String = "",
    val buttons: List<ButtonType> = emptyList(),
    val tag: String? = null,
    val redSysUsername: String? = null,
    val redSysPassword: String? = null,
    val isRedSysDialog: Boolean = false
)
