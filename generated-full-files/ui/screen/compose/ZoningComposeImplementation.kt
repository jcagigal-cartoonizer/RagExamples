package ifac.td.taxi.ui.screen.compose
import ifac.td.taxi.viewmodel.model.OrderOptions
import androidx.lifecycle.ViewModel
import ifac.td.taxi.framework.util.Logs
import kotlinx.coroutines.launch
import androidx.compose.foundation.clickable
import ifac.td.taxi.repository.connections.service.model.ShortBreakStatus
import androidx.compose.ui.Modifier
import ifac.td.taxi.domain.usecase.PortugalUseCase
import androidx.compose.material3.Divider
import ifac.td.taxi.domain.usecase.UserPreferencesUseCase
import androidx.compose.foundation.lazy.LazyColumn
import ifac.td.taxi.domain.usecase.ShiftStatusUseCase
import androidx.compose.material3.Surface
import androidx.compose.ui.unit.dp
import ifac.td.taxi.domain.usecase.LocationUseCase
import ifac.td.taxi.ui.model.ScrollModeEnum
import com.interfacom.sdk.taximeter.bravocomm.W2CLocation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import ifac.td.taxi.domain.usecase.SoonInZoneUseCase
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Text
import kotlinx.coroutines.flow.*
import androidx.compose.material3.OutlinedButton
import ifac.td.taxi.domain.usecase.PrinterUseCase
import com.interfacom.sdk.taximeter.licensing.models.zoning.Macrozone
import ifac.td.taxi.domain.usecase.BluetoothLocalUseCase
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.text.style.TextAlign
import ifac.td.taxi.domain.usecase.TripUseCase
import androidx.lifecycle.viewModelScope
import androidx.compose.foundation.lazy.items
import com.interfacom.sdk.taximeter.bravocomm.rest.infozones.AvailableColumnsEnum
import kotlinx.coroutines.withContext
import ifac.td.taxi.framework.sdk.usecase.LicensingUseCase
import com.interfacom.sdk.taximeter.licensing.models.zoning.Zone
import kotlinx.coroutines.Dispatchers
import ifac.td.taxi.ui.model.ZoneModel
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.material3.CircularProgressIndicator
import ifac.td.taxi.domain.usecase.PendingTripsUseCase
import androidx.compose.foundation.background
import androidx.lifecycle.AndroidViewModel
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.delay
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.foundation.layout.*
import ifac.td.taxi.viewmodel.model.FilterOptions
import ifac.td.taxi.ui.model.ZoneCarRowModel
import androidx.compose.ui.Alignment
import ifac.td.taxi.framework.sdk.usecase.ZoningUseCase
import ifac.td.taxi.domain.usecase.GoingHomeUseCase
import android.app.Application
import com.interfacom.sdk.taximeter.bravocomm.ifConstants
import ifac.td.taxi.ui.custom.button.CustomButton
import androidx.compose.material3.ButtonDefaults
import ifac.td.taxi.R


/* ----------------------------- MODELS / STATE ----------------------------- */

data class ZoningButtonVisualState(
    val visible: Boolean = false,
    val enabled: Boolean = true,
    val style: CustomButton.StyleButton = CustomButton.StyleButton.ENABLE,
    val background: CustomButton.BackgroundButtonColor = CustomButton.BackgroundButtonColor.BLUE,
    val text: String = "",
    val iconRes: Int? = null,
)

data class ZoningButtonsState(
    val locateOnHired: ZoningButtonVisualState = ZoningButtonVisualState(),
    val pending: ZoningButtonVisualState = ZoningButtonVisualState(
        visible = true,
        enabled = true,
        style = CustomButton.StyleButton.ENABLE,
        background = CustomButton.BackgroundButtonColor.BLUE,
        text = "PENDIENTES"
    ),
    val soonInZone: ZoningButtonVisualState = ZoningButtonVisualState(),
    val trips: ZoningButtonVisualState = ZoningButtonVisualState(
        visible = true,
        enabled = true,
        style = CustomButton.StyleButton.ENABLE,
        background = CustomButton.BackgroundButtonColor.BLUE,
        text = "TRIPS"
    ),
    val cars: ZoningButtonVisualState = ZoningButtonVisualState(
        visible = true,
        enabled = true,
        style = CustomButton.StyleButton.ENABLE,
        background = CustomButton.BackgroundButtonColor.BLUE,
        text = "CARS"
    ),
    val back: ZoningButtonVisualState = ZoningButtonVisualState(
        visible = true,
        enabled = true,
        style = CustomButton.StyleButton.ENABLE,
        background = CustomButton.BackgroundButtonColor.BLUE,
        text = "VOLVER"
    ),
    val close: ZoningButtonVisualState = ZoningButtonVisualState(
        visible = true,
        enabled = true,
        style = CustomButton.StyleButton.ENABLE,
        background = CustomButton.BackgroundButtonColor.RED,
        text = "CERRAR"
    ),
)

