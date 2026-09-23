package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.ReceiptRedSysDialogState
import ifac.td.taxi.ui.screen.components.ReceiptRedSysButtonsState = ReceiptRedSysButtonsState
import ifac.td.taxi.ui.screen.components.ReceiptRedSysUiState
import ifac.td.taxi.ui.screen.components.ReceiptRedSysDialogButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 12-1: import ifac.td.taxi.domain.model.RedSysOperation
data class ReceiptRedSysUiState(
    val operations: List<RedSysOperation> = emptyList(),
    val isLoading: Boolean = false,
    val dialogState: ReceiptRedSysDialogState? = null,
    val buttonsState: ReceiptRedSysButtonsState = ReceiptRedSysButtonsState()
)
data class ReceiptRedSysDialogState(
    val title: String,
    val description: String,
    val buttons: List<ReceiptRedSysDialogButton>
)
enum class ReceiptRedSysDialogButton {
    DEVOLVER,
    IMPRIMIR,
    ACCEPT
}
sealed interface ReceiptRedSysUiEvent {
    data object ScreenOpened : ReceiptRedSysUiEvent
    data class OperationClicked(val operation: RedSysOperation) : ReceiptRedSysUiEvent
    data class DialogButtonClicked(val button: ReceiptRedSysDialogButton) : ReceiptRedSysUiEvent
    data object DialogDismissed : ReceiptRedSysUiEvent
    data object ReloadRequested : ReceiptRedSysUiEvent
}
sealed interface ReceiptRedSysUiEffect {
    data class ShowDialog(val dialogState: ReceiptRedSysDialogState) : ReceiptRedSysUiEffect
    data class ShowMessage(val message: String) : ReceiptRedSysUiEffect
    data class PrintServiceTicket(val operation: RedSysOperation) : ReceiptRedSysUiEffect
    data class PrintRefundTicket(val operation: RedSysOperation) : ReceiptRedSysUiEffect
}
