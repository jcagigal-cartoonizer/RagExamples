package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.PendingTripsUiState
import ifac.td.taxi.ui.screen.components.PendingTripsUiEffect
import ifac.td.taxi.compose.viewmodel.PendingTripsComposeViewModel
import ifac.td.taxi.ui.screen.components.PendingTripsUiEvent
import ifac.td.taxi.ui.screen.components.PendingTripsButtonsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 158-2: import android.app.Application
data class PendingTripsUiState(
    val isLoading: Boolean = false,
    val pendingTrips: List<PendingTrip> = emptyList(),
    val showDialogOnAccept: Boolean = true,
    val buttonsState: PendingTripsButtonsState = PendingTripsButtonsState.default()
)
sealed interface PendingTripsUiEvent {
    data object ScreenStarted : PendingTripsUiEvent
    data object ScreenStopped : PendingTripsUiEvent
    data object RefreshRequested : PendingTripsUiEvent
    data class TripClicked(val tripId: String) : PendingTripsUiEvent
    data class ConfirmTrip(val trip: PendingTrip) : PendingTripsUiEvent
    data object DismissDialog : PendingTripsUiEvent
}
sealed interface PendingTripsUiEffect {
    data object NavigateBack : PendingTripsUiEffect
    data object CloseIfEmpty : PendingTripsUiEffect
    data class ShowToast(val messageRes: Int) : PendingTripsUiEffect
    data class OpenConfirmDialog(
        val title: String,
        val description: String,
        val pendingTrip: PendingTrip
    ) : PendingTripsUiEffect
}
class PendingTripsComposeViewModel(
    private val pendingTripsUseCase: PendingTripsUseCase,
    private val bravoCentralUseCase: BravoCentralUseCase,
    private val userPreferencesUseCase: UserPreferencesUseCase,
    context: Application,
) : BaseViewModel(context) {
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable
    private val _uiState = MutableStateFlow(PendingTripsUiState())
    val uiState: StateFlow<PendingTripsUiState> = _uiState.asStateFlow()
    private val _effects = MutableSharedFlow<PendingTripsUiEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<PendingTripsUiEffect> = _effects.asSharedFlow()
    private val _pendingTripsFlow = MutableStateFlow<ArrayList<PendingTrip>?>(null)
    fun onScreenStarted() {
        checkAcceptDialogPermission()
        loadTrips(forced = true)
    }
    fun onScreenStopped() {
        removeHandlerCallback()
    }
    fun onUiEvent(event: PendingTripsUiEvent) {
        when (event) {
            PendingTripsUiEvent.ScreenStarted -> onScreenStarted()
            PendingTripsUiEvent.ScreenStopped -> onScreenStopped()
            PendingTripsUiEvent.RefreshRequested -> loadTrips(forced = true)
            is PendingTripsUiEvent.TripClicked -> {
                val trip = _uiState.value.pendingTrips.firstOrNull { it.tripID == event.tripId }
                if (trip != null) onTripSelected(trip)
            }
            is PendingTripsUiEvent.ConfirmTrip -> confirmAndRequestTrip(event.trip)
            PendingTripsUiEvent.DismissDialog -> Unit
        }
    }
    fun onTripSelected(trip: PendingTrip) {
        if (_uiState.value.showDialogOnAccept) {
            viewModelScope.launch {
                _effects.emit(
                    PendingTripsUiEffect.OpenConfirmDialog(
                        title = "Auction confirm",
                        description = if (trip.pickupAddress.isNullOrBlank()) {
                            "Confirm auction trip?"
                        } else {
                            "Confirm auction trip: ${trip.pickupAddress}"
                        },
                        pendingTrip = trip
                    )
                )
            }
        } else {
            requestTrip(trip)
            emitToastAndNavigateBack()
        }
    }
    fun confirmAndRequestTrip(trip: PendingTrip) {
        requestTrip(trip)
        emitToastAndNavigateBack()
    }
    fun emitToastAndNavigateBack() {
        viewModelScope.launch {
            _effects.emit(PendingTripsUiEffect.ShowToast(ifac.td.taxi.R.string.strDatosTransmitidos))
            _effects.emit(PendingTripsUiEffect.NavigateBack)
        }
    }
    fun loadTrips(forced: Boolean = false) {
        if (BravoCentral.isPendingTripsAllowed(false) || forced) {
            refreshPendingTripsManual(forced)
        }
        startChronometer()
    }
    fun refreshPendingTripsManual(forced: Boolean = false) {
        viewModelScope.launch {
            pendingTripsUseCase.getPendingTripsZone(
                flow = _pendingTripsFlow,
                forced = forced
            )
            _uiState.update {
                it.copy(pendingTrips = _pendingTripsFlow.value.orEmpty())
            }
            checkClosePendingIfEmpty()
        }
    }
    fun startChronometer() {
        viewModelScope.launch(Dispatchers.IO) {
            val refreshRate = 2000L
            val initRefreshRate = 500L
            runnable = object : Runnable {
                override fun run() {
                    viewModelScope.launch {
                        if (BravoCentral.isPendingTripsAllowed(true)) {
                            pendingTripsUseCase.getPendingTripsZone(flow = _pendingTripsFlow)
                            _uiState.update {
                                it.copy(pendingTrips = _pendingTripsFlow.value.orEmpty())
                            }
                        }
                    }
                    handler.postDelayed(this, refreshRate)
                }
            }
            handler.postDelayed(runnable, initRefreshRate)
        }
    }
    fun removeHandlerCallback() {
        if (::runnable.isInitialized) handler.removeCallbacks(runnable)
    }
    fun requestTrip(trip: PendingTrip) {
        bravoCentralUseCase.requestPendingTrip(trip)
    }
    fun checkAcceptDialogPermission() {
        viewModelScope.launch {
            val preferences = userPreferencesUseCase.getUserPreferences()
            val showDialog = preferences?.showConfAcceptDispatch ?: true
            _uiState.update { it.copy(showDialogOnAccept = showDialog) }
        }
    }
    fun checkClosePendingIfEmpty() {
        viewModelScope.launch {
            userPreferencesUseCase.getUserPreferences()?.let {
                if (it.closePendingTripsIfEmpty && _uiState.value.pendingTrips.isEmpty()) {
                    _effects.emit(PendingTripsUiEffect.CloseIfEmpty)
                    _effects.emit(PendingTripsUiEffect.NavigateBack)
                }
            }
        }
    }
    fun onTripClicked(tripId: String) {
        val trip = _uiState.value.pendingTrips.firstOrNull { it.tripID == tripId }
        trip?.let { onTripSelected(it) }
    }
}
