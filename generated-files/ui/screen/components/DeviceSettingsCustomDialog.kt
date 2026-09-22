package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ifac.td.taxi.ui.screen.state.DeviceSettingsDialogState
@Composable
fun DeviceSettingsCustomDialog(
    state: DeviceSettingsDialogState,
    onConfirm: () -> Unit,
    onDismiss: (() -> Unit)? = null,
) {
    AlertDialog(
        onDismissRequest = { onDismiss?.invoke() },
        title = { Text(state.title) },
        text = { Text(state.message) },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(state.confirmText)
            }
        },
        dismissButton = {
            if (state.dismissText != null) {
                Button(onClick = { onDismiss?.invoke() }) {
                    Text(state.dismissText)
                }
            }
        }
    )
}
