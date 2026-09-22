package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.ui.window.Dialog
import androidx.annotation.StringRes
data class OpenPartialUiState(
    val isLoading: Boolean = true,
    val rawPartialContent: String? = null,
    val ticketContent: String = "",
    val buttons: OpenPartialButtonsState = OpenPartialButtonsState(),
    val dialog: OpenPartialDialogState? = null,
)
data class OpenPartialButtonsState(
    val back: ButtonUiState = ButtonUiState.visibleEnabled(),
    val print: ButtonUiState = ButtonUiState.visibleEnabled(),
    val close: ButtonUiState = ButtonUiState.gone(),
    val totalizers: ButtonUiState = ButtonUiState.gone(),
)
data class ButtonUiState(
    val visible: Boolean,
    val enabled: Boolean,
    val style: ButtonStyle,
) {
    companion object {
        fun visibleEnabled(style: ButtonStyle = ButtonStyle.ENABLE) =
            ButtonUiState(visible = true, enabled = true, style = style)
        fun visibleDisabled(style: ButtonStyle = ButtonStyle.DISABLE) =
            ButtonUiState(visible = true, enabled = false, style = style)
        fun gone() =
            ButtonUiState(visible = false, enabled = false, style = ButtonStyle.DISABLE)
    }
}
enum class ButtonStyle {
    ENABLE, DISABLE
}
data class OpenPartialDialogState(
    @StringRes val titleRes: Int = R.string.alert,
    @StringRes val messageRes: Int,
    val buttons: List<OpenPartialDialogButtonType> = listOf(OpenPartialDialogButtonType.CANCEL, OpenPartialDialogButtonType.ACCEPT)
)
enum class OpenPartialDialogButtonType {
    CANCEL, ACCEPT
}
sealed interface OpenPartialUiEvent {
    data object OnBackClicked : OpenPartialUiEvent
    data object OnPrintClicked : OpenPartialUiEvent
    data object OnCloseClicked : OpenPartialUiEvent
    data object OnTotalizersClicked : OpenPartialUiEvent
    data object OnDialogDismissed : OpenPartialUiEvent
    data object OnDialogAccepted : OpenPartialUiEvent
}
sealed interface OpenPartialUiEffect {
    data object NavigateBack : OpenPartialUiEffect
    data object NavigateToTotalizers : OpenPartialUiEffect
    data object ShowToastNoPartialsToPrint : OpenPartialUiEffect
    data object RequestClosePartialsConfirmation : OpenPartialUiEffect
    data object ClosePartialsAndNavigate : OpenPartialUiEffect
}
