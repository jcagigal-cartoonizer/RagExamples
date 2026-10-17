package ifac.td.taxi.ui.screen.compose
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.asSharedFlow
import androidx.compose.runtime.mutableStateOf
import ifac.td.taxi.framework.util.Logs
import kotlinx.coroutines.launch
import androidx.compose.foundation.clickable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.TextButton
import ifac.td.taxi.ui.custom.button.ButtonType
import androidx.compose.ui.Modifier
import ifac.td.taxi.domain.usecase.PortugalUseCase
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.Composable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import kotlinx.coroutines.flow.SharedFlow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.remember
import androidx.compose.material3.Button
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Text
import ifac.td.taxi.domain.usecase.PrinterUseCase
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.text.style.TextAlign
import ifac.td.taxi.domain.usecase.TripUseCase
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.collectAsState
import ifac.td.taxi.domain.usecase.DispatchUseCase
import kotlinx.coroutines.withContext
import androidx.compose.runtime.setValue
import androidx.lifecycle.repeatOnLifecycle
import kotlin.math.abs
import kotlinx.coroutines.Dispatchers
import android.content.Intent
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.getValue
import ifac.td.taxi.framework.sdk.usecase.NavigatorUseCase
import androidx.compose.foundation.background
import androidx.lifecycle.AndroidViewModel
import androidx.compose.foundation.layout.*
import ifac.td.taxi.domain.model.Trip
import androidx.compose.ui.Alignment
import androidx.lifecycle.Lifecycle
import android.app.Application
import kotlinx.coroutines.flow.MutableSharedFlow
import androidx.compose.material3.ButtonDefaults
import ifac.td.taxi.R


// ------------------------------------------------------------
// UI STATE / EVENT / EFFECT
// ------------------------------------------------------------

@Stable
data class TripHistoryUiState(
    val trips: List<Trip> = emptyList(),
    val isLoading: Boolean = false,
    val selectedTrips: Set<Trip> = emptySet(),
    val buttonsState: TripHistoryButtonsState = TripHistoryButtonsState(),
    val dialogState: TripHistoryDialogState? = null
)

sealed interface TripHistoryUiEvent {
    data object LoadFirstPage : TripHistoryUiEvent
    data object LoadNextPage : TripHistoryUiEvent
    data object BackClicked : TripHistoryUiEvent
    data object AllClicked : TripHistoryUiEvent
    data object DeleteClicked : TripHistoryUiEvent
    data class TripClicked(val trip: Trip, val position: Int) : TripHistoryUiEvent
    data object DialogDismissed : TripHistoryUiEvent
    data object DialogAccepted : TripHistoryUiEvent
    data object DialogCancelled : TripHistoryUiEvent
}

sealed interface TripHistoryUiEffect {
    data object NavigateBack : TripHistoryUiEffect
    data class NavigateToReceiptHistory(val tripId: Long) : TripHistoryUiEffect
    data class OpenDialog(val dialogState: TripHistoryDialogState) : TripHistoryUiEffect
    data class ShowToast(val messageRes: Int) : TripHistoryUiEffect
}

@Stable
data class TripHistoryDialogState(
    val title: String,
    val description: String,
    val buttons: List<ButtonType>,
    val editText: String? = null,
    val hint: String? = null
)

// ------------------------------------------------------------
// BUTTON VISUAL STATE + REDUCER
// ------------------------------------------------------------

@Stable
data class TripHistoryButtonVisualState(
    val text: String,
    val backgroundColor: Color,
    val contentColor: Color,
    val enabled: Boolean,
    val visible: Boolean,
    val showIcon: Boolean,
    val iconText: String? = null
)

