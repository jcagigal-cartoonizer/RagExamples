package ifac.td.taxi.compose.viewmodel
import ifac.td.taxi.ui.screen.components.PreReservationTripsUiEvent
import ifac.td.taxi.ui.screen.components.PreReservationTripsButtonState
import ifac.td.taxi.ui.screen.components.PreReservationTripsButtonsState
import ifac.td.taxi.ui.screen.components.PreReservationTripsUiState
import ifac.td.taxi.ui.screen.components.PreReservationTripsUiEffect
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 91-3: import android.app.Application
class PreReservationTripsComposeViewModel(
    application: Application,
    private val preReservationUseCase: PreReservationUseCase,
    private val bravoCentralUseCase: BravoCentralUseCase,
    private val userPreferencesUseCase: UserPreferencesUseCase,
) : AndroidViewModel(application) {
    private val TAG = "PreReservationTripsVM"
    private val _uiState = MutableStateFlow(PreReservationTripsUiState())
    val uiState: StateFlow<PreReservationTripsUiState> = _uiState.asStateFlow()
    private val _uiEffects = MutableSharedFlow<PreReservationTripsUiEffect>(
        extraBufferCapacity = 1
    )
    val uiEffects: SharedFlow<PreReservationTripsUiEffect> = _uiEffects.asSharedFlow()
    private val _trips = MutableStateFlow<List<Prereservation>>(emptyList())
    private var refreshJob: Job? = null
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable
    init {
        observeTrips()
        startPolling()
        observeBidResults()
    }
    fun onEvent(event: PreReservationTripsUiEvent) {
        when (event) {
            PreReservationTripsUiEvent.ScreenStarted -> Unit
            PreReservationTripsUiEvent.ScreenPaused -> stopPolling()
            is PreReservationTripsUiEvent.TripClicked -> handleTripClick(event.trip)
            PreReservationTripsUiEvent.DialogAcceptClicked -> Unit
            PreReservationTripsUiEvent.DialogCancelClicked -> {
                viewModelScope.launch {
                    _uiEffects.emit(PreReservationTripsUiEffect.DismissDialog)
                }
            }
        }
    }
    fun observeTrips() {
        viewModelScope.launch {
            _trips.collect { list ->
                _uiState.update { state ->
                    state.copy(
                        trips = list,
                        buttonsState = buildButtonsState(list)
                    )
                }
            }
        }
    }
    fun handleTripClick(trip: Prereservation) {
        viewModelScope.launch {
            when {
                trip.isAssignedToMyCar && trip.isTooLateToCancelAssignment -> {
                    _uiEffects.emit(
                        PreReservationTripsUiEffect.ShowToast(R.string.can_not_cancel_reservation)
                    )
                }
                trip.isAssignedToMyCar -> {
                    _uiEffects.emit(
                        PreReservationTripsUiEffect.ShowDialog(
                            PreReservationTripsDialogState(
                                title = getApplication<Application>().getString(R.string.dialog_desasignar_trip_title),
                                description = getApplication<Application>().getString(
                                    R.string.pickup_time_in_zone_placeholder_str,
                                    trip.pickupTime.getDateTimeFromISO8601(),
                                    trip.pickupZone
                                ),
                                acceptLabel = getApplication<Application>().getString(android.R.string.ok),
                                cancelLabel = getApplication<Application>().getString(android.R.string.cancel),
                                isDestructive = true
                            )
                        )
                    )
                }
                trip.isAvailable -> {
                    _uiEffects.emit(
                        PreReservationTripsUiEffect.ShowDialog(
                            PreReservationTripsDialogState(
                                title = getApplication<Application>().getString(R.string.dialog_asignar_trip_title),
                                description = getApplication<Application>().getString(
                                    R.string.pickup_time_in_zone_placeholder_str,
                                    trip.pickupTime.getDateTimeFromISO8601(),
                                    trip.pickupZone
                                ),
                                acceptLabel = getApplication<Application>().getString(android.R.string.ok),
                                cancelLabel = getApplication<Application>().getString(android.R.string.cancel),
                                isDestructive = false
                            )
                        )
                    )
                }
            }
        }
    }
    fun onDialogConfirmed(trip: Prereservation, remove: Boolean) {
        viewModelScope.launch {
            preReservationUseCase.bidPreReservation(trip, remove, MutableSharedFlow<Pair<Boolean, Boolean>>().also {
                // If your use case requires a flow, you can adapt here.
            })
        }
    }
    fun observeBidResults() {
        // If your original use case writes to _bidFlow, keep that flow here instead.
        // This is a template showing how to convert side effects into UiEffects.
    }
    fun startPolling() {
        if (::runnable.isInitialized) return
        runnable = object : Runnable {
            override fun run() {
                viewModelScope.launch {
                    preReservationUseCase.requestPreReservations(
                        MutableStateFlow<List<Prereservation>?>(null),
                        MutableSharedFlow<Boolean>()
                    )
                }
                handler.postDelayed(this, 30_000L)
            }
        }
        handler.post(runnable)
    }
    fun stopPolling() {
        if (::runnable.isInitialized) {
            handler.removeCallbacks(runnable)
        }
    }
    override fun onCleared() {
        stopPolling()
        super.onCleared()
    }
    fun buildButtonsState(list: List<Prereservation>): PreReservationTripsButtonsState {
        return PreReservationTripsButtonsState(
            assign = PreReservationTripsButtonState(
                textRes = R.string.dialog_asignar_trip_title,
                visibility = if (list.any { it.isAvailable }) PreReservationTripsButtonVisibility.Visible else PreReservationTripsButtonVisibility.Gone,
                kind = PreReservationTripsButtonKind.Primary,
                enabled = true
            ),
            cancel = PreReservationTripsButtonState(
                textRes = R.string.dialog_desasignar_trip_title,
                visibility = if (list.any { it.isAssignedToMyCar }) PreReservationTripsButtonVisibility.Visible else PreReservationTripsButtonVisibility.Gone,
                kind = PreReservationTripsButtonKind.Warning,
                enabled = true
            ),
            availableInfo = PreReservationTripsButtonState(
                textRes = R.string.prereservation,
                visibility = if (list.isNotEmpty()) PreReservationTripsButtonVisibility.Visible else PreReservationTripsButtonVisibility.Gone,
                kind = PreReservationTripsButtonKind.Secondary,
                enabled = false
            )
        )
    }
}
