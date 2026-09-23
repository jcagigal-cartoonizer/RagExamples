package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.TccButtonsState
import ifac.td.taxi.ui.screen.components.TccUiState
import ifac.td.taxi.ui.screen.components.TccButtonState = TccButtonState
import ifac.td.taxi.ui.screen.components.TccButtonsState = TccButtonsState
import ifac.td.taxi.ui.screen.components.TccButtonState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 15-1: import ifac.td.taxi.domain.model.Dispatch
data class TccUiState(
    val isLoading: Boolean = false,
    val dispatch: Dispatch? = null,
    val buttonsState: TccButtonsState = TccButtonsState(),
    val dialogState: TccDialogState = TccDialogState.Hidden,
    val errorMessageRes: Int? = null
)
data class TccButtonsState(
    val accept: TccButtonState = TccButtonState(),
    val cancel: TccButtonState = TccButtonState(),
    val attributes1: AttributeSpinnerState = AttributeSpinnerState(),
    val attributes2: AttributeSpinnerState = AttributeSpinnerState(),
    val attributes3: AttributeSpinnerState = AttributeSpinnerState(),
    val attributes4: AttributeSpinnerState = AttributeSpinnerState(),
)
data class TccButtonState(
    val visible: Boolean = true,
    val enabled: Boolean = true,
    val loading: Boolean = false,
    val textRes: Int? = null
)
data class AttributeSpinnerState(
    val visible: Boolean = false,
    val title: String = "",
    val value: Int = 1
)
sealed interface TccDialogState {
    data object Hidden : TccDialogState
    data class ConfirmAccept(val dispatchTitle: String? = null) : TccDialogState
    data class Error(val message: String) : TccDialogState
}
sealed interface TccUiEffect {
    data object NavigateBack : TccUiEffect
    data class NavigateToSignature(val dispatchId: Long) : TccUiEffect
    data class NavigateToVoucher(val dispatchId: Long) : TccUiEffect
    data class NavigateToQr(val dispatchId: Long) : TccUiEffect
    data class FinishTcc(
        val tripId: Long?,
        val dispatch: InfoDispatchModel?,
    ) : TccUiEffect
    data class ToastRes(val resId: Int) : TccUiEffect
}
