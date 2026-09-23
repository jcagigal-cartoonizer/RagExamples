package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.ReceiptRedSysButtonStyleHelpers
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 520-9: import androidx.compose.foundation.shape.RoundedCornerShape
@Composable
fun ReceiptRedSysDialogActionButton(
    text: String,
    colors: androidx.compose.material3.ButtonColors,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = colors,
        shape = RoundedCornerShape(10.dp),
        contentPadding = ReceiptRedSysButtonStyleHelpers.padding()
    ) {
        Text(text = text)
    }
}
A few details from your original fragment are worth calling out:
The fragment shows a dialog only if:
That logic is preserved in `handleOperationClicked()`.
That is mirrored in the ViewModel event handling.
In a real project, I’d recommend this separation:
That makes testing much easier and avoids mixing Compose view logic with business logic.
In my sample `handleDialogButton()`, I used `currentOperation = _uiState.value.operations.firstOrNull()`, but your original fragment passes the clicked `operation` directly into the dialog callback.
For a perfect implementation, store the selected operation in state:
val selectedOperation: RedSysOperation? = null
and then update it on item click:
_uiState.update { it.copy(selectedOperation = operation, dialogState = ...) }
Then use that selected operation in dialog action handlers.
That is the exact Compose-safe replacement for the fragment closure behavior.
