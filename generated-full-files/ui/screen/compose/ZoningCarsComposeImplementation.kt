package ifac.td.taxi.ui.screen.compose
import ifac.td.taxi.HomeDirections
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ifac.td.taxi.repository.room.entities.BravoConfigurationVariableEntity
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.ViewModel
import ifac.td.taxi.framework.util.Logs
import kotlinx.coroutines.launch
import ifac.td.taxi.repository.connections.service.model.ShortBreakStatus
import ifac.td.taxi.framework.sdk.usecase.LicensingUseCase
import ifac.td.taxi.ui.model.ZoneCarModel
import com.interfacom.sdk.taximeter.licensing.models.zoning.Zone
import ifac.td.taxi.ui.custom.button.ButtonType
import androidx.compose.ui.Modifier
import kotlinx.coroutines.Dispatchers
import android.content.Intent
import com.interfacom.sdk.taximeter.bravocomm.rest.pending_trips.response.PendingTrip
import ifac.td.taxi.domain.usecase.UserPreferencesUseCase
import androidx.compose.foundation.lazy.LazyColumn
import ifac.td.taxi.domain.usecase.ShiftStatusUseCase
import ifac.td.taxi.framework.sdk.usecase.NavigatorUseCase
import ifac.td.taxi.domain.usecase.PendingTripsUseCase
import androidx.compose.foundation.background
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import ifac.td.taxi.domain.usecase.LocationUseCase
import ifac.td.taxi.ui.model.LocationEnum
import androidx.compose.material3.*
import ifac.td.taxi.ui.model.ZoneCarRowModel
import com.interfacom.sdk.taximeter.bravocomm.W2CLocation
import com.interfacom.sdk.taximeter.bravocomm.BravoCentral
import androidx.compose.ui.Alignment
import ifac.td.taxi.framework.sdk.usecase.ZoningUseCase
import com.interfacom.sdk.taximeter.bravocomm.ifConstants
import ifac.td.taxi.repository.room.dao.BravoConfigurationVariableDao
import androidx.compose.ui.graphics.Color
import ifac.td.taxi.ui.custom.button.CustomButton
import kotlinx.coroutines.flow.*
import ifac.td.taxi.R


// -----------------------------
// UI STATE / EFFECT / EVENT
// -----------------------------

data class ZoningCarsUiState(
    val zoneName: String = "",
    val isLoadingCars: Boolean = true,
    val refreshProgress: Int = 0,
    val cars: List<ZoneCarRowModel> = emptyList(),
    val noCarsVisible: Boolean = false,
    val buttons: ZoningCarsButtonsState = ZoningCarsButtonsState(),
    val dialog: ZoningCarsDialogState? = null,
)

data class ZoningCarsButtonsState(
    val pending: ZoningCarsButtonVisualState = ZoningCarsButtonVisualState(
        visible = true,
        enabled = false,
        background = CustomButton.BackgroundButtonColor.BLUE,
        buttonType = ButtonType.PENDIENTES,
    ),
    val soonInZone: ZoningCarsButtonVisualState = ZoningCarsButtonVisualState(
        visible = false,
        enabled = true,
        background = CustomButton.BackgroundButtonColor.GREEN,
        buttonType = ButtonType.SOON_IN_ZONE,
    ),
    val locateOnHired: ZoningCarsButtonVisualState = ZoningCarsButtonVisualState(
        visible = false,
        enabled = true,
        background = CustomButton.BackgroundButtonColor.BLUE,
        buttonType = ButtonType.UBICACION,
    ),
    val trips: ZoningCarsButtonVisualState = ZoningCarsButtonVisualState(
        visible = true,
        enabled = true,
        background = CustomButton.BackgroundButtonColor.BLUE,
        buttonType = ButtonType.TRIPS,
    ),
    val cars: ZoningCarsButtonVisualState = ZoningCarsButtonVisualState(
        visible = true,
        enabled = true,
        background = CustomButton.BackgroundButtonColor.BLUE,
        buttonType = ButtonType.CARS,
    ),
)

data class ZoningCarsButtonVisualState(
    val visible: Boolean = true,
    val enabled: Boolean = true,
    val background: CustomButton.BackgroundButtonColor = CustomButton.BackgroundButtonColor.BLUE,
    val buttonType: ButtonType = ButtonType.PENDIENTES,
)

