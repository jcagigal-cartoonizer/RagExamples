package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.SignatureButtons
import ifac.td.taxi.ui.screen.components.SignatureCustomDialog
import ifac.td.taxi.ui.screen.components.SignatureButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 397-5: import androidx.compose.foundation.layout.*
@Composable
fun SignatureButtons(
    state: SignatureButtonsState,
    onAccept: () -> Unit,
    onClear: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SignatureButton(
            state = state.accept,
            onClick = onAccept
        )
        SignatureButton(
            state = state.clear,
            onClick = onClear
        )
    }
}
@Composable
fun SignatureButton(
    state: SignatureButtonState,
    onClick: () -> Unit,
) {
    if (!state.visible) return
    val container = if (state.enabled) state.containerColor else state.disabledContainerColor
    val content = if (state.enabled) state.contentColor else state.disabledContentColor
    Button(
        onClick = onClick,
        enabled = state.enabled && !state.isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = container,
            contentColor = content,
            disabledContainerColor = container,
            disabledContentColor = content
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = content
            )
            Spacer(Modifier.width(10.dp))
        }
        Text(text = stringResource(id = state.textResId))
    }
}
@Composable
fun SignatureCustomDialog(
    state: SignatureDialogState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(id = state.titleResId)) },
        text = { Text(text = stringResource(id = state.messageResId)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(id = state.confirmResId))
            }
        },
        dismissButton = {
            if (state.cancelResId != null) {
                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(id = state.cancelResId))
                }
            }
        }
    )
}
