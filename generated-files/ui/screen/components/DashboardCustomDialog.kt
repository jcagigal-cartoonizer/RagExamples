package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.DashboardCustomDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.ui.window.Dialog
// # Block 452-6: import androidx.compose.foundation.background
@Composable
fun DashboardCustomDialog(
    dialogState: DashboardDialogState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = dialogState.title)
        },
        text = {
            Text(text = dialogState.message)
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
            ) {
                Text(dialogState.confirmText)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB00020))
            ) {
                Text(dialogState.dismissText)
            }
        }
    )
}
