package ifac.td.taxi.compose.viewmodel
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 92-4: import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import es.redsys.paysys.Utils.RedCLSErrorCodes
import ifac.td.taxi.domain.model.RedSysLoginResponse
import ifac.td.taxi.domain.usecase.RedSysUseCase
import ifac.td.taxi.framework.TemporalData
import ifac.td.taxi.ui.screen.redsys.LoginUserRedSysButtonsState
import ifac.td.taxi.ui.screen.redsys.LoginUserRedSysDialogState
import ifac.td.taxi.ui.screen.redsys.LoginUserRedSysUiEffect
import ifac.td.taxi.ui.screen.redsys.LoginUserRedSysUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
class LoginUserRedSysComposeViewModel(
    private val redSysUseCase: RedSysUseCase,
    application: Application
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(LoginUserRedSysUiState())
    val uiState: StateFlow<LoginUserRedSysUiState> = _uiState.asStateFlow()
    private val _uiEffect = MutableSharedFlow<LoginUserRedSysUiEffect>()
    val uiEffect: SharedFlow<LoginUserRedSysUiEffect> = _uiEffect.asSharedFlow()
    fun getUserRedSys() {
        viewModelScope.launch {
            val user = redSysUseCase.getUsernameRedSys().orEmpty()
            _uiState.update {
                it.copy(
                    user = user,
                    password = TemporalData.passwordRedSys
                )
            }
        }
    }
    fun onUserChange(value: String) {
        _uiState.update { it.copy(user = value, userError = null) }
    }
    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null) }
    }
    fun onCancelClick() {
        viewModelScope.launch {
            _uiEffect.emit(LoginUserRedSysUiEffect.NavigateBack)
        }
    }
    fun onAcceptClick() {
        val current = _uiState.value
        val validUser = current.user.isNotBlank()
        val validPassword = current.password.isNotBlank()
        if (!validUser || !validPassword) {
            _uiState.update {
                it.copy(
                    userError = if (!validUser) getApplication<Application>().getString(R.string.incorrect_login) else null,
                    passwordError = if (!validPassword) getApplication<Application>().getString(R.string.incorrect_login) else null
                )
            }
            return
        }
        _uiState.update {
            it.copy(
                isLoading = true,
                buttons = LoginUserRedSysButtonsState.loading(),
                userError = null,
                passwordError = null
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            redSysUseCase.configureRedSys()
            val login = redSysUseCase.loginRedSys(
                username = current.user,
                password = current.password
            )
            when (login) {
                is RedSysLoginResponse.Success -> {
                    if (login.merchantList.isNotEmpty()) {
                        redSysUseCase.saveUsernameRedSys(current.user)
                        if (current.password.isNotEmpty()) {
                            TemporalData.passwordRedSys = current.password
                        }
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                buttons = LoginUserRedSysButtonsState.enabledGreen()
                            )
                        }
                        _uiEffect.emit(LoginUserRedSysUiEffect.ShowToast(R.string.toast_red_sys_login_ok))
                        _uiEffect.emit(LoginUserRedSysUiEffect.NavigateBack)
                    } else {
                        handleLoginFailure()
                    }
                }
                is RedSysLoginResponse.Error -> {
                    if (login.errorCode == RedCLSErrorCodes.STATUS_KO_FORMATO_RESP_LOGIN_PWD_CAD) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                buttons = LoginUserRedSysButtonsState.enabledGreen()
                            )
                        }
                        _uiEffect.emit(LoginUserRedSysUiEffect.NavigateToChangePassword)
                    } else {
                        handleLoginFailure()
                    }
                }
            }
        }
    }
    private suspend fun handleLoginFailure() {
        _uiState.update {
            it.copy(
                isLoading = false,
                userError = getApplication<Application>().getString(R.string.incorrect_login),
                buttons = LoginUserRedSysButtonsState.enabledGreen()
            )
        }
    }
    fun onDialogDismiss() {
        _uiState.update { it.copy(dialog = null) }
    }
}
