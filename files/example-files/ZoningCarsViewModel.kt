package ifac.td.taxi.viewmodel

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.viewModelScope
import com.interfacom.sdk.taximeter.bravocomm.BravoCentral
import com.interfacom.sdk.taximeter.bravocomm.W2CLocation
import com.interfacom.sdk.taximeter.bravocomm.rest.pending_trips.response.PendingTrip
import com.interfacom.sdk.taximeter.licensing.models.zoning.Zone
import ifac.td.taxi.domain.usecase.LocationUseCase
import ifac.td.taxi.domain.usecase.PendingTripsUseCase
import ifac.td.taxi.domain.usecase.ShiftStatusUseCase
import ifac.td.taxi.domain.usecase.UserPreferencesUseCase
import ifac.td.taxi.framework.sdk.usecase.LicensingUseCase
import ifac.td.taxi.framework.sdk.usecase.ZoningUseCase
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.repository.room.dao.BravoConfigurationVariableDao
import ifac.td.taxi.repository.room.entities.BravoConfigurationVariableEntity
import ifac.td.taxi.ui.model.LocationEnum
import ifac.td.taxi.ui.model.ZoneCarModel
import ifac.td.taxi.ui.model.ZoneCarRowModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ZoningCarsViewModel(
    private val zoningUseCase: ZoningUseCase,
    private val licensingUseCase: LicensingUseCase,
    private val locationUseCase: LocationUseCase,
    private val shiftStatusUseCase: ShiftStatusUseCase,
    private val bravoConfigurationVariableDao: BravoConfigurationVariableDao,
    private val userPreferencesUseCase: UserPreferencesUseCase,
    private val pendingTripsUseCase: PendingTripsUseCase,
    context: Application,
) : BaseViewModel(context) {

    val TAG = "ZoningCarsViewModel"

    private val _locationOnHiredSentFlow = MutableSharedFlow<Zone?>()
    val locationOnHiredSentFlow = _locationOnHiredSentFlow.asSharedFlow()

    private val _carsListFlow = MutableStateFlow<List<ZoneCarModel>?>(null)
    val carsListFlow = _carsListFlow.asStateFlow()

    private val _pendingServicesButtonPressedCallback = MutableSharedFlow<Boolean>()
    val pendingServicesButtonPressedCallback = _pendingServicesButtonPressedCallback.asSharedFlow()

    private val _pendingServicesButtonFlow = MutableStateFlow<Boolean?>(null)
    val pendingServicesButtonFlow = _pendingServicesButtonFlow.asStateFlow()

    private val _pendingServicesHiredButtonFlow = MutableStateFlow<Boolean?>(null)
    val pendingServicesHiredButtonFlow = _pendingServicesHiredButtonFlow.asStateFlow()

    var bravoConfiguration: BravoConfigurationVariableEntity? = null

    val handler = Handler(Looper.getMainLooper())

    private lateinit var zonesCarsRunnable: Runnable

    private val _refreshProgressFlow = MutableStateFlow(0)
    val refreshProgressFlow = _refreshProgressFlow.asStateFlow()

    private val callback: (List<ZoneCarModel>) -> Unit = { cars ->
        viewModelScope.launch(Dispatchers.IO) {
            Logs.d(TAG, "ZoningCarsViewModel.callback received cars: ${cars.size}")
            _carsListFlow.emit(cars)
        }
    }

    fun initViewModelData(idMacroZone: Int, idZone: Int) {
        // Evita que se acumulen varios runnables al volver a entrar en la pantalla:
        // onResume() vuelve a llamar a este metodo y onPause() no siempre limpia a tiempo,
        // porque el runnable se asigna dentro de una corrutina asincrona.
        removeHandlerCallbacks()

        viewModelScope.launch {
            Logs.d(TAG, "initViewModelData: Starting initialization with idMacroZone=$idMacroZone, idZone=$idZone")
            val refreshRate = licensingUseCase.getLicensingParameters()?.segRefrescoConsUbicacion
            Logs.d(TAG, "initViewModelData: Refresh rate for cars: $refreshRate seconds")

            refreshRate?.let { seconds ->
                val millis = (seconds * 1000).toLong()
                Logs.d(TAG, "initViewModelData: Converted refresh rate to milliseconds: $millis ms")

                var lastUpdate = System.currentTimeMillis()

                // Initial fetch
                zoningUseCase.getCarsInformation(idMacroZone, idZone, callback)
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
                                Logs.d(TAG, "initViewModelData: Fetching car information...")
                                zoningUseCase.getCarsInformation(idMacroZone, idZone, callback)
                                lastUpdate = System.currentTimeMillis()
                                _refreshProgressFlow.emit(1000)
                            }
                        }
                        handler.postDelayed(this, 1000) // Check every second (solo para la barra de progreso)
                        Logs.d(TAG, "initViewModelData: Scheduled next check in 1000 ms")
                    }
                }

                handler.post(zonesCarsRunnable)
                Logs.d(TAG, "initViewModelData: Started periodic car information update")
            } ?: Logs.e(TAG, "initViewModelData: Refresh rate is null, periodic update not started")
        }
    }

    fun removeHandlerCallbacks() {
        if (::zonesCarsRunnable.isInitialized) {
            handler.removeCallbacks(zonesCarsRunnable)
        }
    }

    fun checkPendingServiceOnHiredPermission() {
        viewModelScope.launch {
            val shiftStatus = shiftStatusUseCase.getStatus()?.currentStatus
            shiftStatus?.let {
                if (shiftStatusUseCase.isHired(shiftStatus)) {
                    val parameters = licensingUseCase.getLicensingParameters()
                    Logs.d(TAG, "isPendingOnHired: ${parameters?.isPendingOnHired}")
                    _pendingServicesHiredButtonFlow.emit(parameters?.isPendingOnHired)
                }
            }
        }
    }

    fun checkPendingServiceOnForHirePermission() {
        viewModelScope.launch {
            val shiftStatus = shiftStatusUseCase.getStatus()?.currentStatus
            shiftStatus?.let {
                if (shiftStatusUseCase.isVacant(shiftStatus)) {
                    val parameters = licensingUseCase.getLicensingParameters()
                    Logs.d(TAG, "isVacantPendingServices: ${parameters?.isVacantPendingServices}")
                    _pendingServicesButtonFlow.emit(parameters?.isVacantPendingServices)
                }
            }
        }
    }

    fun canOpenPendingTripsFragment(pendingTripsListFlow: MutableStateFlow<ArrayList<PendingTrip>?>) {
        viewModelScope.launch {
            userPreferencesUseCase.getUserPreferences()?.let {
                if (it.closePendingTripsIfEmpty) {
                    //Check if pendingTrips
                    if (BravoCentral.isPendingTripsAllowed(false)) {
                        pendingTripsUseCase.getPendingTripsZone(
                            pendingTripsListFlow,
                            _pendingServicesButtonPressedCallback
                        )
                    } else {
                        _pendingServicesButtonPressedCallback.emit(false)
                    }
                } else {
                    _pendingServicesButtonPressedCallback.emit(true)
                }
            } ?: run {
                _pendingServicesButtonPressedCallback.emit(true)
            }
        }
    }

    fun isHired(state: Int): Boolean {
        return shiftStatusUseCase.isHired(state)
    }

    fun getPOIsValue(){
        viewModelScope.launch {
            bravoConfiguration = bravoConfigurationVariableDao.getBravoConfigurationVariable()
        }
    }

    fun locateOnHired(zone: Zone) {
        viewModelScope.launch(Dispatchers.IO) {
            var idMacroZone: Int? = null
            var index = 0
            do {
                val hasTheZone = W2CLocation.getZoning().macrozones[index].zones.find { z ->
                    zone.idZone == z.idZone && zone.nombreZone == z.nombreZone
                }
                hasTheZone?.let {
                    idMacroZone = W2CLocation.getZoning().macrozones[index].idMacrozone
                }
                index++

            } while (idMacroZone == null && index < W2CLocation.getZoning().macrozones.size)

            idMacroZone?.let {
                locationUseCase.locateOnHired(it, zone.idZone)
                _locationOnHiredSentFlow.emit(zone)
            }
        }
    }

    fun delocateOnHired() {
        viewModelScope.launch(Dispatchers.IO) {
            locationUseCase.delocateOnHired()
            _locationOnHiredSentFlow.emit(null)
        }
    }

    fun getCarListByType(list: List<ZoneCarModel>): List<ZoneCarRowModel> {
        Logs.d(TAG, "getCarListByType: Grouping cars by status")
        val carsByType = list.groupBy { it.status }

        val maxCarTypesInRow = listOf(
            carsByType[LocationEnum.HIRED]?.size ?: 0,
            carsByType[LocationEnum.ZONE]?.size ?: 0,
            carsByType[LocationEnum.STOP]?.size ?: 0
        ).maxOrNull() ?: 0
        Logs.d(TAG, "getCarListByType: Calculated maxCarTypesInRow: $maxCarTypesInRow")

        val result = mutableListOf<ZoneCarRowModel>()

        for (i in 0 until maxCarTypesInRow) {
            val hired = carsByType[LocationEnum.HIRED]?.getOrNull(i)
            val zone = carsByType[LocationEnum.ZONE]?.getOrNull(i)
            val stop = carsByType[LocationEnum.STOP]?.getOrNull(i)

            Logs.d(TAG, "getCarListByType: Creating row $i -> Hired: $hired, Zone: $zone, Stop: $stop")

            val row = ZoneCarRowModel(
                index = result.size + 1,
                carHired = hired,
                carRank = stop,
                carZone = zone
            )
            result.add(row)
        }

        Logs.d(TAG, "getCarListByType: Final result size: ${result.size}")
        return result
    }

}