data class ZoningHeaderState(
    val showOnStop: Boolean = true,
    val showOnZone: Boolean = true,
    val showHired: Boolean = true,
    val onStopSortVisible: Boolean = false,
    val onZoneSortVisible: Boolean = false,
    val hiredSortVisible: Boolean = false,
    val tripsSortVisible: Boolean = false,
    val placeholderText: String = "",
)

data class ZoningUiState(
    val loading: Boolean = true,
    val refreshProgress: Int = 0,
    val zoneName: String = "",
    val placeholderText: String = "",
    val showNoCars: Boolean = false,
    val showZonesProgress: Boolean = true,
    val header: ZoningHeaderState = ZoningHeaderState(),
    val zones: List<ZoneModel> = emptyList(),
    val buttons: ZoningButtonsState = ZoningButtonsState(),
    val selectedZone: ZoneModel? = null,
    val listFilter: FilterOptions = FilterOptions.ZONE_BY_MACROZONE,
    val listOrder: OrderOptions = OrderOptions.NONE,
    val isNumericZoneList: Boolean = false,
    val isTablet: Boolean = false,
    val isLandscape: Boolean = false,
    val pendingServicesButton: Boolean? = null,
    val pendingServicesHiredButton: Boolean? = null,
    val hasSoonInZone: Boolean? = null,
    val isInSoonInZone: Boolean = false,
    val isLocatedInZone: Boolean = false,
    val soonToClearButtonStyle: CustomButton.StyleButton? = null,
    val showLocateOnHiredDialog: Boolean = false,
    val showDelocateDialog: Boolean = false,
    val showManualFilterDialog: Boolean = false,
    val dataTypes: String? = null,
    val manualFilterData: List<String> = emptyList(),
)

sealed interface ZoningUiEvent {
    data class Init(val idMacroZone: Int, val onHiredZone: Zone?) : ZoningUiEvent
    data class ZoneClicked(val zone: ZoneModel) : ZoningUiEvent
    data class ZoneLongClicked(val idMacroZone: Int, val idZone: Int) : ZoningUiEvent
    data object PendingClicked : ZoningUiEvent
    data object TripsClicked : ZoningUiEvent
    data object CarsClicked : ZoningUiEvent
    data object BackClicked : ZoningUiEvent
    data object CloseClicked : ZoningUiEvent
    data object LocateOnHiredClicked : ZoningUiEvent
    data object SoonInZoneClicked : ZoningUiEvent
    data object HeaderOnStopClicked : ZoningUiEvent
    data object HeaderOnZoneClicked : ZoningUiEvent
    data object HeaderHiredClicked : ZoningUiEvent
    data object HeaderTripsClicked : ZoningUiEvent
    data object HeaderPlaceholderClicked : ZoningUiEvent
    data class FilterSelected(val filter: FilterOptions) : ZoningUiEvent
    data class ManualFilterConfirmed(val dataSelected: List<String>, val numericInput: Boolean) : ZoningUiEvent
    data class DialogResponse(val button: ZoningDialogButton) : ZoningUiEvent
}

sealed interface ZoningUiEffect {
    data object NavigateBackToMacroZone : ZoningUiEffect
    data object NavigateToHomeAndOnTrip : ZoningUiEffect
    data class NavigateToCars(val idMacroZone: Int, val idZone: Int) : ZoningUiEffect
    data class NavigateToTrips(val idMacroZone: Int, val idZone: Int) : ZoningUiEffect
    data object NavigateToPendingTrips : ZoningUiEffect
    data object NavigateToPointsOfInterest : ZoningUiEffect
    data class ShowToast(val resId: Int) : ZoningUiEffect
    data class OpenDialog(val dialog: ZoningDialogState) : ZoningUiEffect
    data object CloseDialog : ZoningUiEffect
    data object CheckGoingHome : ZoningUiEffect
    data object StartSoonInZone : ZoningUiEffect
    data object CancelSoonInZone : ZoningUiEffect
}

enum class ZoningDialogButton { ACCEPT, CANCEL, WITH_ZONE, WITHOUT_ZONE, POI, ADD_FAVOURITES, REMOVE_FAVOURITES, GOING_HOME, OPEN_REINFORCEMENT }

data class ZoningDialogState(
    val title: String,
    val description: String? = null,
    val buttons: List<ZoningDialogButton> = emptyList(),
    val listOptions: Map<Int, String>? = null,
)

/* ----------------------------- MAIN BRIDGE ------------------------------ */

