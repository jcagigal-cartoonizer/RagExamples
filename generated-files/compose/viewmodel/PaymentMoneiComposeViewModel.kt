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
// # Block 259-2: import android.app.Application
class PaymentMoneiComposeViewModel(
    private val moneiUseCase: MoneiUseCase,
    private val tripUseCase: TripUseCase,
    private val shiftStatusUseCase: ShiftStatusUseCase,
    app: Application
) : AndroidViewModel(app) {
    private val handler = Handler(Looper.getMainLooper())
    private var pollRunnable: Runnable? = null
    private val _uiState = MutableStateFlow(PaymentMoneiUiState())
    val uiState = _uiState.asStateFlow()
    private val _buttonsState = MutableStateFlow(PaymentMoneiButtonsState())
    val buttonsState = _buttonsState.asStateFlow()
    private val _effects = MutableSharedFlow<PaymentMoneiUiEffect>()
    val effects = _effects.asSharedFlow()
    fun init(totalAmount: Int, isoMoneda: String, orderId: String) {
        _uiState.update {
            it.copy(
                amountText = totalAmount.toCurrency(),
                orderId = orderId,
                isLoading = true
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            when (val response = moneiUseCase.generatePayment(
                GeneratePaymentRequest(totalAmount, isoMoneda, orderId)
            )) {
                is Resource.Success -> response.data?.let { payment ->
                    handleGeneratePayment(payment)
                    startPolling()
                }
                is Resource.Error -> {
                    emitError(response.exception)
                }
            }
        }
    }
    fun onEvent(event: PaymentMoneiUiEvent) {
        when (event) {
            PaymentMoneiUiEvent.CancelClicked -> onCancelClicked()
            PaymentMoneiUiEvent.PrintClicked -> onPrintClicked()
            PaymentMoneiUiEvent.DialogConfirm -> onDialogConfirm()
            PaymentMoneiUiEvent.DialogDismiss -> hideDialog()
        }
    }
    fun updateActualStatus(status: String) {
        viewModelScope.launch {
            val parsed = runCatching { StatusPayments.valueOf(status) }.getOrNull() ?: return@launch
            setStatus(parsed)
        }
    }
    fun finishPayment(trip: Trip) {
        viewModelScope.launch {
            trip.moneiPaymentId = uiState.value.paymentId
            tripUseCase.endTrip(trip, PaymentMethod.MONEI)
            val status = shiftStatusUseCase.checkForSubStateModifications(com.interfacom.sdk.taximeter.bravocomm.ifConstants.STATE_FOR_HIRE)
            status?.let { shiftStatusUseCase.setStatus(it, true) }
            _effects.emit(PaymentMoneiUiEffect.NavigateHome)
        }
    }
    fun handleGeneratePayment(payment: MoneiPaymentInfo) {
        _uiState.update {
            it.copy(
                paymentId = payment.id,
                qrBitmap = payment.urlToPay?.let(::qrFromString),
                isLoading = false,
                showQrLoading = false,
                showInfoQr = true
            )
        }
        payment.status?.let { updateActualStatus(it) }
    }
    fun startPolling() {
        pollRunnable?.let { handler.removeCallbacks(it) }
        pollRunnable = object : Runnable {
            override fun run() {
                viewModelScope.launch(Dispatchers.IO) {
                    uiState.value.paymentId?.let { paymentId ->
                        when (val response = moneiUseCase.getInfoPayment(paymentId)) {
                            is Resource.Success -> response.data?.let { info ->
                                handleInfoPayment(info)
                            }
                            is Resource.Error -> emitError(response.exception)
                        }
                    }
                }
                handler.postDelayed(this, 3000)
            }
        }
        handler.post(pollRunnable!!)
    }
    private suspend fun handleInfoPayment(info: InfoPayment) {
        _uiState.update {
            it.copy(
                qrBitmap = info.nextUrl?.let(::qrFromString) ?: it.qrBitmap,
                showQrLoading = false
            )
        }
        info.status?.let { updateActualStatus(it) }
    }
    fun setStatus(status: StatusPayments) {
        _uiState.update {
            it.copy(
                status = status,
                statusText = status.name,
                statusBackgroundColor = when (status) {
                    StatusPayments.PENDING -> Color(0xFFFFC107)
                    StatusPayments.FAILED,
                    StatusPayments.EXPIRED -> Color(0xFFD32F2F)
                    StatusPayments.SUCCEEDED -> Color(0xFF2E7D32)
                    else -> Color(0xFF607D8B)
                }
            )
        }
        _buttonsState.update { current ->
            current.copy(
                cancel = current.cancel.copy(
                    visible = true,
                    enabled = true,
                    text = when (status) {
                        StatusPayments.SUCCEEDED -> "Finish"
                        else -> "Cancel"
                    }
                ),
                print = current.print.copy(
                    visible = status == StatusPayments.SUCCEEDED,
                    enabled = status == StatusPayments.SUCCEEDED
                )
            )
        }
        when (status) {
            StatusPayments.SUCCEEDED -> {
                _uiState.update {
                    it.copy(
                        showSuccessMessage = true,
                        showQrLoading = false,
                        showInfoQr = false,
                        showAmount = false,
                        showStatus = false
                    )
                }
                _effects.tryEmit(PaymentMoneiUiEffect.ShowDialog(
                    title = "Payment succeeded",
                    message = "Do you want to finish the trip?",
                    confirmText = "Finish",
                    dismissText = "Close"
                ))
            }
            StatusPayments.PENDING -> Unit
            StatusPayments.FAILED -> {
                _uiState.update { it.copy(showStatus = true) }
            }
            StatusPayments.EXPIRED -> {
                _effects.tryEmit(PaymentMoneiUiEffect.NavigateBack)
            }
            else -> Unit
        }
    }
    fun onCancelClicked() {
        when (uiState.value.status) {
            StatusPayments.FAILED,
            StatusPayments.PENDING -> {
                _effects.tryEmit(PaymentMoneiUiEffect.NavigateBack)
            }
            StatusPayments.SUCCEEDED -> {
                _effects.tryEmit(PaymentMoneiUiEffect.ShowDialog(
                    title = "Finish payment",
                    message = "Do you want to finish the trip?",
                    confirmText = "Finish",
                    dismissText = "Cancel"
                ))
            }
            else -> Unit
        }
    }
    fun onPrintClicked() {
        val payment = uiState.value
        val ticket = buildString {
            append("Bizum\r\n")
            append("Amount ")
            append(Tickets.formatear(Tickets.FORM_MON, 12, payment.amountText))
            append("\r\n")
            payment.qrUrl?.let { append(Tickets.getQrCode(it)) }
        }
        _effects.tryEmit(PaymentMoneiUiEffect.PrintTicket(ticket))
    }
    fun onDialogConfirm() {
        when (uiState.value.status) {
            StatusPayments.SUCCEEDED -> {
                _effects.tryEmit(PaymentMoneiUiEffect.HideDialog)
                // actual finish is triggered from UI via shared trip if needed
            }
            else -> _effects.tryEmit(PaymentMoneiUiEffect.HideDialog)
        }
    }
    fun hideDialog() {
        _uiState.update { it.copy(showDialog = false) }
        _effects.tryEmit(PaymentMoneiUiEffect.HideDialog)
    }
    private suspend fun emitError(exception: Throwable?) {
        _effects.emit(PaymentMoneiUiEffect.ShowToast(R.string.toast_onerror_generatepaymentrequest))
        _uiState.update { it.copy(isLoading = false, showQrLoading = false) }
    }
    fun qrFromString(info: String): Bitmap? {
        return try {
            BarcodeEncoder().encodeBitmap(info, BarcodeFormat.QR_CODE, 400, 400)
        } catch (_: WriterException) {
            null
        }
    }
    override fun onCleared() {
        super.onCleared()
        pollRunnable?.let { handler.removeCallbacks(it) }
    }
    enum class StatusPayments {
        PENDING,
        SUCCEEDED,
        FAILED,
        CANCELED,
        REFUNDED,
        PARTIALLY_REFUNDED,
        AUTHORIZED,
        EXPIRED
    }
}
