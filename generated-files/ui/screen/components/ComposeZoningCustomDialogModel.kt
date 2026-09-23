package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.ZoningCustomDialog
import ifac.td.taxi.ui.screen.components.ZoningCustomDialogModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.ui.window.Dialog
// # Block 245-5: import androidx.annotation.StringRes
data class ComposeZoningCustomDialogModel(
    val title: String,
    val description: String? = null,
    val buttons: List<DialogAction> = emptyList(),
    val listOptions: Map<Int, String>? = null,
)
data class DialogAction(
    val type: ButtonTypeUi,
    val label: String,
)
@Composable
fun ComposeZoningCustomDialog(
    model: ComposeZoningCustomDialogModel,
    onDismiss: () -> Unit,
    onAction: (ButtonTypeUi, Int?) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(text = model.title, style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                model.description?.let {
                    Text(text = it, style = MaterialTheme.typography.bodyMedium)
                }
                model.listOptions?.let { options ->
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 240.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(options.entries.toList()) { entry ->
                            Surface(
                                tonalElevation = 1.dp,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onAction(ButtonTypeUi.Accept, entry.key) }
                            ) {
                                Text(
                                    text = entry.value,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                model.buttons.forEach { action ->
                    TextButton(onClick = { onAction(action.type, null) }) {
                        Text(action.label)
                    }
                }
            }
        }
    )
}
> It does not remove your existing domain logic; it reorganizes it.
