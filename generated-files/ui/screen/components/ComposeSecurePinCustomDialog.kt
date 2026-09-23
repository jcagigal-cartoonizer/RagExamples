package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.SecurePinCustomDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.ui.window.Dialog
// # Block 437-7: import androidx.compose.foundation.layout.*
@Composable
fun ComposeSecurePinCustomDialog(
    title: String,
    description: String?,
    buttons: List<ComposeDialogButton>,
    onDismiss: () -> Unit,
    onAccept: () -> Unit = {},
    onCancel: () -> Unit = {},
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(text = title, style = MaterialTheme.typography.titleLarge)
                if (description != null) {
                    Text(text = description, style = MaterialTheme.typography.bodyMedium)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (ComposeDialogButton.Cancel in buttons) {
                        TextButton(onClick = onCancel) {
                            Text("Cancel")
                        }
                    }
                    if (ComposeDialogButton.Accept in buttons) {
                        TextButton(onClick = onAccept) {
                            Text("Accept")
                        }
                    }
                }
            }
        }
    }
}
