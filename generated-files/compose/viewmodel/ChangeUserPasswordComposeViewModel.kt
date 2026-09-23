package ifac.td.taxi.compose.viewmodel
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 94-4: import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.interfacom.sdk.taximeter.licensing.rest.user.ChangePasswordPinView
import com.interfacom.sdk.taximeter.licensing.rest.user.UserModule
import com.interfacom.sdk.taximeter.licensing.rest.user.UserPresenter
import ifac.td.taxi.domain.usecase.SessionUseCase
import ifac.td.taxi.ui.screen.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
class ChangeUserPasswordComposeViewModel(
    application: Application,
    private val sessionUseCase: SessionUseCase
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(ChangeUserPasswordUiState(
        buttonsState = ChangeUserPasswordButtonsState(
            accept = ChangeUserPasswordButtonStyles.Primary,
            cancel = ChangeUserPasswordButtonStyles.Secondary
        )
    ))
    val uiState = _uiState.asStateFlow()
    private val _uiEffects = MutableSharedFlow<ChangeUserPasswordUiEffect>()
    val uiEffects: SharedFlow<ChangeUserPasswordUiEffect> = _uiEffects.asSharedFlow()
    private var pendingNewPassword: String? = null
    fun onCurrentPasswordChanged(value: String) {
        _uiState.update { it.copy(currentPassword = value) }
    }
    fun onNewPasswordChanged(value: String) {
        _uiState.update { it.copy(newPassword = value) }
    }
    fun onRepeatPasswordChanged(value: String) {
        _uiState.update { it.copy(repeatPassword = value) }
    }
    fun onAcceptClicked() {
        val state = _uiState.value
        val validationError = validate(state.currentPassword, state.newPassword, state.repeatPassword)
        if (validationError != null) {
            viewModelScope.launch {
                _uiEffects.emit(
                    ChangeUserPasswordUiEffect.ShowDialog(
                        ChangeUserPasswordDialogState(
                            title = validationError,
                            buttons = listOf(ChangeUserPasswordDialogButton.Accept)
                        )
                    )
                )
            }
            return
        }
        pendingNewPassword = state.newPassword.trim()
        viewModelScope.launch {
            if (sessionUseCase.isUserLoggedIn()) {
                val presenter = UserModule.provideUserPresenter(
                    object : ChangePasswordPinView {
                        override fun updateSuccess() {
                            viewModelScope.launch {
                                _uiEffects.emit(
                                    ChangeUserPasswordUiEffect.ShowDialog(
                                        ChangeUserPasswordDialogState(
                                            title = "Password changed successfully",
                                            buttons = listOf(ChangeUserPasswordDialogButton.Accept)
                                        )
                                    )
                                )
                            }
                        }
                        override fun updateFailure() {
                            viewModelScope.launch {
                                _uiEffects.emit(
                                    ChangeUserPasswordUiEffect.ShowDialog(
                                        ChangeUserPasswordDialogState(
                                            title = "Error changing password",
                                            buttons = listOf(ChangeUserPasswordDialogButton.Accept)
                                        )
                                    )
                                )
                            }
                        }
                    },
                    getApplication()
                )
                changeUserPassword(presenter, pendingNewPassword.orEmpty())
            } else {
                _uiEffects.emit(
                    ChangeUserPasswordUiEffect.ShowDialog(
                        ChangeUserPasswordDialogState(
                            title = "User error",
                            buttons = listOf(ChangeUserPasswordDialogButton.Accept)
                        )
                    )
                )
            }
        }
    }
    fun onCancelClicked() {
        viewModelScope.launch { _uiEffects.emit(ChangeUserPasswordUiEffect.NavigateBack) }
    }
    fun onDialogAccepted() {
        viewModelScope.launch { _uiEffects.emit(ChangeUserPasswordUiEffect.NavigateBack) }
    }
    fun validate(current: String, newPwd: String, repeated: String): String? {
        return when {
            current.isBlank() || newPwd.isBlank() || repeated.isBlank() ->
                "Please fill all inputs"
            newPwd != repeated ->
                "Passwords do not match"
            current == newPwd ->
                "New password must be different from old password"
            else -> null
        }
    }
    fun changeUserPassword(userPresenter: UserPresenter, newPassword: String) {
        userPresenter.changePassword(newPassword, getApplication())
    }
}