data class ZoningCarsDialogState(
    val title: String,
    val description: String,
    val buttons: List<ButtonType>,
    val kind: Kind,
) {
    enum class Kind { LOCATE_ON_HIRED, DELOCATE_ON_HIRED }
}

sealed interface ZoningCarsUiEvent {
    data object OnResume : ZoningCarsUiEvent
    data object OnBackClicked : ZoningCarsUiEvent
    data object OnCloseClicked : ZoningCarsUiEvent
    data object OnPendingClicked : ZoningCarsUiEvent
    data object OnTripsClicked : ZoningCarsUiEvent
    data object OnCarsClicked : ZoningCarsUiEvent
    data object OnSoonInZoneClicked : ZoningCarsUiEvent
    data object OnLocateOnHiredClicked : ZoningCarsUiEvent
    data object OnDialogDismissed : ZoningCarsUiEvent

    data class OnInit(val idMacroZone: Int, val idZone: Int) : ZoningCarsUiEvent
    data class OnCarsLoaded(val rows: List<ZoneCarRowModel>) : ZoningCarsUiEvent
    data class OnRefreshProgress(val progress: Int) : ZoningCarsUiEvent
    data class OnZoneFlowChanged(val zone: String?) : ZoningCarsUiEvent
    data class OnShortBreakChanged(val status: ShortBreakStatus?) : ZoningCarsUiEvent
    data class OnPendingServicesButtonChanged(val value: Boolean?) : ZoningCarsUiEvent
    data class OnPendingServicesHiredButtonChanged(val value: Boolean?) : ZoningCarsUiEvent
    data class OnOrangePendingChanged(val value: Boolean?) : ZoningCarsUiEvent
    data class OnSoonInZoneChanged(val value: Boolean) : ZoningCarsUiEvent
    data class OnLocationAllowedChanged(val value: Boolean) : ZoningCarsUiEvent
    data class OnHiredZoneChanged(val exists: Boolean) : ZoningCarsUiEvent
}

sealed interface ZoningCarsUiEffect {
    data object NavigateBack : ZoningCarsUiEffect
    data object NavigateToHomeThenOnTrip : ZoningCarsUiEffect
    data object NavigateToPendingTrips : ZoningCarsUiEffect
    data object NavigateToPointsOfInterest : ZoningCarsUiEffect
    data object ShowNoPendingToast : ZoningCarsUiEffect
    data class SaveHiredZone(val zone: Zone?) : ZoningCarsUiEffect
    data class OpenDialog(val dialog: ZoningCarsDialogState) : ZoningCarsUiEffect
}

// -----------------------------
// EXACT BUTTON REDUCER
// -----------------------------

object ZoningCarsButtonsReducer {

    fun reduce(
        current: ZoningCarsButtonsState,
        zoneFlow: String?,
        shortBreakStatus: ShortBreakStatus?,
        pendingServicesButton: Boolean?,
        pendingServicesHiredButton: Boolean?,
        orangeBtnPending: Boolean?,
        isSoonInZone: Boolean,
        isLocationAllowedByCentral: Boolean,
        hiredZoneExists: Boolean,
    ): ZoningCarsButtonsState {
        val pendingBase = current.pending.copy(
            background = if (orangeBtnPending == true) {
                CustomButton.BackgroundButtonColor.ORANGE
            } else {
                CustomButton.BackgroundButtonColor.BLUE
            }
        )

        if (zoneFlow.isNullOrBlank()) {
            return current.copy(
                pending = pendingBase.copy(enabled = false)
            )
        }

        val isInShortBreak =
            shortBreakStatus == ShortBreakStatus.IN_SHORT_BREAK ||
                shortBreakStatus == ShortBreakStatus.IN_SHORT_BREAK_FORCED

        if (zoneFlow.isNullOrEmpty()) {
            return current.copy(
                pending = pendingBase.copy(enabled = false)
            )
        }

        if (isInShortBreak) {
            return current.copy(
                pending = pendingBase.copy(enabled = false)
            )
        }

        if (!isLocationAllowedByCentral) {
            return current.copy(
                pending = pendingBase.copy(enabled = false)
            )
        }

        if (isSoonInZone) {
            return current.copy(
                pending = pendingBase.copy(enabled = true)
            )
        }

        if (pendingServicesHiredButton != null) {
            return current.copy(
                pending = pendingBase.copy(
                    enabled = pendingServicesHiredButton && hiredZoneExists
                )
            )
        }

        if (pendingServicesButton == true) {
            return current.copy(
                pending = pendingBase.copy(enabled = true)
            )
        }

        return current.copy(
            pending = pendingBase.copy(enabled = false)
        )
    }
}

