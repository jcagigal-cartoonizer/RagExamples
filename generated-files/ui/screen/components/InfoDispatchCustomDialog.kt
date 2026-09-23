package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.InfoDispatchCustomDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.ui.window.Dialog
// # Block 550-9: import androidx.compose.foundation.background
@Composable
fun InfoDispatchCustomDialog(
    state: InfoDispatchDialogState,
    onDismiss: () -> Unit,
    onButtonClick: (InfoDispatchDialogButtonType) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = state.title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (state.description.isNotBlank()) {
                    Text(text = state.description)
                }
            }
        },
        confirmButton = {
            DialogButtonsRow(state.buttons, onButtonClick)
        }
    )
}
@Composable
fun DialogButtonsRow(
    buttons: List<DialogButton>,
    onButtonClick: (InfoDispatchDialogButtonType) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        buttons.forEach { button ->
            TextButton(onClick = { onButtonClick(button.type) }) {
                Text(button.text)
            }
        }
    }
}
