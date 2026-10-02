package ifac.td.taxi.ui.screen
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
// # Block 77-2: import androidx.compose.foundation.layout.*
@Composable
fun LoginUserScreen(
    uiState: LoginUserUiState,
    buttonsState: LoginUserButtonsState,
    dialogState: LoginUserDialogState?,
    onEvent: (LoginUserEvent) -> Unit,
    onDialogDismiss: () -> Unit,
    onDialogConfirmPin: (String) -> Unit,
    autoDownloadConfigurationFromMigration: Boolean,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LoginUserFields(
                user = uiState.user,
                password = uiState.password,
                userEnabled = uiState.userEnabled,
                passwordEnabled = uiState.passwordEnabled,
                userError = uiState.userError,
                changePasswordVisible = uiState.changePasswordVisible,
                onUserChange = { onEvent(LoginUserEvent.UserChanged(it)) },
                onPasswordChange = { onEvent(LoginUserEvent.PasswordChanged(it)) },
                onChangePasswordClick = { onEvent(LoginUserEvent.ChangePasswordClicked) }
            )
            Spacer(modifier = Modifier.height(8.dp))
            LoginUserButtons(
                state = buttonsState,
                onCancelClick = { onEvent(LoginUserEvent.CancelClicked) },
                onAcceptClick = { onEvent(LoginUserEvent.AcceptClicked) },
                onChangeUserClick = { onEvent(LoginUserEvent.ChangeUserClicked) }
            )
        }
        when (dialogState) {
            LoginUserDialogState.SettingsPassword -> {
                ComposeLoginUserComposeFragmentLoginUserCustomDialog(
                    title = "PIN actual",
                    description = "Introduce el PIN actual",
                    editText = "",
                    editTextTypePin = true,
                    editTextMaxLength = 4,
                    buttons = listOf(ButtonType.CANCEL, ButtonType.ACCEPT),
                    onDismiss = onDialogDismiss,
                    onConfirm = onDialogConfirmPin
                )
            }
            null -> Unit
        }
    }
    LaunchedEffect(autoDownloadConfigurationFromMigration) {
        if (autoDownloadConfigurationFromMigration) {
            onEvent(LoginUserEvent.AutoDownloadRequested)
        }
    }
}
