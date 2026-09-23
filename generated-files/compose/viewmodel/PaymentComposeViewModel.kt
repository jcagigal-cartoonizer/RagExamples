package ifac.td.taxi.compose.viewmodel
import ifac.td.taxi.ui.screen.components.PaymentUiEvent
import ifac.td.taxi.ui.screen.components.PaymentButtonsState
import ifac.td.taxi.ui.screen.components.PaymentScreenUiState
import ifac.td.taxi.ui.screen.components.PaymentUiEffect
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 135-3: import android.app.Application
class PaymentComposeViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(PaymentScreenUiState())
    val uiState = _uiState.asStateFlow()
    private val _uiEffect = MutableSharedFlow<PaymentUiEffect>(extraBufferCapacity = 64)
    val uiEffect = _uiEffect.asSharedFlow()
    fun onEvent(event: PaymentUiEvent) {
        when (event) {
            PaymentUiEvent.OnCreate -> {
                // init state here
            }
            PaymentUiEvent.OnResume -> {
                // refresh state if needed
            }
            PaymentUiEvent.OnCardClicked -> handlePayment(PaymentMethod.CARD)
            PaymentUiEvent.OnCashClicked -> handlePayment(PaymentMethod.CASH)
            PaymentUiEvent.OnSubscriberClicked -> handlePayment(PaymentMethod.SUBSCRIBER)
            PaymentUiEvent.OnBizumClicked -> {
                // navigation handled by effect
            }
            PaymentUiEvent.OnAddAmountClicked -> {
                // emit navigation effect to add amount
            }
            PaymentUiEvent.OnCancelAppPaymentClicked -> {
                _uiState.value = _uiState.value.copy(showAppPaymentLabel = false)
            }
            PaymentUiEvent.OnAppPaymentClicked -> {
                viewModelScope.launch { _uiEffect.emit(PaymentUiEffect.RequestAppPaymentAuth(currentAmount())) }
            }
            PaymentUiEvent.OnOthersPaymentClicked -> {
                _uiState.value = _uiState.value.copy(showDialog = PaymentDialogState(
                    title = getString(R.string.dialog_app_payment_title),
                    description = getString(R.string.app_payment_change),
                    buttons = listOf(PaymentDialogButton.CANCEL, PaymentDialogButton.ACCEPT),
                    isCancellable = false
                ))
                viewModelScope.launch {
                    _uiEffect.emit(PaymentUiEffect.OpenDialog(_uiState.value.showDialog!!))
                }
            }
            PaymentUiEvent.OnBackToDispatchClicked -> {
                viewModelScope.launch { _uiEffect.emit(PaymentUiEffect.BackToDispatched) }
            }
            PaymentUiEvent.OnLocutionClicked -> { }
            PaymentUiEvent.OnCourtesyLightClicked -> { }
            PaymentUiEvent.OnUvLightClicked -> { }
            is PaymentUiEvent.OnDialogButtonClicked -> {
                when (event.button) {
                    PaymentDialogButton.ACCEPT -> {
                        // consume and continue
                    }
                    PaymentDialogButton.RETRY -> { }
                    PaymentDialogButton.CANCEL -> { }
                    PaymentDialogButton.OTHERS -> { }
                }
            }
            is PaymentUiEvent.OnDialogOptionSelected -> Unit
            is PaymentUiEvent.OnPaymentResult -> {
                if (event.success) {
                    // handle success
                }
            }
        }
    }
    fun setButtons(state: PaymentButtonsState) {
        _uiState.value = _uiState.value.copy(buttons = state)
    }
    fun setAmount(amountText: String) {
        _uiState.value = _uiState.value.copy(amountText = amountText)
    }
    fun setRedSysState(state: RedSysPaymentState) {
        _uiState.value = _uiState.value.copy(redSysState = state)
        viewModelScope.launch { _uiEffect.emit(PaymentUiEffect.ShowRedSysPopup(state)) }
    }
    fun showDialog(dialog: PaymentDialogState) {
        _uiState.value = _uiState.value.copy(showDialog = dialog)
        viewModelScope.launch { _uiEffect.emit(PaymentUiEffect.OpenDialog(dialog)) }
    }
    fun handlePayment(method: PaymentMethod) {
        when (method) {
            PaymentMethod.CARD -> {}
            PaymentMethod.CASH -> {}
            PaymentMethod.SUBSCRIBER -> {}
            else -> Unit
        }
    }
    fun currentAmount(): Int = 0
    fun getString(resId: Int): String = getApplication<Application>().getString(resId)
}
