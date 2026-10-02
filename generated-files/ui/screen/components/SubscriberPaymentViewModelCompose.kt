package ifac.td.taxi.ui.screen.components
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
// # Block 345-7: import android.app.Application
class SubscriberPaymentComposeViewModelCompose(
    private val subscriberUseCase: SubscriberUseCase,
    private val shiftStatusUseCase: ShiftStatusUseCase,
    private val tripUseCase: TripUseCase,
    private val bravoCentralUseCase: BravoCentralUseCase,
    private val ticketUseCase: TicketUseCase,
    app: Application,
) : BaseViewModel(app) {
    private val _uiState = MutableStateFlow(SubscriberPaymentUiState())
    val uiState: StateFlow<SubscriberPaymentUiState> = _uiState.asStateFlow()
    private val _uiEffect = MutableSharedFlow<SubscriberPaymentUiEffect>()
    val uiEffect: SharedFlow<SubscriberPaymentUiEffect> = _uiEffect.asSharedFlow()
    private var actualTrip: Trip? = null
    private var actualDispatch: InfoDispatchModel? = null
    private var hasScannedQR: Boolean = false
    var scannedCompanyId: Int? = null
        private set
    init {
        viewModelScope.launch {
            val subscriber = subscriberUseCase.getSubscriber()
            _uiState.value = _uiState.value.copy(
                subscriber = subscriber?.manualSubscriber?.toString().orEmpty(),
                user = subscriber?.manualUser?.toString().orEmpty(),
                authorization = subscriber?.manualAuthorization.orEmpty(),
                pin = subscriber?.manualPin.orEmpty()
            )
        }
    }
    fun bindArgs(
        user: String?,
        subscriber: String?,
        fromDispatch: Boolean,
        trip: Trip?,
        dispatch: InfoDispatchModel?
    ) {
        actualTrip = trip
        actualDispatch = dispatch
        _uiState.value = _uiState.value.copy(
            user = user.orEmpty(),
            subscriber = subscriber.orEmpty(),
            isFromDispatch = fromDispatch,
            fieldsEnabled = SubscriberPaymentFieldsEnabled(
                subscriberEnabled = !(fromDispatch && dispatch?.autorizacion.isNullOrEmpty().not()),
                userEnabled = !(fromDispatch && dispatch?.autorizacion.isNullOrEmpty().not()),
                authorizationEnabled = true,
                pinEnabled = true
            ),
            buttonsState = buildSubscriberPaymentButtonsState(
                fromDispatch = fromDispatch,
                dispatchSubscriberExists = dispatch?.subscriber?.isNotEmpty() == true,
                authPresent = !dispatch?.autorizacion.isNullOrEmpty(),
                loading = false
            )
        )
    }
    fun onEvent(event: SubscriberPaymentUiEvent) {
        when (event) {
            is SubscriberPaymentUiEvent.SubscriberChanged -> updateSubscriber(event.value)
            is SubscriberPaymentUiEvent.UserChanged -> updateUser(event.value)
            is SubscriberPaymentUiEvent.AuthorizationChanged -> updateAuthorization(event.value)
            is SubscriberPaymentUiEvent.PinChanged -> updatePin(event.value)
            SubscriberPaymentUiEvent.AcceptClicked -> acceptClicked()
            SubscriberPaymentUiEvent.CancelClicked -> cancelClicked()
            SubscriberPaymentUiEvent.QrClicked -> {
                _uiEffect.tryEmit(SubscriberPaymentUiEffect.OpenScanner)
            }
            is SubscriberPaymentUiEvent.QrScanned -> handleQrResult(event.result)
        }
    }
    fun updateSubscriber(value: String) {
        _uiState.value = _uiState.value.copy(subscriber = value)
    }
    fun updateUser(value: String) {
        _uiState.value = _uiState.value.copy(user = value)
    }
    fun updateAuthorization(value: String) {
        _uiState.value = _uiState.value.copy(authorization = value)
    }
    fun updatePin(value: String) {
        _uiState.value = _uiState.value.copy(pin = value)
    }
    fun acceptClicked() {
        val current = _uiState.value
        _uiState.value = current.copy(buttonsState = current.buttonsState.copy(accept = current.buttonsState.accept.copy(loading = true, enabled = false)))
        if (current.isFromDispatch && actualDispatch?.subscriber?.isNotEmpty() == true) {
            clickedOkSubscriberDispatch()
        } else {
            clickedOkSubscriberStreet()
        }
    }
    fun cancelClicked() {
        viewModelScope.launch {
            bravoCentralUseCase.cancelCredit()
            _uiEffect.emit(SubscriberPaymentUiEffect.NavigateBack)
        }
    }
    fun clickedOkSubscriberDispatch() {
        if (_uiState.value.pin == actualDispatch?.pin) {
            actualDispatch?.let { dispatch ->
                actualTrip?.let { trip ->
                    saveSubscriber()?.let { subscriber ->
                        subscriberFromDispatch(dispatch, trip, subscriber)
                    }
                }
            }
        } else {
            StaticConfiguration.subscriberFailPin = true
            viewModelScope.launch {
                _uiEffect.emit(SubscriberPaymentUiEffect.ShowErrorDialog(app.getString(R.string.pin_incorrecto)))
                _uiEffect.emit(SubscriberPaymentUiEffect.NavigateBack)
            }
        }
    }
    fun clickedOkSubscriberStreet() {
        saveSubscriber()?.let { subscriber ->
            subscriberLogic(
                tripId = actualTrip?.id ?: return,
                subscriber = subscriber,
                isFromIngenico = false
            )
        }
    }
    fun saveSubscriber(): Subscriber? {
        val subscriberText = _uiState.value.subscriber.trim()
        val userText = _uiState.value.user.trim()
        val subscriberValue = subscriberText.toIntOrNull()
        val userValue = userText.toIntOrNull()
        return if (subscriberValue != null && userValue != null && subscriberValue > 0 && userValue > 0) {
            val sub = Subscriber(
                tripId = actualTrip?.id,
                isManual = false,
                manualSubscriber = subscriberValue,
                manualUser = userValue,
                manualAuthorization = _uiState.value.authorization,
                manualPin = _uiState.value.pin.ifBlank { "" },
                companyId = scannedCompanyId?.toString().orEmpty()
            )
            viewModelScope.launch { subscriberUseCase.registerSubscriberAndAssignToTrip(sub) }
            sub
        } else null
    }
    fun handleQrResult(result: String) {
        val gson = Gson()
        try {
            val subscriberQR = gson.fromJson(result, SubscriberQR::class.java)
            if (subscriberQR.type == 1) {
                saveSubscriberQR(subscriberQR)
                _uiState.value = _uiState.value.copy(
                    subscriber = subscriberQR.accountId,
                    user = subscriberQR.userId?.toString().orEmpty().ifBlank { "1" }
                )
                return
            }
        } catch (_: JsonSyntaxException) { }
        try {
            val voucherQR = gson.fromJson(result, VoucherQR::class.java)
            saveVoucherQR(voucherQR)
            _uiState.value = _uiState.value.copy(
                subscriber = voucherQR.accountId,
                user = voucherQR.userId?.toString().orEmpty().ifBlank { "1" }
            )
        } catch (_: JsonSyntaxException) {
            viewModelScope.launch {
                _uiEffect.emit(SubscriberPaymentUiEffect.ShowErrorDialog("Failed to parse QR code"))
            }
        }
    }
    fun saveVoucherQR(voucherQR: VoucherQR?) {
        scannedCompanyId = voucherQR?.companyId
        hasScannedQR = true
        viewModelScope.launch { subscriberUseCase.saveVoucherQR(voucherQR) }
    }
    fun saveSubscriberQR(subscriberQR: SubscriberQR?) {
        scannedCompanyId = subscriberQR?.companyId
    }
    fun cancelSubscriberPetition() {
        viewModelScope.launch { bravoCentralUseCase.cancelCredit() }
    }
    fun subscriberLogic(tripId: Long, subscriber: Subscriber, isFromIngenico: Boolean) {
        viewModelScope.launch {
            if (!hasScannedQR) subscriberUseCase.saveVoucherQR(null)
            val request = subscriberUseCase.getAccountPaymentRequest(tripId, subscriber)
            if (isFromIngenico) request?._abonadoConTarjeta = true
            subscriberUseCase.sendAccountPaymentRequest(request)
        }
    }
    fun subscriberFromDispatch(actualDispatch: InfoDispatchModel, actualTrip: Trip, subscriber: Subscriber?) {
        viewModelScope.launch(Dispatchers.IO) {
            if (!hasScannedQR) subscriberUseCase.saveVoucherQR(null)
            val manualSubscriberId = actualDispatch.subscriber?.toIntOrNull()
            val manualUserId = actualDispatch.subscriberUser?.toIntOrNull()
            val validSubscriber = if (manualSubscriberId != null && manualUserId != null && manualSubscriberId > 0 && manualUserId > 0) {
                Subscriber(
                    manualSubscriber = manualSubscriberId,
                    manualUser = manualUserId,
                    tripId = actualTrip.id
                )
            } else subscriber?.copy(tripId = actualTrip.id)
            if (validSubscriber != null) {
                subscriberUseCase.updateSubscriber(validSubscriber)
                if (actualDispatch.checkCreditLimit) {
                    subscriberUseCase.checkCreditLimit(
                        actualTrip.totalAmount,
                        _checkSubscriberCreditLimitFlowInternal
                    )
                } else {
                    dispatchSubscriberNextStep(actualDispatch, actualTrip)
                }
            } else {
                _uiEffect.emit(
                    SubscriberPaymentUiEffect.ShowErrorDialog(app.getString(R.string.dialog_error_subscriber))
                )
            }
        }
    }
    private val _checkSubscriberCreditLimitFlowInternal = MutableSharedFlow<String>()
    init {
        viewModelScope.launch {
            _checkSubscriberCreditLimitFlowInternal.collect { value ->
                val validCredit = value == "1" || value == "true"
                if (validCredit) {
                    _uiEffect.emit(SubscriberPaymentUiEffect.ShowToast(R.string.toast_valid_credit))
                    actualDispatch?.let { dispatch ->
                        when {
                            dispatch.requireSignature -> _uiEffect.emit(SubscriberPaymentUiEffect.NavigateToSignature(dispatch.longDispatchNumber))
                            dispatch.requireVoucher -> _uiEffect.emit(SubscriberPaymentUiEffect.NavigateToVoucher(dispatch.longDispatchNumber))
                            dispatch.requireQr -> _uiEffect.emit(SubscriberPaymentUiEffect.NavigateToQr(dispatch.longDispatchNumber))
                            else -> {
                                actualTrip?.let {
                                    validCredit(it)
                                }
                            }
                        }
                    }
                } else {
                    _uiEffect.emit(SubscriberPaymentUiEffect.ShowToast(R.string.toast_not_enough_credit))
                }
                _uiState.value = _uiState.value.copy(buttonsState = _uiState.value.buttonsState.copy(accept = _uiState.value.buttonsState.accept.copy(loading = false, enabled = true)))
            }
        }
    }
    fun dispatchSubscriberNextStep(dispatchValue: InfoDispatchModel, trip: Trip) {
        viewModelScope.launch {
            when {
                dispatchValue.requireSignature -> _uiEffect.emit(SubscriberPaymentUiEffect.NavigateToSignature(dispatchValue.longDispatchNumber))
                dispatchValue.requireVoucher -> _uiEffect.emit(SubscriberPaymentUiEffect.NavigateToVoucher(dispatchValue.longDispatchNumber))
                dispatchValue.requireQr -> _uiEffect.emit(SubscriberPaymentUiEffect.NavigateToQr(dispatchValue.longDispatchNumber))
                else -> {
                    withContext(Dispatchers.IO) {
                        tripUseCase.endTrip(trip, ifac.td.taxi.domain.model.PaymentMethod.SUBSCRIBER)
                    }
                    ticketUseCase.prepareTicket(trip, dispatchValue, shiftStatusUseCase.getStatus())
                    _uiEffect.emit(SubscriberPaymentUiEffect.ShowOperationAuthorizedDialog)
                    _uiEffect.emit(SubscriberPaymentUiEffect.NavigateHome)
                }
            }
            _uiState.value = _uiState.value.copy(buttonsState = _uiState.value.buttonsState.copy(accept = _uiState.value.buttonsState.accept.copy(loading = false, enabled = true)))
        }
    }
    fun validCredit(trip: Trip) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                tripUseCase.endTrip(trip, ifac.td.taxi.domain.model.PaymentMethod.SUBSCRIBER)
            }
            ticketUseCase.prepareTicket(trip, null, shiftStatusUseCase.getStatus())
            _uiEffect.emit(SubscriberPaymentUiEffect.NavigateHome)
        }
    }
}
@Immutable
data class SubscriberPaymentUiState(
    val subscriber: String = "",
    val user: String = "",
    val authorization: String = "",
    val pin: String = "",
    val isFromDispatch: Boolean = false,
    val fieldsEnabled: SubscriberPaymentFieldsEnabled = SubscriberPaymentFieldsEnabled(),
    val buttonsState: SubscriberPaymentButtonsState = SubscriberPaymentButtonsState.default()
)
@Immutable
data class SubscriberPaymentFieldsEnabled(
    val subscriberEnabled: Boolean = true,
    val userEnabled: Boolean = true,
    val authorizationEnabled: Boolean = true,
    val pinEnabled: Boolean = true
)
sealed interface SubscriberPaymentUiEvent {
    data class SubscriberChanged(val value: String) : SubscriberPaymentUiEvent
    data class UserChanged(val value: String) : SubscriberPaymentUiEvent
    data class AuthorizationChanged(val value: String) : SubscriberPaymentUiEvent
    data class PinChanged(val value: String) : SubscriberPaymentUiEvent
    data object AcceptClicked : SubscriberPaymentUiEvent
    data object CancelClicked : SubscriberPaymentUiEvent
    data object QrClicked : SubscriberPaymentUiEvent
    data class QrScanned(val result: String) : SubscriberPaymentUiEvent
}
sealed interface SubscriberPaymentUiEffect {
    data class ShowToast(val messageRes: Int) : SubscriberPaymentUiEffect
    data class ShowErrorDialog(val description: String) : SubscriberPaymentUiEffect
    data object ShowOperationAuthorizedDialog : SubscriberPaymentUiEffect
    data object NavigateBack : SubscriberPaymentUiEffect
    data object NavigateHome : SubscriberPaymentUiEffect
    data class NavigateToSignature(val dispatchId: Long) : SubscriberPaymentUiEffect
    data class NavigateToVoucher(val dispatchId: Long) : SubscriberPaymentUiEffect
    data class NavigateToQr(val dispatchId: Long) : SubscriberPaymentUiEffect
    data object OpenScanner : SubscriberPaymentUiEffect
}
