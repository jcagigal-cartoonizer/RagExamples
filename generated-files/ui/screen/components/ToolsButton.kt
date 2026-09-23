package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.ToolsButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 288-3: import androidx.compose.foundation.BorderStroke
@Composable
fun ToolsButton(
    text: String,
    colors: ToolsButtonColors,
    visible: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    if (!visible) return
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(colors.containerColor),
            contentColor = Color(colors.contentColor),
            disabledContainerColor = Color(colors.disabledContainerColor),
            disabledContentColor = Color(colors.disabledContentColor)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = Color(colors.borderColor)
        )
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.SemiBold
        )
    }
}
