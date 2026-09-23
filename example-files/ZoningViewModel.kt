package ifac.td.taxi.viewmodel

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.lifecycle.viewModelScope
import com.interfacom.sdk.taximeter.bravocomm.BravoCentral
import com.interfacom.sdk.taximeter.bravocomm.W2CLocation
import com.interfacom.sdk.taximeter.bravocomm.rest.pending_trips.response.PendingTrip
import com.interfacom.sdk.taximeter.licensing.models.zoning.LatLong
import com.interfacom.sdk.taximeter.licensing.models.zoning.Macrozone
import com.interfacom.sdk.taximeter.licensing.models.zoning.Zone
import ifac.td.taxi.R
import ifac.td.taxi.domain.usecase.BluetoothLocalUseCase
import ifac.td.taxi.domain.usecase.GoingHomeUseCase
import ifac.td.taxi.domain.usecase.LocationUseCase
import ifac.td.taxi.domain.usecase.PendingTripsUseCase
import ifac.td.taxi.domain.usecase.ShiftStatusUseCase
import ifac.td.taxi.domain.usecase.SoonInZoneUseCase
import ifac.td.taxi.domain.usecase.UserPreferencesUseCase
import ifac.td.taxi.framework.sdk.usecase.LicensingUseCase
import ifac.td.taxi.framework.sdk.usecase.ZoningUseCase
import ifac.td.taxi.framework.util.Logs
import ifac.td.taxi.repository.room.dao.BravoConfigurationVariableDao
import ifac.td.taxi.repository.room.entities.BravoConfigurationVariableEntity
import ifac.td.taxi.repository.room.entities.ZoningConfigurationEntity
import ifac.td.taxi.ui.custom.button.CustomButton
import ifac.td.taxi.ui.custom.dialog.filterDialog.CustomFilterDialog
import ifac.td.taxi.ui.model.ScrollModeEnum
import ifac.td.taxi.ui.model.ZoneModel
import ifac.td.taxi.ui.util.ConfigurationUtils
import ifac.td.taxi.ui.util.ZoneUtils
import ifac.td.taxi.viewmodel.model.FilterOptions
import ifac.td.taxi.viewmodel.model.OrderOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ZoningViewModel(
    private val zoningUseCase: ZoningUseCase,
    private val licensingUseCase: LicensingUseCase,
    private val shiftStatusUseCase: ShiftStatusUseCase,
    private val locationUseCase: LocationUseCase,
    private val bravoConfigurationVariableDao: BravoConfigurationVariableDao,
    private val userPreferencesUseCase: UserPreferencesUseCase,
    private val pendingTripsUseCase: PendingTripsUseCase,
    private val soonInZoneUseCase: SoonInZoneUseCase,
    private val goingHomeUseCase: GoingHomeUseCase,
    private val bluetoothLocalUseCase: BluetoothLocalUseCase,
    context: Application,
) : BaseViewModel(context) {

    val TAG = "ZoningViewModel"

    private val ZONES_MAX_SIZE = 50

    private val _zonesListFlow = MutableStateFlow<List<ZoneModel>?>(null)
    val zonesListFlow = _zonesListFlow.asStateFlow()

    private val _zonesListFavouritesFlow = MutableStateFlow<List<ZoneModel>?>(null)
    val zonesListFavouritesFlow = _zonesListFavouritesFlow.asStateFlow()

    private val _zonesListSortedFlow = MutableStateFlow<List<ZoneModel>?>(null)
    val zonesListSortedFlow = _zonesListSortedFlow.asStateFlow()

    private val _listOrderFlow =
        MutableStateFlow<OrderOptions>(OrderOptions.NONE)
    val listOrderFlow = _listOrderFlow.asStateFlow()

    private val _listFilterFlow = MutableStateFlow<FilterOptions>(FilterOptions.ZONE_BY_MACROZONE)
    val listFilterFlow = _listFilterFlow.asStateFlow()

    private val _zoneIsInFavouriteListFlow = MutableSharedFlow<ZoneFavouriteData>()
    val zoneIsInFavouriteListFlow = _zoneIsInFavouriteListFlow.asSharedFlow()

    private val _macroZoneFlow = MutableStateFlow<Macrozone?>(null)
    val macroZoneFlow = _macroZoneFlow.asStateFlow()

    private val _locationOnHiredSentFlow = MutableSharedFlow<Zone?>()
    val locationOnHiredSentFlow = _locationOnHiredSentFlow.asSharedFlow()

    private val _serviceButtonFlow = MutableStateFlow<Boolean?>(null)
    val serviceButtonFlow = _serviceButtonFlow.asStateFlow()

    private val _taxisButtonFlow = MutableStateFlow<Boolean?>(null)
    val taxisButtonFlow = _taxisButtonFlow.asStateFlow()

    private val _pendingServicesButtonFlow = MutableStateFlow<Boolean?>(null)
    val pendingServicesButtonFlow = _pendingServicesButtonFlow.asStateFlow()

    private val _pendingServicesHiredButtonFlow = MutableStateFlow<Boolean?>(null)
    val pendingServicesHiredButtonFlow = _pendingServicesHiredButtonFlow.asStateFlow()

    private val _pendingServicesButtonPressedCallback = MutableSharedFlow<Boolean>()
    val pendingServicesButtonPressedCallback = _pendingServicesButtonPressedCallback.asSharedFlow()

    private val _zoningListDataTypes = MutableStateFlow<String?>(null)
    val zoningListDataTypes = _zoningListDataTypes.asStateFlow()

    private val _hasSoonInZoneFlow = MutableStateFlow<Boolean?>(null)
    val hasSoonInZoneFlow = _hasSoonInZoneFlow.asStateFlow()

    private val _soonInZoneConfirmationFlow = MutableSharedFlow<Boolean>()
    val soonInZoneConfirmationFlow = _soonInZoneConfirmationFlow.asSharedFlow()

    private val _canStartSoonToClearFlow = MutableSharedFlow<Boolean>()
    val canStartSoonToClearFlow = _canStartSoonToClearFlow.asSharedFlow()

    private val _locationOnHiredParameter = MutableStateFlow<Int?>(null)
    val locationOnHiredParameter = _locationOnHiredParameter.asStateFlow()

    private val _scrollPositionFlow = MutableStateFlow<ScrollModeEnum>(ScrollModeEnum.FOLLOW_SELECTED)
    val scrollPositionFlow = _scrollPositionFlow.asStateFlow()

    private val _soonToClearBtnEnabledFlow = MutableStateFlow<CustomButton.StyleButton?>(null)
    val soonToClearBtnEnabledFlow = _soonToClearBtnEnabledFlow.asStateFlow()

    private val _swapOnHiredZoneFlow = MutableSharedFlow<Boolean>()
    val swapOnHiredZoneFlow = _swapOnHiredZoneFlow.asSharedFlow()

    private val _goingHomeCallback = MutableSharedFlow<Boolean>()
    val goingHomeCallback = _goingHomeCallback.asSharedFlow()

    private val _driverGoingHomeCounter = MutableStateFlow(0)
    val driverGoingHomeCounter = _driverGoingHomeCounter.asStateFlow()

    private val _isNumericZoneList = MutableStateFlow(false)
    val isNumericZoneList = _isNumericZoneList.asStateFlow()

    private val _isOrderFavouritesByProximity = MutableStateFlow(false)
    val isOrderFavouritesByProximity = _isOrderFavouritesByProximity.asStateFlow()

    private val _selectedZoneFlow = MutableStateFlow<ZoneModel?>(null)
    val selectedZoneFlow = _selectedZoneFlow.asStateFlow()

    private val _isLocatedInZoneFlow = MutableStateFlow<Boolean>(false)
    val isLocatedInZoneFlow = _isLocatedInZoneFlow.asStateFlow()

    private val _isInSoonInZone = MutableStateFlow<Boolean>(false)
    val isInSoonInZone = _isInSoonInZone.asStateFlow()

    private val _driverGoingHomeValues = MutableStateFlow<String?>(null)
    val driverGoingHomeValue = _driverGoingHomeValues.asStateFlow()

    private val _hasCancelledSIZRecently = MutableStateFlow(false)
    val hasCancelledSIZRecently = _hasCancelledSIZRecently.asStateFlow()

    private val _refreshProgressFlow = MutableStateFlow(0)
    val refreshProgressFlow = _refreshProgressFlow.asStateFlow()

    var selectedZone: ZoneModel? = null

    var bravoConfiguration: BravoConfigurationVariableEntity? = null

    val handler = Handler(Looper.getMainLooper())

    var manualFilterDialogResponse = CustomFilterDialog.CustomFilterDialogResponse()

    private var zonesRunnable: Runnable? = null

    var isCustomersAtStand: Boolean = false

    private val zoneCallback: (List<Zone>, String) -> Unit = { zones, availableColumns ->
        viewModelScope.launch(Dispatchers.IO) {
            Logs.d(TAG, "zoneCallback called")
            val zonesList = getZoneModelFromZoning(zones)
            if (_zonesListFlow.value == zonesList) {
                Logs.d(TAG, "zoneCallback: _zonesListFlow wont emit. Filtering possible data")
                filterData()
            }
            _zonesListFlow.emit(zonesList)
            if (availableColumns.isNotBlank()) {
                _zoningListDataTypes.emit(availableColumns)
            }
        }
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            initZonesConfiguration(zoningUseCase.getZoneConfiguration())
            licensingUseCase.getLicensingParameters()?.isCustomersAtStand?.let {
                isCustomersAtStand = it
            }
        }
        viewModelScope.launch {
            _driverGoingHomeCounter.emit(goingHomeUseCase.getRemainingDriverGoingHomeTimes())
        }
        viewModelScope.launch() {
            zonesListFlow.collect {
                it?.let { zones ->
                    Logs.d(TAG, "zoneListFlow: updated")
                    filterNewData(
                        zones,
                    )
                }
            }
        }
        viewModelScope.launch() {
            zonesListFavouritesFlow.collect {
                filterData()
            }
        }

        viewModelScope.launch {
            listOrderFlow.collect {
                filterData()
            }
        }

        viewModelScope.launch {
            listFilterFlow.collect {
                filterData()
            }
        }
        viewModelScope.launch {
            _hasSoonInZoneFlow.emit(bravoConfigurationVariableDao.getBravoConfigurationVariable()?.hasSoonInZone)
        }
        viewModelScope.launch {
            _locationOnHiredParameter.emit(licensingUseCase.getLicensingParameters()?.locationInHired)
        }
        viewModelScope.launch {
            _isNumericZoneList.emit(userPreferencesUseCase.getUserPreferences()?.numericInputFilter ?: false)
        }
        viewModelScope.launch {
            _isOrderFavouritesByProximity.emit(userPreferencesUseCase.getUserPreferences()?.orderFavoritesByProximity ?: false)
        }
        viewModelScope.launch {
            _driverGoingHomeValues.emit(bravoConfigurationVariableDao.getBravoConfigurationVariable()?.driverGoingHomeValues)
        }

    }

    fun initViewModelData(idMacroZone: Int, onHiredZone: Zone?) {
        // Evita que se acumulen varios runnables al volver a entrar en la pantalla:
        // onResume() vuelve a llamar a este metodo y onPause() no siempre limpia a tiempo,
        // porque el runnable se asigna dentro de una corrutina asincrona.
        removeHandlerCallbacks()

        viewModelScope.launch {
            var idMacroZoneSaved = idMacroZone
            _zonesListFavouritesFlow.emit(zoningUseCase.getFavouriteZoneList())

            val refreshRate = licensingUseCase.getLicensingParameters()?.segRefrescoConsUbicacion
            if (_macroZoneFlow.value == null) {
                _macroZoneFlow.emit(W2CLocation.getZoning().macrozones.find { it.idMacrozone == idMacroZone })
            } else {
                _macroZoneFlow.value?.let {
                    idMacroZoneSaved = it.idMacrozone
                }
            }
            Logs.d(TAG, "idMacroZoneSaved: $idMacroZoneSaved")
            refreshRate?.let { seconds ->
                val millis = (seconds * 1000).toLong()
                var lastZoningUpdate = zoningUseCase.getLastZoningUpdate()

                val shouldLoadData = (System.currentTimeMillis() - lastZoningUpdate) < millis
                Logs.d(TAG, "System.currentTimeMillis() - lastZoningUpdate: ${(System.currentTimeMillis() - lastZoningUpdate)}")
                Logs.d(TAG, "se cargan los datos antiguos: $shouldLoadData")
                if (shouldLoadData) {
                    val initialProgress = 1000 - (((System.currentTimeMillis() - lastZoningUpdate).toFloat() / millis) * 1000).toInt().coerceIn(0, 1000)
                    _refreshProgressFlow.emit(initialProgress)
                    val zoning = W2CLocation.getZoning()
                    zoning?.getMacrozoneById(idMacroZoneSaved)?.zones?.let { data ->
                        Logs.d(TAG, "calling zoneCallback from shouldLoadData if")
                        zoneCallback.invoke(data, zoning.availableColumns)
                    }
                }

                // Se vuelve a limpiar justo antes de programar el nuevo runnable para cubrir
                // posibles condiciones de carrera entre varias llamadas asincronas a initViewModelData.
                removeHandlerCallbacks()

                zonesRunnable = object : Runnable {
                    override fun run() {
                        viewModelScope.launch {
                            val elapsed = System.currentTimeMillis() - lastZoningUpdate
                            val progress = 1000 - ((elapsed.toFloat() / millis) * 1000).toInt().coerceIn(0, 1000)
                            _refreshProgressFlow.emit(progress)

                            val newCall = elapsed > millis

                            if (newCall) {
                                Logs.d(TAG, "actualizar zonas")
                                zoningUseCase.getZonesInformation(
                                    macroZoneFlow.value,
                                    zoneCallback,
                                )

                                lastZoningUpdate = System.currentTimeMillis()
                                _refreshProgressFlow.emit(1000)
                                Logs.d(TAG, "guardar lastZoningUpdate $lastZoningUpdate")
                            }
                        }
                        handler.postDelayed(this, 1000)
                    }
                }
                Logs.d(TAG, "se setea handler?")
                zonesRunnable?.let {
                    Logs.d(TAG, "zonesRunnable not null")
                    handler.post(it)
                }
            }
        }
        viewModelScope.launch {
            if (W2CLocation.getLastIdZone() != 0 && selectedZone == null) {
                Logs.d(TAG, "selectedZone: ${W2CLocation.getLastIdMacrozone()}")
                selectedZone = if (onHiredZone != null) {
                    ZoneModel(onHiredZone, true)
                } else {
                    ZoneModel(
                        W2CLocation.getZoning().getMacrozoneById(W2CLocation.getLastIdMacrozone())
                            .getZoneById(W2CLocation.getLastIdZone()), true
                    )
                }
                _selectedZoneFlow.emit(selectedZone)
                updateBtnStcOnZoneSelected(onHiredZone)
                Logs.d(TAG, "selectedZone -> ZoneModel = ${selectedZone?.zone?.nombreZone}")
            }
        }
    }

    private suspend fun initZonesConfiguration(zoneConfiguration: ZoningConfigurationEntity) {
        if (!bluetoothLocalUseCase.isCurrentBluetoothItop()) {
            Logs.d(TAG, "initZonesConfiguration: !bluetoothLocalUseCase.isCurrentBluetoothItop() -> Emitting zone order")
            _listOrderFlow.emit(zoneConfiguration.zoneOrder)
        }
        Logs.d(TAG, "initZonesConfiguration: Emitting zone filter")
        _listFilterFlow.emit(zoneConfiguration.zoneFilter)
    }

    fun filterData() {
        zonesListFlow.value?.let {
            filterNewData(it)
        }
    }

    private fun filterNewData(listZones: List<ZoneModel>) {
        viewModelScope.launch {
            listFilterFlow.value.let { filters ->
                listOrderFlow.value.let { sortedBy ->
                    processZoningData(
                        listZones,
                        filters,
                        sortedBy
                    )
                }
            }
        }
    }

    private suspend fun processZoningData(
        listZones: List<ZoneModel>,
        selectedFilters: FilterOptions,
        sortBy: OrderOptions
    ) {
        Logs.d(TAG, "processZoningData filter: $selectedFilters")
        var zones = applyFilters(listZones, selectedFilters)
        Logs.d(TAG, "processZoningData sort: $sortBy")
        zones = applySorting(zones, sortBy, selectedFilters)

        //Stockholm - Hot Zones - Show located/SIZ zone
        zones = applyHotZoneFilterHeaderData(zones, selectedFilters, isInSoonInZone.value, isLocatedInZoneFlow.value)

        //Check if selectedZone is correct
        val current = zones.find { it.isSelected }
        if (current?.zone?.idZone != selectedZone?.zone?.idZone && current?.zone?.nombreZone != selectedZone?.zone?.nombreZone) {
            current?.isSelected = false
            zones.find { it.zone == selectedZone?.zone }?.isSelected = true
        }

        _zonesListSortedFlow.emit(zones)
    }

    private fun applyHotZoneFilterHeaderData(
        zones: List<ZoneModel>,
        selectedFilters: FilterOptions,
        isInSoonInZone: Boolean,
        isLocated: Boolean
    ): List<ZoneModel> {
        val list = zones.toMutableList()

        val orientation = context.resources.configuration.orientation
        val isTablet = ConfigurationUtils.isTablet(context.resources)
        val isTabletLandscape = isTablet && (orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE)

        if (selectedFilters == FilterOptions.HOT_ZONES) {
            if (isInSoonInZone) {
                //find siz zone in list
                if (isTabletLandscape) {
                    list.add(0, ZoneModel(Zone(), false, isHeader = true, isEmpty = true))
                }

                val sizZone = W2CLocation.getZoning()?.getMacrozoneById(W2CLocation.getSoonInZoneMacroZoneId())
                    ?.getZoneById(W2CLocation.getSoonInZoneZoneId()) ?: return list
                list.removeIf { it.zone.idZone == sizZone.idZone && it.zone.nombreZone == sizZone.nombreZone }

                if (isTabletLandscape) {
                    list.add(1, ZoneModel(zone = sizZone.copyValues(), false, isHeader = true))
                } else {
                    list.add(0, ZoneModel(zone = sizZone.copyValues(), false, isHeader = true))
                }
            } else if (isLocated) {
                //find located zone in list
                val locatedZone =
                    W2CLocation.getZoning()?.getMacrozoneById(W2CLocation.getLastIdMacrozone())
                        ?.getZoneById(W2CLocation.getLastIdZone()) ?: return list
                list.removeIf { it.zone.idZone == locatedZone.idZone && it.zone.nombreZone == locatedZone.nombreZone }
                list.add(0,
                    ZoneModel(zone = locatedZone.copyValues(),
                    isSelected = false,
                    isHeader = true)
                )
                if (isTablet && orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
                    list.add(1, ZoneModel(Zone(), false, isHeader = true, isEmpty = true))
                }
            }
        }

        return list
    }


    private fun applySorting(
        zones: List<ZoneModel>,
        sortBy: OrderOptions,
        filter: FilterOptions
    ): List<ZoneModel> {
        val zonesToReturn = when (sortBy) {
            OrderOptions.NONE -> {
                if (isNumericZoneList.value && (filter != FilterOptions.HOT_ZONES && filter != FilterOptions.NEARNESS)) {
                    zones.sortedBy { it.zone.nombreZone }
                } else {
                    zones
                }
            }
            OrderOptions.ID -> {
                //Not used
//                zones.sortedBy { it.zone.idZone }
                zones
            }

            OrderOptions.NAME -> {
                zones.sortedBy { it.zone.nombreZone }
            }

            OrderOptions.IN_ZONE -> {
                zones.sortedByDescending { it.zone.cochesZone }
            }

            OrderOptions.HIRED -> {
                zones.sortedByDescending { it.zone.cochesOcupado }
            }

            OrderOptions.TRIPS -> {
                zones.sortedByDescending { it.zone.servicios }
            }

            OrderOptions.IN_STAND -> {
                zones.sortedByDescending { it.zone.cochesParada }
            }

            OrderOptions.NEARNESS -> {
                //Not used
//                zones.sortedBy { zoneSort ->
//                    getDistanceFromLatLonInMetersLocale(
//                        W2CLocation.getLastLatLong(),
//                        zoneSort.zone.centralCoord
//                    )
//                }
                zones
            }

            else -> {
                Logs.d(TAG, "applySorting: not sorted")
                zones
            }
        }
        return zonesToReturn
    }

    private fun applyFilters(
        listZones: List<ZoneModel>,
        selectedFilters: FilterOptions
    ): List<ZoneModel> {
        var zones = mutableListOf<ZoneModel>()
        zones.addAll(listZones)
        when (selectedFilters) {
            FilterOptions.ZONE_BY_MACROZONE -> {
                zones = listZones.toMutableList()
                /*
                val mz =
                    W2CLocation.getZoning().macrozones.find { it.idMacrozone == macroZoneFlow.value?.idMacrozone }
                zones.filter {
                    mz?.zones?.find { mzZones -> mzZones.idZone == it.zone.idZone } != null
                }

                 */
            }

            FilterOptions.ID_NO_HIERARCHY -> {
                zones.clear()
                W2CLocation.getZoning().macrozones.forEach { mz ->
                    zones.addAll(mz.zones.map { zone: Zone ->
                        ZoneModel(
                            zone,
                            zone == selectedZone?.zone
                        )
                    })
                }
            }

            FilterOptions.NEARNESS -> {
                zones.clear()
                W2CLocation.getZoning().macrozones.forEach { mz ->
                    zones.addAll(mz.zones.map { zone: Zone ->
                        ZoneModel(
                            zone,
                            zone == selectedZone?.zone
                        )
                    })
                }

                val zoneLocated = W2CLocation.getZoning().getZoneById(
                    W2CLocation.getLastIdMacrozone(),
                    W2CLocation.getLastIdZone()
                )

                val fromList = if (zoneLocated != null) {
                    zones.find { it == ZoneModel(zoneLocated, false) || it == ZoneModel(zoneLocated, true) }
                } else null

                zones = zones.sortedBy { zoneSort ->
                    getDistanceFromLatLonInMetersLocale(
                        W2CLocation.getLastLatLong(),
                        zoneSort.zone.centralCoord
                    )
                }.toMutableList()

                if (fromList != null) {
                    zones.remove(fromList)
                    zones.add(0, fromList)
                }

                if (zones.size > 50) {
                    zones = zones.subList(0, ZONES_MAX_SIZE)
                }
            }

            FilterOptions.FAVOURITES -> {
                zones.clear()
                W2CLocation.getZoning().macrozones.forEach { mz ->
                    zones.addAll(mz.zones.map { zone: Zone ->
                        ZoneModel(
                            zone,
                            zone == selectedZone?.zone
                        )
                    })
                }
                val favouriteZones =
                    zonesListFavouritesFlow.value?.map { fav -> fav.zone }
                val favouriteOrder = favouriteZones?.withIndex()?.associate { (index, zone) -> zone to index }
                zones = zones.filter { zoneModel: ZoneModel ->
                    favouriteZones?.contains(zoneModel.zone) ?: false
                }.sortedBy { favouriteOrder?.get(it.zone) ?: Int.MAX_VALUE }.toMutableList()

                if (isOrderFavouritesByProximity.value) {
                    zones = zones.sortedBy { zoneSort ->
                        getDistanceFromLatLonInMetersLocale(
                            W2CLocation.getLastLatLong(),
                            zoneSort.zone.centralCoord
                        )
                    }.toMutableList()
                }
            }

            FilterOptions.HOT_ZONES -> {
                zones.clear()
                W2CLocation.getZoning().macrozones.forEach { mz ->
                    zones.addAll(mz.zones.map { zone: Zone ->
                        ZoneModel(
                            zone,
                            zone == selectedZone?.zone
                        )
                    })
                }

                zones = zones.filter {
                    ZoneUtils.checkDiff(it.zone, zoningListDataTypes.value ?: "") > 0
                }.toMutableList()

                zones = zones.sortedByDescending { zoneSort ->
                    ZoneUtils.checkDiff(zoneSort.zone, zoningListDataTypes.value ?: "")
                }.toMutableList()
            }

            FilterOptions.UPDATE_MANUAL,
            FilterOptions.MANUAL -> {
                zones.clear()
                W2CLocation.getZoning().macrozones.forEach { mz ->
                    zones.addAll(mz.zones.map { zone: Zone ->
                        ZoneModel(
                            zone,
                            zone == selectedZone?.zone
                        )
                    })
                }

                zones = zones.filter { zone ->
                    manualFilterDialogResponse.dataSelected.any { keyword ->
                        if (manualFilterDialogResponse.numericInput) {
                            when {
                                keyword.isEmpty() -> true
                                keyword.length <= 3 -> {
                                    val digitPattern = keyword.replace(".", "[0-9]")

                                    val pattern = if (keyword.length == 3) {
                                        Regex("^$digitPattern$")
                                    } else {
                                        Regex("^$digitPattern.*$")
                                    }

                                    pattern.matches(zone.zone.nombreZone)
                                }

                                else -> false
                            }
                        } else {
                            zone.zone.nombreZone.contains(keyword, true)
                        }
                    }
                }.toMutableList()
            }
        }

        val toReturn = mutableListOf<ZoneModel>()
        zones.forEach {
            toReturn.add(ZoneModel(it.zone.copyValues(), it.isSelected))
        }

        return toReturn.toList()
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

    fun isZoneInFavourites(idMacroZone: Int, idZone: Int) {
        viewModelScope.launch {
            val map = zoningUseCase.getFavouriteZoneMap()
            map?.let {
                if (map.containsKey(idMacroZone)) {
                    map[idMacroZone]?.let { list ->
                        _zoneIsInFavouriteListFlow.emit(
                            ZoneFavouriteData(
                                idMacroZone,
                                idZone,
                                list.contains(idZone)
                            )
                        )
                        return@launch
                    }
                }
            }
            _zoneIsInFavouriteListFlow.emit(
                ZoneFavouriteData(
                    idMacroZone,
                    idZone,
                    false
                )
            )
        }
    }

    fun addZoneToFavourites(idMacroZone: Int, idZone: Int, zone: Zone) {
        viewModelScope.launch {
            zoningUseCase.addFavouriteZone(idMacroZone, idZone)
            val list = zonesListFavouritesFlow.value?.toMutableList() ?: mutableListOf()
            list.add(ZoneModel(zone, false))
            _zonesListFavouritesFlow.emit(list)
        }
    }

    fun removeZoneFromFavourites(idMacroZone: Int, idZone: Int, zone: Zone) {
        viewModelScope.launch {
            zoningUseCase.removeFavouriteZone(idMacroZone, idZone)
            val list = zonesListFavouritesFlow.value?.toMutableList() ?: mutableListOf()
            val item = list.find { it.zone.nombreZone == zone.nombreZone }
            list.remove(item)
            if (list.isEmpty()) {
                val filters = _listFilterFlow.value
                if (filters == FilterOptions.FAVOURITES) {
                    _listFilterFlow.emit(FilterOptions.ZONE_BY_MACROZONE)
                }
                //sortListBy(OrderOptions.ID, false)
            } else {
                _zonesListFavouritesFlow.emit(list)
            }
        }
    }

    fun isHired(value: Int): Boolean {
        return shiftStatusUseCase.isHired(value)
    }

    fun locateOnHired() {
        viewModelScope.launch(Dispatchers.IO) {
            selectedZone?.zone?.let { selectedZone ->
                var idMacroZone: Int? = null
                var index = 0
                do {
                    val hasTheZone =
                        W2CLocation.getZoning().macrozones[index].zones.find { zone -> selectedZone.idZone == zone.idZone && selectedZone.nombreZone == zone.nombreZone }
                    hasTheZone?.let {
                        idMacroZone = W2CLocation.getZoning().macrozones[index].idMacrozone
                    }
                    index++

                } while (idMacroZone == null && index < W2CLocation.getZoning().macrozones.size)

                idMacroZone?.let {
                    locationUseCase.locateOnHired(it, selectedZone.idZone)
                    _locationOnHiredSentFlow.emit(selectedZone)
                    if (bravoConfigurationVariableDao.getBravoConfigurationVariable()?.soonToClearMaxTimes != 0) {
                        locationUseCase.insertNewSoonToClear(it, idZone = selectedZone.idZone)
                    }
                }
            }
        }
    }

    fun removeHandlerCallbacks() {
        zonesRunnable?.let {
            handler.removeCallbacks(it)
        }
    }

    fun changeSelectedItem(zoneModel: ZoneModel, onHiredZone: Zone?) {
        if (soonToClearBtnEnabledFlow.value == CustomButton.StyleButton.LOADING) {
            return
        }
        viewModelScope.launch {
            selectedZone = if (selectedZone?.zone == zoneModel.zone) {
                //Unselect the zone
                null
            } else {
                zoneModel
            }
            _selectedZoneFlow.emit(selectedZone)
            updateBtnStcOnZoneSelected(onHiredZone)
            _zonesListSortedFlow.value?.let { list ->
                val newData = mutableListOf<ZoneModel>()
                for (zone in list) {
                    newData.add(zone.copy(isSelected = zone.zone.nombreZone == selectedZone?.zone?.nombreZone && zone.zone.idZone == selectedZone?.zone?.idZone))
                }

                _zonesListSortedFlow.emit(newData)
                Logs.d(TAG, "changeSelectedItem: select start")
            }
        }
    }

    suspend fun updateBtnStcOnZoneSelected(onHiredZone: Zone?) {
        // when selecting zone,
        // if currently not on soonToClear -> btn soonToClear = new stc
        // if currently on soonToClear on another zone -> btn soonToClear = new stc
        // if currently on soonToClear on same zone -> btn soonToClear = cancel stc
        Logs.d(TAG, "soonToClearBtn selectedZone: ${selectedZone?.zone?.nombreZone}")
        val selection = selectedZone
        if (selection == null && onHiredZone != null) {
            _soonToClearBtnEnabledFlow.emit(CustomButton.StyleButton.DISABLE)
            return
        } else if (selection == null) {
            _soonToClearBtnEnabledFlow.emit(CustomButton.StyleButton.ENABLE)
            return
        }
        val idZone = selection.zone.idZone
        val idMacroZone = W2CLocation.getMacrozoneFromZone(selection.zone).idMacrozone

        if (onHiredZone == null) {
            if (_soonToClearBtnEnabledFlow.value != CustomButton.StyleButton.LOADING) {
                _soonToClearBtnEnabledFlow.emit(CustomButton.StyleButton.ENABLE)
            }
            return
        }
        val onHiredMacroZone: Macrozone? = W2CLocation.getMacrozoneFromZone(onHiredZone)
        if (onHiredMacroZone == null) {
            _soonToClearBtnEnabledFlow.emit(CustomButton.StyleButton.ENABLE)
            return
        }

        if (idMacroZone == onHiredMacroZone.idMacrozone && idZone == onHiredZone.idZone) {
            _soonToClearBtnEnabledFlow.emit(CustomButton.StyleButton.DISABLE)
        } else {
            _soonToClearBtnEnabledFlow.emit(CustomButton.StyleButton.ENABLE)
        }
    }


    private fun getZoneModelFromZoning(zones: List<Zone>): List<ZoneModel> {
        val zonesList = mutableListOf<ZoneModel>()
        zones.forEach {
            val isZoneSelected =
                (it.idZone == selectedZone?.zone?.idZone && it.nombreZone == selectedZone?.zone?.nombreZone)
            zonesList.add(ZoneModel(it.copyValues(), isZoneSelected).copy())
        }
        return zonesList
    }

    fun swapOnHiredZone(zone: Zone, withZone: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (zone.allowedUbOcupado) { // si estaba en soon to clear con zona
                W2CLocation.setDelocateHiredCallback {
                    viewModelScope.launch {
                        _swapOnHiredZoneFlow.emit(withZone)
                    }
                }
                delocateOnHired()
            } else {
                delocateOnHiredWithoutZone()
                _swapOnHiredZoneFlow.emit(withZone)
            }
            _soonToClearBtnEnabledFlow.emit(CustomButton.StyleButton.LOADING)
        }
    }

    fun delocateOnHired() {
        viewModelScope.launch(Dispatchers.IO) {
            locationUseCase.delocateOnHired()
            _locationOnHiredSentFlow.emit(null)
        }
    }

    fun delocateOnHiredWithoutZone() {
        viewModelScope.launch {
            sendPositionStatic()
            _locationOnHiredSentFlow.emit(null)
        }
    }

    fun returnSelectedZoneData(): Pair<Int, Int>? {

        var idMacroZone: Int? = null
        var index = 0
        do {
            val hasTheZone =
                W2CLocation.getZoning().macrozones[index].zones.find { zone -> selectedZone?.zone?.idZone == zone.idZone && selectedZone?.zone?.nombreZone == zone.nombreZone }
            hasTheZone?.let {
                idMacroZone = W2CLocation.getZoning().macrozones[index].idMacrozone
            }
            index++

        } while (idMacroZone == null && index < W2CLocation.getZoning().macrozones.size)
        idMacroZone?.let { idM ->
            selectedZone?.zone?.idZone?.let { idZ ->
                return Pair(idM, idZ)
            }
        }

        return null
    }

    data class ZoneFavouriteData(
        val idMacroZone: Int,
        val idZone: Int,
        val isInFavourites: Boolean,
    )

    fun checkServiceButton() {
        viewModelScope.launch(Dispatchers.IO) {
            val parameters = licensingUseCase.getLicensingParameters()
            _serviceButtonFlow.emit(parameters?.isServicesDetail)
            Logs.d(TAG, "isServicesDetail: ${parameters?.isServicesDetail}")
        }
    }

    fun checkTaxisButton() {
        viewModelScope.launch(Dispatchers.IO) {
            val parameters = licensingUseCase.getLicensingParameters()
            _taxisButtonFlow.emit(parameters?.isTaxisDetail)
            Logs.d(TAG, "isTaxisDetail: ${parameters?.isTaxisDetail}")
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

    fun getPOIsValue() {
        viewModelScope.launch {
            bravoConfiguration = bravoConfigurationVariableDao.getBravoConfigurationVariable()
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

    fun checkZoningChanged(data: Pair<Int, Int>, fromBackPressed: Boolean, onHiredZone: Zone?) {
        viewModelScope.launch {
            if (fromBackPressed && firstCheck) {
                firstCheck = false
                updateData = false
            } else {
                updateData = true
            }

            val newZone = W2CLocation.getZoning().getZoneById(data.first, data.second)
            if (selectedZone != null && newZone != null && newZone != selectedZone?.zone && updateData) {
                setScrollMode(ScrollModeEnum.FOLLOW_SELECTED)
                selectedZone = ZoneModel(newZone, true)
                _selectedZoneFlow.emit(selectedZone)
                updateBtnStcOnZoneSelected(onHiredZone)
                _macroZoneFlow.emit(W2CLocation.getZoning().macrozones.find { it.idMacrozone == data.first })
                processZoningData(
                    W2CLocation.getZoning().getMacrozoneById(data.first).zones.map { zone: Zone ->
                        ZoneModel(
                            zone,
                            false
                        )
                    },
                    listFilterFlow.value,
                    listOrderFlow.value
                )
            }
        }
    }

    private var firstCheck = true
    private var updateData = true

    fun sortListBy(orderOption: OrderOptions) {
        viewModelScope.launch {
            if (orderOption != OrderOptions.NONE) {
                setScrollMode(ScrollModeEnum.JUMP_TO_TOP)
            }
            _listOrderFlow.emit(orderOption)
            zoningUseCase.updateZoningOrderConfiguration(orderOption)
        }
    }

    fun filterListBy(selectedOptions: FilterOptions) {
        viewModelScope.launch {
            if (selectedOptions == FilterOptions.MANUAL && listFilterFlow.value == selectedOptions) {
                _listFilterFlow.emit(FilterOptions.UPDATE_MANUAL)
            } else {
                _listFilterFlow.emit(selectedOptions)
            }

            zoningUseCase.updateZoningFilterConfiguration(selectedOptions)
        }
    }

    fun saveManualFilterData(response: CustomFilterDialog.CustomFilterDialogResponse) {
        manualFilterDialogResponse = response
    }

    fun checkSoonInZone(isInSoonInZone: Boolean) {
        viewModelScope.launch {
            if (isInSoonInZone && checkSoonInZoneInSelectedZone()
                || alreadyLocatedInSelectedZone()) {
                //askForSoonInZoneCancellation
                _soonInZoneConfirmationFlow.emit(false) // show cancellation dialog
            } else if (checkCanStartSoonInZoneInZoneSelected()) {
                _soonInZoneConfirmationFlow.emit(true) // proceed with soon in zone
            } else {
                Toast.makeText(context,
                    context.getString(R.string.siz_invalid_zone), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun alreadyLocatedInSelectedZone(): Boolean {
        selectedZone?.let {
            val macroZone = W2CLocation.getMacrozoneFromZone(it.zone) ?: return false
            return macroZone.idMacrozone == W2CLocation.getLastIdMacrozone() && it.zone.idZone == W2CLocation.getLastIdZone()
        }
        return false
    }

    private fun checkSoonInZoneInSelectedZone(): Boolean {
        selectedZone?.let {
            val macroZone = W2CLocation.getMacrozoneFromZone(it.zone) ?: return false
            return macroZone.idMacrozone == W2CLocation.getSoonInZoneMacroZoneId() && it.zone.idZone == W2CLocation.getSoonInZoneZoneId()
        }
        return false
    }

    private fun checkCanStartSoonInZoneInZoneSelected(): Boolean {
        selectedZone?.let {
            val macroZone = W2CLocation.getMacrozoneFromZone(it.zone) ?: return false
            return macroZone.idMacrozone != W2CLocation.getLastIdMacrozone() || it.zone.idZone != W2CLocation.getLastIdZone()
            // return soonInZoneUseCase.getCanZoneStartSoonInZone(macroZone.idMacrozone, it.zone.idZone)
        }
        return false
    }

    fun getCurrentSelectedIds(): Pair<Int, Int>? {
        selectedZone?.let {
            val macroZone = W2CLocation.getMacrozoneFromZone(it.zone)

            if (macroZone.idMacrozone != W2CLocation.getLastIdMacrozone() || it.zone.idZone != W2CLocation.getLastIdZone()) {
                return Pair(macroZone.idMacrozone, it.zone.idZone)
            }

        }

        return null
    }



    fun getCanZoneStartSoonToClear(zone: Zone) {
        viewModelScope.launch {
            //Save on DB zonas con el numero de veces que se han hecho los Soon to Clear
            val macroZone = W2CLocation.getMacrozoneFromZone(zone)
            val askedTimes = locationUseCase.returnAskedTimesSoonToClear(macroZone.idMacrozone, zone.idZone)
            val soonToClearMaxTimes = bravoConfigurationVariableDao.getBravoConfigurationVariable()?.soonToClearMaxTimes
            Logs.d(TAG, "getCanZoneStartSoonToClear: zone: ${zone.nombreZone}/askedTimes: $askedTimes")
            if (soonToClearMaxTimes != null && soonToClearMaxTimes != 0) {
                _canStartSoonToClearFlow.emit(askedTimes < soonToClearMaxTimes)
            } else {
                _canStartSoonToClearFlow.emit(true)
            }
        }
    }

    fun sendPositionStatic() {
        val EV_TRACKING = 'z'
        W2CLocation.sendEvent(EV_TRACKING, null)
    }

    fun setScrollMode(scrollMode: ScrollModeEnum) {
        viewModelScope.launch {
            Logs.d(TAG, "setScrollMode: $scrollMode")
            _scrollPositionFlow.emit(scrollMode)
        }
    }

    fun saveDriverGoingHomeData(idMacroZone: Int, idZone: Int, millis: Int?) {
        viewModelScope.launch(Dispatchers.IO) {
            millis?.let {
                goingHomeUseCase.addGoingHome(
                    idMacroZone,
                    idZone,
                    millis,
                )
                _goingHomeCallback.emit(true)
                _driverGoingHomeCounter.emit(_driverGoingHomeCounter.value - 1)
            }
        }
    }

    fun saveLocationType(it: String) {
        viewModelScope.launch {
            _isLocatedInZoneFlow.emit(it == "Z")
        }
    }

    fun saveIsInSoonInZone(bol: Boolean) {
        viewModelScope.launch {
            _isInSoonInZone.emit(bol)
        }
    }

    fun setCancelledSIZ() {
        viewModelScope.launch {
            _hasCancelledSIZRecently.emit(true)
            delay(3000)
            _hasCancelledSIZRecently.emit(false)
        }
    }

    fun isCurrentBluetoothItop(): Boolean {
        return bluetoothLocalUseCase.isCurrentBluetoothItop()
    }
}