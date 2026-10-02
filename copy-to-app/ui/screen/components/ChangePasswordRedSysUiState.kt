package ifac.td.taxi.ui.screen.components
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
// # Block 5-1: import android.app.Application
data class ChangePasswordRedSysUiState(
    val user: String = "",
    val password: String = "",
    val newPassword: String = "",
    val repeatNewPassword: String = "",
    val isLoading: Boolean = false,
    val isAcceptEnabled: Boolean = true,
    val dialog: ChangePasswordRedSysDialogState? = null,
)
data class ChangePasswordRedSysDialogState(
    val title: String? = null,
    val description: String,
    val positiveButtonText: String = "OK"
)
sealed interface ChangePasswordRedSysUiEffect {
    data object NavigateBack : ChangePasswordRedSysUiEffect
    data class ShowDialog(val dialog: ChangePasswordRedSysDialogState) : ChangePasswordRedSysUiEffect
}
class ChangePasswordRedSysComposeViewModel(
    private val redSysUseCase: RedSysUseCase,
    application: Application,
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(ChangePasswordRedSysUiState())
    val uiState: StateFlow<ChangePasswordRedSysUiState> = _uiState.asStateFlow()
    private val _uiEffect = MutableSharedFlow<ChangePasswordRedSysUiEffect>()
    val uiEffect: SharedFlow<ChangePasswordRedSysUiEffect> = _uiEffect.asSharedFlow()
    fun onUserChanged(value: String) = _uiState.update { it.copy(user = value) }
    fun onPasswordChanged(value: String) = _uiState.update { it.copy(password = value) }
    fun onNewPasswordChanged(value: String) = _uiState.update { it.copy(newPassword = value) }
    fun onRepeatNewPasswordChanged(value: String) = _uiState.update { it.copy(repeatNewPassword = value) }
    fun onCancelClick() {
        viewModelScope.launch {
            _uiEffect.emit(ChangePasswordRedSysUiEffect.NavigateBack)
        }
    }
    fun onAcceptClick() {
        val state = _uiState.value
        if (!isFormValid(state)) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val context = getApplication<Application>()
            val clsChange = RedCLSChangePassData(
                context,
                state.user,
                state.password,
                state.newPassword
            )
            val result = redSysUseCase.changePassword(clsChange)
            _uiState.update { it.copy(isLoading = false) }
            if (result != null && result.code == RedCLSErrorCodes.STATUS_OK) {
                _uiEffect.emit(
                    ChangePasswordRedSysUiEffect.ShowDialog(
                        ChangePasswordRedSysDialogState(
                            title = context.getString(ifac.td.taxi.R.string.red_sys_title),
                            description = context.getString(ifac.td.taxi.R.string.dialog_change_password),
                            positiveButtonText = "OK"
                        )
                    )
                )
            } else {
                _uiEffect.emit(
                    ChangePasswordRedSysUiEffect.ShowDialog(
                        ChangePasswordRedSysDialogState(
                            title = context.getString(ifac.td.taxi.R.string.red_sys_title),
                            description = result?.desc ?: context.getString(ifac.td.taxi.R.string.no_response_error),
                            positiveButtonText = "OK"
                        )
                    )
                )
            }
        }
    }
    fun onDialogAccepted() {
        viewModelScope.launch {
            _uiEffect.emit(ChangePasswordRedSysUiEffect.NavigateBack)
        }
    }
    fun isFormValid(state: ChangePasswordRedSysUiState): Boolean {
        // Replace with your exact validation rules if needed.
        return state.user.isNotBlank() &&
            state.password.isNotBlank() &&
            state.newPassword.isNotBlank() &&
            state.repeatNewPassword.isNotBlank() &&
            state.newPassword == state.repeatNewPassword
    }
}
