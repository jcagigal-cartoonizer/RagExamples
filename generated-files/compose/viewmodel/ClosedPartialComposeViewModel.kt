package ifac.td.taxi.compose.viewmodel
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 52-2: import android.app.Application
import androidx.lifecycle.viewModelScope
import ifac.td.taxi.domain.model.PartialModel
import ifac.td.taxi.domain.usecase.PartialUseCase
import ifac.td.taxi.domain.usecase.PrinterUseCase
import ifac.td.taxi.framework.sdk.usecase.TaximeterConnectUseCase
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.viewmodel.model.UtilsModel.Companion.addTicketLines
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
class ClosedPartialComposeViewModel(
    context: Application,
    private val printerUseCase: PrinterUseCase,
    private val partialUseCase: PartialUseCase,
    private val taximeterUseCase: TaximeterConnectUseCase,
    private val sharedViewModel: MainActivityViewModel
) : BaseViewModel(context) {
    private val TAG = "ClosedPartialComposeVM"
    private val _uiState = MutableStateFlow(ClosedPartialUiState())
    val uiState = _uiState.asStateFlow()
    private val _uiEffects = MutableSharedFlow<ClosedPartialUiEffect>()
    val uiEffects = _uiEffects.asSharedFlow()
    private val _partial = MutableStateFlow<PartialModel?>(null)
    fun onStart() {
        viewModelScope.launch {
            Logs.d(TAG, "onStart")
            checkTaximeterStatus()
            getClosedPartial()
        }
    }
    fun onCancelClicked(justClosed: Boolean) {
        viewModelScope.launch {
            Logs.d(TAG, "onCancelClicked justClosed=$justClosed")
            if (justClosed) {
                _uiEffects.emit(ClosedPartialUiEffect.NavigateBack)
                _uiEffects.emit(ClosedPartialUiEffect.NavigateBack)
            } else {
                _uiEffects.emit(ClosedPartialUiEffect.NavigateBack)
            }
        }
    }
    fun onPrintClicked() {
        viewModelScope.launch {
            val partial = _partial.value
            if (partial != null) {
                _uiEffects.emit(ClosedPartialUiEffect.PrintPartial)
                printerUseCase.printMessage(
                    header = null,
                    message = partial.bufLastTicketCierre.addTicketLines(),
                    paperFeed = false
                )
            } else {
                _uiState.update {
                    it.copy(
                        dialog = ClosedPartialDialogState(
                            title = "Print",
                            message = "No partial data available to print"
                        )
                    )
                }
            }
        }
    }
    fun onTotalizersClicked(destinationId: Int) {
        viewModelScope.launch {
            _uiEffects.emit(ClosedPartialUiEffect.NavigateToTotalizers(destinationId))
        }
    }
    fun onDialogDismiss() {
        _uiState.update { it.copy(dialog = null) }
        viewModelScope.launch { _uiEffects.emit(ClosedPartialUiEffect.CloseDialog) }
    }
    fun getClosedPartial() {
        viewModelScope.launch {
            Logs.d(TAG, "getClosedPartial")
            val result = partialUseCase.getClosedPartial()
            _partial.value = result
            val ticketContent = result?.bufLastTicketCierre?.takeIf { it.isNotEmpty() }
                ?: getString(R.string.no_closed_partials)
            _uiState.update {
                it.copy(
                    ticketContent = ticketContent,
                    buttonsState = buildButtonsState(result != null)
                )
            }
        }
    }
    private suspend fun checkTaximeterStatus() {
        val isTaximeterConnected = taximeterUseCase.isTaximeterConnected()
        _uiState.update {
            it.copy(buttonsState = buildButtonsState(hasPartial = _partial.value != null, isConnected = isTaximeterConnected))
        }
    }
    fun buildButtonsState(
        hasPartial: Boolean = _partial.value != null,
        isConnected: Boolean = false
    ): ClosedPartialButtonsState {
        val currentStatus = sharedViewModel.shiftStatusFlow.value?.currentStatus
        val hasTotalizers = sharedViewModel.taximeterTotalizersFlow.value != null
        val shouldShowTotalizers = currentStatus != com.interfacom.sdk.taximeter.bravocomm.ifConstants.STATE_DISCONNECTED && isConnected
        val totalizersStyle = if (!shouldShowTotalizers) {
            ClosedPartialButtonStyle.Hidden
        } else if (isConnected && hasTotalizers) {
            ClosedPartialButtonStyle.Enabled
        } else {
            ClosedPartialButtonStyle.Disabled
        }
        return ClosedPartialButtonsState(
            cancel = ClosedPartialButtonStyle.Enabled,
            print = if (hasPartial) ClosedPartialButtonStyle.Enabled else ClosedPartialButtonStyle.Disabled,
            totalizers = totalizersStyle
        )
    }
}
