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
// # Block 387-5: import androidx.compose.runtime.Immutable
@Immutable
data class LoginUserUiState(
    val user: String = "",
    val password: String = "",
    val userError: String? = null,
    val userEnabled: Boolean = true,
    val passwordEnabled: Boolean = true,
    val changePasswordVisible: Boolean = false,
    val hasSettingsPassword: Boolean = false,
    val fromMigration: Boolean = false,
) {
    val userValid: Boolean get() = user.isNotBlank()
    val passwordValid: Boolean get() = password.isNotBlank()
}
sealed interface LoginUserEvent {
    data object CancelClicked : LoginUserEvent
    data object AcceptClicked : LoginUserEvent
    data object ChangePasswordClicked : LoginUserEvent
    data object ChangeUserClicked : LoginUserEvent
    data object AutoDownloadRequested : LoginUserEvent
    data class UserChanged(val value: String) : LoginUserEvent
    data class PasswordChanged(val value: String) : LoginUserEvent
    data class SettingsPasswordSubmitted(val pin: String) : LoginUserEvent
}
sealed interface LoginUserUiEffect {
    data object HideKeyboard : LoginUserUiEffect
    data object NavigateBack : LoginUserUiEffect
    data object NavigateToChangePassword : LoginUserUiEffect
    data object OpenSettingsPasswordDialog : LoginUserUiEffect
    data object CloseSettingsPasswordDialog : LoginUserUiEffect
    data object StartBravoService : LoginUserUiEffect
    data class SaveUserCredentials(val user: String, val password: String) : LoginUserUiEffect
    data class ShowToast(val messageRes: Int) : LoginUserUiEffect
    data class ShowIncorrectLoginError(val messageRes: Int) : LoginUserUiEffect
    data class UpdateProgressBar(val visible: Int, val progress: Int) : LoginUserUiEffect
    data object DownloadBravoConfiguration : LoginUserUiEffect
    data object NavigateBackAfterDownload : LoginUserUiEffect
}
@Immutable
data class LoginUserButtonsState(
    val cancel: LoginUserComposeButtonState = LoginUserComposeButtonState.enabled(label = "Cancelar"),
    val accept: LoginUserComposeButtonState = LoginUserComposeButtonState.enabled(label = "Aceptar"),
    val changeUser: LoginUserComposeButtonState = LoginUserComposeButtonState.hidden(label = "Cambiar usuario"),
) {
    companion object {
        fun initial() = LoginUserButtonsState(
            cancel = LoginUserComposeButtonState.enabled(label = "Cancelar"),
            accept = LoginUserComposeButtonState.enabled(label = "Aceptar"),
            changeUser = LoginUserComposeButtonState.hidden(label = "Cambiar usuario")
        )
    }
    fun copyFromUiState(uiState: LoginUserUiState): LoginUserButtonsState {
        val acceptState = when {
            uiState.userValid && uiState.passwordValid -> LoginUserComposeButtonState.enabled("Aceptar")
            else -> LoginUserComposeButtonState.enabled("Aceptar")
        }
        val changeUserState = if (uiState.hasSettingsPassword) {
            LoginUserComposeButtonState.enabled("Cambiar usuario")
        } else {
            LoginUserComposeButtonState.hidden("Cambiar usuario")
        }
        return copy(
            accept = acceptState,
            changeUser = changeUserState
        )
    }
}
@Immutable
data class LoginUserComposeButtonState(
    val visible: Boolean,
    val enabled: Boolean,
    val loading: Boolean,
    val label: String,
    val background: ButtonBackground,
    val textColor: Long,
    val borderColor: Long,
) {
    companion object {
        fun enabled(label: String) = LoginUserComposeButtonState(
            visible = true,
            enabled = true,
            loading = false,
            label = label,
            background = ButtonBackground.Green,
            textColor = 0xFFFFFFFF,
            borderColor = 0xFF2E7D32
        )
        fun disabled(label: String) = LoginUserComposeButtonState(
            visible = true,
            enabled = false,
            loading = false,
            label = label,
            background = ButtonBackground.Gray,
            textColor = 0xFF9E9E9E,
            borderColor = 0xFF9E9E9E
        )
        fun hidden(label: String) = LoginUserComposeButtonState(
            visible = false,
            enabled = false,
            loading = false,
            label = label,
            background = ButtonBackground.Green,
            textColor = 0xFFFFFFFF,
            borderColor = 0xFF2E7D32
        )
    }
    fun toLoading() = copy(loading = true, enabled = false, background = ButtonBackground.Green)
    fun disabled() = copy(enabled = false, loading = false, background = ButtonBackground.Gray)
}
enum class ButtonBackground {
    Green, Gray
}
