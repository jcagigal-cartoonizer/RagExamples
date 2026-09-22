package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import ifac.td.taxi.viewmodel.LegalTextButtonsState
@Composable
fun LegalTextButtons(
    state: LegalTextButtonsState,
    onAccept: () -> Unit,
) {
    if (!state.acceptVisible) return
    Button(
        onClick = onAccept,
        enabled = state.acceptEnabled,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = state.acceptMinHeightDp.dp),
        shape = state.acceptButtonShape(),
        colors = state.acceptButtonColors(),
    ) {
        Text(text = "Accept")
    }
}
@Composable
fun LegalTextButtonsState.acceptButtonShape(): Shape {
    return RoundedCornerShape(acceptCornerRadiusDp.dp)
}
@Composable
fun LegalTextButtonsState.acceptButtonColors() = ButtonDefaults.buttonColors(
    containerColor = acceptBackgroundColor,
    contentColor = acceptContentColor,
    disabledContainerColor = acceptDisabledBackgroundColor,
    disabledContentColor = acceptDisabledContentColor
)
// // # Block 265-5: import androidx.compose.foundation.layout.*
// // import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
@Composable
fun LegalTextLegalTextCustomDialog(
    title: String,
    message: String,
    confirmText: String,
    dismissText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title)
        },
        text = {
            Text(
                text = message,
                textAlign = TextAlign.Start
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        }
    )
}
The original fragment simply:
fun onAcceptClick() {
    viewModelScope.launch {
        _effects.emit(LegalTextUiEffect.NavigateToWelcome)
    }
}
But since your requirement explicitly asks for:
…the version above includes dialog handling.
// // # Block 335-6: import androidx.compose.foundation.shape.RoundedCornerShape
// import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
// import androidx.compose.ui.graphics.Color
@Composable
fun LegalTextButtonsState.customFilledButtonColors() = ButtonDefaults.buttonColors(
    containerColor = acceptBackgroundColor,
    contentColor = acceptContentColor,
    disabledContainerColor = acceptDisabledBackgroundColor,
    disabledContentColor = acceptDisabledContentColor
)
@Composable
fun LegalTextButtonsState.customFilledButtonShape() =
    RoundedCornerShape(acceptCornerRadiusDp.dp)
1. a **Koin module** for `LegalTextComposeViewModel`
2. a **Fragment-hosted ComposeView version**
3. a version that matches your existing `CustomButton` styling more precisely if you paste the XML/class definitions
