package ifac.td.taxi.compose.viewmodel
import ifac.td.taxi.ui.screen.state.DashboardDialogState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import ifac.td.taxi.ui.screen.state.DashboardUiEvent
import ifac.td.taxi.ui.screen.state.DashboardUiState
import ifac.td.taxi.ui.screen.state.MessageUiState
import ifac.td.taxi.domain.usecase.PendingTripsUseCaseImpl
import ifac.td.taxi.ui.screen.state.DashboardButtonsState
import com.interfacom.sdk.taximeter.bravocomm.rest.pending_trips.response.PendingTrip
import ifac.td.taxi.ui.screen.state.ComposeButtonState
import ifac.td.taxi.ui.screen.state.DashboardUiEffect
// // ## 3) Compose-friendly ViewModel with `UiState + UiEvent + SharedFlow<DashboardUiEffect>`

// This is a Compose version of your existing `DashboardViewModel`, preserving the same data source behavior and adding a single effect stream.


import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.interfacom.sdk.taximeter.bravocomm.BravoCentral
import com.interfacom.sdk.taximeter.bravocomm.W2CLocation
import com.interfacom.sdk.taximeter.licensing.models.zoning.LatLong
import com.interfacom.sdk.taximeter.licensing.models.zoning.Zone
import com.interfacom.sdk.taximeter.log.Log
import ifac.td.taxi.domain.usecase.PendingTripsUseCase
import ifac.td.taxi.framework.sdk.usecase.LicensingUseCase
import ifac.td.taxi.framework.sdk.usecase.ZoningUseCase
import ifac.td.taxi.ui.model.ZoneModel
import ifac.td.taxi.ui.util.ZoneUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DashboardComposeViewModel(
    private val pendingTripsUseCase: PendingTripsUseCase,
    private val zoningUseCase: ZoningUseCase,
    private val licensingUseCase: LicensingUseCase,
    application: Application,
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "DashboardComposeVM"
    }

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<DashboardUiEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<DashboardUiEffect> = _effects.asSharedFlow()

    private val handler = Handler(Looper.getMainLooper())
    private val pendingTripsHandler = Handler(Looper.getMainLooper())
    private var zonesRunnable: Runnable? = null
    private var pendingTripsRunnable: Runnable? = null

    private val _pendingTripsFlow = MutableStateFlow<ArrayList<PendingTrip>?>(null)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            licensingUseCase.getLicensingParameters()?.isCustomersAtStand?.let {
                updateButtonsForStandState(it)
            }
        }

        viewModelScope.launch {
            _pendingTripsFlow.collect { trips ->
                _uiState.update { it.copy(pendingTripsCount = trips?.size ?: 0) }
            }
        }
    }

    fun onEvent(event: DashboardUiEvent) {
        when (event) {
            DashboardUiEvent.OnResume -> onResume()
            DashboardUiEvent.OnPause -> onPause()
            is DashboardUiEvent.OnZoneClick -> {
                viewModelScope.launch {
                    _effects.emit(DashboardUiEffect.NavigateToZoneDetail(event.zone))
                }
            }
            is DashboardUiEvent.OnZoneLongClick -> {
                viewModelScope.launch {
                    _effects.emit(
                        DashboardUiEffect.OpenDialog(
                            DashboardDialogState(
                                title = "Favorite zone",
                                message = "Do you want to manage this zone in favorites?",
                                confirmText = "Yes",
                                dismissText = "No"
                            )
                        )
                    )
                }
            }
            DashboardUiEvent.OnLocateOnHiredClick -> {
                viewModelScope.launch {
                    _effects.emit(
                        DashboardUiEffect.OpenDialog(
                            DashboardDialogState(
                                title = "Locate on hired",
                                message = "Do you want to delocate the hired zone?",
                                confirmText = "Confirm",
                                dismissText = "Cancel"
                            )
                        )
                    )
                }
            }
            DashboardUiEvent.OnChangeButtonClick -> {
                viewModelScope.launch {
                    _effects.emit(DashboardUiEffect.ShowToast("Change button clicked"))
                }
            }
            DashboardUiEvent.OnDialogConfirm -> {
                viewModelScope.launch { _effects.emit(DashboardUiEffect.CloseDialog) }
            }
            DashboardUiEvent.OnDialogDismiss -> {
                viewModelScope.launch { _effects.emit(DashboardUiEffect.CloseDialog) }
            }
        }
    }

    private fun onResume() {
        initViewModelData()
        loadTrips(_pendingTripsFlow)
    }

    private fun onPause() {
        removeHandlerCallbacks()
    }

    fun loadTrips(flow: MutableStateFlow<ArrayList<PendingTrip>?>) {
        if (BravoCentral.isPendingTripsAllowed(false)) {
            refreshPendingTripsManual(flow)
        }
        startChronometer(flow)
    }

    private fun refreshPendingTripsManual(mFlow: MutableStateFlow<ArrayList<PendingTrip>?>) {
        viewModelScope.launch {
            pendingTripsUseCase.getPendingTripsZone(flow = mFlow)
        }
    }

    private fun startChronometer(mFlow: MutableStateFlow<ArrayList<PendingTrip>?>) {
        viewModelScope.launch(Dispatchers.IO) {
            val refreshRate = 2000L
            val initRefreshRate = 500L
            pendingTripsRunnable = Runnable {
                viewModelScope.launch {
                    if (BravoCentral.isPendingTripsAllowed(true)) {
                        pendingTripsUseCase.getPendingTripsZone(flow = mFlow)
                    }
                }
                pendingTripsHandler.postDelayed(pendingTripsRunnable!!, refreshRate)
            }
            pendingTripsHandler.postDelayed(pendingTripsRunnable!!, initRefreshRate)
        }
    }

    fun initViewModelData() {
        viewModelScope.launch {
            val refreshRate = licensingUseCase.getLicensingParameters()?.segRefrescoConsUbicacion
            refreshRate?.let { seconds ->
                val millis = (seconds * 1000).toLong()
                var lastZoningUpdate = zoningUseCase.getLastZoningUpdate()

                if ((System.currentTimeMillis() - lastZoningUpdate) < millis) {
                    zoneCallback(
                        W2CLocation.getZoning().macrozones[0].zones,
                        W2CLocation.getZoning().availableColumns
                    )
                }

                zonesRunnable = Runnable {
                    viewModelScope.launch {
                        val newCall = (System.currentTimeMillis() - lastZoningUpdate) > millis
                        if (newCall) {
                            zoningUseCase.getZonesInformation(
                                W2CLocation.getZoning().macrozones[0],
                                zoneCallback
                            )
                            lastZoningUpdate = System.currentTimeMillis()
                            zoningUseCase.setLastZoningUpdate()
                        }
                    }
                    handler.postDelayed(zonesRunnable!!, 1000)
                }
                handler.post(zonesRunnable!!)
            }
        }
    }

    private val zoneCallback: (List<Zone>, String) -> Unit = { _, availableColumns ->
        viewModelScope.launch(Dispatchers.IO) {
            val zonesList = mutableListOf<ZoneModel>()
            W2CLocation.getZoning().macrozones.forEach { mz ->
                zonesList.addAll(mz.zones.map { zone -> ZoneModel(zone, false) })
            }
            _uiState.update { it.copy(zoningDataTypes = availableColumns) }
            filterData(zonesList)
        }
    }

    private suspend fun filterData(zonesList: List<ZoneModel>) {
        val dataTypes = uiState.value.zoningDataTypes
        val zonesWithMoreTrips = zonesList.filter {
            ZoneUtils.checkDiff(it.zone, dataTypes) > 0
        }

        val (farZones, nearbyZones) = zonesWithMoreTrips.partition {
            getDistanceFromLatLonInMetersLocale(
                W2CLocation.getLastLatLong(),
                it.zone.centralCoord
            ) > 5000
        }

        val nearbyZonesSorted = nearbyZones.sortedWith(
            compareByDescending<ZoneModel> { ZoneUtils.checkDiff(it.zone, dataTypes) }
                .thenBy { getDistanceFromLatLonInMetersLocale(W2CLocation.getLastLatLong(), it.zone.centralCoord) }
        )
        val farZonesSorted = farZones.sortedWith(
            compareByDescending<ZoneModel> { ZoneUtils.checkDiff(it.zone, dataTypes) }
                .thenBy { getDistanceFromLatLonInMetersLocale(W2CLocation.getLastLatLong(), it.zone.centralCoord) }
        )

        _uiState.update {
            it.copy(
                nearbyZones = nearbyZonesSorted,
                farZones = farZonesSorted
            )
        }

        refreshActualZoneData()
    }

    fun refreshActualZoneData() {
        viewModelScope.launch {
            val lastZoneSent = W2CLocation.getZoning()
                .getZoneById(W2CLocation.getLastIdMacrozoneSent(), W2CLocation.getLastIdZoneSent())
            val lastZoneLocated = W2CLocation.getZoneFromLatLong(W2CLocation.getLastLatLong())

            val actualZoneList = buildList {
                if (lastZoneSent != null) add(ZoneModel(lastZoneSent, false))
                if (lastZoneLocated != null) add(ZoneModel(lastZoneLocated, false))
            }

            _uiState.update { it.copy(actualZones = actualZoneList) }
        }
    }

    private fun updateButtonsForStandState(isCustomersAtStand: Boolean) {
        _uiState.update {
            it.copy(
                buttonsState = it.buttonsState.copy(
                    locateButtonVisible = true,
                    locateButtonEnabled = true,
                    locateButtonTextRes = if (isCustomersAtStand) {
                        ifac.td.taxi.R.string.btn_delocate
                    } else {
                        ifac.td.taxi.R.string.btn_soon_to_clear
                    },
                    locateButtonType = if (isCustomersAtStand) {
                        DashboardButtonType.Red
                    } else {
                        DashboardButtonType.Blue
                    }
                )
            )
        }
    }

    private fun getDistanceFromLatLonInMetersLocale(
        lastLatLong: LatLong?,
        centralCoord: LatLong?
    ): Int {
        if (lastLatLong == null || lastLatLong.Lat.isNaN() || centralCoord == null || centralCoord.Lat.isNaN()) {
            return Int.MAX_VALUE
        }
        return W2CLocation.getDistanceFromLatLonInMeters(lastLatLong, centralCoord)
    }

    fun removeHandlerCallbacks() {
        pendingTripsRunnable?.let { pendingTripsHandler.removeCallbacks(it) }
        zonesRunnable?.let { handler.removeCallbacks(it) }
    }
}


