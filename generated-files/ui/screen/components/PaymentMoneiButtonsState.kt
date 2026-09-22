package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
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
import ifac.td.taxi.domain.model.Trip
data class MainActivitySharedUiModel(
    val trip: Trip? = null
)
In the fragment version you used:
In Compose, keep that behavior by passing lambdas from your navigation host or activity:
PaymentMoneiScreen(
    viewModel = paymentViewModel,
    sharedViewModel = sharedUiModel,
    onNavigateBack = { navController.popBackStack() },
    onNavigateHome = { navController.navigate("home") },
    onPrintTicket = { ticket -> sharedViewModel.printTicket(ticket) },
    showHeader = { visible -> mainActivity.showHeader(visible) }
)
