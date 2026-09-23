package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.SecurePinUiEvent
import ifac.td.taxi.ui.screen.components.SecurePinCustomDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 314-5: import androidx.compose.foundation.layout.*
@Composable
fun SecurePinContent(
    uiState: SecurePinUiState,
    onEvent: (SecurePinUiEvent) -> Unit
) {
    val titlePin = when (uiState.hasSecurePin) {
        true -> stringResource(R.string.title_update_pin)
        false -> stringResource(R.string.title_insert_pin)
        null -> stringResource(R.string.title_insert_pin)
    }
    val titleRepeat = when (uiState.hasSecurePin) {
        true -> stringResource(R.string.title_update_repeat_pin)
        false -> stringResource(R.string.title_insert_repeat_pin)
        null -> stringResource(R.string.title_insert_repeat_pin)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = titlePin, style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(
            value = uiState.pin,
            onValueChange = { onEvent(SecurePinUiEvent.PinChanged(it)) },
            label = { Text(stringResource(R.string.pin)) },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Text(text = titleRepeat, style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(
            value = uiState.pinRepeat,
            onValueChange = { onEvent(SecurePinUiEvent.PinRepeatChanged(it)) },
            label = { Text(stringResource(R.string.repeat_pin)) },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = { onEvent(SecurePinUiEvent.AcceptClicked) },
                enabled = uiState.buttonsState.accept.enabled
            ) {
                Text(stringResource(R.string.accept))
            }
            OutlinedButton(
                onClick = { onEvent(SecurePinUiEvent.CancelClicked) },
                enabled = uiState.buttonsState.cancel.enabled
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    }
}
enum class ComposeDialogButton {
    Accept,
    Cancel
}
@Composable
fun ComposeSecurePinCustomDialog(
    title: String,
    description: String?,
    buttons: List<ComposeDialogButton>,
    onDismiss: () -> Unit,
    onAccept: () -> Unit = {},
    onCancel: () -> Unit = {},
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            if (description != null) {
                Text(text = description)
            }
        },
        confirmButton = {
            if (ComposeDialogButton.Accept in buttons) {
                TextButton(onClick = onAccept) {
                    Text("OK")
                }
            }
        },
        dismissButton = {
            if (ComposeDialogButton.Cancel in buttons) {
                TextButton(onClick = onCancel) {
                    Text("Cancel")
                }
            }
        }
    )
}
