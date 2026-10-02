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
// # Block 250-4: import androidx.lifecycle.ViewModel
class ClosedPartialComposeViewModel(
    private val printerUseCase: PrinterUseCase,
    private val partialUseCase: PartialUseCase,
    private val taximeterUseCase: TaximeterConnectUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ClosedPartialUiState())
    val uiState: StateFlow<ClosedPartialUiState> = _uiState.asStateFlow()
    private val _uiEffect = MutableSharedFlow<ClosedPartialUiEffect>(extraBufferCapacity = 1)
    val uiEffect: SharedFlow<ClosedPartialUiEffect> = _uiEffect.asSharedFlow()
    private var currentPartial: PartialModel? = null
    private var justClosed: Boolean = false
    fun onEvent(event: ClosedPartialUiEvent) {
        when (event) {
            is ClosedPartialUiEvent.ScreenStarted -> {
                justClosed = event.justClosed
                checkTaximeterStatus()
                getClosedPartial()
            }
            ClosedPartialUiEvent.CancelClicked -> manageBack()
            ClosedPartialUiEvent.PrintClicked -> printPartial()
            ClosedPartialUiEvent.TotalizersClicked -> emitEffect(ClosedPartialUiEffect.NavigateToTotalizers)
            ClosedPartialUiEvent.DialogConfirmed -> emitEffect(ClosedPartialUiEffect.HidePrintDialog)
            ClosedPartialUiEvent.DialogDismissed -> emitEffect(ClosedPartialUiEffect.HidePrintDialog)
        }
    }
    fun emitEffect(effect: ClosedPartialUiEffect) {
        viewModelScope.launch {
            _uiEffect.emit(effect)
        }
    }
    fun checkTaximeterStatus() {
        viewModelScope.launch {
            val connected = taximeterUseCase.isTaximeterConnected()
            updateButtons(isConnected = connected)
        }
    }
    fun getClosedPartial() {
        viewModelScope.launch {
            val result = partialUseCase.getClosedPartial()
            currentPartial = result
            _uiState.update {
                it.copy(
                    ticketContent = result?.bufLastTicketCierre?.takeIf { text -> text.isNotEmpty() }
                        ?: "No closed partials"
                )
            }
            updateButtons()
        }
    }
    fun manageBack() {
        viewModelScope.launch {
            if (justClosed) {
                emitEffect(ClosedPartialUiEffect.NavigateBackTwice)
            } else {
                emitEffect(ClosedPartialUiEffect.NavigateBackOnce)
            }
        }
    }
    fun printPartial() {
        viewModelScope.launch {
            val partial = currentPartial
            if (partial != null) {
                printerUseCase.printMessage(
                    header = null,
                    message = partial.bufLastTicketCierre.addTicketLines(),
                    paperFeed = false
                )
            }
        }
    }
    fun updateButtons(isConnected: Boolean? = null) {
        val connected = isConnected ?: false
        _uiState.update { state ->
            state.copy(
                buttonsState = ClosedPartialButtonsState.from(
                    isTaximeterConnected = connected,
                    hasTotalizers = true,
                    currentStatus = null
                )
            )
        }
    }
}
