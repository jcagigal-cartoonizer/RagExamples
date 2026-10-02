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
// # Block 33-2: import android.app.Application
class RefundMoneiComposeViewModel(
    private val moneiUseCase: MoneiUseCase,
    private val tripUseCase: TripUseCase,
    private val shiftStatusUseCase: ShiftStatusUseCase,
    context: Application,
) : BaseViewModel(context) {
    private val handler = Handler(Looper.getMainLooper())
    private var pollingRunnable: Runnable? = null
    private var pollingJob: Job? = null
    private val _uiState = MutableStateFlow(RefundMoneiUiState())
    val uiState = _uiState.asStateFlow()
    private val _uiEffect = MutableSharedFlow<RefundMoneiUiEffect>(extraBufferCapacity = 1)
    val uiEffect = _uiEffect.asSharedFlow()
    private var currentTrip: Trip? = null
    private var currentPaymentId: String? = null
    fun onEvent(event: RefundMoneiUiEvent) {
        when (event) {
            RefundMoneiUiEvent.OnCancelClicked -> {
                viewModelScope.launch { _uiEffect.emit(RefundMoneiUiEffect.NavigateBack) }
            }
            RefundMoneiUiEvent.OnRefundClicked -> {
                _uiState.update { it.copy(showConfirmDialog = true) }
            }
            RefundMoneiUiEvent.OnDismissDialog -> {
                _uiState.update { it.copy(showConfirmDialog = false, showErrorDialog = false) }
            }
            RefundMoneiUiEvent.OnConfirmRefund -> {
                _uiState.update { it.copy(showConfirmDialog = false, isLoading = true) }
                refund()
            }
        }
    }
    fun start(tripId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val trip = tripUseCase.getTripById(tripId)
            currentTrip = trip
            currentPaymentId = trip?.moneiPaymentId
            _uiState.update {
                it.copy(
                    amountText = trip?.totalAmount?.toString().orEmpty(),
                    canRefund = trip?.moneiPaymentId != null,
                )
            }
            trip?.moneiPaymentId?.let { paymentId ->
                startPolling(paymentId)
            }
        }
    }
    fun startPolling(paymentId: String) {
        stopPolling()
        pollingRunnable = object : Runnable {
            override fun run() {
                pollingJob = viewModelScope.launch(Dispatchers.IO) {
                    when (val response = moneiUseCase.getInfoPayment(paymentId)) {
                        is Resource.Success -> {
                            response.data?.let { info ->
                                handlePaymentInfo(info)
                            }
                        }
                        is Resource.Error -> {
                            response.exception?.let { ex ->
                                val uiEx = SmartTDUIException.fromException(ex)
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        showErrorDialog = true,
                                        errorMessage = uiEx.message ?: ex.message
                                    )
                                }
                                _uiEffect.emit(RefundMoneiUiEffect.ShowSnackbar(uiEx.message ?: "Error"))
                            }
                        }
                    }
                }
                handler.postDelayed(this, 3000)
            }
        }
        handler.post(pollingRunnable!!)
    }
    fun handlePaymentInfo(info: InfoPayment) {
        _uiState.update { state ->
            state.copy(
                isLoading = false,
                status = runCatching {
                    PaymentMoneiViewModel.StatusPayments.valueOf(info.status)
                }.getOrElse { PaymentMoneiViewModel.StatusPayments.SUCCEEDED },
                canRefund = info.status != PaymentMoneiViewModel.StatusPayments.REFUNDED.name,
            )
        }
        if (info.status == PaymentMoneiViewModel.StatusPayments.REFUNDED.name) {
            stopPolling()
        }
    }
    fun refund() {
        viewModelScope.launch(Dispatchers.IO) {
            val trip = currentTrip
            val paymentId = currentPaymentId
            if (trip == null || paymentId == null) {
                _uiState.update { it.copy(isLoading = false, showErrorDialog = true, errorMessage = "Missing trip/payment") }
                return@launch
            }
            when (val response = moneiUseCase.refundPayment(paymentId, trip.totalAmount)) {
                is Resource.Success -> {
                    response.data?.let { info ->
                        handlePaymentInfo(info)
                        stopPolling()
                    }
                }
                is Resource.Error -> {
                    response.exception?.let { ex ->
                        val uiEx = SmartTDUIException.fromException(ex)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                showErrorDialog = true,
                                errorMessage = uiEx.message ?: ex.message
                            )
                        }
                        _uiEffect.emit(RefundMoneiUiEffect.ShowSnackbar(uiEx.message ?: "Error"))
                    }
                }
            }
        }
    }
    fun stopPolling() {
        pollingRunnable?.let { handler.removeCallbacks(it) }
        pollingRunnable = null
        pollingJob?.cancel()
        pollingJob = null
    }
    override fun onCleared() {
        stopPolling()
        super.onCleared()
    }
}
