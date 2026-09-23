package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.ChangeUserPasswordButtonsState = ChangeUserPasswordButtonsState
import ifac.td.taxi.ui.screen.components.ChangeUserPasswordUiState
import ifac.td.taxi.ui.screen.components.ChangeUserPasswordDialogState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 4-1: import androidx.compose.runtime.Immutable
@Immutable
data class ChangeUserPasswordUiState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val repeatPassword: String = "",
    val isLoading: Boolean = false,
    val buttonsState: ChangeUserPasswordButtonsState = ChangeUserPasswordButtonsState(),
    val dialogState: ChangeUserPasswordDialogState? = null
)
@Immutable
sealed interface ChangeUserPasswordUiEffect {
    data object NavigateBack : ChangeUserPasswordUiEffect
    data class ShowDialog(val dialogState: ChangeUserPasswordDialogState) : ChangeUserPasswordUiEffect
    data object OpenUserPresenter : ChangeUserPasswordUiEffect
}
@Immutable
data class ChangeUserPasswordDialogState(
    val title: String,
    val description: String? = null,
    val buttons: List<ChangeUserPasswordDialogButton> = listOf(ChangeUserPasswordDialogButton.Accept)
)
enum class ChangeUserPasswordDialogButton {
    Accept
}
