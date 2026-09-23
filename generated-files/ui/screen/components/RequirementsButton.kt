package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.RequirementsButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 338-4: import androidx.compose.foundation.BorderStroke
@Composable
fun RequirementsButton(
    text: String,
    state: RequirementsButtonVisualState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp)
) {
    if (!state.visible) return
    Button(
        onClick = onClick,
        enabled = state.enabled,
        modifier = modifier.defaultMinSize(minHeight = 48.dp),
        shape = shape,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = state.background,
            contentColor = state.contentColor,
            disabledContainerColor = state.background,
            disabledContentColor = state.contentColor
        ),
        border = if (state.borderColor.value != 0f) BorderStroke(1.dp, state.borderColor) else null,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = state.elevation.dp,
            pressedElevation = state.elevation.dp,
            disabledElevation = 0.dp
        )
    ) {
        Text(text = text, textAlign = TextAlign.Center)
    }
}
