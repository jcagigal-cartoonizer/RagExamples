package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 485-4: import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ifac.td.taxi.viewmodel.ComposeButtonStyle
import ifac.td.taxi.viewmodel.backgroundColor
import ifac.td.taxi.viewmodel.borderColor
import ifac.td.taxi.viewmodel.contentColor
@Composable
fun ComposeCustomButton(
    text: String,
    style: ComposeButtonStyle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val enabled = style !is ComposeButtonStyle.LoadingGreen && style !is ComposeButtonStyle.DisabledGreen
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(52.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = style.backgroundColor(),
            contentColor = style.contentColor(),
            disabledContainerColor = style.backgroundColor(),
            disabledContentColor = style.contentColor()
        ),
        border = if (style is ComposeButtonStyle.Outline) {
            BorderStroke(1.dp, style.borderColor())
        } else null
    ) {
        if (style is ComposeButtonStyle.LoadingGreen) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = style.contentColor()
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(text)
    }
}
