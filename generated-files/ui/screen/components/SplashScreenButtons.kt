package ifac.td.taxi.ui.screen.components
import ifac.td.taxi.ui.screen.components.SplashScreenUiEvent
import ifac.td.taxi.ui.screen.components.SplashScreenButtons
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 340-4: import androidx.compose.foundation.BorderStroke
@Composable
fun SplashScreenButtons(
    state: SplashScreenButtonsState,
    onEvent: (SplashScreenUiEvent) -> Unit
) {
    if (!state.isVisible) return
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (state.primaryButtonVisible) {
            SplashStyledButton(
                text = "Primary",
                style = state.primaryButtonStyle,
                enabled = state.primaryEnabled,
                onClick = { onEvent(SplashScreenUiEvent.SkipRequested) }
            )
        }
        if (state.secondaryButtonVisible) {
            Spacer(Modifier.height(8.dp))
            SplashStyledButton(
                text = "Secondary",
                style = state.secondaryButtonStyle,
                enabled = state.secondaryEnabled,
                onClick = { onEvent(SplashScreenUiEvent.SkipRequested) }
            )
        }
        if (state.tertiaryButtonVisible) {
            Spacer(Modifier.height(8.dp))
            SplashStyledButton(
                text = "Tertiary",
                style = state.tertiaryButtonStyle,
                enabled = state.tertiaryEnabled,
                onClick = { onEvent(SplashScreenUiEvent.SkipRequested) }
            )
        }
    }
}
@Composable
fun SplashStyledButton(
    text: String,
    style: SplashButtonStyle,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val c = style.colors()
    if (style == SplashButtonStyle.Secondary || style == SplashButtonStyle.Tertiary) {
        OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            border = BorderStroke(1.dp, if (enabled) c.border else c.disabledContent),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = c.container,
                contentColor = c.content,
                disabledContentColor = c.disabledContent
            )
        ) {
            Text(text)
        }
    } else {
        Button(
            onClick = onClick,
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = c.container,
                contentColor = c.content,
                disabledContainerColor = c.disabledContainer,
                disabledContentColor = c.disabledContent
            )
        ) {
            Text(text)
        }
    }
}
