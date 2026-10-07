package ifac.td.taxi.compose.viewmodel
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
import ifac.td.taxi.viewmodel.LoginUserRedSysViewModel
import ifac.td.taxi.ui.custom.button.ButtonType
import com.interfacom.sdk.taximeter.licensing.rest.user.ChangePasswordPinView
import com.interfacom.sdk.taximeter.licensing.rest.user.UserModule
import com.interfacom.sdk.taximeter.licensing.rest.user.UserPresenter
import androidx.compose.ui.text.input.PasswordVisualTransformation
import android.app.Application
import android.content.Intent
import android.content.Context
import android.content.ActivityNotFoundException
import android.media.ToneGenerator
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.annotation.RawRes
import android.net.Uri
import ifac.td.taxi.viewmodel.BaseViewModel
import ifac.td.taxi.framework.util.Logs
import android.provider.Settings
import ifac.td.taxi.domain.usecase.oldv2.MigrationV2UseCase
import ifac.td.taxi.framework.sdk.ExternalBridgeInterface
import ifac.td.taxi.repository.connections.receivers.utils.NetworkUtils.Companion.isInternetConnectionAvailable
import android.widget.Toast
import ifac.td.taxi.ui.screen.LoginUserRedSysScreen
// # Block 92-4: import android.app.Application
class LoginUserRedSysComposeViewModel(
    private val redSysUseCase: RedSysUseCase,
    application: Application
) : AndroidViewModel(application) {
    lateinit var buttonsState : LoginUserRedSysButtonsState
    lateinit var dialogState : LoginUserRedSysDialogState
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
                    userError = if (!validUser) context.getString(R.string.incorrect_login) else null,
                    passwordError = if (!validPassword) context.getString(R.string.incorrect_login) else null
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
                userError = context.getString(R.string.incorrect_login),
                buttons = LoginUserRedSysButtonsState.enabledGreen()
            )
        }
    }
    fun onDialogDismiss() {
        _uiState.update { it.copy(dialog = null) }
    }
}
