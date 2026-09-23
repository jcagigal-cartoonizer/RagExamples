package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.PaymentCustomDialog
import ifac.td.taxi.ui.screen.components.PaymentUiEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.ui.window.Dialog
// # Block 562-8: import androidx.compose.foundation.clickable
@Composable
fun PaymentCustomDialog(
    dialog: PaymentDialogState,
    onDismiss: () -> Unit,
    onButtonClick: (PaymentDialogButton, String?) -> Unit,
) {
    var text by remember { mutableStateOf(TextFieldValue("")) }
    var checked by remember { mutableStateOf(false) }
    var selectedOptionId by remember { mutableStateOf<Int?>(null) }
    AlertDialog(
        onDismissRequest = { if (dialog.isCancellable) onDismiss() },
        title = { Text(dialog.title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = dialog.description,
                    textAlign = if (dialog.centerText) androidx.compose.ui.text.style.TextAlign.Center
                    else androidx.compose.ui.text.style.TextAlign.Start
                )
                if (dialog.listOptions.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        dialog.listOptions.forEach { option ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedOptionId = option.id },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedOptionId == option.id,
                                    onClick = { selectedOptionId = option.id }
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(option.title)
                            }
                        }
                    }
                }
                if (dialog.editTextHint != null) {
                    OutlinedTextField(
                        value = text,
                        onValueChange = { if (dialog.editTextMaxLength == null || it.text.length <= dialog.editTextMaxLength) text = it },
                        placeholder = { Text(dialog.editTextHint) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (dialog.showCheckBox && dialog.checkBoxText != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = checked, onCheckedChange = { checked = it })
                        Spacer(Modifier.width(8.dp))
                        Text(dialog.checkBoxText)
                    }
                }
            }
        },
        confirmButton = {
            val hasAccept = dialog.buttons.contains(PaymentDialogButton.ACCEPT)
            if (hasAccept) {
                TextButton(onClick = {
                    val payload = if (dialog.editTextHint != null) text.text else null
                    onButtonClick(PaymentDialogButton.ACCEPT, payload)
                }) {
                    Text("Accept")
                }
            }
        },
        dismissButton = {
            if (dialog.buttons.contains(PaymentDialogButton.CANCEL)) {
                TextButton(onClick = { onButtonClick(PaymentDialogButton.CANCEL, null) }) {
                    Text("Cancel")
                }
            }
            if (dialog.buttons.contains(PaymentDialogButton.RETRY)) {
                TextButton(onClick = { onButtonClick(PaymentDialogButton.RETRY, null) }) {
                    Text("Retry")
                }
            }
            if (dialog.buttons.contains(PaymentDialogButton.OTHERS)) {
                TextButton(onClick = { onButtonClick(PaymentDialogButton.OTHERS, null) }) {
                    Text("Others")
                }
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
Example:
LaunchedEffect(Unit) {
    viewModel.uiEffect.collectLatest { effect ->
        when (effect) {
            is PaymentUiEffect.NavigateTo -> navController.navigate(effect.routeId)
            is PaymentUiEffect.ShowToast -> showToast(effect.messageRes)
            else -> Unit
        }
    }
}
To fully replicate the fragment you’ll want to move these into state/effects gradually:
The pattern should be:
