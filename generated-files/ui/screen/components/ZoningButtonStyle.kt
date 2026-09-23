package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 160-4: import androidx.annotation.DrawableRes
object ZoningButtonStyle {
    val Blue = Color(0xFF1E88E5)
    val Red = Color(0xFFE53935)
    val Orange = Color(0xFFFF9800)
    val Green = Color(0xFF43A047)
    val Gray = Color(0xFF9E9E9E)
    val White = Color.White
}
fun ComposeButtonColor.toColor(): Color = when (this) {
    ComposeButtonColor.Blue -> ZoningButtonStyle.Blue
    ComposeButtonColor.Red -> ZoningButtonStyle.Red
    ComposeButtonColor.Orange -> ZoningButtonStyle.Orange
    ComposeButtonColor.Green -> ZoningButtonStyle.Green
    ComposeButtonColor.Gray -> ZoningButtonStyle.Gray
}
@Composable
fun ZoningActionButton(
    state: ComposeActionButtonState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state is ComposeActionButtonState.Hidden) return
    val enabled = state is ComposeActionButtonState.Enabled
    val loading = state is ComposeActionButtonState.Loading
    val background = state.color.toColor()
    Box(
        modifier = modifier
            .height(64.dp)
            .background(background, RoundedCornerShape(12.dp))
            .alpha(if (enabled) 1f else 0.62f),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (loading) {
                CircularProgressIndicator(
                    color = ZoningButtonStyle.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
            } else {
                if (state.iconRes != 0) {
                    Icon(
                        painter = painterResource(state.iconRes),
                        contentDescription = null,
                        tint = ZoningButtonStyle.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                }
            }
            if (state.textRes != 0) {
                Text(
                    text = androidx.compose.ui.res.stringResource(state.textRes),
                    color = ZoningButtonStyle.White
                )
            }
        }
    }
}
