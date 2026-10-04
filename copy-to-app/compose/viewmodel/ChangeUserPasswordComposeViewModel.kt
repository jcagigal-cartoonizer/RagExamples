package ifac.td.taxi.compose.viewmodel
import  android.app.Application
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
import ifac.td.taxi.ui.screen.ChangeUserPasswordScreen
// # Block 10-1: import android.app.Application
class ChangeUserPasswordComposeViewModel(
    application: Application,
    private val sessionUseCase: SessionUseCase,
) : AndroidViewModel(application) {
    private val context = application
    private val _uiState = MutableStateFlow(ChangeUserPasswordUiState())
    val uiState = _uiState.asStateFlow()
    private val _uiEffect = MutableSharedFlow<ChangeUserPasswordUiEffect>()
    val uiEffect: SharedFlow<ChangeUserPasswordUiEffect> = _uiEffect.asSharedFlow()
    fun onActualPasswordChanged(value: String) {
        _uiState.update { it.copy(actualPassword = value, errorMessage = null) }
    }
    fun onNewPasswordChanged(value: String) {
        _uiState.update { it.copy(newPassword = value, errorMessage = null) }
    }
    fun onRepeatPasswordChanged(value: String) {
        _uiState.update { it.copy(repeatPassword = value, errorMessage = null) }
    }
    fun onCancelClicked() {
        viewModelScope.launch {
            _uiEffect.emit(ChangeUserPasswordUiEffect.NavigateBack)
        }
    }
    fun onAcceptClicked() {
        val state = _uiState.value
        when {
            state.actualPassword.isBlank() ||
                state.newPassword.isBlank() ||
                state.repeatPassword.isBlank() -> {
                emitDialog(
                    title = "Please fill in all inputs"
                )
            }
            state.newPassword != state.repeatPassword -> {
                emitDialog(
                    title = "New passwords do not match"
                )
            }
            state.actualPassword == state.newPassword -> {
                emitDialog(
                    title = "New password must be different from old password"
                )
            }
            else -> {
                requestUserPresenter()
            }
        }
    }
    fun requestUserPresenter() {
        viewModelScope.launch {
            if (sessionUseCase.isUserLoggedIn()) {
                val presenter = UserModule.provideUserPresenter(
                    object : ChangePasswordPinView {
                        override fun updateSuccess() {
                            viewModelScope.launch {
                                _uiEffect.emit(
                                    ChangeUserPasswordUiEffect.ShowDialog(
                                        ChangeUserPasswordDialogModel(
                                            title = "Password changed successfully",
                                            buttons = listOf(DialogButton.Accept)
                                        )
                                    )
                                )
                            }
                        }
                        override fun updateFailure() {
                            viewModelScope.launch {
                                _uiEffect.emit(
                                    ChangeUserPasswordUiEffect.ShowDialog(
                                        ChangeUserPasswordDialogModel(
                                            title = "Error changing password",
                                            buttons = listOf(DialogButton.Accept)
                                        )
                                    )
                                )
                            }
                        }
                    },
                    context
                )
                changeUserPassword(presenter, _uiState.value.newPassword.trim())
            } else {
                _uiEffect.emit(
                    ChangeUserPasswordUiEffect.ShowDialog(
                        ChangeUserPasswordDialogModel(
                            title = "User error",
                            buttons = listOf(DialogButton.Accept)
                        )
                    )
                )
            }
        }
    }
    fun changeUserPassword(userPresenter: UserPresenter, newPassword: String) {
        userPresenter.changePassword(newPassword, context)
    }
    fun emitDialog(title: String) {
        viewModelScope.launch {
            _uiEffect.emit(
                ChangeUserPasswordUiEffect.ShowDialog(
                    ChangeUserPasswordDialogModel(
                        title = title,
                        buttons = listOf(DialogButton.Accept)
                    )
                )
            )
        }
    }
}
data class ChangeUserPasswordUiState(
    val actualPassword: String = "",
    val newPassword: String = "",
    val repeatPassword: String = "",
)
sealed interface ChangeUserPasswordUiEffect {
    data class ShowDialog(val model: ChangeUserPasswordDialogModel) : ChangeUserPasswordUiEffect
    data object NavigateBack : ChangeUserPasswordUiEffect
}
data class ChangeUserPasswordDialogModel(
    val title: String,
    val description: String? = null,
    val buttons: List<DialogButton> = listOf(DialogButton.Accept)
)
enum class DialogButton {
    Accept
}
