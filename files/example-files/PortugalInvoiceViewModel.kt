package ifac.td.taxi.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import ifac.td.taxi.domain.model.Trip
import ifac.td.taxi.domain.usecase.DispatchUseCase
import ifac.td.taxi.domain.usecase.PortugalUseCase
import ifac.td.taxi.domain.usecase.PrinterUseCase
import ifac.td.taxi.domain.usecase.ShiftStatusUseCase
import ifac.td.taxi.domain.usecase.TripUseCase
import ifac.td.taxi.domain.utils.CountryUtils
import ifac.td.taxi.framework.sdk.usecase.TicketUseCase
import ifac.td.taxi.framework.sdk.usecase.TicketUseCaseImpl
import ifac.td.taxi.ui.screen.PortugalInvoiceFragmentDirections
import ifac.td.taxi.viewmodel.model.InfoDispatchModel
import ifac.td.taxi.viewmodel.model.UtilsModel.Companion.toInfoDispatchModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PortugalInvoiceViewModel(
    context: Application,
    private val portugalUseCase: PortugalUseCase,
    private val tripUseCase: TripUseCase,
    private val ticketUseCase: TicketUseCase,
    private val dispatchUseCase: DispatchUseCase,
    private val shiftStatusUseCase: ShiftStatusUseCase,
    private val printerUseCase: PrinterUseCase,
) : BaseViewModel(context) {

    private val _externalCustomerFlow = MutableStateFlow(false)
    val externalCustomerFlow = _externalCustomerFlow.asStateFlow()

    fun acceptInvoice(
        nif: String,
        name: String,
        localidade: String,
        country: String
    ) {
        viewModelScope.launch {
            val getLastTrip = tripUseCase.getLastTrip()
            getLastTrip?.let { trip ->
                trip.nif = nif
                trip.name = name
                trip.localidade = localidade
                trip.country = country
                trip.iso = CountryUtils.arrayISOCountries[CountryUtils.arrayCountries.indexOf(country)]
                tripUseCase.updateTripPortugalThings(trip.id, trip.nif, trip.name, trip.localidade, trip.country, trip.iso)
                portugalUseCase.calculateHashPortugal(true, trip)
                prepareTicket(trip)
            }
        }
    }

    private suspend fun prepareTicket(trip: Trip) {
        val dispatch : InfoDispatchModel? = if (trip.fromDispatch) {
            dispatchUseCase.getDispatchByTripId(trip.id)?.let {
                it.id?.let { id ->
                    it.toInfoDispatchModel()
                }
            }
        } else {
            null
        }
        val destination = PortugalInvoiceFragmentDirections.actionPortugalInvoiceFragmentToReceiptHistoryFragment(-1L)
        val callback = object : TicketUseCaseImpl.PrepareTicketCallback {
            override fun onSuccess() {
                viewModelScope.launch {
                    navigateTo(destination)
                }
            }
        }
        shiftStatusUseCase.getStatus()?.let { shiftStatus ->
            ticketUseCase.prepareTicket(
                trip = trip,
                dispatch = dispatch,
                shiftStatus = shiftStatus,
                portugalByPass = true,
                ticketPrinted = true,
                callback = callback
            )
        }
    }

    fun setExternalCustomer(externalCustomer: Boolean) {
        viewModelScope.launch {
            _externalCustomerFlow.emit(externalCustomer)
        }
    }
}
