package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.ShiftsUiState
import ifac.td.taxi.ui.screen.components.ShiftsButtonsState = ShiftsButtonsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 211-2: import ifac.td.taxi.repository.room.entities.ShiftEntity
data class ShiftsUiState(
    val shifts: List<ShiftEntity> = emptyList(),
    val isLoading: Boolean = false,
    val hasShifts: Boolean = false,
    val page: Int = 0,
    val sortState: SortState = SortState(ShiftOrderOptions.NONE),
    val buttonsState: ShiftsButtonsState = ShiftsButtonsState(),
    val selectedShiftIds: Set<Long> = emptySet(),
    val dialog: DialogSpec? = null
)
sealed interface ShiftsUiEvent {
    data object ScreenResumed : ShiftsUiEvent
    data object LoadNextPage : ShiftsUiEvent
    data object ToggleMenu : ShiftsUiEvent
    data object ExportSelected : ShiftsUiEvent
    data object DeleteSelected : ShiftsUiEvent
    data object SortById : ShiftsUiEvent
    data object SortByAmount : ShiftsUiEvent
    data class ItemClicked(val shift: ShiftEntity) : ShiftsUiEvent
    data object DialogDismiss : ShiftsUiEvent
    data object DialogConfirmDelete : ShiftsUiEvent
}
sealed interface ShiftsUiEffect {
    data class OpenIntent(val intent: android.content.Intent) : ShiftsUiEffect
    data class NavigateToShiftDetail(val shift: ShiftEntity) : ShiftsUiEffect
}
sealed interface DialogSpec {
    data class Message(val title: String, val message: String) : DialogSpec
    data class ConfirmDelete(val title: String, val message: String) : DialogSpec
}
enum class ArrowState { Hidden, Up, Down }