// -----------------------------
// MAIN ACTIVITY COMPOSE BRIDGE
// -----------------------------

class ZoningCarsMainActivityViewModel : ViewModel() {

    val _zoneFlow = MutableStateFlow<String?>(null)
    val zoneFlow = _zoneFlow.asStateFlow()

    val _shortBreakStatus = MutableStateFlow<ShortBreakStatus?>(null)
    val shortBreakStatus = _shortBreakStatus.asStateFlow()

    val _orangeBtnPendingFlow = MutableStateFlow<Boolean?>(null)
    val orangeBtnPendingFlow = _orangeBtnPendingFlow.asStateFlow()

    val _pendingTripsListFlow =
        MutableStateFlow<ArrayList<PendingTrip>?>(null)
    val pendingTripsListFlow = _pendingTripsListFlow.asStateFlow()

    val _hiredZone = MutableStateFlow<Zone?>(null)
    val hiredZone = _hiredZone.asStateFlow()

    val _tripFlow = MutableStateFlow<Any?>(null)
    val tripFlow = _tripFlow.asStateFlow()

    val _shiftStatusFlow = MutableStateFlow<Any?>(null)
    val shiftStatusFlow = _shiftStatusFlow.asStateFlow()

    val _manualZoningNavigation = MutableStateFlow(false)
    val manualZoningNavigation = _manualZoningNavigation.asStateFlow()

    fun emitManualZoningNavigation(value: Boolean) {
        _manualZoningNavigation.value = value
    }

    fun saveHiredZone(zone: Zone?) {
        _hiredZone.value = zone
    }

    fun setZone(value: String?) {
        _zoneFlow.value = value
    }

    fun setShortBreakStatus(value: ShortBreakStatus?) {
        _shortBreakStatus.value = value
    }

    fun setOrangeBtnPending(value: Boolean?) {
        _orangeBtnPendingFlow.value = value
    }

    fun setPendingTripsList(value: ArrayList<PendingTrip>?) {
        _pendingTripsListFlow.value = value
    }

    fun setTrip(value: Any?) {
        _tripFlow.value = value
    }

    fun setShiftStatus(value: Any?) {
        _shiftStatusFlow.value = value
    }
}

// -----------------------------
// ZONING CARS VIEWMODEL
// -----------------------------

