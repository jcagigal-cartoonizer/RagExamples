package ifac.td.taxi.compose.viewmodel
import ifac.td.taxi.ui.screen.components.ZoningServicesUiEvent
import ifac.td.taxi.ui.screen.components.ZoningServicesUiState
import ifac.td.taxi.ui.screen.components.ZoningServicesUiEffect
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 140-2: import android.app.Application
class ZoningServicesComposeViewModel(
    private val zoningUseCase: ZoningUseCase,
    private val licensingUseCase: LicensingUseCase,
    private val shiftStatusUseCase: ShiftStatusUseCase,
    private val bravoConfigurationVariableDao: BravoConfigurationVariableDao,
    application: Application,
) : AndroidViewModel(application) {
    private val handler = Handler(Looper.getMainLooper())
    private var zonesCarsRunnable: Runnable? = null
    private var refreshJob: Job? = null
    private var idMacroZone: Int = 0
    private var idZone: Int = 0
    private var showAllTrips: Boolean = false
    private var companyNumber: String? = null
    private val _uiState = MutableStateFlow(ZoningServicesUiState())
    val uiState: StateFlow<ZoningServicesUiState> = _uiState.asStateFlow()
    private val _effects = MutableSharedFlow<ZoningServicesUiEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<ZoningServicesUiEffect> = _effects.asSharedFlow()
    init {
        viewModelScope.launch(Dispatchers.IO) {
            companyNumber = bravoConfigurationVariableDao.getBravoConfigurationVariable()?.companyNumber
            val requestAll = bravoConfigurationVariableDao.getBravoConfigurationVariable()?.requestAllServices ?: false
            _uiState.update { it.copy(buttonsState = it.buttonsState.copy(showAllVisible = requestAll)) }
        }
    }
    fun onEvent(event: ZoningServicesUiEvent) {
        when (event) {
            is ZoningServicesUiEvent.Init -> initViewModelData(event.idMacroZone, event.idZone)
            ZoningServicesUiEvent.ShowAllClicked -> {
                showAllTrips = true
                loadTrips()
                _uiState.update { it.copy(buttonsState = it.buttonsState.withShowAllHidden()) }
            }
            ZoningServicesUiEvent.ShowRecentClicked -> {
                showAllTrips = false
                loadTrips()
                _uiState.update { it.copy(buttonsState = it.buttonsState.withShowRecentHidden()) }
            }
            ZoningServicesUiEvent.CancelClicked -> {
                viewModelScope.launch {
                    _effects.emit(ZoningServicesUiEffect.NavigateBack)
                }
            }
            ZoningServicesUiEvent.CloseClicked -> {
                val currentStatus = _uiState.value.currentShiftStatus
                val shouldGoToOnTrip = isHiredState(currentStatus)
                viewModelScope.launch {
                    if (shouldGoToOnTrip && _uiState.value.tripId != null) {
                        _effects.emit(ZoningServicesUiEffect.NavigateToHome)
                        _effects.emit(ZoningServicesUiEffect.NavigateToOnTrip)
                    } else {
                        _effects.emit(ZoningServicesUiEffect.NavigateToHome)
                    }
                }
            }
            ZoningServicesUiEvent.ShowCloseDialog -> {
                _uiState.update { it.copy(dialogState = it.dialogState.copy(visible = true)) }
            }
            ZoningServicesUiEvent.HideCloseDialog,
            ZoningServicesUiEvent.DismissDialog -> {
                _uiState.update { it.copy(dialogState = it.dialogState.copy(visible = false)) }
            }
            ZoningServicesUiEvent.ConfirmDialog -> {
                _uiState.update { it.copy(dialogState = it.dialogState.copy(visible = false)) }
                viewModelScope.launch { _effects.emit(ZoningServicesUiEffect.NavigateToHome) }
            }
        }
    }
    fun initViewModelData(idMacroZone: Int, idZone: Int) {
        this.idMacroZone = idMacroZone
        this.idZone = idZone
        removeHandlerCallbacks()
        viewModelScope.launch {
            val refreshRate = licensingUseCase.getLicensingParameters()?.segRefrescoConsUbicacion ?: return@launch
            _uiState.update { it.copy(zoneLoading = true) }
            zoningUseCase.getServicesInformation(
                idMacroZone,
                idZone,
                showAllTrips,
                bravoConfigurationVariableDao.getBravoConfigurationVariable()?.showCarsInTripRange,
                callback = ::onTripsReceived
            )
            _uiState.update { it.copy(refreshProgress = 1000, showLoading = false) }
            var lastUpdate = System.currentTimeMillis()
            val millis = refreshRate * 1000L
            zonesCarsRunnable = object : Runnable {
                override fun run() {
                    viewModelScope.launch {
                        val elapsed = System.currentTimeMillis() - lastUpdate
                        val progress = 1000 - ((elapsed.toFloat() / millis) * 1000).toInt().coerceIn(0, 1000)
                        _uiState.update { it.copy(refreshProgress = progress) }
                        if (elapsed >= millis) {
                            zoningUseCase.getServicesInformation(
                                idMacroZone,
                                idZone,
                                showAllTrips,
                                bravoConfigurationVariableDao.getBravoConfigurationVariable()?.showCarsInTripRange,
                                callback = ::onTripsReceived
                            )
                            lastUpdate = System.currentTimeMillis()
                            _uiState.update { it.copy(refreshProgress = 1000) }
                        }
                    }
                    handler.postDelayed(this, 1000)
                }
            }
            handler.post(zonesCarsRunnable!!)
        }
    }
    fun loadTrips() {
        viewModelScope.launch {
            zoningUseCase.getServicesInformation(
                idMacroZone,
                idZone,
                showAllTrips,
                bravoConfigurationVariableDao.getBravoConfigurationVariable()?.showCarsInTripRange,
                callback = ::onTripsReceived
            )
        }
    }
    fun onTripsReceived(trips: List<ZoneTrip>?) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    zoneTrips = trips?.map { trip ->
                        ZoneTripModel(
                            id = trip.tripID,
                            pickupTime = trip.pickupTime,
                            company = trip.company,
                            passengerNumber = trip.passengerNumber,
                            baggageNumber = trip.baggageNumber,
                            vehicleRequirements = trip.vehicleRequirements,
                            driverRequirements = trip.driverRequirements,
                            maxDistanceZoneService = trip.maxDistanceZoneService,
                            ownNearbyVehicles = trip.ownNearbyVehicles,
                            totalNearbyVehicles = trip.totalNearbyVehicles,
                            myOrder = trip.myOrder
                        )
                    } ?: emptyList(),
                    zoneLoading = false
                )
            }
        }
    }
    fun removeHandlerCallbacks() {
        zonesCarsRunnable?.let { handler.removeCallbacks(it) }
    }
    fun isHiredState(state: Int?): Boolean {
        return when (state) {
            null -> false
            else -> shiftStatusUseCase.isHired(state)
        }
    }
    override fun onCleared() {
        removeHandlerCallbacks()
        super.onCleared()
    }
}
