package ifac.td.taxi.viewmodel

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.viewModelScope
import com.interfacom.sdk.taximeter.bravocomm.BravoCentral
import com.interfacom.sdk.taximeter.bravocomm.W2CLocation
import com.interfacom.sdk.taximeter.bravocomm.rest.pending_trips.response.PendingTrip
import com.interfacom.sdk.taximeter.licensing.models.zoning.Macrozone
import ifac.td.taxi.domain.usecase.PendingTripsUseCase
import ifac.td.taxi.domain.usecase.ShiftStatusUseCase
import ifac.td.taxi.domain.usecase.UserPreferencesUseCase
import ifac.td.taxi.framework.sdk.usecase.LicensingUseCase
import ifac.td.taxi.framework.sdk.usecase.ZoningUseCase
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.repository.connections.receivers.utils.DateUtils
import ifac.td.taxi.repository.room.dao.BravoConfigurationVariableDao
import ifac.td.taxi.repository.room.entities.ZoningConfigurationEntity
import ifac.td.taxi.ui.model.MacroZoneModel
import ifac.td.taxi.ui.model.ScrollModeEnum
import ifac.td.taxi.viewmodel.model.FilterOptions
import ifac.td.taxi.viewmodel.model.OrderOptions
import ifac.td.taxi.viewmodel.model.UtilsModel.Companion.toBoolean1or0
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MacroZoningViewModel(
    private val zoningUseCase: ZoningUseCase,
    private val licensingUseCase: LicensingUseCase,
    private val shiftStatusUseCase: ShiftStatusUseCase,
    private val bravoConfigurationVariableDao: BravoConfigurationVariableDao,
    private val userPreferencesUseCase: UserPreferencesUseCase,
    private val pendingTripsUseCase: PendingTripsUseCase,
    context: Application,
) : BaseViewModel(context) {

    val TAG = "MacroZoningViewModel"

    private val _macroZonesListFlow = MutableStateFlow<List<Macrozone>?>(null)
    val macroZonesListFlow = _macroZonesListFlow.asStateFlow()

//    private val _macroZonesListSortedFlow = MutableStateFlow<List<MacroZoneModel>?>(null)
//    val macroZonesListSortedFlow = _macroZonesListSortedFlow.asStateFlow()

    private val _listOrderFlow = MutableStateFlow(OrderOptions.NONE)
    val listOrderFlow = _listOrderFlow.asStateFlow()

    private val _listFilterFlow = MutableStateFlow<FilterOptions>(FilterOptions.ZONE_BY_MACROZONE)
    val listFilterFlow = _listFilterFlow.asStateFlow()

    private val _pendingServicesButtonFlow = MutableStateFlow<Boolean?>(null)
    val pendingServicesButtonFlow = _pendingServicesButtonFlow.asStateFlow()

    private val _pendingServicesHiredButtonFlow = MutableStateFlow<Boolean?>(null)
    val pendingServicesHiredButtonFlow = _pendingServicesHiredButtonFlow.asStateFlow()

    private val _showPreReservationTripsButtonFlow = MutableStateFlow<Boolean?>(null)
    val showPreReservationTripsButtonFlow = _showPreReservationTripsButtonFlow.asStateFlow()

    private val _pendingServicesButtonPressedCallback = MutableSharedFlow<Boolean>()
    val pendingServicesButtonPressedCallback = _pendingServicesButtonPressedCallback.asSharedFlow()

    private val _scrollPositionFlow = MutableStateFlow<ScrollModeEnum>(ScrollModeEnum.FOLLOW_SELECTED)
    val scrollPositionFlow = _scrollPositionFlow.asStateFlow()

    private val _macroZoningListDataTypes = MutableStateFlow<String?>(null)
    val macroZoningListDataTypes = _macroZoningListDataTypes.asStateFlow()

    private val _macroZonesListSortedFlow = MutableStateFlow<List<MacroZoneModel>?>(null)
    val macroZonesListSortedFlow = _macroZonesListSortedFlow.asStateFlow()

    private val _hasSetDataTypes = MutableStateFlow(false)
    val hasSetDataTypes = _hasSetDataTypes.asStateFlow()

    private val _refreshProgressFlow = MutableStateFlow(0)
    val refreshProgressFlow = _refreshProgressFlow.asStateFlow()

    var selectedZone: MacroZoneModel? = null

    private var hasZonesInFavourites = false

    private val handler = Handler(Looper.getMainLooper())

    private lateinit var runnable: Runnable

    private val macroZoneCallback: (List<Macrozone>, String) -> Unit = { mz, availableColumns ->
        viewModelScope.launch(Dispatchers.IO) {
            Logs.d(TAG, "macroZoneCallback called")
            if (availableColumns.isNotBlank()) {
                _macroZoningListDataTypes.emit(availableColumns)
            }

            val macroZones = copyListValues(mz)
            _macroZonesListFlow.emit(macroZones)
        }
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            initZonesConfiguration(zoningUseCase.getZoneConfiguration())
        }

        viewModelScope.launch(Dispatchers.IO) {
            launch {
                bravoConfigurationVariableDao.getBravoConfigurationVariable()?.let { config ->
                    val showButton = config.showPreassignedTrips.toBoolean1or0()
                    _showPreReservationTripsButtonFlow.emit(
                        showButton && DateUtils.isLaterThan(
                            config.preassignTripsButtonStartTime
                        )
                    )
                }
            }
        }
    }

    private suspend fun initZonesConfiguration(zoneConfiguration: ZoningConfigurationEntity) {
        _listOrderFlow.emit(zoneConfiguration.zoneOrder)
    }

    fun sortNewData(listMacroZones: List<Macrozone>) {
        listOrderFlow.value.let {
            sortListBy(listMacroZones, it)
        }
    }

    fun initViewModeldata() {
        // Evita que se acumulen varios runnables al volver a entrar en la pantalla:
        // onResume() vuelve a llamar a este metodo y onPause() no siempre limpia a tiempo,
        // porque el runnable se asigna dentro de una corrutina asincrona.
        removeHandlerCallback()

        viewModelScope.launch(Dispatchers.IO) {
            val refreshRate = licensingUseCase.getLicensingParameters()?.segRefrescoConsUbicacion

            refreshRate?.let { seconds ->
                val millis = (seconds * 1000).toLong()

                var lastZoningUpdate = zoningUseCase.getLastZoningUpdate()

                val shouldLoadData = (System.currentTimeMillis() - lastZoningUpdate) < millis
                Logs.d(TAG, "se cargan los datos antiguos: $shouldLoadData")
                if (shouldLoadData) {
                    val initialProgress = 1000 - (((System.currentTimeMillis() - lastZoningUpdate).toFloat() / millis) * 1000).toInt().coerceIn(0, 1000)
                    _refreshProgressFlow.emit(initialProgress)
                    macroZoneCallback.invoke(W2CLocation.getZoning().macrozones, W2CLocation.getZoning().availableColumns)
                }

                // Se vuelve a limpiar justo antes de programar el nuevo runnable para cubrir
                // posibles condiciones de carrera entre varias llamadas asincronas a initViewModeldata.
                removeHandlerCallback()

                runnable = object : Runnable {
                    override fun run() {
                        viewModelScope.launch {
                            val elapsed = System.currentTimeMillis() - lastZoningUpdate
                            val progress = 1000 - ((elapsed.toFloat() / millis) * 1000).toInt().coerceIn(0, 1000)
                            _refreshProgressFlow.emit(progress)

                            val newCall = elapsed > millis

                            if (newCall) {
                                zoningUseCase.getMacroZonesInformation(
                                    W2CLocation.getLastIdMacrozone(),
                                    false,
                                    macroZoneCallback
                                )

                                lastZoningUpdate = System.currentTimeMillis()
                                _refreshProgressFlow.emit(1000)
                            }
                        }
                        handler.postDelayed(this, 1000)
                    }
                }
                handler.post(runnable)
            }
        }
    }
    fun sortListBy(orderOption: OrderOptions) {
        macroZonesListFlow.value?.let {
            if (orderOption != OrderOptions.NONE) {
                setScrollMode(ScrollModeEnum.JUMP_TO_TOP)
            }
            sortListBy(it, orderOption)
        }
    }

    private fun sortListBy(
        listMacroZones: List<Macrozone>,
        orderOption: OrderOptions
    ) {
        viewModelScope.launch {
            updateListOrder(orderOption)
        }
        viewModelScope.launch {
            var macroZones = mutableListOf<Macrozone>()

            macroZones.addAll(listMacroZones)


            macroZones = when (orderOption) {
                OrderOptions.NONE,
                OrderOptions.ID -> {
                    macroZones.sortedBy { it.idMacrozone }.toMutableList()
                }

                OrderOptions.NAME -> {
                    macroZones.sortedBy { it.nombreMacrozone }.toMutableList()
                }

                OrderOptions.IN_STAND -> {
                    macroZones.sortedByDescending { it.carsStop }.toMutableList()
                }

                OrderOptions.IN_ZONE -> {
                    macroZones.sortedByDescending { it.carsZone }.toMutableList()
                }

                OrderOptions.HIRED -> {
                    macroZones.sortedByDescending { it.occupiedCars }.toMutableList()
                }

                OrderOptions.TRIPS -> {
                    macroZones.sortedByDescending { it.services }.toMutableList()
                }
                else -> {
                    Logs.d(TAG, "sortListBy: FAVOURITES, NEARNESS OR ID_NO_HIERARCHY in MacroZones")
                    macroZones
                }
            }
            Logs.d(TAG, "sortListBy: $orderOption")
            val macroZoneModels = macroZones.map { MacroZoneModel(macroZone = it, isSelected = it.idMacrozone ==  W2CLocation.getLastIdMacrozone()) }

            selectedZone = macroZoneModels.find { it.isSelected }
            _macroZonesListSortedFlow.emit(macroZoneModels)
        }

    }

    private suspend fun updateListOrder(orderOption: OrderOptions) {
        zoningUseCase.updateZoningOrderConfiguration(orderOption)
        _listOrderFlow.emit(orderOption)
    }

    fun removeHandlerCallback() {
        if (::runnable.isInitialized) {
            handler.removeCallbacks(runnable)
        }
    }

    fun updateZoningSortConfigurationToFavouriteOrNearness(orderOption: OrderOptions) {
        viewModelScope.launch {
            zoningUseCase.updateZoningOrderConfiguration(orderOption)
        }
    }

    fun isHired(value: Int): Boolean {
        return shiftStatusUseCase.isHired(value)
    }

    private fun copyListValues(list: List<Macrozone>): List<Macrozone> {
        val listToReturn = mutableListOf<Macrozone>()
        list.forEach {
            listToReturn.add(it.copyValues())
        }
        return listToReturn
    }

    fun checkPendingServiceOnForHirePermission() {
        viewModelScope.launch {
            val parameters = licensingUseCase.getLicensingParameters()
            Logs.d(TAG, "isVacantPendingServices: ${parameters?.isVacantPendingServices}")
            _pendingServicesButtonFlow.emit(parameters?.isVacantPendingServices)
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

    fun canOpenPendingTripsFragment(pendingTripsListFlow: MutableStateFlow<ArrayList<PendingTrip>?>) {
        viewModelScope.launch {
            userPreferencesUseCase.getUserPreferences()?.let {
                if (it.closePendingTripsIfEmpty) {
                    //Check if pendingTrips
                    if (BravoCentral.isPendingTripsAllowed(false)) {
                        pendingTripsUseCase.getPendingTripsZone(pendingTripsListFlow, _pendingServicesButtonPressedCallback)
                    } else {
                        _pendingServicesButtonPressedCallback.emit(false)
                    }
                } else {
                    _pendingServicesButtonPressedCallback.emit(true)
                }
            }   ?: run {
                _pendingServicesButtonPressedCallback.emit(true)
            }
        }

    }

    fun hasZonesInFavourites(): Boolean {
        return hasZonesInFavourites
    }

    fun checkHasZonesInFavourites() {
        viewModelScope.launch {
            hasZonesInFavourites = zoningUseCase.getFavouriteZoneList()?.isNotEmpty() ?: false
        }
    }

    fun filterListBy(selectedOption: FilterOptions) {
        viewModelScope.launch {
            zoningUseCase.updateZoningFilterConfiguration(selectedOption)
            _listFilterFlow.emit(selectedOption)
        }
    }

    fun setScrollMode(scrollMode: ScrollModeEnum) {
        viewModelScope.launch {
            Logs.d(TAG, "setScrollMode: $scrollMode")
            _scrollPositionFlow.emit(scrollMode)
        }
    }

    fun setHasSetDataTypes() {
        _hasSetDataTypes.value = true
    }


    /*
    fun updateZoneListData(idMacroZone: Int){
        viewModelScope.launch {
            zoningUseCase.getZoneInformation(idMacroZone,false, _zonesListFlow)
        }
    }

     */
}