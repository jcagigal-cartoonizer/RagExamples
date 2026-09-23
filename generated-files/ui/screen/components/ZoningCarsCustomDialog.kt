package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.ZoningCarsCustomDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.ui.window.Dialog
// # Block 608-7: import androidx.compose.foundation.background
@Composable
fun ZoningCarsCustomDialog(
    state: ZoningCarsDialogState,
    onDismiss: () -> Unit,
    onButtonClick: (String) -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(20.dp)
        ) {
            Text(
                text = state.title,
                style = MaterialTheme.typography.titleLarge,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = state.message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                state.buttons.forEach { buttonId ->
                    val label = when (buttonId) {
                        "ACCEPT" -> "Aceptar"
                        "CANCEL" -> "Cancelar"
                        "POI" -> "POI"
                        else -> buttonId
                    }
                    OutlinedButton(
                        onClick = { onButtonClick(buttonId) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = label)
                    }
                }
            }
        }
    }
}
The route keeps `NavController.navigate(...)` with your existing navigation IDs and `HomeDirections`. If you want to keep `NavDirections` more strictly, you can replace raw IDs with generated directions where available.
The Compose route uses:
A few behaviors from the fragment are domain-specific and may need integration with your shared `MainActivityViewModel`:
I mirrored them by reading from the shared VM inside the Compose VM, but in a clean Compose architecture you may prefer:
You’ll typically have:
I can also provide a second version with:
