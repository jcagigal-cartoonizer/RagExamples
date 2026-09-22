package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// // # Block 4-1: import androidx.compose.runtime.Immutable
// import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
@Immutable
data class ChangeDriverPinUiState(
    val currentDriverNumber: String = "",
    val currentPin: String = "",
    val newPin: String = "",
    val repeatNewPin: String = "",
    val isLoading: Boolean = false,
    val buttonsState: ChangeDriverPinButtonsState = ChangeDriverPinButtonsState()
)
sealed interface ChangeDriverPinUiEvent {
    data class CurrentDriverNumberChanged(val value: String) : ChangeDriverPinUiEvent
    data class CurrentPinChanged(val value: String) : ChangeDriverPinUiEvent
    data class NewPinChanged(val value: String) : ChangeDriverPinUiEvent
    data class RepeatNewPinChanged(val value: String) : ChangeDriverPinUiEvent
    data object AcceptClicked : ChangeDriverPinUiEvent
    data object CancelClicked : ChangeDriverPinUiEvent
    data object DialogConfirmed : ChangeDriverPinUiEvent
    data object DialogDismissed : ChangeDriverPinUiEvent
}
sealed interface ChangeDriverPinUiEffect {
    data object NavigateBack : ChangeDriverPinUiEffect
    data class ShowDialog(
        val dialog: CustomDialogState
    ) : ChangeDriverPinUiEffect
    data object RequestUserPresenter : ChangeDriverPinUiEffect
    data class ChangePin(
        val driverNumber: String,
        val oldPin: String,
        val newPin: String
    ) : ChangeDriverPinUiEffect
}
@Immutable
data class ChangeDriverPinButtonsState(
    val accept: ChangeDriverPinButtonVisualState = ChangeDriverPinButtonVisualState.primary(),
    val cancel: ChangeDriverPinButtonVisualState = ChangeDriverPinButtonVisualState.secondary()
)
@Immutable
data class ChangeDriverPinButtonVisualState(
    val text: String,
    val enabled: Boolean = true,
    val visible: Boolean = true,
    val backgroundColor: Long,
    val contentColor: Long,
    val borderColor: Long? = null
) {
    companion object {
        fun primary(
            text: String = "Accept",
            enabled: Boolean = true,
            visible: Boolean = true
        ) = ChangeDriverPinButtonVisualState(
            text = text,
            enabled = enabled,
            visible = visible,
            backgroundColor = 0xFF1E88E5,
            contentColor = 0xFFFFFFFF,
            borderColor = null
        )
        fun secondary(
            text: String = "Cancel",
            enabled: Boolean = true,
            visible: Boolean = true
        ) = ChangeDriverPinButtonVisualState(
            text = text,
            enabled = enabled,
            visible = visible,
            backgroundColor = 0xFFFFFFFF,
            contentColor = 0xFF1E88E5,
            borderColor = 0xFF1E88E5
        )
    }
}
@Immutable
data class ChangeDriverPinCustomDialogState(
    val title: String,
    val buttons: List<ChangeDriverPinDialogButtonType> = listOf(ChangeDriverPinDialogButtonType.Accept)
)
enum class ChangeDriverPinDialogButtonType {
    Accept,
    Cancel
}
