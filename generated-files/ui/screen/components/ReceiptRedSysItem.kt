package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.ReceiptRedSysButtonStyleHelpers
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 397-6: import androidx.compose.foundation.clickable
@Composable
fun ReceiptRedSysItem(
    operation: RedSysOperation,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "RTS: ${operation.RTSIdentifier}", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Type: ${operation.operationType}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Result: ${operation.result}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
@Composable
fun ReceiptRedSysDialog(
    state: ReceiptRedSysDialogState,
    onDismiss: () -> Unit,
    onButtonClick: (ReceiptRedSysDialogButton) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = state.title) },
        text = { Text(text = state.description) },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                state.buttons.forEach { button ->
                    when (button) {
                        ReceiptRedSysDialogButton.DEVOLVER -> {
                            ReceiptRedSysDialogActionButton(
                                text = "Devolver",
                                colors = ReceiptRedSysButtonStyleHelpers.refundColors(),
                                onClick = { onButtonClick(button) }
                            )
                        }
                        ReceiptRedSysDialogButton.IMPRIMIR -> {
                            ReceiptRedSysDialogActionButton(
                                text = "Imprimir",
                                colors = ReceiptRedSysButtonStyleHelpers.printColors(),
                                onClick = { onButtonClick(button) }
                            )
                        }
                        ReceiptRedSysDialogButton.ACCEPT -> {
                            ReceiptRedSysDialogActionButton(
                                text = "Aceptar",
                                colors = ReceiptRedSysButtonStyleHelpers.acceptColors(),
                                onClick = { onButtonClick(button) }
                            )
                        }
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
These are closer to the custom button component and can be reused in dialog/buttons.