class ZoningMainActivityViewModelBridge(
    val sharedViewModel: MainActivityViewModel
) {
    val hiredZone = sharedViewModel.hiredZone
    val tripFlow = sharedViewModel.tripFlow
    val zoneFlow = sharedViewModel.zoneFlow
    val shortBreakStatus = sharedViewModel.shortBreakStatus
    val orangeBtnPendingFlow = sharedViewModel.orangeBtnPendingFlow
    val bravoStateFlow = sharedViewModel.bravoStateFlow
    val locationType = sharedViewModel.locationType
    val iTopMeterBreakStatus = sharedViewModel.iTopMeterBreakStatus
    val pendingTripsListFlow = sharedViewModel.pendingTripsListFlow
    val zoningScrollPositionFlow = sharedViewModel.zoningScrollPositionFlow
    val shiftStatusFlow = sharedViewModel.shiftStatusFlow
    val manualZoningNavigationEventFlow = sharedViewModel.manualZoningNavigationEventFlow
    val _pendingTripsListFlow = sharedViewModel._pendingTripsListFlow

    fun saveHiredZone(zone: ZoneModel?) = sharedViewModel.saveHiredZone(zone)
    fun saveZoningScrollPosition(mode: ScrollModeEnum) = sharedViewModel.saveZoningScrollPosition(mode)
    fun emitManualZoningNavigation(value: Boolean) = sharedViewModel.emitManualZoningNavigation(value)
}

/* ----------------------------- VIEWMODEL -------------------------------- */

