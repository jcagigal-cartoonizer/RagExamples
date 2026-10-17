package ifac.td.taxi.ui.screen.compose
import androidx.compose.foundation.layout.padding
import kotlinx.coroutines.flow.MutableStateFlow
import ifac.td.taxi.viewmodel.model.ShiftOrderOptions
import androidx.annotation.StringRes
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.asSharedFlow
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.layout.Spacer
import ifac.td.taxi.framework.util.Logs
import androidx.compose.foundation.layout.fillMaxWidth
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Modifier
import ifac.td.taxi.domain.usecase.ShiftExportUseCaseImpl.Companion.PURPOSE_OPEN_CSV_FILE
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.weight
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.unit.dp
import ifac.td.taxi.viewmodel.model.SortState
import androidx.compose.foundation.layout.Box
import ifac.td.taxi.domain.usecase.ShiftExportUseCaseImpl.Companion.EXTRA_INTENT_PURPOSE
import androidx.compose.foundation.layout.RowScope
import kotlinx.coroutines.flow.asStateFlow
import androidx.compose.runtime.Immutable
import ifac.td.taxi.domain.usecase.ShiftExportUseCase
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.navigation.NavController
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ifac.td.taxi.domain.usecase.TripUseCase
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.items
import ifac.td.taxi.domain.usecase.ShiftExportUseCaseImpl.Companion.ACTION_EXPORT_FILE
import ifac.td.taxi.repository.room.entities.ShiftEntity
import androidx.compose.runtime.setValue
import android.content.Intent
import kotlinx.coroutines.Dispatchers
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.background
import androidx.lifecycle.AndroidViewModel
import ifac.td.taxi.domain.usecase.ShiftUseCase
import ifac.td.taxi.domain.usecase.ShiftExportUseCaseImpl.Companion.ACTION_SEND_FILE
import ifac.td.taxi.domain.usecase.ShiftExportUseCaseImpl.Companion.PURPOSE_SEND_EMAIL
import ifac.td.taxi.domain.model.Trip
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.TopAppBar
import androidx.compose.foundation.layout.WindowInsets
import ifac.td.taxi.domain.usecase.ShiftExportUseCaseImpl.Companion.PURPOSE_OPEN_XLS_FILE
import android.app.Application
import kotlinx.coroutines.flow.MutableSharedFlow
import ifac.td.taxi.R
import androidx.compose.foundation.layout.Column


// -----------------------------
// State / Event / Effect
// -----------------------------

@Immutable
data class ShiftDetailUiState(
    val shiftId: Long = 0L,
    val trips: List<Trip> = emptyList(),
    val buttonsState: ShiftDetailButtonsState = ShiftDetailButtonsState(),
    val isLoading: Boolean = false
)

sealed interface ShiftDetailUiEvent {
    data class OnScreenStarted(val shiftId: Long) : ShiftDetailUiEvent
    data class OnSortClicked(val option: ShiftOrderOptions) : ShiftDetailUiEvent
    data object OnExportClicked : ShiftDetailUiEvent
    data object OnEmailClicked : ShiftDetailUiEvent
    data object OnPrintClicked : ShiftDetailUiEvent
}

sealed interface ShiftDetailUiEffect {
    data class OpenIntent(val intent: Intent) : ShiftDetailUiEffect
    data class ShowToast(@StringRes val messageRes: Int) : ShiftDetailUiEffect
}

@Immutable
data class ShiftDetailButtonVisualState(
    val isVisible: Boolean = false,
    @DrawableRes val iconRes: Int? = null
)

