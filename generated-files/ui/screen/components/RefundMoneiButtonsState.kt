package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.RefundMoneiButtonsState
import ifac.td.taxi.ui.screen.components.RefundMoneiButtonStyle
import ifac.td.taxi.ui.screen.components.RefundMoneiButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 196-3: import androidx.compose.foundation.background
data class RefundMoneiButtonsState(
    val showCancel: Boolean = true,
    val showRefund: Boolean = true,
    val cancelEnabled: Boolean = true,
    val refundEnabled: Boolean = true,
    val cancelText: String = "Cancel",
    val refundText: String = "Refund",
)
object RefundMoneiButtonStyle {
    val shape = RoundedCornerShape(10.dp)
    val primary = Color(0xFF1E88E5)
    val secondary = Color(0xFFE53935)
    val textOnPrimary = Color.White
    val textOnSecondary = Color.White
    val disabled = Color(0xFFBDBDBD)
    val contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
}
@Composable
fun RefundMoneiButton(
    text: String,
    enabled: Boolean,
    background: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Text(
        text = text,
        color = if (enabled) textColor else Color.White,
        fontSize = 16.sp,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .background(if (enabled) background else RefundMoneiButtonStyle.disabled, RefundMoneiButtonStyle.shape)
            .border(1.dp, background, RefundMoneiButtonStyle.shape)
            .clickable(enabled = enabled, onClick = onClick)
            .then(Modifier)
            .paddingCompat()
    )
}
fun Modifier.paddingCompat(): Modifier = this
> If your XML custom button had a stronger “button” look, you can swap the `Text` with a `Box`/`Surface`; I kept it minimal and Compose-native.