class ZoningComposeViewModel(
    val zoningUseCase: ZoningUseCase,
    val licensingUseCase: LicensingUseCase,
    val shiftStatusUseCase: ShiftStatusUseCase,
    val locationUseCase: LocationUseCase,
    val userPreferencesUseCase: UserPreferencesUseCase,
    val pendingTripsUseCase: PendingTripsUseCase,
    val soonInZoneUseCase: SoonInZoneUseCase,
    val goingHomeUseCase: GoingHomeUseCase,
    val bluetoothLocalUseCase: BluetoothLocalUseCase,
    val application: Application,
) : AndroidViewModel(application) {

    val TAG = "ZoningComposeViewModel"

    val _uiState = MutableStateFlow(ZoningUiState())
    val uiState: StateFlow<ZoningUiState> = _uiState.asStateFlow()

    val _effects = MutableSharedFlow<ZoningUiEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<ZoningUiEffect> = _effects.asSharedFlow()

    val _zonesListFlow = MutableStateFlow<List<ZoneModel>>(emptyList())
    val _zonesListSortedFlow = MutableStateFlow<List<ZoneModel>>(emptyList())
    val _selectedZoneFlow = MutableStateFlow<ZoneModel?>(null)
    val _listOrderFlow = MutableStateFlow(OrderOptions.NONE)
    val _listFilterFlow = MutableStateFlow(FilterOptions.ZONE_BY_MACROZONE)
    val _macroZoneFlow = MutableStateFlow<Macrozone?>(null)
    val _refreshProgressFlow = MutableStateFlow(0)
    val _hasSoonInZoneFlow = MutableStateFlow<Boolean?>(null)
    val _pendingServicesButtonFlow = MutableStateFlow<Boolean?>(null)
    val _pendingServicesHiredButtonFlow = MutableStateFlow<Boolean?>(null)
    val _soonToClearBtnEnabledFlow = MutableStateFlow<CustomButton.StyleButton?>(null)
    val _isNumericZoneList = MutableStateFlow(false)
    val _isInSoonInZone = MutableStateFlow(false)
    val _isLocatedInZoneFlow = MutableStateFlow(false)
    val _hasCancelledSIZRecently = MutableStateFlow(false)
    val _locationOnHiredParameter = MutableStateFlow<Int?>(null)
    val _driverGoingHomeCounter = MutableStateFlow(0)
    val _zoningListDataTypes = MutableStateFlow<String?>(null)

    var selectedZone: ZoneModel? = null
    var bravoConfiguration: Any? = null
    var isCustomersAtStand: Boolean = false
    private var firstCheck = true
    private var updateData = true

    init {
        viewModelScope.launch {
            _isNumericZoneList.value = userPreferencesUseCase.getUserPreferences()?.numericInputFilter ?: false
            _locationOnHiredParameter.value = licensingUseCase.getLicensingParameters()?.locationInHired
            _hasSoonInZoneFlow.value = null
            _driverGoingHomeCounter.value = goingHomeUseCase.getRemainingDriverGoingHomeTimes()
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isNumericZoneList = _isNumericZoneList.value) }
        }
    }

    fun onEvent(event: ZoningUiEvent) {
        when (event) {
            is ZoningUiEvent.Init -> initViewModelData(event.idMacroZone, event.onHiredZone)
            is ZoningUiEvent.ZoneClicked -> changeSelectedItem(event.zone, null)
            is ZoningUiEvent.ZoneLongClicked -> isZoneInFavourites(event.idMacroZone, event.idZone)
            ZoningUiEvent.PendingClicked -> canOpenPendingTripsFragment()
            ZoningUiEvent.TripsClicked -> returnSelectedZoneData()?.let { (m, z) -> _effects.tryEmit(ZoningUiEffect.NavigateToTrips(m, z)) } ?: _effects.tryEmit(ZoningUiEffect.ShowToast(R.string.select_zone_first))
            ZoningUiEvent.CarsClicked -> returnSelectedZoneData()?.let { (m, z) -> _effects.tryEmit(ZoningUiEffect.NavigateToCars(m, z)) } ?: _effects.tryEmit(ZoningUiEffect.ShowToast(R.string.select_zone_first))
            ZoningUiEvent.BackClicked -> handleBack()
            ZoningUiEvent.CloseClicked -> _effects.tryEmit(ZoningUiEffect.NavigateBackToMacroZone)
            ZoningUiEvent.LocateOnHiredClicked -> getCanZoneStartSoonToClear(selectedZone?.zone ?: return)
            ZoningUiEvent.SoonInZoneClicked -> checkSoonInZone(_isInSoonInZone.value)
            ZoningUiEvent.HeaderOnStopClicked -> onHeaderClick(OrderOptions.IN_STAND)
            ZoningUiEvent.HeaderOnZoneClicked -> onHeaderClick(OrderOptions.IN_ZONE)
            ZoningUiEvent.HeaderHiredClicked -> onHeaderClick(OrderOptions.HIRED)
            ZoningUiEvent.HeaderTripsClicked -> onHeaderClick(OrderOptions.TRIPS)
            ZoningUiEvent.HeaderPlaceholderClicked -> onHeaderClick(OrderOptions.NAME)
            is ZoningUiEvent.FilterSelected -> filterListBy(event.filter)
            is ZoningUiEvent.ManualFilterConfirmed -> {
                _uiState.update { it.copy(manualFilterData = event.dataSelected) }
                filterListBy(FilterOptions.MANUAL)
            }
            is ZoningUiEvent.DialogResponse -> Unit
        }
    }

    fun initViewModelData(idMacroZone: Int, onHiredZone: Zone?) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true) }
            _macroZoneFlow.value = W2CLocation.getZoning().macrozones.find { it.idMacrozone == idMacroZone }
            _uiState.update {
                it.copy(
                    loading = false,
                    zoneName = _macroZoneFlow.value?.nombreMacrozone.orEmpty(),
                    selectedZone = onHiredZone?.let { z -> ZoneModel(z, true) }
                )
            }
            _zonesListFlow.value = W2CLocation.getZoning().getMacrozoneById(idMacroZone).zones.map { ZoneModel(it, it == onHiredZone) }
            processZoningData(_zonesListFlow.value, _listFilterFlow.value, _listOrderFlow.value)
        }
    }

    private fun processZoningData(listZones: List<ZoneModel>, selectedFilters: FilterOptions, sortBy: OrderOptions) {
        var zones = applyFilters(listZones, selectedFilters)
        zones = applySorting(zones, sortBy, selectedFilters)
        zones = applyHotZoneFilterHeaderData(zones, selectedFilters, _isInSoonInZone.value, _isLocatedInZoneFlow.value)
        _zonesListSortedFlow.value = zones
        _uiState.update {
            it.copy(
                zones = zones,
                selectedZone = selectedZone,
                listFilter = selectedFilters,
                listOrder = sortBy,
                hasSoonInZone = _hasSoonInZoneFlow.value,
                isInSoonInZone = _isInSoonInZone.value,
                isLocatedInZone = _isLocatedInZoneFlow.value,
                soonToClearButtonStyle = _soonToClearBtnEnabledFlow.value,
                pendingServicesButton = _pendingServicesButtonFlow.value,
                pendingServicesHiredButton = _pendingServicesHiredButtonFlow.value,
                refreshProgress = _refreshProgressFlow.value,
                placeholderText = _macroZoneFlow.value?.nombreMacrozone.orEmpty(),
                header = ZoningHeaderState(
                    showOnStop = true,
                    showOnZone = true,
                    showHired = true,
                    onStopSortVisible = false,
                    onZoneSortVisible = false,
                    hiredSortVisible = false,
                    tripsSortVisible = false,
                    placeholderText = _macroZoneFlow.value?.nombreMacrozone.orEmpty()
                )
            )
        }
    }

    private fun applyHotZoneFilterHeaderData(zones: List<ZoneModel>, selectedFilters: FilterOptions, isInSoonInZone: Boolean, isLocated: Boolean): List<ZoneModel> = zones

    private fun applySorting(zones: List<ZoneModel>, sortBy: OrderOptions, filter: FilterOptions): List<ZoneModel> = when (sortBy) {
        OrderOptions.NAME -> zones.sortedBy { it.zone.nombreZone }
        OrderOptions.IN_ZONE -> zones.sortedByDescending { it.zone.cochesZone }
        OrderOptions.HIRED -> zones.sortedByDescending { it.zone.cochesOcupado }
        OrderOptions.TRIPS -> zones.sortedByDescending { it.zone.servicios }
        OrderOptions.IN_STAND -> zones.sortedByDescending { it.zone.cochesParada }
        else -> zones
    }

    private fun applyFilters(listZones: List<ZoneModel>, selectedFilters: FilterOptions): List<ZoneModel> = when (selectedFilters) {
        FilterOptions.ZONE_BY_MACROZONE -> listZones
        FilterOptions.ID_NO_HIERARCHY -> W2CLocation.getZoning().macrozones.flatMap { mz -> mz.zones.map { ZoneModel(it, it == selectedZone?.zone) } }
        FilterOptions.NEARNESS -> listZones
        FilterOptions.FAVOURITES -> listZones
        FilterOptions.HOT_ZONES -> listZones
        FilterOptions.UPDATE_MANUAL, FilterOptions.MANUAL -> listZones.filter { zone ->
            _uiState.value.manualFilterData.any { keyword -> zone.zone.nombreZone.contains(keyword, true) }
        }
    }

    private fun onHeaderClick(newOrder: OrderOptions) {
        viewModelScope.launch {
            _listOrderFlow.value = if (_listOrderFlow.value == newOrder) OrderOptions.NONE else newOrder
            processZoningData(_zonesListFlow.value, _listFilterFlow.value, _listOrderFlow.value)
        }
    }

    private fun filterListBy(selectedOptions: FilterOptions) {
        viewModelScope.launch {
            _listFilterFlow.value = if (selectedOptions == FilterOptions.MANUAL && _listFilterFlow.value == selectedOptions) FilterOptions.UPDATE_MANUAL else selectedOptions
            processZoningData(_zonesListFlow.value, _listFilterFlow.value, _listOrderFlow.value)
        }
    }

    private fun changeSelectedItem(zoneModel: ZoneModel, onHiredZone: Zone?) {
        viewModelScope.launch {
            selectedZone = if (selectedZone?.zone == zoneModel.zone) null else zoneModel
            _selectedZoneFlow.value = selectedZone
            updateBtnStcOnZoneSelected(onHiredZone)
            _zonesListSortedFlow.value = _zonesListSortedFlow.value.map {
                it.copy(isSelected = it.zone.idZone == selectedZone?.zone?.idZone && it.zone.nombreZone == selectedZone?.zone?.nombreZone)
            }
            _uiState.update { it.copy(selectedZone = selectedZone, zones = _zonesListSortedFlow.value) }
        }
    }

    private suspend fun updateBtnStcOnZoneSelected(onHiredZone: Zone?) {
        val selection = selectedZone
        if (selection == null && onHiredZone != null) {
            _soonToClearBtnEnabledFlow.emit(CustomButton.StyleButton.DISABLE)
            return
        } else if (selection == null) {
            _soonToClearBtnEnabledFlow.emit(CustomButton.StyleButton.ENABLE)
            return
        }
        if (onHiredZone == null) {
            _soonToClearBtnEnabledFlow.emit(CustomButton.StyleButton.ENABLE)
            return
        }
        val onHiredMacroZone = W2CLocation.getMacrozoneFromZone(onHiredZone)
        val selectedMacroZone = W2CLocation.getMacrozoneFromZone(selection.zone)
        _soonToClearBtnEnabledFlow.emit(
            if (selectedMacroZone.idMacrozone == onHiredMacroZone.idMacrozone && selection.zone.idZone == onHiredZone.idZone) {
                CustomButton.StyleButton.DISABLE
            } else {
                CustomButton.StyleButton.ENABLE
            }
        )
    }

    private fun isZoneInFavourites(idMacroZone: Int, idZone: Int) {
        viewModelScope.launch {
            val map = zoningUseCase.getFavouriteZoneMap()
            val inFav = map?.get(idMacroZone)?.contains(idZone) == true
            _effects.tryEmit(
                ZoningUiEffect.OpenDialog(
                    ZoningDialogState(
                        title = "Favourites",
                        description = if (inFav) "Remove from favourites?" else "Add to favourites?",
                        buttons = if (inFav) listOf(ZoningDialogButton.REMOVE_FAVOURITES) else listOf(ZoningDialogButton.ADD_FAVOURITES)
                    )
                )
            )
        }
    }

    private fun canOpenPendingTripsFragment() {
        viewModelScope.launch {
            _effects.tryEmit(ZoningUiEffect.NavigateToPendingTrips)
        }
    }

    private fun handleBack() {
        viewModelScope.launch {
            if (_listFilterFlow.value == FilterOptions.MANUAL ||
                _listFilterFlow.value == FilterOptions.ID_NO_HIERARCHY ||
                _listFilterFlow.value == FilterOptions.NEARNESS ||
                _listFilterFlow.value == FilterOptions.HOT_ZONES ||
                _listFilterFlow.value == FilterOptions.FAVOURITES
            ) {
                when (shiftStatusUseCase.getStatus()?.currentStatus) {
                    ifConstants.STATE_HIRED,
                    ifConstants.STATE_DISPATCHED,
                    ifConstants.STATE_HIRED_DISPATCHED,
                    ifConstants.STATE_HIRED_NO_CENTRAL -> _effects.tryEmit(ZoningUiEffect.NavigateToHomeAndOnTrip)
                    else -> _effects.tryEmit(ZoningUiEffect.NavigateBackToMacroZone)
                }
            } else {
                _effects.tryEmit(ZoningUiEffect.NavigateBackToMacroZone)
            }
        }
    }

    private fun returnSelectedZoneData(): Pair<Int, Int>? {
        val zone = selectedZone?.zone ?: return null
        val macro = W2CLocation.getMacrozoneFromZone(zone)
        return macro.idMacrozone to zone.idZone
    }

    private fun getCanZoneStartSoonToClear(zone: Zone) {
        viewModelScope.launch {
            _effects.tryEmit(ZoningUiEffect.OpenDialog(ZoningDialogState(title = zone.nombreZone, description = "Ask locate", buttons = listOf(ZoningDialogButton.WITH_ZONE, ZoningDialogButton.WITHOUT_ZONE, ZoningDialogButton.POI))))
        }
    }

    private fun checkSoonInZone(isInSoonInZone: Boolean) {
        viewModelScope.launch {
            _effects.tryEmit(ZoningUiEffect.StartSoonInZone)
        }
    }

    fun saveLocationType(it: String) {
        viewModelScope.launch { _isLocatedInZoneFlow.emit(it == "Z") }
    }

    fun saveIsInSoonInZone(bol: Boolean) {
        viewModelScope.launch { _isInSoonInZone.emit(bol) }
    }

    fun setCancelledSIZ() {
        viewModelScope.launch {
            _hasCancelledSIZRecently.emit(true)
            delay(3000)
            _hasCancelledSIZRecently.emit(false)
        }
    }
}

