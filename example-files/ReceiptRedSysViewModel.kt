package ifac.td.taxi.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import ifac.td.taxi.framework.util.Logs
import es.redsys.paysys.Operative.Managers.RedCLSRefundResponse
import ifac.td.taxi.domain.model.RedSysOperation
import ifac.td.taxi.domain.model.RedSysLoginResponse.Error
import ifac.td.taxi.domain.model.RedSysLoginResponse.Success
import ifac.td.taxi.domain.model.RedSysReturn
import ifac.td.taxi.domain.model.RedSysSessionState
import ifac.td.taxi.domain.usecase.PrinterUseCase
import ifac.td.taxi.domain.usecase.RedSysUseCase
import ifac.td.taxi.domain.usecase.TripUseCase
import ifac.td.taxi.framework.TemporalData
import ifac.td.taxi.framework.sdk.usecase.TicketUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReceiptRedSysViewModel(
    context: Application,
    private val redSysUseCase: RedSysUseCase,
    private val ticketUseCase: TicketUseCase,
    private val printerUseCase: PrinterUseCase,
    private val redSysSessionState: RedSysSessionState,
    private val tripUseCase: TripUseCase,
) :
    BaseViewModel(context) {

    val TAG = "ReceiptRedSysViewModel"

    private val _refundResultFlow = MutableSharedFlow<Boolean>()
    val refundResultFlow = _refundResultFlow.asSharedFlow()

    private val _updateListFlow = MutableStateFlow<List<RedSysOperation>?>(null)
    val updateListFlow = _updateListFlow.asStateFlow()

    fun doRefund(redSysOperation: RedSysOperation, callback: (RedSysOperation) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val usernameRedSys = redSysUseCase.getUsernameRedSys()
                val passwordRedSys = TemporalData.passwordRedSys

                val loginResponse = usernameRedSys?.let { redSysUseCase.loginRedSys(it, passwordRedSys) }

                when (loginResponse) {
                    is Success -> {
                        val terminal = loginResponse.merchantList.firstOrNull()?.terminalList?.firstOrNull()

                        if (terminal == null) {
                            Logs.d(TAG, "No terminal available in the login response")
                            return@launch
                        }

                        val rtsIdentifierLast5 = redSysOperation.RTSIdentifier.takeLast(5)
                        val consultaResponse = redSysUseCase.consultaRedSys(terminal, null)

                        val paymentInfo = consultaResponse?.transactionDataList?.find {
                            it.identifierRTS.takeLast(5) == rtsIdentifierLast5
                        }

                        if (paymentInfo == null) {
                            Logs.d(TAG, "No payment info found for the given RTSIdentifier")
                            return@launch
                        }

                        val refundResponse = redSysUseCase.doRefund(terminal, paymentInfo)
                        redSysOperation.refundResponse = refundResponse
                        callback(redSysOperation)
                        processRefundResponse(refundResponse)
                    }
                    is Error -> {
                        Logs.e(TAG, "Error during login: ${loginResponse.errorCode} - ${loginResponse.errorMessage}")
                        _refundResultFlow.emit(false)
                    }

                    null -> {
                        Logs.e(TAG, "Login response is null")
                        _refundResultFlow.emit(false)
                    }
                }
            }.onFailure { e ->
                Logs.e(TAG, "Exception in doRefund: ${e.message}")
                _refundResultFlow.emit(false)
            }
        }
    }

    private suspend fun processRefundResponse(refundResponse: RedCLSRefundResponse?) {
        refundResponse?.let {
            val result = redSysUseCase.parseDevolucion(it)

            if (result != null) {
                when {
                    result.result.equals(RedSysOperation.AUTHORIZED, ignoreCase = true) -> {
                        Logs.d(TAG, "processRefundResponse: Refund authorized")
                        _refundResultFlow.emit(true)
                        printRefundTicket(refund = result)
                    }

                    else -> {
                        Logs.d(TAG, "processRefundResponse: Refund result: $result")
                        _refundResultFlow.emit(false)
                    }
                }
            }
        } ?: run {
            Logs.d(TAG, "processRefundResponse: Refund response is null")
            _refundResultFlow.emit(false)
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

    fun printRefundTicket(redSysOperation: RedSysOperation? = null, refund: RedSysReturn? = null) {
        viewModelScope.launch {
            val parsedResult = refund ?: redSysOperation?.refundResponse?.let {
                redSysUseCase.parseDevolucion(it)
            } ?: run {
                Logs.e(TAG, "printRefundTicket: Aborted. No refund data available.")
                return@launch
            }

            val terminalData = redSysSessionState.terminalData ?: run {
                Logs.e(TAG, "printRefundTicket: Aborted. No terminal data found in session state")
                return@launch
            }

            try {
                val ticket = ticketUseCase.prepareRedSysRefundTicket(
                    signature = false,
                    resultadoDevolucion = parsedResult,
                    terminalData = terminalData
                )

                Logs.d(TAG, "printRefundTicket: Ticket prepared successfully. Sending to printer...")

                printerUseCase.printTicket(ticket, true)
                Logs.d(TAG, "printRefundTicket: Print command sent.")
            } catch (e: Exception) {
                Logs.e(TAG, "printRefundTicket: Error during ticket preparation or printing: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private suspend fun fetchOperations(): List<RedSysOperation>? {
        Logs.d(TAG, "Fetching operations...")

        redSysUseCase.configureRedSys()
        Logs.d(TAG, "RedSys configured.")

        val usernameRedSys = redSysUseCase.getUsernameRedSys()
        if (usernameRedSys == null) {
            Logs.e(TAG, "Username RedSys is null.")
            return null
        }
        Logs.d(TAG, "Username RedSys: $usernameRedSys")

        val passwordRedSys = TemporalData.passwordRedSys

        val loginResponse = redSysUseCase.loginRedSys(usernameRedSys, passwordRedSys)

        return when (loginResponse) {
            is Success -> {
                val terminal = loginResponse.merchantList.firstOrNull()?.terminalList?.firstOrNull()

                if (terminal == null) {
                    Logs.e(TAG, "No terminal available in the login response.")
                    return null
                }
                Logs.d(TAG, "Terminal: $terminal")

                val consultaResponse = redSysUseCase.consultaRedSys(terminal, null)
                Logs.d(TAG, "consultaResponse: $consultaResponse")

                val operations = redSysUseCase.parseConsulta(consultaResponse)
                Logs.d(TAG, "Parsed operations: $operations")

                operations
            }

            is Error -> {
                Logs.e(TAG, "Login RedSys failed: ${loginResponse.errorCode} - ${loginResponse.errorMessage}")
                null
            }

            null -> {
                Logs.e(TAG, "Login response is null.")
                null
            }
        }
    }

    fun updateList() {
        viewModelScope.launch(Dispatchers.IO) {
            Logs.d(TAG, "Updating list...")
            val operations = fetchOperations()
            Logs.d(TAG, "Updated list: $operations")
            _updateListFlow.emit(operations)
        }
    }
}