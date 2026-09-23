package ifac.td.taxi.compose.viewmodel
import ifac.td.taxi.ui.screen.components.ReceiptRedSysButtonsState
import ifac.td.taxi.ui.screen.components.ReceiptRedSysUiEffect
import ifac.td.taxi.ui.screen.components.ReceiptRedSysUiEvent
import ifac.td.taxi.ui.screen.components.ReceiptRedSysUiState
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 115-4: import android.app.Application
class ReceiptRedSysComposeViewModel(
    context: Application,
    private val redSysUseCase: RedSysUseCase,
    private val ticketUseCase: TicketUseCase,
    private val printerUseCase: PrinterUseCase,
    private val redSysSessionState: RedSysSessionState,
    private val tripUseCase: TripUseCase,
) : BaseViewModel(context) {
    private val _uiState = MutableStateFlow(ReceiptRedSysUiState())
    val uiState = _uiState.asStateFlow()
    private val _uiEffect = MutableSharedFlow<ReceiptRedSysUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()
    fun onEvent(event: ReceiptRedSysUiEvent) {
        when (event) {
            ReceiptRedSysUiEvent.ScreenOpened,
            ReceiptRedSysUiEvent.ReloadRequested -> loadOperations()
            is ReceiptRedSysUiEvent.OperationClicked -> handleOperationClicked(event.operation)
            is ReceiptRedSysUiEvent.DialogButtonClicked -> handleDialogButton(event.button)
            ReceiptRedSysUiEvent.DialogDismissed -> {
                _uiState.update { it.copy(dialogState = null) }
            }
        }
    }
    fun loadOperations() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            val operations = fetchOperations()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    operations = operations ?: emptyList()
                )
            }
        }
    }
    fun handleOperationClicked(operation: RedSysOperation) {
        val buttonsState = ReceiptRedSysButtonsState.forOperation(operation)
        _uiState.update { it.copy(buttonsState = buttonsState) }
        val showDialog = (operation.operationType == RedSysOperation.REFUND && operation.refundResponse != null) ||
            (operation.operationType == RedSysOperation.AUTHORIZATION && operation.result != RedSysOperation.DENIED)
        if (showDialog) {
            val buttons = if (buttonsState.showRefund) {
                listOf(ReceiptRedSysDialogButton.DEVOLVER, ReceiptRedSysDialogButton.IMPRIMIR)
            } else {
                listOf(ReceiptRedSysDialogButton.IMPRIMIR)
            }
            _uiState.update {
                it.copy(
                    dialogState = ReceiptRedSysDialogState(
                        title = getApplication<Application>().getString(ifac.td.taxi.R.string.red_sys_operation_title),
                        description = getApplication<Application>().getString(ifac.td.taxi.R.string.red_sys_operation_description),
                        buttons = buttons
                    )
                )
            }
        }
    }
    fun handleDialogButton(button: ReceiptRedSysDialogButton) {
        val currentOperation = _uiState.value.operations.firstOrNull() ?: return
        when (button) {
            ReceiptRedSysDialogButton.DEVOLVER -> {
                doRefund(currentOperation)
            }
            ReceiptRedSysDialogButton.IMPRIMIR -> {
                if (currentOperation.result != RedSysOperation.DENIED &&
                    currentOperation.operationType == RedSysOperation.AUTHORIZATION
                ) {
                    printServiceTicket(currentOperation)
                } else if (currentOperation.result != RedSysOperation.DENIED &&
                    currentOperation.operationType == RedSysOperation.REFUND
                ) {
                    printRefundTicket(currentOperation)
                }
            }
            ReceiptRedSysDialogButton.ACCEPT -> Unit
        }
        _uiState.update { it.copy(dialogState = null) }
    }
    fun doRefund(redSysOperation: RedSysOperation) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val usernameRedSys = redSysUseCase.getUsernameRedSys()
                val passwordRedSys = TemporalData.passwordRedSys
                val loginResponse = usernameRedSys?.let { redSysUseCase.loginRedSys(it, passwordRedSys) }
                when (loginResponse) {
                    is Success -> {
                        val terminal = loginResponse.merchantList.firstOrNull()?.terminalList?.firstOrNull()
                        if (terminal == null) {
                            _uiEffect.emit(
                                ReceiptRedSysUiEffect.ShowMessage("No terminal available")
                            )
                            return@launch
                        }
                        val rtsIdentifierLast5 = redSysOperation.RTSIdentifier.takeLast(5)
                        val consultaResponse = redSysUseCase.consultaRedSys(terminal, null)
                        val paymentInfo = consultaResponse?.transactionDataList?.find {
                            it.identifierRTS.takeLast(5) == rtsIdentifierLast5
                        }
                        if (paymentInfo == null) {
                            _uiEffect.emit(ReceiptRedSysUiEffect.ShowMessage("No payment info found"))
                            return@launch
                        }
                        val refundResponse = redSysUseCase.doRefund(terminal, paymentInfo)
                        redSysOperation.refundResponse = refundResponse
                        processRefundResponse(refundResponse, redSysOperation)
                    }
                    is Error -> _uiEffect.emit(ReceiptRedSysUiEffect.ShowMessage("Refund login error"))
                    null -> _uiEffect.emit(ReceiptRedSysUiEffect.ShowMessage("Refund login response null"))
                }
            }.onFailure {
                _uiEffect.emit(ReceiptRedSysUiEffect.ShowMessage("Refund failed"))
            }
        }
    }
    private suspend fun processRefundResponse(
        refundResponse: es.redsys.paysys.Operative.Managers.RedCLSRefundResponse?,
        operation: RedSysOperation
    ) {
        val result = redSysUseCase.parseDevolucion(refundResponse)
        if (result != null && result.result.equals(RedSysOperation.AUTHORIZED, ignoreCase = true)) {
            _uiEffect.emit(ReceiptRedSysUiEffect.ShowMessage("Refund success"))
            _uiEffect.emit(ReceiptRedSysUiEffect.PrintRefundTicket(operation))
        } else {
            _uiEffect.emit(ReceiptRedSysUiEffect.ShowMessage("Refund failure"))
        }
    }
    fun printServiceTicket(redSysOperation: RedSysOperation?) {
        viewModelScope.launch {
            redSysOperation?.RTSIdentifier?.let {
                val buffTicketRedsys = tripUseCase.getTripByRedsysByRTS(it)
                val ticketBuff = buffTicketRedsys?.ticketBufferRedSys
                if (!ticketBuff.isNullOrEmpty()) {
                    printerUseCase.printTicket(ticketBuff, true)
                }
            }
        }
    }
    fun printRefundTicket(
        redSysOperation: RedSysOperation? = null,
        refund: RedSysReturn? = null
    ) {
        viewModelScope.launch {
            val parsedResult = refund ?: redSysOperation?.refundResponse?.let {
                redSysUseCase.parseDevolucion(it)
            } ?: return@launch
            val terminalData = redSysSessionState.terminalData ?: return@launch
            val ticket = ticketUseCase.prepareRedSysRefundTicket(
                signature = false,
                resultadoDevolucion = parsedResult,
                terminalData = terminalData
            )
            printerUseCase.printTicket(ticket, true)
        }
    }
    private suspend fun fetchOperations(): List<RedSysOperation>? {
        redSysUseCase.configureRedSys()
        val usernameRedSys = redSysUseCase.getUsernameRedSys() ?: return null
        val passwordRedSys = TemporalData.passwordRedSys
        return when (val loginResponse = redSysUseCase.loginRedSys(usernameRedSys, passwordRedSys)) {
            is Success -> {
                val terminal = loginResponse.merchantList.firstOrNull()?.terminalList?.firstOrNull() ?: return null
                val consultaResponse = redSysUseCase.consultaRedSys(terminal, null)
                redSysUseCase.parseConsulta(consultaResponse)
            }
            is Error -> null
            null -> null
        }
    }
}
