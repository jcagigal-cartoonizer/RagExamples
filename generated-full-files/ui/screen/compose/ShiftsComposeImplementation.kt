package ifac.td.taxi.ui.screen.compose
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.annotation.StringRes
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ifac.td.taxi.viewmodel.model.ShiftOrderOptions
import ifac.td.taxi.domain.usecase.TripUseCase
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.asSharedFlow
import ifac.td.taxi.framework.util.Logs
import kotlinx.coroutines.launch
import androidx.compose.foundation.clickable
import ifac.td.taxi.repository.room.entities.ShiftEntity
import androidx.compose.ui.Modifier
import kotlinx.coroutines.Dispatchers
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.lazy.LazyColumn
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.lifecycle.AndroidViewModel
import ifac.td.taxi.domain.usecase.ShiftUseCase
import androidx.compose.ui.unit.dp
import ifac.td.taxi.viewmodel.model.SortState
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import kotlinx.coroutines.flow.asStateFlow
import androidx.compose.ui.Alignment
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.Checkbox
import android.app.Application
import ifac.td.taxi.domain.usecase.ShiftExportUseCase
import androidx.compose.material3.Button
import androidx.navigation.NavController
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Text
import kotlinx.coroutines.flow.MutableSharedFlow
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.draw.rotate
import ifac.td.taxi.R


// ------------------------------------------------------------
// UI STATE / EVENTS / EFFECTS
// ------------------------------------------------------------

data class ShiftsUiState(
    val shifts: List<ShiftEntity> = emptyList(),
    val isLoading: Boolean = false,
    val hasShifts: Boolean = false,
    val page: Int = 0,
    val selectedShifts: Set<ShiftEntity> = emptySet(),
    val isSelectionMenuOpen: Boolean = false,
    val sortState: SortState = SortState(ShiftOrderOptions.NONE),
    val buttons: ShiftsButtonsState = ShiftsButtonsState()
)

sealed interface ShiftsUiEvent {
    data object OnResume : ShiftsUiEvent
    data object OnFabMainClicked : ShiftsUiEvent
    data object OnFabExportClicked : ShiftsUiEvent
    data object OnFabTrashClicked : ShiftsUiEvent
    data object OnLoadMore : ShiftsUiEvent
    data class OnSortClicked(val option: ShiftOrderOptions) : ShiftsUiEvent
    data class OnShiftClicked(val shift: ShiftEntity) : ShiftsUiEvent
    data class OnShiftSelectionChanged(val shift: ShiftEntity, val selected: Boolean) : ShiftsUiEvent
}

sealed interface ShiftsUiEffect {
    data class NavigateToShiftDetail(val shift: ShiftEntity) : ShiftsUiEffect
    data class OpenIntent(val intent: Intent) : ShiftsUiEffect
    data class ShowToast(@StringRes val messageRes: Int) : ShiftsUiEffect
}

// ------------------------------------------------------------
// BUTTON VISUAL STATE
// ------------------------------------------------------------

data class ShiftsButtonsState(
    val selectedHiddenVisible: Boolean = false,
    val fabMainRotation: Float = 0f,
    val fabExportVisible: Boolean = false,
    val fabTrashVisible: Boolean = false,
    val fabExportAlpha: Float = 0f,
    val fabTrashAlpha: Float = 0f,
    val fabExportTranslationY: Float = 100f,
    val fabTrashTranslationY: Float = 100f,
    val id: ShiftsButtonVisualState = ShiftsButtonVisualState(
        textRes = R.string.table_corner,
        sortArrowVisible = false,
        sortArrowRes = null
    ),
    val amount: ShiftsButtonVisualState = ShiftsButtonVisualState(
        textRes = R.string.currency_eur,
        sortArrowVisible = false,
        sortArrowRes = null
    )
)

data class ShiftsButtonVisualState(
    @StringRes val textRes: Int,
    val sortArrowVisible: Boolean,
    @DrawableRes val sortArrowRes: Int?
)

