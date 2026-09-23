package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 389-4: import androidx.compose.foundation.BorderStroke
@Composable
fun DashboardHeaderButton(
    state: HeaderButtonState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!state.visible) return
    Button(
        onClick = onClick,
        enabled = state.enabled,
        modifier = modifier.height(44.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = state.backgroundColor,
            contentColor = state.textColor
        ),
        border = BorderStroke(1.dp, Color.Transparent)
    ) {
        Text(text = state.text, color = state.textColor)
    }
}
