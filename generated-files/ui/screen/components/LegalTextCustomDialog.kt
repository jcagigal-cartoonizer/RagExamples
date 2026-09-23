package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.LegalTextUiEffect
import ifac.td.taxi.ui.screen.components.LegalTextCustomDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.ui.window.Dialog
// # Block 265-5: import androidx.compose.foundation.layout.*
@Composable
fun LegalTextCustomDialog(
    title: String,
    message: String,
    confirmText: String,
    dismissText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title)
        },
        text = {
            Text(
                text = message,
                textAlign = TextAlign.Start
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        }
    )
}
The original fragment simply:
fun onAcceptClick() {
    viewModelScope.launch {
        _effects.emit(LegalTextUiEffect.NavigateToWelcome)
    }
}
But since your requirement explicitly asks for:
…the version above includes dialog handling.
