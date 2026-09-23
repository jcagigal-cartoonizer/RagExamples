package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 505-8: import androidx.compose.foundation.background
@Composable
fun DispatchActionButton(
    state: ComposeButtonState,
    onClick: () -> Unit
) {
    if (!state.visible) return
    val bg = ComposeCustomButtonDefaults.backgroundColorOf(state.style.background)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(bg)
            .then(
                if (state.enabled && !state.loading) Modifier.clickable(onClick = onClick)
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (state.loading) {
            CircularProgressIndicator()
        } else {
            Text(
                text = state.label,
                color = androidx.compose.ui.graphics.Color(state.style.textColor),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
