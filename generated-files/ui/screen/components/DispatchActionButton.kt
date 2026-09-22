package ifac.td.taxi.ui.screen.components
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import ifac.td.taxi.R
// // // import androidx.compose.foundation.background
// // import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
// import androidx.compose.runtime.Composable
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
// // # Block 505-8: import androidx.compose.foundation.background
// // // import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
// // import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
// import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
@Composable
fun DispatchActionButton(
    state: ComposeButtonState,
    onClick: () -> Unit
) {
    if (!state.visible) return
    val bg = ComposeCustomButtonDefaults.backgroundColorOf(state.style.background)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(bg)
            .then(
                if (state.enabled && !state.loading) Modifier.clickable(onClick = onClick)
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (state.loading) {
            CircularProgressIndicator()
        } else {
            Text(
                text = state.label,
                color = androidx.compose.ui.graphics.Color(state.style.textColor),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
// // # Block 550-9: import androidx.compose.foundation.background
// // // import androidx.compose.foundation.background
// // import androidx.compose.foundation.layout.*
// import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
@Composable
fun InfoDispatchInfoDispatchCustomDialog(
    state: InfoDispatchDialogState,
    onDismiss: () -> Unit,
    onButtonClick: (InfoDispatchDialogButtonType) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = state.title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (state.description.isNotBlank()) {
                    Text(text = state.description)
                }
            }
        },
        confirmButton = {
            DialogButtonsRow(state.buttons, onButtonClick)
        }
    )
}
@Composable
fun DialogButtonsRow(
    buttons: List<DialogButton>,
    onButtonClick: (InfoDispatchDialogButtonType) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        buttons.forEach { button ->
            TextButton(onClick = { onButtonClick(button.type) }) {
                Text(button.text)
            }
        }
    }
}
