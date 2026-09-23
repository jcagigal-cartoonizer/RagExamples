package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.MacroZoningUiEvent
import ifac.td.taxi.ui.screen.components.MacroZoningUiEffect
import ifac.td.taxi.ui.screen.components.MacroZoningCustomDialogState
import ifac.td.taxi.ui.screen.components.MacroZoningUiState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 200-2: import android.app.Application
class MacroZoningComposeViewModelCompose(
    private val zoningUseCase: ZoningUseCase,
    private val licensingUseCase: LicensingUseCase,
    private val shiftStatusUseCase: ShiftStatusUseCase,
    private val bravoConfigurationVariableDao: BravoConfigurationVariableDao,
    private val userPreferencesUseCase: UserPreferencesUseCase,
    private val pendingTripsUseCase: PendingTripsUseCase,
    application: Application,
) : AndroidViewModel(application) {
    private val handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null
    private val _uiState = MutableStateFlow(MacroZoningUiState())
    val uiState: StateFlow<MacroZoningUiState> = _uiState.asStateFlow()
    private val _uiEffects = MutableSharedFlow<MacroZoningUiEffect>()
    val uiEffects: SharedFlow<MacroZoningUiEffect> = _uiEffects.asSharedFlow()
    private var pendingServicesButton: Boolean? = null
    private var pendingServicesHiredButton: Boolean? = null
    private var hasZonesInFavourites: Boolean = false
    private var selectedZone: MacroZoneModelUi? = null
    init {
        viewModelScope.launch(Dispatchers.IO) {
            val config = bravoConfigurationVariableDao.getBravoConfigurationVariable()
            val showButton = config?.showPreassignedTrips?.toBoolean1or0() == true
            val visible = showButton && DateUtils.isLaterThan(config?.preassignTripsButtonStartTime)
            _uiState.update { it.copy(buttonsState = it.buttonsState.copy(preReservationButton = it.buttonsState.preReservationButton.copy(visible = visible, enabled = visible))) }
        }
        viewModelScope.launch(Dispatchers.IO) {
            initZonesConfiguration(zoningUseCase.getZoneConfiguration())
            checkHasZonesInFavourites()
            checkPendingServiceOnForHirePermission()
            checkPendingServiceOnHiredPermission()
        }
    }
    fun onEvent(event: MacroZoningUiEvent) {
        when (event) {
            is MacroZoningUiEvent.OnResume -> initViewModelData()
            is MacroZoningUiEvent.OnPause -> removeHandlerCallback()
            is MacroZoningUiEvent.OnOrderSelected -> onHeaderClick(event.order)
            is MacroZoningUiEvent.OnFilterClick -> onFilterClick()
            is MacroZoningUiEvent.OnPendingClick -> canOpenPendingTripsFragment()
            is MacroZoningUiEvent.OnPreReservationClick -> emitEffect(MacroZoningUiEffect.NavigateToPreReservationTrips)
            is MacroZoningUiEvent.OnMacroZoneClick -> openMacroZone(event.macroZoneId)
            is MacroZoningUiEvent.OnDialogDismiss -> _uiState.update { it.copy(dialogState = null) }
            is MacroZoningUiEvent.OnDialogConfirm -> _uiState.update { it.copy(dialogState = null) }
        }
    }
    fun initViewModelData() {
        removeHandlerCallback()
        viewModelScope.launch(Dispatchers.IO) {
            val refreshRate = licensingUseCase.getLicensingParameters()?.segRefrescoConsUbicacion ?: return@launch
            val millis = refreshRate * 1000L
            var lastUpdate = zoningUseCase.getLastZoningUpdate()
            runnable = object : Runnable {
                override fun run() {
                    viewModelScope.launch {
                        val elapsed = System.currentTimeMillis() - lastUpdate
                        val progress = (1000 - ((elapsed.toFloat() / millis) * 1000).toInt()).coerceIn(0, 1000)
                        _uiState.update { it.copy(refreshProgress = progress) }
                        if (elapsed > millis) {
                            zoningUseCase.getMacroZonesInformation(
                                W2CLocation.getLastIdMacrozone(),
                                false
                            ) { mz, availableColumns ->
                                viewModelScope.launch(Dispatchers.IO) {
                                    if (availableColumns.isNotBlank()) {
                                        _uiState.update { it.copy(dataTypes = availableColumns) }
                                    }
                                    val macroZones = mz.map { it.copyValues() }
                                    val models = macroZones.map { MacroZoneModelUi.from(it) }
                                    sortNewData(models)
                                }
                            }
                            lastUpdate = System.currentTimeMillis()
                            _uiState.update { it.copy(refreshProgress = 1000) }
                        }
                    }
                    handler.postDelayed(this, 1000)
                }
            }
            handler.post(runnable!!)
        }
    }
    fun sortNewData(list: List<MacroZoneModelUi>) {
        val order = uiState.value.listOrder
        sortListBy(list, order)
    }
    fun sortListBy(list: List<MacroZoneModelUi>, order: OrderOptions) {
        val sorted = when (order) {
            OrderOptions.NONE, OrderOptions.ID -> list.sortedBy { it.idMacrozone }
            OrderOptions.NAME -> list.sortedBy { it.name }
            OrderOptions.IN_STAND -> list.sortedByDescending { it.carsStop }
            OrderOptions.IN_ZONE -> list.sortedByDescending { it.carsZone }
            OrderOptions.HIRED -> list.sortedByDescending { it.occupiedCars }
            OrderOptions.TRIPS -> list.sortedByDescending { it.services }
            else -> list
        }
        selectedZone = sorted.firstOrNull { it.idMacrozone == W2CLocation.getLastIdMacrozone() }
        _uiState.update {
            it.copy(
                macroZones = sorted,
                selectedZoneId = selectedZone?.idMacrozone,
                scrollMode = if (order != OrderOptions.NONE) ScrollModeEnum.JUMP_TO_TOP else it.scrollMode
            )
        }
    }
    fun onHeaderClick(newOrder: OrderOptions) {
        val current = uiState.value.listOrder
        val target = if (current == newOrder) OrderOptions.NONE else newOrder
        viewModelScope.launch { zoningUseCase.updateZoningOrderConfiguration(target) }
        _uiState.update { it.copy(listOrder = target, scrollMode = if (target != OrderOptions.NONE) ScrollModeEnum.JUMP_TO_TOP else it.scrollMode) }
        uiState.value.macroZones.let { sortListBy(it, target) }
    }
    fun onFilterClick() {
        _uiState.update {
            it.copy(
                dialogState = MacroZoningCustomDialogState(
                    visible = true,
                    title = "Filter",
                    description = "Open filter bottom sheet",
                    buttons = listOf(MacroZoningDialogButtonSpec.Accept)
                )
            )
        }
        emitEffect(MacroZoningUiEffect.ShowDialog(_uiState.value.dialogState!!))
    }
    fun openMacroZone(id: Int) {
        emitEffect(MacroZoningUiEffect.NavigateToZoning(id))
    }
    fun canOpenPendingTripsFragment() {
        viewModelScope.launch {
            userPreferencesUseCase.getUserPreferences()?.let {
                if (it.closePendingTripsIfEmpty) {
                    if (BravoCentral.isPendingTripsAllowed(false)) {
                        pendingTripsUseCase.getPendingTripsZone(
                            MutableStateFlow<ArrayList<PendingTrip>?>(null)
                        ) { _, _ -> }
                        emitEffect(MacroZoningUiEffect.NavigateToPendingTrips)
                    } else {
                        emitEffect(MacroZoningUiEffect.ShowToast(getApplication<Application>().getString(R.string.toast_no_hay_pendientes)))
                    }
                } else {
                    emitEffect(MacroZoningUiEffect.NavigateToPendingTrips)
                }
            } ?: emitEffect(MacroZoningUiEffect.NavigateToPendingTrips)
        }
    }
    fun checkPendingServiceOnForHirePermission() {
        viewModelScope.launch {
            val parameters = licensingUseCase.getLicensingParameters()
            pendingServicesButton = parameters?.isVacantPendingServices
            updateButtonsState()
        }
    }
    fun checkPendingServiceOnHiredPermission() {
        viewModelScope.launch {
            val status = shiftStatusUseCase.getStatus()?.currentStatus
            if (status != null && shiftStatusUseCase.isHired(status)) {
                val parameters = licensingUseCase.getLicensingParameters()
                pendingServicesHiredButton = parameters?.isPendingOnHired
                updateButtonsState()
            }
        }
    }
    fun checkHasZonesInFavourites() {
        viewModelScope.launch { hasZonesInFavourites = zoningUseCase.getFavouriteZoneList()?.isNotEmpty() == true }
    }
    fun updateButtonsState() {
        val shortBreak = uiState.value.shortBreakStatus
        val zone = uiState.value.currentZone
        val pendingEnabled = computePendingEnabled(shortBreak, zone)
        _uiState.update { state ->
            state.copy(
                buttonsState = state.buttonsState.copy(
                    pendingButton = state.buttonsState.pendingButton.copy(enabled = pendingEnabled),
                    preReservationButton = state.buttonsState.preReservationButton
                )
            )
        }
    }
    fun computePendingEnabled(shortBreak: ShortBreakStatus?, zone: String?): Boolean {
        if (zone.isNullOrEmpty()) return false
        if (shortBreak == ShortBreakStatus.IN_SHORT_BREAK || shortBreak == ShortBreakStatus.IN_SHORT_BREAK_FORCED) return false
        if (!W2CLocation.isLocationAllowedByCentral()) return false
        if (W2CLocation.getIsInSoonInZone()) return true
        pendingServicesHiredButton?.let { hired ->
            if (!hired) return false
            return uiState.value.hiredZone != null
        }
        if (pendingServicesButton == true) return true
        return false
    }
    fun emitEffect(effect: MacroZoningUiEffect) {
        viewModelScope.launch { _uiEffects.emit(effect) }
    }
    fun removeHandlerCallback() {
        runnable?.let { handler.removeCallbacks(it) }
    }
    private suspend fun initZonesConfiguration(zoneConfiguration: ZoningConfigurationEntity) {
        _uiState.update { it.copy(listOrder = zoneConfiguration.zoneOrder) }
    }
    override fun onCleared() {
        removeHandlerCallback()
        super.onCleared()
    }
}
