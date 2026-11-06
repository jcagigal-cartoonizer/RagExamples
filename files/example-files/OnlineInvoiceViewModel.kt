package ifac.td.taxi.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import ifac.td.taxi.domain.model.Dispatch
import ifac.td.taxi.domain.model.SmartTDException
import ifac.td.taxi.domain.model.Trip
import ifac.td.taxi.ui.model.SmartTDUIException
import ifac.td.taxi.domain.usecase.BravoRestInvoiceUseCase
import ifac.td.taxi.domain.usecase.DispatchUseCase
import ifac.td.taxi.domain.usecase.TripUseCase
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.repository.connections.rest.invoice.Resource
import ifac.td.taxi.ui.model.FiscalData
import ifac.td.taxi.viewmodel.model.UtilsModel.Companion.toInfoDispatchModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OnlineInvoiceViewModel(
    context: Application,
    private val tripUseCase: TripUseCase,
    private val dispatchUseCase: DispatchUseCase,
    private val bravoRestInvoiceUseCase: BravoRestInvoiceUseCase
) : BaseViewModel(context) {

    private val TAG = "OnlineInvoiceViewModel"

    private val _invoiceFlow = MutableSharedFlow<Unit>()
    val invoiceFlow = _invoiceFlow.asSharedFlow()

    private val _fiscalDataFlow = MutableStateFlow<FiscalData?>(null)
    val fiscalDataFlow = _fiscalDataFlow.asStateFlow()

    private val _errorFlow = MutableSharedFlow<SmartTDUIException>()
    val errorFlow = _errorFlow.asSharedFlow()

    private val _tripFlow = MutableStateFlow<Trip?>(null)
    val tripFlow = _tripFlow.asStateFlow()

    fun getFiscalData(fiscalId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            Logs.d(TAG, "getFiscalData: Fetching fiscal data for ID = $fiscalId")
            when (val result = bravoRestInvoiceUseCase.getFiscalData(fiscalId)) {
                is Resource.Success -> {
                    result.data?.let {
                        Logs.d(TAG, "getFiscalData: Successfully fetched fiscal data")
                        _fiscalDataFlow.emit(it)
                    }
                }

                is Resource.Error -> {
                    result.exception?.let {
                        Logs.e(TAG, "getFiscalData: Error fetching fiscal data")
                        _errorFlow.emit(SmartTDUIException.fromException(it))
                    }
                }
            }
        }
    }

    fun generateInvoice(userFiscalData: FiscalData) {
        viewModelScope.launch(Dispatchers.IO) {
            Logs.d(TAG, "generateInvoice: Generating invoice")
            _tripFlow.value?.let { trip ->
                Logs.d(TAG, "generateInvoice: Trip ID = ${trip.id}")

                Logs.d(TAG, "generateInvoice: Fiscal data ID = ${userFiscalData.fiscalID}")
                var dispatch: Dispatch? = null
                if (trip.fromDispatch && trip.pkDispatchId != null) {
                    dispatch = dispatchUseCase.getDispatchById(trip.pkDispatchId!!)
                }

                when (val result = bravoRestInvoiceUseCase.generateInvoice(trip, dispatch?.toInfoDispatchModel(), fiscalData = userFiscalData)) {
                    is Resource.Success -> {
                        updateTripInvoiceStatus(trip)
                        _invoiceFlow.emit(Unit)
                    }
                    is Resource.Error -> {
                        result.exception?.let { error ->
                            _errorFlow.emit(SmartTDUIException.fromException(error))
                        }
                    }
                }
            }
        }
    }

    fun getTrip(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            Logs.d(TAG, "getTrip: Fetching trip with ID = $id")
            val trip = tripUseCase.getTripById(id)
            trip?.let {
                _tripFlow.emit(it)
            } ?: run { _errorFlow.emit(SmartTDUIException.fromException(SmartTDException.DATABASE_ERROR)) }
        }
    }

    private suspend fun updateTripInvoiceStatus(trip: Trip) {
        Logs.d(TAG, "updateTripInvoiceStatus: tripId: ${trip.id}")
        trip.invoiceAlreadyGenerated = true
        tripUseCase.updateTripInvoiceFlag(trip.id, trip.invoiceAlreadyGenerated)
    }
}