package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.TripHistoryUiEffect
import ifac.td.taxi.ui.screen.components.TripHistoryUiState
import ifac.td.taxi.ui.screen.components.TripHistoryButtonsState
import ifac.td.taxi.compose.viewmodel.TripHistoryComposeViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 64-2: import android.app.Application
data class TripHistoryUiState(
    val trips: List<Trip> = emptyList(),
    val isLoading: Boolean = false,
    val isLastPage: Boolean = false,
    val page: Int = 0,
    val selectedTrips: Set<Long> = emptySet(),
    val dialog: TripHistoryDialogState? = null,
) {
    val hasSelection: Boolean get() = selectedTrips.isNotEmpty()
}
data class TripHistoryDialogState(
    val title: String,
    val description: String,
    val trip: Trip? = null,
    val showAccept: Boolean = true,
)
sealed interface TripHistoryUiEffect {
    data object NavigateBackToReceiptHistory : TripHistoryUiEffect
    data class NavigateToReceiptHistory(val tripId: Long) : TripHistoryUiEffect
    data class ShowNoTicketDialog(val trip: Trip) : TripHistoryUiEffect
    data object HideDialog : TripHistoryUiEffect
    data class ToastMessage(val messageRes: Int) : TripHistoryUiEffect
}
class TripHistoryComposeViewModel(
    application: Application,
    private val tripUseCase: TripUseCase,
    private val printerUseCase: PrinterUseCase,
    private val portugalUseCase: PortugalUseCase,
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(TripHistoryUiState())
    val uiState: StateFlow<TripHistoryUiState> = _uiState.asStateFlow()
    private val _effects = MutableSharedFlow<TripHistoryUiEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<TripHistoryUiEffect> = _effects.asSharedFlow()
    private val _buttonsState = MutableStateFlow(TripHistoryButtonsState())
    val buttonsState: StateFlow<TripHistoryButtonsState> = _buttonsState.asStateFlow()
    private val pageSize = 25
    init {
        refreshButtons()
        loadTrips(0)
    }
    fun loadNextPage() {
        val state = _uiState.value
        if (state.isLoading || state.isLastPage) return
        loadTrips(state.page + 1)
    }
    fun loadTrips(page: Int) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                refreshButtons()
                val loadedItems = page * pageSize
                val newTrips = tripUseCase.getAllTripsFinishedPaged(loadedItems)
                val currentTrips = _uiState.value.trips
                val isLastPage = newTrips.size < pageSize
                val combined = (currentTrips + newTrips).distinctBy { it.id }
                _uiState.update {
                    it.copy(
                        trips = combined,
                        isLoading = false,
                        isLastPage = isLastPage,
                        page = page,
                    )
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            } finally {
                refreshButtons()
            }
        }
    }
    fun onBackClicked() {
        viewModelScope.launch {
            _effects.emit(TripHistoryUiEffect.NavigateBackToReceiptHistory)
        }
    }
    fun onAllClicked() {
        _uiState.update { state ->
            state.copy(selectedTrips = state.trips.map { it.id }.toSet())
        }
        refreshButtons()
    }
    fun onDeleteClicked() {
        viewModelScope.launch {
            val selected = _uiState.value.trips.filter { it.id in _uiState.value.selectedTrips }
            if (selected.isEmpty()) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        getApplication(),
                        getApplication<Application>().getString(R.string.toast_seleccione_algun_trip),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return@launch
            }
            selected.forEach { trip ->
                tripUseCase.deleteTrip(trip)
            }
            _uiState.update { state ->
                state.copy(
                    trips = state.trips.filterNot { it.id in state.selectedTrips },
                    selectedTrips = emptySet()
                )
            }
            refreshButtons()
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    getApplication(),
                    getApplication<Application>().getString(R.string.toast_trips_eliminados_correctamente),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    fun onTripClick(trip: Trip) {
        if (trip.ticketBufferNoCopy.isNullOrBlank()) {
            viewModelScope.launch {
                _effects.emit(TripHistoryUiEffect.ShowNoTicketDialog(trip))
            }
        } else {
            viewModelScope.launch {
                _effects.emit(TripHistoryUiEffect.NavigateToReceiptHistory(trip.id))
            }
        }
    }
    fun showNoTicketDialog(trip: Trip) {
        _uiState.update {
            it.copy(
                dialog = TripHistoryDialogState(
                    title = getApplication<Application>().getString(R.string.warning),
                    description = getApplication<Application>().getString(
                        R.string.dialog_no_ticket_warning_description
                    ),
                    trip = trip,
                    showAccept = true
                )
            )
        }
    }
    fun hideDialog() {
        _uiState.update { it.copy(dialog = null) }
    }
    fun onDialogDismiss() {
        hideDialog()
    }
    fun onDialogAccept() {
        val trip = _uiState.value.dialog?.trip ?: return
        hideDialog()
        viewModelScope.launch {
            _effects.emit(TripHistoryUiEffect.NavigateToReceiptHistory(trip.id))
        }
    }
    fun toggleTripSelection(tripId: Long) {
        _uiState.update { state ->
            val updated = state.selectedTrips.toMutableSet()
            if (!updated.add(tripId)) updated.remove(tripId)
            state.copy(selectedTrips = updated)
        }
        refreshButtons()
    }
    fun refreshButtons() {
        val state = _uiState.value
        _buttonsState.value = TripHistoryButtonsState.from(
            hasTrips = state.trips.isNotEmpty(),
            hasSelection = state.selectedTrips.isNotEmpty()
        )
    }
    fun printTicket(trip: Trip) {
        viewModelScope.launch {
            portugalUseCase.increaseTicketPrinted(trip)
            printerUseCase.printTicket(trip, true)
        }
    }
}
