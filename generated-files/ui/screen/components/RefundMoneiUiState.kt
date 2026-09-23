package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.RefundMoneiUiState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 4-1: import ifac.td.taxi.viewmodel.PaymentMoneiViewModel
data class RefundMoneiUiState(
    val isLoading: Boolean = false,
    val amountText: String = "",
    val status: PaymentMoneiViewModel.StatusPayments = PaymentMoneiViewModel.StatusPayments.SUCCEEDED,
    val showConfirmDialog: Boolean = false,
    val showErrorDialog: Boolean = false,
    val errorMessage: String? = null,
    val canRefund: Boolean = true,
    val canCancel: Boolean = true,
)
sealed interface RefundMoneiUiEvent {
    data object OnCancelClicked : RefundMoneiUiEvent
    data object OnRefundClicked : RefundMoneiUiEvent
    data object OnDismissDialog : RefundMoneiUiEvent
    data object OnConfirmRefund : RefundMoneiUiEvent
}
sealed interface RefundMoneiUiEffect {
    data object NavigateBack : RefundMoneiUiEffect
    data object StartPolling : RefundMoneiUiEffect
    data object StopPolling : RefundMoneiUiEffect
    data class ShowSnackbar(val message: String) : RefundMoneiUiEffect
}
