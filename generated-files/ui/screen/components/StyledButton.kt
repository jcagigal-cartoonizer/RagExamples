package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
@Composable
fun StyledButton(
    text: String,
    state: ButtonUiState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = if (state.style == ButtonStyle.ENABLE) {
        ButtonDefaults.buttonColors(
            containerColor = Color(0xFF1E88E5),
            contentColor = Color.White,
            disabledContainerColor = Color(0xFF90A4AE),
            disabledContentColor = Color.White
        )
    } else {
        ButtonDefaults.buttonColors(
            containerColor = Color(0xFFB0BEC5),
            contentColor = Color.White,
            disabledContainerColor = Color(0xFFB0BEC5),
            disabledContentColor = Color.White
        )
    }
    OutlinedButton(
        onClick = onClick,
        enabled = state.enabled,
        modifier = modifier.height(48.dp),
        colors = colors,
        border = BorderStroke(
            width = 1.dp,
            color = if (state.enabled) Color(0xFF1565C0) else Color(0xFF90A4AE)
        )
    ) {
        Text(text = text)
    }
}