@Immutable
data class ShiftDetailButtonsState(
    val sortState: SortState = SortState(ShiftOrderOptions.NONE),
    val id: ShiftDetailButtonVisualState = ShiftDetailButtonVisualState(),
    val tripAmount: ShiftDetailButtonVisualState = ShiftDetailButtonVisualState(),
    val initHour: ShiftDetailButtonVisualState = ShiftDetailButtonVisualState(),
    val distance: ShiftDetailButtonVisualState = ShiftDetailButtonVisualState()
) {
    companion object {
        fun reducer(
            current: ShiftDetailButtonsState,
            clicked: ShiftOrderOptions
        ): ShiftDetailButtonsState {
            val nextState = when {
                current.sortState.option != clicked -> SortState(clicked, true)
                current.sortState.isAscending == true -> SortState(clicked, false)
                current.sortState.isAscending == false -> SortState(ShiftOrderOptions.NONE)
                else -> SortState(clicked, true)
            }

            val hidden = ShiftDetailButtonVisualState(false, null)

            fun iconFor(state: SortState): ShiftDetailButtonVisualState = when (state.isAscending) {
                true -> ShiftDetailButtonVisualState(true, R.drawable.round_arrow_drop_up_24)
                false -> ShiftDetailButtonVisualState(true, R.drawable.round_arrow_drop_down_24)
                null -> ShiftDetailButtonVisualState(false, null)
            }

            return when (nextState.option) {
                ShiftOrderOptions.ID -> current.copy(
                    sortState = nextState,
                    id = iconFor(nextState),
                    tripAmount = hidden,
                    initHour = hidden,
                    distance = hidden
                )
                ShiftOrderOptions.AMOUNT -> current.copy(
                    sortState = nextState,
                    id = hidden,
                    tripAmount = iconFor(nextState),
                    initHour = hidden,
                    distance = hidden
                )
                ShiftOrderOptions.START_DATE -> current.copy(
                    sortState = nextState,
                    id = hidden,
                    tripAmount = hidden,
                    initHour = iconFor(nextState),
                    distance = hidden
                )
                ShiftOrderOptions.DISTANCE -> current.copy(
                    sortState = nextState,
                    id = hidden,
                    tripAmount = hidden,
                    initHour = hidden,
                    distance = iconFor(nextState)
                )
                ShiftOrderOptions.NONE -> current.copy(
                    sortState = nextState,
                    id = hidden,
                    tripAmount = hidden,
                    initHour = hidden,
                    distance = hidden
                )
            }
        }
    }
}

// -----------------------------
// Compose ViewModel
// -----------------------------

