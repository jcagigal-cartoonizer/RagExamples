package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.PaymentMoneiCustomDialog
import ifac.td.taxi.ui.screen.components.PaymentMoneiButtonsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 566-4: import androidx.compose.material3.ButtonColors
data class PaymentMoneiButtonsState(
    val cancel: ButtonState = ButtonState.cancel(),
    val print: ButtonState = ButtonState.print(hidden = true)
) {
    data class ButtonState(
        val text: String,
        val visible: Boolean,
        val enabled: Boolean,
        val colors: ButtonColors,
        val contentColor: Color,
    ) {
        companion object {
            @Composable
            fun cancel(
                text: String = "Cancel",
                hidden: Boolean = false,
                enabled: Boolean = true
            ): ButtonState = ButtonState(
                text = text,
                visible = !hidden,
                enabled = enabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE53935),
                    contentColor = Color.White
                ),
                contentColor = Color.White
            )
            @Composable
            fun finish(
                text: String = "Finish",
                hidden: Boolean = false,
                enabled: Boolean = true
            ): ButtonState = ButtonState(
                text = text,
                visible = !hidden,
                enabled = enabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2E7D32),
                    contentColor = Color.White
                ),
                contentColor = Color.White
            )
            @Composable
            fun print(
                text: String = "Print",
                hidden: Boolean = false,
                enabled: Boolean = true
            ): ButtonState = ButtonState(
                text = text,
                visible = !hidden,
                enabled = enabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1976D2),
                    contentColor = Color.White
                ),
                contentColor = Color.White
            )
        }
    }
}
@Composable
fun PaymentMoneiCustomDialog(
    title: String,
    message: String,
    confirmText: String,
    dismissText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(text = message)
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
        },
        containerColor = Color.White
    )
}