/* ----------------------------- COMPOSABLES ------------------------------ */

@Composable
fun ZoningScreen(
    viewModel: ZoningComposeViewModel,
    sharedViewModel: ZoningMainActivityViewModelBridge,
    idMacroZone: Int,
    onNavigateToHome: () -> Unit,
    onNavigateToOnTrip: () -> Unit,
    onNavigateToCars: (Int, Int) -> Unit,
    onNavigateToTrips: (Int, Int) -> Unit,
    onNavigateToPendingTrips: () -> Unit,
    onNavigateToPoi: () -> Unit,
    onNavigateBackToMacroZone: () -> Unit,
    onShowToast: (Int) -> Unit,
    onCheckGoingHome: () -> Unit,
    onStartSoonInZone: () -> Unit,
    onCancelSoonInZone: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val hiredZone by sharedViewModel.hiredZone.collectAsStateWithLifecycle()
    val shiftStatus by sharedViewModel.shiftStatusFlow.collectAsStateWithLifecycle()
    val bravoState by sharedViewModel.bravoStateFlow.collectAsStateWithLifecycle()
    val shortBreak by sharedViewModel.shortBreakStatus.collectAsStateWithLifecycle()
    val orangePending by sharedViewModel.orangeBtnPendingFlow.collectAsStateWithLifecycle()
    val locationType by sharedViewModel.locationType.collectAsStateWithLifecycle()
    val iTopBreak by sharedViewModel.iTopMeterBreakStatus.collectAsStateWithLifecycle()

    LaunchedEffect(idMacroZone, hiredZone) {
        viewModel.onEvent(ZoningUiEvent.Init(idMacroZone, hiredZone?.zone))
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                ZoningUiEffect.NavigateBackToMacroZone -> onNavigateBackToMacroZone()
                ZoningUiEffect.NavigateToHomeAndOnTrip -> {
                    onNavigateToHome()
                    onNavigateToOnTrip()
                }
                is ZoningUiEffect.NavigateToCars -> onNavigateToCars(effect.idMacroZone, effect.idZone)
                is ZoningUiEffect.NavigateToTrips -> onNavigateToTrips(effect.idMacroZone, effect.idZone)
                ZoningUiEffect.NavigateToPendingTrips -> onNavigateToPendingTrips()
                ZoningUiEffect.NavigateToPointsOfInterest -> onNavigateToPoi()
                is ZoningUiEffect.ShowToast -> onShowToast(effect.resId)
                ZoningUiEffect.CheckGoingHome -> onCheckGoingHome()
                ZoningUiEffect.StartSoonInZone -> onStartSoonInZone()
                ZoningUiEffect.CancelSoonInZone -> onCancelSoonInZone()
                is ZoningUiEffect.OpenDialog -> Unit
                ZoningUiEffect.CloseDialog -> Unit
            }
        }
    }

    val buttonsState = remember(uiState, shiftStatus, bravoState, shortBreak, orangePending, locationType, iTopBreak, hiredZone) {
        zoningButtonsReducer(
            current = uiState.buttons,
            uiState = uiState,
            shiftStatus = shiftStatus?.currentStatus,
            bravoIsInSoonInZone = bravoState.isInSoonInZone,
            shortBreak = shortBreak,
            orangePending = orangePending,
            locationType = locationType,
            iTopBreak = iTopBreak,
            hiredZone = hiredZone
        )
    }

    val dialogState = remember { mutableStateOf<ZoningDialogState?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            LinearProgressIndicator(
                progress = { uiState.refreshProgress / 1000f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
            )

            Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Text(
                    text = uiState.zoneName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    textAlign = TextAlign.Center
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = uiState.placeholderText.ifBlank { uiState.zoneName }, modifier = Modifier.weight(7f))
                    if (uiState.header.showOnStop) Text(text = "STOP", modifier = Modifier.weight(36f))
                    if (uiState.header.showOnZone) Text(text = "ZONE", modifier = Modifier.weight(36f))
                    if (uiState.header.showHired) Text(text = "HIRED", modifier = Modifier.weight(21f))
                }

                if (uiState.showZonesProgress) {
                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.zones) { zone ->
                        ZoneRow(
                            zone = zone,
                            onClick = { viewModel.onEvent(ZoningUiEvent.ZoneClicked(zone)) },
                            onLongClick = { viewModel.onEvent(ZoningUiEvent.ZoneLongClicked(zone.zone.idMacrozone, zone.zone.idZone)) }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .height(96.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ZoningCustomButton(state = buttonsState.back, onClick = { viewModel.onEvent(ZoningUiEvent.BackClicked) })
                ZoningCustomButton(state = buttonsState.pending, onClick = { viewModel.onEvent(ZoningUiEvent.PendingClicked) })
                ZoningCustomButton(state = buttonsState.close, onClick = { viewModel.onEvent(ZoningUiEvent.CloseClicked) })
            }
        }

        if (dialogState.value != null) {
            ZoningDialog(
                state = dialogState.value!!,
                onDismiss = { dialogState.value = null },
                onButton = { button ->
                    dialogState.value = null
                    viewModel.onEvent(ZoningUiEvent.DialogResponse(button))
                }
            )
        }
    }
}

