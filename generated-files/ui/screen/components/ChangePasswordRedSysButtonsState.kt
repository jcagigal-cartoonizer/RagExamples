package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.ChangePasswordRedSysButtonsState
import ifac.td.taxi.ui.screen.components.ChangePasswordRedSysUiState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 129-2: import androidx.compose.runtime.Composable
@Stable
data class ChangePasswordRedSysButtonsState(
    val cancelVisible: Boolean = true,
    val acceptVisible: Boolean = true,
    val acceptEnabled: Boolean = true,
    val acceptLoading: Boolean = false,
)
object ChangePasswordRedSysButtonsStateHolder {
    @Composable
    fun from(uiState: ChangePasswordRedSysUiState): ChangePasswordRedSysButtonsState {
        val canAccept =
            uiState.user.isNotBlank() &&
            uiState.password.isNotBlank() &&
            uiState.newPassword.isNotBlank() &&
            uiState.repeatNewPassword.isNotBlank() &&
            uiState.newPassword == uiState.repeatNewPassword &&
            !uiState.isLoading
        return ChangePasswordRedSysButtonsState(
            cancelVisible = true,
            acceptVisible = true,
            acceptEnabled = canAccept,
            acceptLoading = uiState.isLoading
        )
    }
}
