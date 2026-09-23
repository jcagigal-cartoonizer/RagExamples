package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 402-3: import ifac.td.taxi.R
data class AddAmountUiState(
    val serviceAmount: Int = 0,
    val extraAmount: Int = 0,
    val tollAmount: Int = 0,
    val tipAmount: Int = 0,
    val totalAmount: Int = 0,
    val serviceAmountText: String = "",
    val extraAmountText: String = "",
    val tollAmountText: String = "",
    val tipAmountText: String = "",
    val totalAmountText: String = "",
    val serviceAmountHint: String = "",
    val extraAmountHint: String = "",
    val tollAmountHint: String = "",
    val tipAmountHint: String = "",
    val buttonsState: AddAmountButtonsState = AddAmountButtonsState()
)
sealed interface AddAmountUiEvent {
    data class Init(val trip: ifac.td.taxi.domain.model.Trip) : AddAmountUiEvent
    data class ServiceAmountChanged(val value: String) : AddAmountUiEvent
    data class ExtraAmountChanged(val value: String) : AddAmountUiEvent
    data class TollAmountChanged(val value: String) : AddAmountUiEvent
    data class TipAmountChanged(val value: String) : AddAmountUiEvent
    data object AcceptClicked : AddAmountUiEvent
    data object CancelClicked : AddAmountUiEvent
    data object DismissDialog : AddAmountUiEvent
    data object DialogAccept : AddAmountUiEvent
}
sealed interface AddAmountUiEffect {
    data object NavigateBack : AddAmountUiEffect
    data class ShowDialog(val dialogState: AddAmountDialogState) : AddAmountUiEffect
}
data class AddAmountDialogState(
    val visible: Boolean = false,
    val title: String = "Warning",
    val descriptionRes: Int = R.string.warning,
    val acceptText: String = "OK"
)