@Composable
private fun ZoningCustomButton(
    state: ZoningButtonVisualState,
    onClick: () -> Unit
) {
    if (!state.visible) return
    Button(
        onClick = onClick,
        enabled = state.enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = when (state.background) {
                CustomButton.BackgroundButtonColor.GREEN -> Color(0xFF2E7D32)
                CustomButton.BackgroundButtonColor.RED -> Color(0xFFC62828)
                CustomButton.BackgroundButtonColor.BLUE -> Color(0xFF1565C0)
                CustomButton.BackgroundButtonColor.YELLOW -> Color(0xFFF9A825)
                CustomButton.BackgroundButtonColor.ORANGE -> Color(0xFFEF6C00)
                CustomButton.BackgroundButtonColor.BLACK -> Color.Black
                CustomButton.BackgroundButtonColor.TRANSPARENT -> Color.Transparent
                CustomButton.BackgroundButtonColor.DEFAULT -> Color(0xFF1565C0)
            }
        ),
        modifier = Modifier
            .weight(1f)
            .padding(1.dp)
            .fillMaxHeight()
    ) {
        Text(text = state.text)
    }
}

@Composable
private fun ZoneRow(
    zone: ZoneModel,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(text = zone.zone.nombreZone)
        Divider()
    }
}

@Composable
private fun ZoningDialog(
    state: ZoningDialogState,
    onDismiss: () -> Unit,
    onButton: (ZoningDialogButton) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(state.title) },
        text = { state.description?.let { Text(it) } },
        confirmButton = {
            Row {
                state.buttons.forEach { button ->
                    OutlinedButton(onClick = { onButton(button) }) {
                        Text(button.name)
                    }
                }
            }
        }
    )
}

