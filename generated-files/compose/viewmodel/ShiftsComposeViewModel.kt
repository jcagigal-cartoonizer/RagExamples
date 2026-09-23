package ifac.td.taxi.compose.viewmodel
import ifac.td.taxi.ui.screen.components.ShiftsUiEffect
import ifac.td.taxi.ui.screen.components.ShiftsUiState
import ifac.td.taxi.ui.screen.components.ShiftsUiEvent
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 6-1: import android.app.Application
class ShiftsComposeViewModel(
    application: Application,
    private val shiftUseCase: ShiftUseCase,
    private val shiftExportUseCase: ShiftExportUseCase,
) : AndroidViewModel(application) {
    private val TAG = "ShiftsComposeViewModel"
    private val pageSize = 25
    private val _uiState = MutableStateFlow(ShiftsUiState())
    val uiState: StateFlow<ShiftsUiState> = _uiState.asStateFlow()
    private val _uiEffect = MutableSharedFlow<ShiftsUiEffect>()
    val uiEffect: SharedFlow<ShiftsUiEffect> = _uiEffect
    private var currentShifts: MutableList<ShiftEntity> = mutableListOf()
    private var currentSortState: SortState = SortState(ShiftOrderOptions.NONE)
    fun onEvent(event: ShiftsUiEvent) {
        when (event) {
            ShiftsUiEvent.ScreenResumed -> checkShifts()
            ShiftsUiEvent.LoadNextPage -> loadNextPage()
            is ShiftsUiEvent.SortById -> handleSortClick(ShiftOrderOptions.ID)
            is ShiftsUiEvent.SortByAmount -> handleSortClick(ShiftOrderOptions.AMOUNT)
            ShiftsUiEvent.ToggleMenu -> toggleMenu()
            ShiftsUiEvent.ExportSelected -> exportSelected()
            ShiftsUiEvent.DeleteSelected -> deleteSelected()
            is ShiftsUiEvent.ItemClicked -> openShiftDetail(event.shift)
            is ShiftsUiEvent.DialogConfirmDelete -> confirmDeleteSelected()
            ShiftsUiEvent.DialogDismiss -> updateDialog(null)
        }
    }
    fun checkShifts() {
        viewModelScope.launch {
            val shifts = shiftUseCase.getShiftCount()
            _uiState.value = _uiState.value.copy(hasShifts = (shifts != null && shifts > 0L))
            if (_uiState.value.hasShifts && _uiState.value.shifts.isEmpty()) {
                loadNextPage()
            }
        }
    }
    fun loadNextPage() {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val page = _uiState.value.page
            val shifts = shiftUseCase.getShiftsPaged(pageSize, pageSize * page).orEmpty()
            currentShifts.addAll(shifts)
            if (currentSortState.option != ShiftOrderOptions.NONE && currentSortState.isAscending != null) {
                sortShiftsBy(currentShifts, currentSortState.option, currentSortState.isAscending!!)
            } else {
                _uiState.value = _uiState.value.copy(shifts = currentShifts.toList())
            }
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                page = page + 1
            )
        }
    }
    fun sortShiftsBy(
        shifts: List<ShiftEntity>,
        orderOptions: ShiftOrderOptions,
        isAscending: Boolean
    ) {
        viewModelScope.launch {
            val sorted = when (orderOptions) {
                ShiftOrderOptions.ID -> if (isAscending) {
                    shifts.sortedBy { it.id }
                } else {
                    shifts.sortedByDescending { it.id }
                }
                ShiftOrderOptions.AMOUNT -> if (isAscending) {
                    shifts.sortedBy { it.amount }
                } else {
                    shifts.sortedByDescending { it.amount }
                }
                else -> shifts
            }
            currentShifts = sorted.toMutableList()
            _uiState.value = _uiState.value.copy(shifts = currentShifts.toList())
        }
    }
    fun handleSortClick(newOrder: ShiftOrderOptions) {
        val nextState = when {
            currentSortState.option != newOrder -> SortState(newOrder, true)
            currentSortState.isAscending == true -> SortState(newOrder, false)
            currentSortState.isAscending == false -> SortState(ShiftOrderOptions.NONE)
            else -> SortState(newOrder, true)
        }
        currentSortState = nextState
        _uiState.value = _uiState.value.copy(sortState = nextState)
        when (nextState.isAscending) {
            true -> _uiState.value = _uiState.value.copy(
                buttonsState = _uiState.value.buttonsState.copy(
                    sortIdArrow = if (newOrder == ShiftOrderOptions.ID) ArrowState.Up else ArrowState.Hidden,
                    sortAmountArrow = if (newOrder == ShiftOrderOptions.AMOUNT) ArrowState.Up else ArrowState.Hidden
                )
            )
            false -> _uiState.value = _uiState.value.copy(
                buttonsState = _uiState.value.buttonsState.copy(
                    sortIdArrow = if (newOrder == ShiftOrderOptions.ID) ArrowState.Down else ArrowState.Hidden,
                    sortAmountArrow = if (newOrder == ShiftOrderOptions.AMOUNT) ArrowState.Down else ArrowState.Hidden
                )
            )
            null -> _uiState.value = _uiState.value.copy(
                buttonsState = _uiState.value.buttonsState.copy(
                    sortIdArrow = ArrowState.Hidden,
                    sortAmountArrow = ArrowState.Hidden
                )
            )
        }
        if (nextState.option == ShiftOrderOptions.NONE) {
            sortShiftsBy(currentShifts, ShiftOrderOptions.NONE, true)
        } else {
            sortShiftsBy(currentShifts, nextState.option, nextState.isAscending!!)
        }
    }
    fun toggleMenu() {
        val current = _uiState.value.buttonsState
        _uiState.value = _uiState.value.copy(
            buttonsState = current.copy(isOpen = !current.isOpen)
        )
    }
    fun exportSelected() {
        if (_uiState.value.selectedShiftIds.isEmpty()) {
            updateDialog(
                DialogSpec.Message(
                    title = "Export shifts",
                    message = "Please select at least one shift to export."
                )
            )
            return
        }
        viewModelScope.launch {
            val selected = currentShifts.filter { it.id in _uiState.value.selectedShiftIds }
            val ids = selected.map { it.id }
            shiftExportUseCase.exportMultipleShifts(ids)?.let {
                _uiEffect.emit(ShiftsUiEffect.OpenIntent(it))
            }
        }
    }
    fun deleteSelected() {
        if (_uiState.value.selectedShiftIds.isEmpty()) {
            updateDialog(
                DialogSpec.Message(
                    title = "Delete shifts",
                    message = "Please select at least one shift to delete."
                )
            )
            return
        }
        updateDialog(
            DialogSpec.ConfirmDelete(
                title = "Delete selected shifts?",
                message = "This action cannot be undone."
            )
        )
    }
    fun confirmDeleteSelected() {
        viewModelScope.launch {
            val ids = _uiState.value.selectedShiftIds.toSet()
            val toDelete = currentShifts.filter { it.id in ids }
            toDelete.forEach { shift ->
                shiftUseCase.deleteShift(shift)
            }
            currentShifts.removeAll(toDelete)
            _uiState.value = _uiState.value.copy(
                shifts = currentShifts.toList(),
                selectedShiftIds = emptySet(),
                buttonsState = _uiState.value.buttonsState.copy(isSelectionMode = false, isOpen = false)
            )
            updateDialog(null)
        }
    }
    fun openShiftDetail(shift: ShiftEntity) {
        viewModelScope.launch {
            _uiEffect.emit(ShiftsUiEffect.NavigateToShiftDetail(shift))
        }
    }
    fun toggleSelection(shiftId: Long) {
        val updated = _uiState.value.selectedShiftIds.toMutableSet()
        if (!updated.add(shiftId)) updated.remove(shiftId)
        _uiState.value = _uiState.value.copy(selectedShiftIds = updated)
    }
    fun updateDialog(dialog: DialogSpec?) {
        _uiState.value = _uiState.value.copy(dialog = dialog)
    }
}
