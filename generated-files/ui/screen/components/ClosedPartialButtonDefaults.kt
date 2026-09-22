package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// // # Block 482-8: import androidx.compose.foundation.shape.RoundedCornerShape
// import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
object ClosedPartialButtonDefaults {
    private val Shape = RoundedCornerShape(8.dp)
    @Composable
    fun buttonColors(enabled: Boolean): ButtonColors {
        return if (enabled) {
            ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1E88E5),
                contentColor = Color.White
            )
        } else {
            ButtonDefaults.buttonColors(
                containerColor = Color(0xFFB0BEC5),
                contentColor = Color(0xFF455A64),
                disabledContainerColor = Color(0xFFB0BEC5),
                disabledContentColor = Color(0xFF455A64).copy(alpha = 0.6f)
            )
        }
    }
    @Composable
    fun colorsFor(style: ClosedPartialButtonStyle): ButtonColors {
        return when (style) {
            ClosedPartialButtonStyle.Enabled -> buttonColors(true)
            ClosedPartialButtonStyle.Disabled -> buttonColors(false)
            ClosedPartialButtonStyle.Hidden -> buttonColors(false)
        }
    }
}
@Composable
fun StyledActionButton(
    text: String,
    style: ClosedPartialButtonStyle,
    onClick: () -> Unit
) {
    if (style == ClosedPartialButtonStyle.Hidden) return
    val enabled = style == ClosedPartialButtonStyle.Enabled
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ClosedPartialButtonDefaults.colorsFor(style),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text)
    }
}
Original behavior preserved:
Because the XML and the `CustomButton.kt` implementation were not included, I matched the **behavioral logic** exactly and provided a styling layer that is easy to tune. If you share:
I can rewrite the Compose styling to mirror:
ready to paste into your project.
