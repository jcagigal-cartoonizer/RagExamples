package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.PredefinedMessageButtonType, enabled: Boolean
import ifac.td.taxi.ui.screen.components.PredefinedMessageButtons
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 384-5: import androidx.compose.foundation.BorderStroke
@Composable
fun PredefinedMessageButtons(
    state: PredefinedMessageButtonsState,
    onNewMessageClick: () -> Unit,
    onCancelClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (state.newMessage.visible) {
            ComposeStyledButton(
                text = "New message",
                state = state.newMessage,
                modifier = Modifier.weight(1f),
                onClick = onNewMessageClick
            )
        }
        if (state.cancel.visible) {
            ComposeStyledButton(
                text = "Cancel",
                state = state.cancel,
                modifier = Modifier.weight(1f),
                onClick = onCancelClick
            )
        }
    }
}
@Composable
fun ComposeStyledButton(
    text: String,
    state: PredefinedMessageButtonVisualState,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val colors = buttonColorsFor(state.type, state.enabled)
    val shape = RoundedCornerShape(14.dp)
    when (state.type) {
        PredefinedMessageButtonType.PRIMARY -> {
            Button(
                onClick = onClick,
                enabled = state.enabled,
                modifier = modifier,
                shape = shape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.container,
                    contentColor = colors.content,
                    disabledContainerColor = colors.disabledContainer,
                    disabledContentColor = colors.disabledContent
                )
            ) {
                Text(text = text)
            }
        }
        PredefinedMessageButtonType.SECONDARY,
        PredefinedMessageButtonType.GHOST -> {
            OutlinedButton(
                onClick = onClick,
                enabled = state.enabled,
                modifier = modifier,
                shape = shape,
                border = BorderStroke(1.dp, colors.container),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = colors.container,
                    disabledContentColor = colors.disabledContent
                )
            ) {
                Text(text = text)
            }
        }
        PredefinedMessageButtonType.DANGER -> {
            Button(
                onClick = onClick,
                enabled = state.enabled,
                modifier = modifier,
                shape = shape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.container,
                    contentColor = colors.content,
                    disabledContainerColor = colors.disabledContainer,
                    disabledContentColor = colors.disabledContent
                )
            ) {
                Text(text = text)
            }
        }
    }
}
data class StyledColors(
    val container: Color,
    val content: Color,
    val disabledContainer: Color,
    val disabledContent: Color
)
fun buttonColorsFor(type: PredefinedMessageButtonType, enabled: Boolean): StyledColors {
    return when (type) {
        PredefinedMessageButtonType.PRIMARY -> StyledColors(
            container = Color(0xFF1565C0),
            content = Color.White,
            disabledContainer = Color(0xFF90A4AE),
            disabledContent = Color(0xFFEEEEEE)
        )
        PredefinedMessageButtonType.SECONDARY -> StyledColors(
            container = Color(0xFF455A64),
            content = Color.White,
            disabledContainer = Color(0xFFB0BEC5),
            disabledContent = Color(0xFFEEEEEE)
        )
        PredefinedMessageButtonType.DANGER -> StyledColors(
            container = Color(0xFFD32F2F),
            content = Color.White,
            disabledContainer = Color(0xFFEF9A9A),
            disabledContent = Color(0xFFFAFAFA)
        )
        PredefinedMessageButtonType.GHOST -> StyledColors(
            container = Color(0xFF1565C0),
            content = Color(0xFF1565C0),
            disabledContainer = Color.Transparent,
            disabledContent = Color(0xFF90A4AE)
        )
    }
}
