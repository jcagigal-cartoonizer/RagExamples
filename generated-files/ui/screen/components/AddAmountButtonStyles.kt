package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.AddAmountButtons
import ifac.td.taxi.ui.screen.components.AddAmountButtonStyles
import ifac.td.taxi.ui.screen.components.AddAmountButtonsState
import ifac.td.taxi.ui.screen.components.AddAmountCustomDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 475-5: import androidx.compose.foundation.BorderStroke
object AddAmountButtonStyles {
    @Composable
    fun acceptColors(state: AddAmountButtonsState): ButtonColors {
        return ButtonDefaults.buttonColors(
            containerColor = state.acceptContainerColor,
            contentColor = state.acceptContentColor,
            disabledContainerColor = state.acceptDisabledContainerColor,
            disabledContentColor = state.acceptDisabledContentColor
        )
    }
    @Composable
    fun cancelColors(state: AddAmountButtonsState): ButtonColors {
        return ButtonDefaults.buttonColors(
            containerColor = state.cancelContainerColor,
            contentColor = state.cancelContentColor,
            disabledContainerColor = state.cancelDisabledContainerColor,
            disabledContentColor = state.cancelDisabledContentColor
        )
    }
    @Composable
    fun outlineBorder(): BorderStroke =
        BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
}
@Composable
fun AddAmountButtons(
    state: AddAmountButtonsState,
    onAccept: () -> Unit,
    onCancel: () -> Unit
) {
    Row(
        modifier = Modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (state.cancelVisible) {
            Button(
                onClick = onCancel,
                enabled = state.cancelEnabled,
                colors = AddAmountButtonStyles.cancelColors(state),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
            ) {
                Text("Cancel")
            }
        }
        if (state.acceptVisible) {
            Button(
                onClick = onAccept,
                enabled = state.acceptEnabled,
                colors = AddAmountButtonStyles.acceptColors(state),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
            ) {
                Text("Accept")
            }
        }
    }
}
@Composable
fun AddAmountCustomDialog(
    dialog: AddAmountDialogState,
    onDismiss: () -> Unit,
    onAccept: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = dialog.title,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResourceCompat(dialog.descriptionRes),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onAccept) {
                        Text(dialog.acceptText)
                    }
                }
            }
        }
    }
}
