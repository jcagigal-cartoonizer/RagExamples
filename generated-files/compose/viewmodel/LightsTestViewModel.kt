package ifac.td.taxi.compose.viewmodel
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 306-5: import androidx.compose.foundation.Image
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
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
