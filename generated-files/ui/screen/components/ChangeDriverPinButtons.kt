package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 389-4: import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
