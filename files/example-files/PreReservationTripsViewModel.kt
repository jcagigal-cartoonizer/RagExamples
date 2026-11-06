package ifac.td.taxi.viewmodel

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.viewModelScope
import com.interfacom.sdk.taximeter.bravocomm.models.prereservations.Prereservation
import com.interfacom.sdk.taximeter.bravocomm.rest.pending_trips.response.PendingTrip
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.domain.usecase.PreReservationUseCase
import ifac.td.taxi.domain.usecase.UserPreferencesUseCase
import ifac.td.taxi.framework.sdk.bravocentral.usecase.BravoCentralUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PreReservationTripsViewModel(
    private val preReservationUseCase: PreReservationUseCase,
    private val bravoCentralUseCase: BravoCentralUseCase,
    private val userPreferencesUseCase: UserPreferencesUseCase,
    context: Application,
) : BaseViewModel(context) {

    val TAG = "PendingtripsViewModel"

    private val _preReservationTripsListFlow = MutableStateFlow<List<Prereservation>?>(null)
    val preReservationTripsListFlow = _preReservationTripsListFlow.asStateFlow()

    private val _bidFlow = MutableSharedFlow<Pair<Boolean, Boolean>>()
    val bidFlow = _bidFlow.asSharedFlow()

    private val _errorFlow = MutableSharedFlow<Boolean>()
    val errorFlow = _errorFlow.asSharedFlow()

    private val _showDialogOnAcceptFlow = MutableStateFlow<Boolean?>(true)
    val showDialogOnAcceptFlow = _showDialogOnAcceptFlow.asStateFlow()

    private lateinit var runnable : Runnable

    private val handler = Handler(Looper.getMainLooper())

    init {
        val refreshRate = 30

        refreshRate.let { seconds ->
            val millis = (seconds * 1000).toLong()
            runnable = object : Runnable {
                override fun run() {
                    viewModelScope.launch {
                        preReservationUseCase.requestPreReservations(_preReservationTripsListFlow, _errorFlow)
                    }
                    handler.postDelayed(this, millis)
                }
            }
            handler.post(runnable)
        }

        viewModelScope.launch {
            bidFlow.collect { (wasCompleted, cancelPreReservation) ->
                Logs.d(TAG, "bidFlow called from PreReservationTripsViewModel wasCompleted: $wasCompleted, requestType: ${if (!cancelPreReservation) "PreReserve" else "Cancel PreReservation" }")

                if (wasCompleted) {
                    Logs.d(TAG, "Completed request from bidPreReservation()")
                    preReservationUseCase.requestPreReservations(_preReservationTripsListFlow, _errorFlow)
                } else {
                    Logs.d(TAG, "Error from bidPreReservation()")
                }
            }
        }
    }

    fun removeHandlerCallback() {
        handler.removeCallbacks(runnable)
    }

    fun requestTrip(trip: PendingTrip) {
        bravoCentralUseCase.requestPendingTrip(trip)
    }

    fun preReserveTrip(it: Prereservation, remove: Boolean) {
        viewModelScope.launch {
            preReservationUseCase.bidPreReservation(it, remove, _bidFlow)
        }
    }
}