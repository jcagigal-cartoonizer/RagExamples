package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.OpenPartialButtons
import ifac.td.taxi.ui.screen.components.OpenPartialUiEvent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 317-5: import androidx.compose.foundation.layout.*
@Composable
fun OpenPartialButtons(
    buttons: OpenPartialButtonsState,
    onEvent: (OpenPartialUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (buttons.back.visible) {
            StyledButton(
                text = "Back",
                state = buttons.back,
                onClick = { onEvent(OpenPartialUiEvent.OnBackClicked) },
                modifier = Modifier.weight(1f)
            )
        }
        if (buttons.print.visible) {
            StyledButton(
                text = "Print",
                state = buttons.print,
                onClick = { onEvent(OpenPartialUiEvent.OnPrintClicked) },
                modifier = Modifier.weight(1f)
            )
        }
        if (buttons.close.visible) {
            StyledButton(
                text = "Close",
                state = buttons.close,
                onClick = { onEvent(OpenPartialUiEvent.OnCloseClicked) },
                modifier = Modifier.weight(1f)
            )
        }
        if (buttons.totalizers.visible) {
            StyledButton(
                text = "Totalizers",
                state = buttons.totalizers,
                onClick = { onEvent(OpenPartialUiEvent.OnTotalizersClicked) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}
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
