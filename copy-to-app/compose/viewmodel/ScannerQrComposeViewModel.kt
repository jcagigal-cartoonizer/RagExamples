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
                val model = ScannerQrButtonCustomDialogModel(
                    title = "Open camera",
                    description = "Select a camera",
                    buttons = listOf(ScannerQrButtonDialogButtonType.FRONT_CAMERA, ScannerQrButtonDialogButtonType.BACK_CAMERA)
                )
                _uiState.update { it.copy(dialogState = ScannerQrButtonCustomDialogState(model)) }
                _uiEffects.tryEmit(ScannerQrUiEffect.ShowCameraDialog(model))
            }
            is ScannerQrUiEvent.DialogButtonClicked -> {
                val cameraPosition = when (event.button) {
                    ScannerQrButtonDialogButtonType.FRONT_CAMERA -> 1
                    ScannerQrButtonDialogButtonType.BACK_CAMERA -> 0
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
