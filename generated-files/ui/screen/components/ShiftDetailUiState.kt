package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.ShiftDetailUiState
import ifac.td.taxi.ui.screen.components.ShiftDetailCustomDialogState
import ifac.td.taxi.ui.screen.components.ShiftDetailButtonsState = ShiftDetailButtonsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 13-1: import android.content.Intent
data class ShiftDetailUiState(
    val shiftId: Long = 0L,
    val trips: List<Trip> = emptyList(),
    val sortState: SortStateUi = SortStateUi(),
    val buttons: ShiftDetailButtonsState = ShiftDetailButtonsState(),
    val dialog: ShiftDetailCustomDialogState? = null
)
data class SortStateUi(
    val option: ShiftOrderOptions = ShiftOrderOptions.NONE,
    val isAscending: Boolean? = null
)
data class ShiftDetailDialogState(
    val title: String,
    val message: String,
    val positiveText: String = "OK",
    val negativeText: String? = null
)
data class ShiftDetailCustomDialogState(
    val title: String,
    val message: String,
    val positiveButtonText: String,
    val negativeButtonText: String? = null
)
sealed interface ShiftDetailUiEffect {
    data class OpenIntent(val intent: Intent) : ShiftDetailUiEffect
    data class ShowDialog(val dialog: ShiftDetailCustomDialogState) : ShiftDetailUiEffect
    data class ShowToast(val messageRes: Int) : ShiftDetailUiEffect
}