// Exact reducer mirroring the fragment button logic
fun reduceShiftsButtonsState(
    currentSortState: SortState,
    isSelectionMenuOpen: Boolean
): ShiftsButtonsState {
    fun sortVisual(option: ShiftOrderOptions, textRes: Int): ShiftsButtonVisualState {
        return when {
            currentSortState.option != option -> {
                ShiftsButtonVisualState(
                    textRes = textRes,
                    sortArrowVisible = false,
                    sortArrowRes = null
                )
            }

            currentSortState.isAscending == true -> {
                ShiftsButtonVisualState(
                    textRes = textRes,
                    sortArrowVisible = true,
                    sortArrowRes = R.drawable.round_arrow_drop_up_24
                )
            }

            currentSortState.isAscending == false -> {
                ShiftsButtonVisualState(
                    textRes = textRes,
                    sortArrowVisible = true,
                    sortArrowRes = R.drawable.round_arrow_drop_down_24
                )
            }

            else -> {
                ShiftsButtonVisualState(
                    textRes = textRes,
                    sortArrowVisible = false,
                    sortArrowRes = null
                )
            }
        }
    }

    return ShiftsButtonsState(
        selectedHiddenVisible = isSelectionMenuOpen,
        fabMainRotation = if (isSelectionMenuOpen) 180f else 0f,
        fabExportVisible = isSelectionMenuOpen,
        fabTrashVisible = isSelectionMenuOpen,
        fabExportAlpha = if (isSelectionMenuOpen) 1f else 0f,
        fabTrashAlpha = if (isSelectionMenuOpen) 1f else 0f,
        fabExportTranslationY = if (isSelectionMenuOpen) 0f else 100f,
        fabTrashTranslationY = if (isSelectionMenuOpen) 0f else 100f,
        id = sortVisual(ShiftOrderOptions.ID, R.string.table_corner),
        amount = sortVisual(ShiftOrderOptions.AMOUNT, R.string.currency_eur)
    )
}

// ------------------------------------------------------------
// VIEWMODEL (Compose-friendly bridge)
// ------------------------------------------------------------