@Stable
data class TripHistoryButtonsState(
    val back: TripHistoryButtonVisualState = TripHistoryButtonVisualState(
        text = "VOLVER",
        backgroundColor = Color(0xFF1565C0),
        contentColor = Color.White,
        enabled = true,
        visible = true,
        showIcon = true,
        iconText = "←"
    ),
    val all: TripHistoryButtonVisualState = TripHistoryButtonVisualState(
        text = "TODOS",
        backgroundColor = Color(0xFF2E7D32),
        contentColor = Color.White,
        enabled = true,
        visible = true,
        showIcon = true,
        iconText = "✓"
    ),
    val delete: TripHistoryButtonVisualState = TripHistoryButtonVisualState(
        text = "DELETE",
        backgroundColor = Color(0xFFC62828),
        contentColor = Color.White,
        enabled = true,
        visible = true,
        showIcon = true,
        iconText = "✕"
    )
) {
    fun reduce(action: TripHistoryButtonsAction): TripHistoryButtonsState {
        return when (action) {
            TripHistoryButtonsAction.Back -> copy(
                back = back.copy(
                    text = "VOLVER",
                    backgroundColor = Color(0xFF1565C0),
                    contentColor = Color.White,
                    enabled = true,
                    visible = true,
                    showIcon = true,
                    iconText = "←"
                )
            )

            TripHistoryButtonsAction.SelectAll -> copy(
                all = all.copy(
                    text = "TODOS",
                    backgroundColor = Color(0xFF2E7D32),
                    contentColor = Color.White,
                    enabled = true,
                    visible = true,
                    showIcon = true,
                    iconText = "✓"
                )
            )

            TripHistoryButtonsAction.ClearSelection -> copy(
                delete = delete.copy(
                    text = "DELETE",
                    backgroundColor = Color(0xFFC62828),
                    contentColor = Color.White,
                    enabled = true,
                    visible = true,
                    showIcon = true,
                    iconText = "✕"
                )
            )
        }
    }
}

sealed interface TripHistoryButtonsAction {
    data object Back : TripHistoryButtonsAction
    data object SelectAll : TripHistoryButtonsAction
    data object ClearSelection : TripHistoryButtonsAction
}

// ------------------------------------------------------------
// COMPOSE VIEWMODEL
// ------------------------------------------------------------

