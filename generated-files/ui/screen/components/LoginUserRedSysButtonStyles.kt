package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// // import androidx.compose.ui.graphics.Color
data class LoginUserRedSysUiState(
    val user: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val userError: String? = null,
    val passwordError: String? = null,
    val dialog: LoginUserRedSysDialogState? = null,
    val buttons: LoginUserRedSysButtonsState = LoginUserRedSysButtonsState()
)
sealed interface LoginUserRedSysUiEffect {
    data object NavigateBack : LoginUserRedSysUiEffect
    data object NavigateToChangePassword : LoginUserRedSysUiEffect
    data class ShowToast(val resId: Int) : LoginUserRedSysUiEffect
}
sealed interface LoginUserRedSysDialogState {
    data class Error(
        val title: String,
        val message: String,
        val confirmText: String = "OK"
    ) : LoginUserRedSysDialogState
}
