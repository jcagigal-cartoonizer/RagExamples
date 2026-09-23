package ifac.td.taxi.compose.viewmodel
import ifac.td.taxi.ui.screen.components.OpenPartialUiState
import ifac.td.taxi.ui.screen.components.OpenPartialUiEffect
import ifac.td.taxi.ui.screen.components.OpenPartialUiEvent
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 66-2: import android.app.Application
class OpenPartialComposeViewModel(
    application: Application,
    private val partialUseCase: PartialUseCase,
    private val printerUseCase: PrinterUseCase,
    private val licensingUseCase: LicensingUseCase,
    private val taximeterUseCase: TaximeterConnectUseCase,
    private val ticketUseCase: TicketUseCase,
) : AndroidViewModel(application) {
    private val TAG = "OpenPartialViewModel"
    private val _uiState = MutableStateFlow(OpenPartialUiState())
    val uiState = _uiState.asStateFlow()
    private val _uiEffect = MutableSharedFlow<OpenPartialUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()
    private var canCloseFromArgs: Boolean = false
    fun init(canClose: Boolean) {
        canCloseFromArgs = canClose
        checkClosuresPermission()
        getPartial()
    }
    fun onEvent(event: OpenPartialUiEvent) {
        when (event) {
            OpenPartialUiEvent.OnBackClicked -> emitEffect(OpenPartialUiEffect.NavigateBack)
            OpenPartialUiEvent.OnPrintClicked -> handlePrint()
            OpenPartialUiEvent.OnCloseClicked -> emitEffect(OpenPartialUiEffect.RequestClosePartialsConfirmation)
            OpenPartialUiEvent.OnTotalizersClicked -> emitEffect(OpenPartialUiEffect.NavigateToTotalizers)
            OpenPartialUiEvent.OnDialogDismissed -> setDialog(null)
            OpenPartialUiEvent.OnDialogAccepted -> {
                setDialog(null)
                closePartials()
            }
        }
    }
    fun checkClosuresPermission() {
        viewModelScope.launch {
            val parameters = licensingUseCase.getLicensingParameters()
            val canClosePermission = parameters?.isClosuresButton == true
            val isTaximeterConnected = taximeterUseCase.isTaximeterConnected()
            updateButtons(
                canClosePermission = canClosePermission,
                isTaximeterConnected = isTaximeterConnected
            )
        }
    }
    fun getPartial() {
        viewModelScope.launch {
            ticketUseCase.loadCampos()
            ticketUseCase.loadTickets {
                viewModelScope.launch {
                    partialUseCase.doPartials { partial ->
                        Logs.d(TAG, "doPartials result = $partial")
                        handlePartial(partial)
                    }
                }
            }
        }
    }
    fun handlePartial(partial: PartialModel?) {
        val raw = partial?.bufLastTicketParciales
        val ticketContent = raw.takeIf { !raw.isNullOrEmpty() }
            ?: getApplication<Application>().getString(R.string.no_active_partials_with_data)
        _uiState.value = _uiState.value.copy(
            isLoading = partial == null,
            rawPartialContent = raw,
            ticketContent = if (partial == null) {
                getApplication<Application>().getString(R.string.no_active_partials)
            } else ticketContent
        )
    }
    fun updateButtons(canClosePermission: Boolean, isTaximeterConnected: Boolean) {
        val canClose = canClosePermission && canCloseFromArgs
        val hasTotalizers = false // Replace with your shared totalizers source if needed
        val closeState = if (canClose) {
            ButtonUiState.visibleEnabled(ButtonStyle.ENABLE)
        } else {
            ButtonUiState.gone()
        }
        val totalizersVisible = !canClose && isTaximeterConnected
        val totalizersState = if (totalizersVisible) {
            if (isTaximeterConnected && hasTotalizers) {
                ButtonUiState.visibleEnabled(ButtonStyle.ENABLE)
            } else {
                ButtonUiState.visibleDisabled(ButtonStyle.DISABLE)
            }
        } else {
            ButtonUiState.gone()
        }
        _uiState.value = _uiState.value.copy(
            buttons = _uiState.value.buttons.copy(
                close = closeState,
                totalizers = totalizersState
            )
        )
    }
    fun handlePrint() {
        val content = _uiState.value.rawPartialContent
        if (content.isNullOrEmpty()) {
            viewModelScope.launch { emitEffect(OpenPartialUiEffect.ShowToastNoPartialsToPrint) }
        } else {
            viewModelScope.launch {
                printerUseCase.printTicket(buf = content, paperFeed = true)
            }
        }
    }
    fun closePartials() {
        viewModelScope.launch {
            partialUseCase.closePartial()
            emitEffect(OpenPartialUiEffect.ClosePartialsAndNavigate)
        }
    }
    fun setDialog(dialog: OpenPartialDialogState?) {
        _uiState.value = _uiState.value.copy(dialog = dialog)
    }
    fun showCloseDialog() {
        setDialog(
            OpenPartialDialogState(
                messageRes = R.string.alert_close_partial_dialog
            )
        )
    }
    fun emitEffect(effect: OpenPartialUiEffect) {
        viewModelScope.launch { _uiEffect.emit(effect) }
    }
}