/* ----------------------------- REDUCER ---------------------------------- */

fun zoningButtonsReducer(
    current: ZoningButtonsState,
    uiState: ZoningUiState,
    shiftStatus: Int?,
    bravoIsInSoonInZone: Boolean,
    shortBreak: ShortBreakStatus?,
    orangePending: Boolean?,
    locationType: String?,
    iTopBreak: Boolean,
    hiredZone: ZoneModel?
): ZoningButtonsState {
    val isHired = shiftStatus?.let {
        it == ifConstants.STATE_HIRED ||
            it == ifConstants.STATE_DISPATCHED ||
            it == ifConstants.STATE_HIRED_DISPATCHED ||
            it == ifConstants.STATE_HIRED_NO_CENTRAL
    } ?: false

    val locateVisible = isHired
    val soonVisible = !isHired && shiftStatus != ifConstants.STATE_DISPATCHED && (uiState.hasSoonInZone == true)

    val pendingEnabled = when {
        uiState.zones.isEmpty() -> false
        shortBreak == ShortBreakStatus.IN_SHORT_BREAK || shortBreak == ShortBreakStatus.IN_SHORT_BREAK_FORCED -> false
        !W2CLocation.isLocationAllowedByCentral() -> false
        bravoIsInSoonInZone -> true
        uiState.pendingServicesHiredButton != null -> uiState.pendingServicesHiredButton == true && hiredZone != null
        uiState.pendingServicesButton == true -> true
        else -> false
    }

    val pendingBg = if (orangePending == true) {
        CustomButton.BackgroundButtonColor.ORANGE
    } else {
        CustomButton.BackgroundButtonColor.BLUE
    }

    val soonStyle = when {
        (!bravoIsInSoonInZone && (locationType != "Z" || shortBreak == ShortBreakStatus.IN_SHORT_BREAK || shortBreak == ShortBreakStatus.IN_SHORT_BREAK_FORCED || iTopBreak)) || uiState.showDelocateDialog -> CustomButton.StyleButton.DISABLE
        else -> CustomButton.StyleButton.ENABLE
    }

    val locateStyle = uiState.soonToClearButtonStyle ?: CustomButton.StyleButton.ENABLE

    return current.copy(
        locateOnHired = current.locateOnHired.copy(
            visible = locateVisible,
            enabled = locateStyle != CustomButton.StyleButton.DISABLE,
            style = locateStyle,
            background = if (locateStyle == CustomButton.StyleButton.DISABLE) CustomButton.BackgroundButtonColor.RED else CustomButton.BackgroundButtonColor.BLUE,
            text = if (locateStyle == CustomButton.StyleButton.DISABLE) "DELOCATE" else "SOON TO CLEAR"
        ),
        pending = current.pending.copy(
            visible = true,
            enabled = pendingEnabled,
            style = if (pendingEnabled) CustomButton.StyleButton.ENABLE else CustomButton.StyleButton.DISABLE,
            background = pendingBg,
            text = "PENDIENTES"
        ),
        soonInZone = current.soonInZone.copy(
            visible = soonVisible,
            enabled = soonStyle == CustomButton.StyleButton.ENABLE,
            style = soonStyle,
            background = if (bravoIsInSoonInZone) CustomButton.BackgroundButtonColor.RED else CustomButton.BackgroundButtonColor.GREEN,
            text = "SOON IN ZONE"
        ),
        trips = current.trips.copy(
            visible = true,
            enabled = true,
            style = CustomButton.StyleButton.ENABLE,
            background = CustomButton.BackgroundButtonColor.BLUE,
            text = "TRIPS"
        ),
        cars = current.cars.copy(
            visible = true,
            enabled = true,
            style = CustomButton.StyleButton.ENABLE,
            background = CustomButton.BackgroundButtonColor.BLUE,
            text = "CARS"
        )
    )
}

