package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.Composable
// // ## 9) Dialogs

// ### LocationDialog
@Composable
fun LocationDialog(
    title: String,
    confirmText: String,
    cancelText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(confirmText) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(cancelText) }
        }
    )
}

// ### RoofLightDialog
@Composable
fun RoofLightDialog(
    onConfirmOn: () -> Unit,
    onConfirmOff: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Roof light") },
        text = { Text("Choose roof light action") },
        confirmButton = {
            TextButton(onClick = onConfirmOn) { Text("Turn ON") }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onConfirmOff) { Text("Turn OFF") }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}

// ### ManualTripDialog
@Composable
fun ManualTripDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manual trip") },
        text = { Text("Confirm switch to manual hired state?") },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Accept") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// ### PendingTripsDialog
@Composable
fun PendingTripsDialog(
    onOpen: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pending trips") },
        text = { Text("Open pending trips screen?") },
        confirmButton = {
            TextButton(onClick = onOpen) { Text("Open") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}


// ## 10) Notes on behavior mapping
