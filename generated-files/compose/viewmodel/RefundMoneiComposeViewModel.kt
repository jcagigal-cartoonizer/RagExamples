package ifac.td.taxi.compose.viewmodel
import ifac.td.taxi.ui.screen.components.RefundMoneiUiEvent
import ifac.td.taxi.ui.screen.components.RefundMoneiUiEffect
import ifac.td.taxi.ui.screen.components.RefundMoneiUiState
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
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
