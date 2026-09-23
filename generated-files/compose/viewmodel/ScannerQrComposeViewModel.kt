package ifac.td.taxi.compose.viewmodel
import ifac.td.taxi.ui.screen.components.ScannerQrUiEvent
import ifac.td.taxi.ui.screen.components.ScannerQrUiEffect
import ifac.td.taxi.ui.screen.components.ScannerQrCustomDialogModel
import ifac.td.taxi.ui.screen.components.ScannerQrCustomDialogState
import ifac.td.taxi.ui.screen.components.ScannerQrUiState
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 110-3: import android.app.Application
class ScannerQrComposeViewModel(
    app: Application,
    private val subscriberUseCase: SubscriberUseCase,
    private val ticketUseCase: TicketUseCase
) : AndroidViewModel(app) {
    private val TAG = "ScannerQrComposeVM"
    private val _uiState = MutableStateFlow(ScannerQrUiState())
    val uiState: StateFlow<ScannerQrUiState> = _uiState.asStateFlow()
    private val _uiEffects = MutableSharedFlow<ScannerQrUiEffect>(extraBufferCapacity = 1)
    val uiEffects: SharedFlow<ScannerQrUiEffect> = _uiEffects.asSharedFlow()
    fun onEvent(event: ScannerQrUiEvent) {
        when (event) {
            is ScannerQrUiEvent.ServiceIdLoaded -> {
                _uiState.update { it.copy(serviceId = event.serviceId) }
            }
            ScannerQrUiEvent.ScannerClicked -> {
                val model = ScannerQrCustomDialogModel(
                    title = "Open camera",
                    description = "Select a camera",
                    buttons = listOf(ScannerQrDialogButtonType.FRONT_CAMERA, ScannerQrDialogButtonType.BACK_CAMERA)
                )
                _uiState.update { it.copy(dialogState = ScannerQrCustomDialogState(model)) }
                _uiEffects.tryEmit(ScannerQrUiEffect.ShowCameraDialog(model))
            }
            is ScannerQrUiEvent.DialogButtonClicked -> {
                val cameraPosition = when (event.button) {
                    ScannerQrDialogButtonType.FRONT_CAMERA -> 1
                    ScannerQrDialogButtonType.BACK_CAMERA -> 0
                }
                _uiState.update { it.copy(dialogState = null) }
                _uiEffects.tryEmit(ScannerQrUiEffect.OpenScanner(cameraPosition))
            }
            ScannerQrUiEvent.DialogDismissed -> {
                _uiState.update { it.copy(dialogState = null) }
            }
            ScannerQrUiEvent.CancelClicked -> {
                _uiEffects.tryEmit(ScannerQrUiEffect.NavigateBack)
            }
            is ScannerQrUiEvent.ScannerQrResult -> {
                handleResult(event.rawQr)
            }
        }
    }
    fun onScannerResult(
        rawResult: String,
        infoDispatchModel: ifac.td.taxi.viewmodel.model.InfoDispatchModel?,
        trip: Trip?,
        updatePrintFlowCallback: suspend (Trip) -> Unit,
    ) {
        val serviceId = uiState.value.serviceId
        try {
            val qrVoucher = Gson().fromJson(rawResult, VoucherQR::class.java)
            Logs.d(TAG, "onScannerResult: parsed QR=$qrVoucher")
            _uiState.update { it.copy(scannerQrButtonsStateLoading()) }
            viewModelScope.launch {
                subscriberUseCase.uploadVoucherId(
                    serviceId = serviceId,
                    qrVoucher = qrVoucher,
                    infoDispatchModel = infoDispatchModel,
                    trip = trip,
                    navigateFunction = { success ->
                        if (success) {
                            _uiEffects.tryEmit(ScannerQrUiEffect.NavigateHome)
                        } else {
                            _uiEffects.tryEmit(ScannerQrUiEffect.EnableScannerButton)
                            _uiState.update { it.copy(buttonsState = it.buttonsState.withScannerEnabled()) }
                        }
                    },
                    updatePrintFlow = { t, dispatch, status ->
                        viewModelScope.launch {
                            if (t != null) ticketUseCase.prepareTicket(t, dispatch, status)
                        }
                    },
                    openDialogQrFlow = null
                )
            }
        } catch (e: Exception) {
            Logs.e(TAG, "onScannerResult: ${e.message}")
            _uiEffects.tryEmit(ScannerQrUiEffect.EnableScannerButton)
            _uiState.update { it.copy(buttonsState = it.buttonsState.withScannerEnabled()) }
        }
    }
    fun handleResult(result: String) {
        // kept for parity with fragment; parsing happens in onScannerResult in Compose host
    }
    fun ScannerQrUiState.scannerQrButtonsStateLoading(): ScannerQrUiState =
        copy(buttonsState = buttonsState.withScannerLoading())
}
