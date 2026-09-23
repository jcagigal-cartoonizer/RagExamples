package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 510-5: import androidx.compose.foundation.background
@Composable
fun CustomButtonLike(
    text: String,
    style: CustomButtonStyle,
    color: CustomButtonBackgroundColor,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val enabled = style == CustomButtonStyle.ENABLE
    val background = when (color) {
        CustomButtonBackgroundColor.BLUE -> Color(0xFF1976D2)
        CustomButtonBackgroundColor.ORANGE -> Color(0xFFFF9800)
    }.let { if (enabled) it else Color(0xFF9E9E9E) }
    Box(
        modifier = modifier
            .height(48.dp)
            .background(background, RoundedCornerShape(12.dp))
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 14.sp
        )
    }
}
