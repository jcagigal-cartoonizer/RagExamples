package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.PreReservationTripsCustomDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.ui.window.Dialog
// # Block 391-5: import androidx.compose.foundation.layout.*
@Composable
fun PreReservationTripsCustomDialog(
    title: String,
    description: String,
    acceptLabel: String,
    cancelLabel: String? = null,
    onAccept: () -> Unit,
    onCancel: () -> Unit,
    destructive: Boolean = false,
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(text = title)
        },
        text = {
            Text(text = description)
        },
        confirmButton = {
            DialogActionButton(
                text = acceptLabel,
                destructive = destructive,
                onClick = onAccept
            )
        },
        dismissButton = {
            if (cancelLabel != null) {
                DialogActionButton(
                    text = cancelLabel,
                    destructive = false,
                    secondary = true,
                    onClick = onCancel
                )
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
