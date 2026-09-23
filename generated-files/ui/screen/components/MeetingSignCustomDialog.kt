package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.MeetingSignCustomDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.ui.window.Dialog
// # Block 229-4: import androidx.compose.foundation.layout.*
@Composable
fun MeetingSignCustomDialog(
    title: String,
    hint: String,
    editText: String?,
    isCancellable: Boolean,
    onDismiss: () -> Unit,
    onCancel: () -> Unit,
    onAccept: (String) -> Unit,
) {
    var text by remember(editText) { mutableStateOf(TextFieldValue(editText.orEmpty())) }
    AlertDialog(
        onDismissRequest = { if (isCancellable) onDismiss() },
        title = { Text(text = title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text(hint) }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onAccept(text.text) }) {
                Text("Accept")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
        }
    )
}
