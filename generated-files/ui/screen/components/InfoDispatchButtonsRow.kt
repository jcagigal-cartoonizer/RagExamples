package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// # Block 459-7: import androidx.compose.foundation.background
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
@Composable
fun InfoDispatchButtonsRow(
    buttons: InfoDispatchButtonsState,
    onEvent: (InfoDispatchUiEvent) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (buttons.notifications.visible) {
            DispatchActionButton(
                state = buttons.notifications,
                onClick = { onEvent(InfoDispatchUiEvent.OnClickNotifications) }
            )
        }
        DispatchActionButton(
            state = buttons.navigate,
            onClick = { onEvent(InfoDispatchUiEvent.OnNavigateDirections) }
        )
        DispatchActionButton(
            state = buttons.voiceCall,
            onClick = { onEvent(InfoDispatchUiEvent.OnClickVoiceCall) }
        )
        DispatchActionButton(
            state = buttons.print,
            onClick = { onEvent(InfoDispatchUiEvent.OnClickPrint) }
        )
        DispatchActionButton(
            state = buttons.noClient,
            onClick = { onEvent(InfoDispatchUiEvent.OnClickNoClient) }
        )
        DispatchActionButton(
            state = buttons.returnButton,
            onClick = { onEvent(InfoDispatchUiEvent.OnClickReturn) }
        )
    }
}
