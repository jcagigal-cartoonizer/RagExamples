package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.foundation.shape.RoundedCornerShape
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
