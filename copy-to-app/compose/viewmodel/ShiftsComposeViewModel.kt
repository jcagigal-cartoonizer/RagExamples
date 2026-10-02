package ifac.td.taxi.compose.viewmodel
import  android.app.Application
import  ifac.td.taxi.R
import ifac.td.taxi.repository.connections.service.model.*
import androidx.annotation.StringRes
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import ifac.td.taxi.compose.viewmodel.*
import androidx.navigation.NavController
import ifac.td.taxi.viewmodel.MainActivityViewModel
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.platform.*
import androidx.core.net.toUri
import androidx.navigation.*
import ifac.td.taxi.ui.screen.components.*
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.*
import com.interfacom.sdk.taximeter.bravocomm.*
import ifac.td.taxi.domain.model.*
import ifac.td.taxi.domain.usecase.*
import ifac.td.taxi.framework.sdk.bravocentral.usecase.*
import ifac.td.taxi.framework.sdk.usecase.*
import ifac.td.taxi.repository.room.entities.*
import ifac.td.taxi.repository.room.entities.countdown.*
import ifac.td.taxi.repository.room.entities.message.*
import ifac.td.taxi.viewmodel.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
