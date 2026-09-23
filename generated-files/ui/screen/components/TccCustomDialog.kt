package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.TccCustomDialogCard
import ifac.td.taxi.compose.viewmodel.TccComposeViewModel
import ifac.td.taxi.ui.screen.components.TccCustomDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.ui.window.Dialog
// # Block 680-5: import androidx.compose.foundation.layout.*
@Composable
fun TccCustomDialog(
    visible: Boolean,
    title: String,
    message: String,
    confirmText: String,
    dismissText: String? = null,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!visible) return
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = { Text(text = message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmText)
            }
        },
        dismissButton = dismissText?.let {
            {
                TextButton(onClick = onDismiss) {
                    Text(it)
                }
            }
        }
    )
}
@Composable
fun TccCustomDialogCard(
    visible: Boolean,
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!visible) return
    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = title, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))
                Text(text = message, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = onConfirm) {
                        Text(confirmText)
                    }
                }
            }
        }
    }
}
In Compose, use:
That is exactly what the screen above does.
LaunchedEffect(Unit) {
    StaticConfiguration.subscriberFailPin = true
}
Example:
@Composable
fun TccRoute(
    tripId: Long?,
    navController: NavController,
    viewModel: TccComposeViewModel,
    onToast: (Int) -> Unit
) {
    TccScreen(
        tripId = tripId,
        navController = navController,
        viewModel = viewModel,
        onToast = onToast
    )
}
composable("tcc/{tripId}") { backStackEntry ->
    val tripId = backStackEntry.arguments?.getString("tripId")?.toLongOrNull()
    TccRoute(
        tripId = tripId,
        navController = navController,
        viewModel = hiltViewModel(), // or your DI equivalent
        onToast = { resId -> /* show toast */ }
    )
}
1. a **full drop-in Compose `TccRoute + ViewModel + repository/usecase wiring` example**, or  
2. a **more exact Material 2 / XML-matching button and dialog look** if you share the `custom_button.xml` and `custom_dialog.xml` layouts.
