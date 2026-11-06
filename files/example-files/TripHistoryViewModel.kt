package ifac.td.taxi.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.viewModelScope
import ifac.td.taxi.R
import ifac.td.taxi.domain.model.Trip
import ifac.td.taxi.domain.usecase.PortugalUseCase
import ifac.td.taxi.domain.usecase.PrinterUseCase
import ifac.td.taxi.domain.usecase.TripUseCase
import ifac.td.taxi.framework.util.Logs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TripHistoryViewModel(
    context: Application,
    val tripUseCase: TripUseCase,
    val printerUseCase: PrinterUseCase,
    val portugalUseCase: PortugalUseCase,
) : BaseViewModel(context) {

    private val TAG = "TripHistoryViewModel"

    private val _tripListFlow = MutableStateFlow<List<Trip>?>(null)
    val tripListFlow = _tripListFlow.asStateFlow()

    private val _loadingFlow = MutableStateFlow(false)
    val loadingFlow = _loadingFlow.asStateFlow()

    var isLastPage = false
    private val PAGE_SIZE = 25;

    fun getTrips(page: Int) {
        viewModelScope.launch {
            try {
                Logs.d(TAG, "getTrips: page: $page")
                if (isLastPage) {
                    return@launch
                }

                _loadingFlow.value = true
                val loadedItems = page * PAGE_SIZE
                Logs.d(TAG, "getTrips: loadedItems $loadedItems")
                val newTrips = tripUseCase.getAllTripsFinishedPaged(loadedItems)
                Logs.d(TAG, "getTrips: newTrips ${newTrips.size}")
                val currentTrips = _tripListFlow.value ?: emptyList()
                Logs.d(TAG, "getTrips: currentTrips ${currentTrips.size}")

                if (newTrips.size < PAGE_SIZE) {
                    isLastPage = true
                }

                Logs.d(TAG, "getTrips: isLastPage $isLastPage")

                val combinedTrips = (currentTrips + newTrips).distinctBy { it.id }

                _tripListFlow.emit(combinedTrips)
            } catch (e: Exception) {
                Logs.e(TAG, "getTrips: error: ${e.message}")
            } finally {
                _loadingFlow.value = false
            }
        }
    }


    fun deleteSelectedTrips(selectedTrips: Set<Trip>, callback: () -> Unit) {
        viewModelScope.launch {
            if (selectedTrips.isNotEmpty()) {
                val selectedTripsList = selectedTrips.toList()
                for (trip in selectedTripsList) {
                    tripUseCase.deleteTrip(trip)
                    Logs.d("TripHistoryViewModel", "trip ${trip.id} deleted ")
                    callback.invoke()
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.toast_trips_eliminados_correctamente),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.toast_seleccione_algun_trip), Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }

        }
    }

    fun printTicket(trip: Trip) {
        viewModelScope.launch {
            portugalUseCase.increaseTicketPrinted(trip)
            printerUseCase.printTicket(trip, true)
        }
    }
}
