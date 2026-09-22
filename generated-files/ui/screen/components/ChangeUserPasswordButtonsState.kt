package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// import androidx.compose.runtime.Immutable
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
// // # Block 35-2: import androidx.compose.runtime.Immutable
// // import androidx.compose.runtime.Immutable
@Immutable
data class ChangeUserPasswordButtonsState(
    val accept: ButtonAppearance = ButtonAppearance.Primary,
    val cancel: ButtonAppearance = ButtonAppearance.Secondary,
    val showAccept: Boolean = true,
    val showCancel: Boolean = true,
    val acceptEnabled: Boolean = true,
    val cancelEnabled: Boolean = true
) {
    companion object {
        fun default() = ChangeUserPasswordButtonsState()
    }
}
@Immutable
data class ButtonAppearance(
    val backgroundColor: Long,
    val contentColor: Long,
    val strokeColor: Long? = null,
    val strokeWidthDp: Int = 0,
    val cornerRadiusDp: Int = 8,
    val minHeightDp: Int = 48,
    val textStyle: ButtonTextStyle = ButtonTextStyle.Default
)
enum class ButtonTextStyle {
    Default,
    Emphasis
}
