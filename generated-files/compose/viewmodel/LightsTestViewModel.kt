package ifac.td.taxi.compose.viewmodel
import ifac.td.taxi.ui.screen.components.LightsTestButtonStyle
import ifac.td.taxi.ui.screen.components.LightsTestUiEvent
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 306-5: import androidx.compose.foundation.Image
@Composable
fun LightsTestContent(
    uiState: LightsTestUiState,
    onEvent: (LightsTestUiEvent) -> Unit
) {
    val buttons = uiState.buttonsState
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LightsImageButton(
                state = buttons.uvButton,
                onClick = { onEvent(LightsTestUiEvent.UvLightClicked) }
            )
            LightsImageButton(
                state = buttons.courtesyButton,
                onClick = { onEvent(LightsTestUiEvent.CourtesyLightClicked) }
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(text = "Beeper volume: ${uiState.beeperVolume}")
        Slider(
            value = uiState.beeperVolume.toFloat(),
            onValueChange = { onEvent(LightsTestUiEvent.BeeperVolumeChanged(it.toInt())) },
            valueRange = 0f..100f,
            enabled = true
        )
    }
}
@Composable
fun LightsImageButton(
    state: ButtonState,
    onClick: () -> Unit
) {
    androidx.compose.foundation.Image(
        painter = painterResource(id = state.iconRes),
        contentDescription = state.contentDescription,
        modifier = Modifier
            .size(LightsTestButtonStyle.ButtonSize)
            .alpha(if (state.enabled) 1f else LightsTestButtonStyle.DisabledAlpha)
            .clickable(enabled = state.enabled && state.visible) { onClick() }
            .padding(LightsTestButtonStyle.ButtonIconPadding)
    )
}
Because you didn’t include the actual XML/Kt file, I’m matching the usual structure: title, message, confirm and dismiss actions, rounded panel, dimmed background.
