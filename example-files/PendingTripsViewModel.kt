package ifac.td.taxi.viewmodel

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.viewModelScope
import com.interfacom.sdk.taximeter.bravocomm.BravoCentral
import com.interfacom.sdk.taximeter.bravocomm.rest.pending_trips.response.PendingTrip
import ifac.td.taxi.domain.usecase.PendingTripsUseCase
import ifac.td.taxi.domain.usecase.UserPreferencesUseCase
import ifac.td.taxi.framework.sdk.bravocentral.usecase.BravoCentralUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PendingTripsViewModel(
    private val pendingTripsUseCase: PendingTripsUseCase,
    private val bravoCentralUseCase: BravoCentralUseCase,
    private val userPreferencesUseCase: UserPreferencesUseCase,
    context: Application,
) : BaseViewModel(context) {

    val TAG = "PendingTripsViewModel"

    private val _showDialogOnAcceptFlow = MutableStateFlow<Boolean?>(true)
    val showDialogOnAcceptFlow = _showDialogOnAcceptFlow.asStateFlow()


    private val handler = Handler(Looper.getMainLooper())

    private lateinit var runnable: Runnable

    fun loadTrips(flow: MutableStateFlow<ArrayList<PendingTrip>?>, forced: Boolean = false) {
        if (BravoCentral.isPendingTripsAllowed(false) || forced) {
            refreshPendingTripsManual(flow, forced)
        }
        startChronometer(flow)
    }

    private fun refreshPendingTripsManual(mFlow: MutableStateFlow<ArrayList<PendingTrip>?>, forced: Boolean = false) {
        viewModelScope.launch {
            pendingTripsUseCase.getPendingTripsZone(
                flow = mFlow,
                forced = forced
            )
        }
    }

    private fun startChronometer(mFlow: MutableStateFlow<ArrayList<PendingTrip>?>) {
        viewModelScope.launch(Dispatchers.IO) {
            val refreshRate = 2000L
            val initRefreshRate = 500L

            runnable = object : Runnable {
                override fun run() {
                    viewModelScope.launch {
                        if (BravoCentral.isPendingTripsAllowed(true)) {
                            pendingTripsUseCase.getPendingTripsZone(
                                flow = mFlow
                            )
                        }
                    }
                    handler.postDelayed(this, refreshRate)
                }
            }
            // Usar postDelayed en lugar de post para evitar llamada duplicada
            // con refreshPendingTripsManual() que ya se ejecuta en loadTrips()
            handler.postDelayed(runnable, initRefreshRate)
        }
    }

    fun removeHandlerCallback() {
        handler.removeCallbacks(runnable)
    }

    fun requestTrip(trip: PendingTrip) {
        bravoCentralUseCase.requestPendingTrip(trip)
    }

    fun checkAcceptDialogPermission() {
        viewModelScope.launch {
            val preferences = userPreferencesUseCase.getUserPreferences()
            preferences?.showConfAcceptDispatch?.let { _showDialogOnAcceptFlow.emit(it) }
        }
    }

    fun checkClosePendingIfEmpty() {
        viewModelScope.launch {
            userPreferencesUseCase.getUserPreferences()?.let {
                if (it.closePendingTripsIfEmpty) {
                    navigateBack()
                }
            }
        }
    }
}