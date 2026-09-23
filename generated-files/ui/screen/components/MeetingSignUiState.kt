package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 11-1: import androidx.annotation.ColorInt
import androidx.annotation.ColorInt
data class MeetingSignUiState(
    val message: String = "",
    @ColorInt val textColor: Int = 0,
    @ColorInt val backgroundColor: Int = 0,
    val isMenuOpen: Boolean = false,
    val dialogState: MeetingSignDialogState? = null,
    val buttonsState: MeetingSignButtonsState = MeetingSignButtonsState(),
    val isInSettings: Boolean = false,
    val fromDispatch: Boolean = false,
    val lastShiftStatus: Int? = null,
)
data class MeetingSignDialogState(
    val visible: Boolean = false,
    val title: String = "",
    val hint: String = "",
    val editText: String? = null,
    val isCancellable: Boolean = true,
)
sealed interface MeetingSignUiEffect {
    data object NavigateBack : MeetingSignUiEffect
    data object NavigateToInfoDispatch : MeetingSignUiEffect
    data class ShowToast(val messageRes: Int) : MeetingSignUiEffect
    data class OpenEditDialog(
        val isCancellable: Boolean,
        val currentName: String?
    ) : MeetingSignUiEffect
}
