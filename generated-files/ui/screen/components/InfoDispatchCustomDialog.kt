package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.ui.window.Dialog
// # Block 550-9: import androidx.compose.foundation.background
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
