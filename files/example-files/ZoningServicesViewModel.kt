package ifac.td.taxi.viewmodel

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.viewModelScope
import com.interfacom.sdk.taximeter.bravocomm.rest.zone_trips.response.ZoneTrip
import ifac.td.taxi.domain.usecase.ShiftStatusUseCase
import ifac.td.taxi.framework.sdk.usecase.LicensingUseCase
import ifac.td.taxi.framework.sdk.usecase.ZoningUseCase
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.repository.room.dao.BravoConfigurationVariableDao
import ifac.td.taxi.ui.model.ZoneTripModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ZoningServicesViewModel(
    private val zoningUseCase: ZoningUseCase,
    private val licensingUseCase: LicensingUseCase,
    private val shiftStatusUseCase: ShiftStatusUseCase,
    private val bravoConfigurationVariableDao: BravoConfigurationVariableDao,
    context: Application,
) : BaseViewModel(context) {

    val TAG = "ZoningServicesViewModel"

    private val _zoneTripsListFlow = MutableStateFlow<List<ZoneTripModel>?>(null)
    val zoneTripsListFlow = _zoneTripsListFlow.asStateFlow()

    private val _enableShowAllButtonFlow = MutableStateFlow<Boolean?>(null)
    val enableShowAllButtonFlow = _enableShowAllButtonFlow.asStateFlow()

    var showAllTrips: Boolean = false

    var companyNumber: String? = null

    val handler = Handler(Looper.getMainLooper())

    private lateinit var zonesCarsRunnable: Runnable

    private val _refreshProgressFlow = MutableStateFlow(0)
    val refreshProgressFlow = _refreshProgressFlow.asStateFlow()

    private val callback: (List<ZoneTrip>?) -> Unit = { trips ->
        viewModelScope.launch {
            Logs.d(TAG, "ZoningServicesViewModel.callback: received ${trips?.size ?: 0} trips")
            _zoneTripsListFlow.emit(trips?.map {
                ZoneTripModel(
                    id = it.tripID,
                    pickupTime = it.pickupTime,
                    company = it.company,
                    passengerNumber = it.passengerNumber,
                    baggageNumber = it.baggageNumber,
                    vehicleRequirements = it.vehicleRequirements,
                    driverRequirements = it.driverRequirements,
                    maxDistanceZoneService = it.maxDistanceZoneService,
                    ownNearbyVehicles = it.ownNearbyVehicles,
                    totalNearbyVehicles = it.totalNearbyVehicles,
                    myOrder = it.myOrder,
                )
            })
            Logs.d(TAG, "ZoningServicesViewModel.callback: trips mapped and emitted to zoneTripsListFlow")
        }
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            companyNumber = bravoConfigurationVariableDao.getBravoConfigurationVariable()?.companyNumber
            Logs.d(TAG, "ZoningServicesViewModel.init: companyNumber = $companyNumber")

            launch {
                bravoConfigurationVariableDao.getBravoConfigurationVariable()?.let { config ->
                    _enableShowAllButtonFlow.emit(config.requestAllServices)
                    Logs.d(TAG, "ZoningServicesViewModel.init: requestAllServices = ${config.requestAllServices}")
                }
            }
        }
    }

    fun initViewModelData(idMacroZone: Int, idZone: Int) {
        // Evita que se acumulen varios runnables al volver a entrar en la pantalla:
        // onResume() vuelve a llamar a este metodo y onPause() no siempre limpia a tiempo,
        // porque el runnable se asigna dentro de una corrutina asincrona.
        removeHandlerCallbacks()

        viewModelScope.launch {
            Logs.d(TAG, "initViewModelData: loading data for macroZone=$idMacroZone zone=$idZone")
            val refreshRate = licensingUseCase.getLicensingParameters()?.segRefrescoConsUbicacion
            Logs.d(TAG, "initViewModelData: refreshRate = $refreshRate")
            refreshRate?.let { seconds ->
                val millis = (seconds * 1000).toLong()

                var lastUpdate = System.currentTimeMillis()

                // Initial fetch
                zoningUseCase.getServicesInformation(
                    idMacroZone,
                    idZone,
                    showAllTrips,
                    bravoConfigurationVariableDao.getBravoConfigurationVariable()?.showCarsInTripRange,
                    callback
                )
                _refreshProgressFlow.emit(1000)

                // Se vuelve a limpiar justo antes de programar el nuevo runnable para cubrir
                // posibles condiciones de carrera entre varias llamadas asincronas a initViewModelData.
                removeHandlerCallbacks()

                zonesCarsRunnable = object : Runnable {
                    override fun run() {
                        viewModelScope.launch {
                            val elapsed = System.currentTimeMillis() - lastUpdate
                            val progress = 1000 - ((elapsed.toFloat() / millis) * 1000).toInt().coerceIn(0, 1000)
                            _refreshProgressFlow.emit(progress)

                            // La peticion SOLO se realiza cuando ha transcurrido el refreshRate completo.
                            if (elapsed >= millis) {
                                Logs.d(TAG, "zonesCarsRunnable: fetching services information")
                                zoningUseCase.getServicesInformation(
                                    idMacroZone,
                                    idZone,
                                    showAllTrips,
                                    bravoConfigurationVariableDao.getBravoConfigurationVariable()?.showCarsInTripRange,
                                    callback
                                )
                                lastUpdate = System.currentTimeMillis()
                                _refreshProgressFlow.emit(1000)
                            }
                        }
                        handler.postDelayed(this, 1000) // Check every second (solo para la barra de progreso)
                    }
                }
                handler.post(zonesCarsRunnable)
                Logs.d(TAG, "initViewModelData: zonesCarsRunnable started")
            }
        }
    }

    fun removeHandlerCallbacks() {
        Logs.d(TAG, "removeHandlerCallbacks: removing callbacks")
        if (::zonesCarsRunnable.isInitialized) {
            handler.removeCallbacks(zonesCarsRunnable)
        }
    }

    fun setShowAllTrips(b: Boolean, idMacroZone: Int, idZone: Int) {
        Logs.d(TAG, "setShowAllTrips: setting showAllTrips=$b and fetching data")
        showAllTrips = b
        viewModelScope.launch {
            zoningUseCase.getServicesInformation(
                idMacroZone,
                idZone,
                showAllTrips,
                bravoConfigurationVariableDao.getBravoConfigurationVariable()?.showCarsInTripRange,
                callback
            )
        }

    }

    fun isHired(state: Int): Boolean {
        val result = shiftStatusUseCase.isHired(state)
        Logs.d(TAG, "isHired: state=$state result=$result")
        return result
    }
}