package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.PermissionsCustomDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.ui.window.Dialog
// # Block 764-5: import androidx.compose.foundation.layout.*
@Composable
fun PermissionsCustomDialog(
    state: PermissionsDialogState,
    onDismiss: () -> Unit,
    onUsernameChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onAccept: () -> Unit,
    onChangePassword: () -> Unit,
) {
    if (!state.show) return
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(text = state.title.ifBlank { "Dialog" })
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = state.description)
                OutlinedTextField(
                    value = state.username.orEmpty(),
                    onValueChange = onUsernameChanged,
                    label = { Text(state.usernameHint.ifBlank { "Username" }) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state.password.orEmpty(),
                    onValueChange = onPasswordChanged,
                    label = { Text(state.passwordHint.ifBlank { "Password" }) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                TextButton(onClick = onChangePassword) {
                    Text("Change password")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onAccept) {
                Text("ACCEPT")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}
When permission launcher returns:
viewModel.onPermissionResult(permissionString, granted)
That updates the `PermissionsButtonsState`, replacing the old `permissionGrantedFlow` collector logic.
These can all remain in the host and be triggered by `PermissionsUiEffect`.
1. a **full `PermissionsFragment` wrapper using `ComposeView`**,  
2. a **complete Koin module for the Compose ViewModel**, and  
3. a **permission launcher bridge that exactly replaces `iMainActivity.requestPermission(...)`**.
