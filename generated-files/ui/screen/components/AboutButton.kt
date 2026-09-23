package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.AboutButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 378-6: import androidx.compose.foundation.BorderStroke
@Composable
fun AboutButton(
    text: String,
    enabled: Boolean,
    visible: Boolean,
    colors: AboutButtonColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!visible) return
    val shape = RoundedCornerShape(12.dp)
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.containerColor,
            contentColor = colors.contentColor,
            disabledContainerColor = colors.disabledContainerColor,
            disabledContentColor = colors.disabledContentColor
        ),
        border = colors.borderColor?.let { BorderStroke(1.dp, it) },
        modifier = modifier.height(48.dp)
    ) {
        Text(text = text)
    }
}