class ShiftsViewModel(
    context: Application,
    val shiftUseCase: ShiftUseCase,
    val tripUseCase: TripUseCase,
    val shiftExportUseCase: ShiftExportUseCase,
) : BaseViewModel(context) {

    val pageSize = 25
    val tag = "ShiftsViewModel"

    val _uiState = MutableStateFlow(ShiftsUiState())
    val uiState = _uiState.asStateFlow()

    val _uiEffect = MutableSharedFlow<ShiftsUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()

    val _shiftFlow = MutableStateFlow<List<ShiftEntity>>(emptyList())
    val shiftFlow = _shiftFlow.asStateFlow()

    val _loadingFlow = MutableSharedFlow<Boolean>()
    val loadingFlow = _loadingFlow.asSharedFlow()

    val _hasShiftsFlow = MutableSharedFlow<Boolean>()
    val hasShiftsFlow = _hasShiftsFlow.asSharedFlow()

    val _intentFlow = MutableSharedFlow<Intent>()
    val intentFlow = _intentFlow.asSharedFlow()

    var currentShifts: MutableList<ShiftEntity> = _shiftFlow.value.toMutableList()
        private set

    var currentSortState: SortState = SortState(ShiftOrderOptions.NONE)

    fun onEvent(event: ShiftsUiEvent) {
        when (event) {
            ShiftsUiEvent.OnResume -> checkShifts()
            ShiftsUiEvent.OnFabMainClicked -> toggleSelectionMenu()
            ShiftsUiEvent.OnFabExportClicked -> exportSelectedItems()
            ShiftsUiEvent.OnFabTrashClicked -> deleteSelectedItems()
            ShiftsUiEvent.OnLoadMore -> loadMore()
            is ShiftsUiEvent.OnSortClicked -> handleSortButtonClick(event.option)
            is ShiftsUiEvent.OnShiftClicked -> {
                viewModelScope.launch {
                    _uiEffect.emit(ShiftsUiEffect.NavigateToShiftDetail(event.shift))
                }
            }
            is ShiftsUiEvent.OnShiftSelectionChanged -> {
                val updated = _uiState.value.selectedShifts.toMutableSet()
                if (event.selected) updated.add(event.shift) else updated.remove(event.shift)
                _uiState.value = _uiState.value.copy(selectedShifts = updated)
            }
        }
    }

    fun addShifts(page: Int) {
        viewModelScope.launch {
            _loadingFlow.emit(true)
            _uiState.value = _uiState.value.copy(isLoading = true)

            val shifts = shiftUseCase.getShiftsPaged(pageSize, pageSize * page) ?: emptyList()
            currentShifts.addAll(shifts)

            if (currentSortState.option != ShiftOrderOptions.NONE && currentSortState.isAscending != null) {
                sortShiftsBy(currentShifts, currentSortState.option, currentSortState.isAscending!!)
            } else {
                _shiftFlow.emit(currentShifts.toList())
                _uiState.value = _uiState.value.copy(shifts = currentShifts.toList())
            }

            _loadingFlow.emit(false)
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun sortShiftsBy(
        shifts: List<ShiftEntity>?,
        orderOptions: ShiftOrderOptions,
        isAscending: Boolean
    ) {
        viewModelScope.launch {
            if (shifts == null) return@launch

            currentShifts = when (orderOptions) {
                ShiftOrderOptions.ID -> {
                    if (isAscending) ArrayList(shifts.sortedBy { it.id })
                    else ArrayList(shifts.sortedByDescending { it.id })
                }

                ShiftOrderOptions.AMOUNT -> {
                    if (isAscending) ArrayList(shifts.sortedBy { it.amount })
                    else ArrayList(shifts.sortedByDescending { it.amount })
                }

                else -> ArrayList(shifts)
            }

            _shiftFlow.emit(currentShifts)
            _uiState.value = _uiState.value.copy(shifts = currentShifts.toList())
        }
    }

    fun deleteShift(shiftEntity: ShiftEntity) {
        viewModelScope.launch {
            currentShifts.remove(shiftEntity)
            shiftUseCase.deleteShift(shiftEntity)
            _shiftFlow.emit(currentShifts.toList())
            _uiState.value = _uiState.value.copy(shifts = currentShifts.toList())
        }
    }

    fun exportShifts(shiftList: Set<ShiftEntity>) {
        viewModelScope.launch {
            Logs.d(tag, "exportShifts: $shiftList")
            val shiftIds = shiftList.map { it.id }
            shiftExportUseCase.exportMultipleShifts(shiftIds)?.let { intent ->
                _intentFlow.emit(intent)
                _uiEffect.emit(ShiftsUiEffect.OpenIntent(intent))
            }
        }
    }

    fun checkShifts() {
        viewModelScope.launch {
            val shifts = shiftUseCase.getShiftCount()
            Logs.d(tag, "shifts: $shifts")
            val hasShifts = (shifts != null) && (shifts > 0L)
            _hasShiftsFlow.emit(hasShifts)
            _uiState.value = _uiState.value.copy(hasShifts = hasShifts)

            if (hasShifts && _shiftFlow.value.isEmpty()) {
                addShifts(_uiState.value.page)
            } else if (!hasShifts) {
                _shiftFlow.emit(emptyList())
                _uiState.value = _uiState.value.copy(shifts = emptyList())
            }
        }
    }

    private fun handleSortButtonClick(newOrder: ShiftOrderOptions) {
        val nextState = when {
            currentSortState.option != newOrder -> SortState(newOrder, true)
            currentSortState.isAscending == true -> SortState(newOrder, false)
            currentSortState.isAscending == false -> SortState(ShiftOrderOptions.NONE)
            else -> SortState(newOrder, true)
        }

        currentSortState = nextState
        _uiState.value = _uiState.value.copy(
            sortState = nextState,
            buttons = reduceShiftsButtonsState(nextState, _uiState.value.isSelectionMenuOpen)
        )

        if (nextState.option == ShiftOrderOptions.NONE) {
            sortShiftsBy(currentShifts, ShiftOrderOptions.NONE, true)
        } else {
            sortShiftsBy(currentShifts, nextState.option, nextState.isAscending!!)
        }

        Logs.d(tag, "Sort clicked: ${nextState.option}, ascending: ${nextState.isAscending}")
    }

    private fun toggleSelectionMenu() {
        val open = !_uiState.value.isSelectionMenuOpen
        _uiState.value = _uiState.value.copy(
            isSelectionMenuOpen = open,
            buttons = reduceShiftsButtonsState(_uiState.value.sortState, open)
        )
    }

    private fun deleteSelectedItems() {
        val selectedItems = _uiState.value.selectedShifts
        if (selectedItems.isNotEmpty()) {
            selectedItems.forEach { shift ->
                deleteShift(shift)
                Logs.d(tag, "Deleted shiftStatus with ID: ${shift.id}")
            }
            _uiState.value = _uiState.value.copy(selectedShifts = emptySet())
        } else {
            viewModelScope.launch {
                _uiEffect.emit(ShiftsUiEffect.ShowToast(R.string.toast_selecciona_turno_eliminar))
            }
        }
    }

    private fun exportSelectedItems() {
        val selectedItems = _uiState.value.selectedShifts
        if (selectedItems.isNotEmpty()) {
            exportShifts(selectedItems)
        } else {
            viewModelScope.launch {
                _uiEffect.emit(ShiftsUiEffect.ShowToast(R.string.toast_selecciona_turno_exportar))
            }
        }
    }

    private fun loadMore() {
        if (_uiState.value.isLoading) return
        val nextPage = _uiState.value.page + 1
        _uiState.value = _uiState.value.copy(page = nextPage)
        addShifts(nextPage)
    }
}

// ------------------------------------------------------------
// SHIFT DETAIL VIEWMODEL (included for single-file completeness)
// ------------------------------------------------------------

class ShiftDetailViewModel(
    context: Application,
    val tripUseCase: TripUseCase,
    val shiftExportUseCase: ShiftExportUseCase,
    val shiftUseCase: ShiftUseCase
) : BaseViewModel(context) {

    val tag = "ShiftDetailViewModel"

    val _tripsFlow = MutableStateFlow<List<ifac.td.taxi.domain.model.Trip>?>(null)
    val tripFlow = _tripsFlow.asStateFlow()

    val _intentFlow = MutableSharedFlow<Intent>()
    val intentFlow = _intentFlow.asSharedFlow()

    var currentSortState: SortState = SortState(ShiftOrderOptions.NONE)

    fun getTripsFromShiftId(shiftId: Long) {
        viewModelScope.launch {
            val trips = tripUseCase.getTripsByShiftId(shiftId)
            _tripsFlow.emit(trips)
        }
    }

    fun sortShiftTripsBy(shiftId: Long, orderOptions: ShiftOrderOptions, isAscending: Boolean) {
        viewModelScope.launch {
            val shiftTrips = when (orderOptions) {
                ShiftOrderOptions.ID -> {
                    if (isAscending) tripUseCase.getTripsByShiftId(shiftId).sortedBy { it.id }
                    else tripUseCase.getTripsByShiftId(shiftId).sortedByDescending { it.id }
                }

                ShiftOrderOptions.AMOUNT -> {
                    if (isAscending) tripUseCase.getTripsByShiftId(shiftId).sortedBy { it.totalAmount }
                    else tripUseCase.getTripsByShiftId(shiftId).sortedByDescending { it.totalAmount }
                }

                ShiftOrderOptions.START_DATE -> {
                    if (isAscending) tripUseCase.getTripsByShiftId(shiftId).sortedBy { it.initDate }
                    else tripUseCase.getTripsByShiftId(shiftId).sortedByDescending { it.initDate }
                }

                ShiftOrderOptions.DISTANCE -> {
                    if (isAscending) tripUseCase.getTripsByShiftId(shiftId).sortedBy { it.distance }
                    else tripUseCase.getTripsByShiftId(shiftId).sortedByDescending { it.distance }
                }

                ShiftOrderOptions.NONE -> tripUseCase.getTripsByShiftId(shiftId)
            }
            _tripsFlow.emit(shiftTrips)
        }
    }

    fun exportFile(shiftId: Long, action: Int) {
        viewModelScope.launch {
            Logs.d(tag, "exportFile() called with: shiftId = $shiftId, action = $action")
            shiftExportUseCase.exportShift(shiftId, action)?.let { _intentFlow.emit(it) }
        }
    }

    fun printShift(shiftId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val trips = tripUseCase.getTripsByShiftId(shiftId)
            shiftUseCase.printShift(trips)
        }
    }
}

// ------------------------------------------------------------
// MAIN ACTIVITY BRIDGE
// ------------------------------------------------------------

sealed interface ShiftsMainActivityEffect {
    data class ShowHeader(val visible: Boolean) : ShiftsMainActivityEffect
    data class ShowToast(@StringRes val messageRes: Int) : ShiftsMainActivityEffect
    data class StartActivity(val intent: Intent) : ShiftsMainActivityEffect
}

class ShiftsMainActivityViewModel : ViewModel() {
    val _effects = MutableSharedFlow<ShiftsMainActivityEffect>()
    val effects = _effects.asSharedFlow()

    suspend fun showHeader(visible: Boolean) {
        _effects.emit(ShiftsMainActivityEffect.ShowHeader(visible))
    }

    suspend fun showToast(@StringRes messageRes: Int) {
        _effects.emit(ShiftsMainActivityEffect.ShowToast(messageRes))
    }

    suspend fun startActivity(intent: Intent) {
        _effects.emit(ShiftsMainActivityEffect.StartActivity(intent))
    }
}

// ------------------------------------------------------------
// COMPOSABLE SCREEN
// ------------------------------------------------------------

@Composable
fun ShiftsScreen(
    viewModel: ShiftsViewModel,
    navController: NavController,
    onShowHeader: (Boolean) -> Unit,
    onShowToast: (Int) -> Unit,
    onStartActivity: (Intent) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleResumeEffect(Unit) {
        onShowHeader(true)
        viewModel.onEvent(ShiftsUiEvent.OnResume)
        onDispose { }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is ShiftsUiEffect.NavigateToShiftDetail -> {
                    val direction =
                        ShiftsFragmentDirections.actionShiftsFragmentToShiftDetailFragment(effect.shift)
                    navController.navigate(direction)
                }

                is ShiftsUiEffect.OpenIntent -> {
                    try {
                        onStartActivity(effect.intent)
                    } catch (_: Exception) {
                        onShowToast(R.string.start_activity_error_toast)
                    }
                }

                is ShiftsUiEffect.ShowToast -> onShowToast(effect.messageRes)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            ShiftsHeader(
                buttons = uiState.buttons,
                onSortId = { viewModel.onEvent(ShiftsUiEvent.OnSortClicked(ShiftOrderOptions.ID)) },
                onSortAmount = { viewModel.onEvent(ShiftsUiEvent.OnSortClicked(ShiftOrderOptions.AMOUNT)) },
                onFabMain = { viewModel.onEvent(ShiftsUiEvent.OnFabMainClicked) },
                onFabExport = { viewModel.onEvent(ShiftsUiEvent.OnFabExportClicked) },
                onFabTrash = { viewModel.onEvent(ShiftsUiEvent.OnFabTrashClicked) }
            )

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                itemsIndexed(uiState.shifts) { _, shift ->
                    ShiftRow(
                        shift = shift,
                        onClick = { viewModel.onEvent(ShiftsUiEvent.OnShiftClicked(shift)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ShiftsHeader(
    buttons: ShiftsButtonsState,
    onSortId: () -> Unit,
    onSortAmount: () -> Unit,
    onFabMain: () -> Unit,
    onFabExport: () -> Unit,
    onFabTrash: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = buttons.selectedHiddenVisible,
                onCheckedChange = null,
                enabled = false
            )

            SortButton(
                visual = buttons.id,
                onClick = onSortId
            )

            SortButton(
                visual = buttons.amount,
                onClick = onSortAmount
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 32.dp, bottom = 32.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Column(horizontalAlignment = Alignment.End) {
                if (buttons.fabTrashVisible) {
                    FloatingMiniActionButton(
                        iconRes = R.drawable.outline_delete_24,
                        alpha = buttons.fabTrashAlpha,
                        translationY = buttons.fabTrashTranslationY,
                        onClick = onFabTrash
                    )
                }

                if (buttons.fabExportVisible) {
                    FloatingMiniActionButton(
                        iconRes = R.drawable.baseline_arrow_circle_down_24,
                        alpha = buttons.fabExportAlpha,
                        translationY = buttons.fabExportTranslationY,
                        onClick = onFabExport
                    )
                }

                FloatingMainActionButton(
                    rotation = buttons.fabMainRotation,
                    onClick = onFabMain
                )
            }
        }
    }
}

@Composable
private fun SortButton(
    visual: ShiftsButtonVisualState,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(horizontal = 2.dp)
            .heightIn(min = 48.dp)
    ) {
        Button(onClick = onClick) {
            Text(text = stringResource(visual.textRes))
        }

        if (visual.sortArrowVisible && visual.sortArrowRes != null) {
            Icon(
                painter = painterResource(visual.sortArrowRes),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 8.dp)
            )
        }
    }
}

@Composable
private fun FloatingMainActionButton(
    rotation: Float,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Icon(
            painter = painterResource(R.drawable.arrow_up),
            contentDescription = null,
            modifier = Modifier.rotate(rotation)
        )
    }
}

@Composable
private fun FloatingMiniActionButton(
    @DrawableRes iconRes: Int,
    alpha: Float,
    translationY: Float,
    onClick: () -> Unit
) {
    val visible = alpha > 0f
    if (!visible) return

    IconButton(onClick = onClick) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier
                .graphicsLayer {
                    this.alpha = alpha
                    translationY = this@FloatingMiniActionButton.translationY
                }
        )
    }
}

@Composable
private fun ShiftRow(
    shift: ShiftEntity,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Text(text = "Shift #${shift.id}")
    }
}