class ZoningCarsViewModel(
    val zoningUseCase: ZoningUseCase,
    val licensingUseCase: LicensingUseCase,
    val locationUseCase: LocationUseCase,
    val shiftStatusUseCase: ShiftStatusUseCase,
    val bravoConfigurationVariableDao: BravoConfigurationVariableDao,
    val userPreferencesUseCase: UserPreferencesUseCase,
    val pendingTripsUseCase: PendingTripsUseCase,
    context: android.app.Application,
) : BaseViewModel(context) {

    val TAG = "ZoningCarsViewModel"

    val _uiState = MutableStateFlow(ZoningCarsUiState())
    val uiState = _uiState.asStateFlow()

    val _uiEffects = MutableSharedFlow<ZoningCarsUiEffect>()
    val uiEffects = _uiEffects.asSharedFlow()

    val _locationOnHiredSentFlow = MutableSharedFlow<Zone?>()

    private var pendingServicesButton: Boolean? = null
    private var pendingServicesHiredButton: Boolean? = null
    private var orangeBtnPending: Boolean? = null
    private var zoneFlowValue: String? = null
    private var shortBreakStatusValue: ShortBreakStatus? = null
    private var isSoonInZoneValue: Boolean = false
    private var isLocationAllowedByCentralValue: Boolean = true
    private var hiredZoneExistsValue: Boolean = false

    var bravoConfiguration: BravoConfigurationVariableEntity? = null

    val handler = android.os.Handler(android.os.Looper.getMainLooper())
    private lateinit var zonesCarsRunnable: Runnable

    fun onEvent(event: ZoningCarsUiEvent) {
        when (event) {
            is ZoningCarsUiEvent.OnInit -> initViewModelData(event.idMacroZone, event.idZone)
            is ZoningCarsUiEvent.OnCarsLoaded -> {
                _uiState.update { it.copy(cars = event.rows, noCarsVisible = event.rows.isEmpty(), isLoadingCars = false) }
            }
            is ZoningCarsUiEvent.OnRefreshProgress -> _uiState.update { it.copy(refreshProgress = event.progress) }
            is ZoningCarsUiEvent.OnZoneFlowChanged -> {
                zoneFlowValue = event.zone
                reduceButtons()
            }
            is ZoningCarsUiEvent.OnShortBreakChanged -> {
                shortBreakStatusValue = event.status
                reduceButtons()
            }
            is ZoningCarsUiEvent.OnPendingServicesButtonChanged -> {
                pendingServicesButton = event.value
                reduceButtons()
            }
            is ZoningCarsUiEvent.OnPendingServicesHiredButtonChanged -> {
                pendingServicesHiredButton = event.value
                reduceButtons()
            }
            is ZoningCarsUiEvent.OnOrangePendingChanged -> {
                orangeBtnPending = event.value
                reduceButtons()
            }
            is ZoningCarsUiEvent.OnSoonInZoneChanged -> {
                isSoonInZoneValue = event.value
                reduceButtons()
            }
            is ZoningCarsUiEvent.OnLocationAllowedChanged -> {
                isLocationAllowedByCentralValue = event.value
                reduceButtons()
            }
            is ZoningCarsUiEvent.OnHiredZoneChanged -> {
                hiredZoneExistsValue = event.exists
                reduceButtons()
            }
            ZoningCarsUiEvent.OnBackClicked -> viewModelScope.launch { _uiEffects.emit(ZoningCarsUiEffect.NavigateBack) }
            ZoningCarsUiEvent.OnCloseClicked -> handleCloseClicked()
            ZoningCarsUiEvent.OnPendingClicked -> canOpenPendingTripsFragment(sharedPendingTripsFlow)
            ZoningCarsUiEvent.OnTripsClicked -> Unit
            ZoningCarsUiEvent.OnCarsClicked -> Unit
            ZoningCarsUiEvent.OnSoonInZoneClicked -> Unit
            ZoningCarsUiEvent.OnLocateOnHiredClicked -> openLocateOnHiredDialog()
            ZoningCarsUiEvent.OnDialogDismissed -> _uiState.update { it.copy(dialog = null) }
            ZoningCarsUiEvent.OnResume -> Unit
        }
    }

    private fun reduceButtons() {
        _uiState.update {
            it.copy(
                buttons = ZoningCarsButtonsReducer.reduce(
                    current = it.buttons,
                    zoneFlow = zoneFlowValue,
                    shortBreakStatus = shortBreakStatusValue,
                    pendingServicesButton = pendingServicesButton,
                    pendingServicesHiredButton = pendingServicesHiredButton,
                    orangeBtnPending = orangeBtnPending,
                    isSoonInZone = isSoonInZoneValue,
                    isLocationAllowedByCentral = isLocationAllowedByCentralValue,
                    hiredZoneExists = hiredZoneExistsValue
                )
            )
        }
    }

    fun initViewModelData(idMacroZone: Int, idZone: Int) {
        removeHandlerCallbacks()
        viewModelScope.launch {
            val refreshRate = licensingUseCase.getLicensingParameters()?.segRefrescoConsUbicacion
            refreshRate?.let { seconds ->
                val millis = (seconds * 1000).toLong()
                var lastUpdate = System.currentTimeMillis()

                zoningUseCase.getCarsInformation(idMacroZone, idZone) { cars ->
                    viewModelScope.launch(Dispatchers.IO) {
                        _uiState.update {
                            it.copy(
                                cars = getCarListByType(cars),
                                noCarsVisible = cars.isEmpty(),
                                isLoadingCars = false
                            )
                        }
                    }
                }
                _uiState.update { it.copy(refreshProgress = 1000) }

                removeHandlerCallbacks()
                zonesCarsRunnable = object : Runnable {
                    override fun run() {
                        viewModelScope.launch {
                            val elapsed = System.currentTimeMillis() - lastUpdate
                            val progress = 1000 - ((elapsed.toFloat() / millis) * 1000).toInt().coerceIn(0, 1000)
                            _uiState.update { it.copy(refreshProgress = progress) }

                            if (elapsed >= millis) {
                                zoningUseCase.getCarsInformation(idMacroZone, idZone) { cars ->
                                    viewModelScope.launch(Dispatchers.IO) {
                                        _uiState.update {
                                            it.copy(
                                                cars = getCarListByType(cars),
                                                noCarsVisible = cars.isEmpty(),
                                                isLoadingCars = false
                                            )
                                        }
                                    }
                                }
                                lastUpdate = System.currentTimeMillis()
                                _uiState.update { it.copy(refreshProgress = 1000) }
                            }
                        }
                        handler.postDelayed(this, 1000)
                    }
                }
                handler.post(zonesCarsRunnable)
            }
        }
    }

    fun removeHandlerCallbacks() {
        if (::zonesCarsRunnable.isInitialized) handler.removeCallbacks(zonesCarsRunnable)
    }

    fun checkPendingServiceOnHiredPermission() {
        viewModelScope.launch {
            val shiftStatus = shiftStatusUseCase.getStatus()?.currentStatus
            if (shiftStatus != null && shiftStatusUseCase.isHired(shiftStatus)) {
                val parameters = licensingUseCase.getLicensingParameters()
                onEvent(ZoningCarsUiEvent.OnPendingServicesHiredButtonChanged(parameters?.isPendingOnHired))
            }
        }
    }

    fun checkPendingServiceOnForHirePermission() {
        viewModelScope.launch {
            val shiftStatus = shiftStatusUseCase.getStatus()?.currentStatus
            if (shiftStatus != null && shiftStatusUseCase.isVacant(shiftStatus)) {
                val parameters = licensingUseCase.getLicensingParameters()
                onEvent(ZoningCarsUiEvent.OnPendingServicesButtonChanged(parameters?.isVacantPendingServices))
            }
        }
    }

    fun getPOIsValue() {
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
        val carsByType = list.groupBy { it.status }
        val maxCarTypesInRow = listOf(
            carsByType[LocationEnum.HIRED]?.size ?: 0,
            carsByType[LocationEnum.ZONE]?.size ?: 0,
            carsByType[LocationEnum.STOP]?.size ?: 0
        ).maxOrNull() ?: 0

        return buildList {
            for (i in 0 until maxCarTypesInRow) {
                add(
                    ZoneCarRowModel(
                        index = size + 1,
                        carHired = carsByType[LocationEnum.HIRED]?.getOrNull(i),
                        carRank = carsByType[LocationEnum.STOP]?.getOrNull(i),
                        carZone = carsByType[LocationEnum.ZONE]?.getOrNull(i)
                    )
                )
            }
        }
    }

    private fun canOpenPendingTripsFragment(pendingTripsListFlow: MutableStateFlow<ArrayList<PendingTrip>?>) {
        viewModelScope.launch {
            userPreferencesUseCase.getUserPreferences()?.let {
                if (it.closePendingTripsIfEmpty) {
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

    val _pendingServicesButtonPressedCallback = MutableSharedFlow<Boolean>()

    init {
        viewModelScope.launch {
            _locationOnHiredSentFlow.collect { zone ->
                _uiEffects.emit(ZoningCarsUiEffect.SaveHiredZone(zone))
                if (zone != null) {
                    _uiEffects.emit(ZoningCarsUiEffect.NavigateToHomeThenOnTrip)
                }
            }
        }
        viewModelScope.launch {
            _pendingServicesButtonPressedCallback.collect { allowed ->
                if (allowed) {
                    _uiEffects.emit(ZoningCarsUiEffect.NavigateToPendingTrips)
                } else {
                    _uiEffects.emit(ZoningCarsUiEffect.ShowNoPendingToast)
                }
            }
        }
    }

    private fun handleCloseClicked() {
        viewModelScope.launch {
            when (shiftStatusUseCase.getStatus()?.currentStatus) {
                ifConstants.STATE_HIRED,
                ifConstants.STATE_DISPATCHED,
                ifConstants.STATE_HIRED_DISPATCHED,
                ifConstants.STATE_HIRED_NO_CENTRAL -> _uiEffects.emit(ZoningCarsUiEffect.NavigateToHomeThenOnTrip)
                else -> _uiEffects.emit(ZoningCarsUiEffect.NavigateToHomeThenOnTrip)
            }
        }
    }

    private fun openLocateOnHiredDialog() {
        val zone = W2CLocation
            .getZoning()
            .macrozones
            .firstOrNull()
            ?.zones
            ?.firstOrNull()

        if (zone != null) {
            val dialog = ZoningCarsDialogState(
                title = zone.nombreZone,
                description = "Ask locate",
                buttons = listOf(ButtonType.POI, ButtonType.ACCEPT),
                kind = ZoningCarsDialogState.Kind.LOCATE_ON_HIRED
            )
            viewModelScope.launch { _uiEffects.emit(ZoningCarsUiEffect.OpenDialog(dialog)) }
        }
    }
}

// -----------------------------
// COMPOSE SCREEN
// -----------------------------

@Composable
fun ZoningCarsScreen(
    viewModel: ZoningCarsViewModel,
    sharedViewModel: ZoningCarsMainActivityViewModel,
    idMacroZone: Int,
    idZone: Int,
    onNavigateBack: () -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateOnTrip: () -> Unit,
    onNavigatePendingTrips: () -> Unit,
    onNavigatePoi: () -> Unit,
    onShowToastNoPending: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(idMacroZone, idZone) {
        viewModel.onEvent(ZoningCarsUiEvent.OnInit(idMacroZone, idZone))
        viewModel.getPOIsValue()
        viewModel.checkPendingServiceOnForHirePermission()
        viewModel.checkPendingServiceOnHiredPermission()
    }

    LaunchedEffect(Unit) {
        viewModel.uiEffects.collectLatest { effect ->
            when (effect) {
                ZoningCarsUiEffect.NavigateBack -> onNavigateBack()
                ZoningCarsUiEffect.NavigateToHomeThenOnTrip -> {
                    onNavigateHome()
                    onNavigateOnTrip()
                }
                ZoningCarsUiEffect.NavigateToPendingTrips -> onNavigatePendingTrips()
                ZoningCarsUiEffect.NavigateToPointsOfInterest -> onNavigatePoi()
                ZoningCarsUiEffect.ShowNoPendingToast -> onShowToastNoPending()
                is ZoningCarsUiEffect.SaveHiredZone -> sharedViewModel.saveHiredZone(effect.zone)
                is ZoningCarsUiEffect.OpenDialog -> Unit
            }
        }
    }

    val dialogState = uiState.dialog
    if (dialogState != null) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(ZoningCarsUiEvent.OnDialogDismissed) },
            title = { Text(dialogState.title) },
            text = { Text(dialogState.description) },
            confirmButton = {
                TextButton(onClick = { viewModel.onEvent(ZoningCarsUiEvent.OnDialogDismissed) }) {
                    Text("OK")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        LinearProgressIndicator(
            progress = { uiState.refreshProgress / 1000f },
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.isLoadingCars) {
                CircularProgressIndicator()
            } else if (uiState.noCarsVisible) {
                Text("No cars", color = Color.White)
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.cars.size) { index ->
                        Text(
                            text = uiState.cars[index].toString(),
                            color = Color.White,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }

        ZoningCarsBottomMenu(
            buttons = uiState.buttons,
            onPending = { viewModel.onEvent(ZoningCarsUiEvent.OnPendingClicked) },
            onSoonInZone = { viewModel.onEvent(ZoningCarsUiEvent.OnSoonInZoneClicked) },
            onLocateOnHired = { viewModel.onEvent(ZoningCarsUiEvent.OnLocateOnHiredClicked) },
            onTrips = { viewModel.onEvent(ZoningCarsUiEvent.OnTripsClicked) },
            onCars = { viewModel.onEvent(ZoningCarsUiEvent.OnCarsClicked) },
        )
    }
}

@Composable
private fun ZoningCarsBottomMenu(
    buttons: ZoningCarsButtonsState,
    onPending: () -> Unit,
    onSoonInZone: () -> Unit,
    onLocateOnHired: () -> Unit,
    onTrips: () -> Unit,
    onCars: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
            .padding(6.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            ZoningCarsCustomButton(buttons.pending, onPending, Modifier.weight(1f))
            if (buttons.soonInZone.visible) {
                ZoningCarsCustomButton(buttons.soonInZone, onSoonInZone, Modifier.weight(1f))
            } else {
                Spacer(Modifier.weight(1f))
            }
            if (buttons.locateOnHired.visible) {
                ZoningCarsCustomButton(buttons.locateOnHired, onLocateOnHired, Modifier.weight(1f))
            } else {
                Spacer(Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            ZoningCarsCustomButton(buttons.trips, onTrips, Modifier.weight(1f))
            ZoningCarsCustomButton(buttons.cars, onCars, Modifier.weight(1f))
            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun ZoningCarsCustomButton(
    state: ZoningCarsButtonVisualState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!state.visible) return

    val bg = when (state.background) {
        CustomButton.BackgroundButtonColor.GREEN -> Color(0xFF2E7D32)
        CustomButton.BackgroundButtonColor.RED -> Color(0xFFC62828)
        CustomButton.BackgroundButtonColor.BLUE -> Color(0xFF1565C0)
        CustomButton.BackgroundButtonColor.YELLOW -> Color(0xFFF9A825)
        CustomButton.BackgroundButtonColor.ORANGE -> Color(0xFFEF6C00)
        CustomButton.BackgroundButtonColor.DEFAULT -> Color(0xFF1565C0)
        CustomButton.BackgroundButtonColor.BLACK -> Color.Black
        CustomButton.BackgroundButtonColor.TRANSPARENT -> Color.Transparent
    }

    val enabledBg = if (state.enabled) bg else Color(0xFF9E9E9E)
    val textColor = if (state.enabled) Color.White else Color(0xFFDDDDDD)

    Button(
        onClick = onClick,
        enabled = state.enabled,
        modifier = modifier
            .padding(1.dp)
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = enabledBg,
            contentColor = textColor,
            disabledContainerColor = Color(0xFF9E9E9E),
            disabledContentColor = Color(0xFFDDDDDD)
        )
    ) {
        Text(
            text = state.buttonType.name,
            textAlign = TextAlign.Center
        )
    }
}

// -----------------------------
// EXTRA VIEWMODELS FROM YOUR INPUT
// -----------------------------

class RequestStandReinforcementViewModel(
    val zoningUseCase: ZoningUseCase,
    context: android.app.Application
) : BaseViewModel(context) {

    fun sendReinforcementRequest(idMacrozone: Int, idZone: Int, numCustomers: Int) {
        viewModelScope.launch {
            zoningUseCase.sendReinforcementRequest(idMacrozone, idZone, numCustomers)
        }
    }

    fun addZoneToFavourites(idMacroZone: Int, idZone: Int) {
        viewModelScope.launch {
            zoningUseCase.addFavouriteZone(idMacroZone, idZone)
        }
    }

    fun removeZoneFromFavourites(idMacroZone: Int, idZone: Int) {
        viewModelScope.launch {
            zoningUseCase.removeFavouriteZone(idMacroZone, idZone)
        }
    }
}

class DestinationMapViewModel(
    val dispatchUseCase: ifac.td.taxi.domain.usecase.DispatchUseCase,
    val navigatorUseCase: NavigatorUseCase,
    context: android.app.Application,
) : BaseViewModel(context) {

    val TAG = "DestinationMapViewModel"

    val _navigatorFlow = MutableSharedFlow<Intent?>()
    val navigatorFlow = _navigatorFlow.asSharedFlow()

    fun openNavigatorApp(coordinates: String, street: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            Logs.d(TAG, "openNavigatorApp: Coordinates = $coordinates, Street = $street")

            val navigatorIntent = when {
                coordinates.isNotEmpty() && !street.isNullOrEmpty() -> {
                    Logs.d(TAG, "openNavigatorApp: Opening navigator with coordinates ($coordinates) and street: $street")
                    navigatorUseCase.openNavigatorWithCoordinatesAndStreetName(coordinates, street)
                }
                coordinates.isNotEmpty() -> {
                    Logs.d(TAG, "openNavigatorApp: Opening navigator with coordinates ($coordinates)")
                    navigatorUseCase.openNavigatorWithCoordinates(coordinates)
                }
                !street.isNullOrEmpty() -> {
                    Logs.d(TAG, "openNavigatorApp: Opening navigator with street: $street")
                    navigatorUseCase.openNavigatorApp(street)
                }
                else -> {
                    Logs.d(TAG, "openNavigatorApp: Opening navigator without specific parameters")
                    navigatorUseCase.openNavigatorApp()
                }
            }

            _navigatorFlow.emit(navigatorIntent)
        }
    }
}

If you want, I can do a third pass and split this into:
- a **real production-ready file** with only the ZoningCars feature,
- or a **cleaner compile-safe version** that removes the extra example viewmodels and keeps only the requested screen.
