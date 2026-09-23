package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.UserPreferencesUiEffect
import ifac.td.taxi.ui.screen.components.UserPreferencesCustomDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.ui.window.Dialog
// # Block 710-4: import androidx.compose.foundation.background
@Composable
fun UserPreferencesCustomDialog(
    dialog: UserPreferencesDialogModel,
    onDismiss: () -> Unit,
    onAction: (UserPreferencesUiEffect.DialogAction) -> Unit,
) {
    var text by remember(dialog.text) { mutableStateOf(dialog.text) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = dialog.title)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                dialog.description?.let { Text(it) }
                if (dialog.showTextField) {
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(dialog.hint ?: "") },
                        visualTransformation = if (dialog.isPin) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None
                    )
                }
            }
        },
        confirmButton = {
            if (dialog.buttons.contains(UserPreferencesDialogButton.Accept)) {
                TextButton(onClick = {
                    onAction(UserPreferencesUiEffect.DialogAction.Accept(text))
                }) {
                    Text("Accept")
                }
            }
        },
        dismissButton = {
            if (dialog.buttons.contains(UserPreferencesDialogButton.Cancel)) {
                TextButton(onClick = {
                    onAction(UserPreferencesUiEffect.DialogAction.Cancel)
                }) {
                    Text("Cancel")
                }
            }
        }
    )
}
