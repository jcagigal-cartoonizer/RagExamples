package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// import androidx.compose.ui.graphics.Color
import ifac.td.taxi.domain.model.PaymentMethod
import ifac.td.taxi.viewmodel.model.RedSysPaymentState
enum class PaymentButtonStyle {
    ENABLE,
    DISABLE,
    LOADING
}
data class PaymentButtonUiState(
    val visible: Boolean = true,
    val enabled: Boolean = true,
    val style: PaymentButtonStyle = PaymentButtonStyle.ENABLE,
    val text: String,
    @DrawableRes val iconRes: Int? = null
)
data class PaymentButtonsState(
    val card: PaymentButtonUiState = PaymentButtonUiState(text = "Card"),
    val cash: PaymentButtonUiState = PaymentButtonUiState(text = "Cash"),
    val subscriber: PaymentButtonUiState = PaymentButtonUiState(text = "Subscriber"),
    val bizum: PaymentButtonUiState = PaymentButtonUiState(text = "Bizum"),
    val appPayment: PaymentButtonUiState = PaymentButtonUiState(visible = false, text = "Pay in App"),
    val othersPayment: PaymentButtonUiState = PaymentButtonUiState(visible = false, text = "Others"),
    val cancel: PaymentButtonUiState = PaymentButtonUiState(visible = false, text = "Cancel"),
    val addAmount: PaymentButtonUiState = PaymentButtonUiState(text = "Add amount"),
    val backToDispatch: PaymentButtonUiState = PaymentButtonUiState(visible = false, text = "Back"),
    val coutesyLight: PaymentButtonUiState = PaymentButtonUiState(visible = false, text = "Light"),
    val uvLight: PaymentButtonUiState = PaymentButtonUiState(visible = false, text = "UV"),
    val locution: PaymentButtonUiState = PaymentButtonUiState(visible = false, text = "Locution"),
) {
    companion object {
        fun initial() = PaymentButtonsState()
    }
}
data class PaymentDialogState(
    val title: String,
    val description: String,
    val buttons: List<PaymentDialogButton>,
    val isCancellable: Boolean = true,
    val centerText: Boolean = false,
    val showCheckBox: Boolean = false,
    val checkBoxText: String? = null,
    val listOptions: List<PaymentDialogOption> = emptyList(),
    val iconRes: Int? = null,
    val editTextHint: String? = null,
    val editTextMaxLength: Int? = null,
    val tag: String? = null
)
data class PaymentDialogOption(
    val id: Int,
    val title: String
)
enum class PaymentDialogButton {
    CANCEL,
    ACCEPT,
    RETRY,
    OTHERS
}
data class PaymentScreenUiState(
    val amountText: String = "",
    val buttons: PaymentButtonsState = PaymentButtonsState.initial(),
    val showBottomMenu: Boolean = true,
    val showAppPaymentLabel: Boolean = false,
    val showExtraButtons: Boolean = true,
    val showDialog: PaymentDialogState? = null,
    val redSysState: RedSysPaymentState = RedSysPaymentState.IDLE,
    val timerText: String? = null,
    val showCourtesyLight: Boolean = false,
    val showUvLight: Boolean = false,
    val showLocution: Boolean = false,
    val waitingAppPaymentResponse: Boolean = true,
    val isLandscape: Boolean = false
)
