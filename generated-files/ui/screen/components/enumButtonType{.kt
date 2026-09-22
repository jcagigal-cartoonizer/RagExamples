package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// // # Block 237-4: import androidx.compose.foundation.layout.*
// // import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
enum class ButtonType {
    ACCEPT,
    CANCEL
}
data class CustomDialogModel(
    val title: String? = null,
    val description: String? = null,
    val buttons: List<ButtonType> = listOf(ButtonType.ACCEPT)
)
data class CustomDialogResponse(
    val buttonPressed: ButtonType
)
@Composable
fun ChangePasswordRedSysChangePasswordRedSysCustomDialog(
    model: CustomDialogModel,
    onResponse: (CustomDialogResponse) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { model.title?.let { Text(text = it) } },
        text = { model.description?.let { Text(text = it) } },
        confirmButton = {
            if (ButtonType.ACCEPT in model.buttons) {
                TextButton(onClick = { onResponse(CustomDialogResponse(ButtonType.ACCEPT)) }) {
                    Text("Accept")
                }
            }
        },
        dismissButton = {
            if (ButtonType.CANCEL in model.buttons) {
                TextButton(onClick = { onResponse(CustomDialogResponse(ButtonType.CANCEL)) }) {
                    Text("Cancel")
                }
            }
        }
    )
}
// // # Block 286-5: import androidx.compose.foundation.layout.*
// // import androidx.compose.foundation.layout.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.runtime.LaunchedEffect
import ifac.td.taxi.viewmodel.*
import ifac.td.taxi.ui.custom.dialog.compose.*
@Composable
fun ChangePasswordRedSysScreen(
    viewModel: ChangePasswordRedSysComposeViewModel,
    navigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val buttonsState = ChangePasswordRedSysButtonsStateHolder.from(uiState)
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(viewModel.uiEffect, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.uiEffect.collectLatest { effect ->
                when (effect) {
                    ChangePasswordRedSysUiEffect.NavigateBack -> navigateBack()
                }
            }
        }
    }
    uiState.dialog?.let { dialog ->
        ChangePasswordRedSysCustomDialog(
            model = CustomDialogModel(
                title = dialog.title,
                description = dialog.description,
                buttons = listOf(ButtonType.ACCEPT)
            ),
            onResponse = { response ->
                when (response.buttonPressed) {
                    ButtonType.ACCEPT -> viewModel.onEvent(ChangePasswordRedSysUiEvent.DialogAccepted)
                    ButtonType.CANCEL -> viewModel.onEvent(ChangePasswordRedSysUiEvent.DialogDismissed)
                }
            },
            onDismiss = {
                viewModel.onEvent(ChangePasswordRedSysUiEvent.DialogDismissed)
            }
        )
    }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Replace with your actual fields:
        OutlinedTextField(
            value = uiState.user,
            onValueChange = { viewModel.onEvent(ChangePasswordRedSysUiEvent.UserChanged(it)) },
            label = { Text("User") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = uiState.password,
            onValueChange = { viewModel.onEvent(ChangePasswordRedSysUiEvent.PasswordChanged(it)) },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = uiState.newPassword,
            onValueChange = { viewModel.onEvent(ChangePasswordRedSysUiEvent.NewPasswordChanged(it)) },
            label = { Text("New password") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = uiState.repeatNewPassword,
            onValueChange = { viewModel.onEvent(ChangePasswordRedSysUiEvent.RepeatNewPasswordChanged(it)) },
            label = { Text("Repeat new password") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            if (buttonsState.cancelVisible) {
                StyledCustomButton(
                    text = "Cancel",
                    enabled = true,
                    loading = false,
                    colors = ChangePasswordRedSysButtonStyles.cancelColors,
                    onClick = { viewModel.onEvent(ChangePasswordRedSysUiEvent.CancelClicked) },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.width(12.dp))
            if (buttonsState.acceptVisible) {
                StyledCustomButton(
                    text = "Accept",
                    enabled = buttonsState.acceptEnabled,
                    loading = buttonsState.acceptLoading,
                    colors = ChangePasswordRedSysButtonStyles.acceptColors,
                    onClick = { viewModel.onEvent(ChangePasswordRedSysUiEvent.AcceptClicked) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
I can produce a much more exact Compose port, including:
1. a full Compose `ChangePasswordRedSysRoute` with navigation and Koin injection, or  
2. a version integrated with `Scaffold` + Material 3 + `TextField` validation helpers.
