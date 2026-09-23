package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.OpenPartialCustomDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.ui.window.Dialog
// # Block 421-7: import androidx.annotation.StringRes
@Composable
fun OpenPartialCustomDialog(
    state: OpenPartialDialogState,
    onDismiss: () -> Unit,
    onAccept: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Alert",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Are you sure you want to close partials?",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = onAccept) {
                        Text("Accept")
                    }
                }
            }
        }
    }
}
@Composable
fun OpenPartialCustomDialog(
    state: OpenPartialDialogState,
    onDismiss: () -> Unit,
    onAccept: () -> Unit,
) {
    val title = androidx.compose.ui.res.stringResource(state.titleRes)
    val message = androidx.compose.ui.res.stringResource(state.messageRes)
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = title, style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = message, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = onAccept) { Text("Accept") }
                }
            }
        }
    }
}
Your fragment logic had a few conditions:
Because the shared `MainActivityViewModel` is not provided in the request, I left `hasTotalizers = false` as a placeholder in the view model. If you want, you can inject a shared repository or a parent-scoped state holder and compute it there exactly as the fragment did.
To fully preserve the original fragment logic, add a shared state source for:
Then compute button state like this:
val canShowTotalizers =
    !canClose &&
    shiftStatus?.currentStatus != ifConstants.STATE_DISCONNECTED &&
    isTaximeterConnected
and
val totalizersEnabled = isTaximeterConnected && hasTotalizers
Then store it in `OpenPartialButtonsState`.
1. a full `OpenPartialComposable` wired into `NavHost`
2. a Koin module for the Compose `ViewModel`
3. a more exact Material-style clone of your `CustomButton` and `OpenPartialCustomDialog` XML visuals
