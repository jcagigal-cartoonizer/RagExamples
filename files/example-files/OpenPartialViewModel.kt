package ifac.td.taxi.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import ifac.td.taxi.R
import ifac.td.taxi.domain.model.PartialModel
import ifac.td.taxi.domain.usecase.PartialUseCase
import ifac.td.taxi.domain.usecase.PrinterUseCase
import ifac.td.taxi.framework.sdk.usecase.LicensingUseCase
import ifac.td.taxi.framework.sdk.usecase.TaximeterConnectUseCase
import ifac.td.taxi.framework.sdk.usecase.TicketUseCase
import ifac.td.taxi.framework.util.Logs
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OpenPartialViewModel(
    context: Application,
    private val partialUseCase: PartialUseCase,
    private val printerUseCase: PrinterUseCase,
    private val licensingUseCase: LicensingUseCase,
    private val taximeterUseCase: TaximeterConnectUseCase,
    private val ticketUseCase: TicketUseCase,
) : BaseViewModel(context) {

    private val TAG = "OpenPartialViewModel"

    private val _partialFlow = MutableSharedFlow<PartialModel?>()
    val partialFlow = _partialFlow.asSharedFlow()

    private val _partialButtonFlow = MutableStateFlow<Boolean?>(null)
    val partialButtonFlow = _partialButtonFlow.asStateFlow()

    private val _isTaximeterConnectedFlow = MutableStateFlow<Boolean>(false)
    val isTaximeterConnectedFlow = _isTaximeterConnectedFlow.asStateFlow()


    val loadedTicketFiles: (() ->  Unit) = {
        viewModelScope.launch {
            partialUseCase.doPartials {
                Logs.d(TAG, "XXX getPartial: doPartials callback received result = $it")
                _partialFlow.emit(it)
            }
        }
    }

    fun getPartial() {
        Logs.d(TAG, "getPartial: Requesting partials data")
        viewModelScope.launch {
            //mirar si hay tickets, campos y ficheros cargados - callback
            ticketUseCase.loadCampos()
            ticketUseCase.loadTickets(loadedTicketFiles)
        }
    }


    fun closePartials() {
        viewModelScope.launch {
            Logs.d(TAG, "closePartials: executing closePartial useCase")
            partialUseCase.closePartial()
            Logs.d(TAG, "closePartials: navigating to ClosedPartialFragment")
            navigateTo(R.id.action_openPartialsFragment_to_closedPartialFragment)
        }
    }

    fun printPartial(ticket: String) {
        viewModelScope.launch {
            Logs.d(TAG, "printPartial: Attempting to print ticket (length: ${ticket.length})")
            printerUseCase.printTicket(buf = ticket, paperFeed = true)
        }
    }

    fun checkClosuresPermission() {
        viewModelScope.launch {
            Logs.d(TAG, "checkClosuresPermission: Checking parameters")
            val parameters = licensingUseCase.getLicensingParameters()
            Logs.d(TAG, "checkClosuresPermission: isClosuresButton allowed = ${parameters?.isClosuresButton}")
            _partialButtonFlow.emit(parameters?.isClosuresButton)
            _isTaximeterConnectedFlow.emit(taximeterUseCase.isTaximeterConnected())
        }
    }
}