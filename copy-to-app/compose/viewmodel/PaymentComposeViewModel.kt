package ifac.td.taxi.compose.viewmodel
import  android.app.Application
import  ifac.td.taxi.R
import ifac.td.taxi.repository.connections.service.model.*
import androidx.annotation.StringRes
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import ifac.td.taxi.compose.viewmodel.*
import androidx.navigation.NavController
import ifac.td.taxi.viewmodel.MainActivityViewModel
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.platform.*
import androidx.core.net.toUri
import androidx.navigation.*
import ifac.td.taxi.ui.screen.components.*
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.*
import com.interfacom.sdk.taximeter.bravocomm.*
import ifac.td.taxi.domain.model.*
import ifac.td.taxi.domain.usecase.*
import ifac.td.taxi.framework.sdk.bravocentral.usecase.*
import ifac.td.taxi.framework.sdk.usecase.*
import ifac.td.taxi.repository.room.entities.*
import ifac.td.taxi.repository.room.entities.countdown.*
import ifac.td.taxi.repository.room.entities.message.*
import ifac.td.taxi.viewmodel.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