class ShiftDetailComposeViewModel(
    application: Application,
    val tripUseCase: TripUseCase,
    val shiftExportUseCase: ShiftExportUseCase,
    val shiftUseCase: ShiftUseCase
) : AndroidViewModel(application) {

    val TAG = "ShiftDetailComposeViewModel"

    val _uiState = MutableStateFlow(ShiftDetailUiState())
    val uiState = _uiState.asStateFlow()

    val _uiEffect = MutableSharedFlow<ShiftDetailUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()

    fun onEvent(event: ShiftDetailUiEvent) {
        when (event) {
            is ShiftDetailUiEvent.OnScreenStarted -> {
                loadTrips(event.shiftId)
            }

            is ShiftDetailUiEvent.OnSortClicked -> {
                val shiftId = _uiState.value.shiftId
                val nextButtons = ShiftDetailButtonsState.reducer(
                    current = _uiState.value.buttonsState,
                    clicked = event.option
                )
                _uiState.value = _uiState.value.copy(buttonsState = nextButtons)

                if (nextButtons.sortState.option == ShiftOrderOptions.NONE) {
                    sortShiftTripsBy(shiftId, ShiftOrderOptions.NONE, true)
                } else {
                    sortShiftTripsBy(
                        shiftId,
                        nextButtons.sortState.option,
                        nextButtons.sortState.isAscending == true
                    )
                }
            }

            ShiftDetailUiEvent.OnExportClicked -> exportFile(_uiState.value.shiftId, ACTION_EXPORT_FILE)
            ShiftDetailUiEvent.OnEmailClicked -> exportFile(_uiState.value.shiftId, ACTION_SEND_FILE)
            ShiftDetailUiEvent.OnPrintClicked -> printShift(_uiState.value.shiftId)
        }
    }

    private fun loadTrips(shiftId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(shiftId = shiftId, isLoading = true)
            val trips = tripUseCase.getTripsByShiftId(shiftId)
            _uiState.value = _uiState.value.copy(trips = trips, isLoading = false)
        }
    }

    private fun sortShiftTripsBy(shiftId: Long, orderOptions: ShiftOrderOptions, isAscending: Boolean) {
        viewModelScope.launch {
            val shiftTrips = when (orderOptions) {
                ShiftOrderOptions.ID -> if (isAscending) {
                    tripUseCase.getTripsByShiftId(shiftId).sortedBy { it.id }
                } else {
                    tripUseCase.getTripsByShiftId(shiftId).sortedByDescending { it.id }
                }

                ShiftOrderOptions.AMOUNT -> if (isAscending) {
                    tripUseCase.getTripsByShiftId(shiftId).sortedBy { it.totalAmount }
                } else {
                    tripUseCase.getTripsByShiftId(shiftId).sortedByDescending { it.totalAmount }
                }

                ShiftOrderOptions.START_DATE -> if (isAscending) {
                    tripUseCase.getTripsByShiftId(shiftId).sortedBy { it.initDate }
                } else {
                    tripUseCase.getTripsByShiftId(shiftId).sortedByDescending { it.initDate }
                }

                ShiftOrderOptions.DISTANCE -> if (isAscending) {
                    tripUseCase.getTripsByShiftId(shiftId).sortedBy { it.distance }
                } else {
                    tripUseCase.getTripsByShiftId(shiftId).sortedByDescending { it.distance }
                }

                ShiftOrderOptions.NONE -> tripUseCase.getTripsByShiftId(shiftId)
            }

            _uiState.value = _uiState.value.copy(trips = shiftTrips)
        }
    }

    private fun exportFile(shiftId: Long, action: Int) {
        viewModelScope.launch {
            Logs.d(TAG, "exportFile() called with: shiftId = $shiftId, action = $action")
            shiftExportUseCase.exportShift(shiftId, action)?.let { _uiEffect.emit(ShiftDetailUiEffect.OpenIntent(it)) }
        }
    }

    private fun printShift(shiftId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val trips = tripUseCase.getTripsByShiftId(shiftId)
            shiftUseCase.printShift(trips)
        }
    }
}

// -----------------------------
// MainActivity bridge ViewModel
// -----------------------------

class ShiftDetailMainActivityViewModel(
    application: Application,
    val tripUseCase: TripUseCase,
    val shiftExportUseCase: ShiftExportUseCase,
    val shiftUseCase: ShiftUseCase
) : AndroidViewModel(application) {

    val _uiEffect = MutableSharedFlow<ShiftDetailUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()

    fun exportFile(shiftId: Long, action: Int) {
        viewModelScope.launch {
            shiftExportUseCase.exportShift(shiftId, action)?.let { _uiEffect.emit(ShiftDetailUiEffect.OpenIntent(it)) }
        }
    }

    fun printShift(shiftId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val trips = tripUseCase.getTripsByShiftId(shiftId)
            shiftUseCase.printShift(trips)
        }
    }

    fun onExportClicked(shiftId: Long) = exportFile(shiftId, ACTION_EXPORT_FILE)
    fun onEmailClicked(shiftId: Long) = exportFile(shiftId, ACTION_SEND_FILE)
    fun onPrintClicked(shiftId: Long) = printShift(shiftId)
}

// -----------------------------
// Screen
// -----------------------------