class TripHistoryComposeViewModel(
    application: Application,
    val tripUseCase: TripUseCase,
    val printerUseCase: PrinterUseCase,
    val portugalUseCase: PortugalUseCase,
) : AndroidViewModel(application) {

    val pageSize = 25
    private var page = 0
    private var isLastPage = false

    val _uiState = MutableStateFlow(TripHistoryUiState())
    val uiState: StateFlow<TripHistoryUiState> = _uiState.asStateFlow()

    val _uiEffect = MutableSharedFlow<TripHistoryUiEffect>(extraBufferCapacity = 1)
    val uiEffect: SharedFlow<TripHistoryUiEffect> = _uiEffect.asSharedFlow()

    fun onEvent(event: TripHistoryUiEvent) {
        when (event) {
            TripHistoryUiEvent.LoadFirstPage -> loadTrips(reset = true)
            TripHistoryUiEvent.LoadNextPage -> loadTrips(reset = false)
            TripHistoryUiEvent.BackClicked -> emitEffect(TripHistoryUiEffect.NavigateBack)
            TripHistoryUiEvent.AllClicked -> {
                _uiState.update {
                    it.copy(
                        buttonsState = it.buttonsState.reduce(TripHistoryButtonsAction.SelectAll)
                    )
                }
            }

            TripHistoryUiEvent.DeleteClicked -> deleteSelectedTrips()
            is TripHistoryUiEvent.TripClicked -> handleTripClick(event.trip)
            TripHistoryUiEvent.DialogDismissed -> _uiState.update { it.copy(dialogState = null) }
            TripHistoryUiEvent.DialogAccepted -> _uiState.update { it.copy(dialogState = null) }
            TripHistoryUiEvent.DialogCancelled -> _uiState.update { it.copy(dialogState = null) }
        }
    }

    private fun loadTrips(reset: Boolean) {
        viewModelScope.launch {
            if (reset) {
                page = 0
                isLastPage = false
                _uiState.update { it.copy(trips = emptyList()) }
            }

            if (isLastPage) return@launch

            _uiState.update { it.copy(isLoading = true) }

            try {
                Logs.d("TripHistoryComposeViewModel", "getTrips: page: $page")
                val loadedItems = page * pageSize
                val newTrips = tripUseCase.getAllTripsFinishedPaged(loadedItems)
                val currentTrips = _uiState.value.trips

                if (newTrips.size < pageSize) {
                    isLastPage = true
                }

                val combinedTrips = (currentTrips + newTrips).distinctBy { it.id }
                _uiState.update { it.copy(trips = combinedTrips) }
                page++
            } catch (e: Exception) {
                Logs.e("TripHistoryComposeViewModel", "getTrips: error: ${e.message}")
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun deleteSelectedTrips() {
        viewModelScope.launch {
            val selectedTrips = _uiState.value.selectedTrips
            if (selectedTrips.isNotEmpty()) {
                selectedTrips.forEach { trip ->
                    tripUseCase.deleteTrip(trip)
                    Logs.d("TripHistoryComposeViewModel", "trip ${trip.id} deleted")
                }

                _uiState.update {
                    it.copy(
                        trips = it.trips.filterNot { trip -> trip in selectedTrips },
                        selectedTrips = emptySet(),
                        buttonsState = it.buttonsState.reduce(TripHistoryButtonsAction.ClearSelection)
                    )
                }

                emitEffect(TripHistoryUiEffect.ShowToast(R.string.toast_trips_eliminados_correctamente))
            } else {
                emitEffect(TripHistoryUiEffect.ShowToast(R.string.toast_seleccione_algun_trip))
            }
        }
    }

    private fun handleTripClick(trip: Trip) {
        if (trip.ticketBufferNoCopy.isNullOrBlank()) {
            emitEffect(
                TripHistoryUiEffect.OpenDialog(
                    TripHistoryDialogState(
                        title = context.getString(R.string.warning),
                        description = context.getString(R.string.dialog_no_ticket_warning_description),
                        buttons = listOf(ButtonType.CANCEL, ButtonType.ACCEPT)
                    )
                )
            )
        } else {
            emitEffect(TripHistoryUiEffect.NavigateToReceiptHistory(trip.id))
        }
    }

    fun printTicket(trip: Trip) {
        viewModelScope.launch {
            portugalUseCase.increaseTicketPrinted(trip)
            printerUseCase.printTicket(trip, true)
        }
    }

    private fun emitEffect(effect: TripHistoryUiEffect) {
        _uiEffect.tryEmit(effect)
    }
}

// ------------------------------------------------------------
// MAIN ACTIVITY BRIDGE VIEWMODEL
// ------------------------------------------------------------

class TripHistoryMainActivityViewModel : ViewModel() {

    val _navigationEffect = MutableSharedFlow<TripHistoryMainActivityEffect>(extraBufferCapacity = 1)
    val navigationEffect: SharedFlow<TripHistoryMainActivityEffect> = _navigationEffect.asSharedFlow()

    fun showHeader(show: Boolean) {
        _navigationEffect.tryEmit(TripHistoryMainActivityEffect.ShowHeader(show))
    }

    fun navigateToReceiptHistory(tripId: Long) {
        _navigationEffect.tryEmit(TripHistoryMainActivityEffect.NavigateToReceiptHistory(tripId))
    }

    fun navigateBack() {
        _navigationEffect.tryEmit(TripHistoryMainActivityEffect.NavigateBack)
    }

    fun openDialog(dialogState: TripHistoryDialogState) {
        _navigationEffect.tryEmit(TripHistoryMainActivityEffect.OpenDialog(dialogState))
    }
}

sealed interface TripHistoryMainActivityEffect {
    data class ShowHeader(val show: Boolean) : TripHistoryMainActivityEffect
    data class NavigateToReceiptHistory(val tripId: Long) : TripHistoryMainActivityEffect
    data object NavigateBack : TripHistoryMainActivityEffect
    data class OpenDialog(val dialogState: TripHistoryDialogState) : TripHistoryMainActivityEffect
}

// ------------------------------------------------------------
// COMPOSE SCREEN
// ------------------------------------------------------------

@Composable
fun TripHistoryScreen(
    viewModel: TripHistoryComposeViewModel,
    onShowHeader: (Boolean) -> Unit,
    onNavigateToReceiptHistory: (Long) -> Unit,
    onNavigateBack: () -> Unit,
    onOpenCustomDialog: (TripHistoryDialogState, onAccept: () -> Unit, onCancel: () -> Unit) -> Unit,
    onShowToast: (Int) -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        onShowHeader(true)
        viewModel.onEvent(TripHistoryUiEvent.LoadFirstPage)
    }

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.uiEffect.collectLatest { effect ->
                when (effect) {
                    is TripHistoryUiEffect.NavigateToReceiptHistory -> onNavigateToReceiptHistory(effect.tripId)
                    TripHistoryUiEffect.NavigateBack -> onNavigateBack()
                    is TripHistoryUiEffect.OpenDialog -> {
                        onOpenCustomDialog(
                            effect.dialogState,
                            onAccept = { viewModel.onEvent(TripHistoryUiEvent.DialogAccepted) },
                            onCancel = { viewModel.onEvent(TripHistoryUiEvent.DialogCancelled) }
                        )
                    }
                    is TripHistoryUiEffect.ShowToast -> onShowToast(effect.messageRes)
                }
            }
        }
    }

    TripHistoryContent(
        uiState = uiState,
        onEvent = viewModel::onEvent
    )
}

