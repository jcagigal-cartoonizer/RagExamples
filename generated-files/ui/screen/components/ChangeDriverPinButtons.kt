package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.ChangeDriverPinButtons
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 389-4: import androidx.compose.foundation.BorderStroke
@Composable
fun ChangeDriverPinButtons(
    state: ChangeDriverPinButtonsState,
    onAccept: () -> Unit,
    onCancel: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (state.accept.visible) {
            CustomStyledButton(
                visualState = state.accept,
                onClick = onAccept
            )
        }
        if (state.cancel.visible) {
            CustomStyledButton(
                visualState = state.cancel,
                onClick = onCancel
            )
        }
    }
}
@Composable
fun CustomStyledButton(
    visualState: ChangeDriverPinButtonVisualState,
    onClick: () -> Unit
) {
    val bg = Color(visualState.backgroundColor)
    val fg = Color(visualState.contentColor)
    if (visualState.borderColor != null) {
        OutlinedButton(
            onClick = onClick,
            enabled = visualState.enabled,
            border = BorderStroke(1.dp, Color(visualState.borderColor)),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = fg
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(visualState.text)
        }
    } else {
        Button(
            onClick = onClick,
            enabled = visualState.enabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = bg,
                contentColor = fg
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(visualState.text)
        }
    }
}
