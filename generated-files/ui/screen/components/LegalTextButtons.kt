package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.LegalTextButtonsState
import ifac.td.taxi.ui.screen.components.LegalTextButtons
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 218-4: import androidx.compose.foundation.layout.fillMaxWidth
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