@Composable
fun ShiftDetailScreen(
    navController: NavController,
    shiftId: Long,
    viewModel: ShiftDetailComposeViewModel,
    onShowToast: (Int) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(shiftId) {
        viewModel.onEvent(ShiftDetailUiEvent.OnScreenStarted(shiftId))
    }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is ShiftDetailUiEffect.OpenIntent -> {
                    when (effect.intent.getStringExtra(EXTRA_INTENT_PURPOSE)) {
                        PURPOSE_OPEN_CSV_FILE, PURPOSE_OPEN_XLS_FILE -> {
                            try {
                                context.startActivity(effect.intent)
                            } catch (_: Exception) {
                                onShowToast(R.string.start_activity_error_toast)
                            }
                        }

                        PURPOSE_SEND_EMAIL -> {
                            try {
                                context.startActivity(
                                    Intent.createChooser(
                                        effect.intent,
                                        context.getString(R.string.send_email_title)
                                    )
                                )
                            } catch (_: Exception) {
                                onShowToast(R.string.start_activity_error_toast)
                            }
                        }
                    }
                }

                is ShiftDetailUiEffect.ShowToast -> onShowToast(effect.messageRes)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = "Shift detail") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            ShiftDetailSortHeader(
                buttonsState = state.buttonsState,
                onSortClick = { viewModel.onEvent(ShiftDetailUiEvent.OnSortClicked(it)) }
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(state.trips) { trip ->
                    Text(
                        text = "Trip #${trip.id} - ${trip.totalAmount}",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            ShiftDetailBottomMenu(
                onExport = { viewModel.onEvent(ShiftDetailUiEvent.OnExportClicked) },
                onEmail = { viewModel.onEvent(ShiftDetailUiEvent.OnEmailClicked) },
                onPrint = { viewModel.onEvent(ShiftDetailUiEvent.OnPrintClicked) }
            )
        }
    }
}

// -----------------------------
// Sort header
// -----------------------------

@Composable
fun ShiftDetailSortHeader(
    buttonsState: ShiftDetailButtonsState,
    onSortClick: (ShiftOrderOptions) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ShiftDetailSortHeaderButton(
            text = "ID",
            visualState = buttonsState.id,
            weight = 0.12f,
            onClick = { onSortClick(ShiftOrderOptions.ID) }
        )
        ShiftDetailSortHeaderButton(
            text = "€",
            visualState = buttonsState.tripAmount,
            weight = 0.21f,
            onClick = { onSortClick(ShiftOrderOptions.AMOUNT) }
        )
        ShiftDetailSortHeaderButton(
            text = "TIME",
            visualState = buttonsState.initHour,
            weight = 0.21f,
            onClick = { onSortClick(ShiftOrderOptions.START_DATE) }
        )
        ShiftDetailSortHeaderButton(
            text = "PAYMENT",
            visualState = ShiftDetailButtonVisualState(false, null),
            weight = 0.19f,
            onClick = { }
        )
        ShiftDetailSortHeaderButton(
            text = "DIST",
            visualState = buttonsState.distance,
            weight = 0.28f,
            onClick = { onSortClick(ShiftOrderOptions.DISTANCE) }
        )
    }
}

@Composable
private fun RowScope.ShiftDetailSortHeaderButton(
    text: String,
    visualState: ShiftDetailButtonVisualState,
    weight: Float,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(weight)
            .padding(horizontal = 1.dp),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = text, maxLines = 1)
        }

        if (visualState.isVisible && visualState.iconRes != null) {
            Icon(
                painter = painterResource(visualState.iconRes),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 4.dp)
                    .size(20.dp)
            )
        }
    }
}

// -----------------------------
// Bottom menu / CustomButton replacement
// -----------------------------

@Composable
fun ShiftDetailBottomMenu(
    onExport: () -> Unit,
    onEmail: () -> Unit,
    onPrint: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        ShiftDetailCustomButton(
            modifier = Modifier.weight(1f),
            text = "EXPORTAR",
            onClick = onExport
        )
        ShiftDetailCustomButton(
            modifier = Modifier.weight(1f),
            text = "EMAIL",
            onClick = onEmail
        )
        ShiftDetailCustomButton(
            modifier = Modifier.weight(1f),
            text = "IMPRIMIR",
            onClick = onPrint
        )
    }
}

@Composable
fun ShiftDetailCustomButton(
    modifier: Modifier = Modifier,
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        modifier = modifier.fillMaxHeight(),
        onClick = onClick,
        enabled = enabled
    ) {
        Text(text = text)
    }
}

// -----------------------------
// Optional simple shift list item
// -----------------------------

@Composable
fun ShiftDetailTripRow(
    trip: Trip,
    modifier: Modifier = Modifier
) {
    Text(
        text = "Trip #${trip.id} - ${trip.totalAmount}",
        modifier = modifier.padding(16.dp)
    )
}