@Composable
private fun TripHistoryContent(
    uiState: TripHistoryUiState,
    onEvent: (TripHistoryUiEvent) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    itemsIndexed(uiState.trips, key = { _, trip -> trip.id }) { index, trip ->
                        TripHistoryTripRow(
                            trip = trip,
                            onClick = { onEvent(TripHistoryUiEvent.TripClicked(trip, index)) }
                        )
                    }

                    if (uiState.isLoading && uiState.trips.isNotEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }

                if (uiState.isLoading && uiState.trips.isEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }

            TripHistoryButtonsBar(
                buttonsState = uiState.buttonsState,
                onBack = { onEvent(TripHistoryUiEvent.BackClicked) },
                onAll = { onEvent(TripHistoryUiEvent.AllClicked) },
                onDelete = { onEvent(TripHistoryUiEvent.DeleteClicked) }
            )
        }
    }
}

@Composable
private fun TripHistoryTripRow(
    trip: Trip,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = "Trip #${trip.id}",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = if (trip.ticketBufferNoCopy.isNullOrBlank()) "No ticket" else "Has ticket",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun TripHistoryButtonsBar(
    buttonsState: TripHistoryButtonsState,
    onBack: () -> Unit,
    onAll: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .background(Color.Black)
            .padding(1.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        TripHistoryCustomButton(
            modifier = Modifier.weight(1f),
            state = buttonsState.back,
            onClick = onBack
        )
        TripHistoryCustomButton(
            modifier = Modifier.weight(1f),
            state = buttonsState.all,
            onClick = onAll
        )
        TripHistoryCustomButton(
            modifier = Modifier.weight(1f),
            state = buttonsState.delete,
            onClick = onDelete
        )
    }
}

@Composable
private fun TripHistoryCustomButton(
    modifier: Modifier = Modifier,
    state: TripHistoryButtonVisualState,
    onClick: () -> Unit,
) {
    if (!state.visible) return

    Button(
        onClick = onClick,
        enabled = state.enabled,
        modifier = modifier
            .fillMaxHeight()
            .padding(1.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = state.backgroundColor,
            disabledContainerColor = state.backgroundColor,
            contentColor = state.contentColor,
            disabledContentColor = state.contentColor
        )
    ) {
        if (state.showIcon && state.iconText != null) {
            Text(text = state.iconText, color = state.contentColor)
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = state.text,
            color = state.contentColor,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun TripHistoryDialog(
    dialogState: TripHistoryDialogState,
    onAccept: () -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(dialogState.title) },
        text = { Text(dialogState.description) },
        confirmButton = {
            TextButton(onClick = onAccept) {
                Text("ACCEPT")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("CANCEL")
            }
        }
    )
}

// ------------------------------------------------------------
// OPTIONAL: DESTINATION MAP VIEWMODEL (FROM YOUR INPUT)
// ------------------------------------------------------------

class DestinationMapViewModel(
    val dispatchUseCase: DispatchUseCase,
    val navigatorUseCase: NavigatorUseCase,
    context: Application,
) : ifac.td.taxi.viewmodel.BaseViewModel(context) {

    val TAG = "DestinationMapViewModel"

    val _navigatorFlow = MutableSharedFlow<Intent?>()
    val navigatorFlow = _navigatorFlow.asSharedFlow()

    fun openNavigatorApp(coordinates: String, street: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            Logs.d(TAG, "openNavigatorApp: Coordinates = $coordinates, Street = $street")

            val navigatorIntent = when {
                !coordinates.isNullOrEmpty() && !street.isNullOrEmpty() -> {
                    Logs.d(TAG, "openNavigatorApp: Opening navigator with coordinates ($coordinates) and street: $street")
                    navigatorUseCase.openNavigatorWithCoordinatesAndStreetName(coordinates, street)
                }
                !coordinates.isNullOrEmpty() -> {
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
