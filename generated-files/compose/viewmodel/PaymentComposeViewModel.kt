package ifac.td.taxi.compose.viewmodel
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ifac.td.taxi.domain.model.PaymentMethod
import ifac.td.taxi.domain.model.Trip
import ifac.td.taxi.viewmodel.model.RedSysPaymentState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
            }
            PaymentUiEvent.OnResume -> {
            }
            PaymentUiEvent.OnCardClicked -> handlePayment(PaymentMethod.CARD)
            PaymentUiEvent.OnCashClicked -> handlePayment(PaymentMethod.CASH)
            PaymentUiEvent.OnSubscriberClicked -> handlePayment(PaymentMethod.SUBSCRIBER)
            PaymentUiEvent.OnBizumClicked -> {
            }
            PaymentUiEvent.OnAddAmountClicked -> {
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
                    }
                    PaymentDialogButton.RETRY -> { }
                    PaymentDialogButton.CANCEL -> { }
                    PaymentDialogButton.OTHERS -> { }
                }
            }
            is PaymentUiEvent.OnDialogOptionSelected -> Unit
            is PaymentUiEvent.OnPaymentResult -> {
                if (event.success) {
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
import androidx.compose.ui.graphics.Color
import ifac.td.taxi.domain.usecase.DispatchUseCase.Companion.SUBSCRIBER_CASH_CARD
import ifac.td.taxi.domain.usecase.DispatchUseCase.Companion.SUBSCRIBER_CASH_NO_CARD
import ifac.td.taxi.domain.usecase.DispatchUseCase.Companion.SUBSCRIBER_CREDIT_CARD
import ifac.td.taxi.domain.usecase.DispatchUseCase.Companion.SUBSCRIBER_CREDIT_CARD_COMPULSORY
import ifac.td.taxi.domain.usecase.DispatchUseCase.Companion.SUBSCRIBER_CREDIT_NO_CARD
import ifac.td.taxi.domain.usecase.DispatchUseCase.Companion.SUBSCRIBER_TCC
import ifac.td.taxi.domain.utils.StaticConfiguration
object PaymentButtonsStateFactory {
    fun build(
        clientType: Int?,
        subscriberFailPin: Boolean,
        isConcertedOrMax: Boolean,
        hasMoneiAccount: Boolean,
        showAppPayment: Boolean,
        waitingAppPaymentResponse: Boolean,
        showBackToDispatch: Boolean,
        showCourtesyLight: Boolean,
        showUvLight: Boolean,
        showLocution: Boolean,
        showAddAmount: Boolean,
    ): PaymentButtonsState {
        val cardEnabled = when (clientType) {
            SUBSCRIBER_CASH_NO_CARD,
            SUBSCRIBER_CASH_CARD -> false
            SUBSCRIBER_CREDIT_NO_CARD,
            SUBSCRIBER_CREDIT_CARD,
            SUBSCRIBER_CREDIT_CARD_COMPULSORY,
            SUBSCRIBER_TCC -> subscriberFailPin
            else -> true
        }
        val cashEnabled = when (clientType) {
            SUBSCRIBER_CASH_NO_CARD,
            SUBSCRIBER_CASH_CARD -> true
            SUBSCRIBER_CREDIT_NO_CARD,
            SUBSCRIBER_CREDIT_CARD,
            SUBSCRIBER_CREDIT_CARD_COMPULSORY,
            SUBSCRIBER_TCC -> subscriberFailPin
            else -> true
        }
        return PaymentButtonsState(
            card = PaymentButtonUiState(
                visible = true,
                enabled = cardEnabled,
                style = if (cardEnabled) PaymentButtonStyle.ENABLE else PaymentButtonStyle.DISABLE,
                text = "Card"
            ),
            cash = PaymentButtonUiState(
                visible = true,
                enabled = cashEnabled,
                style = if (cashEnabled) PaymentButtonStyle.ENABLE else PaymentButtonStyle.DISABLE,
                text = "Cash"
            ),
            subscriber = PaymentButtonUiState(
                visible = true,
                enabled = true,
                style = PaymentButtonStyle.ENABLE,
                text = "Subscriber"
            ),
            bizum = PaymentButtonUiState(
                visible = hasMoneiAccount,
                enabled = true,
                style = PaymentButtonStyle.ENABLE,
                text = "Bizum"
            ),
            appPayment = PaymentButtonUiState(
                visible = showAppPayment,
                enabled = true,
                style = PaymentButtonStyle.ENABLE,
                text = "App payment"
            ),
            othersPayment = PaymentButtonUiState(
                visible = showAppPayment,
                enabled = !isConcertedOrMax,
                style = if (isConcertedOrMax) PaymentButtonStyle.DISABLE else PaymentButtonStyle.ENABLE,
                text = "Others"
            ),
            cancel = PaymentButtonUiState(
                visible = false,
                enabled = true,
                style = PaymentButtonStyle.ENABLE,
                text = "Cancel"
            ),
            addAmount = PaymentButtonUiState(
                visible = showAddAmount,
                enabled = true,
                style = PaymentButtonStyle.ENABLE,
                text = "+"
            ),
            backToDispatch = PaymentButtonUiState(
                visible = showBackToDispatch,
                enabled = true,
                style = PaymentButtonStyle.ENABLE,
                text = "Back"
            ),
            coutesyLight = PaymentButtonUiState(
                visible = showCourtesyLight,
                enabled = true,
                style = PaymentButtonStyle.ENABLE,
                text = "Courtesy"
            ),
            uvLight = PaymentButtonUiState(
                visible = showUvLight,
                enabled = true,
                style = PaymentButtonStyle.ENABLE,
                text = "UV"
            ),
            locution = PaymentButtonUiState(
                visible = showLocution,
                enabled = true,
                style = PaymentButtonStyle.ENABLE,
                text = "TTS"
            )
        )
    }
}
