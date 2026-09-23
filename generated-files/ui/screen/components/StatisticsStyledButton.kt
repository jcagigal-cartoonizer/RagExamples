package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 352-6: import androidx.compose.foundation.BorderStroke
@Composable
fun StatisticsStyledButton(
    text: String,
    state: StatisticsButtonState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
) {
    if (!state.visible) return
    Surface(
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .wrapContentWidth()
            .clickable(enabled = state.enabled) { onClick() },
        color = state.backgroundColor,
        contentColor = state.contentColor,
        shape = shape,
        border = BorderStroke(1.dp, state.borderColor)
    ) {
        Text(
            text = text,
            modifier = Modifier,
            color = state.contentColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