/* ----------------------------- EXTRA VIEWMODELS ------------------------- */

class RequestStandReinforcementViewModel(
    val zoningUseCase: ZoningUseCase,
    context: Application
) : BaseViewModel(context) {
    fun sendReinforcementRequest(idMacrozone: Int, idZone: Int, numCustomers: Int) {
        viewModelScope.launch { zoningUseCase.sendReinforcementRequest(idMacrozone, idZone, numCustomers) }
    }
    fun addZoneToFavourites(idMacroZone: Int, idZone: Int) {
        viewModelScope.launch { zoningUseCase.addFavouriteZone(idMacroZone, idZone) }
    }
    fun removeZoneFromFavourites(idMacroZone: Int, idZone: Int) {
        viewModelScope.launch { zoningUseCase.removeFavouriteZone(idMacroZone, idZone) }
    }
}

class TripHistoryViewModel(
    context: Application,
    val tripUseCase: TripUseCase,
    val printerUseCase: PrinterUseCase,
    val portugalUseCase: PortugalUseCase,
) : BaseViewModel(context) {

    val TAG = "TripHistoryViewModel"
    val _tripListFlow = MutableStateFlow<List<Trip>?>(null)
    val tripListFlow = _tripListFlow.asStateFlow()
    val _loadingFlow = MutableStateFlow(false)
    val loadingFlow = _loadingFlow.asStateFlow()
    var isLastPage = false
    val PAGE_SIZE = 25

    fun getTrips(page: Int) {
        viewModelScope.launch {
            try {
                Logs.d(TAG, "getTrips: page: $page")
                if (isLastPage) return@launch
                _loadingFlow.value = true
                val loadedItems = page * PAGE_SIZE
                val newTrips = tripUseCase.getAllTripsFinishedPaged(loadedItems)
                val currentTrips = _tripListFlow.value ?: emptyList()
                if (newTrips.size < PAGE_SIZE) isLastPage = true
                _tripListFlow.emit((currentTrips + newTrips).distinctBy { it.id })
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
                selectedTrips.forEach { trip ->
                    tripUseCase.deleteTrip(trip)
                    callback.invoke()
                }
                withContext(Dispatchers.Main) {
                    android.widget.Toast.makeText(
                        context,
                        context.getString(R.string.toast_trips_eliminados_correctamente),
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                withContext(Dispatchers.Main) {
                    android.widget.Toast.makeText(
                        context,
                        context.getString(R.string.toast_seleccione_algun_trip),
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
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
