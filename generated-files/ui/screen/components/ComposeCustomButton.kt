package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 365-4: import androidx.compose.foundation.background
@Composable
fun ComposeCustomButton(
    state: ButtonUiState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!state.visible) return
    val bg = if (state.enabled) state.backgroundColor else state.disabledBackgroundColor.takeOrElse {
        state.backgroundColor.copy(alpha = 0.4f)
    }
    val fg = if (state.enabled) state.contentColor else state.disabledContentColor.takeOrElse {
        state.contentColor.copy(alpha = 0.6f)
    }
    Box(
        modifier = modifier
            .height(48.dp)
            .widthIn(min = 120.dp)
            .background(bg, RoundedCornerShape(12.dp))
            .clickable(enabled = state.enabled && !state.loading) { onClick() }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (state.loading) {
            CircularProgressIndicator(
                color = fg,
                strokeWidth = 2.dp,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Text(
                text = state.text,
                color = fg,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
fun Color.takeOrElse(default: () -> Color): Color =
    if (this == Color.Unspecified) default() else this
