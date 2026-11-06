package ifac.td.taxi.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import ifac.td.taxi.domain.model.UserPreferences
import ifac.td.taxi.domain.usecase.DispatchUseCase
import ifac.td.taxi.domain.usecase.TripUseCase
import ifac.td.taxi.domain.usecase.UserPreferencesUseCase
import ifac.td.taxi.framework.sdk.usecase.ReceiptUseCase
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.framework.util.tickets.TicketsPrinter
import ifac.td.taxi.viewmodel.model.UtilsModel.Companion.toInfoDispatchModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class OfflineInvoiceViewModel(
    context: Application,
    private val receiptUseCase: ReceiptUseCase,
    private val tripUseCase: TripUseCase,
    private val dispatchUseCase: DispatchUseCase,
    private val userPreferencesUseCase: UserPreferencesUseCase,
) : BaseViewModel(context) {

    private val TAG = "OfflineInvoiceViewModel"

    private val _invoiceFlow = MutableSharedFlow< Pair<String,String> >()
    val invoiceFlow = _invoiceFlow.asSharedFlow()

    private val _preferencesFlow = MutableSharedFlow<UserPreferences?>()
    val preferencesFlow = _preferencesFlow.asSharedFlow()

    fun doBilling(
        idTrip: Long,
        driverDirection: String,
        driverPostalCode: String,
        driverCity: String,
        clientNameAndSurname: String,
        clientNIF: String,
        clientDirection: String,
        clientPostalCode: String,
        clientCity: String
    ) {
        viewModelScope.launch {
            Logs.d(TAG, "doBilling: idTrip: $idTrip")
            tripUseCase.getTripById(idTrip)?.let { trip ->
                Logs.d(TAG, "doBilling: for trip with id = ${trip.id}")

                val receipt = receiptUseCase.processReceipt(
                    trip = trip,
                    driverDirection = driverDirection,
                    driverPostalCode = driverPostalCode,
                    driverCity = driverCity,
                    clientNameAndSurname = clientNameAndSurname,
                    clientNIF = clientNIF,
                    clientDirection = clientDirection,
                    clientPostalCode = clientPostalCode,
                    clientCity = clientCity,
                )
                Logs.d(TAG, "doBilling: receipt: $receipt")

                val tripDispatch = dispatchUseCase.getDispatchByTripId(idTrip)
                val brokenDownTaxTicket = TicketsPrinter.getBrokenDownTax(trip, context, tripDispatch?.toInfoDispatchModel())
                Logs.d(TAG, "doBilling: brokenDownTaxTicket: $brokenDownTaxTicket")

                _invoiceFlow.emit(Pair(receipt, brokenDownTaxTicket))
            } ?: run {
                Logs.d(TAG, "doBilling: trip is null")
            }
        }
    }

    fun checkUserPreferencesSettings() {
        viewModelScope.launch {
            Logs.d(TAG, "checkUserPreferencesSettings: getting user preferences")
            val preferences = userPreferencesUseCase.getUserPreferences()
            _preferencesFlow.emit(preferences)
        }
    }

    fun updateTripInvoiceStatus(idTrip: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            Logs.d(TAG, "updateTripInvoiceStatus: tripId: $idTrip")
            tripUseCase.getTripById(idTrip)?.let { trip ->
                trip.invoiceAlreadyGenerated = true
                tripUseCase.updateTripInvoiceFlag(trip.id, trip.invoiceAlreadyGenerated)
            }
        }
    }
}