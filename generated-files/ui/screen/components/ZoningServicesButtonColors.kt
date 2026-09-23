package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 389-4: import androidx.compose.foundation.BorderStroke
private val EnabledBg = Color(0xFF1E88E5)
private val DisabledBg = Color(0xFFE0E0E0)
private val EnabledText = Color.White
private val DisabledText = Color(0xFF9E9E9E)
private val Stroke = Color(0xFFBDBDBD)
@Composable
fun zoningServicesButtonColors(enabled: Boolean): ButtonColors {
    return ButtonDefaults.buttonColors(
        containerColor = if (enabled) EnabledBg else DisabledBg,
        contentColor = if (enabled) EnabledText else DisabledText,
        disabledContainerColor = DisabledBg,
        disabledContentColor = DisabledText
    )
}
@Composable
fun zoningServicesOutlinedColors(enabled: Boolean): ButtonColors {
    return ButtonDefaults.outlinedButtonColors(
        contentColor = if (enabled) EnabledBg else DisabledText,
        disabledContentColor = DisabledText
    )
}
fun zoningServicesBorder(enabled: Boolean): BorderStroke {
    return BorderStroke(1.dp, if (enabled) Stroke else DisabledBg)
}
fun zoningServicesShape() = RoundedCornerShape(8.dp)
fun zoningServicesPadding() = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
