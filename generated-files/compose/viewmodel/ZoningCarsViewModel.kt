package ifac.td.taxi.compose.viewmodel
import ifac.td.taxi.ui.screen.components.ZoningCarsUiState
import ifac.td.taxi.ui.screen.components.ZoningCarsUiEvent
import ifac.td.taxi.ui.screen.components.ZoningCarsButtonsState
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 177-3: import android.app.Application
class ZoningCarsComposeViewModel(
    private val zoningUseCase: ZoningUseCase,
    private val licensingUseCase: LicensingUseCase,
    private val locationUseCase: LocationUseCase,
    private val shiftStatusUseCase: ShiftStatusUseCase,
    private val bravoConfigurationVariableDao: BravoConfigurationVariableDao,
    private val userPreferencesUseCase: UserPreferencesUseCase,
    private val pendingTripsUseCase: PendingTripsUseCase,
    private val sharedState: MainActivityViewModel,
    context: Application,
) : BaseViewModel(context) {
    private val _uiState = MutableStateFlow(ZoningCarsUiState())
    val uiState = _uiState.asStateFlow()
    private val _buttonsState = MutableStateFlow(ZoningCarsButtonsState())
    val buttonsState = _buttonsState.asStateFlow()
    private val _dialogState = MutableStateFlow<ZoningCarsDialogState?>(null)
    val dialogState = _dialogState.asStateFlow()
    private val _effects = MutableSharedFlow<ZoningCarsEffect>(extraBufferCapacity = 1)
    val effects = _effects.asSharedFlow()
    private var pendingServicesButton: Boolean? = null
    private var pendingServicesHiredButton: Boolean? = null
    private var bravoConfiguration: BravoConfigurationVariableEntity? = null
    private val carsCallback: (List<ZoneCarModel>) -> Unit = { cars ->
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {
                it.copy(
                    carRows = getCarListByType(cars),
                    isLoadingCars = false
                )
            }
        }
    }
    fun onEvent(event: ZoningCarsUiEvent) {
        when (event) {
            is ZoningCarsUiEvent.Init -> initViewModelData(event.idMacroZone, event.idZone)
            ZoningCarsUiEvent.BackClicked -> viewModelScope.launch {
                sharedState.emitManualZoningNavigation(false)
                _effects.emit(ZoningCarsEffect.NavigateBack)
            }
            ZoningCarsUiEvent.CloseClicked -> handleClose()
            ZoningCarsUiEvent.PendingClicked -> canOpenPendingTripsFragment(sharedState._pendingTripsListFlow)
            ZoningCarsUiEvent.ShowLocateOnHiredDialog -> {
                val zone = currentZoneOrNull() ?: return
                _dialogState.value = ZoningCarsDialogState.locate(zone)
            }
            ZoningCarsUiEvent.ShowDelocateOnHiredDialog -> {
                _dialogState.value = ZoningCarsDialogState.delocate(
                    title = sharedState.hiredZone.value?.nombreZone.orEmpty()
                )
            }
            ZoningCarsUiEvent.DismissDialog -> _dialogState.value = null
            is ZoningCarsUiEvent.DialogButtonClicked -> handleDialogButton(event.buttonId)
        }
    }
    fun handleClose() {
        val status = sharedState.shiftStatusFlow.value?.currentStatus
        when (status) {
            ifConstants.STATE_HIRED,
            ifConstants.STATE_DISPATCHED,
            ifConstants.STATE_HIRED_DISPATCHED,
            ifConstants.STATE_HIRED_NO_CENTRAL -> viewModelScope.launch {
                _effects.emit(ZoningCarsEffect.NavigateToHome)
                _effects.emit(ZoningCarsEffect.NavigateToOnTrip)
            }
            else -> viewModelScope.launch {
                _effects.emit(ZoningCarsEffect.NavigateToHome)
            }
        }
    }
    fun handleDialogButton(buttonId: String) {
        val zone = currentZoneOrNull() ?: return
        when (buttonId) {
            ButtonType.ACCEPT.name -> {
                if (_dialogState.value?.kind == ZoningCarsDialogKind.LocateOnHired) {
                    locateOnHired(zone)
                } else {
                    delocateOnHired()
                }
                _dialogState.value = null
            }
            ButtonType.POI.name -> {
                _dialogState.value = null
                // Preserve navigation behavior
                viewModelScope.launch { _effects.emit(ZoningCarsEffect.NavigateToPointsOfInterest) }
            }
            else -> _dialogState.value = null
        }
    }
    fun initViewModelData(idMacroZone: Int, idZone: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingCars = true, idMacroZone = idMacroZone, idZone = idZone) }
            val zoneName = W2CLocation.getZoning().getMacrozoneById(idMacroZone).getZoneById(idZone).nombreZone
            _uiState.update { it.copy(zoneName = zoneName) }
            val refreshRate = licensingUseCase.getLicensingParameters()?.segRefrescoConsUbicacion
            refreshRate?.let { seconds ->
                zoningUseCase.getCarsInformation(idMacroZone, idZone, carsCallback)
            }
            getPOIsValue()
            checkPendingServiceOnForHirePermission()
            checkPendingServiceOnHiredPermission()
            recomputeButtons()
        }
    }
    fun removeHandlerCallbacks() {}
    fun checkPendingServiceOnHiredPermission() {
        viewModelScope.launch {
            val shiftStatus = shiftStatusUseCase.getStatus()?.currentStatus
            if (shiftStatus != null && shiftStatusUseCase.isHired(shiftStatus)) {
                _buttonsState.update {
                    it.copy(pendingServicesHiredButtonAllowed = licensingUseCase.getLicensingParameters()?.isPendingOnHired)
                }
                pendingServicesHiredButton = licensingUseCase.getLicensingParameters()?.isPendingOnHired
                recomputeButtons()
            }
        }
    }
    fun checkPendingServiceOnForHirePermission() {
        viewModelScope.launch {
            val shiftStatus = shiftStatusUseCase.getStatus()?.currentStatus
            if (shiftStatus != null && shiftStatusUseCase.isVacant(shiftStatus)) {
                _buttonsState.update {
                    it.copy(pendingServicesButtonAllowed = licensingUseCase.getLicensingParameters()?.isVacantPendingServices)
                }
                pendingServicesButton = licensingUseCase.getLicensingParameters()?.isVacantPendingServices
                recomputeButtons()
            }
        }
    }
    fun getPOIsValue() {
        viewModelScope.launch { bravoConfiguration = bravoConfigurationVariableDao.getBravoConfigurationVariable() }
    }
    fun canOpenPendingTripsFragment(pendingTripsListFlow: MutableStateFlow<ArrayList<com.interfacom.sdk.taximeter.bravocomm.rest.pending_trips.response.PendingTrip>?>) {
        viewModelScope.launch {
            userPreferencesUseCase.getUserPreferences()?.let {
                if (it.closePendingTripsIfEmpty && !BravoCentral.isPendingTripsAllowed(false)) {
                    _effects.emit(ZoningCarsEffect.ShowToast(R.string.toast_no_hay_pendientes))
                } else {
                    _effects.emit(ZoningCarsEffect.NavigateToPendingTrips)
                }
            } ?: _effects.emit(ZoningCarsEffect.NavigateToPendingTrips)
        }
    }
    fun locateOnHired(zone: Zone) {
        viewModelScope.launch(Dispatchers.IO) {
            locationUseCase.locateOnHired(0, zone.idZone)
            _effects.emit(ZoningCarsEffect.NavigateToHome)
            _effects.emit(ZoningCarsEffect.NavigateToOnTrip)
        }
    }
    fun delocateOnHired() {
        viewModelScope.launch(Dispatchers.IO) {
            locationUseCase.delocateOnHired()
        }
    }
    fun currentZoneOrNull(): Zone? {
        val idMacroZone = _uiState.value.idMacroZone ?: return null
        val idZone = _uiState.value.idZone ?: return null
        return W2CLocation.getZoning().getMacrozoneById(idMacroZone).getZoneById(idZone)
    }
    fun recomputeButtons() {
        _buttonsState.update { old ->
            old.copy(
                isPendingVisible = true,
                pendingButtonStyle = resolvePendingButtonStyle(),
                pendingButtonColor = resolvePendingButtonColor(),
                backButtonColor = CustomButtonColor.BLUE,
                closeButtonColor = CustomButtonColor.BLUE,
            )
        }
    }
    fun resolvePendingButtonStyle(): CustomButtonStyle {
        val zone = sharedState.zoneFlow.value
        val isInShortBreak =
            sharedState.shortBreakStatus.value == ShortBreakStatus.IN_SHORT_BREAK ||
            sharedState.shortBreakStatus.value == ShortBreakStatus.IN_SHORT_BREAK_FORCED
        if (zone.isNullOrEmpty()) return CustomButtonStyle.DISABLE
        if (isInShortBreak) return CustomButtonStyle.DISABLE
        if (!W2CLocation.isLocationAllowedByCentral()) return CustomButtonStyle.DISABLE
        if (W2CLocation.getIsInSoonInZone()) return CustomButtonStyle.ENABLE
        if (pendingServicesHiredButton != null) {
            return if (pendingServicesHiredButton == true && sharedState.hiredZone.value != null) {
                CustomButtonStyle.ENABLE
            } else {
                CustomButtonStyle.DISABLE
            }
        }
        return if (pendingServicesButton == true) CustomButtonStyle.ENABLE else CustomButtonStyle.DISABLE
    }
    fun resolvePendingButtonColor(): CustomButtonBackgroundColor {
        return if (sharedState.orangeBtnPendingFlow.value == true) {
            CustomButtonBackgroundColor.ORANGE
        } else {
            CustomButtonBackgroundColor.BLUE
        }
    }
    fun handlePendingButtonStateFromFlows() {
        recomputeButtons()
    }
    fun getCarListByType(list: List<ZoneCarModel>): List<ZoneCarRowModel> {
        val carsByType = list.groupBy { it.status }
        val maxCarTypesInRow = listOf(
            carsByType[ifac.td.taxi.ui.model.LocationEnum.HIRED]?.size ?: 0,
            carsByType[ifac.td.taxi.ui.model.LocationEnum.ZONE]?.size ?: 0,
            carsByType[ifac.td.taxi.ui.model.LocationEnum.STOP]?.size ?: 0
        ).maxOrNull() ?: 0
        return buildList {
            for (i in 0 until maxCarTypesInRow) {
                add(
                    ZoneCarRowModel(
                        index = size + 1,
                        carHired = carsByType[ifac.td.taxi.ui.model.LocationEnum.HIRED]?.getOrNull(i),
                        carRank = carsByType[ifac.td.taxi.ui.model.LocationEnum.STOP]?.getOrNull(i),
                        carZone = carsByType[ifac.td.taxi.ui.model.LocationEnum.ZONE]?.getOrNull(i)
                    )
                )
            }
        }
    }
}
