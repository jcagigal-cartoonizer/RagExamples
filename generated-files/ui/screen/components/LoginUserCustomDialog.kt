package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import ifac.td.taxi.ui.custom.button.ButtonType
import ifac.td.taxi.ui.screen.state.LoginUserCustomDialog
import ifac.td.taxi.viewmodel.LoginUserUiEffect
import ifac.td.taxi.viewmodel.LoginUserUiEvent
import ifac.td.taxi.viewmodel.LoginUserUiState
import ifac.td.taxi.viewmodel.LoginUserComposeViewModel
@Composable
fun LoginUserRoute(
    navController: NavController,
    viewModel: LoginUserComposeViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showConfigPasswordDialog by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        viewModel.onEvent(LoginUserUiEvent.ScreenStarted)
    }
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                LoginUserUiEffect.NavigateBack -> navController.popBackStack()
                LoginUserUiEffect.NavigateToChangePassword -> {
                    navController.navigate(R.id.action_loginUserFragment_to_changeUserPasswordFragment)
                }
                LoginUserUiEffect.OpenSettingsPasswordDialog -> {
                    showConfigPasswordDialog = true
                }
                LoginUserUiEffect.HideSettingsPasswordDialog -> {
                    showConfigPasswordDialog = false
                }
                LoginUserUiEffect.ShowIncorrectPinToast -> {
                    android.widget.Toast.makeText(
                        context,
                        context.getString(R.string.pin_incorrecto),
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
                LoginUserUiEffect.StartBravoService -> {
                }
                LoginUserUiEffect.DownloadBravoConfiguration -> {
                    viewModel.onEvent(LoginUserUiEvent.DownloadBravoConfiguration)
                }
                LoginUserUiEffect.AutoFillCredentialsAndMigrate -> {
                    viewModel.onEvent(LoginUserUiEvent.AutoDownloadMigration)
                }
            }
        }
    }
    LoginUserScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onChangePasswordClick = {
            viewModel.onEvent(LoginUserUiEvent.ChangePasswordClicked)
        },
        onCancelClick = {
            viewModel.onEvent(LoginUserUiEvent.CancelClicked)
        },
        onAcceptClick = {
            viewModel.onEvent(LoginUserUiEvent.AcceptClicked)
        },
        onChangeUserClick = {
            showConfigPasswordDialog = true
        },
        showConfigPasswordDialog = showConfigPasswordDialog,
        onDismissConfigPasswordDialog = {
            showConfigPasswordDialog = false
        },
        onConfirmConfigPasswordDialog = { pin ->
            viewModel.onEvent(LoginUserUiEvent.ConfigurationPasswordEntered(pin))
            showConfigPasswordDialog = false
        },
        modifier = modifier
    )
}
@Composable
fun LoginUserScreen(
    state: LoginUserUiState,
    onEvent: (LoginUserUiEvent) -> Unit,
    onChangePasswordClick: () -> Unit,
    onCancelClick: () -> Unit,
    onAcceptClick: () -> Unit,
    onChangeUserClick: () -> Unit,
    showConfigPasswordDialog: Boolean,
    onDismissConfigPasswordDialog: () -> Unit,
    onConfirmConfigPasswordDialog: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = state.user,
                onValueChange = { onEvent(LoginUserUiEvent.UserChanged(it)) },
                enabled = state.editTextsEnabled,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Usuario") },
                isError = state.userError != null,
                supportingText = {
                    state.userError?.let { Text(it, color = Color.Red) }
                }
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = state.password,
                onValueChange = { onEvent(LoginUserUiEvent.PasswordChanged(it)) },
                enabled = state.editTextsEnabled,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(Modifier.height(12.dp))
            if (state.showChangePasswordText) {
                TextButton(onClick = onChangePasswordClick) {
                    Text("Cambiar contraseña")
                }
            }
            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (state.buttons.changeUser.visible) {
                    ComposeCustomButton(
                        text = state.buttons.changeUser.text,
                        style = state.buttons.changeUser.style,
                        onClick = onChangeUserClick,
                        modifier = Modifier.weight(1f)
                    )
                }
                ComposeCustomButton(
                    text = state.buttons.cancel.text,
                    style = state.buttons.cancel.style,
                    onClick = onCancelClick,
                    modifier = Modifier.weight(1f)
                )
                ComposeCustomButton(
                    text = state.buttons.accept.text,
                    style = state.buttons.accept.style,
                    onClick = onAcceptClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        if (state.progress.visible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                LinearProgressIndicator(
                    progress = { state.progress.percent / 100f },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        if (showConfigPasswordDialog) {
            LoginUserCustomDialog(
                title = "PIN actual",
                description = "Introduce el PIN actual",
                pinMode = true,
                maxLength = 4,
                onDismiss = onDismissConfigPasswordDialog,
                onAccept = onConfirmConfigPasswordDialog
            )
        }
    }
}
