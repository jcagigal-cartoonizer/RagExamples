package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.SettingsButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 525-5: import androidx.compose.foundation.background
@Composable
fun SettingsButton(
    text: String,
    style: SettingsButtonStyle,
    visible: Boolean,
    onClick: () -> Unit,
) {
    if (!visible) return
    val enabled = style == SettingsButtonStyle.Enabled
    val background = if (enabled) Color(0xFF1565C0) else Color(0xFFB0B0B0)
    val content = Color.White
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .alpha(if (enabled) 1f else 0.5f)
            .background(background, RoundedCornerShape(12.dp))
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 16.dp),
    ) {
        Text(
            text = text,
            color = content,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp)
        )
    }
}
