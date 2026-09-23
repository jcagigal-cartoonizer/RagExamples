package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.OnlineInvoiceCustomDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.ui.window.Dialog
// # Block 526-5: import androidx.compose.foundation.background
@Composable
fun OnlineInvoiceCustomDialog(
    dialogState: OnlineInvoiceDialogState,
    onDismiss: () -> Unit,
    onAccept: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(id = dialogState.titleRes),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(id = dialogState.descriptionRes),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Start
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (dialogState.buttons.any { it is OnlineInvoiceDialogButton.Accept }) {
                        TextButton(onClick = onAccept) {
                            Text(text = stringResource(id = R.string.accept))
                        }
                    }
                }
            }
        }
    }
}
A few details from the XML version are not visible in your snippet, so the Compose version above matches the behavior rather than the exact XML pixels. To make it visually identical, you’d typically port:
I can produce a pixel-closer Compose version with exact colors, sizes, and typography.
