package ifac.td.taxi.viewmodel

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.viewModelScope
import com.interfacom.sdk.taximeter.bravocomm.BravoCentral
import com.interfacom.sdk.taximeter.bravocomm.W2CLocation
import com.interfacom.sdk.taximeter.bravocomm.rest.pending_trips.response.PendingTrip
import com.interfacom.sdk.taximeter.licensing.models.zoning.LatLong
import com.interfacom.sdk.taximeter.licensing.models.zoning.Zone
import com.interfacom.sdk.taximeter.log.Log
import ifac.td.taxi.domain.usecase.PendingTripsUseCase
import ifac.td.taxi.framework.sdk.usecase.LicensingUseCase
import ifac.td.taxi.framework.sdk.usecase.ZoningUseCase
import ifac.td.taxi.repository.room.entities.BravoConfigurationVariableEntity
import ifac.td.taxi.ui.model.ZoneModel
import ifac.td.taxi.ui.util.ZoneUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val pendingTripsUseCase: PendingTripsUseCase,
    private val zoningUseCase: ZoningUseCase,
    private val licensingUseCase: LicensingUseCase,
    context: Application,
) : BaseViewModel(context) {

    val TAG = "DashboardViewModel"

    private val _zonesListFlow = MutableStateFlow<List<ZoneModel>?>(null)
    val zonesListFlow = _zonesListFlow.asStateFlow()

    private val _nearbyZonesListSortedFlow = MutableStateFlow<List<ZoneModel>?>(null)
    val nearbyZonesListSortedFlow = _nearbyZonesListSortedFlow.asStateFlow()

    private val _farZonesListSortedFlow = MutableStateFlow<List<ZoneModel>?>(null)
    val farZonesListSortedFlow = _farZonesListSortedFlow.asStateFlow()

    private val _actualZoneListSortedFlow = MutableStateFlow<List<ZoneModel>?>(null)
    val actualZoneListSortedFlow = _actualZoneListSortedFlow.asStateFlow()

    private val _zoningListDataTypes = MutableStateFlow<String?>(null)
    val zoningListDataTypes = _zoningListDataTypes.asStateFlow()

    var bravoConfiguration: BravoConfigurationVariableEntity? = null

    val handler = Handler(Looper.getMainLooper())

    private var zonesRunnable: Runnable? = null

    var isCustomersAtStand: Boolean = false

    private val pendingTripsHandler = Handler(Looper.getMainLooper())

    private var pendingTripsRunnable: Runnable? = null

    fun loadTrips(flow: MutableStateFlow<ArrayList<PendingTrip>?>) {
        if (BravoCentral.isPendingTripsAllowed(false)) {
            refreshPendingTripsManual(flow)
        }
        startChronometer(flow)
    }

    private fun refreshPendingTripsManual(mFlow: MutableStateFlow<ArrayList<PendingTrip>?>) {
        viewModelScope.launch {
            pendingTripsUseCase.getPendingTripsZone(
                flow = mFlow
            )
        }
    }

    private fun startChronometer(mFlow: MutableStateFlow<ArrayList<PendingTrip>?>) {
        viewModelScope.launch(Dispatchers.IO) {
            val refreshRate = 2000L
            val initRefreshRate = 500L

            pendingTripsRunnable = object : java.lang.Runnable {
                override fun run() {
                    viewModelScope.launch {
                        if (BravoCentral.isPendingTripsAllowed(true)) {
                            pendingTripsUseCase.getPendingTripsZone(
                                flow = mFlow
                            )
                        }
                    }
                    pendingTripsHandler.postDelayed(this, refreshRate)
                }
            }
            pendingTripsRunnable?.let {
                // Usar postDelayed en lugar de post para evitar llamada duplicada
                // con refreshPendingTripsManual() que ya se ejecuta en loadTrips()
                pendingTripsHandler.postDelayed(it, initRefreshRate)
            }
        }
    }

    private val zoneCallback: (List<Zone>, String) -> Unit = { zones, availableColumns ->
        viewModelScope.launch(Dispatchers.IO) {
            Log.d(TAG, "zoneCallback called")
            val zonesList = mutableListOf<ZoneModel>()
            W2CLocation.getZoning().macrozones.forEach { mz ->
                zonesList.addAll(mz.zones.map { zone: Zone ->
                    ZoneModel(
                        zone,
                        false
                    )
                })
            }

            if (_zonesListFlow.value == zonesList) {
                Log.d(TAG, "zoneCallback: _zonesListFlow wont emit. Filtering possible data")
                filterData(zonesList)
            }
            _zonesListFlow.emit(zonesList)
            if (availableColumns.isNotBlank()) {
                _zoningListDataTypes.emit(availableColumns)
            }
        }
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            licensingUseCase.getLicensingParameters()?.isCustomersAtStand?.let {
                isCustomersAtStand = it
            }
        }
        viewModelScope.launch() {
            zonesListFlow.collect {
                it?.let { zones ->
                    Log.d(TAG, "zoneListFlow: updated")
                    filterData(
                        zones,
                    )
                }
            }
        }
    }

    fun initViewModelData() {
        viewModelScope.launch {
            val refreshRate = licensingUseCase.getLicensingParameters()?.segRefrescoConsUbicacion

            refreshRate?.let { seconds ->
                val millis = (seconds * 1000).toLong()
                var lastZoningUpdate = zoningUseCase.getLastZoningUpdate()

                val shouldLoadData = (System.currentTimeMillis() - lastZoningUpdate) < millis

                if (shouldLoadData) {
                    //TODO: Manual checks
                    //No need to send the zone
                    zoneCallback.invoke(W2CLocation.getZoning().macrozones[0].zones, W2CLocation.getZoning().availableColumns)
                }

                zonesRunnable = object : Runnable {
                    override fun run() {
                        viewModelScope.launch {
                            val newCall = (System.currentTimeMillis() - lastZoningUpdate) > millis

                            if (newCall) {
                                //No need to send MZ
                                zoningUseCase.getZonesInformation(
                                    W2CLocation.getZoning().macrozones[0],
                                    zoneCallback,
                                )

                                lastZoningUpdate = System.currentTimeMillis()
                                zoningUseCase.setLastZoningUpdate()
                            }
                        }
                        handler.postDelayed(this, 1000)
                    }
                }

                zonesRunnable?.let {
                    handler.post(it)
                }
            }
        }
    }

    private suspend fun filterData(zonesList: List<ZoneModel>) {
        //Left Side
        val zonesWithMoreTrips = zonesList.filter {
            ZoneUtils.checkDiff(it.zone, zoningListDataTypes.value ?: "") > 0
        }.toMutableList()

        val (farZones, nearbyZones) = zonesWithMoreTrips.partition {
            getDistanceFromLatLonInMetersLocale(
            W2CLocation.getLastLatLong(),
            it.zone.centralCoord
        ) > 5000 }

        val nearbyZonesSorted = nearbyZones.sortedWith(compareByDescending<ZoneModel> { ZoneUtils.checkDiff(it.zone, zoningListDataTypes.value ?: "") }.thenBy { getDistanceFromLatLonInMetersLocale(W2CLocation.getLastLatLong(),it.zone.centralCoord) })
        val farZonesSorted = farZones.sortedWith(compareByDescending<ZoneModel> { ZoneUtils.checkDiff(it.zone, zoningListDataTypes.value ?: "") }.thenBy { getDistanceFromLatLonInMetersLocale(W2CLocation.getLastLatLong(),it.zone.centralCoord) })

        _nearbyZonesListSortedFlow.emit(nearbyZonesSorted)
        _farZonesListSortedFlow.emit(farZonesSorted)

        refreshActualZoneData()
    }

    fun refreshActualZoneData() {
        viewModelScope.launch {
            //Right Side
            val lastZoneSent = W2CLocation.getZoning().getZoneById(W2CLocation.getLastIdMacrozoneSent(), W2CLocation.getLastIdZoneSent())
            val lastZoneLocated = W2CLocation.getZoneFromLatLong(W2CLocation.getLastLatLong())


            var soonInZone: Zone? = null
            if (W2CLocation.getIsInSoonInZone()) {
                soonInZone = W2CLocation.getZoning().getZoneById(
                    W2CLocation.getSoonInZoneMacroZoneId(),
                    W2CLocation.getSoonInZoneZoneId()
                )
            }

            val actualZoneList = mutableListOf<ZoneModel>()

            if (lastZoneSent != null) {
                actualZoneList.add(ZoneModel(lastZoneSent, false))
            }

            if (lastZoneLocated != null) {
                actualZoneList.add(ZoneModel(lastZoneLocated, false))
            }

            /*
            if (soonInZone != null) {
                actualZoneList.add(ZoneModel(soonInZone, false))
            }

             */

            _actualZoneListSortedFlow.emit(actualZoneList)
        }
    }

    private fun getDistanceFromLatLonInMetersLocale(
        lastLatLong: LatLong?,
        centralCoord: LatLong?
    ): Int {
        if (lastLatLong == null || lastLatLong.Lat.isNaN() || centralCoord == null || centralCoord.Lat.isNaN()) {
            return Integer.MAX_VALUE
        }

        return W2CLocation.getDistanceFromLatLonInMeters(lastLatLong, centralCoord)
    }

    fun removeHandlerCallbacks() {
        pendingTripsRunnable?.let {
            pendingTripsHandler.removeCallbacks(it)
        }

        zonesRunnable?.let {
            handler.removeCallbacks(it)
        }
    }
}